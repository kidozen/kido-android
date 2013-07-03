package kidozen.client;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import kidozen.client.authentication.KidoZenUser;

import org.json.JSONObject;
import android.util.Log;

/**
 * Queue service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class Queue  extends KZService implements Observer {
	private static final String TAG = "Queue";
	String _endpoint;
	String _name;
	
	/**
	 * Constructor
	 * 
	 * You should not create a new instances of this constructor. Instead use the PubSubChannel[""] method of the KZApplication object. 
	 * @param endpoint The service endpoint
	 * @param name The name of the queue to be created
	 */
	public Queue(String endpoint, String name)
	{
		_endpoint=endpoint;
		_name = name;
	}

	/**
	 * Enqueues a message
	 * 
	 * @param message The message to enqueue
	 * @param callback The callback with the result of the service call
	 */
	public void Enqueue(final JSONObject message, final ServiceEventListener callback) 
	{
		String  url = _endpoint + "/" + _name;
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("isPrivate","true");
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
		headers.put(CONTENT_TYPE,APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);

        this.ExecuteTask(url, KZHttpMethod.POST, params, headers, callback, message, BypassSSLVerification);
	}


	/**
	 * Dequeues a message
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void Dequeue(final ServiceEventListener callback) 
	{
		String  url = _endpoint + "/" + _name + "/next";

		HashMap<String, String> params = null;
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());

        this.ExecuteTask(url, KZHttpMethod.DELETE, params, headers, callback, BypassSSLVerification);
	}

}
