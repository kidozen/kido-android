package kidozen.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;
import kidozen.client.internal.JsonStringToMap;
import kidozen.client.internal.Utilities;

/**
 * Created by christian on 2/27/14.
 */
public class DataSource extends KZService {
    private DataSource mSelf = this;
    private String TAG="DataSource";

    public DataSource(String ds, String name, String provider , String username, String pass, String clientId, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(ds,name, provider, username, pass, clientId, userIdentity, applicationIdentity);
    }

    /**
     * Invokes a Query DataSource
     *
     * @param timeout  service timeout
     * @param callback The callback with the result of the service call
     */
    public void Query(final int timeout, final ServiceEventListener callback) {
        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {

                String  url = mEndpoint + "/" + mName + "/";
                HashMap<String, String> params = new HashMap<String, String>();
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                if (timeout>0)
                    headers.put(Constants.SERVICE_TIMEOUT_HEADER, Integer.toString(timeout));

                new KZServiceAsyncTask(KZHttpMethod.GET, params, headers, callback, StrictSSL).execute(url);
            }
        });
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
    public void Invoke(final int timeout,final ServiceEventListener callback) {
        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {

                String  url = mEndpoint + "/" + mName + "/";
                HashMap<String, String> params = new HashMap<String, String>();
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
                if (timeout>0)
                    headers.put(Constants.SERVICE_TIMEOUT_HEADER, Integer.toString(timeout));

                new KZServiceAsyncTask(KZHttpMethod.POST, params, headers,  new JSONObject(), callback, StrictSSL).execute(url);
            }
        });

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
    public void Invoke(final JSONObject data, final int timeout, final ServiceEventListener callback) {
        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {

                if (data==null)
                    throw new InvalidParameterException("data cannot be null or empty");

                String  url = mEndpoint + "/" + mName;
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
                if (timeout>0)
                    headers.put(Constants.SERVICE_TIMEOUT_HEADER, Integer.toString(timeout));

                new KZServiceAsyncTask(KZHttpMethod.POST, null, headers,  data, callback, StrictSSL).execute(url);
            }
        });

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
    public void Query(final JSONObject data,final int timeout, final ServiceEventListener callback) {
        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                if (data==null)
                    throw new InvalidParameterException("data cannot be null or empty");

                try {
                    String url = mEndpoint + "/" + mName + appendJsonAsQueryString(data);
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put(Constants.AUTHORIZATION_HEADER, token);
                    if (timeout>0)
                        headers.put(Constants.SERVICE_TIMEOUT_HEADER, Integer.toString(timeout));
                    new KZServiceAsyncTask(KZHttpMethod.GET, null, headers, callback, StrictSSL).execute(url);

                } catch (Exception e) {
                    if (callback!=null)
                        callback.onFinish(new ServiceEvent(this, HttpStatus.SC_NOT_FOUND, e.getMessage(), e));
                }

            }
        });

    }

    private String appendJsonAsQueryString(JSONObject data) {
        JsonStringToMap jsm = new JsonStringToMap();
        String qs = Utilities.MapAsQueryString(jsm.parse(data.toString()), false, null);
        qs = qs.substring(0, qs.length() - 1);
        return "?" + qs;
    }

}
