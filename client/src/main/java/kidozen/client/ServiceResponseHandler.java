package kidozen.client;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by christian on 7/29/14.
 */
public abstract class ServiceResponseHandler {

    public abstract void OnSuccess(int statusCode,  String response);

    public abstract void OnSuccess(int statusCode,  JSONObject response) ;

    public abstract void OnSuccess(int statusCode,  JSONArray response) ;

    public abstract void OnError(int statusCode,  String response);

}
