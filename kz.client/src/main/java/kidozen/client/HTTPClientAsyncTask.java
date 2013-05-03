package kidozen.client;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class HTTPClientAsyncTask extends AsyncTask<String, Void, Void> {
	HttpParams _params = new BasicHttpParams();
	HashMap<String, String> _headers = null;
	KZHttpMethod _method= null;
	ServiceEventListener _callback= null;
	ServiceEvent _event = null;
	UrlEncodedFormEntity _message;

	public HTTPClientAsyncTask(KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers)
	{
		_headers = headers;
		_method = method;

		if (params!=null) {
			Iterator<Entry<String, String>> it = params.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> pairs = it.next();
				_params.setParameter(pairs.getKey(), pairs.getValue());
				it.remove();
			}
		}
	}
	public HTTPClientAsyncTask(KZHttpMethod method, HashMap<String, String> params, HashMap<String, String> headers, List<NameValuePair> form)
	{
		this(method,params, headers);
		try {
			_message = new UrlEncodedFormEntity(form);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
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
			addHeaders(query);
			HttpClient httpClient = new DefaultHttpClient();  

			HttpResponse resp = httpClient.execute(query);
			statusCode= resp.getStatusLine().getStatusCode();
			if (resp.getEntity()!=null) {
				body= EntityUtils.toString(resp.getEntity());
			}
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
		catch(JSONException j)
		{
			if (j.getMessage().indexOf("Value OK of type java.lang.String cannot be converted to JSONObject")==-1) {
				_event = new ServiceEvent(this, statusCode, body, null,j);
			}
			else{
				_event = new ServiceEvent(this, statusCode, body, body);
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
				post.setEntity(_message);
			}
			return post;
		case PUT:
			HttpPut put = new HttpPut(url);
			if (_message!=null) {
				put.setEntity(_message);
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
