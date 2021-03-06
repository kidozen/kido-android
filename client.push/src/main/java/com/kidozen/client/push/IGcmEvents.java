package com.kidozen.client.push;

import org.json.JSONArray;

/**
 * Created by christian on 7/8/14.
 */
public interface IGcmEvents {
    void onInitializationComplete(Boolean success, String message, String registrationId, String deviceId);
    void onSubscriptionComplete(Boolean success, String message);
    void onPushMessageComplete(Boolean success, String message);
    void onRemoveSubscriptionComplete(Boolean success, String message);
    void onGetSubscriptionsComplete(boolean success, String message);
}
