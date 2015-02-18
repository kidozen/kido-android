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
    public String userid;
    public DeviceInfo eventAttr;
    public String eventName;
    public String eventValue;

    public boolean isPeding = false;

    public SessionDetails(String uuid, Context context, String userid) {
        sessionUUID = uuid;
        this.userid = userid;
        StartDate = new Date().getTime();
        eventName = "usersession";
        eventValue = "Session ID: " + sessionUUID;
        eventAttr = new DeviceInfo(context);
    }
}
