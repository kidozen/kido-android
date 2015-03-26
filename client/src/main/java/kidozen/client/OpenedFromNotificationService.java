package kidozen.client;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;

/* This class is used to notify KidoZen that the application has been opened by tapping
 * on a push notification.
*/
public class OpenedFromNotificationService extends KZService {

    public OpenedFromNotificationService(String baseURL, String provider , String username, String pass, String clientId,
                                         KidoZenUser userIdentity, KidoZenUser applicationIdentity) {

        super(baseURL + "api/v2/notifications/track/open", "", provider,  username, pass, clientId, userIdentity, applicationIdentity);
    }

    public void didOpen(JSONObject trackContext) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);

        this.ExecuteTask(this.mEndpoint,
                KZHttpMethod.POST,
                null,
                headers,
                new ServiceEventListener() {
                    @Override
                    public void onFinish(ServiceEvent e) {
                        if (e != null) {
//                            Log.e("OpenedFromNotificationService", "Finished didOpenFromNotification" + e.Exception.toString());
                        }
                    }
                },
                trackContext,
                getStrictSSL());
    }

}
