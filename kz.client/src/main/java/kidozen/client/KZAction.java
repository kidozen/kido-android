package kidozen.client;


public interface KZAction<T> {
	
public void onServiceResponse(T response) throws Exception;
	
}



