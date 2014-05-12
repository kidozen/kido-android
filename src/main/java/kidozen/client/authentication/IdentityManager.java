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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import kidozen.client.KZAction;
import kidozen.client.KZHttpMethod;
import kidozen.client.internal.SNIConnectionManager;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.internal.Utilities;

/**
 * Created by christian on 4/29/14.
 */
public class IdentityManager {
    protected static final String ACCEPT = "Accept";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String CONTENT_TYPE = "content-type";

    private HashMap<String, JSONObject> _tokensCache = new HashMap<String, JSONObject>();
    private final String TAG = "IdentityManager";
    private boolean _strictSSL;
    private JSONObject _authConfig;
    private String _lastErrorDescription;
    private KidoZenUser _kidoZenUser;
    private KidoZenUser _applicationUser;

    String ipEndpoint = null;
    private long DEFAULT_TIMEOUT = 30;

    private static IdentityManager INSTANCE = null;

    // Private constructor suppresses
    private IdentityManager(){}

    private static void createInstance() {
        if (INSTANCE == null) {
            // synchronized to avoid possible  multi-thread issues
            synchronized(IdentityManager.class) {
                // must check for null again
                if (INSTANCE == null) {
                    INSTANCE = new IdentityManager();
                }
            }
        }
    }

    public static IdentityManager getInstance() {
        createInstance();
        return INSTANCE;
    }

    public void Setup(JSONObject authConfig, boolean strictSSL){
        _strictSSL = !strictSSL;
        _authConfig = authConfig;
    }

    public String Authenticate(final String providerName,final String username, final String password,final ServiceEventListener callback) {
        String cacheKey = Utilities.createHash(String.format("%s%s%s", providerName, username, password));
        String rawToken = null;
        try {
            JSONObject cacheItem = _tokensCache.get(cacheKey);
            if (cacheItem!=null) {
                rawToken = cacheItem.getString("rawToken");
                KidoZenUser usr = (KidoZenUser) _tokensCache.get(cacheKey).get("user");
                usr.PulledFromCache = true;
                invokeCallback(callback, rawToken, usr);
            }
            else {
                String applicationScope =_authConfig.getString("applicationScope");
                String authServiceEndpoint =_authConfig.getString("authServiceEndpoint");
                IIdentityProvider identityProvider = createIP(providerName, username, password);
                FederatedIdentity id = new FederatedIdentity(identityProvider);

                Object[] response = id.execute(ipEndpoint, authServiceEndpoint,applicationScope).get();
                if (response[1]!=null)
                {
                    invokeCallbackWithException(callback, (Exception) response[1]);
                }
                else
                {
                    String token = response[0].toString();
                    addToTokensCache(cacheKey, token, KidoZenUserIdentityType.USER_IDENTITY);
                    rawToken = getRawToken(token);
                    invokeCallback(callback, rawToken, _tokensCache.get(cacheKey).get("user"));
                }
            }
        }
        catch (InterruptedException e) {
            invokeCallbackWithException(callback,e);
        }
        catch (ExecutionException e) {
            invokeCallbackWithException(callback,e);
        }
        catch (JSONException e) {
            invokeCallbackWithException(callback,e);
        }
        catch (Exception e) {
            invokeCallbackWithException(callback,e);
        }
        finally {
            return rawToken;
        }
    }

    private IIdentityProvider createIP(String providerName,String username, String password) throws Exception {
        String authServiceScope =_authConfig.getString("authServiceScope");
        String ipProtocol = null;

        JSONObject identityProviders = _authConfig.getJSONObject("identityProviders");
        Iterator<String> providersIterator = identityProviders.keys();
        while(providersIterator.hasNext()){
            String providerIdentifier = providersIterator.next();
            JSONObject ip = identityProviders.getJSONObject(providerIdentifier);
            if (providerIdentifier.equalsIgnoreCase(providerName)) {
                ipEndpoint =  ip.getString("endpoint");
                ipProtocol = ip.getString("protocol");
            }
        }
        if (ipProtocol==null && ipEndpoint==null) {
            throw new Exception("provider not found");
        }

        IIdentityProvider ip = null;
        if (ipProtocol.equalsIgnoreCase("wrapv0.9")) {
            ip = new WRAPv09IdentityProvider();
            ((WRAPv09IdentityProvider)ip).bypassSSLValidation= _strictSSL;
        }
        else {
            ip = new ADFSWSTrustIdentityProvider();
            ((ADFSWSTrustIdentityProvider)ip).bypassSSLValidation= _strictSSL;
        }
        ip.Initialize(username, password, authServiceScope);
        return ip;
    }

    public String Authenticate(final String key, final ServiceEventListener callback) {
        KeyIdentity ki = new KeyIdentity();
        String rawToken = null;
        try {
            JSONObject cacheItem = _tokensCache.get(key);
            if (cacheItem!=null) {
                rawToken = cacheItem.getString("rawToken");
                KidoZenUser usr = (KidoZenUser) _tokensCache.get(key).get("user");
                usr.PulledFromCache = true;
                invokeCallback(callback, rawToken, usr);
            }
            else {
                String oauthTokenEndpoint = _authConfig.getString("oauthTokenEndpoint");
                String domain = _authConfig.getString("domain");
                String applicationScope = _authConfig.getString("applicationScope");
                Object[] response = ki.execute(oauthTokenEndpoint,domain,applicationScope,key).get();
                if (response[1]!=null)
                {
                    invokeCallbackWithException(callback, (Exception) response[1]);
                }
                else
                {
                    //Quick and dirty fix to use 'createKidoZenUser function
                    String token = response[0].toString().replace("access_token","rawToken");
                    addToTokensCache(key, token, KidoZenUserIdentityType.APPLICATION_IDENTITY);
                    rawToken = getRawToken(token);
                    invokeCallback(callback, rawToken, _tokensCache.get(key).get("user"));
                }
            }
        } catch (JSONException e) {
            invokeCallbackWithException(callback, e);
        } catch (InterruptedException e) {
            invokeCallbackWithException(callback, e);
        } catch (ExecutionException e) {
            invokeCallbackWithException(callback, e);
        }
        finally {
            return rawToken;
        }
    }

    public void GetRawToken(final String providerName,final String username, final String password,final ServiceEventListener callback) {
        String cacheKey = Utilities.createHash(String.format("%s%s%s", providerName, username, password));
        String rawToken = null;
        try {
            JSONObject cacheItem = _tokensCache.get(cacheKey);
            if (cacheItem!=null)
            {
                rawToken = cacheItem.getString("rawToken");
                KidoZenUser usr = (KidoZenUser) _tokensCache.get(cacheKey).get("user");
                if (usr.HasExpired())
                {
                    _tokensCache.remove(cacheKey);
                    this.Authenticate(providerName, username, password, callback);
                }
                else invokeCallback(callback, rawToken, usr);
            }
            else this.Authenticate(providerName, username, password, callback);
        }
        catch (Exception e) {
            invokeCallbackWithException(callback, e);
        }
    }

    public void GetRawToken(final String hashKey, final ServiceEventListener callback) {
        String rawToken = null;
        try {
            JSONObject cacheItem = _tokensCache.get(hashKey);
            if (cacheItem!=null)
            {
                rawToken = cacheItem.getString("rawToken");
                KidoZenUser usr = (KidoZenUser) _tokensCache.get(hashKey).get("user");

                if (usr.HasExpired())
                {
                    _tokensCache.remove(hashKey);
                    this.Authenticate(hashKey, callback);
                }
                else invokeCallback(callback, rawToken, usr);
            }
            else this.Authenticate(hashKey, callback);
        }
        catch (Exception e) {
            invokeCallbackWithException(callback, e);
        }
    }

    private KidoZenUser createKidoZenUser(String tokenAsString, KidoZenUserIdentityType userIdentity) throws JSONException {
        JSONObject token = new JSONObject(tokenAsString);
        String rawTokenAsString = token.get("rawToken").toString();
        Log.d(TAG, String.format("Got Kidozen auth token from AuthService"));
        //
        Log.d(TAG,String.format("Building KidoZen User object using the token"));
        String rawToken = URLDecoder.decode(tokenAsString);
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
        user.IdentityType = userIdentity;
        user.Token = rawTokenAsString;
        user.Claims = tokenClaims;
        user.SetExpiration(Long.parseLong(tokenClaims.get("ExpiresOn")));
        if (tokenClaims.get("role")!=null)
        {
            user.Roles = Arrays.asList(tokenClaims.get("role").split(","));
        }
        return user;
    }

    private void addToTokensCache(String cacheKey, String token, KidoZenUserIdentityType userIdentity) throws JSONException {
        String rawToken = getRawToken(token);
        KidoZenUser user = createKidoZenUser(token, userIdentity);
        user.HashKey = cacheKey;
        JSONObject cacheItem = new JSONObject()
                .put("user", user)
                .put("rawToken", rawToken);
        _tokensCache.put(cacheKey, cacheItem);
    }

    private String getRawToken(String token) throws JSONException{
        JSONObject jsonToken = new JSONObject(token);
        return jsonToken.get("rawToken").toString();
    }

    private void invokeCallbackWithException(ServiceEventListener serviceEventListener, Exception ex) {
        ex.printStackTrace();
        if (serviceEventListener!=null) {
            ServiceEvent event = new ServiceEvent(this, HttpStatus.SC_BAD_REQUEST, ex.getMessage(), null, ex);
            serviceEventListener.onFinish(event);
        }
    }

    private void invokeCallback(ServiceEventListener serviceEventListener, String rawToken, Object user) {
        if (serviceEventListener!=null) {
            ServiceEvent event = new ServiceEvent(this, HttpStatus.SC_OK, rawToken, user);
            serviceEventListener.onFinish(event);
        }
    }

    public void SignOut(String cacheKey) {
        _tokensCache.remove(cacheKey);
    }

    //Calls IP and then KidoZen identity provider to get the token
    private class FederatedIdentity extends AsyncTask<String, Void, Object[]> {
        IIdentityProvider _identityProvider;
        String _userTokeFromAuthService, _statusCode;
        final CountDownLatch _lcd = new CountDownLatch(1);

        public FederatedIdentity(IIdentityProvider iIdentityProvider) {
            _identityProvider = iIdentityProvider;
        }

        @Override
        protected Object[] doInBackground(String... params) {
            Object[] response = new Object[2];
            try
            {
                this.getFederatedToken(params[0], params[1], params[2]);
                response[0] = _userTokeFromAuthService;
            }
            catch (Exception e) {
                response[1] = e;
            }
            finally
            {
                //_lcd.await(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
                return response;
            }
        }

        private void getFederatedToken(String endpoint, final String authServiceEndpoint, final String applicationScope) throws Exception {
            _identityProvider.RequestToken(new URI(endpoint), new KZAction<String>() {
                @SuppressWarnings("deprecation")
                public void onServiceResponse(String wrapAssertionFromIp) throws Exception {
                    Log.d(TAG, String.format("Got auth token from Identity Provider"));
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("wrap_scope", applicationScope));
                    nameValuePairs.add(new BasicNameValuePair("wrap_assertion_format", "SAML"));
                    nameValuePairs.add(new BasicNameValuePair("wrap_assertion", wrapAssertionFromIp));
                    String message = Utilities.getQuery(nameValuePairs);

                    SNIConnectionManager sniManager = new SNIConnectionManager(authServiceEndpoint, message, null, null, _strictSSL);
                    Hashtable<String, String> authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
                    _userTokeFromAuthService = authResponse.get("responseBody");
                    _statusCode = authResponse.get("statusCode");
                    //_lcd.countDown();
                    if (Integer.parseInt(_statusCode) >= HttpStatus.SC_BAD_REQUEST) throw new Exception(String.format("Invalid Response (Http Status Code = %s). Body : %s", _statusCode, _userTokeFromAuthService));
                }
            });
        }
    }
    //
    private class KeyIdentity extends AsyncTask<String, Void, Object[]> {
        @Override
        protected Object[] doInBackground(String... params) {
            return getTokenForApplication(params[0],params[1], params[2], params[3]);
        }
        private Object[] getTokenForApplication(String oauthEndpoint, String domain, String applicationScope, String applicationKey) {
            Object[] response = new Object[2];
            HashMap<String, String> params = null;
            Hashtable<String, String> requestProperties = new Hashtable<String, String>();
            requestProperties.put(CONTENT_TYPE,APPLICATION_JSON);
            requestProperties.put(ACCEPT, APPLICATION_JSON);
            String statusCode = null;
            String body = null;
            try {
                String message = new JSONObject()
                        .put("client_id", domain)
                        .put("client_secret", applicationKey)
                        .put("grant_type", "client_credentials")
                        .put("scope", applicationScope).toString();

                SNIConnectionManager sniManager = new SNIConnectionManager(oauthEndpoint, message, requestProperties, null, _strictSSL);
                Hashtable<String, String>  authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
                body = authResponse.get("responseBody");
                statusCode = authResponse.get("statusCode");
                response[0] = body;
                if (Integer.parseInt(statusCode) >= HttpStatus.SC_BAD_REQUEST) throw new Exception(String.format("Invalid Response (Http StatusCode = %s). Body : %s", statusCode, body));
            }
            catch (JSONException e) {
                response[1] = e;
            }
            catch (Exception e) {
                response[1] = e;
            }
            finally {
                return response;
            }
        }


    }

}

