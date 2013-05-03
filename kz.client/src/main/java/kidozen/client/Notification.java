package kidozen.client;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import kidozen.client.authentication.KidoZenUser;

import org.json.JSONObject;

import android.util.Log;
/**
 * Push notifications service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class Notification extends KZService implements Observer {
	private static final String PLATFORM_C2DM = "gcm";
	private String _deviceId;
	private String _channel;
	private String _endpoint;
	private String _name;
	
	/**
	 * Constructor
	 * 
	 * You should not create a new instances of this constructor. Instead use the Notification[""] method of the KZApplication object. 
	 * 
	 * @param endpoint The Configuration service endpoint
	 * @param name The name of the notification channel to be created
	 */
	public Notification(String endpoint, String name) 
	{
		_endpoint=endpoint;
		_name = name;
	}

	/**
	 * Subscribe the device to the channel
	 * 
	 * @param androidId The unique identifier of the device. You can get it by using the Secure.getString(getActivity().getContentResolver(), Secure.ANDROID_ID) Android API call
	 * @param channel The name of the channel to push and receive messages
	 * @param subscriptionID The Google Cloud Message subscription ID associated with your application. For more information check this link {@link http://developer.android.com/google/gcm/gs.html}
	 * @param callback The callback with the result of the service call
	 */
	public void Subscribe(String androidId, String channel, String subscriptionID, final ServiceEventListener callback) {
		_channel = channel;
		_deviceId = androidId;
		
		HashMap<String, String> s = new HashMap<String, String>();
		s.put("deviceId", _deviceId);
		s.put("subscriptionId", subscriptionID);
		s.put("platform", PLATFORM_C2DM);
		
		String  url = _endpoint + "/subscriptions/" + _name + "/" + _channel;
		
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
		headers.put(CONTENT_TYPE,APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);

		ServiceInvokeAsyncTask task = new ServiceInvokeAsyncTask(KZHttpMethod.POST, null, headers, new JSONObject(s), callback, BypassSSLVerification);
		task.execute(url);
	}

	/**
	 * Unsubscribes from the channel
	 *  
	 * @param channel The name of the channel to push and receive messages
	 * @param subscriptionID The Google Cloud Message subscription ID associated with your application. For more information check this link {@link http://developer.android.com/google/gcm/gs.html}
	 * @param callback The callback with the result of the service call
	 */
	public void Unsubscribe(final String channel, final String subscriptionId, final ServiceEventListener callback) 
	{
		String  url = _endpoint + "/subscriptions/" + _name + "/" + channel + "/" + subscriptionId ;
		HashMap<String, String> params = null;
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
		ServiceInvokeAsyncTask task = new ServiceInvokeAsyncTask(KZHttpMethod.DELETE, params, headers, callback, BypassSSLVerification);
		task.execute(url);
	}

	/**
	 * Push a message into the specified channel
	 * 
	 * @param channel The name of the channel to push the message
	 * @param data The message to push in the channel
	 * @param callback The callback with the result of the service call
	 */
	public void Push(final String channel, JSONObject data, final ServiceEventListener callback) 
	{
		String  url = _endpoint + "/push/" + _name + "/" + channel;

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
		headers.put(CONTENT_TYPE,APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);

		ServiceInvokeAsyncTask task = new ServiceInvokeAsyncTask(KZHttpMethod.POST, null, headers,data, callback, BypassSSLVerification);
		task.execute(url);

	}

	/**
	 * Retrieves all the subscriptions for the current device and subscription
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void  GetSubscriptions(final ServiceEventListener callback) 
	{
		String  url = _endpoint + "/devices/" + _deviceId + "/" + _name;		

		HashMap<String, String> params = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
		ServiceInvokeAsyncTask t = new ServiceInvokeAsyncTask(KZHttpMethod.GET, params, headers, callback, BypassSSLVerification);
		t.execute(url);
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable arg0, Object data) {
		Log.d("PushNotification", "token updated");
		this.KidozenUser = (KidoZenUser) data;		
	}

}
