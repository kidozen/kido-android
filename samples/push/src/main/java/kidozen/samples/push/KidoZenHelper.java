package kidozen.samples.push;

import android.app.Activity;
import android.content.Context;

import com.kidozen.client.push.GCM;

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

    private Activity mActivity;
    private IPushEvents pushEvents;
    private GCM mKidoGcm;

    public KidoZenHelper(Activity activity) {
        mActivity = activity;
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

    public void SignIn() {
        kido.Authenticate(provider,user,passw, new kidozen.client.ServiceEventListener() {
            @Override
            public void onFinish(kidozen.client.ServiceEvent e) {
                if (e.StatusCode == HttpStatus.SC_OK ) {
                    if (pushEvents!=null && isInitialized) {
                        pushEvents.ReturnUserName(kido.GetKidoZenUser().Claims.get("name"));
                    }
                }
            }
        });
    }

    public void Register() {
        mKidoGcm = new GCM(mActivity,kido,projectid);
    }

    public void Push() {

    }

    public void setPushEvents(IPushEvents authEvents) {
        this.pushEvents = authEvents;
    }

}