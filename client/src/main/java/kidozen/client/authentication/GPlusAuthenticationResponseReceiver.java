package kidozen.client.authentication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import kidozen.client.ServiceEventListener;

/**
 * Created by christian on 9/23/14.
 */
public class GPlusAuthenticationResponseReceiver extends BroadcastReceiver {
    public static final String ACTION_RESP = "com.kidozen.intent.action.GPLUS_AUTHENTICATION_RESULT";
    private final String TAG = this.getClass().getSimpleName();

    public GPlusAuthenticationResponseReceiver(ServiceEventListener callback) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,intent.toString());

    }
}
