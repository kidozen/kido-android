package kidozen.client;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import kidozen.client.authentication.KidoZenUser;

import org.json.JSONObject;

import android.util.Log;
/**
 * Mail service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class MailSender extends KZService implements Observer {
	String _endpoint;
	public KZApplication Application;
	private String KEY = "MailSender";

	/**
	 * Constructor
	 * 
	 * @param endpoint The Configuration service endpoint
	 */
	public MailSender(String endpoint)
	{
		_endpoint=endpoint;
	}

	/**
	 * @param mail The Email message to send
	 * @param callback The callback with the result of the service call
	 */
	public void Send(final Mail mail, final ServiceEventListener callback) 
	{
		JSONObject message=null;
		message = new JSONObject(mail.GetHashMap());
		HashMap<String, String> params = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		
		headers.put(AUTHORIZATION_HEADER,this.KidozenUser.Token);
		headers.put(CONTENT_TYPE,APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		ServiceInvokeAsyncTask task = new ServiceInvokeAsyncTask(KZHttpMethod.POST, params, headers, message, callback, BypassSSLVerification);
		task.execute(_endpoint);
	}

	public void update(Observable observable, Object data) {
		Log.d(KEY, "token updated");
		this.KidozenUser = (KidoZenUser) data;
	}
}
