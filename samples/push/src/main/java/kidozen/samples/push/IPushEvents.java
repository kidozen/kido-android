package kidozen.samples.push;

/**
 * Created by christian on 7/15/14.
 */
public interface IPushEvents {
    void onInitializationDone(String message);
    void onSubscriptionDone(String message);
    void onPushDone(String message);
    void onRemoveSubscriptionDone(String message);

}
