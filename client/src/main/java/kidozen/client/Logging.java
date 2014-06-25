package kidozen.client;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

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

    /**
     * Writes a new entry in the application Log
     *
     * @param message The message you want to save
     * @param level The log level: Verbose, Information, Warning, Error, Critical
     * @param callback The callback with the result of the service call
     */
    public void Write(final String message, final LogLevel level, final ServiceEventListener callback)
    {
        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                InputStream is = null;
                try {
                    String messageEnvelope = String.format("\"%s\"", message);
                    is = new ByteArrayInputStream(messageEnvelope.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                Integer lvl = level.ordinal();
                String url = String.format("%s/log?level=%s",mEndpoint, level.ordinal());

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("level", lvl.toString());
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
                mSelf.ProcessAsStream = true;
                new KZServiceAsyncTask(KZHttpMethod.POST, params, headers, is, callback, StrictSSL).execute(url);
            }
        });
    }

	/**
	 * Writes a new entry in the application Log
	 * 
	 * @param message The message you want to save
	 * @param level The log level: Verbose, Information, Warning, Error, Critical
	 * @param callback The callback with the result of the service call
	 */
	public void Write(final JSONObject message, final LogLevel level, final ServiceEventListener callback) 
	{
        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                Integer lvl = level.ordinal();
                String url = String.format("%s/log?level=%s",mEndpoint, level.ordinal());

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("level", lvl.toString());
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
                new KZServiceAsyncTask(KZHttpMethod.POST, params, headers, message, callback, StrictSSL).execute(url);
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
                String url = String.format("%s/log/",mEndpoint);

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
		this.Query("{}", "{}", callback);
	}

	/**
	 * Executes a query against the Log
	 * 
	 * @param query An string with the same syntax used for a MongoDb query
	 * @param callback The callback with the result of the service call
	 */
	public void Query(final String query,final ServiceEventListener callback) 
	{
		if (query == null )
		{
			throw new  InvalidParameterException("query cannot be null or empty");
		}
		if (query == "")
		{
			throw new  InvalidParameterException("query cannot be null or empty");
		}
		this.Query(query, "{}", callback);
	}

	/**
	 * Executes a query against the Log
	 * 
	 * @param query An string with the same syntax used for a MongoDb query
	 * @param options An string with the same syntax used for a MongoDb query options
	 * @param callback The callback with the result of the service call
	 */
	public void Query(final String query, final String options,final ServiceEventListener callback)
	{
		
		if (query == null )
		{
			throw new  InvalidParameterException("query cannot be null");
		}
		if (query == "" || query.isEmpty())
		{
			throw new  InvalidParameterException("query cannot be empty");
		}
		if (options == null )
		{
			throw new  InvalidParameterException("options cannot be null");
		}
		if (options == "" || options.isEmpty())
		{
			throw new  InvalidParameterException("options cannot be empty");
		}
        CreateAuthHeaderValue(new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                try
                {
                    String fixedQuery = checkDateTimeInQuery(query);
                    String url = String.format("%s/log",mEndpoint);

                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("query", fixedQuery);
                    params.put("options", options);
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put(Constants.AUTHORIZATION_HEADER,token);
        

                    ServiceEventListener se = new ServiceEventListener() {

                        @Override
                        public void onFinish(ServiceEvent e) {
                            if(e.Exception==null)
                                callback.onFinish( serializeJsonArray( e ) );
                        }
                    };

                    new KZServiceAsyncTask(KZHttpMethod.GET,params,headers,callback, StrictSSL).execute(url);
                }
                catch (Exception e)
                {
                    ServiceEvent se = new ServiceEvent(this);
                    se.Exception = e;
                    se.Body = e.getMessage();
                    se.StatusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                    callback.onFinish(se);
                }
            }
        });
	}

	private ServiceEvent serializeJsonArray(ServiceEvent e) 
	{
		ServiceEvent updatedse = e;
		try {
			JSONArray response =(JSONArray) e.Response;
			JSONArray serializedresponse = new JSONArray();
			for (int i = 0; i < response.length(); i++) {
				JSONObject itm = response.getJSONObject(i);
				serializedresponse.put(serializeMessage(itm));
				updatedse.Response = serializedresponse;
			}

		} catch (Exception e1) {
			updatedse.Response = e1;
			updatedse.Body = e1.getMessage();
			updatedse.StatusCode = HttpStatus.SC_BAD_REQUEST;
		}
		return updatedse;		
	}
	
	private JSONObject serializeMessage(JSONObject e) throws Exception {
		JSONObject message = e;
		if (message!=null) {
			Date upd = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'").parse(message.getString("dateTime"));
			message.remove("dateTime");
			message.put("dateTime",upd);
		}
		return message;
	}
	
	private String checkDateTimeInQuery(String query) throws JSONException {
		if (query ==null || query.equals("null")) {
			query = "{}";	
		}
		if (query.isEmpty() ) {
			query = "{}";
		}
		String value = "";
		try {
			if (query.indexOf("dateTime")<0) {
				HashMap<String, Boolean> dt = new HashMap<String, Boolean>();
				dt.put("$exists", true);
				
				value = new JSONStringer().object()
							.key("dateTime")
							.value(new JSONObject(dt))
						.endObject().toString();
			}
			else {
				value = query;
			}
		} catch (JSONException e) {
			throw e;
		}
		return value;
	}

    public void Write(String message, LogLevel level, Object callback) {

    }
}
