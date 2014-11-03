package kidozen.client.analytics;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.util.Locale;

/**
 * Created by christian on 10/22/14.
 */
public class DeviceInfo {
    public String carrierName = "Unknown";
    public String networkAccess = "Unknown";
    public String platform = "Android";
    public String systemVersion = "Unknown";
    public String deviceModel = "Unknown";
    public String sessionLength = "-1";
    //public String uniqueId = "0";
    public String isoCountryCode = "Unknown";
    public String mobileCountryCode = "Unknown";

    public DeviceInfo(Context context) {
        TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        carrierName = manager.getNetworkOperatorName();

        ConnectivityManager cm =(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        switch (activeNetwork.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                networkAccess = "WIFI";
                break;
            case ConnectivityManager.TYPE_MOBILE:
                networkAccess = "MOBILE";
                break;
            case ConnectivityManager.TYPE_BLUETOOTH:
                networkAccess = "BLUETOOTH";
                break;
        }

        deviceModel = getDeviceName();
        systemVersion = Build.VERSION.RELEASE + "." + Build.VERSION.INCREMENTAL;
        isoCountryCode = getIsoCountryCode(context);
        mobileCountryCode = Locale.getDefault().getCountry();
    }

    private String getIsoCountryCode(Context context) {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimCountryIso();
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
