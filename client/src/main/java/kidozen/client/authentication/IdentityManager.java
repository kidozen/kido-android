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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import kidozen.client.InitializationException;
import kidozen.client.KZHttpMethod;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.internal.SNIConnectionManager;
import kidozen.client.internal.Utilities;


/**
 * Created by christian on 4/29/14.
 */
public class IdentityManager {
    public static final String GPLUS_AUTH_ACTION_CODE = "ACTION_CODE";
    public static final int GPLUS_AUTH_ACTION_CODE_SIGN_OUT = 0;
    public static final int GPLUS_AUTH_ACTION_CODE_REVOKE = 1;

    public static String FORCE_CLEAN_COOKIES = "FORCE_CLEAN_COOKIES";
    public static String PASSIVE_STRICT_SSL = "PASSIVE_STRICT_SSL";
    public static String PASSIVE_SIGNIN_URL = "PASSIVE_SIGNIN_URL";

    protected static final String ACCEPT = "Accept";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String CONTENT_TYPE = "content-type";

    private HashMap<String, JSONObject> mTokensCache = new HashMap<String, JSONObject>();
    private final String TAG = "IdentityManager";
    private boolean mStrictSSL;
    private JSONObject mAuthConfig;

    private String mAssertionFormat;

    private String ipEndpoint = null;

    private static IdentityManager INSTANCE = null;

    private String mApplicationKey;
    private Context mContext;

    private GPlusAuthenticationResponseReceiver mGPlusAuthenticationReceiver;
    private PassiveAuthenticationResponseReceiver mPassiveAuthenticationReceiver;


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

    public void Setup(boolean strictSSL, String applicationKey) {
        mStrictSSL = !strictSSL;
        mApplicationKey = applicationKey;
    }

    public void Setup(JSONObject authConfig, boolean strictSSL, String applicationKey){
        mStrictSSL = !strictSSL;
        mApplicationKey = applicationKey;
        mAuthConfig = authConfig;
    }

    // Active authentication with User and Password
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
                String applicationScope = mAuthConfig.getString("authServiceScope");
                String authServiceEndpoint = mAuthConfig.getString("authServiceEndpoint");

                BaseIdentityProvider identityProvider = createIpWithUsername(providerName, username, password, authServiceEndpoint,applicationScope);
                mAssertionFormat = identityProvider.assertionFormat;

                FederatedIdentity id = new FederatedIdentity(identityProvider);
                Object[] response = id.execute().get();
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

    private BaseIdentityProvider createIpWithUsername(String providerName, String username, String password, String serviceEndpoint,String applicationScope) throws Exception {
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

        BaseIdentityProvider ip = null;
        if (ipProtocol.equalsIgnoreCase("wrapv0.9")) {
            ip = new WRAPv09IdentityProvider(username, password,ipEndpoint, applicationScope);
            ((WRAPv09IdentityProvider)ip).StrictSSL = mStrictSSL;
        }
        else {
            ip = new ADFSWSTrustIdentityProvider(username, password, ipEndpoint, applicationScope);
            ((ADFSWSTrustIdentityProvider)ip).bypassSSLValidation= mStrictSSL;
        }
        //ip.Initialize(authServiceScope);
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

    public void Authenticate(Context context, KZPassiveAuthTypes userIdentifierType, ServiceEventListener callback) throws JSONException {
        this.Authenticate(context,true,userIdentifierType,callback);
    }

    // Social / passive authentication
    public void Authenticate(Context context, Boolean cleanCookies, KZPassiveAuthTypes userIdentifierType, ServiceEventListener callback) throws JSONException {
        String key = String.valueOf(userIdentifierType);
        mContext = context;
        JSONObject cacheItem = mTokensCache.get(key);
        if (cacheItem != null) {
            String rawToken = cacheItem.getString("rawToken"); // TODO: use refresh token
            KidoZenUser usr = (KidoZenUser) mTokensCache.get(key).get("user");
            usr.PulledFromCache = true;
            invokeCallback(callback, rawToken, usr);
        }
        else {
            if (userIdentifierType == KZPassiveAuthTypes.PASSIVE_AUTHENTICATION_USERID) {
                IntentFilter filter = new IntentFilter(PassiveAuthenticationResponseReceiver.ACTION_RESP);
                filter.addCategory(Intent.CATEGORY_DEFAULT);
                mPassiveAuthenticationReceiver = new PassiveAuthenticationResponseReceiver(callback);
                context.registerReceiver(mPassiveAuthenticationReceiver, filter);

                Intent startPassiveAuth = new Intent(context, PassiveAuthenticationActivity.class);
                startPassiveAuth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startPassiveAuth.putExtra(PASSIVE_SIGNIN_URL, mAuthConfig.getString("signInUrl"));
                startPassiveAuth.putExtra(PASSIVE_STRICT_SSL, String.valueOf(mStrictSSL));
                startPassiveAuth.putExtra(FORCE_CLEAN_COOKIES, String.valueOf(cleanCookies));
                context.startActivity(startPassiveAuth);
            }
            else {
                IntentFilter filter = new IntentFilter(GPlusAuthenticationResponseReceiver.ACTION_RESP);
                filter.addCategory(Intent.CATEGORY_DEFAULT);
                mGPlusAuthenticationReceiver = new GPlusAuthenticationResponseReceiver(callback);
                context.registerReceiver(mGPlusAuthenticationReceiver, filter);

                Intent startGPlusAuth = new Intent(context, GPlusAuthenticationActivity.class);
                startGPlusAuth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startGPlusAuth.putExtra(PASSIVE_SIGNIN_URL, mAuthConfig.getString("authServiceEndpoint"));
                startGPlusAuth.putExtra(KZPassiveAuthBroadcastConstants.GOOGLE_PLUS_KIDOZEN_SCOPE, mAuthConfig.getString("applicationScope"));
                startGPlusAuth.putExtra(KZPassiveAuthBroadcastConstants.GOOGLE_PLUS_SCOPE, mAuthConfig.getJSONObject("google").getString("scopes"));
                startGPlusAuth.putExtra(PASSIVE_STRICT_SSL, String.valueOf(mStrictSSL));
                startGPlusAuth.putExtra(FORCE_CLEAN_COOKIES, String.valueOf(cleanCookies));
                context.startActivity(startGPlusAuth);
            }
        }
    }

    //Custom IP implementation
    public String Authenticate(BaseIdentityProvider provider, ServiceEventListener callback) {
        String cacheKey = Utilities.createHash(String.format("%d", provider.hashCode()));
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
                CustomFederatedIdentity id = new CustomFederatedIdentity(provider);
                mAssertionFormat = provider.assertionFormat;
                Object[] response = id.execute().get();
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

    // Next release must use this method for all auth types
    public void GetToken(final KidoZenUser user, final ServiceEventListener callback) {
        try {
                JSONObject cacheItem = null;
                if (user.HashKey.contains("GPLUS_AUTHENTICATION_USERID") || user.HashKey.contains("PASSIVE_AUTHENTICATION_USERID") )
                    cacheItem = mTokensCache.get(user.HashKey);
                else
                    cacheItem = mTokensCache.get(user.getUserHash());

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
                    throw new Exception("Could not get token from internal cache. Please, try to authenticate again");
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
            String v ="";
            try { v=  keyValue[1];}
            catch (IndexOutOfBoundsException e) {}
            tokenClaims.put(keyName,v);
        }
        KidoZenUser user = new KidoZenUser();
        user.authenticationResponse = tokenAsString;
        user.IdentityType = userIdentity;
        user.Token = rawTokenAsString;
        user.RefreshToken = refreshToken;
        user.Claims = tokenClaims;
        user.SetExpiration(Long.parseLong(tokenClaims.get("ExpiresOn")));

        for(String claim : tokenClaims.keySet()){
            if (claim.contains("://schemas.kidozen.com/role"))
                user.Roles = Arrays.asList(tokenClaims.get(claim).split(","));
            if (claim.contains("://schemas.microsoft.com/ws/2008/06/identity/claims/role"))
                user.Roles.addAll( Arrays.asList(tokenClaims.get(claim).split(","))) ;
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

    private void unregisterReceivers() {
        if (mPassiveAuthenticationReceiver!=null) {
            mContext.unregisterReceiver(mPassiveAuthenticationReceiver);
        }
        if (mGPlusAuthenticationReceiver!=null) {
            mContext.unregisterReceiver(mGPlusAuthenticationReceiver);
        }
    }
    public void SignOut(String cacheKey) {
        this.unregisterReceivers();
        mTokensCache.remove(cacheKey);
    }

    public void SignOut(Context context, KZPassiveAuthTypes authenticationUserId) throws InitializationException {
        this.SignOut(String.valueOf(authenticationUserId));
        Intent startGPlusAuth = new Intent(context, GPlusAuthenticationActivity.class);
        startGPlusAuth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startGPlusAuth.putExtra(GPLUS_AUTH_ACTION_CODE, GPLUS_AUTH_ACTION_CODE_SIGN_OUT);
        try {
            startGPlusAuth.putExtra(KZPassiveAuthBroadcastConstants.GOOGLE_PLUS_KIDOZEN_SCOPE, mAuthConfig.getString("applicationScope"));
            startGPlusAuth.putExtra(KZPassiveAuthBroadcastConstants.GOOGLE_PLUS_SCOPE, mAuthConfig.getJSONObject("google").getString("scopes"));
        } catch ( NullPointerException ex) {
            throw new InitializationException("There was an error trying to sign out from G+. You must be logged in to KidoZen first using G+ to sign out");
        }catch ( JSONException ex) {
            throw new InitializationException("There was an error trying to sign out from G+. You must be logged in to KidoZen first using G+ to sign out");
        }
        startGPlusAuth.putExtra(PASSIVE_STRICT_SSL, String.valueOf(mStrictSSL));

        context.startActivity(startGPlusAuth);
        this.unregisterReceivers();
    }

    public void RevokeAccessFromGPlus(Context context) throws InitializationException {
        Intent startGPlusAuth = new Intent(context, GPlusAuthenticationActivity.class);
        startGPlusAuth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startGPlusAuth.putExtra(GPLUS_AUTH_ACTION_CODE, GPLUS_AUTH_ACTION_CODE_REVOKE);
        try {
            startGPlusAuth.putExtra(KZPassiveAuthBroadcastConstants.GOOGLE_PLUS_KIDOZEN_SCOPE, mAuthConfig.getString("applicationScope"));
            startGPlusAuth.putExtra(KZPassiveAuthBroadcastConstants.GOOGLE_PLUS_SCOPE, mAuthConfig.getJSONObject("google").getString("scopes"));
        } catch ( NullPointerException ex) {
            throw new InitializationException("There was an error trying to revoke token from G+. You must be logged in to KidoZen first using G+ to sign out");
        }catch ( JSONException ex) {
            throw new InitializationException("There was an error trying to revoke token from G+. You must be logged in to KidoZen first using G+ to sign out");
        }
        startGPlusAuth.putExtra(PASSIVE_STRICT_SSL, String.valueOf(mStrictSSL));

        context.startActivity(startGPlusAuth);
    }


    //Calls IP and then KidoZen identity provider to get the token
    private class FederatedIdentity extends AsyncTask<Void, Void, Object[]> {
        BaseIdentityProvider _identityProvider;
        String _userTokeFromAuthService, _statusCode;

        public FederatedIdentity(BaseIdentityProvider iIdentityProvider) {
            _identityProvider = iIdentityProvider;
        }

        @Override
        protected Object[] doInBackground(Void... params) {
            Object[] response = new Object[2];
            try
            {
                String authServiceScope = mAuthConfig.getString("applicationScope");
                String authServiceEndpoint = mAuthConfig.getString("authServiceEndpoint");

                String wrapAssertionFromIp = _identityProvider.RequestToken();
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("wrap_scope", authServiceScope));
                nameValuePairs.add(new BasicNameValuePair("wrap_assertion_format", mAssertionFormat));
                nameValuePairs.add(new BasicNameValuePair("wrap_assertion", wrapAssertionFromIp));
                String message = Utilities.getQuery(nameValuePairs);
                SNIConnectionManager sniManager = new SNIConnectionManager(authServiceEndpoint, message, null, null, mStrictSSL);
                Hashtable<String, String> authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
                _userTokeFromAuthService = authResponse.get("responseBody");
                _statusCode = authResponse.get("statusCode");
                //System.out.println("Got auth token from Identity Provider, _userTokeFromAuthService: " +  _userTokeFromAuthService);
                //System.out.println("Got auth token from Identity Provider, _statusCode" + _statusCode);

                if (Integer.parseInt(_statusCode) >= HttpStatus.SC_BAD_REQUEST) throw new Exception(String.format("Invalid Response (Http Status Code = %s). Body : %s", _statusCode, _userTokeFromAuthService));
                if (!URLDecoder.decode(_userTokeFromAuthService).contains(Constants.USER_SOURCE_AUTHORIZATION_CLAIM)) {
                    _statusCode = String.valueOf(HttpStatus.SC_UNAUTHORIZED);
                    throw new Exception("unauthorized");
                }
                response[0] = _userTokeFromAuthService;
            }
            catch (Exception e) {
                response[1] = e;
            }
            finally
            {
                return response;
            }
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

    //Calls IP and then KidoZen identity provider to get the token
    private class CustomFederatedIdentity extends AsyncTask<Void, Void, Object[]> {
        BaseIdentityProvider _identityProvider;
        String _userTokeFromAuthService, _statusCode;

        public CustomFederatedIdentity(BaseIdentityProvider iIdentityProvider) {
            _identityProvider = iIdentityProvider;
        }

        @Override
        protected Object[] doInBackground(Void... params) {
            Object[] response = new Object[2];
            try
            {
                String authServiceScope = mAuthConfig.getString("applicationScope");
                String authServiceEndpoint = mAuthConfig.getString("authServiceEndpoint");

                String wrapAssertionFromIp = _identityProvider.RequestToken();

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("wrap_scope", authServiceScope));
                nameValuePairs.add(new BasicNameValuePair("wrap_assertion_format", mAssertionFormat));
                nameValuePairs.add(new BasicNameValuePair("wrap_assertion", wrapAssertionFromIp));
                String message = Utilities.getQuery(nameValuePairs);
                SNIConnectionManager sniManager = new SNIConnectionManager(authServiceEndpoint, message, null, null, mStrictSSL);
                Hashtable<String, String> authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
                _userTokeFromAuthService = authResponse.get("responseBody");
                _statusCode = authResponse.get("statusCode");
                //System.out.println("Got auth token from Identity Provider, _userTokeFromAuthService: " +  _userTokeFromAuthService);
                //System.out.println("Got auth token from Identity Provider, _statusCode" + _statusCode);

                if (Integer.parseInt(_statusCode) >= HttpStatus.SC_BAD_REQUEST) throw new Exception(String.format("Invalid Response (Http Status Code = %s). Body : %s", _statusCode, _userTokeFromAuthService));
                if (!URLDecoder.decode(_userTokeFromAuthService).contains(Constants.USER_SOURCE_AUTHORIZATION_CLAIM)) {
                    _statusCode = String.valueOf(HttpStatus.SC_UNAUTHORIZED);
                    throw new Exception("unauthorized");
                }
                response[0] = _userTokeFromAuthService;
            }
            catch (Exception e) {
                response[1] = e;
            }
            finally
            {
                return response;
            }
        }
    }
}

