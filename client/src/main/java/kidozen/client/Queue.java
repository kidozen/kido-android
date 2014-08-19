package kidozen.client;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;
import kidozen.client.internal.SyncHelper;

/**
 * Queue service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class Queue  extends KZService {
	private static final String TAG = "Queue";

    /**
     * You should not create a new instances of this constructor. Instead use the Queue() method of the KZApplication object.
     *
     * @param queue
     * @param name
     * @param provider
     * @param username
     * @param pass
     * @param clientId
     * @param userIdentity
     * @param applicationIdentity
     */
	public Queue(String queue, String name,String provider , String username, String pass, String clientId, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(queue,name, provider, username, pass, clientId, userIdentity, applicationIdentity);
    }
	/**
	 * Enqueues a message
	 * 
	 * @param message The message to enqueue
	 * @param callback The callback with the result of the service call
	 */
	public void Enqueue(final JSONObject message, final ServiceEventListener callback) 
	{
        String  url = mEndpoint + "/" + mName;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("isPrivate","true");
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

        new KZServiceAsyncTask(KZHttpMethod.POST, params, headers, message, callback, getStrictSSL()).execute(url);
	}

    public boolean Enqueue(JSONObject message) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper<String>(this, "Enqueue", String.class, ServiceEventListener.class);
        helper.Invoke(new Object[]{message});
        return (helper.getStatusCode() == HttpStatus.SC_CREATED);
    }

	/**
	 * Dequeues a message
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void Dequeue(final ServiceEventListener callback) 
	{
        String  url = mEndpoint + "/" + mName + "/next";
        new KZServiceAsyncTask(KZHttpMethod.DELETE, null, null, callback, getStrictSSL()).execute(url);
    }

    public JSONObject Dequeue() throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONObject>(this, "Dequeue", ServiceEventListener.class)
                .Invoke(new Object[]{});
    }
}
