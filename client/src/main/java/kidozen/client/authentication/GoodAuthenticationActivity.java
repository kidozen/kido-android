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

    private class GoodStateListener implements GDStateListener, GDAuthTokenCallback {

        private GoodAuthenticationActivity parent;

        public GoodStateListener(GoodAuthenticationActivity parent) {
            this.parent = parent;
        }

        @Override
        public void onAuthorized() {
            Log.d(TAG, "onAuthorized()");
            GDUtility util = new GDUtility();

            //FIXME hardcoded url
            //once authorized, request the good auth token
            util.getGDAuthToken("challenge", "https://goodcontrol.kidozen.com", this);
        }

        public void onGDAuthTokenSuccess(java.lang.String token) {
            //broadcast the token to the receiver in IdentityManager
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_RESP);
            broadcastIntent.putExtra(EXTRA_GOOD_TOKEN, token);
            sendBroadcast(broadcastIntent);

            this.parent.finish();
        }

        //TODO handle other cases
        public void onGDAuthTokenFailure(int i, java.lang.String s) {

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
        //set a listener to handle the GD events
        GDAndroid.getInstance().setGDStateListener(new GoodStateListener(this));
        //init activity to show good login
        GDAndroid.getInstance().activityInit(this);
    }

}
