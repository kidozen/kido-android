package kidozen.client;

import android.util.Log;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import kidozen.client.authentication.KidoZenUser;

/**
 * SMS  service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class SMSSender extends KZService {
	private static final String TAG = "SMSSender";
	String _number;

	public SMSSender(String endpoint, String number, String provider , String username, String pass, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(endpoint, number, provider, username, pass, userIdentity, applicationIdentity);
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
        CreateAuthHeaderValue(_provider,_username,_password,new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {

                String encodedNumber =  URLEncoder.encode(_number);
                String encodedMessage =  URLEncoder.encode(message);
                String  url = mEndpoint;
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("to", encodedNumber);
                params.put("message", encodedMessage);

                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
                new KZServiceAsyncTask(KZHttpMethod.POST, params, headers, callback, StrictSSL).execute(url);
            }
        });

    }

    /**
     * Get the status of one message: Sent or queued
     * 
     * @param messageId The unique identifier of the sent message
	 * @param callback The callback with the result of the service call
     */
    public void GetStatus(final String messageId,  final ServiceEventListener callback) 
    {
        CreateAuthHeaderValue(_provider,_username,_password,new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {

            String  url = mEndpoint + "/" + messageId;
            HashMap<String, String> params = new HashMap<String, String>();
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put(Constants.AUTHORIZATION_HEADER, token);

            new KZServiceAsyncTask(KZHttpMethod.GET, params, headers, callback, StrictSSL).execute(url);
            }
        });
    }
}
