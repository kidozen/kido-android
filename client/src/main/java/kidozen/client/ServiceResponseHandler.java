package kidozen.client;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by christian on 7/29/14.
 */
public abstract class ServiceResponseHandler  implements  ServiceEventListener {
    private  final String TAG = this.getClass().getSimpleName();

    public abstract void onStart();

    public abstract void onSuccess(int statusCode, String response);

    public abstract void onSuccess(int statusCode, JSONObject response) ;

    public abstract void onSuccess(int statusCode, JSONArray response) ;

    public abstract void onError(int statusCode, String response);

    @Override
    final public void onFinish(ServiceEvent e) {
        Log.d(TAG, String.format("onFinish ; StatusCode : %s", e.StatusCode));
    }

}
