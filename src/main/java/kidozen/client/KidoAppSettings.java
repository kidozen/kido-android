package kidozen.client;

import android.os.AsyncTask;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.text.html.HTMLDocument;

import kidozen.client.authentication.IdentityManager;

/**
* Created by christian on 4/30/14.
*/
class KidoAppSettings extends AsyncTask<String, Void, JSONObject> {
    private static KidoAppSettings INSTANCE = null;
    private ServiceEventListener _callback;
    private Boolean _strictSSL;
    private JSONObject _settings;
    private int _statusCode;
    private String _response;
    private Exception _exception;
    public boolean IsInitialized = false;

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
        }
        catch (JSONException e)
        {
            _exception = e;
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
        if (jsonObject!=null) {
            IsInitialized = true;
            _callback.onFinish(new ServiceEvent(this, _statusCode, _response, jsonObject));
        }
        else {
            IsInitialized = false;
            _callback.onFinish(new ServiceEvent(this, _statusCode, _response, null, _exception));
        }
    }

    public String GetSettingAsString(String name) throws JSONException {
        return _settings.getString(name);
    }

    public JSONObject GetSettingAsJObject(String name) throws JSONException {
        return _settings.getJSONObject(name);
    }

    private KidoAppSettings(Boolean strictSSL) {
        _strictSSL = strictSSL;
    }

    // Private constructor suppresses
    private KidoAppSettings(ServiceEventListener cb, Boolean strictSSL) {
        this(strictSSL);
        _callback = cb;
    }

    private static void createInstance(ServiceEventListener cb, Boolean strictSSL) {
        if (INSTANCE == null) {
            // synchronized to avoid possible  multi-thread issues
            synchronized(IdentityManager.class) {
                // must check for null again
                if (INSTANCE == null) {
                    INSTANCE = new KidoAppSettings(cb, strictSSL);
                }
            }
        }
    }

    public static KidoAppSettings getInstance() {
        return INSTANCE;
    }

    public static KidoAppSettings getInstance(ServiceEventListener cb, Boolean strictSSL) {
        createInstance(cb, strictSSL);
        return INSTANCE;
    }
}
