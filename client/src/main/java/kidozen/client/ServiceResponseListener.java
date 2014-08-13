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
        //System.out.println("onStart");
        Log.d(TAG, "onStart");
    }

    @Override
    public void onSuccess(int statusCode, String response) {
        //System.out.println(String.format("onSuccess (String); StatusCode : %s", statusCode));
        Log.d(TAG, String.format("onSuccess (String); StatusCode : %s", statusCode));
    }
    @Override
    public void onSuccess(int statusCode, JSONObject response) {
        //System.out.println( String.format("onSuccess (JSONObject) ; StatusCode : %s", statusCode));
        Log.d(TAG, String.format("onSuccess (JSONObject) ; StatusCode : %s", statusCode));
    }
    @Override
    public void onSuccess(int statusCode, JSONArray response) {
        //System.out.println(String.format("onSuccess (JSONArray) ; StatusCode : %s", statusCode));
        Log.d(TAG, String.format("onSuccess (JSONArray) ; StatusCode : %s", statusCode));
    }

    @Override
    public void onError(int statusCode, String response) {
        //System.out.println( String.format("onError ; StatusCode : %s", statusCode));
        Log.d(TAG, String.format("onError ; StatusCode : %s", statusCode));
    }
}
