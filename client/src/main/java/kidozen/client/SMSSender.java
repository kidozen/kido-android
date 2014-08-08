package kidozen.client;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;
import kidozen.client.internal.SyncHelper;

/**
 * SMS  service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class SMSSender extends KZService {
	private static final String TAG = "SMSSender";
	String _number;

    /**
     * You should not create a new instances of this constructor. Instead use the SMSSender() method of the KZApplication object.
     *
     * @param endpoint
     * @param number
     * @param provider
     * @param username
     * @param pass
     * @param clientId
     * @param userIdentity
     * @param applicationIdentity
     */
	public SMSSender(String endpoint, String number, String provider , String username, String pass, String clientId, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(endpoint, number, provider, username, pass, clientId, userIdentity, applicationIdentity);
		mEndpoint=endpoint;
		_number = number;
	}

	/**
	 * Sends the sms message
	 * 
	 * @param message The message to send
	 * @param callback The callback with the result of the service call
	 */
	@SuppressWarnings("deprecation")
	public void Send(final String message, final ServiceEventListener callback) 
    {
        String encodedNumber =  URLEncoder.encode(_number);
        String encodedMessage =  URLEncoder.encode(message);
        String  url = mEndpoint;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("to", encodedNumber);
        params.put("message", encodedMessage);

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
        new KZServiceAsyncTask(KZHttpMethod.POST, params, headers, callback, getStrictSSL()).execute(url);
    }

    public boolean Send(JSONObject message) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper<String>(this, "Send", JSONObject.class, ServiceEventListener.class);
        helper.Invoke(new Object[]{message});
        return (helper.getStatusCode() == HttpStatus.SC_CREATED);
    }

    /**
     * Get the status of one message: Sent or queued
     * 
     * @param messageId The unique identifier of the sent message
	 * @param callback The callback with the result of the service call
     */
    public void GetStatus(final String messageId,  final ServiceEventListener callback) 
    {
        String  url = mEndpoint + "/" + messageId;
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();

        new KZServiceAsyncTask(KZHttpMethod.GET, params, headers, callback, getStrictSSL()).execute(url);
    }

    public JSONObject GetStatus(JSONObject message) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONObject>(this, "GetStatus", JSONObject.class, ServiceEventListener.class)
            .Invoke(new Object[]{message});
    }

}
