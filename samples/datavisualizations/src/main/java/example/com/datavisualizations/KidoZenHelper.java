package example.com.datavisualizations;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.Editable;

import org.apache.http.HttpStatus;

import example.com.datavisualizations.IAuthenticationEvents;
import kidozen.client.InitializationException;
import kidozen.client.KZApplication;
import kidozen.client.authentication.GPlusAuthenticationResponseReceiver;

/**
 * Created by christian on 7/8/14.
 */
public class KidoZenHelper {
    private KZApplication kido = null;

    private String tenantMarketPlace = "https://kidodemo.kidocloud.com";
    private String application = "tasks";
    private String appkey = "get this value from: marketplace -> application -> coding -> keys";

    private Boolean isInitialized    = false;

    private IAuthenticationEvents authEvents;

    public void setAuthEvents(IAuthenticationEvents authEvents) {
        this.authEvents = authEvents;
    }


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
        kido.Authenticate(context,false, new kidozen.client.ServiceEventListener() {
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

    public void setDataVisualization(Context context, String name) {
        kido.showDataVisualization(context,name);
    }
}