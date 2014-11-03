package kidozen.client.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import kidozen.client.AnalyticsLog;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;

/**
 * Created by christian on 10/22/14.
 */
public class Uploader {
    private static final String NO_SESSION_EVENTS = "NO_SESSION_EVENTS";
    static Uploader mSingleton = null;

    private Context mContext = null;
    private String TAG = this.getClass().getSimpleName();
    private Session mSession = null;
    private Handler mUploaderHandler = new Handler();
    private Runnable mUploaderTimerTask;
    private AnalyticsLog mLogger;


    public void StartUploaderTransitionTimer() {
        if (mUploaderHandler!=null) mUploaderHandler.removeCallbacksAndMessages(0);

        final int timerTimeout = mSession.getSessionTimeoutInSeconds();
        this.mUploaderTimerTask = new Runnable() {
            public void run() {
                uploadCurrentEvents();
                mUploaderHandler.postDelayed(this, timerTimeout);
            }
        };
        this.mUploaderHandler.postDelayed(mUploaderTimerTask, timerTimeout);
    }

    private void uploadCurrentEvents() {
        String events = mSession.GetEventsSerializedAsJson();
        if ( events==null || events.isEmpty()) return;
        String sessionInformation = events;

        if (!mWasInBackground) {
            try {
                String sessionDetails = mSession.LoadSessionInformationFromDisk();
                Gson gson = new Gson();
                SessionDetails details =  gson.fromJson(sessionDetails,SessionDetails.class);
                details.EndDate = new Date().getTime();
                details.length = details.EndDate - details.StartDate;
                details.eventAttr.sessionLength = String.valueOf(details.length);
                //serialize again to upload to server
                sessionDetails =  gson.toJson(details);

                int endJsonArray = events.lastIndexOf("]");
                if (endJsonArray>0) {
                    String message = events.substring(0,endJsonArray);
                    sessionInformation = message + "," + sessionDetails + "]";
                }
                Log.d(TAG,"Uploading events after session timeout : " + sessionInformation);
                this.mUploaderHandler.removeCallbacksAndMessages(0);
                mLogger.Write(sessionInformation,new ServiceEventListener() {
                    @Override
                    public void onFinish(ServiceEvent e) {
                        if (e.StatusCode== HttpStatus.SC_CREATED) {
                            mSession.Reset();
                        }
                        StartUploaderTransitionTimer();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public Uploader(Context context, Session session, AnalyticsLog log) {
        mContext = context;
        mSession = session;
        mLogger = log;
        registerApplicationCallbacks();
    }

    public static Uploader getInstance(Context context, Session session, AnalyticsLog log) {
        if (mSingleton == null) {
            mSingleton = new Uploader(context,session, log);

        }
        return mSingleton;
    }

    private void uploadSessionAndEvents(String message) {
        //Log.d(TAG,"Uploading events and session information: " + message);
        this.mUploaderHandler.removeCallbacksAndMessages(0);
        mLogger.Write(message,new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                if (e.StatusCode== HttpStatus.SC_CREATED) {
                    mSession.Reset();
                }
                StartUploaderTransitionTimer();
            }
        });
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
                    try {
                        String message = mergeSessionAndEvents();
                        if (!message.equalsIgnoreCase(NO_SESSION_EVENTS)) {
                            uploadSessionAndEvents(message);
                        }
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
                if (activity.isFinishing()) {

                }
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }
        });
    }
    //TODO: Fix this Quick and dirty merge.
    private String mergeSessionAndEvents() throws IOException {
        String events = mSession.LoadEventsFromDisk();
        String sessionDetails = mSession.LoadSessionInformationFromDisk();
        //Log.d("MERGE","READING EVENTS: " + events);
        //Log.d("MERGE","READING sessionDetails: " + sessionDetails);
        //Deserialize the session details to update end date
        Gson gson = new Gson();
        SessionDetails details =  gson.fromJson(sessionDetails,SessionDetails.class);
        details.EndDate = new Date().getTime();
        details.length = details.EndDate - details.StartDate;
        details.eventAttr.sessionLength = String.valueOf(details.length);
        //details.eventAttr.uniqueId = details.sessionUUID;
        //serialize again to upload to server
        sessionDetails =  gson.toJson(details);
        String sessionInformation;

        int endJsonArray = events.lastIndexOf("]");
        if (endJsonArray>0) {
            String message = events.substring(0,endJsonArray);
            sessionInformation = message + "," + sessionDetails + "]";
            return sessionInformation;
        }
        else {
            return NO_SESSION_EVENTS;
        }
    }

    /*
    * So in the case when the user is simply navigating between the activities of your app, the onPause() of the departing activity starts
    * the timer, but almost immediately the new activity being entered cancels the timer before it can reach the max transition time.
    * And so wasInBackground would be false.
    * On the other hand when an Activity comes to the foreground from the Launcher, device wake up, end phone call, etc., more than likely
    * the timer task executed prior to this event, and thus wasInBackground was set to true.
    * */
}
