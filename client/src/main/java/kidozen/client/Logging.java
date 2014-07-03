package kidozen.client;

import com.google.gson.Gson;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;

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
        if (message!=null) message = String.format("&message=%s",message);
        String logEndpoint = String.format("%s?level=%s",mEndpoint, level.ordinal());
        return (message!=null ? logEndpoint + message : logEndpoint);
    }

    public void Write(final String message, final ArrayList data, final LogLevel level, final ServiceEventListener callback)
    {
        final String jMessage = new LogSerializer<ArrayList>().ToJsonString(data);

        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                Integer lvl = level.ordinal();
                String url = createLogEndpoint(message, level);

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("level", lvl.toString());
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

                new KZServiceAsyncTask(KZHttpMethod.POST, null, headers, jMessage, callback, StrictSSL).execute(url);
            }
        });
    }


    public void Write(final String message,final Map data,final  LogLevel level,final  ServiceEventListener callback) {
        final String jMessage = new LogSerializer<Map>().ToJsonString(data);

        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                Integer lvl = level.ordinal();
                String url = createLogEndpoint(message, level);

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("level", lvl.toString());
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

                new KZServiceAsyncTask(KZHttpMethod.POST, null, headers, jMessage, callback, StrictSSL).execute(url);
            }
        });
    }


    public void Write(final String message, final int data, final LogLevel level, final ServiceEventListener callback)
    {
        final String jMessage = new LogSerializer<Integer>().ToJsonString(data);

        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                Integer lvl = level.ordinal();
                String url = createLogEndpoint(message, level);

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("level", lvl.toString());
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

                new KZServiceAsyncTask(KZHttpMethod.POST, null, headers, jMessage, callback, StrictSSL).execute(url);
            }
        });
    }

    public void Write(final String message, final String data, final LogLevel level, final ServiceEventListener callback)
    {
        final String jMessage = new LogSerializer<String>().ToJsonString(data);

        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                Integer lvl = level.ordinal();
                String url = createLogEndpoint(message, level);

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("level", lvl.toString());
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

                new KZServiceAsyncTask(KZHttpMethod.POST, null, headers, jMessage, callback, StrictSSL).execute(url);
            }
        });
    }


	public void Write(final String message, final JSONObject data, final LogLevel level, final ServiceEventListener callback)
	{
        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                Integer lvl = level.ordinal();
                String url = createLogEndpoint(message, level);

                HashMap<String, String> params = new HashMap<String, String>();
                //params.put("level", lvl.toString());
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
                new KZServiceAsyncTask(KZHttpMethod.POST, null, headers, data, callback, StrictSSL).execute(url);
            }
        });
	}

	/**
	 * Clears the log. 
	 * This method add a new entry to the Log with the information of the user that executes this action
	 * 
	 */
	public void Clear()
	{
		this.Clear(null);
	}

	/**
	 * Clears the log. 
	 * This method add a new entry to the Log with the information of the user that executes this action
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void Clear(final ServiceEventListener callback) 
	{
        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                String url = String.format("%s/log/", mEndpoint);

                HashMap<String, String> params = null;
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                new KZServiceAsyncTask(KZHttpMethod.DELETE, params, headers, callback, StrictSSL).execute(mEndpoint);
            }
        });
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

	/**
	 * Executes a query against the Log
	 * 
	 * @param query An string with the same syntax used for a MongoDb query
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
        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("query", query);
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER,token);

                new KZServiceAsyncTask(KZHttpMethod.GET,params,headers,callback, StrictSSL).execute(mEndpoint);
            }
        });
	}

    class LogSerializer<T> {
        public String ToJsonString (T message) {
            Gson gson = new Gson();
            return gson.toJson(message);
        }
    }

}
