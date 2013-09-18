package kidozen.client;

import java.util.HashMap;
import java.util.Observer;

import org.json.JSONObject;

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

        this.ExecuteTask(_endpoint, KZHttpMethod.POST, params, headers, callback, message, BypassSSLVerification);
	}
}
