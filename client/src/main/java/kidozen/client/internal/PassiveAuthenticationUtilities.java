package kidozen.client.internal;

import android.content.Intent;

import org.apache.http.HttpStatus;

import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.authentication.IdentityManager;
import kidozen.client.authentication.KZPassiveAuthBroadcastConstants;
import kidozen.client.authentication.KZPassiveAuthTypes;
import kidozen.client.authentication.KidoZenUser;
import kidozen.client.authentication.KidoZenUserIdentityType;


/**
 * Created by christian on 9/23/14.
 */
public class PassiveAuthenticationUtilities {
    public static void DispatchServiceEventListener( Object target, Intent intent, int response, ServiceEventListener eventListener, KZPassiveAuthTypes passiveAuthType) {
        kidozen.client.ServiceEvent event = null;
        switch (response) {
            case 0:
                event = new ServiceEvent(target, HttpStatus.SC_METHOD_FAILURE, KZPassiveAuthBroadcastConstants.COULD_NOT_GET_AUTHENTICATION_RESPONSE, null, new Exception(KZPassiveAuthBroadcastConstants.COULD_NOT_GET_AUTHENTICATION_RESPONSE));
                break;
            case KZPassiveAuthBroadcastConstants.REQUEST_CANCEL_BY_USER_CODE:
                event = new ServiceEvent(target, HttpStatus.SC_NOT_FOUND, KZPassiveAuthBroadcastConstants.CANCEL_BY_USER_MESSAGE, null);
                break;
            case KZPassiveAuthBroadcastConstants.REQUEST_FAILED_CODE:
                String errorDescription = intent.getStringExtra(KZPassiveAuthBroadcastConstants.ERROR_DESCRIPTION);
                event = new ServiceEvent(target, HttpStatus.SC_UNAUTHORIZED, errorDescription, null);
                break;
            case KZPassiveAuthBroadcastConstants.REQUEST_COMPLETE_CODE:
                String authServicePayload = intent.getStringExtra(KZPassiveAuthBroadcastConstants.AUTH_SERVICE_PAYLOAD);
                if(authServicePayload != null && !authServicePayload.isEmpty()) {
                    String token = authServicePayload.replace("access_token", "rawToken");
                    try {
                        IdentityManager im = IdentityManager.getInstance();
                        KidoZenUser user = im.createKidoZenUser(token, KidoZenUserIdentityType.PASSIVE_IDENTITY);
                        if (user!=null) {
                            String userUniqueIdentifier = String.valueOf( passiveAuthType);
                            user.HashKey = userUniqueIdentifier;
                            im.addToTokensCache(userUniqueIdentifier, token, user.RefreshToken, KidoZenUserIdentityType.PASSIVE_IDENTITY);
                            event = new ServiceEvent(target, HttpStatus.SC_OK, token, user);
                        }
                        else {
                            throw new Exception("Cannot create user");
                        }
                    }
                    catch(Exception e) {
                        event = new ServiceEvent(target, HttpStatus.SC_METHOD_FAILURE, e.getMessage(), null, e);
                    }
                }
                else {
                    event = new ServiceEvent(target, HttpStatus.SC_METHOD_FAILURE, KZPassiveAuthBroadcastConstants.COULD_NOT_GET_AUTHENTICATION_RESPONSE, null, new Exception(KZPassiveAuthBroadcastConstants.COULD_NOT_GET_AUTHENTICATION_RESPONSE));
                }
                break;
        }
        if (eventListener!=null) {
            eventListener.onFinish(event);
        }
    }
}
