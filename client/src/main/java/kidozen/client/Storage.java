package kidozen.client;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;
import kidozen.client.internal.SyncHelper;

/**
 * Storage  service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class Storage extends KZService {
    private static final String LOGCAT_KEY = "Storage";

    /**
     * You should not create a new instances of this constructor. Instead use the Storage() method of the KZApplication object.
     *
     * @param storage
     * @param name
     * @param provider
     * @param username
     * @param pass
     * @param clientId
     * @param userIdentity
     * @param applicationIdentity
     */
    public Storage(String storage, String name, String provider , String username, String pass, String clientId, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(storage, name, provider, username, pass, clientId, userIdentity, applicationIdentity);
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
        validateParameters(message);

        String  url = mEndpoint + "/" + mName;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("isPrivate", (isPrivate ? "true" : "false"));
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

        KZServiceAsyncTask task = new KZServiceAsyncTask(KZHttpMethod.POST,params,headers,message, callback, getStrictSSL());
        task.execute(url);
    }

    /**
     * Creates a new object in the storage synchronously
     *
     * @param message The object to be created
     * @param isPrivate marks the object as private (true) / public (false)
     * @return a json object with storage metadata.
     * @throws TimeoutException
     * @throws SynchronousException
     */
    public JSONObject Create(final JSONObject message, final boolean isPrivate) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONObject>(this,"Create", JSONObject.class , Boolean.TYPE, ServiceEventListener.class)
            .Invoke(new Object[] { message, isPrivate });
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

    /**
     * Creates a new private object in the storage synchronously
     *
     * @param message The object to be created
     * @return a json object with storage metadata.
     * @throws TimeoutException
     * @throws SynchronousException
     */
    public JSONObject Create(final JSONObject message) throws TimeoutException, SynchronousException {
        return this.Create(message, true);
    }

    /**
	 * Updates an object 
	 * 
	 * @param message The updated object
	 * @param id The unique identifier of the object
	 * @param callback The callback with the result of the service call
	 */
	public void Update(final String id, final JSONObject message, final ServiceEventListener callback) {
        JSONObject serializedMsg = null;
        try {
            serializedMsg = checkDateSerialization(message);
        }
        catch (JSONException e) {
            //System.out.println(e.getMessage());
            createServiceEventWithException(e, callback);
        }

        String  url = mEndpoint + "/" + mName + "/" + id;
        HashMap<String, String> params = null;
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

        new KZServiceAsyncTask(KZHttpMethod.PUT,params,headers,serializedMsg,callback, getStrictSSL()).execute(url);
    }

    /**
     * Updates an object synchronously
     *
     * @param message The updated object
     * @param id The unique identifier of the object
     * @return a json object with storage metadata.
     * @throws TimeoutException
     * @throws SynchronousException
    */
    public JSONObject Update(final String id, final JSONObject message) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONObject>(this,"Update", String.class, JSONObject.class , ServiceEventListener.class)
                .Invoke(new Object[] { id, message});
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
			throw new IllegalArgumentException();
		}
        String  url = mEndpoint + "/" + mName + "/" + id;
        new KZServiceAsyncTask(KZHttpMethod.GET ,null,null, callback, getStrictSSL()).execute(url);
	}

    /**
     * Gets an object
     *
     * @param id The unique identifier of the object
     * @return the requested jsonobject
     * @throws TimeoutException
     * @throws SynchronousException
     */
    public JSONObject Get(final String id) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONObject>(this,"Get", String.class, ServiceEventListener.class)
                .Invoke(new Object[] { id });
    }

	/**
	 * Drops the entire storage
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void Drop(final ServiceEventListener callback)
	{
        String  url = mEndpoint + "/" + mName;
        new KZServiceAsyncTask(KZHttpMethod.DELETE,null,null,callback, getStrictSSL()).execute(url);
    }

    /**
     * Drops the entire storage synchronously
     *
     * @return true if the operation was success
     * @throws TimeoutException
     * @throws SynchronousException
     */
    public boolean Drop() throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper(this,"Drop", ServiceEventListener.class);
        helper.Invoke(new Object[] {});
        return (helper.getStatusCode() == HttpStatus.SC_OK);
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
			throw new IllegalArgumentException();
		}
        String  url = mEndpoint + "/" + mName + "/" + idMessage;
        new KZServiceAsyncTask(KZHttpMethod.DELETE,null,null,callback, getStrictSSL()).execute(url);
    }

    /**
     * Deletes a message from the storage synchronously
     *
     * @param idMessage
     * @return true if the operation was success
     * @throws TimeoutException
     * @throws SynchronousException
     */
    public boolean Delete(final String idMessage) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper(this,"Delete", String.class, ServiceEventListener.class);
        helper.Invoke(new Object[] {idMessage});
        return (helper.getStatusCode() == HttpStatus.SC_OK);
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

    public JSONArray All() throws TimeoutException, SynchronousException {
        return this.Query("{}", "{}", "{}");
    }

	/**
	 * Executes a query against the Storage
	 * 
	 * @param query An string with the same syntax used for a MongoDb query
	 * @param callback The callback with the result of the service call
	 */
	public void Query(final String query,final ServiceEventListener callback) 
	{
		if (query == null || query == "") {
			throw new  IllegalArgumentException("query cannot be null or empty");
		}
		this.Query(query, "{}","{}", callback);
	}

    public JSONArray Query ( final String query) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONArray>(this, "Query", String.class  , ServiceEventListener.class)
                .Invoke(new Object[] { query });
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
		if (query == null || query == "" || options == null || options == "" ){
			throw new  IllegalArgumentException("query and options cannot be null or empty");
		}
		this.Query(query, "{}",options, callback);	
	}

    public JSONArray Query ( final String query, final String options) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONArray>(this, "Query", String.class , String.class , ServiceEventListener.class)
                .Invoke(new Object[] { query, options });
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
			throw new  IllegalArgumentException("query, options or fields, cannot be null or empty");
		}
        String  url = mEndpoint + "/" + mName;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("query", query);
        params.put("options", options);
        params.put("fields", fields);
        new KZServiceAsyncTask(KZHttpMethod.GET,params,null,callback, getStrictSSL()).execute(url);
	}

    public JSONArray Query ( final String query, final String fields, final String options) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONArray>(this, "Query", String.class , String.class , String.class , ServiceEventListener.class)
                .Invoke(new Object[] { query, fields, options });
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

    public JSONObject Save (JSONObject message) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONObject>(this, "Save", JSONObject.class , ServiceEventListener.class)
                .Invoke(new Object[] { message });
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

    public JSONObject Save (JSONObject message, Boolean isPrivate ) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONObject>(this, "Save", JSONObject.class , Boolean.class , ServiceEventListener.class)
                .Invoke(new Object[] { message, isPrivate });
    }

    private void createServiceEventWithException(Exception e, ServiceEventListener callback) {
        ServiceEvent se = new ServiceEvent(this);
        se.Response = e;
        se.Body = e.getMessage();
        se.StatusCode = HttpStatus.SC_BAD_REQUEST;
        se.Exception = e;
        callback.onFinish(se);
    }

    private void validateParameters(JSONObject message) {
        if (message == null)
            throw new IllegalArgumentException("message is Null");

        Object id = null;
        try {id = message.get("_id");} catch (JSONException e) {}

        if (id!=null)
            throw new IllegalArgumentException("_id property is not valid for object creation.");

    }


    private JSONObject checkDateSerialization(JSONObject original) throws JSONException {
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
            throw new JSONException(e.getMessage());
        }
        return updatedMessage;
    }

}
