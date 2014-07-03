package kidozen.client;

import org.json.JSONObject;

import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 5/20/13
 * Time: 1:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Service extends KZService {

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

    /**
     * Invokes a LOB method
     *
     * @param method   the method name
     * @param data     the data requested by the method call
     * @param timeout  service timeout
     * @param callback The callback with the result of the service call
     */
    public void InvokeMethod(final String method, final JSONObject data,final int timeout, final ServiceEventListener callback) {

        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {

                String url = mEndpoint + "api/services/"+ mName +"/invoke/" + method;
                HashMap<String, String> params = new HashMap<String, String>();
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
                if (timeout>0)
                    headers.put(Constants.SERVICE_TIMEOUT_HEADER, Integer.toString(timeout));

                new KZServiceAsyncTask(KZHttpMethod.POST, params, headers, data, callback, getStrictSSL()).execute(url);
            }
        });

    }
}
