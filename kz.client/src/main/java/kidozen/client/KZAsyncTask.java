package kidozen.client;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;

public class KZAsyncTask extends AsyncTask<String, Void, Void> {
	private static final String APPLICATION_JSON = "application/json";
	private static final String CONTENT_TYPE = "content-type";
	HttpParams _params = new BasicHttpParams();
	HashMap<String, String> _headers = null;
	KZHttpMethod _method= null;
	ServiceEventListener _callback= null;
	ServiceEvent _event = null;
	JSONObject _message;


	public KZAsyncTask(KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, ServiceEventListener callback)
	{
		_headers = headers;
		_method = method;
		_callback = callback;

		if (params!=null) {
			Iterator<Entry<String, String>> it = params.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> pairs = it.next();
				_params.setParameter(pairs.getKey(), pairs.getValue());
				it.remove();
			}
		}
	}
	public KZAsyncTask(KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, JSONObject message, ServiceEventListener callback)
	{
		this(method,params, headers, callback);
		_message = message;
	}

	@Override
	protected Void doInBackground(String... params) {
		int statusCode = HttpStatus.SC_BAD_REQUEST;

		String body = null;
		try
		{
			String  url = params[0];
			HttpRequestBase query =  methodFactory(url);
			query.setParams(_params);
			query.setHeader(CONTENT_TYPE, APPLICATION_JSON);
			addHeaders(query);

			HttpClient httpClient = new DefaultHttpClient();  


			HttpResponse resp = httpClient.execute(query);
			statusCode= resp.getStatusLine().getStatusCode();
			body= EntityUtils.toString(resp.getEntity());
			body = (body==null || body.equals("") || body.equals("null") ? "{}" : body);
			if (statusCode>299) {
				throw new Exception("Unexpected HTTP Status Code: " + statusCode);
			}

			if (body.indexOf("[")>-1) {
				JSONArray theObject = new JSONArray(body);
				_event = new ServiceEvent(this, statusCode, body, theObject);
			}
			else
			{
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
	};


	private HttpRequestBase methodFactory(String url)
	{
		switch (_method) {
		case GET:
			return new HttpGet(url);
		case DELETE:
			return new HttpDelete(url);
		case POST:
			HttpPost post = new HttpPost(url);
			if (_message!=null) {
				try {
					post.setEntity(new StringEntity(_message.toString()));
				} catch (UnsupportedEncodingException e) {
					return null;
				}
			}
			return post;
		case PUT:
			HttpPut put = new HttpPut(url);
			if (_message!=null) {
				try {
					put.setEntity(new StringEntity(_message.toString()));
				} catch (UnsupportedEncodingException e) {
					return null;
				}
			}
			return put;
		default:
			break;
		}
		return null;
	}

	private void addHeaders(HttpRequestBase query) {
		if (_headers!=null) {
			Iterator<Entry<String, String>> it = _headers.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> pairs = it.next();
				query.setHeader(pairs.getKey(), pairs.getValue());
				it.remove();
			}
		}
	}
}
