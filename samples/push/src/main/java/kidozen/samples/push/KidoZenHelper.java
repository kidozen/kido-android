package kidozen.samples.push;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kidozen.client.push.GCM;
import com.kidozen.client.push.IGcmEvents;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import kidozen.client.KZApplication;

/**
 * Created by christian on 7/8/14.
 */
public class KidoZenHelper implements IGcmEvents {
    private final String TAG = this.getClass().getSimpleName();
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
    private IPushEvents mPushEvents;
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
        mKidoGcm = new GCM(mActivity,kido,projectid);
        mKidoGcm.setGCMEvents(this);
        kido.Authenticate(provider,user,passw, new kidozen.client.ServiceEventListener() {
            @Override
            public void onFinish(kidozen.client.ServiceEvent e) {
                if (e.StatusCode == HttpStatus.SC_OK ) {
                    if (mPushEvents !=null && isInitialized) {
                        mPushEvents.onInitializationDone(kido.GetKidoZenUser().Claims.get("name"));
                    }
                }
            }
        });
    }

    public void Initialize() {
        mKidoGcm.Initialize();
    }

    public void Subscribe(String channel) {
        mKidoGcm.SubscribeToChannel(channel);
    }

    public void UnSubscribe(String channel) {
        mKidoGcm.UnSubscribeFromChannel(channel);
    }

    public void Push() {
        try {
            JSONObject data = new JSONObject()
                    .put("type","toast")
                    .put("text", "hello")
                    .put("title" , "my notification")
                    .put("image", "default.png")
                    .put("badge","1");
            mKidoGcm.PushMessage("sports",data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void setPushEvents(IPushEvents authEvents) {
        this.mPushEvents = authEvents;
    }

    @Override
    public void onInitializationComplete(Boolean success, String message, String registrationId, String deviceId) {
        if (mPushEvents!=null) mPushEvents.onInitializationDone(message);
    }

    @Override
    public void onSubscriptionComplete(Boolean success, String message) {
        if (mPushEvents!=null) mPushEvents.onSubscriptionDone(message);
    }

    @Override
    public void onPushMessageComplete(Boolean success, String message) {
        if (mPushEvents!=null) mPushEvents.onPushDone(message);
    }

    @Override
    public void onRemoveSubscriptionComplete(Boolean success, String message) {
        if (mPushEvents!=null) mPushEvents.onRemoveSubscriptionDone(message);
    }
}