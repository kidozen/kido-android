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
    public void onStart() {
        Log.d(TAG, "onStart");
    }

    @Override
    public void onSuccess(int statusCode, String response) {
        Log.d(TAG, String.format("onSuccess (String); StatusCode : %s", statusCode));
    }
    @Override
    public void onSuccess(int statusCode, JSONObject response) {
        Log.d(TAG, String.format("onSuccess (JSONObject) ; StatusCode : %s", statusCode));
    }
    @Override
    public void onSuccess(int statusCode, JSONArray response) {
        Log.d(TAG, String.format("onSuccess (JSONArray) ; StatusCode : %s", statusCode));
    }

    @Override
    public void onError(int statusCode, String response) {
        Log.d(TAG, String.format("onError ; StatusCode : %s", statusCode));
    }

    @Override
    final public void onFinish(ServiceEvent e) {
        Log.d(TAG, String.format("onFinish ; StatusCode : %s", e.StatusCode));
    }
}
