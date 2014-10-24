package kidozen.client.analytics;

import java.util.Date;

/**
 * Created by christian on 10/24/14.
 */
public class SessionDetails {
    public long StartDate ;
    public long EndDate;
    public long length;
    public String sessionUUID;
    public String Platform;
    public DeviceInfo DeviceInformation ;

    public SessionDetails(String uuid) {
        sessionUUID = uuid;
        StartDate = new Date().getTime();
        Platform = "Android";
        DeviceInformation = new DeviceInfo();
    }
}
