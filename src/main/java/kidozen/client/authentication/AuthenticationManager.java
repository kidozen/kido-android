package kidozen.client.authentication;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kidozen.client.KZAction;
import kidozen.client.KZHttpMethod;
import kidozen.client.ObservableUser;
import kidozen.client.SNIConnectionManager;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.Utilities;


public class AuthenticationManager extends AsyncTask<Void, Void, Void> {
	public static HashMap<String, KidoZenUser> SecurityTokensCache = new HashMap<String, KidoZenUser>();
	public Boolean Authenticated = false;
	public boolean bypassSSLValidation = false;

	protected String _kidoZenUserHashKey = "";
    protected String _kidoZenApplicationHashKey = "";
	protected KidoZenUser _kidoZenUser = null;
    protected KidoZenUser _applicationUser = null;

    protected String _tenantMarketPlace, _application;
	private IIdentityProvider _currentIdentityProvider ;

	Map<String, JSONObject> _identityProviders;
	String _username, _password;
	String _providerKey;
	String _userTokeFromAuthService;
	Boolean _mustGetTokenFromIP = false;
	Boolean _hasIPToken = false;
	String _errorDescription = "";
	String _ipEndpoint;
	String _applicationScope ;
	String _authServiceScope ;
	String _authServiceEndpoint ;
    String _domain ;
    String _applicationKey ;
    String _oauthTokenEndpoint;
    String _applicationName;

    ObservableUser _tokenUpdater;

	private final String TAG="AuthenticationManager";
	private ServiceEventListener _authCallback;

	public AuthenticationManager(String tenantMarketPlace, String application, Map<String, JSONObject> identityProviders, String applicationScope, String authServiceScope, String authServiceEndpoint, String ipEndpoint, ObservableUser tokenUpdater){
		_tenantMarketPlace = tenantMarketPlace;
		_application = application;
		_identityProviders = identityProviders;
		_applicationScope = applicationScope ;
		_authServiceScope = authServiceScope;
		_authServiceEndpoint = authServiceEndpoint ;
		_ipEndpoint = ipEndpoint;
		_tokenUpdater = tokenUpdater;
	}

    public void RemoveCurrentTokenFromCache(String _username)
    {
        for(Iterator<Map.Entry<String,KidoZenUser>> it= SecurityTokensCache.entrySet().iterator();it.hasNext();){
            Map.Entry<String, KidoZenUser> entry = it.next();
            if (entry.getValue().Token.equalsIgnoreCase(_username)) {
                Log.d(TAG, String.format("Token removed from token cache"));
                it.remove();
            }
        }
    }

	public KidoZenUser Authenticate(final String providerKey,final String username, final String password,final ServiceEventListener callback)
	{
		_authCallback = callback;
		_providerKey = providerKey;
		_username = username;
		_password = password;
		_kidoZenUserHashKey = Utilities.createHash(String.format("%s%s%s%s%s", _tenantMarketPlace, _application, providerKey.toLowerCase(), username, password));
		Log.d(TAG,String.format("Create hash key for Authentication: %s", _kidoZenUserHashKey));

		try
        {
			this.execute().get();
		}
        catch (Exception e)
        {
			if (callback!=null) {
				Authenticated = false;
                ServiceEvent se = new ServiceEvent(this,HttpStatus.SC_BAD_REQUEST,"InterruptedException", null);
                se.Exception = e;
				callback.onFinish(se);
			}
		}
        return _kidoZenUser;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		_kidoZenUser =  SecurityTokensCache.get(_kidoZenUserHashKey);
		_mustGetTokenFromIP = (null== _kidoZenUser);
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			if (_mustGetTokenFromIP) {
				Log.d(TAG,String.format("Token is not in the identity cache"));
                if (_applicationKey!="")
                    getTokenForApplication();
                else
                    getTokenFromIP();
			}
			else
			{
				Log.d(TAG,String.format("Getting token from the identity cache"));
				_userTokeFromAuthService = _kidoZenUser.Token;
				_tokenUpdater.TokenUpdated(_kidoZenUser);
				Authenticated = true;
				_hasIPToken = true;
			}
		}
        catch (Exception e)
        {
			_hasIPToken = false;
			Authenticated = false;
			_errorDescription = "Error calling the specified Identity Provider." + e.getMessage();
            this.cancel(false);
		}
		return null;
	}

    private void getTokenFromIP() throws Exception {
        JSONObject provider = _identityProviders.get(_providerKey);
        _currentIdentityProvider = (IIdentityProvider) provider.get("instance");
        _currentIdentityProvider.Configure(provider);
        _currentIdentityProvider.Initialize(_username, _password, provider.get("authServiceScope").toString());
        URI endpoint = new URI(provider.get("endpoint").toString());
        try {
            _currentIdentityProvider.RequestToken(endpoint, new KZAction<String>() {
                @SuppressWarnings("deprecation")
                public void onServiceResponse(String response) throws Exception {
                    Log.d(TAG, String.format("Got auth token from Identity Provider"));
                    _userTokeFromAuthService = requestKidoZenToken(response);
                    _kidoZenUser = createKidoZenUser(_userTokeFromAuthService);
                }
            });
        }
        catch (Exception e)
        {
            Authenticated = false;
            _hasIPToken =false;
            _errorDescription = "Error trying to call KidoZen Authentication Service Endpoint. " + e.getMessage();
        }
    }

    private KidoZenUser createKidoZenUser(String tokenAsString) throws JSONException {
        JSONObject token = new JSONObject(tokenAsString);
        String rawTokenAsString = token.get("rawToken").toString();
        Log.d(TAG, String.format("Got Kidozen auth token from AuthService"));
        _hasIPToken =true;
        //
        Log.d(TAG,String.format("Building KidoZen User object using the token"));
        String rawToken = URLDecoder.decode(_userTokeFromAuthService);
        String[] claims = rawToken.split("&");
        Hashtable<String, String> tokenClaims = new Hashtable<String, String>();
        for (int i = 0; i < claims.length; i++)
        {
            String[] keyValue = claims[i].split("=");
            String keyName = keyValue[0];
            int indexOfClaimKeyword= keyName.indexOf("/claims/");
            if (indexOfClaimKeyword>-1)
            {
                keyName = keyValue[0].substring(indexOfClaimKeyword + "/claims/".length(), keyName.length());
            }
            String v ;
            try
            {
                v=  keyValue[1];
            }
            catch (IndexOutOfBoundsException e)
            {
                v="";
            }
            tokenClaims.put(keyName,v);
        }
        KidoZenUser user = new KidoZenUser();
        user.Token = rawTokenAsString;
        user.Claims = tokenClaims;
        user.SetExpiration(Long.parseLong(tokenClaims.get("ExpiresOn")));
        if (tokenClaims.get("role")!=null)
        {
            user.Roles = Arrays.asList(tokenClaims.get("role").split(","));
        }
        return user;
    }

    @Override
	protected void onPostExecute (Void result) {
		super.onPostExecute(result);
		if (_hasIPToken)
        {
			try
            {
                int status = HttpStatus.SC_OK;
                String token = this._userTokeFromAuthService;
                KidoZenUser user = null;
                //AuthV2
                if ( _kidoZenUser.Claims.get("http://schemas.kidozen.com/usersource")==null)
                {
                    Log.d(TAG,String.format("user has no access to KidoZen"));

                    this.Authenticated = false;
                    status = HttpStatus.SC_NOT_FOUND;
                    token = "User is not authenticated";
                }
				else
                {
                    Log.d(TAG,String.format("user has access to KidoZen"));

                    SecurityTokensCache.put(_kidoZenUserHashKey, _kidoZenUser);
				    _tokenUpdater.TokenUpdated(_kidoZenUser);
                    user = _kidoZenUser;
                    this.Authenticated = true;
                }
                if (_authCallback!=null) {
					_authCallback.onFinish(new ServiceEvent(this,status, token, user));
				}
			}
            catch (Exception e) {
				Authenticated = false;
				_errorDescription = "Error creating the Kidozen user identity:" + e.getMessage();
				if (_authCallback!=null) {
					_authCallback.onFinish(new ServiceEvent(this, HttpStatus.SC_BAD_REQUEST, _errorDescription,null, e));
				}
			}
		}
		else  {
			if (_authCallback!=null) {
				Authenticated = false;
				_authCallback.onFinish(new ServiceEvent(this,HttpStatus.SC_BAD_REQUEST, _errorDescription, null));
			}
        }
	}

    @Override
    protected void onCancelled()
    {
        if (_authCallback!=null)
        {
            Authenticated = false;
            _authCallback.onFinish(new ServiceEvent(this,HttpStatus.SC_BAD_REQUEST, _errorDescription, null));
        }
    }


	private String requestKidoZenToken(final String response) throws Exception
	{ 
		String body = null;
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();  
			nameValuePairs.add(new BasicNameValuePair("wrap_scope", _applicationScope));  
			nameValuePairs.add(new BasicNameValuePair("wrap_assertion_format", "SAML")); 
			nameValuePairs.add(new BasicNameValuePair("wrap_assertion", response));
			String message = Utilities.getQuery(nameValuePairs);

            SNIConnectionManager sniManager = new SNIConnectionManager(_authServiceEndpoint, message, null, null, bypassSSLValidation);
            Hashtable<String, String>  authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
			body = authResponse.get("responseBody");
		}
        catch (Exception e) {
			throw e;
		}
		return body;
	}

    public void SetAuthenticateApplication(String domain, String oauthTokenEndpoint, String applicationKey, String applicationName) {
        _domain = domain;
        _applicationKey = applicationKey;
        _oauthTokenEndpoint = oauthTokenEndpoint;
        _applicationName = applicationName;
    }

    private void getTokenForApplication() {
        HashMap<String, String> params = null;
        HashMap<String, String> headers = new HashMap<String, String>();

        try {
            String message = new JSONObject()
                    .put("client_id",_domain)
                    .put("client_secret", _applicationKey)
                    .put("grant_type", "client_credentials")
                    .put("scope", _applicationName).toString();

            SNIConnectionManager sniManager = new SNIConnectionManager(_oauthTokenEndpoint, message, null, null, bypassSSLValidation);
            Hashtable<String, String>  authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
            String body = authResponse.get("responseBody");
            _applicationUser = createKidoZenUser(body);
            _kidoZenApplicationHashKey = Utilities.createHash(String.format("%s%s", _domain, _applicationKey));
            SecurityTokensCache.put(_kidoZenApplicationHashKey, _applicationUser);
        }
        catch (JSONException je) {
            Log.e("Error", je.getMessage());
        }
        catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
        finally {
        }

        //_kidoApplication.ExecuteTask(oauthTokenEndpoint, KZHttpMethod.POST, params, headers,  _onAuthenticateApplication, message, bypassSSLValidation);

    }
}
