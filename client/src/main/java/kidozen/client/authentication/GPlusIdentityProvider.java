package kidozen.client.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Created by christian on 9/22/14.
 */
public class GPlusIdentityProvider extends BaseIdentityProvider {
    private String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private String mToken;
    private GPlusAuthenticationActivity mGplusIpActivity;


    public GPlusIdentityProvider(Context context) {
        mContext = context;
    }

    @Override
    public String RequestToken() throws Exception {
        mGplusIpActivity = new GPlusAuthenticationActivity();
        Intent startPassiveAuth = new Intent(mContext, GPlusAuthenticationActivity.class);
        startPassiveAuth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(startPassiveAuth);

        return  "wasabi";
    }

    public GPlusIdentityProvider(Activity context) {
        mContext = context;
        mGplusIpActivity = new GPlusAuthenticationActivity();
    }

    public void Revoke() {

    }

    public void SignOut() {

    }
}
