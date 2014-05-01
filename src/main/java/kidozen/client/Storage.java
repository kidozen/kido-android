package kidozen.client;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;

/**
 * Storage  service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class Storage extends KZService {
    private static final String LOGCAT_KEY = "Storage";

    public Storage(String storage, String name, String provider , String username, String pass, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(storage, name, provider, username, pass, userIdentity, applicationIdentity);
    }

    /**
     * Creates a new object in the storage
     *
     * @param message The object to be created
     * @param isPrivate marks the object as private (true) / public (false)
     * @param callback The callback with the result of the service call
     */
    public void Create(final JSONObject message, final boolean isPrivate, final ServiceEventListener callback)
    {
        Object id = null;
        try {id = message.get("_id");} catch (JSONException e) {}

        if (id!=null)
        {
            String errInfo = "_id property is not valid for object creation.";
            ServiceEvent se = new ServiceEvent(this,HttpStatus.SC_CONFLICT,errInfo,errInfo, new Exception(errInfo));
            callback.onFinish(se);
            return;
        }

        CreateAuthHeaderValue(_provider,_username,_password,new KZServiceEvent<String>() {
            @Override
            public void Fire(String message) {
                String  url = mEndpoint + "/" + nName;
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("isPrivate", (isPrivate ? "true" : "false"));
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, message);
                headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
                // TODO: change true for StrictSSL
                new KZServiceAsyncTask(KZHttpMethod.POST,params,headers,callback, true).execute(url);
            }
        });
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
				SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.GMT_DATE_FORMAT);
				_metadata.remove("createdOn");
				_metadata.put("createdOn", dateFormat.format(createdON));
				updatedMetadata = _metadata;
			}
			if (_metadata.get("updatedOn").getClass().getName().toLowerCase().contains("date")) {
				Date createdON = (Date) _metadata.get("updatedOn");
				SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.GMT_DATE_FORMAT);
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
			String  url = mEndpoint + "/" + nName + "/" + id;
			HashMap<String, String> params = null;
			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());
			headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
			headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
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
		String  url = mEndpoint + "/" + nName + "/" + id;
		HashMap<String, String> params = null;
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());

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
		String  url = mEndpoint + "/" + nName;
		HashMap<String, String> params = null;
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());
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
		String  url = mEndpoint + "/" + nName + "/" + idMessage;
		HashMap<String, String> params = null;
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());
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
		String  url = mEndpoint + "/" + nName;
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("query", query);
		params.put("options", options);
		params.put("fields", fields);
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());
        this.ExecuteTask(url, KZHttpMethod.GET, params, headers,  callback, BypassSSLVerification);
	}

    /**
     * Upserts an object
     *
     * if message has the '_id' property the object is updated. If not is created
     *
     * @param message The object
     * @param callback The callback with the result of the service call
     *
     * Remarks:
     * Due to this method is a wrapper if you want to update an object you must add the '_metadata' property
     */
    public void Save(JSONObject message, final ServiceEventListener callback) throws Exception {
        this.Save(message, true, callback);
    }

    /**
     * Upserts an object
     *
     * if message has the '_id' property the object is updated. If not is created
     *
     * @param message The object
     * @param isPrivate marks the object as private (true) / public (false)
     * @param callback The callback with the result of the service call
     *
     * Remarks:
     * Due to this method is a wrapper if you want to update an object you must add the '_metadata' property
     */

    public void Save(JSONObject message, Boolean isPrivate, final ServiceEventListener callback) throws Exception {
        String id = null;
        try {id = message.getString("_id");} catch (JSONException e) {}

        if (id!=null)
        {
            Update(id,message,callback);
        }
        else
        {
            Create(message, isPrivate, callback);
        }
    }
}
