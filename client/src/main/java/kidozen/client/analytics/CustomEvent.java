package kidozen.client.analytics;

import org.json.JSONObject;

/**
 * Created by christian on 10/22/14.
 */
public class CustomEvent extends Event   {
    public CustomEvent(String title, JSONObject data, String uuid) {
        super();
    }

    @Override
    public JSONObject Serialize() {
        return null;
    }
}