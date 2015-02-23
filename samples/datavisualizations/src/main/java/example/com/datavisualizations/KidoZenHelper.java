package example.com.datavisualizations;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import kidozen.client.InitializationException;
import kidozen.client.KZApplication;
import kidozen.client.PubSubChannel;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.authentication.GPlusAuthenticationResponseReceiver;

/**
 * Created by christian on 7/8/14.
 */
public class KidoZenHelper {
    private static final String TAG = "KidoZenHelper";
    private KZApplication kido = null;

    private String tenantMarketPlace = "https://contoso.local.kidozen.com";
    private String application = "test1";
    String appkey = "7AzOZiYBU0a6x/hg6Z7i1tlASgi9ojv/OSS12FqB/Ko=";

    private Boolean isInitialized    = false;
    PubSubChannel channel;
    private IAuthenticationEvents authEvents;

    private final ServiceEventListener myChannelListenner = new ServiceEventListener() {
        @Override
        public void onFinish(ServiceEvent e) {
            Log.d(TAG,e.Body);
        }
    };

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
        kido.Authenticate("contoso@kidozen.com","pass","Kidozen", new kidozen.client.ServiceEventListener() {
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

            channel = kido.PubSubChannel("HoFinoPolvoDeOroSobreLasCopasVerdes");
            channel.SetChannelMessageListener(myChannelListenner);

            channel.Subscribe(new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    Log.d(TAG,e.Body);

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        //kido.showDataVisualization(context,name);
    }

    public void Push() {
        try {
            channel.Publish(new JSONObject().put("bar","foo"), false, new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    Log.d(TAG,e.Body);

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}