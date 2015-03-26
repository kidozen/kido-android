package kidozen.client.authentication;

import android.app.Activity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GDAndroid.getInstance().activityInit(this);

    }

    /*
     * Activity specific implementation of GDStateListener.
     *
     * If a singleton event Listener is set by the application (as it is in this case) then setting
     * Activity specific implementations of GDStateListener is optional
     */
    @Override
    public void onAuthorized() {
        //If Activity specific GDStateListener is set then its onAuthorized( ) method is called when
        //the activity is started if the App is already authorized
        Log.i(TAG, "onAuthorized()");
        GDUtility util = new GDUtility();
        util.getGDAuthToken("challenge", "https://goodcontrol.kidozen.com", this);
    }

    public void onGDAuthTokenSuccess(java.lang.String s) {
        Log.i(TAG, "Got token " + s);
        this.finish();
    }

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
