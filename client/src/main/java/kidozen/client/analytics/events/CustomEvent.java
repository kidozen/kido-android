package kidozen.client.analytics.events;

import org.json.JSONObject;

/**
 * Created by christian on 10/22/14.
 */
public class CustomEvent extends Event   {
    public CustomEvent(String title, JSONObject data, String uuid) {
        super.Event(data.toString(), uuid);
        setEventName("CustomEvent");
    }

}
