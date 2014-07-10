package com.kidozen.client.push;

/**
 * Created by christian on 7/8/14.
 */
public interface IGcmEvents {
    void InitializationComplete(Boolean success, String message, String registrationId, String deviceId);
    void SubscriptionComplete(Boolean success, String message);
    void SendMessageComplete(Boolean success, String message);
    void RemoveSubscriptionComplete(Boolean success, String message);
}
