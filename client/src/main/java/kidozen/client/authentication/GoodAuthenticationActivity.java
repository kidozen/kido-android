package kidozen.client.authentication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.good.gd.GDAndroid;
import com.good.gd.GDStateListener;
import com.good.gd.utility.GDAuthTokenCallback;
import com.good.gd.utility.GDUtility;

import java.util.Map;

/**
 * Created by facundo on 25/03/15.
 */
public class GoodAuthenticationActivity extends Activity implements GDStateListener, GDAuthTokenCallback {
    private static final String TAG = GoodAuthenticationActivity.class.getSimpleName();
    public static final String ACTION_RESP = "com.kidozen.intent.action.GOOD_AUTHENTICATION_RESULT";
    public static final String EXTRA_GOOD_TOKEN = "EXTRA_GOOD_TOKEN";

    //TODO try to put the logic here instead of the activity
    private class StateListener implements GDStateListener {

        @Override
        public void onAuthorized() {

        }

        @Override
        public void onLocked() {

        }

        @Override
        public void onWiped() {

        }

        @Override
        public void onUpdateConfig(Map<String, Object> stringObjectMap) {

        }

        @Override
        public void onUpdatePolicy(Map<String, Object> stringObjectMap) {

        }

        @Override
        public void onUpdateServices() {

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GDAndroid.getInstance().setGDStateListener(new StateListener());
        GDAndroid.getInstance().activityInit(this);

    }

    @Override
    public void onAuthorized() {
        //If Activity specific GDStateListener is set then its onAuthorized( ) method is called when
        //the activity is started if the App is already authorized
        Log.i(TAG, "onAuthorized()");
        GDUtility util = new GDUtility();

        //FIXME hardcoded url
        util.getGDAuthToken("challenge", "https://goodcontrol.kidozen.com", this);
    }

    public void onGDAuthTokenSuccess(java.lang.String token) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_RESP);
        broadcastIntent.putExtra(EXTRA_GOOD_TOKEN, token);
        sendBroadcast(broadcastIntent);

        this.finish();
    }

    //TODO handle other cases
    public void onGDAuthTokenFailure(int i, java.lang.String s) {

    }

    @Override
    public void onLocked() {
        Log.i(TAG, "onLocked()");
    }

    @Override
    public void onWiped() {
        Log.i(TAG, "onWiped()");
    }

    @Override
    public void onUpdateConfig(Map<String, Object> settings) {
        Log.i(TAG, "onUpdateConfig()");
    }

    @Override
    public void onUpdatePolicy(Map<String, Object> policyValues) {
        Log.i(TAG, "onUpdatePolicy()");
    }

    @Override
    public void onUpdateServices() {
        Log.i(TAG, "onUpdateServices()");
    }
}
