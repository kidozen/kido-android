package kidozen.client;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import kidozen.client.authentication.AuthenticationManager;
import kidozen.client.authentication.KidoZenUser;

public class KZService implements Observer
{
    public String ApplicationKey = Constants.UNSET_APPLICATION_KEY;

    public Boolean BypassSSLVerification = false;
	public KidoZenUser KidozenUser = new KidoZenUser();
    public KidoZenUser KidoZenAnonymousUser = new KidoZenUser();

    protected static final String ACCEPT = "Accept";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String CONTENT_TYPE = "content-type";
    protected static final String AUTHORIZATION_HEADER = "Authorization";
    protected ObservableUser tokenUpdater = new ObservableUser();

    private static final String KEY = "KZService" ;
    private AuthenticationManager _authenticationManager;
    private ServiceEventListener _authenticateCallback;

    String _ipEndpoint;
	String _applicationScope;
	String _authServiceScope;
	String _domain;
    String _oauthTokenEndpoint;
    String _authServiceEndpoint;
    String _applicationName;
    String _tenantMarketPlace;
    String _provider;
    String _username;
    String _password;

    private Map<String, JSONObject> _providers;
    private String _scope;
    private String _authScope;


    public boolean ProcessAsStream = false;


    /*
    * REMARK!!!
    * This method was created because there was a problen with the 'proactive' authentication. Now before sending the
    * AuthHeader value to the requested service, it checks if the auth timeout has been reached and executes authentication
    * to get a new token again.
    * */
    public String CreateAuthHeaderValue()
	{
        long delay = this.KidozenUser.GetExpirationInMilliseconds();
        if (delay<=0)
        {
            _authenticationManager.RemoveCurrentTokenFromCache(KidozenUser.Token);
            _authenticationManager.Authenticate(_provider, _username, _password, new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    KidozenUser = ((KidoZenUser) e.Response);
                }
            });
        }
        return "WRAP access_token=\"" + KidozenUser.Token +"\"";
    }

    public String CreateAnonymousAuthHeaderValue()
    {
        long delay = this.KidoZenAnonymousUser.GetExpirationInMilliseconds();
        if (delay<=0)
        {
            _authenticationManager.RemoveCurrentTokenFromCache(KidoZenAnonymousUser.Token);
            _authenticationManager.AuthenticateApplication(_domain, _oauthTokenEndpoint, ApplicationKey, _applicationName, new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    KidoZenAnonymousUser = ((KidoZenUser) e.Response);
                }
            });
        }
        return "WRAP access_token=\"" + KidoZenAnonymousUser.Token +"\"";
    }


    protected void ExecuteTask(String url, KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, ServiceEventListener callback, Boolean bypassSSLValidation)
    {
        new KZServiceAsyncTask(method,params,headers,callback, bypassSSLValidation).execute(url);
    }

    protected void ExecuteTask(String url, KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, ServiceEventListener callback, JSONObject message,Boolean bypassSSLValidation)
    {
        new KZServiceAsyncTask(method,params,headers,message,callback, bypassSSLValidation).execute(url);
    }

    public void ExecuteTask(String url, KZHttpMethod method, HashMap<String,String> params, HashMap<String,String> headers, ServiceEventListener callback, InputStream message, Boolean bypassSSLValidation) {
        new KZServiceAsyncTask(method,params,headers,message,callback, bypassSSLValidation).execute(url);
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.d(KEY, "token updated");
        this.KidozenUser = (KidoZenUser) data;
    }

    protected void SetAuthenticateParameters(String marketplace, String application, Map<String, JSONObject> providers, String scope, String authScope, String authServiceEndpoint, String ipEndpoint) {
        _tenantMarketPlace = marketplace;
        _applicationName = application;
        _providers = providers;
        _scope = scope;
        _authScope = authScope;
        _authServiceEndpoint = authServiceEndpoint;
        _ipEndpoint = ipEndpoint;

        _authenticationManager = new AuthenticationManager(_tenantMarketPlace, _applicationName, _providers, _scope,  _authScope, _authServiceEndpoint, _ipEndpoint, this.tokenUpdater);
        _authenticationManager.bypassSSLValidation = BypassSSLVerification;
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

        this.KidozenUser = _authenticationManager.Authenticate(_provider, _username, _password, _authenticateCallback);
    }

    protected void AuthenticateApplication(String domain, String oauthTokenEndpoint,String  applicationKey, String applicationName, ServiceEventListener e) {
        _authenticationManager.AuthenticateApplication(domain,oauthTokenEndpoint,applicationKey, applicationName, e);
    }

    protected void SignOut()
    {
        _authenticationManager.RemoveCurrentTokenFromCache(KidozenUser.Token);
    }

    public void RenewAuthenticationToken(ServiceEventListener callback) {
        _authenticationManager.RemoveCurrentTokenFromCache(KidozenUser.Token);
        _authenticationManager = new AuthenticationManager(_tenantMarketPlace, _applicationName, _providers, _scope,  _authScope, _authServiceEndpoint, _ipEndpoint, this.tokenUpdater);
        this.KidozenUser = _authenticationManager.Authenticate(_provider, _username, _password, callback);
    }

    private class KZServiceAsyncTask extends AsyncTask<String, Void, ServiceEvent> {
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

