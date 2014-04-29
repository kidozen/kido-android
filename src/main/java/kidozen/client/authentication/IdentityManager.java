package kidozen.client.authentication;

import android.os.AsyncTask;
import android.util.Log;

import com.sun.jndi.url.iiopname.iiopnameURLContextFactory;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZAction;
import kidozen.client.KZHttpMethod;
import kidozen.client.SNIConnectionManager;
import kidozen.client.ServiceEventListener;
import kidozen.client.Utilities;

/**
 * Created by christian on 4/29/14.
 */
public class IdentityManager {
    public static HashMap<String, KidoZenUser> TokensCache = new HashMap<String, KidoZenUser>();
    private final String TAG = "IdentityManager";

    private IIdentityProvider _fedIdentityProvider;
    private boolean _strictSSL;
    private boolean _authenticated;
    private boolean _hasIPToken;

    private String _lastErrorDescription;
    private String _oauthTokenEndpoint;

    private KidoZenUser _kidoZenUser;
    private KidoZenUser _applicationUser;

    private JSONObject _authConfig;


    public IdentityManager(boolean strictSSL) {
        _strictSSL = !strictSSL;
    }

    public IdentityManager(JSONObject authConfig, boolean strictSSL) {
        this(strictSSL);
        _authConfig = authConfig;
    }

    public KidoZenUser Authenticate(final String providerName,final String username, final String password,final ServiceEventListener callback) {
        try {
            String applicationScope =_authConfig.getString("applicationScope");
            String authServiceScope =_authConfig.getString("authServiceScope");
            String authServiceEndpoint =_authConfig.getString("authServiceEndpoint");
            String ipEndpoint = null;
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
            if (ipProtocol!=null && ipEndpoint!=null) {
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
                FederatedIdentity id = new FederatedIdentity(ip);
                String token = new FederatedIdentity(ip).execute(  ipEndpoint, authServiceEndpoint,applicationScope).get();
            }
            else {
                throw new Exception("provider not found");
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public KidoZenUser Authenticate(final String key,final String applicationName, final ServiceEventListener callback) {
        return null;
    }

/*
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
*/
    //Calls IP and then KidoZen identity provider to get the token
    private class FederatedIdentity extends AsyncTask<String, Void, String> {
        IIdentityProvider _identityProvider;
        String _userTokeFromAuthService;
        final CountDownLatch _lcd = new CountDownLatch(1);

        public FederatedIdentity(IIdentityProvider iIdentityProvider) {
            _identityProvider = iIdentityProvider;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                this.getFederatedToken(params[0],params[1],params[2]);
                _lcd.await(5, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
                _lcd.countDown();
            }
            return _userTokeFromAuthService;
        }

        private void getFederatedToken(String endpoint, final String authServiceEndpoint, final String applicationScope) throws Exception {
            try {
                _identityProvider.RequestToken(new URI(endpoint), new KZAction<String>() {
                    @SuppressWarnings("deprecation")
                    public void onServiceResponse(String response) throws Exception {
                        Log.d(TAG, String.format("Got auth token from Identity Provider"));
                        _userTokeFromAuthService = getTokenForUsername(response, authServiceEndpoint, applicationScope);
                        //_kidoZenUser = createKidoZenUser(_userTokeFromAuthService);
                        _lcd.countDown();
                    }
                });
            }
            catch (Exception e)
            {
                _authenticated = false;
                _hasIPToken = false;
                _lastErrorDescription = "Error trying to call KidoZen Authentication Service Endpoint. " + e.getMessage();
            }
        }

        private String getTokenForUsername(final String wrapAssertionFromIp, String authServiceEndpoint, String applicationScope) throws Exception {
            String body = null;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("wrap_scope", applicationScope));
                nameValuePairs.add(new BasicNameValuePair("wrap_assertion_format", "SAML"));
                nameValuePairs.add(new BasicNameValuePair("wrap_assertion", wrapAssertionFromIp));
                String message = Utilities.getQuery(nameValuePairs);

                SNIConnectionManager sniManager = new SNIConnectionManager(authServiceEndpoint, message, null, null, _strictSSL);
                Hashtable<String, String> authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
                body = authResponse.get("responseBody");
            }
            catch (Exception e) {
                throw e;
            }
            return body;
        }


    }
    //
    private class KeyIdentity extends AsyncTask<JSONObject, Void, String> {
        @Override
        protected String doInBackground(JSONObject... params) {
            return null;
        }
        private void getTokenForApplication(String domain, String applicationName, String applicationKey) {
            HashMap<String, String> params = null;
            HashMap<String, String> headers = new HashMap<String, String>();

            try {
                String message = new JSONObject()
                        .put("client_id", domain)
                        .put("client_secret", applicationKey)
                        .put("grant_type", "client_credentials")
                        .put("scope", applicationName).toString();

                SNIConnectionManager sniManager = new SNIConnectionManager(_oauthTokenEndpoint, message, null, null, _strictSSL);
                Hashtable<String, String>  authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
                String body = authResponse.get("responseBody");
                //_applicationUser = createKidoZenUser(body);
                String kidoZenApplicationHashKey = Utilities.createHash(String.format("%s%s", domain, applicationKey));
                //TokensCache.put(kidoZenApplicationHashKey, _applicationUser);
            }
            catch (JSONException je) {
                Log.e("Error", je.getMessage());
            }
            catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            finally {
            }
        }


    }
}
