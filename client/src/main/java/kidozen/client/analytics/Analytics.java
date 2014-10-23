package kidozen.client.analytics;

import android.app.*;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by christian on 10/22/14.
 */
public class Analytics {
    final String TAG = this.getClass().getSimpleName();
    Session mSession = null;
    Context mContext = null;
    Uploader mUploader = new Uploader();
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
            if(!context.getClass().isInstance(android.app.Application.class))
                mContext = context.getApplicationContext();
            else
                mContext =context;

            mSession = new Session(context);
            registerApplicationCallbacks();
        }
        else {
            mSession = null;
        }
    }

    private void registerApplicationCallbacks() {
        ((android.app.Application)mContext).registerActivityLifecycleCallbacks(new android.app.Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivityCreated");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivityStarted");
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivityCreated");
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivityResumed");
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivityStopped");
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivitySaveInstanceState");
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivityDestroyed");
            }
        });

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
