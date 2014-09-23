package kidozen.client.authentication;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

/**
* Created by christian on 9/22/14.
*/
public class GPlusAuthenticationActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private String TAG = this.getClass().getSimpleName();
    public static final String OAUTH2_HTTPS_WWW_GOOGLEAPIS_COM_AUTH_PLUS_LOGIN = "oauth2:https://www.googleapis.com/auth/plus.login email";
    private GoogleApiClient mGoogleApiClient;
    private String mToken;
    private ProgressDialog mConnectionProgressDialog;
    private static final int GPLUS_ACTIVITY_REQUEST_CODE = 49404;
    private boolean mResolveOnFail;

    private int mActionCode = -1;
    private String mSignInUrl = "";
    private boolean mStrictSSL = true;

    public GPlusAuthenticationActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = buildGoogleApiClient();
        buildGoogleApiClient();
        mConnectionProgressDialog = new ProgressDialog(this);
        mConnectionProgressDialog.setMessage("Signing in...");

        Intent intent = this.getIntent();
        mActionCode = intent.getIntExtra(IdentityManager.GPLUS_AUTH_ACTION_CODE, -1);
        mSignInUrl = intent.getStringExtra(kidozen.client.authentication.IdentityManager.PASSIVE_SIGNIN_URL);
        mStrictSSL = Boolean.parseBoolean(intent.getStringExtra(kidozen.client.authentication.IdentityManager.PASSIVE_STRICT_SSL)) ;


        if (!mGoogleApiClient.isConnected()) {
            // Show the dialog as we are now signing in.
            // mConnectionProgressDialog.show();
            // Make sure that we will start the resolution (e.g. fire the
            // intent and pop up a dialog for the user) for any errors
            // that come in.
            mResolveOnFail = true;
            // We should always have a connection result ready to resolve,
            // so we can start that process.
            if (mConnectionResult != null) {
                startResolution();
            } else {
                mConnectionProgressDialog.show();
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
                //.addScope(Plus.SCOPE_PLUS_LOGIN)
                //.addScope(Plus.SCOPE_PLUS_PROFILE)
                .addScope(new Scope("https://www.googleapis.com/auth/plus.login email"))
                .build();
    }

    public void Revoke(){
        if (mGoogleApiClient==null) buildGoogleApiClient();
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
        GPlusAuthenticationActivity.this.finish();
    }

    public void SignOut(){
        if (mGoogleApiClient==null) buildGoogleApiClient();
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        mGoogleApiClient.disconnect();
        GPlusAuthenticationActivity.this.finish();
    }

    //Cnn callbacks
    @Override
    public void onConnected(Bundle bundle) {
        final Context context = this.getApplicationContext();
        mConnectionProgressDialog.dismiss();
        if (mActionCode==IdentityManager.GPLUS_AUTH_ACTION_CODE_SIGN_OUT) {
            this.SignOut();
        }
        else  if (mActionCode==IdentityManager.GPLUS_AUTH_ACTION_CODE_REVOKE) {
            this.Revoke();
        }
        else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(GPlusAuthenticationResponseReceiver.ACTION_RESP);
                    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    try {
                        mToken = GoogleAuthUtil.getToken(context, Plus.AccountApi.getAccountName(mGoogleApiClient), OAUTH2_HTTPS_WWW_GOOGLEAPIS_COM_AUTH_PLUS_LOGIN);
                        Log.d(TAG, mToken);
                        String userTokeFromAuthService = mToken;
                    /*
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("wrap_scope", "?????"));
                    nameValuePairs.add(new BasicNameValuePair("wrap_assertion_format", "google_signin"));
                    nameValuePairs.add(new BasicNameValuePair("wrap_assertion", mToken));
                    String message = Utilities.getQuery(nameValuePairs);
                    SNIConnectionManager sniManager = new SNIConnectionManager(mSignInUrl, message, null, null, mStrictSSL);
                    Hashtable<String, String> authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
                    String userTokeFromAuthService = authResponse.get("responseBody");
                    String statusCode = authResponse.get("statusCode");

                    */
                        broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.REQUEST_CODE, KZPassiveAuthBroadcastConstants.REQUEST_COMPLETE_CODE);
                        broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.AUTH_SERVICE_PAYLOAD, userTokeFromAuthService);

                        //catch (IOException e) {
                        //catch (GoogleAuthException e) {
                    } catch (Exception e) {
                        broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.REQUEST_CODE, KZPassiveAuthBroadcastConstants.REQUEST_FAILED_CODE);
                        broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.ERROR_DESCRIPTION, e.getMessage());
                    }
                    sendBroadcast(broadcastIntent);

                    GPlusAuthenticationActivity.this.finish();
                }
            }).start();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnSuspended");
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
                // Starts the resolution of the problem ( may be
                // showing the user an account chooser or similar )
                startResolution();
            }
        }
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        Log.v(TAG, "ActivityResult: requestCode :" + requestCode + "; responseCode: " + responseCode);
        if (requestCode == GPLUS_ACTIVITY_REQUEST_CODE && responseCode == RESULT_OK) {
            // If we have a successful result, we will want to be able to
            // resolve any further errors, so turn on resolution with our
            // flag.
            mResolveOnFail = true;
            // If we have a successful result, lets call connect() again. If
            // there are any more errors to resolve we'll get our
            // onConnectionFailed, but if not, we'll get onConnected.
            mGoogleApiClient.connect();
        } else if (requestCode == GPLUS_ACTIVITY_REQUEST_CODE && responseCode != RESULT_OK) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(GPlusAuthenticationResponseReceiver.ACTION_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.REQUEST_CODE, KZPassiveAuthBroadcastConstants.REQUEST_CANCEL_BY_USER_CODE);
            broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.ERROR_DESCRIPTION, KZPassiveAuthBroadcastConstants.CANCEL_BY_USER_MESSAGE);
            sendBroadcast(broadcastIntent);

            GPlusAuthenticationActivity.this.finish();
            mConnectionProgressDialog.dismiss();
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
            mConnectionResult.startResolutionForResult(this, GPLUS_ACTIVITY_REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            // Any problems, just try to connect() again so we get a new
            // ConnectionResult.
            mGoogleApiClient.connect();
        }
    }

}
