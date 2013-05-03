package kidozen.client;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONObject;

import kidozen.client.authentication.KidoZenUser;
import android.util.Log;

/**
 * Configuration service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 *
 */
public class Configuration  extends KZService implements Observer {
	private static final String TAG = "Configuration";
	String _endpoint;
	String _name;

	/**
	 * Constructor
	 * 
	 * You should not create a new instances of this constructor. Instead use the Configuration[""] method of the KZApplication object. 
	 * @param endpoint The Configuration service endpoint
	 */
	public Configuration(String endpoint)
	{
		_endpoint=endpoint;
	}

	/**
	 * Constructor
	 * 
	 * @param endpoint The Configuration service endpoint
	 * @param name The name of the configuration 
	 */
	public Configuration(String endpoint, String name)
	{
		_endpoint=endpoint;
		_name = name;
	}

	/**
	 * Save the value of the configuration
	 * 
	 * @param message A JSONObject that represents the configuration 
	 * @param callback The callback with the result of the service call
	 */
	public void Save(final JSONObject message, final ServiceEventListener callback) 
	{
		String  url = _endpoint + "/" + _name; 
		HashMap<String, String> params = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
		headers.put(CONTENT_TYPE,APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		ServiceInvokeAsyncTask task = new ServiceInvokeAsyncTask(KZHttpMethod.POST, params, headers, message,callback, BypassSSLVerification);
		task.execute(url);

	}

	/**
	 * Retrieves the configuration value
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void Get(final ServiceEventListener callback) 
	{
		String  url = _endpoint + "/" + _name; 
		HashMap<String, String> params = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
		ServiceInvokeAsyncTask task = new ServiceInvokeAsyncTask(KZHttpMethod.GET, params, headers, callback, BypassSSLVerification);
		task.execute(url);
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
		String  url = _endpoint + "/" + _name; 
		HashMap<String, String> params = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
		ServiceInvokeAsyncTask task = new ServiceInvokeAsyncTask(KZHttpMethod.DELETE, params, headers, callback, BypassSSLVerification);
		task.execute(url);
	}

	/**
	 * Pulls all the configuration values
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void All(final ServiceEventListener callback) 
	{
		String  url = _endpoint + "/" + _name; 
		HashMap<String, String> params = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
		ServiceInvokeAsyncTask task = new ServiceInvokeAsyncTask(KZHttpMethod.GET, params, headers, callback, BypassSSLVerification);
		task.execute(url);
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable observable, Object data) {
		Log.d(TAG, "token updated");
		this.KidozenUser = (KidoZenUser) data;
	}
}
