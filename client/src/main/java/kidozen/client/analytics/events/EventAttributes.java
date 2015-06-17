package kidozen.client.analytics.events;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;

/**
 * Created by miya on 2/16/15.
 */
public class EventAttributes {
    private String platform;
    private String appVersion;

    public EventAttributes(String appVersion) {
        this.platform = "Android";
        this.appVersion = appVersion;

    }
}
