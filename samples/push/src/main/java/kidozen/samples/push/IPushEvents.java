package kidozen.samples.push;

/**
 * Created by christian on 7/8/14.
 */
public interface IPushEvents {
    void ReturnUserName(String username);
    void ReturnRegistrationMessage(String message);
    void ReturnPushMessage(String message);
}
