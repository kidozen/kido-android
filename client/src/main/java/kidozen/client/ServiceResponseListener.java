package kidozen.client;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by christian on 7/29/14.
 */
public class ServiceResponseListener extends ServiceResponseHandler {

    public void onSuccess(int statusCode,  String response) {
        // TODO: add default implementation
    }

    public void onSuccess(int statusCode,  JSONObject response) {
        // TODO: add default implementation
    }

    public void onSuccess(int statusCode,  JSONArray response) {
        // TODO: add default implementation
    }

    @Override
    public void OnSuccess(int statusCode, Object response) {
        if (response instanceof JSONObject) {
            onSuccess(statusCode, (JSONObject) response);
        }
        else if (response instanceof JSONArray) {
            onSuccess(statusCode, (JSONArray) response);
        }
        else {
            onSuccess(statusCode, response.toString());
        }
    }

    @Override
    public void OnError(int statusCode, Object errorResponse, Throwable e) {

    }
}
