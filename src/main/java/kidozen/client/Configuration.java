package kidozen.client;

import org.json.JSONObject;

import java.util.HashMap;


/**
 * Configuration service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 *
 */
public class Configuration  extends KZService {
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
		headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());
		headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
		headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

        this.ExecuteTask(url, KZHttpMethod.POST, params, headers, callback, message, BypassSSLVerification);

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
		headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());

        this.ExecuteTask(url, KZHttpMethod.GET, params, headers, callback, BypassSSLVerification);
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
		headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());

        this.ExecuteTask(url, KZHttpMethod.DELETE, params, headers, callback, BypassSSLVerification);
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
		headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());

        this.ExecuteTask(url, KZHttpMethod.GET, params, headers, callback, BypassSSLVerification);
	}
}
