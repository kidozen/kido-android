package kidozen.client;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import kidozen.client.authentication.KidoZenUser;

/* This class is used to notify KidoZen that the application has been opened by tapping
 * on a push notification.
*/
public class OpenedFromNotificationService extends KZService {

    public OpenedFromNotificationService(String provider , String username, String pass, String clientId,
                                         KidoZenUser userIdentity, KidoZenUser applicationIdentity) {

        super("/notifications/track/open", "", provider,  username, pass, clientId, userIdentity, applicationIdentity);
    }

    public void didOpen(JSONObject trackContext) {

        Log.e("didOPen", "track context is " + trackContext);
        Gson gson = new Gson();
        Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
        Map<String,String> map = gson.fromJson(trackContext.toString(), stringStringMap);
        HashMap<String, String> params = new HashMap<String, String>(map);

        new KZServiceAsyncTask(KZHttpMethod.POST, params, null, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                Log.e("OpenedFromNotificationService", "OpenedFromNotificationService finished. --> " + e);
            }
        }, getStrictSSL()).execute("");
    }

}
