package kidozen.client;

/**
 * Created by christian on 7/29/14.
 */
public interface IServiceResponseHandler {
    public void OnStart();

    public void OnSuccess(int statusCode, Object response);

    public void OnError(int statusCode, Object errorResponse, Throwable e);
}
