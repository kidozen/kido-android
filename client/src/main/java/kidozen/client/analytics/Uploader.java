package kidozen.client.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

/**
 * Created by christian on 10/22/14.
 */
public class Uploader {
    static Uploader mSingleton = null;

    private Context mContext = null;
    private String TAG = this.getClass().getSimpleName();
    private Session mSession = null;


    public Uploader(Context context, Session session) {
        mContext = context;
        mSession = session;
        registerApplicationCallbacks();
    }

    public static Uploader getInstance(Context context, Session session) {
        if (mSingleton == null) {
            mSingleton = new Uploader(context,session);
        }
        return mSingleton;
    }

    //http://stackoverflow.com/questions/4414171/how-to-detect-when-an-android-app-goes-to-the-background-and-come-back-to-the-fo
    private void registerApplicationCallbacks() {
        ((android.app.Application)mContext).registerActivityLifecycleCallbacks(new android.app.Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivityCreated");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivityStarted");
                try {
                    String content = mSession.LoadEventsFromDisk();
                    Log.d(TAG, content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivityResumed");
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivityPaused");
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivityStopped");
                try {
                    mSession.Save();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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


}
