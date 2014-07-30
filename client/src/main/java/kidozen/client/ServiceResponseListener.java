package kidozen.client;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by christian on 7/29/14.
 */
public class ServiceResponseListener extends ServiceResponseHandler {

    private  final String TAG = this.getClass().getSimpleName();
    @Override
    public void OnSuccess(int statusCode,  String response) {
        // TODO: add default implementation
        Log.d(TAG, "OnSuccess");
    }
    @Override
    public void OnSuccess(int statusCode,  JSONObject response) {
        // TODO: add default implementation
    }
    @Override
    public void OnSuccess(int statusCode,  JSONArray response) {
        // TODO: add default implementation
    }

    @Override
    public void OnError(int statusCode, String response) {

    }
}
