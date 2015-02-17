package kidozen.client.analytics.events;

/**
 * Created by miya on 2/16/15.
 */
public class EventAttributes {
    private String platform;

    public EventAttributes() {
        this.platform = "Android";
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }


}
