package kidozen.client;

import android.os.AsyncTask;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import kidozen.client.authentication.IdentityManager;
import kidozen.client.authentication.KidoZenUser;
import kidozen.client.authentication.KidoZenUserIdentityType;
import kidozen.client.internal.SNIConnectionManager;

public class KZService {
    public Boolean StrictSSL = true;
    public boolean ProcessAsStream = false;
    protected String mName;
    protected String mEndpoint;
    protected KidoZenUser mUserIdentity = new KidoZenUser();
    protected KidoZenUser mApplicationIdentity = new KidoZenUser();

    String mPassiveClientId;
    String mActiveProvider;
    String mActiveUsername;
    String mActivePassword;

    public KZService() {

    }

    /*
    * Before sending the AuthHeader value to the requested service, it checks if the auth timeout
    * has been reached and executes authentication to get a new token again.
    * */
    public void CreateAuthHeaderValue(final KZServiceEvent<String> cb)
    {
        // User Identity is the higher priority
        if (mUserIdentity != null) {
            ServiceEventListener formatAuthHeaderCallback = new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    mUserIdentity = ((KidoZenUser) e.Response);
                    cb.Fire(String.format("WRAP access_token=\"%s\"", mUserIdentity.Token));
                }
            };
            if (mUserIdentity.IdentityType==KidoZenUserIdentityType.USER_IDENTITY) {
                IdentityManager.getInstance().GetRawToken(mActiveProvider, mActiveUsername, mActivePassword, formatAuthHeaderCallback);
            }
            else {
                IdentityManager.getInstance().GetToken(mUserIdentity,mPassiveClientId, formatAuthHeaderCallback);
            }
        }
        else {
            IdentityManager.getInstance().GetRawToken(mApplicationIdentity.HashKey, new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                mApplicationIdentity = ((KidoZenUser) e.Response);
                cb.Fire(String.format("WRAP access_token=\"%s\"", mApplicationIdentity.Token));
                }
            });
        }
    }

    /**
     * Constructor
     *
     * You should not create a new instances of this constructor. Instead use the Storage[""] method of the KZApplication object.
     * @param endpoint The service endpoint
     * @param name The name of the service to be created
     */
    public KZService(String endpoint, String name,String provider , String username, String pass, String clientSecret,  KidoZenUser userIdentity, KidoZenUser applicationIdentity)
    {
        mPassiveClientId = clientSecret;
        mActiveProvider = provider;
        mActiveUsername = username;
        mActivePassword = pass;
        mEndpoint = endpoint;
        mName = name;
        this.mApplicationIdentity = applicationIdentity;
        this.mUserIdentity = userIdentity;
    }

    public void ExecuteTask(String url, KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, ServiceEventListener callback, JSONObject message,Boolean bypassSSLValidation)
    {
        new KZServiceAsyncTask(method,params,headers,message,callback, bypassSSLValidation).execute(url);
    }

    public class KZServiceAsyncTask extends AsyncTask<String, Void, ServiceEvent> {
        HashMap<String, String> _params = null;
        HashMap<String, String> _headers = null;
        KZHttpMethod _method= null;
        ServiceEventListener _callback= null;
        ServiceEvent _event = null;
        JSONObject _message;

        InputStream _messageAsStream;

        Boolean _bypassSSLValidation;
        private SNIConnectionManager _sniManager;


        public KZServiceAsyncTask(KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, ServiceEventListener callback, Boolean bypassSSLValidation)
        {
            _headers = headers;
            _method = method;
            _callback = callback;
            _bypassSSLValidation = bypassSSLValidation;
            if (params!=null) {
                _params = params;
            }
        }
        public KZServiceAsyncTask(KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, JSONObject message, ServiceEventListener callback, Boolean bypassSSLValidation)
        {
            this(method,params, headers,  callback, bypassSSLValidation);
            _message = message;
        }

        public KZServiceAsyncTask(KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, InputStream message, ServiceEventListener callback, Boolean bypassSSLValidation) {
            this(method,params, headers,  callback, bypassSSLValidation);
            _messageAsStream = message;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ServiceEvent doInBackground(String... params) {
            int statusCode = HttpStatus.SC_BAD_REQUEST;
            try
            {
                Hashtable<String, String> requestProperties = new Hashtable<String, String>();
                Iterator<Map.Entry<String, String>> it = _headers.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> pairs = it.next();
                    requestProperties.put(pairs.getKey(), pairs.getValue());
                    it.remove();
                }

                String  url = params[0];
                if (ProcessAsStream)
                {
                    _sniManager = new SNIConnectionManager(url, _messageAsStream, requestProperties, _params, _bypassSSLValidation);
                }
                else
                {
                    String msg = _message == null ? null : _message.toString();
                    _sniManager = new SNIConnectionManager(url, msg, requestProperties, _params, _bypassSSLValidation);
                }

                if (ProcessAsStream)
                {
                    OutputStream response = _sniManager.ExecuteHttpAsStream(_method);
                    if (_sniManager.LastResponseCode>= HttpStatus.SC_MULTIPLE_CHOICES) {
                        String exceptionMessage = (_sniManager.LastResponseBody!=null ? _sniManager.LastResponseBody : "Unexpected HTTP Status Code: " + _sniManager.LastResponseCode);
                        throw new Exception(exceptionMessage);
                    }
                    _event = new ServiceEvent(this, _sniManager.LastResponseCode, null, response);
                }
                else
                {
                    Hashtable<String, String> response = _sniManager.ExecuteHttp(_method);
                    String body = response.get("responseBody");
                    statusCode = Integer.parseInt(response.get("statusCode"));
System.out.println("======>" + body);
System.out.println("======>" +statusCode);
                    if (statusCode>= HttpStatus.SC_MULTIPLE_CHOICES) {
                        String exceptionMessage = (body!=null ? body : "Unexpected HTTP Status Code: " + statusCode);
                        throw new Exception(exceptionMessage);
                    }
                    body = (body==null || body.equals("") || body.equals("null") ? "" : body);
                    // TODO: fix this based on content-type response
                    if (body.replace("\n", "").toLowerCase().equals(response.get("responseMessage").toLowerCase()))
                    {
                        _event = new ServiceEvent(this, statusCode, body, response.get("responseMessage"));
                    }
                    else
                        if (body.indexOf("[")==0)
                        {
                            JSONArray theObject = new JSONArray(body);
                            _event = new ServiceEvent(this, statusCode, body, theObject);
                        }
                        else
                            if (body.indexOf("{")==0)
                            {
                                JSONObject theObject = new JSONObject(body);
                                _event = new ServiceEvent(this, statusCode, body, theObject);
                            }
                            else
                            {
                                _event = new ServiceEvent(this, statusCode, body, response.get("responseMessage"));
                            }
                }

            }
            catch(Exception e)
            {
System.out.println("======>" + e.getMessage());
                String exMessage = (e.getMessage()==null ? "Unexpected error" : e.getMessage().toString());
                _event = new ServiceEvent(this, statusCode, exMessage, null,e);
            }
            return _event;
        }

        @Override
        protected void onPostExecute(ServiceEvent result) {
            if (_callback!=null) {
                _callback.onFinish(result);
            }
        }

    }

}

