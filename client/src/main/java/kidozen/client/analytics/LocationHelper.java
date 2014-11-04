package kidozen.client.analytics;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by christian on 11/4/14.
 */
public class LocationHelper {
    /*
    private GeoPoint getLocation() {
        try {
            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = locationManager.getBestProvider(criteria,true);
//In order to make sure the device is getting the location, request updates.
// this requests updates every hour or if the user moves 1 kilometer
            Location curLocation = locationManager.getLastKnownLocation(provider);
            GeoPoint curGeoPoint = new GeoPoint((int)(curLocation.getLatitude() * 1e6), (int)(curLocation.getLongitude()*1e6));
            return curGeoPoint;
        } catch (NullPointerException e) {
            Log.e("your app name here", "Log your error here");
        }
        return null;
    }


    public static String getCountryName(Context context, GeoPoint curLocal) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation((double)curLocal.getLatitudeE6() / 1e6,(double)curLocal.getLongitudeE6()/ 1e6,1);
        } catch (IOException ignored) {
            Address result;
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getCountryName();
            }
            return null;
        }
        return addresses.get(0).getCountryCode();
    }
    */
}



