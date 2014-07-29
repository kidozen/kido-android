package kidozen.client.internal;

import android.os.AsyncTask;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

import kidozen.client.KZHttpMethod;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;

/**
* Created by christian on 4/30/14.
*/
public class KidoAppSettings extends AsyncTask<String, Void, JSONObject> {
    private static KidoAppSettings INSTANCE = null;
    private ServiceEventListener _callback;
    private Boolean _strictSSL;
    private JSONObject _settings;
    private int _statusCode;
    private String _response;
    private Exception _exception;
    public boolean IsInitialized = false;
    public boolean IsValid = true;

    public void Setup(ServiceEventListener cb, boolean strictSSL){
        _strictSSL = strictSSL;
        _callback = cb;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            SNIConnectionManager sniManager = new SNIConnectionManager(params[0],"",null, null, _strictSSL);
            Hashtable<String, String> authResponse = sniManager.ExecuteHttp(KZHttpMethod.GET);
            _response = authResponse.get("responseBody");
            _statusCode = Integer.parseInt(authResponse.get("statusCode"));
            if (_statusCode>= HttpStatus.SC_BAD_REQUEST) throw  new Exception(_response);
            JSONArray cfg = new JSONArray(_response);
            _settings = cfg.getJSONObject(0);
            this.IsValid = (_settings!=null);
        }
        catch (JSONException e)
        {
            _statusCode = HttpStatus.SC_NOT_FOUND;
            _exception = new Exception(String.format("Application not found. %s",  _response));
        }
        catch (Exception e) {
            _exception = new Exception(String.format("Invalid Response (Http StatusCode = %s). Body : %s", _statusCode, _response));
        }
        finally {
            return  _settings;
        }
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        if (_callback!=null) {
            if (jsonObject!=null) {
                IsInitialized = true;
                _callback.onFinish(new ServiceEvent(this, _statusCode, _response, jsonObject));
            }
            else {
                IsInitialized = false;
                _callback.onFinish(new ServiceEvent(this, _statusCode, _response, null, _exception));
            }
        }
    }

    public String GetSettingAsString(String name) throws JSONException {
        return _settings.getString(name);
    }

    public JSONObject GetSettingAsJObject(String name) throws JSONException {
        return _settings.getJSONObject(name);
    }

}
