package kidozen.client;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by christian on 7/29/14.
 */
public abstract class ServiceResponseHandler  implements  ServiceEventListener{
    public abstract void onStart();

    public abstract void onSuccess(int statusCode, String response);

    public abstract void onSuccess(int statusCode, JSONObject response) ;

    public abstract void onSuccess(int statusCode, JSONArray response) ;

    public abstract void onError(int statusCode, String response);

    @Override
    public void onFinish(ServiceEvent e) {

    }

}
