package kidozen.client;

import com.google.gson.Gson;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;
import kidozen.client.internal.SyncHelper;

/**
 * Log service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class Logging extends KZService {
    private final Logging mSelf;

    public Logging(String logging, String provider , String username, String pass, String clientId, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(logging,"", provider, username, pass, clientId, userIdentity, applicationIdentity);
        mSelf = this;

    }

    private String createLogEndpoint(String message, LogLevel level) {
        if (message!=null) try {
            message = String.format("&message=%s", URLEncoder.encode(message,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            message = String.format("&message=%s", URLEncoder.encode(message));
        }
        String logEndpoint = String.format("%s?level=%s",mEndpoint, level.ordinal());
        return (message!=null ? logEndpoint + message : logEndpoint);
    }

    public void Write(final String message, final ArrayList data, final LogLevel level, final ServiceEventListener callback)
    {
        final String jMessage = new LogSerializer<ArrayList>().ToJsonString(data);

        Integer lvl = level.ordinal();
        String url = createLogEndpoint(message, level);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("level", lvl.toString());
        HashMap<String, String> headers = new HashMap<String, String>();

        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

        new KZServiceAsyncTask(KZHttpMethod.POST, null, headers, jMessage, callback, getStrictSSL()).execute(url);
    }

    public void Write(String message, ArrayList data, LogLevel level) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper(this, "Write", String.class, ArrayList.class, LogLevel.class, ServiceEventListener.class);
        helper.Invoke(new Object[]{ message, data, level });
        if (helper.getStatusCode() != HttpStatus.SC_CREATED) throw new SynchronousException(helper.getError().toString());
    }


    public void Write(final String message,final Map data,final  LogLevel level,final  ServiceEventListener callback) {
        final String jMessage = new LogSerializer<Map>().ToJsonString(data);

        Integer lvl = level.ordinal();
        String url = createLogEndpoint(message, level);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("level", lvl.toString());
        HashMap<String, String> headers = new HashMap<String, String>();

        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

        new KZServiceAsyncTask(KZHttpMethod.POST, null, headers, jMessage, callback, getStrictSSL()).execute(url);
    }

    public void Write(String message, Map data, LogLevel level) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper(this, "Write", String.class, Map.class, LogLevel.class, ServiceEventListener.class);
        helper.Invoke(new Object[]{ message, data, level });
        if (helper.getStatusCode() != HttpStatus.SC_CREATED) throw new SynchronousException(helper.getError().toString());
    }

    public void Write(final String message, final int data, final LogLevel level, final ServiceEventListener callback)
    {
        final String jMessage = new LogSerializer<Integer>().ToJsonString(data);
        Integer lvl = level.ordinal();
        String url = createLogEndpoint(message, level);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("level", lvl.toString());
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

        new KZServiceAsyncTask(KZHttpMethod.POST, null, headers, jMessage, callback, getStrictSSL()).execute(url);
    }

    public void Write(String message, int data, LogLevel level) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper(this, "Write", String.class, int.class, LogLevel.class, ServiceEventListener.class);
        helper.Invoke(new Object[]{ message, data, level });
        if (helper.getStatusCode() != HttpStatus.SC_CREATED) throw new SynchronousException(helper.getError().toString());
    }

    public void Write(final String message, final String data, final LogLevel level, final ServiceEventListener callback)
    {
        final String jMessage = new LogSerializer<String>().ToJsonString(data);

        Integer lvl = level.ordinal();
        String url = createLogEndpoint(message, level);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("level", lvl.toString());
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

        new KZServiceAsyncTask(KZHttpMethod.POST, null, headers, jMessage, callback, getStrictSSL()).execute(url);
    }

    public void Write(String message, String data, LogLevel level) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper(this, "Write", String.class, String.class, LogLevel.class, ServiceEventListener.class);
        helper.Invoke(new Object[]{ message, data, level });
        if (helper.getStatusCode() != HttpStatus.SC_CREATED) throw new SynchronousException(helper.getError().toString());
    }


	public void Write(final String message, final JSONObject data, final LogLevel level, final ServiceEventListener callback)
	{
        Integer lvl = level.ordinal();
        String url = createLogEndpoint(message, level);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("level", lvl.toString());
        HashMap<String, String> headers = new HashMap<String, String>();

        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
        new KZServiceAsyncTask(KZHttpMethod.POST, null, headers, data, callback, getStrictSSL()).execute(url);
	}

    public void Write(String message, JSONObject data, LogLevel level) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper(this, "Write", String.class, JSONObject.class, LogLevel.class, ServiceEventListener.class);
        helper.Invoke(new Object[]{ message, data, level });
        if (helper.getStatusCode() != HttpStatus.SC_CREATED) throw new SynchronousException(helper.getError().toString());
    }

	/**
	 * Clears the log. 
	 * This method add a new entry to the Log with the information of the user that executes this action
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void Clear(final ServiceEventListener callback) 
	{
            String url = String.format("%s/log/", mEndpoint);

            HashMap<String, String> params = null;
            HashMap<String, String> headers = new HashMap<String, String>();
            new KZServiceAsyncTask(KZHttpMethod.DELETE, params, headers, callback, getStrictSSL()).execute(mEndpoint);
	}

    public boolean Clear() throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper<String>(this, "Clear", ServiceEventListener.class);
        helper.Invoke(new Object[]{});
        return (helper.getStatusCode() == HttpStatus.SC_NO_CONTENT);
    }

	/**
	 * Retrieves all the Log entries
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void All(final ServiceEventListener callback) 
	{
		this.Query("{\"query\":{\"match_all\":{}}}", callback);
	}

    public JSONArray All() throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONArray>(this, "All", ServiceEventListener.class)
                .Invoke(new Object[]{ });
    }

	/**
	 * Executes a query against the Log
	 * 
	 * @param query An string with the same syntax used for a LogStash query
	 * @param callback The callback with the result of the service call
	 */
	public void Query(final String query, final ServiceEventListener callback)
	{
		if (query == null )
		{
			throw new  InvalidParameterException("query cannot be null");
		}
		if (query == "" || query.isEmpty())
		{
			throw new  InvalidParameterException("query cannot be empty");
		}

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("query", query);
        HashMap<String, String> headers = new HashMap<String, String>();
        new KZServiceAsyncTask(KZHttpMethod.GET,params,headers,callback, getStrictSSL()).execute(mEndpoint);
	}

    public JSONArray Query(String query) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONArray>(this, "Query", String.class, ServiceEventListener.class)
                .Invoke(new Object[]{ query });
    }

    class LogSerializer<T> {
        public String ToJsonString (T message) {
            Gson gson = new Gson();
            return gson.toJson(message);
        }
    }

}
