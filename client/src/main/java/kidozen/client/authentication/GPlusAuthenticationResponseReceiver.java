package kidozen.client.authentication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.http.HttpStatus;

import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.internal.PassiveAuthenticationUtilities;

/**
 * Created by christian on 9/23/14.
 */
public class GPlusAuthenticationResponseReceiver extends BroadcastReceiver {
    public static final String ACTION_RESP = "com.kidozen.intent.action.GOOGLE_PLUS_AUTHENTICATION_RESULT";
    private final String TAG = this.getClass().getSimpleName();
    private ServiceEventListener mGPlusListener = null;

    public GPlusAuthenticationResponseReceiver(ServiceEventListener callback) {
        this.mGPlusListener = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,intent.toString());

        int response = intent.getIntExtra(KZPassiveAuthBroadcastConstants.REQUEST_CODE,0);

        // Aca, segun el response, tengo que invocar al servicio de Auth de Kidozen, y luego enviar a respuesta

        PassiveAuthenticationUtilities.DispatchServiceEventListener(this, intent, response, mGPlusListener, KZPassiveAuthTypes.GPLUS_AUTHENTICATION_USERID);
    }

}
