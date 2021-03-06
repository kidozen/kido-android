package kidozen.client.analytics;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import kidozen.client.AnalyticsLog;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.analytics.events.ActivityEvent;
import kidozen.client.analytics.events.ClickEvent;
import kidozen.client.analytics.events.CustomEvent;
import kidozen.client.analytics.events.SessionStartEvent;
import kidozen.client.authentication.KidoZenUser;

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
    private KidoZenUser mUserIdentity;

    private String appVersion;

    public void setmUserIdentity(KidoZenUser mUserIdentity) {
        this.mUserIdentity = mUserIdentity;
    }


    public static Analytics getInstance() {
        if (mSingleton == null) {
            mSingleton = new Analytics();
        }
        return mSingleton;
    }

    public static Analytics getInstance(Boolean enable, Context context, AnalyticsLog logger, KidoZenUser user) {
        if (mSingleton == null) {
            mSingleton = new Analytics();
            mSingleton.setmUserIdentity(user);
            mSingleton.Enable(enable,context,logger);
        }
        return mSingleton;
    }

    public void Enable(Boolean enable, Context context, AnalyticsLog logger) {
        if (enable) {
            mContext = (!context.getClass().isInstance(android.app.Application.class) ? context.getApplicationContext() : context);
            appVersion = getAppVersion(mContext);
            mLogger = logger;
            mSession = new Session(context);
            checkPendingSessions();
            mSession.StartNew(mUserIdentity.getUserHash());
            mUploader = Uploader.getInstance(mContext, mSession, mLogger);
            mUploader.StartUploaderTransitionTimer();
            mSession.sessionStart(new SessionStartEvent(mContext, mSession.getUUID(), mUserIdentity.getUserHash(), appVersion));

        }
        else {
            mSession = null;
        }
    }


    private String getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            return new Integer(packageInfo.versionCode).toString();
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    //In android we don't have a reliable method to check if the whole application was closed by user or OS
    //Maybe a previous application was closed and pending events must be sent

    private void checkPendingSessions() {
        try {
            ArrayList<String> sessions = mSession.GetPendingSessions();
            for (final String s : sessions) {
                String sessionDetails = mSession.readFile(s);
                Gson gson = new Gson();
                SessionDetails details =  gson.fromJson(sessionDetails,SessionDetails.class);
                details.EndDate = new Date().getTime();
                details.length = details.EndDate - details.StartDate;
                details.eventAttr.sessionLength = TimeUnit.MILLISECONDS.toSeconds(details.length) % 60;
                details.isPeding = true;
                //serialize again to upload to server
                sessionDetails =  gson.toJson(details);

                String pendingSessionAndEvents = "[" + sessionDetails + "]";
                final String eventsFileName = s.replace(".session",".events");
                final String events = mSession.readFile(eventsFileName);
                // If there was events, adds the session details to events array
                if ( events!=null && !events.isEmpty()) {
                    int endJsonArray = events.lastIndexOf("]");
                    if (endJsonArray>0) {
                        String message = events.substring(0,endJsonArray);
                        pendingSessionAndEvents = message + "," + sessionDetails + "]";
                    }
                }
                Log.d(TAG, "Uploading pending session & events");
                mLogger.Write(pendingSessionAndEvents, new ServiceEventListener() {
                    @Override
                    public void onFinish(ServiceEvent e) {
                        if (e.StatusCode == HttpStatus.SC_CREATED) {
                            if ( events!=null && !events.isEmpty())
                                mContext.deleteFile(eventsFileName);
                            mContext.deleteFile(s);
                            Log.d(TAG, "Uploaded pending session & events");
                        }
                    }
                });

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void StartSession() {
        mSession.ResetEvents();
        mUploader = Uploader.getInstance(mContext, mSession, mLogger);
        mUploader.StartUploaderTransitionTimer();

        mSession.sessionStart(new SessionStartEvent(mContext, mSession.getUUID(), mUserIdentity.getUserHash(), appVersion));

    }

    public void StopSession() {
        new Thread() {
            @Override
            public void run() {
                mUploader.StopUploaderTransmissionTimer();
                mUploader.UploadSession();
                mUploader.StartUploaderTransitionTimer();
            }
        }.start();
    }

    public void TagClick(String data) {
        ClickEvent event = new ClickEvent(data, mSession.getUUID(), mUserIdentity.getUserHash(), appVersion);
        mSession.LogEvent(event);
    }
    public void TagActivity(String data) {
        ActivityEvent event = new ActivityEvent(data, mSession.getUUID(), mUserIdentity.getUserHash(), appVersion);
        mSession.LogEvent(event);
    }
    public void TagEvent(String title, JSONObject data) {
        CustomEvent event = new CustomEvent(title,data, mSession.getUUID(), mUserIdentity.getUserHash(), appVersion);
        mSession.LogEvent(event);
    }

    public void SetSessionTimeOutInSeconds(int timeout) {
        mSession.setSessionTimeoutInSeconds(timeout);
        mUploader.StartUploaderTransitionTimer();
    }
}
