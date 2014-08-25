package kidozen.client.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import kidozen.client.KZHttpMethod;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.internal.KZAction;
import kidozen.client.internal.SNIConnectionManager;
import kidozen.client.internal.Utilities;


/**
 * Created by christian on 4/29/14.
 */
public class IdentityManager {
    public static String PASSIVE_STRICT_SSL = "PASSIVE_STRICT_SSL";
    public static String PASSIVE_SIGNIN_URL = "PASSIVE_SIGNIN_URL";
    protected static final String ACCEPT = "Accept";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String CONTENT_TYPE = "content-type";

    private HashMap<String, JSONObject> mTokensCache = new HashMap<String, JSONObject>();
    private final String TAG = "IdentityManager";
    private boolean mStrictSSL;
    private JSONObject mAuthConfig;

    String ipEndpoint = null;

    private static IdentityManager INSTANCE = null;
    private PassiveAuthenticationResponseReceiver mReceiver;
    private String mApplicationKey;
    private Context mContext;

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

    public void Setup(JSONObject authConfig, boolean strictSSL, String applicationKey){
        mStrictSSL = !strictSSL;
        mAuthConfig = authConfig;
        mApplicationKey = applicationKey;
    }

    // Active authentication
    public String Authenticate(final String providerName,final String username, final String password,final ServiceEventListener callback) {
        String cacheKey = Utilities.createHash(String.format("%s%s%s", providerName, username, password));
        String rawToken = null;
        try {
            JSONObject cacheItem = mTokensCache.get(cacheKey);
            if (cacheItem!=null) {
                rawToken = cacheItem.getString("rawToken");
                KidoZenUser usr = (KidoZenUser) mTokensCache.get(cacheKey).get("user");
                usr.PulledFromCache = true;
                invokeCallback(callback, rawToken, usr);
            }
            else {
                String applicationScope = mAuthConfig.getString("applicationScope");
                String authServiceEndpoint = mAuthConfig.getString("authServiceEndpoint");
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
                    addToTokensCache(cacheKey, token, "", KidoZenUserIdentityType.USER_IDENTITY);
                    rawToken = getRawToken(token);
                    invokeCallback(callback, rawToken, mTokensCache.get(cacheKey).get("user"));
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
        String authServiceScope = mAuthConfig.getString("authServiceScope");
        String ipProtocol = null;

        JSONObject identityProviders = mAuthConfig.getJSONObject("identityProviders");
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
            ((WRAPv09IdentityProvider)ip).bypassSSLValidation= mStrictSSL;
        }
        else {
            ip = new ADFSWSTrustIdentityProvider();
            ((ADFSWSTrustIdentityProvider)ip).bypassSSLValidation= mStrictSSL;
        }
        ip.Initialize(username, password, authServiceScope);
        return ip;
    }

    // Authentication using key
    public String Authenticate(final String key, final ServiceEventListener callback) {
        KeyIdentity ki = new KeyIdentity();
        String rawToken = null;
        try {
            JSONObject cacheItem = mTokensCache.get(key);
            if (cacheItem != null) {
                rawToken = cacheItem.getString("rawToken");
                kidozen.client.authentication.KidoZenUser usr = (kidozen.client.authentication.KidoZenUser) mTokensCache.get(key).get("user");
                usr.PulledFromCache = true;
                invokeCallback(callback, rawToken, usr);
            } else {
                String oauthTokenEndpoint = mAuthConfig.getString("oauthTokenEndpoint");
                String domain = mAuthConfig.getString("domain");
                String applicationScope = mAuthConfig.getString("applicationScope");
                Object[] response = ki.execute(oauthTokenEndpoint, domain, applicationScope, key).get();
                if (response[1] != null) {
                    invokeCallbackWithException(callback, (Exception) response[1]);
                } else {
                    //Quick and dirty fix to use 'createKidoZenUser function
                    String token = response[0].toString().replace("access_token", "rawToken");
                    addToTokensCache(key, token, "", KidoZenUserIdentityType.APPLICATION_IDENTITY);
                    rawToken = getRawToken(token);
                    invokeCallback(callback, rawToken, mTokensCache.get(key).get("user"));
                }
            }
        } catch (JSONException e) {
            invokeCallbackWithException(callback, e);
        } catch (InterruptedException e) {
            invokeCallbackWithException(callback, e);
        } catch (ExecutionException e) {
            invokeCallbackWithException(callback, e);
        } finally {
            return rawToken;
        }
    }

    // Social / passive authentication
    public void Authenticate(Context context, String userUniqueIdentifier, ServiceEventListener callback) throws JSONException{
        String key = userUniqueIdentifier;
        mContext = context;
        JSONObject cacheItem = mTokensCache.get(key);
        if (cacheItem != null) {
            String rawToken = cacheItem.getString("rawToken"); // usar el token de refresh
            KidoZenUser usr = (KidoZenUser) mTokensCache.get(key).get("user");
            usr.PulledFromCache = true;
            invokeCallback(callback, rawToken, usr);
        }
        else {
            IntentFilter filter = new IntentFilter(PassiveAuthenticationResponseReceiver.ACTION_RESP);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            mReceiver = new PassiveAuthenticationResponseReceiver(callback);
            context.registerReceiver(mReceiver, filter);

            Intent startPassiveAuth = new Intent(context, PassiveAuthenticationActivity.class);
            startPassiveAuth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startPassiveAuth.putExtra(PASSIVE_SIGNIN_URL, mAuthConfig.getString("signInUrl"));
            startPassiveAuth.putExtra(PASSIVE_STRICT_SSL, String.valueOf(mStrictSSL));
            context.startActivity(startPassiveAuth);
        }
    }

    // Next release must use this method for all auth types
    public void GetToken(final KidoZenUser user, final ServiceEventListener callback) {
        try {
                JSONObject cacheItem = mTokensCache.get(user.getUserHash());
                if (cacheItem!=null) {
                    if (user.HasExpired()) {
                        JSONObject message = new JSONObject()
                                .put("client_id", mAuthConfig.getString("domain"))
                                .put("grant_type", "refresh_token")
                                .put("client_secret", mApplicationKey)
                                .put("refresh_token", user.RefreshToken)
                                .put("scope", mAuthConfig.getString("applicationScope"));

                        Object[] response = new RefreshTokenTask(message.toString()).execute().get();
                        int status = Integer.parseInt(response[0].toString());
                        String body = new JSONObject(response[1].toString()).getString("access_token");
                        if (status >= HttpStatus.SC_BAD_REQUEST) {
                            Exception ex = new Exception(String.format("Invalid Response (Http Status Code = %s). Body : %s", status, body));
                            invokeCallbackWithException(callback, ex);
                        }
                        invokeCallback(callback, body, user);
                    }
                    else {
                        KidoZenUser usr = (KidoZenUser) cacheItem.get("user");
                        invokeCallback(callback, cacheItem.getString("rawToken"), usr);
                    }
                }
                // Something failed. Try to launch the Authentication activity again.
                else {
                    Log.i(TAG,"Something failed. Try to launch the Authentication activity again ....");
                    this.Authenticate(mContext,"", callback);
                }
        }
        catch (Exception e) {
            invokeCallbackWithException(callback, e);
        }
    }

    public void GetRawToken(final String providerName,final String username, final String password,final ServiceEventListener callback) {
        String cacheKey = Utilities.createHash(String.format("%s%s%s", providerName, username, password));
        String rawToken = null;
        try {
            JSONObject cacheItem = mTokensCache.get(cacheKey);
            if (cacheItem!=null)
            {
                rawToken = cacheItem.getString("rawToken");
                KidoZenUser usr = (KidoZenUser) cacheItem.get("user");
                if (usr.HasExpired())
                {

                    mTokensCache.remove(cacheKey);
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
            JSONObject cacheItem = mTokensCache.get(hashKey);
            if (cacheItem!=null)
            {
                rawToken = cacheItem.getString("rawToken");
                KidoZenUser usr = (KidoZenUser) mTokensCache.get(hashKey).get("user");

                if (usr.HasExpired())
                {
                    mTokensCache.remove(hashKey);
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

    public KidoZenUser createKidoZenUser(String tokenAsString, KidoZenUserIdentityType userIdentity) throws JSONException {
        JSONObject token = new JSONObject(tokenAsString);
        String rawTokenAsString = token.get("rawToken").toString();
        String refreshToken  = getRefreshToken(token);
        Log.d(TAG, String.format("Got KidoZen auth token from AuthService"));
        String rawToken = URLDecoder.decode(tokenAsString);
        String[] claims = rawToken.split("&");
        Hashtable<String, String> tokenClaims = new Hashtable<String, String>();
        for (int i = 0; i < claims.length; i++) {
            String[] keyValue = claims[i].split("=");
            String keyName = keyValue[0];
            int indexOfClaimKeyword= keyName.indexOf("/claims/");
            if (indexOfClaimKeyword>-1) {
                keyName = keyValue[0].substring(indexOfClaimKeyword + "/claims/".length(), keyName.length());
            }
            String v ;
            try {
                v=  keyValue[1];
            }
            catch (IndexOutOfBoundsException e) {
                v="";
            }
            tokenClaims.put(keyName,v);
        }
        KidoZenUser user = new KidoZenUser();
        user.IdentityType = userIdentity;
        user.Token = rawTokenAsString;
        user.RefreshToken = refreshToken;
        user.Claims = tokenClaims;
        user.SetExpiration(Long.parseLong(tokenClaims.get("ExpiresOn")));
        if (tokenClaims.get("role")!=null)
        {
            user.Roles = Arrays.asList(tokenClaims.get("role").split(","));
        }
        return user;
    }

    private String getRefreshToken(JSONObject token)  {
        try {
            return token.get("refresh_token").toString();
        } catch (JSONException e) {
            Log.i(TAG, "Auth service does not provide 'refresh_token'");
            return "";
        }
    }

    public void addToTokensCache(String cacheKey, String token, String refreshToken, KidoZenUserIdentityType userIdentity) throws JSONException {
        String rawToken = getRawToken(token);
        KidoZenUser user = createKidoZenUser(token, userIdentity);
        user.HashKey = cacheKey;
        JSONObject cacheItem = new JSONObject()
                .put("user", user)
                .put("refreshToken", refreshToken)
                .put("rawToken", rawToken);
        mTokensCache.put(cacheKey, cacheItem);
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
        mTokensCache.remove(cacheKey);
    }

    //Calls IP and then KidoZen identity provider to get the token
    private class FederatedIdentity extends AsyncTask<String, Void, Object[]> {
        IIdentityProvider _identityProvider;
        String _userTokeFromAuthService, _statusCode;
        final String USER_SOURCE_CLAIM = "http://schemas.kidozen.com/usersource";

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
                    //System.out.println("IdentityManager, getFederatedToken, wrapAssertionFromIp: " + wrapAssertionFromIp);
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("wrap_scope", applicationScope));
                    nameValuePairs.add(new BasicNameValuePair("wrap_assertion_format", "SAML"));
                    nameValuePairs.add(new BasicNameValuePair("wrap_assertion", wrapAssertionFromIp));
                    String message = Utilities.getQuery(nameValuePairs);
                    SNIConnectionManager sniManager = new SNIConnectionManager(authServiceEndpoint, message, null, null, mStrictSSL);
                    Hashtable<String, String> authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
                    _userTokeFromAuthService = authResponse.get("responseBody");
                    _statusCode = authResponse.get("statusCode");
                    //System.out.println("Got auth token from Identity Provider, _userTokeFromAuthService: " +  _userTokeFromAuthService);
                    //System.out.println("Got auth token from Identity Provider, _statusCode" + _statusCode);

                    if (Integer.parseInt(_statusCode) >= HttpStatus.SC_BAD_REQUEST) throw new Exception(String.format("Invalid Response (Http Status Code = %s). Body : %s", _statusCode, _userTokeFromAuthService));
                    if (!URLDecoder.decode(_userTokeFromAuthService).contains(USER_SOURCE_CLAIM)) {
                        _statusCode = String.valueOf(HttpStatus.SC_UNAUTHORIZED);
                        throw new Exception("unauthorized");
                    }
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

                SNIConnectionManager sniManager = new SNIConnectionManager(oauthEndpoint, message, requestProperties, null, mStrictSSL);
                Hashtable<String, String>  authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
                body = authResponse.get("responseBody");

                // TODO: Refactor entire class and use Interfaces
                // Adds a refresh_token json property.
                JSONObject updatedBody = new JSONObject(body)
                        .put("refresh_token", "");

                statusCode = authResponse.get("statusCode");
                response[0] = updatedBody.toString();

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

    //
    private class RefreshTokenTask extends AsyncTask<String,Void, Object[]> {
        String mMessage;
        public RefreshTokenTask(String message) {
            mMessage = message;
        }
        @Override
        protected Object[] doInBackground(String... strings) {
            Object[] response = new Object[2];

            try {
                Hashtable<String, String> requestProperties = new Hashtable<String, String>();
                requestProperties.put(CONTENT_TYPE,APPLICATION_JSON);
                requestProperties.put(ACCEPT, APPLICATION_JSON);

                SNIConnectionManager snim = new SNIConnectionManager(mAuthConfig.getString("oauthTokenEndpoint"), mMessage.toString(), requestProperties, null, mStrictSSL);
                Hashtable<String, String> authResponse = snim.ExecuteHttp(KZHttpMethod.POST);
                String body = authResponse.get("responseBody");
                String status = authResponse.get("statusCode");
                response[0] = status;
                response[1] = body;

                if (Integer.parseInt(status) >= HttpStatus.SC_BAD_REQUEST) throw new Exception(String.format("Invalid Response (Http StatusCode = %s). Body : %s", status, body));

            }
            catch (Exception e) {
                response[1] = e.getMessage();
            }
            finally {
                return response;
            }
        }
    }
}

