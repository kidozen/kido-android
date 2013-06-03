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
public class LOBService extends KZService {
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
    public LOBService(String endpoint, String name) {
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
        String url = _endpoint + "api/services/"+ _name +"/invoke/" + method;
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(AUTHORIZATION_HEADER, CreateAuthHeaderValue());
        headers.put(CONTENT_TYPE, APPLICATION_JSON);
        headers.put(ACCEPT, APPLICATION_JSON);

        this.ExecuteTask(url, KZHttpMethod.POST, params, headers,  callback, data, BypassSSLVerification);
    }
}

/*
* {
    "op": "list",
    "path": "/",
    "username": "gus@kidozen.com",
    "password": "kidozen*1"
}
* */