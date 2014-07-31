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
    public void OnStart() {
        Log.d(TAG, "OnStart");
    }

    @Override
    public void OnSuccess(int statusCode,  String response) {
        Log.d(TAG, String.format("OnSuccess (String); StatusCode : %s", statusCode));
    }
    @Override
    public void OnSuccess(int statusCode,  JSONObject response) {
        Log.d(TAG, String.format("OnSuccess (JSONObject) ; StatusCode : %s", statusCode));
    }
    @Override
    public void OnSuccess(int statusCode,  JSONArray response) {
        Log.d(TAG, String.format("OnSuccess (JSONArray) ; StatusCode : %s", statusCode));
    }

    @Override
    public void OnError(int statusCode, String response) {
        Log.d(TAG, String.format("OnError ; StatusCode : %s", statusCode));
    }


    @Override
    public void onFinish(ServiceEvent e) {

    }
}
