package kidozen.client.analytics;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.Date;

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


    public void StopUploaderTransmissionTimer() {
        if (mUploaderHandler!=null) mUploaderHandler.removeCallbacksAndMessages(0);
    }

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
        if ( events!=null && !events.isEmpty()) {
            this.mUploaderHandler.removeCallbacksAndMessages(0);
            Log.d(TAG, "Uploading events after session timeout : " + events);
            mLogger.Write(events,new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    if (e.StatusCode== HttpStatus.SC_CREATED) {
                        mSession.ResetEvents();
                        Log.d(TAG, "Upload events after session timeout");
                    }
                    StartUploaderTransitionTimer();
                }
            });
        }
    }

    public void UploadSession() {
        String events = mSession.GetEventsSerializedAsJson();
        try {
            String sessionDetails = mSession.LoadSessionInformationFromDisk();
            Gson gson = new Gson();
            SessionDetails details =  gson.fromJson(sessionDetails,SessionDetails.class);
            details.EndDate = new Date().getTime();
            details.length = details.EndDate - details.StartDate;
            details.eventAttr.sessionLength = String.valueOf(details.length);
            //serialize again to upload to server
            sessionDetails =  gson.toJson(details);

            String sessionAndEvents = "[" + sessionDetails + "]";
            // If there was events, adds the session details to events array
            if ( events!=null && !events.isEmpty()) {
                int endJsonArray = events.lastIndexOf("]");
                if (endJsonArray>0) {
                    String message = events.substring(0,endJsonArray);
                    sessionAndEvents = message + "," + sessionDetails + "]";
                }
            }
            this.mUploaderHandler.removeCallbacksAndMessages(0);
            Log.d(TAG, "Uploading session & events after session timeout : " + sessionAndEvents);
            mLogger.Write(sessionAndEvents,new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    if (e.StatusCode== HttpStatus.SC_CREATED) {
                        mSession.ResetEvents();
                        mSession.RemoveCurrentSession();
                        Log.d(TAG, "Upload session & events after session timeout");
                    }
                    StartUploaderTransitionTimer();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Uploader(Context context, Session session, AnalyticsLog log) {
        mContext = context;
        mSession = session;
        mLogger = log;
    }

    public static Uploader getInstance(Context context, Session session, AnalyticsLog log) {
        if (mSingleton == null) {
            mSingleton = new Uploader(context,session, log);

        }
        return mSingleton;
    }

}
