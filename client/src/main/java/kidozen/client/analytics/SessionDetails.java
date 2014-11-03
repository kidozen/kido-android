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
    public DeviceInfo eventAttr;

    public String eventName;
    public String eventValue;

    public SessionDetails(String uuid, Context context) {
        sessionUUID = uuid;
        StartDate = new Date().getTime();
        eventName = "usersession";
        eventValue = "Session ID: " + sessionUUID;
        eventAttr = new DeviceInfo(context);
    }
}
/*
*
*  event.eventAttr.carrierName
 event.eventAttr.deviceModel
 event.eventAttr.isoCountryCode
 event.eventAttr.mobileCountryCode
 event.eventAttr.networkAccess
 event.eventAttr.platform
 event.eventAttr.sessionLength
 event.eventAttr.systemVersion

* */