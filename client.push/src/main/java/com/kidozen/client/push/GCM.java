package com.kidozen.client.push;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;

/**
 * Created by christian on 7/8/14.
 */
public class GCM {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String TAG = this.getClass().getSimpleName();
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private String mRegistrationId;
    private GoogleCloudMessaging mGcm;
    private KZApplication mKido;
    private Context mContext;
    private Activity mActivity;
    private String mSenderId;
    private String mAndroidId;

    private IGcmEvents mGCMEvents;
    private Boolean mInitializationSuccess = false;
    private Boolean mLastSubscriptionSucess = false;
    private Boolean mLastPushSucess = false;


    private JSONArray mDeviceSubscriptions = new JSONArray();

    //Constructors do Registration
    public GCM(Activity activity, KZApplication kidoApp, String SenderId) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
        mKido = kidoApp;
        mSenderId = SenderId;
        mSenderId = "33779981368";
        mAndroidId =  Settings.Secure.getString(mContext.getContentResolver(),Settings.Secure.ANDROID_ID);
    }

    public void Initialize() {
        getKidoSubscriptions();
        if (checkPlayServices()) {
            mGcm = GoogleCloudMessaging.getInstance(mContext);
            mRegistrationId = getRegistrationId(mContext);
            if (mRegistrationId.isEmpty()) {
                registerInBackground();
            }
            else {
                if (mGCMEvents!=null) mGCMEvents.InitializationComplete(true,"RegistrationId obtained from SharedPreferences",mRegistrationId,mAndroidId);
            }
        }
    }

    /*
    * Wraps KidoZen Subscribe method
    * */
    public void SubscribeToChannel(String channel) {
        try {
            if (isDeviceRegisterInChannel(channel)) {
                if (mGCMEvents!=null) mGCMEvents.SubscriptionComplete(true, "Device already registered in channel");
            }
            else mKido.Notification().Subscribe(mAndroidId,channel,mRegistrationId,new SubscribeToChannelEventListener());
        }
        catch (Exception ex) {
            if (mGCMEvents!=null) mGCMEvents.SubscriptionComplete(false,ex.getMessage());
        }
    }

    public void PushMessage(String channel, JSONObject data) {
        try {
            mKido.Notification().Push(channel,data,new PushMessageEventListener());
        }
        catch (Exception ex) {
            if (mGCMEvents!=null) mGCMEvents.SubscriptionComplete(false,ex.getMessage());
        }
    }

    private boolean isDeviceRegisterInChannel(String channel) {
        Boolean returnValue = false;
        for (int i = 0; i < mDeviceSubscriptions.length(); ++i) {
            JSONObject object = null;

            try {
                object = mDeviceSubscriptions.getJSONObject(i);
                if (object.getString("channelName").equals(channel) && object.getString("applicationName").equals(mKido.getApplicationName())) {
                    returnValue = true;
                    break;
                };
            }
            catch (JSONException e) {
                break;
            }
        }
        return returnValue;
    }

    public void getKidoSubscriptions() {
        try {
            mKido.Notification().GetSubscriptions(mAndroidId, new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    Boolean success = (e.StatusCode == HttpStatus.SC_OK);
                    if (success) mDeviceSubscriptions = (JSONArray) e.Response;
                }
            });
        }
        catch (Exception ex) {
            String message = "Error :" + ex.getMessage();
        }
    }
    /*
        void InitializationComplete(Boolean success, String message, String registrationId, String deviceId);
        void SubscriptionComplete(Boolean success, String message);
        void SendMessageComplete(Boolean success, String message);
        void RemoveSubscriptionComplete(Boolean success, String message);
    */

    private void registerInBackground() {
        new AsyncTask<Void,Void,String>() {
            Boolean mSuccess = true;
            String mRegisterMessage = "";
            @Override
            protected String doInBackground(Void... params) {
                try {
                    if (mGcm == null) {
                        mGcm = GoogleCloudMessaging.getInstance(mContext);
                    }
                    mRegistrationId = mGcm.register(mSenderId);
                    mRegisterMessage = "Device registered, registration ID=" + mRegistrationId;
                    // Persist the regID - no need to register again.
                    storeRegistrationId(mContext, mRegistrationId);
                } catch (IOException ex) {
                    mRegisterMessage = "Error :" + ex.getMessage();
                    mSuccess = false;
                }
                return mRegisterMessage;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i(TAG, "mRegisterMessage :" + msg);
                mInitializationSuccess = mSuccess;
                if (mGCMEvents!=null) mGCMEvents.InitializationComplete(mSuccess,msg,mRegistrationId,mAndroidId);
            }
        }.execute(null, null, null);

    }


    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return mContext.getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE);
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, mActivity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                //finish();
            }
            return false;
        }
        return true;
    }

    public void setGCMEvents(IGcmEvents mGCMEvents) {
        this.mGCMEvents = mGCMEvents;
    }

    /*
    * */
    private class SubscribeToChannelEventListener implements ServiceEventListener {
        Boolean mSuccess = false;
        String mMessage = "";

        @Override
        public void onFinish(ServiceEvent e) {
            mSuccess = (e.StatusCode == HttpStatus.SC_CREATED);
            mLastSubscriptionSucess = mSuccess;
            mMessage = e.Body;
            if (mGCMEvents!=null) mGCMEvents.SubscriptionComplete(mSuccess, mMessage);
        }
    }
    /*
    * */
    private class PushMessageEventListener implements ServiceEventListener {
        Boolean mSuccess = false;
        String mMessage = "";

        @Override
        public void onFinish(ServiceEvent e) {
            mSuccess = (e.StatusCode == HttpStatus.SC_NO_CONTENT);
            mLastPushSucess = mSuccess;
            mMessage = e.Body;
            if (mGCMEvents!=null) mGCMEvents.SubscriptionComplete(mSuccess, mMessage);
        }
    }
}
