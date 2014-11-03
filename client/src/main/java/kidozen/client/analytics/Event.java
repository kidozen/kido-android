package kidozen.client.analytics;

import org.json.JSONObject;

/**
 * Created by christian on 10/22/14.
 */
public abstract class Event {
    protected String eventName = "Event";
    private String eventValue;
    private String sessionUUID;

    public void Event(String data, String UUID) {
        eventValue = data;
        sessionUUID = UUID;
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
