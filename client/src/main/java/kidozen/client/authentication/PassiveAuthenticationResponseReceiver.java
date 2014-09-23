package kidozen.client.authentication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.apache.http.HttpStatus;

import kidozen.client.internal.PassiveAuthenticationUtilities;

/**
* Created by christian on 6/19/14.
*/
public class PassiveAuthenticationResponseReceiver extends BroadcastReceiver {
    public static final String ACTION_RESP = "com.kidozen.intent.action.PASSIVE_AUTHENTICATION_RESULT";
    private final String TAG = PassiveAuthenticationResponseReceiver.class.getSimpleName();
    private kidozen.client.ServiceEventListener mPassiveListener;

    public PassiveAuthenticationResponseReceiver(kidozen.client.ServiceEventListener callback) {
        this.mPassiveListener = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        kidozen.client.ServiceEvent event = null;
        int response = intent.getIntExtra(KZPassiveAuthBroadcastConstants.REQUEST_CODE,0);
        PassiveAuthenticationUtilities.DispatchServiceEventListener(this, intent, response, mPassiveListener, KZPassiveAuthTypes.PASSIVE_AUTHENTICATION_USERID);
    }

}
