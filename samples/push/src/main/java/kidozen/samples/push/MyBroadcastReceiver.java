package kidozen.samples.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by christian on 7/14/14.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {
    private String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private NotificationManager mNotificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        // Here we should create a kidozen instance and push the notificationOpened.

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
        this.sendNotification(message);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra("kidoId", "myKidoId");


        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                intent, 0);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(111, mBuilder.build());
    }
}