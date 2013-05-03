package kidozen.client;

import java.util.EventObject;

/**
 * Service Event information. This class contains the result of a service invocation
 * 
 * @author kidozen
 * @version 1.00, April 2013
 *
 */
public class ServiceEvent extends EventObject {
	private static final long serialVersionUID = 3148475031616903456L;
	/**
	 * The service call HTTP Status code
	 */
	public int StatusCode;
	/**
	 * The HTTP Response body as string
	 */
	public String Body="";
	/**
	 * The service response. It could be a JSONObject, an JArray or any other object
	 */
	public Object Response = null;
	/**
	 * The last Exception is there was one
	 */
	public Exception Exception = null;
	public String Type;
	
	public ServiceEvent(Object source,int statusCode, String body, Object response) {
		super(source);
		this.StatusCode = statusCode;
		this.Body = body;
		this.Response = response;
		this.Type = source.toString();
	}
	public ServiceEvent(Object source,int statusCode, String body, Object response, Exception e) {
		this(source, statusCode, body, response);
		this.Exception = e;
	}
	public ServiceEvent(Object source) {
		super(source);
	}

}
