# Kidozen SDK Push notification helper library
 This library simplifies the process of registration with Google Cloud Messaging and synchronizes the steps needed to make it work with KidoZen (ie, obtain a registration ID by calling the GoogleCloudMessaging API ) 

## Samples
 You can find an example application called "push" in the "samples" folder. 

## How to use it

### Add the library to your project. 

### Configure your AndroidManifest.xml file
 Be sure you include all the permissions required to handle GCM notifications. For more information [check this link](http://developer.android.com/google/gcm/client.html#manifest)
  
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="my.samples.push" >   
        <uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.GET_ACCOUNTS" />
        <uses-permission android:name="android.permission.WAKE_LOCK" />
        <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
    
        <permission android:name="com.example.gcm.permission.C2D_MESSAGE"
            android:protectionLevel="signature" />
        <uses-permission android:name="com.example.gcm.permission.C2D_MESSAGE" />
    
        <application
            ...>
            <meta-data android:name="com.google.android.gms.version"
                android:value="@integer/google_play_services_version" />
            <activity  
                ...>
            <receiver
                android:name=".MyBroadcastReceiver"
                android:permission="com.google.android.c2dm.permission.SEND" >
                <intent-filter>
                    <action android:name="com.google.android.c2dm.intent.RECEIVE" />
                    <category android:name="com.example.gcm" />
                </intent-filter>
            </receiver>
            <service android:name=".GcmIntentService" />
        </application>
    
    </manifest>



### Create a new BroadCastReceiver
 This class will handle the messages received from GCM
 
    public class MyBroadcastReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
         GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
    
         String messageType = gcm.getMessageType(intent);
    
         if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
             // error occurs
         } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
             // the server have deleted some pending messages,
             // because they are collapsible
         } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
             // normal message
             // how the data can be fetched is detailed in the next section
         }
     }
    }


### Write your application and use the following methods

 Initialize: this method initializes and checks the registration process. When finish it fires the onInitializationComplete event. The initialization process includes:
     - checking Google Play Services availability
     - getting a valid registration ID from GCM
     - retrieve or store it in Shared Preferences
     - register with KidoZen services
    
    void Initialize()

 Subscribe: Subscribes the device to receive messages from the specified KidoZen channel. When finish it fires the onSubscriptionComplete event

    public void SubscribeToChannel(String channel) {

 RemoveSubscription: Stops receiving messages from the specified KidoZen channel. When finish it fires the onRemoveSubscriptionComplete event
    
    public void SubscribeToChannel(String channel) {

 PushMessage: Sends a message the specified KidoZen channel. When finish it fires the onPushMessageComplete event

    public void PushMessage(String channel, JSONObject data)
        
 GetSubscriptions:  Returns all the KidoZen channels that the device has subscribed. When finish it fires the onGetSubscriptionsComplete event

    public void GetSubscriptions()
        

### Then you must implement the IGcmEvents interface:

 onInitializationComplete: notifies when the initialization finished

    void onInitializationComplete(Boolean success, String message, String registrationId, String deviceId);

 - success : indicates if the operation was successful (true) or not (false) 
 - message:  a description of the operation 
 - registration id: GCM registration id provided by Android / Google play services
 - deviceId : the device id used
 
 onSubscriptionComplete: notifies when the subscription to a channel finished :

    void onSubscriptionComplete(Boolean success, String message);

 - success : indicates if the operation was successful (true) or not (false) 
 - message:  a description of the operation 

 onPushMessageComplete: This method notifies when the subscription to a channel finished :

    void onPushMessageComplete(Boolean success, String message);

 - success : indicates if the operation was successful (true) or not (false) 
 - message:  a description of the operation 

 onRemoveSubscriptionComplete

    void onRemoveSubscriptionComplete(Boolean success, String message);

 - success : indicates if the operation was successful (true) or not (false) 
 - message:  a description of the operation 

 onGetSubscriptionsComplete

    void onGetSubscriptionsComplete(boolean success, String message);

 - success : indicates if the operation was successful (true) or not (false) 
 - message:  an json array string with the subscription information 


For more information please refer to the [KidoZen documentation](http://docs.kidozen.com/)

