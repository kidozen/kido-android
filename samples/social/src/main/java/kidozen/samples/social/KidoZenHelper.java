package kidozen.samples.social;

import android.content.Context;

import org.apache.http.HttpStatus;

import kidozen.client.KZApplication;

/**
 * Created by christian on 7/8/14.
 */
public class KidoZenHelper {
    private KZApplication kido = null;

    private String tenantMarketPlace = "http://contoso.kidocloud.com";
    private String application       = "myApplication";
    private String appkey            = "get this value from your marketplace";

    private Boolean isInitialized    = false;

    private IAuthenticationEvents authEvents;

    public KidoZenHelper() {
        kido = new KZApplication(tenantMarketPlace, application, appkey, false, new kidozen.client.ServiceEventListener() {
            @Override
            public void onFinish(kidozen.client.ServiceEvent e) {
                isInitialized = (e.StatusCode == HttpStatus.SC_OK);
            }
        });
    }

    public void SignOut() {
        kido.SignOut();
    }

    public void SignIn(Context context) {
        kido.Authenticate(context, new kidozen.client.ServiceEventListener() {
            @Override
            public void onFinish(kidozen.client.ServiceEvent e) {
                if (e.StatusCode == HttpStatus.SC_OK ) {
                    if (authEvents!=null && isInitialized) {
                        authEvents.ReturnUserName(kido.GetKidoZenUser().Claims.get("name"));
                    }
                }
            }
        });
    }

    public void setAuthEvents(IAuthenticationEvents authEvents) {
        this.authEvents = authEvents;
    }

}