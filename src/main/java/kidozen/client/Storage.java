package kidozen.client;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Storage  service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class Storage extends KZService {
	private static final String DATEFORMAT_YYYYMMDDTHHMMSSSSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final String KEY = "Storage";
	String _endpoint;
	String _name;
	
	/**
	 * Constructor
	 * 
	 * You should not create a new instances of this constructor. Instead use the Storage[""] method of the KZApplication object. 
	 * @param endpoint The service endpoint
	 * @param name The name of the queue to be created
	 */
	public Storage(String endpoint, String name)
	{
		_endpoint=endpoint;
		_name = name;		
	}

    /**
     * Creates a new object in the storage
     *
     * @param message The object to be created
     * @param isPrivate marks the object as private (true) / publc (false)
     * @param callback The callback with the result of the service call
     */
    public void Create(final JSONObject message, final boolean isPrivate, final ServiceEventListener callback)
    {
        String  url = _endpoint + "/" + _name;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("isPrivate", (isPrivate ? "true" : "false"));
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
        headers.put(CONTENT_TYPE,APPLICATION_JSON);
        headers.put(ACCEPT, APPLICATION_JSON);

        this.ExecuteTask(url, KZHttpMethod.POST, params, headers,  callback, message, BypassSSLVerification);
    }

    /**
	 * Creates a new private object in the storage
	 * 
	 * @param message The object to be created
	 * @param callback The callback with the result of the service call
	 */
	public void Create(final JSONObject message, final ServiceEventListener callback)
	{
        this.Create(message, true, callback);
	}

	protected JSONObject checkDateSerialization(JSONObject original) throws JSONException {
		JSONObject updatedMessage = original;
		JSONObject updatedMetadata = null;
		try {
			JSONObject _metadata = original.getJSONObject("_metadata");
			if (_metadata.get("createdOn").getClass().getName().toLowerCase().contains("date")) {
				Date createdON = (Date) _metadata.get("createdOn");
				SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT_YYYYMMDDTHHMMSSSSS_Z);
				_metadata.remove("createdOn");
				_metadata.put("createdOn", dateFormat.format(createdON));
				updatedMetadata = _metadata;
			}
			if (_metadata.get("updatedOn").getClass().getName().toLowerCase().contains("date")) {
				Date createdON = (Date) _metadata.get("updatedOn");
				SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT_YYYYMMDDTHHMMSSSSS_Z);
				_metadata.remove("updatedOn");
				_metadata.put("updatedOn", dateFormat.format(createdON));
				updatedMetadata = _metadata;
			}
			if (updatedMetadata!=null) {
				updatedMessage.remove("_metadata");
				updatedMessage.put("_metadata", updatedMetadata);
			}
		} catch (JSONException e) {
            throw e;
		}
		return updatedMessage;
	}

	/**
	 * Updates an object 
	 * 
	 * @param message The updated object
	 * @param id The unique identifier of the object
	 * @param callback The callback with the result of the service call
	 */
	public void Update(final String id, final JSONObject message, final ServiceEventListener callback) throws Exception {
		try {
			JSONObject serializedMsg = this.checkDateSerialization(message);
			String  url = _endpoint + "/" + _name + "/" + id;
			HashMap<String, String> params = null;
			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
			headers.put(CONTENT_TYPE,APPLICATION_JSON);
			headers.put(ACCEPT, APPLICATION_JSON);
            this.ExecuteTask(url, KZHttpMethod.PUT, params, headers,  callback, serializedMsg, BypassSSLVerification);
		}
        catch (Exception e)
        {
			ServiceEvent se = new ServiceEvent(this);
			se.Response = e;
			se.Body = e.getMessage();
			se.StatusCode = HttpStatus.SC_BAD_REQUEST;
            se.Exception = e;
			callback.onFinish(se);
		}
	}

	/**
	 * Gets an object
	 * 
	 * @param id The unique identifier of the object
	 * @param callback The callback with the result of the service call
	 */
	public void Get(final String id, final ServiceEventListener callback)
	{
		if (id=="" || id==null) {
			throw new InvalidParameterException();
		}
		String  url = _endpoint + "/" + _name + "/" + id;
		HashMap<String, String> params = null;
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());

		ServiceEventListener sel = new ServiceEventListener() {
			@Override
			public void onFinish(ServiceEvent e) {
				if(e.Exception==null)
					callback.onFinish(e);
			}
		};
        this.ExecuteTask(url, KZHttpMethod.GET, params, headers,  callback, BypassSSLVerification);
	}

	/**
	 * Drops the entire storage
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void Drop(final ServiceEventListener callback)
	{
		String  url = _endpoint + "/" + _name;
		HashMap<String, String> params = null;
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
        this.ExecuteTask(url, KZHttpMethod.DELETE, params, headers,  callback, BypassSSLVerification);
	}

	/**
	 * Deletes a message from the storage
	 * 
	 * @param idMessage The unique identifier of the object
	 * @param callback The callback with the result of the service call
	 */
	public void Delete(final String idMessage,final ServiceEventListener callback)
	{
		if (idMessage=="" || idMessage==null) {
			throw new InvalidParameterException();
		}
		String  url = _endpoint + "/" + _name + "/" + idMessage;
		HashMap<String, String> params = null;
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
        this.ExecuteTask(url, KZHttpMethod.DELETE, params, headers,  callback, BypassSSLVerification);
	}

	/**
	 * Returns all the objects from the storage
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void All(final ServiceEventListener callback)
	{
		this.Query("{}", "{}","{}", callback);
	}

	/**
	 * Executes a query against the Storage
	 * 
	 * @param query An string with the same syntax used for a MongoDb query
	 * @param callback The callback with the result of the service call
	 */
	public void Query(final String query,final ServiceEventListener callback) 
	{
		if (query == null || query == "")
		{
			throw new  InvalidParameterException("query cannot be null or empty");
		}
		this.Query(query, "{}","{}", callback);
	}
	/**
	 * Executes a query against the Storage
	 * 
	 * @param query An string with the same syntax used for a MongoDb query
	 * @param options An string with the same syntax used for a MongoDb query options
	 * @param callback The callback with the result of the service call
	 */
	public void Query(final String query, final String options,final ServiceEventListener callback)
	{
		if (query == null || query == "" || options == null || options == "" )
		{
			throw new  InvalidParameterException("query and options cannot be null or empty");
		}
		this.Query(query, "{}",options, callback);	
	}

	/**
	 * Executes a query against the Storage
	 * 
	 * @param query An string with the same syntax used for a MongoDb query
	 * @param options An string with the same syntax used for a MongoDb query options
	 * @param fields The fields to retrieve
	 * @param callback The callback with the result of the service call
	 */
	public void Query(final String query, final String fields, final String options,final ServiceEventListener callback)
	{
		if (query == null || query == "" || options == null || options == "" || fields == null || fields == "")
		{
			throw new  InvalidParameterException("query, options or fields, cannot be null or empty");
		}
		String  url = _endpoint + "/" + _name;
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("query", query);
		params.put("options", options);
		params.put("fields", fields);
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
        this.ExecuteTask(url, KZHttpMethod.GET, params, headers,  callback, BypassSSLVerification);
	}
}
