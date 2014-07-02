package kidozen.client;

import android.os.AsyncTask;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

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

            if (mUserIdentity.IdentityType==KidoZenUserIdentityType.USER_IDENTITY) {
                IdentityManager.getInstance().GetRawToken(mActiveProvider, mActiveUsername, mActivePassword, new ServiceEventListener() {
                    @Override
                    public void onFinish(ServiceEvent e) {
                        mUserIdentity = ((KidoZenUser) e.Response);
                        cb.Fire(String.format("WRAP access_token=\"%s\"", mUserIdentity.Token));
                    }
                });
            }
            else  {
                IdentityManager.getInstance().GetToken(mUserIdentity, new ServiceEventListener() {
                    @Override
                    public void onFinish(ServiceEvent e) {
                        cb.Fire(String.format("WRAP access_token=\"%s\"", e.Body));
                    }
                });
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

        String mStringMessage;
        InputStream mStreamMessage;

        Boolean _bypassSSLValidation;
        private SNIConnectionManager mSniManager;


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

        public KZServiceAsyncTask(KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, String message, ServiceEventListener callback, Boolean bypassSSLValidation)
        {
            this(method, params, headers,  callback, bypassSSLValidation);
            mStringMessage = message;
        }


        public KZServiceAsyncTask(KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, JSONObject message, ServiceEventListener callback, Boolean bypassSSLValidation)
        {
            this(method, params, headers,  callback, bypassSSLValidation);
            mStringMessage = message.toString();
        }

        public KZServiceAsyncTask(KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, InputStream message, ServiceEventListener callback, Boolean bypassSSLValidation) {
            this(method, params, headers,  callback, bypassSSLValidation);
            mStreamMessage = message;
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
                System.out.println("***** =>> method:" + _method);
                System.out.println("***** =>> url:" + url);
                System.out.println("***** =>> isStream:" + ProcessAsStream);

                if (ProcessAsStream) {
                    mSniManager = new SNIConnectionManager(url, mStreamMessage, requestProperties, _params, _bypassSSLValidation);
                    OutputStream response = mSniManager.ExecuteHttpAsStream(_method);
                    if (mSniManager.LastResponseCode>= HttpStatus.SC_MULTIPLE_CHOICES) {
                        String exceptionMessage = (mSniManager.LastResponseBody!=null ? mSniManager.LastResponseBody : "Unexpected HTTP Status Code: " + mSniManager.LastResponseCode);
                        throw new Exception(exceptionMessage);
                    }
                    _event = new ServiceEvent(this, mSniManager.LastResponseCode, null, response);
                }
                else {
                    mSniManager = new SNIConnectionManager(url, mStringMessage, requestProperties, _params, _bypassSSLValidation);
                    Hashtable<String, String> response = mSniManager.ExecuteHttp(_method);
                    String body = response.get("responseBody");
                    statusCode = Integer.parseInt(response.get("statusCode"));
                    if (statusCode>= HttpStatus.SC_MULTIPLE_CHOICES) {
                        String exceptionMessage = (body!=null ? body : "Unexpected HTTP Status Code: " + statusCode);
                        throw new Exception(exceptionMessage);
                    }
                    body = (body==null || body.equals("") || body.equals("null") ? "" : body);
                    System.out.println("***** =>> body:" + body);
                    System.out.println("***** =>> status:" + response.get("statusCode"));
                    System.out.println("***** =>> content:" + response.get("contentType"));

                    Object json = new JSONTokener(body).nextValue();
                    if (json instanceof JSONObject) {
                        JSONObject theObject = new JSONObject(body);
                        System.out.println("***** =>> is JSONObject ");
                        _event = new ServiceEvent(this, statusCode, body, theObject);
                    }
                    else
                        if (json instanceof JSONArray) {
                            JSONArray theObject = new JSONArray(body);
                            System.out.println("***** =>> is JSONArray ");
                            _event = new ServiceEvent(this, statusCode, body, theObject);
                        }
                        else {
                            System.out.println("***** =>> is ??? ");

                            _event = new ServiceEvent(this, statusCode, body, response.get("responseMessage"));
                        }
                    //}

                    //if (response.get("contentType").indexOf("[text/plain]")>-1) {
                    //    _event = new ServiceEvent(this, statusCode, body, response.get("responseMessage"));
                    //}

                    System.out.println("--------------------------------------------------");

                }
            }
            catch(Exception e)
            {
                System.out.println("* Exception: =>>" + e.getCause() + " <<== *");

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

