package kidozen.samples.good;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpStatus;

import kidozen.client.InitializationException;
import kidozen.client.KZApplication;

/**
 * Created by christian on 7/8/14.
 */
public class KidoZenHelper {
    private KZApplication kido = null;

    String tenantMarketPlace = "https://contoso.kidocloud.com";
    String application = "myApplication";
    String appkey = "get this value from your marketplace";

    private Boolean isInitialized    = false;

    private IAuthenticationEvents authEvents;

    public KidoZenHelper() {
        kido = new KZApplication(tenantMarketPlace, application, appkey, false);
        try {
            kido.Initialize(new kidozen.client.ServiceEventListener() {
                @Override
                public void onFinish(kidozen.client.ServiceEvent e) {
                    isInitialized = (e.StatusCode == HttpStatus.SC_OK);
                }
            });
        } catch (InitializationException e) {
            e.printStackTrace();
        }
    }

    public void SignOut() {
        kido.SignOut();
    }

    public void SignIn(Context context) throws InitializationException{
        kido.AuthenticateGood(context, "https://goodcontrol.kidozen.com", new kidozen.client.ServiceEventListener() {
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