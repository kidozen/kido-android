package kidozen.samples.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by christian on 7/14/14.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {
    private String TAG = this.getClass().getSimpleName();
    private Context mContext;


    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

        String messageType = gcm.getMessageType(intent);

        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            // error occurs
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            // the server have deleted some pending messages,
            // because they are collapsible
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            // normal message
            // how the data can be fetched is detailed in the next section
        }
        Bundle bundle = intent.getExtras();
        StringBuilder sb = new StringBuilder();
        try {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                sb.append(String.format("%s %s (%s)", key,value.toString(), value.getClass().getName()));
                Log.d(TAG, String.format("%s %s (%s)", key,value.toString(), value.getClass().getName()));
            }
        }
        catch (Exception e) {
            sb = new StringBuilder();
            sb.append(e.getMessage());
            Log.d(TAG,e.getMessage());
        }
        finally {
            showToast(sb.toString());
        }
    }

    public void showToast(String message){
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }
}