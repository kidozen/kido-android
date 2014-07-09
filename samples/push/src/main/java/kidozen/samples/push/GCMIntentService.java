package kidozen.samples.push;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by christian on 7/8/14.
 *
 * http://androidexample.com/Android_Push_Notifications_using_Google_Cloud_Messaging_GCM/index.php?view=article_discription&aid=119&aaid=139
 *
 * Crear un nuevo proyecto aca: https://console.developers.google.com
 * http://developer.android.com/google/gcm/gs.html
 * http://developer.android.com/google/gcm/client.html
 *

public class GCMIntentService extends GCMBaseIntentService {

    public GCMIntentService() {
        super("997659204329");
    }

    @Override
    protected void onError(Context context, String errorId) {
        String message = "REGISTRATION: Error -> " + errorId;
        Log.d("GCMTest", message);

        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        String msg = intent.getExtras().getString("msg");
        Log.d("GCMTest", "Mensaje: " + msg);

        Intent intent1 = new Intent("custom-event-name");
        // You can also include some extra data.
        intent1.putExtra("message", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onRegistered(Context context, String regId) {
        Log.d("GCMTest", "REGISTRATION: Registrado OK.");
        Intent intent = new Intent("custom-event-name");
        intent.putExtra("register", regId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onUnregistered(Context context, String regId) {
        Log.d("GCMTest", "REGISTRATION: Desregistrado OK.");
        Intent intent = new Intent("custom-event-name");
        intent.putExtra("unregister", regId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
 */