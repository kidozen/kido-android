package kidozen.client.analytics.events;

import android.content.Context;

import kidozen.client.analytics.DeviceInfo;

/**
 * Created by miya on 2/17/15.
 */
public class SessionStartEvent {

    private DeviceInfo eventAttr;
    private String eventName = "sessionStart";
    private String sessionUUID;
    private String userid;

    public SessionStartEvent(Context context, String uuid, String userid, String appVersion) {
        this.eventAttr = new DeviceInfo(context);
        this.sessionUUID = uuid;
        this.userid = userid;
    }

    public DeviceInfo getEventAttr() {
        return eventAttr;
    }

    public void setEventAttr(DeviceInfo eventAttr) {
        this.eventAttr = eventAttr;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getSessionUUID() {
        return sessionUUID;
    }

    public void setSessionUUID(String sessionUUID) {
        this.sessionUUID = sessionUUID;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
