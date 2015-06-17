package kidozen.samples.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.kidozen.client.R;

import java.util.Date;


/**
 * This class is an example of how to deal with Kidozen's notifications.
 * The main idea is that the receiver will handle it by creating an item in
 * Android's notification center.
 * The intent you plan on starting up should be able to be referenced using this code:
 *
 *         Intent intent = new Intent("kidozen.client.MainAction");
 *
 * So you should add the Action name in your manifest.xml file.
 *
 */
public class KZBroadcastReceiver extends BroadcastReceiver {
    private Context mContext;
    private static final String TRACK_CONTEXT = "trackContext";

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        Bundle bundle = intent.getExtras();
        if (bundle.containsKey(TRACK_CONTEXT)) {
            this.sendNotification(bundle);
        }
    }

    private void sendNotification(Bundle bundle) {
        NotificationManager notificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // You should add this corresponding action into your manifest.xml file,
        // as it's the default activity that will be called.
        Intent intent = new Intent("kidozen.client.MainAction");


        for (String key : bundle.keySet()) {
            String value = (String) bundle.get(key);
            intent.putExtra(key, value);
        }

        String message = (String)bundle.get("message");
        String title = (String)bundle.get("title");
        intent.putExtra("message", message);
        intent.putExtra("title", title);

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                intent, 0);

        // TODO: We should check how to show the notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(title))
                        .setContentText(message);

        mBuilder.setContentIntent(contentIntent);
        Notification notification = mBuilder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(this.randomNotificationId(), notification);
    }

    private int randomNotificationId() {
        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        String last4Str = tmpStr.substring(tmpStr.length() - 5);
        return Integer.valueOf(last4Str);
    }
}
