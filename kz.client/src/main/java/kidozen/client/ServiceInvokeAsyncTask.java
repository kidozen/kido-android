package kidozen.client;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;

public class ServiceInvokeAsyncTask extends AsyncTask<String, Void, Void> {
	HashMap<String, String> _params = null;
	HashMap<String, String> _headers = null;
	KZHttpMethod _method= null;
	ServiceEventListener _callback= null;
	ServiceEvent _event = null;
	JSONObject _message;
	Boolean _bypassSSLValidation;


	public ServiceInvokeAsyncTask(KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, ServiceEventListener callback, Boolean bypassSSLValidation)
	{
		_headers = headers;
		_method = method;
		_callback = callback;
		_bypassSSLValidation = bypassSSLValidation;
		if (params!=null) {
			_params = params;
		}
	}
	public ServiceInvokeAsyncTask(KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, JSONObject message, ServiceEventListener callback, Boolean bypassSSLValidation)
	{
		this(method,params, headers,  callback, bypassSSLValidation);
		_message = message;
	}

	@Override
	protected Void doInBackground(String... params) {
		int statusCode = HttpStatus.SC_BAD_REQUEST;
		
		String body = null;
		Hashtable<String, String> requestProperties = new Hashtable<String, String>();
		Iterator<Entry<String, String>> it = _headers.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> pairs = it.next();
			requestProperties.put(pairs.getKey(), pairs.getValue());
			it.remove();
		}
		try
		{
			String  url = params[0];
			Hashtable<String, String> response =null;
			String msg = _message == null ? null : _message.toString();
			switch (_method) {
				case DELETE:
					response = Utilities.ExecuteHttpDelete(url, requestProperties, _params, _bypassSSLValidation);
					break;
				case GET:
					response = Utilities.ExecuteHttpGet(url, requestProperties, _params, _bypassSSLValidation);
					break;
				case POST:
					response = Utilities.ExecuteHttpPost(url,msg, requestProperties, _params,_bypassSSLValidation);
					break;
				case PUT:
					response = Utilities.ExecuteHttpPut(url,msg, requestProperties, _params, _bypassSSLValidation);
					break;
				default:
					break;
			}
			statusCode = Integer.parseInt(response.get("statusCode"));
			body = response.get("responseBody");
			body = (body==null || body.equals("") || body.equals("null") ? "" : body);
			if (statusCode>=HttpStatus.SC_MULTIPLE_CHOICES) {
				throw new Exception("Unexpected HTTP Status Code: " + statusCode);
			}
			if (body.replace("\n", "").toLowerCase().equals(response.get("responseMessage").toLowerCase())) {
				_event = new ServiceEvent(this, statusCode, body, response.get("responseMessage"));
			} else if (body.indexOf("[")>-1) {
				JSONArray theObject = new JSONArray(body);
				_event = new ServiceEvent(this, statusCode, body, theObject);
			} 
			else {
				JSONObject theObject = new JSONObject(body);
				_event = new ServiceEvent(this, statusCode, body, theObject);
			}
		}
		catch(Exception e)
		{
			_event = new ServiceEvent(this, statusCode, body, null,e);
		}
		return null;
	}
		
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		if (_callback != null) {
			_callback.onFinish(_event);
		}	
	}
}
