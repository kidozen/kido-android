package kidozen.client;

/**
 * Created by christian on 7/29/14.
 */
public abstract class ServiceResponseHandler implements IServiceResponseHandler {

    @Override
    public void OnStart() {

    }

    @Override
    public void OnSuccess(int statusCode, Object response) {

    }

    @Override
    public void OnError(int statusCode, Object errorResponse, Throwable e) {

    }
}
