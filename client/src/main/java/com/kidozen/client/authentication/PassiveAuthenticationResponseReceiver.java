package com.kidozen.client.authentication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;

import org.apache.http.HttpStatus;
import org.json.JSONException;

/**
* Created by christian on 6/19/14.
*/
public class PassiveAuthenticationResponseReceiver extends BroadcastReceiver {
    public static final String ACTION_RESP = "com.kidozen.intent.action.PASSIVE_AUTHENTICATION_RESULT";
    public static final String COULD_NOT_GET_AUTHENTICATION_RESPONSE = "Could not get authentication response";
    private final String TAG = PassiveAuthenticationResponseReceiver.class.getSimpleName();
    private kidozen.client.ServiceEventListener mPassiveListenner;

    public PassiveAuthenticationResponseReceiver(kidozen.client.ServiceEventListener callback) {
        this.mPassiveListenner = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        kidozen.client.ServiceEvent event = null;
        int response = intent.getIntExtra(kidozen.client.authentication.PassiveAuthenticationActivity.AUTHENTICATION_RESULT,0);
        switch (response) {
            case 0:
                event = new kidozen.client.ServiceEvent(this, HttpStatus.SC_METHOD_FAILURE, COULD_NOT_GET_AUTHENTICATION_RESPONSE, null, new Exception(COULD_NOT_GET_AUTHENTICATION_RESPONSE));
                break;
            case kidozen.client.authentication.PassiveAuthenticationActivity.REQUEST_FAILED:
                String errorDescription = intent.getStringExtra(kidozen.client.authentication.PassiveAuthenticationActivity.ERROR_DESCRIPTION);
                event = new kidozen.client.ServiceEvent(this, HttpStatus.SC_UNAUTHORIZED, errorDescription, null, new Exception(errorDescription));
                break;
            case kidozen.client.authentication.PassiveAuthenticationActivity.REQUEST_COMPLETE:
                String authServicePayload = intent.getStringExtra(kidozen.client.authentication.PassiveAuthenticationActivity.AUTH_SERVICE_PAYLOAD);
                if(authServicePayload != null && !authServicePayload.isEmpty()) {
                    String token = authServicePayload.replace("access_token", "rawToken");
                    try {
                        kidozen.client.authentication.IdentityManager im = kidozen.client.authentication.IdentityManager.getInstance();
                        kidozen.client.authentication.KidoZenUser user = im.createKidoZenUser(token, kidozen.client.authentication.KidoZenUserIdentityType.PASSIVE_IDENTITY);
                        if (user!=null) {
                            String userUniqueIdentifier = user.Claims.get("http://schemas.kidozen.com/userid").toString();
                            im.addToTokensCache(userUniqueIdentifier, token, kidozen.client.authentication.KidoZenUserIdentityType.APPLICATION_IDENTITY);

                        }
                        event = new kidozen.client.ServiceEvent(this, HttpStatus.SC_OK, token, user);

                    }
                    catch(JSONException je) {
                        event = new kidozen.client.ServiceEvent(this, HttpStatus.SC_METHOD_FAILURE, je.getMessage(), null, je);
                    }
                }
                else {
                    event = new kidozen.client.ServiceEvent(this, HttpStatus.SC_METHOD_FAILURE, COULD_NOT_GET_AUTHENTICATION_RESPONSE, null, new Exception(COULD_NOT_GET_AUTHENTICATION_RESPONSE));
                }
                break;
        }
        if (mPassiveListenner!=null) {
            mPassiveListenner.onFinish(event);
        }
    }

}
