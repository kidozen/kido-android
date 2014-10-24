package kidozen.client.analytics;

import android.content.Context;

import java.util.Date;

/**
 * Created by christian on 10/24/14.
 */
public class SessionDetails {
    public long StartDate ;
    public long EndDate;
    public long length;
    public String sessionUUID;
    public String Platform;
    public DeviceInfo DeviceInformation ;

    public String eventName;
    public String eventValue;

    public SessionDetails(String uuid, Context context) {
        sessionUUID = uuid;
        StartDate = new Date().getTime();
        Platform = "Android";
        eventName = "New Session";
        eventValue = "Session ID: " + sessionUUID;
        DeviceInformation = new DeviceInfo(context);
    }
}
