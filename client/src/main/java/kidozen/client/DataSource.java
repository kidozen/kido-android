package kidozen.client;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;
import kidozen.client.internal.EAPIEventListener;
import kidozen.client.internal.JsonStringToMap;
import kidozen.client.internal.SyncHelper;
import kidozen.client.internal.Utilities;

/**
 * Created by christian on 2/27/14.
 */
public class DataSource extends KZService {
    private DataSource mSelf = this;
    private String TAG="DataSource";

    /**
     * You should not create a new instances of this constructor. Instead use the DataSource() method of the KZApplication object.
     *
     * @param ds
     * @param name
     * @param provider
     * @param username
     * @param pass
     * @param clientId
     * @param userIdentity
     * @param applicationIdentity
     */
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
        String  url = mEndpoint + "/" + mName + "/";
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        if (timeout>0)
            headers.put(Constants.SERVICE_TIMEOUT_HEADER, Integer.toString(timeout));

        EAPIEventListener serviceCallback = new EAPIEventListener(callback);
        new KZServiceAsyncTask(KZHttpMethod.GET, params, headers, serviceCallback, getStrictSSL()).execute(url);
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
        if (data==null)
            throw new InvalidParameterException("data cannot be null or empty");

        try {
            String url = mEndpoint + "/" + mName + appendJsonAsQueryString(data);
            HashMap<String, String> headers = new HashMap<String, String>();
            if (timeout>0)
                headers.put(Constants.SERVICE_TIMEOUT_HEADER, Integer.toString(timeout));

            EAPIEventListener serviceCallback = new EAPIEventListener(callback);
            new KZServiceAsyncTask(KZHttpMethod.GET, null, headers, serviceCallback, getStrictSSL()).execute(url);

        } catch (Exception e) {
            if (callback!=null)
                callback.onFinish(new ServiceEvent(this, HttpStatus.SC_NOT_FOUND, e.getMessage(), e));
        }
    }


    public JSONObject Query() throws ExecutionException, InterruptedException, TimeoutException, SynchronousException {
        SyncHelper<JSONObject> helper = new SyncHelper(this,"Query", ServiceEventListener.class);
        return helper.Invoke(new Object[] {});
    }

    public JSONObject Query(JSONObject data) throws ExecutionException, InterruptedException, TimeoutException, SynchronousException {
        SyncHelper<JSONObject> helper = new SyncHelper(this,"Query", JSONObject.class, ServiceEventListener.class);
        return helper.Invoke(new Object[] {data});
    }

    public JSONObject Query(JSONObject data, int timeout) throws ExecutionException, InterruptedException, TimeoutException, SynchronousException {
        SyncHelper<JSONObject> helper = new SyncHelper(this,"Query", JSONObject.class, int.class, ServiceEventListener.class);
        return helper.Invoke(new Object[] {data, timeout});
    }

    /**
     * Invokes a DataSource
     *
     * @param timeout  service timeout
     * @param callback The callback with the result of the service call
     */
    public void Invoke(final int timeout,final ServiceEventListener callback) {
        String  url = mEndpoint + "/" + mName + "/";
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
        if (timeout>0)
            headers.put(Constants.SERVICE_TIMEOUT_HEADER, Integer.toString(timeout));

        EAPIEventListener serviceCallback = new EAPIEventListener(callback);
        new KZServiceAsyncTask(KZHttpMethod.POST, params, headers,  new JSONObject(), serviceCallback, getStrictSSL()).execute(url);
    }

    public JSONObject Invoke(int timeout) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONObject>(this, "Invoke", int.class , ServiceEventListener.class)
                .Invoke(new Object[] { timeout });
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
        if (data==null)
            throw new InvalidParameterException("data cannot be null or empty");

        String  url = mEndpoint + "/" + mName;
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
        if (timeout>0)
            headers.put(Constants.SERVICE_TIMEOUT_HEADER, Integer.toString(timeout));

        EAPIEventListener serviceCallback = new EAPIEventListener(callback);
        new KZServiceAsyncTask(KZHttpMethod.POST, null, headers,  data, serviceCallback, getStrictSSL()).execute(url);
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

    public JSONObject Invoke() throws ExecutionException, InterruptedException, TimeoutException, SynchronousException {
        SyncHelper<JSONObject> helper = new SyncHelper(this,"Invoke", ServiceEventListener.class);
        return helper.Invoke(new Object[] {});
    }

    public JSONObject Invoke(JSONObject data) throws ExecutionException, InterruptedException, TimeoutException, SynchronousException {
        SyncHelper<JSONObject> helper = new SyncHelper(this,"Invoke", JSONObject.class, ServiceEventListener.class);
        return helper.Invoke(new Object[] {data});
    }

    public JSONObject Invoke(JSONObject data, int timeout) throws ExecutionException, InterruptedException, TimeoutException, SynchronousException {
        SyncHelper<JSONObject> helper = new SyncHelper(this,"Invoke", JSONObject.class, int.class, ServiceEventListener.class);
        return helper.Invoke(new Object[] {data, timeout});
    }

    private String appendJsonAsQueryString(JSONObject data) {
        JsonStringToMap jsm = new JsonStringToMap();
        String qs = Utilities.MapAsQueryString(jsm.parse(data.toString()), false, null);
        qs = qs.substring(0, qs.length() - 1);
        return "?" + qs;
    }

}
