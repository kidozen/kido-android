package kidozen.samples.push;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

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
        mKidoGcm = new GCM(mActivity,kido,projectid);
        mKidoGcm.setGCMEvents(this);
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

    public void Initialize() {
        mKidoGcm.Initialize();
    }

    public void Subscribe() {
        mKidoGcm.SubscribeToChannel("sports");
    }

    public void Push() {
        try {
            JSONObject data = new JSONObject().put("thename","thevalue");
            mKidoGcm.PushMessage("sports",data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void setPushEvents(IPushEvents authEvents) {
        this.pushEvents = authEvents;
    }

    @Override
    public void InitializationComplete(Boolean success, String message, String registrationId, String deviceId) {
        Log.d(TAG,message);
    }

    @Override
    public void SubscriptionComplete(Boolean success, String message) {
        Log.d(TAG,message);
    }

    @Override
    public void SendMessageComplete(Boolean success, String message) {
        Log.d(TAG,message);
    }

    @Override
    public void RemoveSubscriptionComplete(Boolean success, String message) {
        Log.d(TAG,message);
    }
}