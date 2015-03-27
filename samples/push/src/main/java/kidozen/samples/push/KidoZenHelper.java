package kidozen.samples.push;

import android.app.Activity;
import android.util.Log;

import com.kidozen.client.push.GCM;
import com.kidozen.client.push.IGcmEvents;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import kidozen.client.CustomAPIService;
import kidozen.client.InitializationException;
import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;

/**
 * Created by christian on 7/8/14.
 */
public class KidoZenHelper implements IGcmEvents {
    private final String TAG = this.getClass().getSimpleName();
    public KZApplication kido = null;

    private String tenantMarketPlace = "";
    private String application = "tasks";
    private String appkey = "";
    private String user              = "@kidozen.com";
    private String passw             = "pass";
    private String provider          = "Kidozen";

    private String projectid          = "Your project number."; // TODO: rename to projectNumber. Check Documentation.
    private Boolean isInitialized    = false;

    private Activity mActivity;
    private IPushEvents mPushEvents;
    private GCM mKidoGcm;

    public KidoZenHelper(Activity activity) {
        mActivity = activity;
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

    public void SignIn() throws InitializationException {
        mKidoGcm = new GCM(mActivity,kido,projectid);
        mKidoGcm.setGCMEvents(this);
        kido.Authenticate(user,passw,provider, new kidozen.client.ServiceEventListener() {
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
        try {
            CustomAPIService service = kido.customAPIService("customScript1");
            service.executeCustomAPI(new JSONObject(), new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    if (e != null) {
                        Log.e("finished", "finished." + e.Body);
                    }
                }
            });
        } catch (Exception e) {
            if (e != null) {
                Log.e("Error", "error" + e.getStackTrace());
            }

        }

//        mKidoGcm.Initialize();
    }

    public void Subscribe(String channel) {
        mKidoGcm.SubscribeToChannel(channel);
    }

    public void UnSubscribe(String channel) {
        mKidoGcm.RemoveSubscription(channel);
    }

    public void Push(String channel) {
        try {
            JSONObject data = new JSONObject()
                    .put("type","toast")
                    .put("text", "hello")
                    .put("title" , "my notification")
                    .put("badge","1");
            mKidoGcm.PushMessage(channel,data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void setPushEvents(IPushEvents authEvents) {
        this.mPushEvents = authEvents;
    }

    @Override
    public void onInitializationComplete(Boolean success, String message, String registrationId, String deviceId) {
        if (mPushEvents!=null) mPushEvents.onInitializationDone("onInitializationComplete. Success: " + String.valueOf(success) );
    }

    @Override
    public void onSubscriptionComplete(Boolean success, String message) {
        if (mPushEvents!=null) mPushEvents.onSubscriptionDone("onSubscriptionComplete. Success: " + String.valueOf(success) );
    }

    @Override
    public void onPushMessageComplete(Boolean success, String message) {
        if (mPushEvents!=null) mPushEvents.onPushDone("onPushMessageComplete. Success: " + String.valueOf(success) );
    }

    @Override
    public void onRemoveSubscriptionComplete(Boolean success, String message) {
        if (mPushEvents!=null) mPushEvents.onRemoveSubscriptionDone("onRemoveSubscriptionComplete. Success: " + String.valueOf(success) );
    }

    @Override
    public void onGetSubscriptionsComplete(boolean success, String message) {
        Log.d(TAG,message);
    }
}