package kidozen.client.analytics.events;

/**
 * Created by christian on 10/22/14.
 */
public class ClickEvent extends Event   {
    public ClickEvent(String data, String uuid, String userid, String appVersion) {
        super.Event(data, uuid, userid, appVersion);
        setEventName("Click");
    }
}
