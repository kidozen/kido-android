package kidozen.client.internal;


public interface KZAction<T> {
	
public void onServiceResponse(T response) throws Exception;
	
}



