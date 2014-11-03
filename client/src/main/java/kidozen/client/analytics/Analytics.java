package kidozen.client.analytics;

import android.content.Context;

import org.json.JSONObject;

import kidozen.client.AnalyticsLog;

/**
 * Created by christian on 10/22/14.
 */
public class Analytics {
    final String TAG = this.getClass().getSimpleName();
    static Analytics mSingleton = null;

    private Session mSession = null;
    private Context mContext = null;

    private Uploader mUploader = null;
    private AnalyticsLog mLogger;

    public static Analytics getInstance() {
        if (mSingleton == null) {
            mSingleton = new Analytics();
        }
        return mSingleton;
    }

    public static Analytics getInstance(Boolean enable, Context context, AnalyticsLog logger) {
        if (mSingleton == null) {
            mSingleton = new Analytics();
            mSingleton.Enable(enable,context,logger);
        }
        return mSingleton;
    }

    public void Enable(Boolean enable, Context context, AnalyticsLog logger) {
        if (enable) {
            mContext = (!context.getClass().isInstance(android.app.Application.class) ? context.getApplicationContext() : context);
            mSession = new Session(context);
            mLogger = logger;
            mUploader = Uploader.getInstance(mContext, mSession, mLogger);
            mUploader.StartUploaderTransitionTimer();
        }
        else {
            mSession = null;
        }
    }

    public void TagClick(String data) {
        ClickEvent event = new ClickEvent(data, mSession.getUUID());
        mSession.LogEvent(event);
    }
    public void TagActivity(String data) {
        ActivityEvent event = new ActivityEvent(data, mSession.getUUID());
        mSession.LogEvent(event);
    }
    public void TagEvent(String title, JSONObject data) {
        CustomEvent event = new CustomEvent(title,data, mSession.getUUID());
        mSession.LogEvent(event);
    }

    public void SetSessionTimeOutInSeconds(int timeout) {
        mSession.setSessionTimeoutInSeconds(timeout);
        mUploader.StartUploaderTransitionTimer();
    }
}
