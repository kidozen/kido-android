package kidozen.client;

/**
 * Created by christian on 7/29/14.
 */
public interface IServiceResponseHandler {
    public void onStart();

    public void onSuccess(int statusCode, Object response);

    public void onError(int statusCode, Object errorResponse, Throwable e);
}
