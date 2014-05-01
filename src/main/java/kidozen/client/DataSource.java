package kidozen.client;

import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.HashMap;

/**
 * Created by christian on 2/27/14.
 */
public class DataSource extends KZService {
    private static final String SERVICETIMEOUT_HEADER = "timeout";

    private String _endpoint = null;
    private String _name = null;

    /**
     * Constructor
     * <p/>
     * You should not create a new instances of this constructor. Instead use the DataSource[""] method of the KZApplication object.
     *
     * @param endpoint The service endpoint
     * @param name     The name of the datasource to be created
     */
    public DataSource(String endpoint, String name) {
        _endpoint = endpoint;
        _name = name;
    }

    /**
     * Invokes a Query DataSource
     *
     * @param timeout  service timeout
     * @param callback The callback with the result of the service call
     */
    public void Query(int timeout, final ServiceEventListener callback) {
        String  url = _endpoint + "/" + _name + "/";
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());
        if (timeout>0)
            headers.put(SERVICETIMEOUT_HEADER, Integer.toString(timeout));

        this.ExecuteTask(url, KZHttpMethod.GET, params, headers,  callback, BypassSSLVerification);
    }
    /**
     * Invokes a Query DataSource
     *
     * @param callback The callback with the result of the service call
     */
    public void Query(final ServiceEventListener callback) {
        this.Query(0,callback);
    }

    /**
     * Invokes a DataSource
     *
     * @param timeout  service timeout
     * @param callback The callback with the result of the service call
     */
    public void Invoke(int timeout,final ServiceEventListener callback) {
        String  url = _endpoint + "/" + _name + "/";
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.AUTHORIZATION_HEADER, CreateAuthHeaderValue());
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
        if (timeout>0)
            headers.put(SERVICETIMEOUT_HEADER, Integer.toString(timeout));

        this.ExecuteTask(url, KZHttpMethod.POST, params, headers,  callback, new JSONObject(), BypassSSLVerification);
    }
    /**
     * Invokes a DataSource
     *
     * @param callback The callback with the result of the service call
     */
    public void Invoke(final ServiceEventListener callback) {
        this.Invoke(0,callback);
    }

    /**
     * Invokes a DataSource
     *
     * @param data The Json object for the datasource call
     * @param timeout  service timeout
     * @param callback The callback with the result of the service call
     *
     */
    public void Invoke(final JSONObject data, int timeout, final ServiceEventListener callback) {
        if (data==null)
            throw new InvalidParameterException("data cannot be null or empty");

        String  url = _endpoint + "/" + _name;
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.AUTHORIZATION_HEADER, CreateAuthHeaderValue());
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
        if (timeout>0)
            headers.put(SERVICETIMEOUT_HEADER, Integer.toString(timeout));

        this.ExecuteTask(url, KZHttpMethod.POST, params, headers,  callback, data, BypassSSLVerification);
    }

    /**
     * Invokes a DataSource
     *
     * @param data The Json object for the datasource call
     * @param callback The callback with the result of the service call
     *
     */
    public void Invoke(final JSONObject data, final ServiceEventListener callback) {
        this.Invoke (data, 0, callback);
    }


    /**
     * Invokes a Query DataSource
     *
     * @param callback The callback with the result of the service call
     * @param data The Json object for the datasource call
     *
     */
    public void Query(final JSONObject data, final ServiceEventListener callback) {
        this.Query(data, 0, callback);
    }

    /**
     * Invokes a Query DataSource
     *
     * @param data The Json object for the datasource call
     * @param timeout  service timeout
     * @param callback The callback with the result of the service call
     *
     */
    public void Query(final JSONObject data,int timeout, final ServiceEventListener callback) {
        if (data==null)
            throw new InvalidParameterException("data cannot be null or empty");

        String  url = _endpoint + "/" + _name + "?" + data.toString();
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.AUTHORIZATION_HEADER, CreateAuthHeaderValue());
        if (timeout>0)
            headers.put(SERVICETIMEOUT_HEADER, Integer.toString(timeout));
        this.ExecuteTask(url, KZHttpMethod.GET, params, headers,  callback, BypassSSLVerification);
    }
}
