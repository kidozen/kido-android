package kidozen.client;

import android.os.AsyncTask;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
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
import kidozen.client.internal.Constants;
import kidozen.client.internal.SNIConnectionManager;
import kidozen.client.internal.Utilities;

/**
 * Base class for the following services:
 * - Storage
 * - Queue
 * - PubSub
 * - Log
 * - Configuration
 * - Files
 * - Mail
 * - DataSource
 * - Service
 */
public class KZService {
    private Boolean mStrictSSL = true;
    private boolean mProcessAsStream = false;
    protected String mName;
    protected String mEndpoint;
    protected KidoZenUser mUserIdentity = new KidoZenUser();
    protected KidoZenUser mApplicationIdentity = new KidoZenUser();

    String mPassiveClientId;
    String mActiveProvider;
    String mActiveUsername;
    String mActivePassword;

    Hashtable<String, String> mRequestHeaders = new Hashtable<String, String>();

    private int mDefaultServiceTimeoutInSeconds = 10;

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

    public Boolean getStrictSSL() {
        return mStrictSSL;
    }

    public void setStrictSSL(Boolean strictSSL) {
        mStrictSSL = strictSSL;
    }

    public boolean shouldProcessAsStream() {
        //System.out.println("KZService, shouldProcessAsStream: " + String.valueOf(mProcessAsStream));
        return mProcessAsStream;
    }

    public void setmProcessAsStream(boolean mProcessAsStream) {
        this.mProcessAsStream = mProcessAsStream;
    }

    public int getDefaultServiceTimeoutInSeconds() {
        return mDefaultServiceTimeoutInSeconds;
    }

    public void setDefaultServiceTimeoutInSeconds(int mDefaultServiceTimeoutInSeconds) {
        this.mDefaultServiceTimeoutInSeconds = mDefaultServiceTimeoutInSeconds;
    }

    public class KZServiceAsyncTask extends AsyncTask<String, Void, ServiceEvent> {
        HashMap<String, String> mQueryStringParameters = null;
        HashMap<String, String> mHeaders = null;
        KZHttpMethod mHttpMethod = null;
        ServiceEventListener mServiceEventCallback = null;
        ServiceEvent mFinalServiceEvent = null;

        String mStringMessage;
        InputStream mStreamMessage;

        Boolean mBypassSSLValidation;
        private SNIConnectionManager mSniManager;
        private String mContentType;


        public KZServiceAsyncTask(KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, ServiceEventListener callback, Boolean bypassSSLValidation)
        {
            mHttpMethod = method;
            mServiceEventCallback = callback;
            mBypassSSLValidation = bypassSSLValidation;
            if (params!=null) {
                mQueryStringParameters = params;
            }
            if (headers==null) {
                mRequestHeaders = new Hashtable<String, String>();
            }
            else {
                Iterator<Map.Entry<String, String>> it = headers.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> pairs = it.next();
                    mRequestHeaders.put(pairs.getKey(), pairs.getValue());
                    it.remove();
                }
            }
            //System.out.println("KZService, ctor.");
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
            CreateAuthHeaderValue(new KZServiceEvent<String>() {
                @Override
                public void Fire(String token) {
                    mRequestHeaders.put(Constants.AUTHORIZATION_HEADER, token);
                    //System.out.println("KZService, onPreExecute, got token, " + token);
                }
            });
            //System.out.println("KZService, onPreExecute");
        }

        @Override
        protected ServiceEvent doInBackground(String... params) {
            //System.out.println("KZService, doInBackground");
            int statusCode = HttpStatus.SC_BAD_REQUEST;
            try
            {
                String  url = params[0];
                //System.out.println("KZService, doInBackground,  method:" + mHttpMethod);
                //System.out.println("KZService, doInBackground,  url:" + url);

                if (shouldProcessAsStream()) {
                    mSniManager = new SNIConnectionManager(url, mStreamMessage, mRequestHeaders, mQueryStringParameters, mBypassSSLValidation);
                    OutputStream response = mSniManager.ExecuteHttpAsStream(mHttpMethod);
                    createCallbackResponseForStream(statusCode, response);
                }
                else {
                    //System.out.println("KZService, doInBackground,  mStringMessage:" + mStringMessage);
                    //System.out.println("KZService, doInBackground,  mRequestHeaders:" + mRequestHeaders.toString());
                    //System.out.println("KZService, doInBackground,  mQueryStringParameters:" + mQueryStringParameters);
                    //System.out.println("KZService, doInBackground,  mBypassSSLValidation:" + mBypassSSLValidation);

                    mSniManager = new SNIConnectionManager(url, mStringMessage, mRequestHeaders, mQueryStringParameters, mBypassSSLValidation);
                    if (mServiceEventCallback instanceof ServiceResponseHandler) {
                        Utilities.DispatchServiceStartListener((ServiceResponseHandler)mServiceEventCallback);
                    }
                    Hashtable<String, String> response = mSniManager.ExecuteHttp(mHttpMethod);
                    String body = response.get("responseBody");
                    statusCode = Integer.parseInt(response.get("statusCode"));
                    mContentType = response.get("contentType").toLowerCase();

                    createCallbackResponse(statusCode, body);
                    body = (body==null || body.equals("") || body.equals("null") ? "" : body);

                    //System.out.println("KZService, doInBackground,  body:" + body);
                    //System.out.println("KZService, doInBackground,  status:" + response.get("statusCode"));
                    //System.out.println("KZService, doInBackground,  content:" + response.get("contentType"));

                    if (body == "") {
                        mFinalServiceEvent = new ServiceEvent(this, statusCode, body, body);
                    }
                    else if (mContentType.contains("application/json")) {
                            Object json = new JSONTokener(body).nextValue();
                            if (json instanceof JSONObject) {
                                //System.out.println("KZService, doInBackground,  Setting a new JSONObject" );

                                JSONObject theObject = new JSONObject(body);
                                mFinalServiceEvent = new ServiceEvent(this, statusCode, body, theObject);
                            }
                            else if (json instanceof JSONArray) {
                                //System.out.println("KZService, doInBackground,  Setting a new JSONArray" );

                                JSONArray theObject = new JSONArray(body);
                                mFinalServiceEvent = new ServiceEvent(this, statusCode, body, theObject);
                            }
                        }
                        else {
                            //System.out.println("KZService, doInBackground,  Setting a new String" );
                            mFinalServiceEvent = new ServiceEvent(this, statusCode, body, response.get("responseMessage"));
                        }
                }
            }
            catch(Exception e)
            {
                //System.out.println("KZService, doInBackground, Exception: " + e.getMessage().toLowerCase() );

                String exMessage = (e.getMessage()==null ? "Unexpected error" : e.getMessage().toString());
                mFinalServiceEvent = new ServiceEvent(this, statusCode, exMessage, null,e);
            }
            return mFinalServiceEvent;
        }

        private void createCallbackResponse(int statusCode, String body) {
            if (statusCode>= HttpStatus.SC_MULTIPLE_CHOICES) {
                String exceptionMessage = (body!=null ? body : "Unexpected HTTP Status Code: " + statusCode);
                mFinalServiceEvent = new ServiceEvent(this, statusCode, exceptionMessage, null, new Exception(exceptionMessage));
            }
        }

        private void createCallbackResponseForStream(int statusCode, OutputStream response) {
            if (mSniManager.LastResponseCode>= HttpStatus.SC_MULTIPLE_CHOICES) {
                String exceptionMessage = (mSniManager.LastResponseBody!=null ? mSniManager.LastResponseBody : "Unexpected HTTP Status Code: " + mSniManager.LastResponseCode);
                mFinalServiceEvent = new ServiceEvent(this, statusCode, exceptionMessage, null, new Exception(exceptionMessage));
            }
            mFinalServiceEvent = new ServiceEvent(this, mSniManager.LastResponseCode, null, response);
        }

        @Override
        protected void onPostExecute(ServiceEvent result) {
            //System.out.println("KZService, onPostExecute");
            if (mServiceEventCallback instanceof ServiceResponseHandler) {
                //System.out.println("KZService, doInBackground,  onPostExecute. Is ServiceResponseHandler");
                dispatchServiceResponseListener(result, (ServiceResponseHandler) mServiceEventCallback);
            }
            else {
                //System.out.println("KZService, doInBackground,  onPostExecute. NOT ServiceResponseHandler");
                mServiceEventCallback.onFinish(result);
            }
        }

        private void dispatchServiceResponseListener(final ServiceEvent e,final ServiceResponseHandler callback) {
            if (e.StatusCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
                callback.onError(e.StatusCode, e.Body);
            }
            else {
                try {
                    if (mContentType.contains("application/json")) {
                        Object json = new JSONTokener(e.Body).nextValue();
                        if (json instanceof JSONObject) {
                            JSONObject theObject = new JSONObject(e.Body);
                            callback.onSuccess(e.StatusCode, theObject);
                        }
                        else if (json instanceof JSONArray) {
                            JSONArray o = (JSONArray) e.Response;
                            callback.onSuccess(e.StatusCode, o);
                        }
                    }
                    else { callback.onSuccess(e.StatusCode, e.Body); }
                } catch (JSONException e1) {
                    callback.onError(e.StatusCode, e1.getMessage());
                }
            }
        }

    }


}

