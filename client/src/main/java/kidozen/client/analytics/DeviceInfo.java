package kidozen.client.analytics;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;
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
    public String isoCountryCode = "Unknown";
    public String countryName= "Unknown";
    public String locality= "Unknown";
    public String adminArea= "Unknown";
    public String subAdminArea= "Unknown";
    public String locale= "Unknown";
    public String appVersion = "";
    private final String TAG = this.getClass().getSimpleName();

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
        getLocationInformation(context);
        deviceModel = getDeviceName();
        systemVersion = "Android " + Build.VERSION.RELEASE ;
        appVersion = new Integer(this.getAppVersion(context)).toString();
    }

    private int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void getLocationInformation(Context context) {
        Location loc = getLastBestLocation(context, 30);
        try {
            if (loc!=null) {
                Geocoder gcd = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = gcd.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);

                    isoCountryCode = address.getCountryCode();
                    countryName = address.getCountryName();
                    locality = address.getLocality();
                    adminArea = address.getAdminArea();
                    subAdminArea = address.getSubAdminArea();
                    locale = address.getLocale().toString();
                }
            }
            else {
                Log.e(TAG,"Could not get location information. Did you enabled location services ?");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Location getLastBestLocation(Context context, long minTime) {
        LocationManager locationManager;

        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        List<String> matchingProviders = locationManager.getAllProviders();
        for (String provider: matchingProviders) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if ((time > minTime && accuracy < bestAccuracy)) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                }
                else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }

        return bestResult;
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
