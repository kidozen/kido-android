package kidozen.client.analytics;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

/**
 * Created by christian on 10/22/14.
 */
public class DeviceInfo {
    public String CarrierName = "Unknown";
    public String ActiveNetwork = "Unknown";

    public String Version= "Unknown";
    public String CodeName= "Unknown";

    public DeviceInfo(Context context) {
        TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        CarrierName = manager.getNetworkOperatorName();

        ConnectivityManager cm =(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        switch (activeNetwork.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                ActiveNetwork = "WIFI";
                break;
            case ConnectivityManager.TYPE_MOBILE:
                ActiveNetwork = "MOBILE";
                break;
            case ConnectivityManager.TYPE_BLUETOOTH:
                ActiveNetwork = "BLUETOOTH";
                break;
        }

        CodeName = Build.VERSION.CODENAME;
        Version = Build.VERSION.RELEASE + "." + Build.VERSION.INCREMENTAL;
    }
}
