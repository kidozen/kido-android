package kidozen.client;

import android.os.AsyncTask;
import android.util.Log;

import kidozen.client.authentication.AuthenticationManager;
import kidozen.client.authentication.KidoZenUser;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class KZService implements Observer
{
    private static final String LOGTAG = "KZService";

    public Boolean BypassSSLVerification = false;
	public KidoZenUser KidozenUser = new KidoZenUser();

    protected static final String ACCEPT = "Accept";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String CONTENT_TYPE = "content-type";
    protected static final String AUTHORIZATION_HEADER = "Authorization";
    protected String _application = "";
	protected String _tenantMarketPlace;
    protected ObservableUser tokenUpdater = new ObservableUser();

    private static final String KEY = "KZService" ;

    String ipEndpoint;
	String applicationScope ;
	String authServiceScope ;
	String authServiceEndpoint ;
    private AuthenticationManager am;
    private String _provider;
    private String _username;
    private String _password;
    private ServiceEventListener _authenticateCallback;

    public String CreateAuthHeaderValue()
	{
            long delay = this.KidozenUser.GetExpirationInMiliseconds();
            if (delay<=0)
            {
                am.RemoveCurrentTokenFromCache(KidozenUser.Token);
                am.Authenticate(_provider,_username, _password, new ServiceEventListener() {
                    @Override
                    public void onFinish(ServiceEvent e) {
                        KidozenUser = ((KidoZenUser) e.Response);
                    }
                });
            }
        return "WRAP access_token=\"" + KidozenUser.Token +"\"";
    }

    protected void ExecuteTask(String url, KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, ServiceEventListener callback, Boolean bypassSSLValidation)
    {
        new KZServiceAsyncTask(method,params,headers,callback, bypassSSLValidation).execute(url);
    }

    protected void ExecuteTask(String url, KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, ServiceEventListener callback, JSONObject message,Boolean bypassSSLValidation)
    {
        new KZServiceAsyncTask(method,params,headers,message,callback, bypassSSLValidation).execute(url);
    }


    @Override
    public void update(Observable observable, Object data) {
        Log.d(KEY, "token updated");
        this.KidozenUser = (KidoZenUser) data;
    }

    protected void SetAuthenticateParameters(String marketplace, String application, Map<String, JSONObject> providers, String scope, String authScope, String authServiceEndpoint, String ipEndpoint) {
        am = new AuthenticationManager(marketplace, application, providers, scope,  authScope,  authServiceEndpoint,  ipEndpoint, this.tokenUpdater);
        am.bypassSSLValidation = BypassSSLVerification;
    }

    public void SetCredentials(String providerKey, String username, String password, ServiceEventListener e ){
        this._provider = providerKey;
        this._username = username;
        this._password = password;
        this._authenticateCallback = e;
    }

    protected void Authenticate(String providerKey, String username, String password, ServiceEventListener e ) {
        this._provider = providerKey;
        this._username = username;
        this._password = password;
        this._authenticateCallback = e;

        this.KidozenUser = am.Authenticate(providerKey, username, password, _authenticateCallback);
    }

    private class KZServiceAsyncTask extends AsyncTask<String, Void, ServiceEvent> {
        HashMap<String, String> _params = null;
        HashMap<String, String> _headers = null;
        KZHttpMethod _method= null;
        ServiceEventListener _callback= null;
        ServiceEvent _event = null;
        JSONObject _message;
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

        @Override
        protected ServiceEvent doInBackground(String... params) {
            int statusCode = HttpStatus.SC_BAD_REQUEST;
            try
            {
                String body = null;
                Hashtable<String, String> requestProperties = new Hashtable<String, String>();
                Iterator<Map.Entry<String, String>> it = _headers.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> pairs = it.next();
                    requestProperties.put(pairs.getKey(), pairs.getValue());
                    it.remove();
                }

                String  url = params[0];
                Hashtable<String, String> response =null;
                String msg = _message == null ? null : _message.toString();
                _sniManager = new SNIConnectionManager(url, msg, requestProperties, _params, _bypassSSLValidation);
                response = _sniManager.ExecuteHttp(_method);

                statusCode = Integer.parseInt(response.get("statusCode"));
                body = response.get("responseBody");
                body = (body==null || body.equals("") || body.equals("null") ? "" : body);

                if (statusCode>= HttpStatus.SC_MULTIPLE_CHOICES) {
                    String exceptionMessage = (body!=null ? body : "Unexpected HTTP Status Code: " + statusCode);
                    throw new Exception(exceptionMessage);
                }

                if (body.replace("\n", "").toLowerCase().equals(response.get("responseMessage").toLowerCase())) {
                    _event = new ServiceEvent(this, statusCode, body, response.get("responseMessage"));
                }

                else if (body.indexOf("[")>-1) {
                    JSONArray theObject = new JSONArray(body);
                    _event = new ServiceEvent(this, statusCode, body, theObject);
                }

                else {
                    JSONObject theObject = new JSONObject(body);
                    _event = new ServiceEvent(this, statusCode, body, theObject);
                }
            }
            catch(Exception e)
            {
                String exMessage = (e.getMessage()==null ? "Unexpected error" : e.getMessage().toString());
                _event = new ServiceEvent(this, statusCode, exMessage, null,e);
            }
            return _event;
        }

        @Override
        protected void onPostExecute(ServiceEvent result) {
            _callback.onFinish(result);
        }
    }
}

