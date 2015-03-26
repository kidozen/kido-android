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
public class GoodAuthenticationActivity extends Activity  {
    private static final String TAG = GoodAuthenticationActivity.class.getSimpleName();
    public static final String ACTION_RESP = "com.kidozen.intent.action.GOOD_AUTHENTICATION_RESULT";
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
            //broadcast the token to the receiver in IdentityManager
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_RESP);
            broadcastIntent.putExtra(EXTRA_GOOD_TOKEN, token);
            sendBroadcast(broadcastIntent);

            this.parent.finish();
        }

        public void onGDAuthTokenFailure(int i, java.lang.String s) {
            //TODO auth failure
        }

        @Override
        public void onWiped() {
            // the authorization of the user has been permanently withdrawn
            //TODO auth failure
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
