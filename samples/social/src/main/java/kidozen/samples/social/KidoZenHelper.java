package kidozen.samples.social;

import android.content.Context;

import org.apache.http.HttpStatus;

import kidozen.client.InitializationException;
import kidozen.client.KZApplication;

/**
 * Created by christian on 7/8/14.
 */
public class KidoZenHelper {
    private KZApplication kido = null;

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

    public void SignIn(final Context context) throws InitializationException{
        kido.Authenticate(context, new kidozen.client.ServiceEventListener() {
            @Override
            public void onFinish(kidozen.client.ServiceEvent e) {
                if (e.StatusCode == HttpStatus.SC_OK ) {
                    if (authEvents!=null && isInitialized) {
                        authEvents.ReturnUserName(kido.GetKidoZenUser().Claims.get("name"));
                        kido.EnableAnalytics(context.getApplicationContext());
                    }
                }
            }
        });
    }

    public void setAuthEvents(IAuthenticationEvents authEvents) {
        this.authEvents = authEvents;
    }

}