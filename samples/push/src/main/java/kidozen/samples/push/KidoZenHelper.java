package kidozen.samples.push;

import android.content.Context;

import org.apache.http.HttpStatus;

import kidozen.client.KZApplication;

/**
 * Created by christian on 7/8/14.
 */
public class KidoZenHelper {
    private KZApplication kido = null;
    private String tenantMarketPlace = "https://tests.qa.kidozen.com";
    private String application       = "tasks";
    private String appkey            = "GZJQetc+VH9JLWoHnLEwlk7tw+XPSniMUSuIzK9kDxE=";
    private String user              = "tests@kidozen.com";
    private String passw             = "pass";
    private String provider          = "Kidozen";

    private String projectid          = "33779981368";



    private Boolean isInitialized    = false;

    //private IAuthenticationEvents authEvents;

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
        kido.Authenticate(provider,user,passw, new kidozen.client.ServiceEventListener() {
            @Override
            public void onFinish(kidozen.client.ServiceEvent e) {
                if (e.StatusCode == HttpStatus.SC_OK ) {
                    //if (authEvents!=null && isInitialized) {
                    //    authEvents.ReturnUserName(kido.GetKidoZenUser().Claims.get("name"));
                    //}
                }
            }
        });
    }

    //public void setAuthEvents(IAuthenticationEvents authEvents) {
    //    this.authEvents = authEvents;
    //}

}