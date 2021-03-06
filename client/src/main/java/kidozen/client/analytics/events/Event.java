package kidozen.client.analytics.events;

/**
 * Created by christian on 10/22/14.
 */
public abstract class Event {
    protected String eventName = "Event";
    private String eventValue;
    private String sessionUUID;
    private String userid;

    protected EventAttributes eventAttr;

    public void Event(String data, String UUID, String userid, String appVersion) {
        eventValue = data;
        sessionUUID = UUID;
        this.userid = userid;
        eventAttr = new EventAttributes(appVersion);
    }

    public EventAttributes getEventAttr() {
        return eventAttr;
    }


    public String getEventName() {
        return eventName;
    }

    public void setEventName(String name) {
        this.eventName = name;
    }


    public String getData() {
        return eventValue;
    }

    public void setData(String mData) {
        this.eventValue = mData;
    }

    public String getUUID() {
        return sessionUUID;
    }

    public void setUUID(String mUUID) {
        this.sessionUUID = mUUID;
    }

}
