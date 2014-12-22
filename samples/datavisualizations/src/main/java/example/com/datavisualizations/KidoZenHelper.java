package example.com.datavisualizations;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import kidozen.client.InitializationException;
import kidozen.client.KZApplication;
import kidozen.client.PubSubChannel;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.SynchronousException;
import kidozen.client.TimeoutException;
import kidozen.client.authentication.GPlusAuthenticationResponseReceiver;

/**
 * Created by christian on 7/8/14.
 */
public class KidoZenHelper {
    private static final String TAG = "KidoZenHelper";
    private KZApplication kido = null;

    private String tenantMarketPlace = "https://testssl.kidocloud.com";
    private String application = "tasks";
    private String appkey = "upTbyOhvjn7/D2jia1D5LkzI66Di4HBoyf4AMFjkZN0=";//"NuSSOjO4d/4Zmm+lbG3ntlGkmeHCPn8x20cj82O4bIo=";

    private Boolean isInitialized    = false;

    private IAuthenticationEvents authEvents;

    public void setAuthEvents(IAuthenticationEvents authEvents) {
        this.authEvents = authEvents;
    }


    public KidoZenHelper() {
        kido = new KZApplication(tenantMarketPlace, application, appkey,true);
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
        try {
            final PubSubChannel channel = kido.PubSubChannel("myAndroidChannel");
            channel.Subscribe(new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    try {
                        channel.Publish(new JSONObject(),false);
                    } catch (TimeoutException e1) {
                        e1.printStackTrace();
                    } catch (SynchronousException e1) {
                        e1.printStackTrace();
                    }
                    Log.d(TAG,e.Body);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        //kido.showDataVisualization(context,name);
    }
}