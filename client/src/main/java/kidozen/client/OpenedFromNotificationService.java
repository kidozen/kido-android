package kidozen.client;

import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;

/* This class is used to notify KidoZen that the application has been opened by tapping
 * on a push notification.
*/
public class OpenedFromNotificationService extends KZService {

    public OpenedFromNotificationService(String provider , String username, String pass, String clientId,
                                         KidoZenUser userIdentity, KidoZenUser applicationIdentity) {

        super("/notifications/track/open", "", provider,  username, pass, clientId, userIdentity, applicationIdentity);
    }

    public void didOpen(Object trackContext) {
        HashMap<String, String> params = (HashMap< String, String>)trackContext;

        new KZServiceAsyncTask(KZHttpMethod.POST, params, null, null, getStrictSSL()).execute("");
    }

}
