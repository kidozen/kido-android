package kidozen.client.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import kidozen.client.AnalyticsLog;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;

/**
 * Created by christian on 10/22/14.
 */
public class Uploader {
    static Uploader mSingleton = null;

    private Context mContext = null;
    private String TAG = this.getClass().getSimpleName();
    private Session mSession = null;
    private Handler mUploaderHandler = new Handler();
    private Runnable mUploaderTimerTask;
    private AnalyticsLog mLogger;

    private ServiceEventListener uploadCallback = new ServiceEventListener() {
        @Override
        public void onFinish(ServiceEvent e) {

        }
    };

    public void StartUploaderTransitionTimer() {
        final int timerTimeout = mSession.getSessionTimeout() * 60000;
        this.mUploaderTimerTask = new Runnable() {
            public void run() {
                uploadCurrentEvents();
                mUploaderHandler.postDelayed(this, timerTimeout);
            }
        };
        this.mUploaderHandler.postDelayed(mUploaderTimerTask, timerTimeout);
    }

    private void uploadCurrentEvents() {
        String message = mSession.GetEventsSerializedAsJson();
        Log.d(TAG,"Timer: " + message);
        this.mUploaderHandler.removeCallbacksAndMessages(0);
        mLogger.Write(message,new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                if (e.StatusCode== HttpStatus.SC_OK) {
                    mSession.RemoveSavedEvents();
                    mSession.RemoveCurrentEvents();
                }
                StartUploaderTransitionTimer();
            }
        });

    }
    public Uploader(Context context, Session session, AnalyticsLog log) {
        mContext = context;
        mSession = session;
        mLogger = log;
        registerApplicationCallbacks();
        StartUploaderTransitionTimer();
    }

    public static Uploader getInstance(Context context, Session session, AnalyticsLog log) {
        if (mSingleton == null) {
            mSingleton = new Uploader(context,session, log);

        }
        return mSingleton;
    }

    /*
    *
    * http://stackoverflow.com/questions/4414171/how-to-detect-when-an-android-app-goes-to-the-background-and-come-back-to-the-fo/15573121#15573121
    *
    * Here's how I've managed to solve this. It works on the premise that using a time reference between activity transitions will most
    * likely provide adequate evidence that an app has been "backgrounded" or not. First, I've used an android.app.Application instance
    * (let's call it MyApplication) which has a Timer, a TimerTask, a constant to represent the maximum number of milliseconds that the
    * transition from one activity to another could reasonably take (I went with a value of 2s), and a boolean to indicate whether or not
    * the app was "in the background":
    *
    * */

    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    private boolean mWasInBackground;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;

    //The application also provides two methods for starting and stopping the timer/task:
    private void startActivityTransitionTimer() {
        this.mActivityTransitionTimer = new Timer();
        this.mActivityTransitionTimerTask = new TimerTask() {
            public void run() { mWasInBackground = true; }
        };
        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask,MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    private void stopActivityTransitionTimer() {
        if (this.mActivityTransitionTimerTask != null) {
            this.mActivityTransitionTimerTask.cancel();
        }
        if (this.mActivityTransitionTimer != null) {
            this.mActivityTransitionTimer.cancel();
        }
        this.mWasInBackground = false;
    }
    // The last piece of this solution is to add a call to each of these methods from the onResume() and onPause() events of all activities
    private void registerApplicationCallbacks() {
        ((android.app.Application)mContext).registerActivityLifecycleCallbacks(new android.app.Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivityResumed");
                if (mWasInBackground) {
                    Log.d(TAG,"WAS in background");
                    try {
                        String content = mSession.LoadEventsFromDisk();
                        Log.d(TAG,content);
                        mSession.RemoveSavedEvents();
                        mSession.RemoveCurrentEvents();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                stopActivityTransitionTimer();
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.d(TAG, activity.getLocalClassName() + "; onActivityPaused");
                startActivityTransitionTimer();
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }
        });
    }

    /*
    * So in the case when the user is simply navigating between the activities of your app, the onPause() of the departing activity starts
    * the timer, but almost immediately the new activity being entered cancels the timer before it can reach the max transition time.
    * And so wasInBackground would be false.
    * On the other hand when an Activity comes to the foreground from the Launcher, device wake up, end phone call, etc., more than likely
    * the timer task executed prior to this event, and thus wasInBackground was set to true.
    * */
}
