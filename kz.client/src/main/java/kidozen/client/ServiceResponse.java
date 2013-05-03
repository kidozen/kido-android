package kidozen.client;

import java.util.EventObject;

public class ServiceResponse extends EventObject {
	public String Response;
	private static final long serialVersionUID = 1L;

	public ServiceResponse(Object source) {
		super(source);
	}
	
}
