package kidozen.client;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 5/20/13
 * Time: 1:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Service extends KZService {
    private static final String SERVICETIMEOUT_HEADER = "timeout";
    private String _endpoint = null;
    private String _name = null;

    /**
     * Constructor
     * <p/>
     * You should not create a new instances of this constructor. Instead use the Storage[""] method of the KZApplication object.
     *
     * @param endpoint The service endpoint
     * @param name     The name of the queue to be created
     */
    public Service(String endpoint, String name) {
        _endpoint = endpoint;
        _name = name;
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
    public void InvokeMethod(String method, final JSONObject data, int timeout, final ServiceEventListener callback) {
        String url = _endpoint + "api/services/"+ _name +"/invoke/" + method;
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.AUTHORIZATION_HEADER, CreateAuthHeaderValue());
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
        if (timeout>0)
            headers.put(SERVICETIMEOUT_HEADER, Integer.toString(timeout));

        this.ExecuteTask(url, KZHttpMethod.POST, params, headers,  callback, data, StrictSSL);
    }
}
