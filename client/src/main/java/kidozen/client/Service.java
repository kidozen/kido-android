package kidozen.client;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;
import kidozen.client.internal.SyncHelper;
import kidozen.client.internal.Utilities;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 5/20/13
 * Time: 1:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Service extends KZService {

    /**
     * You should not create a new instances of this constructor. Instead use the LOBService() method of the KZApplication object.
     *
     * @param service
     * @param name
     * @param provider
     * @param username
     * @param pass
     * @param clientId
     * @param userIdentity
     * @param applicationIdentity
     */
    public Service(String service, String name,String provider , String username, String pass, String clientId, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(service,name, provider, username, pass, clientId, userIdentity, applicationIdentity);
    }

    /**
     * Invokes a LOB method
     *
     * @param method   the method name
     * @param data     the data requested by the method call
     * @param callback The callback with the result of the service call
     */
    public void InvokeMethod(String method, final JSONObject data, final ServiceEventListener callback) {
        this.InvokeMethod(method, data, 0, callback);
    }

    public JSONTokener InvokeMethod(String method, JSONObject data) throws TimeoutException, SynchronousException {
        return this.InvokeMethod(method,data,0) ;
    }

    /**
     * Invokes a LOB method
     *
     * @param method   the method name
     * @param data     the data requested by the method call
     * @param timeout  service timeout
     * @param callback The callback with the result of the service call
     */
    public void InvokeMethod(final String method, final JSONObject data,final int timeout, final ServiceEventListener callback) {
        if (method.isEmpty() || method==null)
            throw new IllegalArgumentException(Utilities.GetInvalidParameterMessage("method"));
        if (data==null)
            throw new IllegalArgumentException(Utilities.GetInvalidParameterMessage("data"));
        if (timeout < 0)
            throw new IllegalArgumentException(Utilities.GetInvalidParameterMessage("timeout"));

        String url = mEndpoint + "api/services/"+ mName +"/invoke/" + method;
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
        if (timeout>0)
            headers.put(Constants.SERVICE_TIMEOUT_HEADER, Integer.toString(timeout));

        new KZServiceAsyncTask(KZHttpMethod.POST, params, headers, data, callback, getStrictSSL()).execute(url);
    }

    public JSONTokener InvokeMethod(String method, JSONObject data, int timeout) throws TimeoutException, SynchronousException {
        String result = new SyncHelper<String>(this, "InvokeMethod", String.class, JSONObject.class, int.class , ServiceEventListener.class)
                .Invoke(new Object[] { method, data, timeout });
        return new JSONTokener(result);
    }

}
