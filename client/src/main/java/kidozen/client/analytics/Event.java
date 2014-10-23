package kidozen.client.analytics;

import org.json.JSONObject;

/**
 * Created by christian on 10/22/14.
 */
public abstract class Event {
    private String mData;
    private String mUUID;


    public void Event(String data, String UUID) {
        mData = data;
        mUUID = UUID;
    }

    public String getData() {
        return mData;
    }

    public void setData(String mData) {
        this.mData = mData;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String mUUID) {
        this.mUUID = mUUID;
    }

    public abstract JSONObject Serialize();
}
