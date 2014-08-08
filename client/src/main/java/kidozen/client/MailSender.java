package kidozen.client;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;
import kidozen.client.internal.SyncHelper;
import kidozen.client.internal.Utilities;

/**
 * Mail service interface
 *
 * @author kidozen
 * @version 1.00, April 2013
 */
public class MailSender extends KZService {
    public KZApplication Application;
    private String KEY = "MailSender";
    private MailSender mSelf;
    /**
     * Constructor
     *
     * @param endpoint The Configuration service endpoint
     */
    public MailSender(String mailsender, String provider , String username, String pass, String clientId, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(mailsender, "", provider, username, pass, clientId, userIdentity, applicationIdentity);
        mSelf = this;
    }

    /**
     * @param mail The Email message to send
     * @param callback The callback with the result of the service call
     */
    public void Send(final Mail mail, final ServiceEventListener callback) throws ExecutionException, InterruptedException, JSONException {
        if ( mail == null ) throw new IllegalArgumentException(Utilities.GetInvalidParameterMessage("mail"));
        if ( mail.from()==null || mail.from().isEmpty() ) throw new IllegalArgumentException(Utilities.GetInvalidParameterMessage("mail.from"));
        if ( mail.to()==null || mail.to().isEmpty() ) throw new IllegalArgumentException(Utilities.GetInvalidParameterMessage("mail.to"));

        JSONObject message= new JSONObject(mail.GetHashMap());
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();

        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

        if ( mail.Attachments!=null)
        {
            Uploader upd =  new Uploader(mSelf, mail, headers, params, callback, mEndpoint, mail.Attachments);
            upd.execute();
        }
        else
        {
            new KZServiceAsyncTask(KZHttpMethod.POST,params,headers,message,callback, getStrictSSL()).execute(mEndpoint);
        }
    }

    public boolean Send(Mail mail) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper<String>(this, "Send", Mail.class);
        helper.Invoke(new Object[]{mail});
        return (helper.getStatusCode() == HttpStatus.SC_OK);
    }

    private class Uploader extends AsyncTask {
        public static final String HTTP_METHOD_POST = "POST";
        public static final String CONNECTION_HEADER = "Connection";
        public static final String CONTENT_TYPE_HEADER = "Content-Type";
        public static final String CONNECTION_KEEP_ALIVE = "Keep-Alive";
        public static final String MULTIPART_FORM_DATA_BOUNDARY = "multipart/form-data;boundary=";
        private static final String TAG = "UPLOAD_ATTACH";
        private String lineEnd = "\r\n";
        private String twoHyphens = "--";
        private String boundary =  "*****";
        private int maxBufferSize = 1*1024*1024;


        private List<String> _attachments;
        private ArrayList<String> _ids = new ArrayList<String>();
        private String _baseUrl;
        private String _authHeaderValue;

        private KZService _mailService;
        private Mail _message;
        private HashMap<String, String> _headers;
        private HashMap<String, String> _params;
        private ServiceEventListener _callback;
        private boolean _sent = false;

        public Uploader(KZService service, Mail message, HashMap<String, String> headers, HashMap<String, String> params, ServiceEventListener callback, String baseUrl, List<String> attachments)
        {
            _mailService = service;
            _message = message;
            _headers = headers;
            _params = params;
            _callback = callback;

            _baseUrl = baseUrl;
            _attachments = attachments;

        }
        private AbstractMap.SimpleEntry<String, String> getNameAndPath(String fullFilePath)
        {
            String[] paths = fullFilePath.split("/");
            String file = paths[paths.length-1];
            String path = fullFilePath.replace("/" + file, "");

            return  new AbstractMap.SimpleEntry<String, String>(file,path);
        }
        @Override
        protected void onPreExecute() {
            CreateAuthHeaderValue(new KZServiceEvent<String>() {
                @Override
                public void Fire(String token) {
                    _authHeaderValue = token;
                }
            });
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            try
            {
                for(Iterator<String> it = _attachments.iterator(); it.hasNext();)
                {
                    String f = doFileUpload(it.next());
                    if (_sent)
                        _ids.add(f);
                    else
                        throw new Exception("couldn't attach file");
                }
                return _ids;
            }
            catch (Exception e)
            {
                _callback.onFinish(new ServiceEvent(this,HttpStatus.SC_INTERNAL_SERVER_ERROR,e.getMessage(),null,e));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o){
            super.onPostExecute(o);
            try
            {
                if (!_sent || _ids.size()<=0)
                    throw new Exception("couldn't attach file");

                JSONObject jo = getJsonObjectMessage();

                _mailService.ExecuteTask(mEndpoint, KZHttpMethod.POST, _params, _headers, _callback, jo, getStrictSSL());

            } catch (Exception e) {
                _callback.onFinish(new ServiceEvent(this, HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), null, e));
            }
        }

        private JSONObject getJsonObjectMessage() throws JSONException {
            JSONArray array = new JSONArray();
            for(Iterator<String> it = _ids.iterator(); it.hasNext();)
            {
                String itm = new JSONArray(it.next()).getString(0);
                array.put(itm);
            }
            JSONObject msg = new JSONObject(_message.GetHashMap());
            // Weird problems in a real android device:
            // - Cannot use JSONObject.put to add a new Array property, it throws an "NoSuchMethodError" exception with a reference to virtual method call
            // - Cannot use HashMap.put: It serializes the array as following: "[ "..." ]" instead of ["..."]
            // So I decided to do this string manipulation
            String messageAsString = msg.toString();
            String arrayAsString = array.toString();
            messageAsString = messageAsString.replace("}", ", attachments=" + arrayAsString + "}");
            return new JSONObject(messageAsString);
        }

        private String doFileUpload(String fileName) throws IOException {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            FileInputStream fileInputStream = new FileInputStream(new File(fileName) );
            AbstractMap.SimpleEntry<String, String> nameAndPath = getNameAndPath(fileName);

            URL url = new URL(_baseUrl + "/" + "attachments");
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(HTTP_METHOD_POST);
            conn.setRequestProperty(CONNECTION_HEADER, CONNECTION_KEEP_ALIVE);
            conn.setRequestProperty(Constants.AUTHORIZATION_HEADER, _authHeaderValue);
            conn.setRequestProperty(CONTENT_TYPE_HEADER, MULTIPART_FORM_DATA_BOUNDARY + boundary);
            dos = new DataOutputStream( conn.getOutputStream() );
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + nameAndPath.getKey() + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0)
            {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            fileInputStream.close();
            dos.flush();
            dos.close();

            String id= null;
            if (conn.getResponseCode()>= HttpStatus.SC_BAD_REQUEST)
            {
                _sent = false;
                id = Utilities.convertStreamToString(conn.getErrorStream());
            }
            else
            {
                _sent = true;
                id = Utilities.convertStreamToString(conn.getInputStream());
            }
            Log.i(TAG,"raw service response :" + id);
            return  id;
        }
    }
}
