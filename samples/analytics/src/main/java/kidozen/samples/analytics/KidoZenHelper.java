package kidozen.samples.analytics;

import android.content.Context;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import kidozen.client.InitializationException;
import kidozen.client.KZApplication;

/**
 * Created by christian on 7/8/14.
 */
public class KidoZenHelper {
    private KZApplication kido = null;
    private String tenantMarketPlace = "https://contoso.kidocloud.com";
    private String application = "tasks";
    private String appkey = "get this value from: marketplace -> application -> coding -> keys";

    private Boolean isInitialized    = false;
    private kidozen.samples.analytics.IAuthenticationEvents authEvents;
    private Context mContext;

    public KidoZenHelper() {
        kido = new KZApplication(tenantMarketPlace, application, appkey, false);
        thisInstance = this;
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

    public KidoZenHelper(Context context) {
        mContext = context;
        kido = new KZApplication(tenantMarketPlace, application, appkey, false);
        thisInstance = this;
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
        kido.Authenticate(context, new kidozen.client.ServiceEventListener() {
            @Override
            public void onFinish(kidozen.client.ServiceEvent e) {
                if (e.StatusCode == HttpStatus.SC_OK ) {
                    if (authEvents!=null && isInitialized) {
                        authEvents.ReturnUserName(kido.GetKidoZenUser().Claims.get("name"));
                        kido.EnableAnalytics(mContext);
                        kido.SetAnalyticsSessionTimeOutInSeconds(60);
                    }
                }
            }
        });
    }

    public void setAuthEvents(IAuthenticationEvents authEvents) {
        this.authEvents = authEvents;
    }

    public void TagCustom(String values) {
        JSONObject message = null;
        try {
            message = new JSONObject().put("bar",values);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        kido.TagCustom("CustomTag",message);
    }

    public void TagClick(String message) {
        kido.TagClick(message);
    }

    public void TagActivity(String name) {
        kido.TagActivity(name);
    }

    private static KidoZenHelper thisInstance;

    public static KidoZenHelper getInstance(Context context) {
        if (thisInstance == null) {
            thisInstance = new KidoZenHelper(context);
        }
        return thisInstance;
    }
}