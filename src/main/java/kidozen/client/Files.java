package kidozen.client;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import kidozen.client.authentication.KidoZenUser;
import android.util.Log;

/**
 * SMS  service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class Files extends KZService  implements Observer {
	private static final String TAG = "SMSSender";
	String _endpoint;
	String _number;

	public void update(Observable observable, Object data) {
		Log.d(TAG, "token updated");
		this.KidozenUser = (KidoZenUser) data;
	}


	/**
	 * Constructor
	 *
	 * You should not create a new instances of this constructor. Instead use the SMSSender["number"] method of the KZApplication object.
	 * @param endpoint The service endpoint
	 * @param name The sms number to send messages
	 */
	public Files(String endpoint, String number)
	{
		_endpoint=endpoint;
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
        this.Upload(CreateAuthHeaderValue());
        //this.ExecuteTask(url, KZHttpMethod.POST, params, headers, callback, BypassSSLVerification);
	}

    /**
     * Get the status of one message: Sent or queued
     * 
     * @param messageId The unique identifier of the sent message
	 * @param callback The callback with the result of the service call
     */
    public void GetStatus(final String messageId,  final ServiceEventListener callback) 
    {
    	String  url = _endpoint + "/" + messageId;
		HashMap<String, String> params = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());

        this.ExecuteTask(url, KZHttpMethod.GET, params, headers, callback, BypassSSLVerification);
    }
}
