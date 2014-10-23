package kidozen.client.analytics;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by christian on 10/22/14.
 */
public class Analytics {
    final String TAG = this.getClass().getSimpleName();
    Session mSession = null;
    Context mContext = null;
    Uploader mUploader = null;

    static Analytics mSingleton = null;

    public static Analytics getInstance() {
        if (mSingleton == null) {
            mSingleton = new Analytics();
        }
        return mSingleton;
    }

    public static Analytics getInstance(Boolean enable, Context context) {
        if (mSingleton == null) {
            mSingleton = new Analytics();
            mSingleton.Enable(enable,context);
        }
        return mSingleton;
    }

    public void Enable(Boolean enable, Context context) {
        if (enable) {
            mContext = (!context.getClass().isInstance(android.app.Application.class) ? context.getApplicationContext() : context);
            mSession = new Session(context);
            mUploader = Uploader.getInstance(mContext, mSession);
        }
        else {
            mSession = null;
        }
    }

    public void Reset() {
        mSession.RemoveSavedEvents();
        mSession.RemoveCurrentEvents();
        mSession.StartNew();
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
}
