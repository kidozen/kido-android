package kidozen.client.authentication;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

/**
* Created by christian on 9/22/14.
*/
public class GPlusIpActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private String TAG = this.getClass().getSimpleName();
    public static final String OAUTH2_HTTPS_WWW_GOOGLEAPIS_COM_AUTH_PLUS_LOGIN = "oauth2:https://www.googleapis.com/auth/plus.login";
    private GoogleApiClient mGoogleApiClient;
    private String mToken;
    private boolean firstConnect = false;

    public GPlusIpActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = buildGoogleApiClient();
        buildGoogleApiClient();

        if (!mGoogleApiClient.isConnected()) {
            // Show the dialog as we are now signing in.
            //mConnectionProgressDialog.show();
            // Make sure that we will start the resolution (e.g. fire the
            // intent and pop up a dialog for the user) for any errors
            // that come in.
            mResolveOnFail = true;
            // We should always have a connection result ready to resolve,
            // so we can start that process.
            if (mConnectionResult != null) {
                startResolution();
            } else {
                // If we don't have one though, we can start connect in
                // order to retrieve one.
                mGoogleApiClient.connect();
            }
        }
    }

    private GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    public void Revoke(){
        // After we revoke permissions for the user with a GoogleApiClient
        // instance, we must discard it and create a new one.
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        // Our sample has caches no user data from Google+, however we
        // would normally register a callback on revokeAccessAndDisconnect
        // to delete user data so that we comply with Google developer
        // policies.
        Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
        //mGoogleApiClient = buildGoogleApiClient();
        //mGoogleApiClient.connect();
    }

    public void SignOut(){
        // We clear the default account on sign out so that Google Play
        // services will not return an onConnected callback without user
        // interaction.
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        mGoogleApiClient.disconnect();
        //mGoogleApiClient.connect();
    }

    //Cnn callbacks
    @Override
    public void onConnected(Bundle bundle) {
        final Context context = this.getApplicationContext();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mToken = GoogleAuthUtil.getToken(
                            context,
                            Plus.AccountApi.getAccountName(mGoogleApiClient),
                            OAUTH2_HTTPS_WWW_GOOGLEAPIS_COM_AUTH_PLUS_LOGIN
                    );
                    Log.d(TAG, mToken);

                    Revoke();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GoogleAuthException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Log.d("", "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("", "onConnSuspended");
    }

    private PendingIntent mSignInIntent;
    private int mSignInError;

    //connection failed
    private ConnectionResult mConnectionResult;

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        mSignInIntent = result.getResolution();
        mSignInError = result.getErrorCode();

        if (result.hasResolution()) {
            mConnectionResult = result;
            if (mResolveOnFail) {
                // This is a local helper function that starts
                // the resolution of the problem, which may be
                // showing the user an account chooser or similar.
                startResolution();
            }
        }
    }
    private static final int OUR_REQUEST_CODE = 49404;
    private boolean mResolveOnFail;

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        Log.v(TAG, "ActivityResult: " + requestCode);
        if (requestCode == OUR_REQUEST_CODE && responseCode == RESULT_OK) {
            // If we have a successful result, we will want to be able to
            // resolve any further errors, so turn on resolution with our
            // flag.
            mResolveOnFail = true;
            // If we have a successful result, lets call connect() again. If
            // there are any more errors to resolve we'll get our
            // onConnectionFailed, but if not, we'll get onConnected.
            mGoogleApiClient.connect();
        } else if (requestCode == OUR_REQUEST_CODE && responseCode != RESULT_OK) {
            // If we've got an error we can't resolve, we're no
            // longer in the midst of signing in, so we can stop
            // the progress spinner.
            //mConnectionProgressDialog.dismiss();
        }
    }

    private void startResolution() {
        try {
            // Don't start another resolution now until we have a
            // result from the activity we're about to start.
            mResolveOnFail = false;
            // If we can resolve the error, then call start resolution
            // and pass it an integer tag we can use to track. This means
            // that when we get the onActivityResult callback we'll know
            // its from being started here.
            mConnectionResult.startResolutionForResult(this, OUR_REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            // Any problems, just try to connect() again so we get a new
            // ConnectionResult.
            mGoogleApiClient.connect();
        }
    }

}
