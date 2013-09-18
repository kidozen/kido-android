package kidozen.client.authentication;
import android.os.AsyncTask;
import android.util.Log;
import kidozen.client.*;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLDecoder;
import java.util.*;


public class AuthenticationManager extends AsyncTask<Void, Void, Void> {
	public static HashMap<String, KidoZenUser> _securityTokens= new HashMap<String, KidoZenUser>();
	public Boolean Authenticated = false;
	public boolean bypassSSLValidation = false;

	protected String _securityTokenKey="";
	protected KidoZenUser _kidozenUser = null;
	protected String _tennantMarketPlace, _application;
	private IIdentityProvider _currentIdentityProvider ;


	Map<String, JSONObject> _identityProviders;
	String _username, _password;
	String _providerKey;
	String tokeFromAuthService;
	Boolean mustGetTokenFromIP = false;
	Boolean hasIPToken = false;
	String errorDescription = "";
	String _ipEndpoint;
	String _applicationScope ;
	String _authServiceScope ;
	String _authServiceEndpoint ;

    ObservableUser _tokenUpdater;

	private String TAG="AuthenticationManager";
	private ServiceEventListener _authCallback;

	public AuthenticationManager(String tennantMarketPlace, String application, Map<String, JSONObject> identityProviders, String applicationScope, String authServiceScope, String authServiceEndpoint, String ipEndpoint, ObservableUser tokenUpdater){
		_tennantMarketPlace = tennantMarketPlace;
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
        for(Iterator<Map.Entry<String,KidoZenUser>> it=_securityTokens.entrySet().iterator();it.hasNext();){
            Map.Entry<String, KidoZenUser> entry = it.next();
            if (entry.getValue().Token.equalsIgnoreCase(_username)) {
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
		_securityTokenKey = Utilities.createHash(String.format("%s%s%s%s%s", _tennantMarketPlace, _application, providerKey.toLowerCase(), username, password));
		Log.d(TAG,String.format("Create hash key for Authentication: %s",_securityTokenKey));

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
        return _kidozenUser;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		_kidozenUser =  _securityTokens.get(_securityTokenKey);
		mustGetTokenFromIP = (null==_kidozenUser);
	}
	@Override
	protected Void doInBackground(Void... params) {
		try {
			if (mustGetTokenFromIP) {
				Log.d(TAG,String.format("Token is not in the identity cache"));
				JSONObject provider = _identityProviders.get(_providerKey);
				_currentIdentityProvider = (IIdentityProvider) provider.get("instance");
				_currentIdentityProvider.Configure(provider);
				_currentIdentityProvider.Initialize(_username, _password, provider.get("authServiceScope").toString());
                URI endpoint = new URI(provider.get("endpoint").toString());

                _currentIdentityProvider.RequestToken(endpoint, new KZAction<String>() {
					@SuppressWarnings("deprecation")
					public void onServiceResponse(String response) throws Exception {
						try
                        {
							tokeFromAuthService = requestKidoZenToken(response);
							JSONObject token = new JSONObject(tokeFromAuthService);
							tokeFromAuthService = token.get("rawToken").toString();
							Log.d(TAG, String.format("Kidozen auth token:%s", tokeFromAuthService));
							hasIPToken =true;
							//
							Log.d(TAG,String.format("Building KidoZen User object using the token"));
							String rawToken = URLDecoder.decode(tokeFromAuthService);
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
							_kidozenUser = new KidoZenUser();
							_kidozenUser.Token = tokeFromAuthService;
							_kidozenUser.Claims = tokenClaims;
							_kidozenUser.SetExpiration(Long.parseLong(tokenClaims.get("ExpiresOn")));
							if (tokenClaims.get("role")!=null)
                            {
								_kidozenUser.Roles = Arrays.asList(tokenClaims.get("role").split(","));
							}
						}
                        catch (Exception e)
                        {
							Authenticated = false;
							hasIPToken =false;
							Log.e(TAG,"Error parsing the IP token" + e.getStackTrace().toString());
							errorDescription = "Error trying to call KidoZen Authentication Service Endpoint:" + e.getMessage();
						}	
					}
				});
			}
			else
			{
				Log.d(TAG,String.format("Getting token from the identity cache"));
				tokeFromAuthService = _kidozenUser.Token;
				_tokenUpdater.TokenUpdated(_kidozenUser);
				Authenticated = true;
				hasIPToken = true;
			}
		}
        catch (Exception e)
        {
			hasIPToken = false;
			Authenticated = false;
			errorDescription = "Error calling the specified Identity Provider." + e.getMessage();
            this.cancel(false);
		}
		return null;
	}
	@Override
	protected void onPostExecute (Void result)
	{
		super.onPostExecute(result);

		if (hasIPToken)
        {
			try
            {
                int status = HttpStatus.SC_OK;
                String token = this.tokeFromAuthService;
                KidoZenUser user = _kidozenUser;
                //TODO : Remove for environments where AuthV2 is present
                if (_kidozenUser.Claims.get("system")==null && _kidozenUser.Claims.get("http://schemas.kidozen.com/usersource")==null)
                {
                    this.Authenticated=false;
                    status = HttpStatus.SC_NOT_FOUND;
                    user = null;
                    token = "User is not authenticated";
                }
				else
                {
                    _securityTokens.put(_securityTokenKey, _kidozenUser);
				    _tokenUpdater.TokenUpdated(_kidozenUser);
                    this.Authenticated = true;
                }

                if (_authCallback!=null)
                {
					_authCallback.onFinish(new ServiceEvent(this,status, token, user));
				}
			}
            catch (Exception e)
            {
				Authenticated = false;
				errorDescription = "Error creating the Kidozen user identity:" + e.getMessage();
				if (_authCallback!=null)
                {
					_authCallback.onFinish(new ServiceEvent(this, HttpStatus.SC_BAD_REQUEST, errorDescription, null));
				}
			}
		}
		else
        {
			if (_authCallback!=null)
            {
				Authenticated = false;
				_authCallback.onFinish(new ServiceEvent(this,HttpStatus.SC_BAD_REQUEST,errorDescription, null));
			}
        }
	}

    @Override
    protected void onCancelled()
    {
        if (_authCallback!=null)
        {
            Authenticated = false;
            _authCallback.onFinish(new ServiceEvent(this,HttpStatus.SC_BAD_REQUEST,errorDescription, null));
        }
    }

	private String requestKidoZenToken(final String response) throws Exception
	{ 
		String body = null;
		try 
		{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();  
			nameValuePairs.add(new BasicNameValuePair("wrap_scope", _applicationScope));  
			nameValuePairs.add(new BasicNameValuePair("wrap_assertion_format", "SAML")); 
			nameValuePairs.add(new BasicNameValuePair("wrap_assertion", response));
			String message = Utilities.getQuery(nameValuePairs);

            SNIConnectionManager sniManager = new SNIConnectionManager(_authServiceEndpoint, message, null, null, bypassSSLValidation);
            Hashtable<String, String>  authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
			body = authResponse.get("responseBody");
		} catch (Exception e) {
			throw e;
		}
		return body;
	}
}
