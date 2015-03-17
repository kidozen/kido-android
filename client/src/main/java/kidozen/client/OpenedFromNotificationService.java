package kidozen.client;

import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;

/* This class is used to notify KidoZen that the application has been opened by tapping
 * on a push notification.
*/
public class OpenedFromNotificationService extends KZService {

    public OpenedFromNotificationService(String provider , String username, String pass, String clientId,
                                         KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super("/notification", "", provider,  username, pass, clientId, userIdentity, applicationIdentity);
    }

    public void didOpen(String notificationId) {
        if (mEndpoint.endsWith("/"))
            mEndpoint = mEndpoint.substring(0,mEndpoint.length()-1);
        String logEndpoint = String.format("%s/%@/android/opened",mEndpoint, notificationId);

        new KZServiceAsyncTask(KZHttpMethod.POST, null, null, null, getStrictSSL()).execute(logEndpoint);
    }

}
