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
 * Activity that displays the Good Authentication view and broadcasts the token if successful.
 */
public class GoodAuthenticationActivity extends Activity  {
    private static final String TAG = GoodAuthenticationActivity.class.getSimpleName();
    public static final String ACTION_RESP = "com.kidozen.intent.action.GOOD_AUTHENTICATION_RESULT";
    public static final String EXTRA_GOOD_AUTH_SUCCESS = "EXTRA_GOOD_AUTH_SUCCESS";
    public static final String EXTRA_GOOD_ERROR_MSG = "EXTRA_GOOD_ERROR_MSG";
    public static final String EXTRA_GOOD_TOKEN = "EXTRA_GOOD_TOKEN";
    public static final String EXTRA_SERVER_URL = "EXTRA_SERVER_URL";

    private class GoodStateListener implements GDStateListener, GDAuthTokenCallback {

        private GoodAuthenticationActivity parent;

        public GoodStateListener(GoodAuthenticationActivity parent) {
            this.parent = parent;
        }

        @Override
        public void onAuthorized() {
            Log.d(TAG, "onAuthorized()");
            GDUtility util = new GDUtility();

            String serverUrl = parent.getIntent().getStringExtra(EXTRA_SERVER_URL);
            //once authorized, request the good auth token
            util.getGDAuthToken("challenge", serverUrl, this);
        }

        public void onGDAuthTokenSuccess(java.lang.String token) {
            this.finishSuccess(token);
        }

        public void onGDAuthTokenFailure(int errCode, java.lang.String errMsg) {
            this.finishFailure("Request for GD Auth token has failed: " + errMsg);
        }

        @Override
        public void onWiped() {
            this.finishFailure("The authorization of the user has been permanently withdrawn");
        }

        private void finishSuccess(String token) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_RESP);
            broadcastIntent.putExtra(EXTRA_GOOD_AUTH_SUCCESS, true);
            broadcastIntent.putExtra(EXTRA_GOOD_TOKEN, token);
            //broadcasts the token to the receiver in IdentityManager
            sendBroadcast(broadcastIntent);

            this.parent.finish();
        }

        private void finishFailure(String errMsg) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_RESP);
            broadcastIntent.putExtra(EXTRA_GOOD_AUTH_SUCCESS, false);
            broadcastIntent.putExtra(EXTRA_GOOD_ERROR_MSG, errMsg);
            sendBroadcast(broadcastIntent);

            this.parent.finish();
        }

        @Override
        public void onLocked() {}
        @Override
        public void onUpdateConfig(Map<String, Object> stringObjectMap) {}
        @Override
        public void onUpdatePolicy(Map<String, Object> stringObjectMap) {}
        @Override
        public void onUpdateServices() {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set a listener to handle the GD events
        GDAndroid.getInstance().setGDStateListener(new GoodStateListener(this));
        //init activity to show good login
        GDAndroid.getInstance().activityInit(this);
    }

}
