package kidozen.client;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kidozen.client.R;

import java.util.Date;
import java.util.HashMap;

public class KZBroadcastReceiver extends BroadcastReceiver {
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        HashMap<String, String> map = new HashMap<String, String>();

        Bundle bundle = intent.getExtras();

        try {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                map.put(key, value.toString());
            }
            map.put("kidoId", "232347x");
        }
        catch (Exception e) {
            // what to do here... ?
        }
        finally {
            this.sendNotification(map);
        }
    }

    private void sendNotification(HashMap<String, String> map) {
        NotificationManager notificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // You should add this corresponding action into your manifest.xml file.
        Intent intent = new Intent("kidozen.client.MainAction");
        intent.putExtra("kidoId", map.get("kidoId"));

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                intent, 0);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(map.get("kidoId"))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(map.get("kidoId")))
                        .setContentText(map.get("message"));

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
