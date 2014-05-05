package kidozen.client;

import org.json.JSONObject;

import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;


/**
 * Configuration service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 *
 */
public class Configuration  extends KZService {
	public Configuration(String configuration,String name,  String provider , String username, String pass, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(configuration,name, provider, username, pass, userIdentity, applicationIdentity);
    }

	/**
	 * Save the value of the configuration
	 * 
	 * @param message A JSONObject that represents the configuration 
	 * @param callback The callback with the result of the service call
	 */
	public void Save(final JSONObject message, final ServiceEventListener callback) 
	{
        CreateAuthHeaderValue(_provider, _username, _password, new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                String  url = mEndpoint + "/" + mName;
                HashMap<String, String> params = new HashMap<String, String>();
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
                new KZServiceAsyncTask(KZHttpMethod.POST, params, headers, message, callback, StrictSSL).execute(url);
            }
        });
	}

	/**
	 * Retrieves the configuration value
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void Get(final ServiceEventListener callback) 
	{
        CreateAuthHeaderValue(_provider,_username,_password,new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                String  url = mEndpoint + "/" + mName;
                HashMap<String, String> params = new HashMap<String, String>();
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);

                new KZServiceAsyncTask(KZHttpMethod.GET,params,headers,callback, StrictSSL).execute(url);
            }
        });
	}

	/**
	 * Deletes the current configuration
	 */
	public void Delete()
	{
		this.Delete(null);
	}

	/**
	 * Deletes the current configuration
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void Delete(final ServiceEventListener callback) 
	{
        CreateAuthHeaderValue(_provider,_username,_password,new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                String  url = mEndpoint + "/" + mName;
                HashMap<String, String> params = new HashMap<String, String>();
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);

                new KZServiceAsyncTask(KZHttpMethod.DELETE, params, headers, callback, StrictSSL).execute(url);
            }
        });
    }

	/**
	 * Pulls all the configuration values
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void All(final ServiceEventListener callback) 
	{
        CreateAuthHeaderValue(_provider,_username,_password,new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                String  url = mEndpoint + "/" + mName;
                HashMap<String, String> params = new HashMap<String, String>();
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                new KZServiceAsyncTask(KZHttpMethod.DELETE, params, headers, callback, StrictSSL).execute(url);
            }
        });
	}
}
