package kidozen.client.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Created by christian on 9/22/14.
 */
public class GooglePlusIdentityProvider extends BaseIdentityProvider {
    private String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private String mToken;
    private GPlusIpActivity mGplusIpActivity;


    public GooglePlusIdentityProvider(Context context) {
        mContext = context;
    }

    @Override
    public String RequestToken() throws Exception {
        mGplusIpActivity = new GPlusIpActivity();
        Intent startPassiveAuth = new Intent(mContext, GPlusIpActivity.class);
        startPassiveAuth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(startPassiveAuth);

        return  "wasabi";
    }

    public GooglePlusIdentityProvider(Activity context) {
        mContext = context;
        mGplusIpActivity = new GPlusIpActivity();
    }

    public void Revoke() {

    }

    public void SignOut() {

    }
}
