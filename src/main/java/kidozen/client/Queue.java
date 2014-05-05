package kidozen.client;

import org.json.JSONObject;

import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;

/**
 * Queue service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class Queue  extends KZService {
	private static final String TAG = "Queue";
	public Queue(String queue, String name,String provider , String username, String pass, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(queue,"", provider, username, pass, userIdentity, applicationIdentity);
    }
	/**
	 * Enqueues a message
	 * 
	 * @param message The message to enqueue
	 * @param callback The callback with the result of the service call
	 */
	public void Enqueue(final JSONObject message, final ServiceEventListener callback) 
	{
        CreateAuthHeaderValue(_provider, _username, _password, new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                String  url = mEndpoint + "/" + mName;
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("isPrivate","true");
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

                new KZServiceAsyncTask(KZHttpMethod.POST, params, headers, message, callback, StrictSSL).execute(url);
            }
        });
	}


	/**
	 * Dequeues a message
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void Dequeue(final ServiceEventListener callback) 
	{
        CreateAuthHeaderValue(_provider, _username, _password, new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                String  url = mEndpoint + "/" + mName + "/next";

                HashMap<String, String> params = null;
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());

                new KZServiceAsyncTask(KZHttpMethod.DELETE, params, headers, callback, StrictSSL).execute(mEndpoint);
            }
        });
       }
}
