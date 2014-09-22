package kidozen.client.authentication;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

/**
 * Created by christian on 9/22/14.
 */
public class GooglePlusIdentityProvider extends BaseIdentityProvider  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String OAUTH2_HTTPS_WWW_GOOGLEAPIS_COM_AUTH_PLUS_LOGIN = "oauth2:https://www.googleapis.com/auth/plus.login";
    private Activity mContext;

    private static final int STATE_DEFAULT = 0;
    private static final int STATE_SIGN_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;
    private static final int SIGN_IN_REQUIRED = 4;
    private static final int RC_SIGN_IN = 0;
    GoogleApiClient mGoogleApiClient;
    private String TAG = this.getClass().getSimpleName();
    private String mToken;


    //BaseIdentityProvider
    @Override
    public String RequestToken() throws Exception {
        mGoogleApiClient.connect();

        return "abc";
    }

    public GooglePlusIdentityProvider(Activity context) {
        mContext = context;
        mGoogleApiClient = buildGoogleApiClient();
        buildGoogleApiClient();
    }

    private GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(mContext)
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mToken = GoogleAuthUtil.getToken(
                            mContext,
                            Plus.AccountApi.getAccountName(mGoogleApiClient),
                            OAUTH2_HTTPS_WWW_GOOGLEAPIS_COM_AUTH_PLUS_LOGIN
                    );
                    Log.d(TAG,mToken);
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
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        mSignInIntent = result.getResolution();
        mSignInError = result.getErrorCode();

        try {
            if (result.hasResolution()) result.startResolutionForResult( mContext, mSignInError);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }
}
