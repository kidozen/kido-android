package kidozen.client;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;
import kidozen.client.internal.SyncHelper;

/**
 * Push notifications service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class Notification extends KZService  {
	private static final String PLATFORM_C2DM = "gcm";
	private String _deviceId;
	private String _channel;

    /**
     * You should not create a new instances of this constructor. Instead use the Notification() method of the KZApplication object.
     *
     * @param endpoint
     * @param name
     * @param provider
     * @param username
     * @param pass
     * @param clientId
     * @param userIdentity
     * @param applicationIdentity
     */
	public Notification(String endpoint, String name,String provider , String username, String pass, String clientId, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(endpoint, name, provider, username, pass, clientId, userIdentity, applicationIdentity);
    }

	/**
	 * Subscribe the device to the channel
	 * 
	 * @param androidId The unique identifier of the device. You can get it by using the Secure.getString(getActivity().getContentResolver(), Secure.ANDROID_ID) Android API call
	 * @param channel The name of the channel to push and receive messages
	 * @param subscriptionID The Google Cloud Message subscription ID associated with your application. For more information check Google Cloud Messaging (GCM)
	 * @param callback The callback with the result of the service call
	 */
	public void Subscribe(final String androidId,final String channel,final String subscriptionID, final ServiceEventListener callback) {
        _channel = channel;
        _deviceId = androidId;

        HashMap<String, String> s = new HashMap<String, String>();
        s.put("deviceId", _deviceId);
        s.put("subscriptionId", subscriptionID);
        s.put("platform", PLATFORM_C2DM);

        String  url = mEndpoint + "/subscriptions/" + mName + "/" + _channel;

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

        new KZServiceAsyncTask(KZHttpMethod.POST, null, headers,new JSONObject(s), callback, getStrictSSL()).execute(url);
    }

    public boolean Subscribe(String androidId,String channel,String subscriptionID) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper<String>(this, "Subscribe", String.class, String.class, String.class, ServiceEventListener.class);
        helper.Invoke(new Object[]{androidId,channel,subscriptionID});
        return (helper.getStatusCode() == HttpStatus.SC_CREATED);
    }


    /**
	 * Ends the subscription to the channel
	 *  
	 * @param channel The name of the channel to push and receive messages
	 * @param subscriptionId The Google Cloud Message subscription ID associated with your application. For more information check Google Cloud Messaging (GCM)
	 * @param callback The callback with the result of the service call
	 */
	public void Unsubscribe(final String channel, final String subscriptionId, final ServiceEventListener callback) 
	{
        String  url = mEndpoint + "/subscriptions/" + mName + "/" + channel + "/" + subscriptionId ;
        HashMap<String, String> params = null;
        HashMap<String, String> headers = new HashMap<String, String>();

        new KZServiceAsyncTask(KZHttpMethod.DELETE, params, headers, callback, getStrictSSL()).execute(url);
    }

    public boolean Unsubscribe(String androidId,String channel,String subscriptionID) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper<String>(this, "Unsubscribe", String.class, String.class, String.class, ServiceEventListener.class);
        helper.Invoke(new Object[]{androidId,channel,subscriptionID});
        return (helper.getStatusCode() == HttpStatus.SC_OK);
    }

    /**
	 * Push a message into the specified channel
	 * 
	 * @param channel The name of the channel to push the message
	 * @param data The message to push in the channel
	 * @param callback The callback with the result of the service call
	 */
	public void Push(final String channel,final JSONObject data, final ServiceEventListener callback)
	{
            String  url = mEndpoint + "/push/" + mName + "/" + channel;

            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
            headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

            new KZServiceAsyncTask(KZHttpMethod.POST, null, headers,  data, callback, getStrictSSL()).execute(url);
    }

    public boolean Push(String deviceId, JSONObject data) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper<String>(this, "Push", String.class, JSONObject.class, ServiceEventListener.class);
         helper.Invoke(new Object[]{deviceId, data});
        return (helper.getStatusCode() == HttpStatus.SC_OK);
    }

	/**
	 * Retrieves all the subscriptions for the current device
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void  GetSubscriptions(String deviceId, final ServiceEventListener callback) throws InvalidParameterException {
        if (deviceId.isEmpty() || deviceId == null) throw new InvalidParameterException("Invalid deviceId");
        _deviceId = deviceId;
        String  url = mEndpoint + "/devices/" + _deviceId + "/" + mName;

        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        new KZServiceAsyncTask(KZHttpMethod.GET, params, headers,  callback, getStrictSSL()).execute(url);
    }

    public JSONArray GetSubscriptions(String deviceId) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONArray>(this, "GetSubscriptions", String.class, ServiceEventListener.class)
                .Invoke(new Object[]{deviceId});
    }

}
