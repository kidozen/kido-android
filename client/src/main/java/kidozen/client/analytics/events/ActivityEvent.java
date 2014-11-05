package kidozen.client.analytics.events;

/**
 * Created by christian on 10/22/14.
 */
public class ActivityEvent extends Event   {
    public ActivityEvent(String data, String uuid) {
        super();
        setUUID(uuid);
        setData(data);
        setEventName("View");
    }

}