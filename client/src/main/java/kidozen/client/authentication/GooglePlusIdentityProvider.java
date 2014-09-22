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
public class GooglePlusIdentityProvider extends Activity  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private android.content.Context mContext;

    private static final int STATE_DEFAULT = 0;
    private static final int STATE_SIGN_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;
    private static final int SIGN_IN_REQUIRED = 4;
    private static final int RC_SIGN_IN = 0;
    GoogleApiClient mGoogleApiClient;
    private String TAG = this.getClass().getSimpleName();
    private String mToken;


    //BaseIdentityProvider
    public String RequestToken() throws Exception {
        mGoogleApiClient = buildGoogleApiClient();
        mGoogleApiClient.connect();
        return "abc";
    }

    public GooglePlusIdentityProvider(Context context) {
        mContext = context;
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

    //Cnn callbacks
    @Override
    public void onConnected(Bundle bundle) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mToken = GoogleAuthUtil.getToken(mContext,"christian.carnero@gmail.com","oauth2:https://www.googleapis.com/auth/plus.login");
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

    private int mSignInProgress;
    private PendingIntent mSignInIntent;
    private int mSignInError;

    //connection failed
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        mSignInIntent = result.getResolution();
        mSignInError = result.getErrorCode();

        try {
            result.startResolutionForResult((Activity) mContext, mSignInError);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }
}
