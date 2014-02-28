package kidozen.client;

import org.json.JSONObject;
import java.util.HashMap;

/**
 * Created by christian on 2/27/14.
 */
public class DataSource extends KZService {
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
    public DataSource(String endpoint, String name) {
        _endpoint = endpoint;
        _name = name;
    }

    /**
     * Invokes a Query DataSource
     *
     * @param callback The callback with the result of the service call
     */
    public void Query(final ServiceEventListener callback) {
        String  url = _endpoint + "/" + _name;
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
        this.ExecuteTask(url, KZHttpMethod.GET, params, headers,  callback, BypassSSLVerification);
    }

    /**
     * Invokes a LOB method
     *
     * @param callback The callback with the result of the service call
     */
    public void Invoke(final ServiceEventListener callback) {
        String  url = _endpoint + "/" + _name;
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(AUTHORIZATION_HEADER, CreateAuthHeaderValue());
        headers.put(CONTENT_TYPE, APPLICATION_JSON);
        headers.put(ACCEPT, APPLICATION_JSON);

        this.ExecuteTask(url, KZHttpMethod.POST, params, headers,  callback, new JSONObject(), BypassSSLVerification);
    }
}
