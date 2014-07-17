
# Kidozen SDK Push notifications Sample
This folder contains code sample about how to use Push notification features.

## Setup GCM (Google Cloud Messaging) with KidoZen
- [Follow this steps](http://docs.kidozen.com/android-push-notifications/) to configure KidoZen Android Push notifications


## How to run the example

### Update the sample code
Navigate to folder 'src/main/java' and edit the file 'KidoZenHelper.java'. Update the following lines with the righ values

    private String tenantMarketPlace = "http://contoso.kidocloud.com";
    private String application = "myApplication";
    private String appkey = "get this value from your marketplace";

    private String user              = "myaccount@kidozen.com";
    private String passw             = "secret";
    private String provider          = "Kidozen";

    private String projectid          = "00000000000";


### Launch the application
 1. Press 'Sign In': This will authenticate the user
 2. Press 'Initialize': The SDK tries to register the device in GCM and KidoZen
 3. Press 'Subscribe': This will subscribe the application to receive message from the specified channel
 4. Press 'Push': This will send a message using the selected channel
 5. Press 'UnSubscribe': Stop receiving push notifications from channel



For more information please refer to the [KidoZen documentation](http://docs.kidozen.com/)
