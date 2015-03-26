package kidozen.client;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import kidozen.client.analytics.Analytics;
import kidozen.client.authentication.BaseIdentityProvider;
import kidozen.client.authentication.IdentityManager;
import kidozen.client.authentication.KZPassiveAuthTypes;
import kidozen.client.authentication.KidoZenUser;
import kidozen.client.crash.CrashReporter;
import kidozen.client.datavisualization.DataVisualizationActivity;
import kidozen.client.datavisualization.DataVisualizationActivityConstants;
import kidozen.client.internal.Constants;
import kidozen.client.internal.KidoAppSettings;
import kidozen.client.internal.SyncHelper;
import kidozen.client.internal.Utilities;

//http://stackoverflow.com/questions/18590276/partial-implementation-of-an-interface

/**
 * @author kidozen
 * @version 1.2.0, July 2014
 * 
 * Main KidoZen application object
 *
 */
public class KZApplication {
    public Boolean UserIsAuthenticated = false;
    public Boolean StrictSSL = true;

    private String TAG = KZApplication.class.getSimpleName();
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private String mApplicationKey;
    private String mTenantMarketPlace;
    private String mApplicationName;
    private String mProvider;
    private String mUsername;
    private String mPassword;
    private String mPassiveClientId;
    private String mReportingUrl;

    private KidoZenUser mUserIdentity;
    private KidoZenUser mApplicationIdentity;
    private AnalyticsLog mAnalyticsLog;
    private Logging mApplicationLog;
    private MailSender mMailSender;
    private static CrashReporter mCrashReporter;

    private KidoAppSettings mApplicationConfiguration;
    private Analytics mAnalytics = null;


    private DataVisualizationActivity dataVisualizationActivity;

    public void showDataVisualization(Context context, String visualization) {
        Intent intent = new Intent(context, DataVisualizationActivity.class);
        intent.putExtra(DataVisualizationActivityConstants.APPLICATION_NAME,mApplicationName);
        try {
            intent.putExtra(DataVisualizationActivityConstants.DOMAIN, mApplicationConfiguration.GetSettingAsString("domain"));
            intent.putExtra(DataVisualizationActivityConstants.DATAVIZ_NAME, visualization);
            intent.putExtra(DataVisualizationActivityConstants.STRICT_SSL, !StrictSSL);
            intent.putExtra(DataVisualizationActivityConstants.AUTH_HEADER, mUserIdentity.Token);
            intent.putExtra(DataVisualizationActivityConstants.AUTH_RESPONSE, mUserIdentity.authenticationResponse);

            intent.putExtra(DataVisualizationActivityConstants.TENANT_MARKET_PLACE, mTenantMarketPlace);
            intent.putExtra(DataVisualizationActivityConstants.USERNAME, mUsername);
            intent.putExtra(DataVisualizationActivityConstants.PASSWORD, mPassword);
            intent.putExtra(DataVisualizationActivityConstants.PROVIDER, mProvider);
            context.startActivity(intent);
        } catch (JSONException e) {
            throw new IllegalStateException("Could not initialize Data visualization. Please check the application configuration");
        }
    }

    /**

    /**
     * Enables crash reporter feature in the current application
     *
     * @param application The Android Application instance
     * @throws IllegalStateException
     */
    public void EnableCrashReporter(Application application) throws IllegalStateException {
        if (mApplicationKey == "" || mApplicationKey ==null) {
            throw new IllegalStateException("Crash report feature can only be enabled using an application key.");
        } else {
            // EnableCrashReporter is called only when the ctor has the app key,
            // in this case the application has already initialized and has a valid Application token ( see ctor code )
            // so, we can send the mApplicationIdentity with the
            Log.d("Crash", String.format("Sending crash to application: %s", mReportingUrl));
            mCrashReporter = new CrashReporter(application, mReportingUrl, mApplicationKey);
        }
    }


    public void EnableAnalytics(Context context) throws IllegalStateException, SecurityException {
        //Checks permissions
        String[] permissions = {"android.permission.ACCESS_COARSE_LOCATION",  "android.permission.ACCESS_FINE_LOCATION"};
        checksPermissions(context, permissions);

        if (mAnalyticsLog == null) {
            try {
                mAnalyticsLog = new AnalyticsLog(
                        mApplicationConfiguration.GetSettingAsString("url"),
                        mProvider,
                        mUsername,
                        mPassword,
                        mPassiveClientId,
                        mUserIdentity,
                        mApplicationIdentity);
            } catch (JSONException e) {
                throw new IllegalStateException("Could not initialize Analytics. Please check the application configuration");
            }
            mAnalyticsLog.mUserIdentity = this.mUserIdentity;
            mAnalyticsLog.setStrictSSL(!StrictSSL);
        }
        mAnalytics = Analytics.getInstance(true,context,mAnalyticsLog);
    }

    private void checksPermissions(Context context, String[] permissions) {
        Boolean hasPermission = false;
        for(String permission:permissions) {
            int res = context.checkCallingOrSelfPermission(permission);
            hasPermission = (PackageManager.PERMISSION_GRANTED==res);
        }
        if (!hasPermission) throw new SecurityException(
                String.format("You must declare one of the following permissions in your manifest.xml: %s", permissions)
        );
    }

    public void TagClick(String buttonName) {
        mAnalytics.TagClick(buttonName);
    }

    public void TagActivity(String activityName) {
        mAnalytics.TagActivity(activityName);
    }

    public void TagCustom(String title, JSONObject data) {
        mAnalytics.TagEvent(title,data);
    }

    public void SetAnalyticsSessionTimeOutInSeconds(int timeout) {
        mAnalytics.SetSessionTimeOutInSeconds(timeout);
    }
    /**
     * Add breadcrumbs. A breadcrumb is a developer-defined text string that allows developers to capture app run-time information
     *
     * @param value The breadcrumb value
     */
    public void AddBreadCrumb(String value) {
        mCrashReporter.AddBreadCrumb(value);
    }

    /**
     * Returns the current KidoZen identity
     *
     * @return The current KidoZen User
     */
    public KidoZenUser GetKidoZenUser() {
        return mUserIdentity;
    }

    private KZApplication() {
    }

    /**
     * Allows to change the current KidoZen user identity
     *
     * @param user the new identity
     */
    public void SetKidoZenUser(KidoZenUser user) {
        this.mUserIdentity = user;
    }

    /**
     * Constructor
     *
     * @param tenantMarketPlace The url of the KidoZen marketplace
     * @param application       The application name
     * @param applicationKey    The application key , you can get it from the marketplace
     * @throws IllegalStateException
     */
    public KZApplication(String tenantMarketPlace, String application, String applicationKey) throws IllegalStateException {
        this(tenantMarketPlace, application, applicationKey, false);
    }

    /**
     * Constructor
     *
     * @param tenantMarketPlace The url of the KidoZen marketplace
     * @param application       The application name
     * @param strictSSL         Set this value to false to bypass the SSL validation, use it only for development purposes. Otherwise you need to install the KidoZen Certificates in your device
     * @param applicationKey    The application key , you can get it from the marketplace
     * @throws IllegalStateException
     */
    public KZApplication(String tenantMarketPlace, String application, String applicationKey, Boolean strictSSL) throws IllegalStateException {
        this.StrictSSL = !strictSSL;
        this.mApplicationKey = applicationKey;
        mTenantMarketPlace = tenantMarketPlace;
        mApplicationName = application;

        if (mApplicationKey.equals("") || mApplicationKey == null) {
            throw new IllegalArgumentException("Application key is required.");
        }

        if (!mTenantMarketPlace.endsWith("/")) mTenantMarketPlace = mTenantMarketPlace + "/";

        try {
            mApplicationConfiguration = new KidoAppSettings();
        } catch (Exception e) {
            throw new IllegalStateException("Initialization failure", e);
        }
    }

    /**
     * Creates a new PubSub channel
     *
     * @return A new PubSub object instance
     * @throws Exception
     */
    public PubSubChannel PubSubChannel(String name) throws Exception {
        checkMethodParameters(name);
        PubSubChannel channel = new PubSubChannel(
                mApplicationConfiguration.GetSettingAsString("pubsub"),
                mApplicationConfiguration.GetSettingAsString("ws"),
                name,
                mProvider,
                mUsername,
                mPassword,
                mPassiveClientId,
                mUserIdentity,
                mApplicationIdentity);
        channel.setApplicationName(this.getApplicationName());
        channel.mUserIdentity = this.mUserIdentity;
        channel.setStrictSSL(!StrictSSL);

        return channel;
    }

    /**
     * Creates a new Push notification service
     *
     * @return A new object instance that allows to interact with the Google Cloud Messaging Services (GCM)
     * @throws Exception
     */
    public Notification Notification() throws Exception {
        Notification notification = new Notification(mApplicationConfiguration.GetSettingAsString("notification"),
                getApplicationName(),
                mProvider,
                mUsername,
                mPassword,
                mPassiveClientId,
                mUserIdentity,
                mApplicationIdentity);

        notification.mUserIdentity = this.mUserIdentity;
        notification.setStrictSSL(!StrictSSL);
        return notification;
    }

    /**
     * Creates a new Configuration object
     *
     * @param name The name that references the Configuration instance
     * @return A new Configuration object instance
     * @throws Exception
     */
    public Configuration Configuration(String name) throws Exception {
        checkMethodParameters(name);
        Configuration configuration = new Configuration(mApplicationConfiguration.GetSettingAsString("config"),
                name,
                mProvider,
                mUsername,
                mPassword,
                mPassiveClientId,
                mUserIdentity,
                mApplicationIdentity);

        configuration.mUserIdentity = this.mUserIdentity;
        configuration.setStrictSSL(!StrictSSL);
        return configuration;
    }

    /**
     * Creates a new Queue object
     *
     * @param name The name that references the Queue instance
     * @return a new Queue object instance
     * @throws Exception
     */
    public Queue Queue(String name) throws Exception {
        checkMethodParameters(name);
        Queue queue = new Queue(mApplicationConfiguration.GetSettingAsString("queue"),
                name,
                mProvider,
                mUsername,
                mPassword,
                mPassiveClientId,
                mUserIdentity,
                mApplicationIdentity);
        queue.mUserIdentity = this.mUserIdentity;
        queue.setStrictSSL(!StrictSSL);
        return queue;
    }

    /**
     * Creates a new Storage object
     *
     * @param name The name that references the Storage instance
     * @return a new Storage object instance
     * @throws Exception
     */
    public Storage Storage(String name) throws Exception {
        checkMethodParameters(name);
        Storage storage = new Storage(mApplicationConfiguration.GetSettingAsString("storage"),
                name,
                mProvider,
                mUsername,
                mPassword,
                mPassiveClientId,
                mUserIdentity,
                mApplicationIdentity);
        storage.setStrictSSL(StrictSSL);
        return storage;
    }

    /**
     * Creates a new SMSSender object
     *
     * @param number The phone number to send messages.
     * @return a new SMSSender object instance
     * @throws Exception
     */
    public SMSSender SMSSender(String number) throws Exception {
        checkMethodParameters(number);
        SMSSender sender = new SMSSender(mApplicationConfiguration.GetSettingAsString("sms"),
                number,
                mProvider,
                mUsername,
                mPassword,
                mPassiveClientId,
                mUserIdentity,
                mApplicationIdentity);

        sender.mUserIdentity = this.mUserIdentity;
        sender.setStrictSSL(!StrictSSL);
        return sender;
    }

    /**
     * Sends an EMail
     *
     * @param mail     The mail object with the information needed to send an email
     * @param callback The callback with the result of the service call
     * @throws Exception
     */
    public void SendEmail(Mail mail, ServiceEventListener callback) throws Exception {
        if (mail == null) {
            throw new Exception("Mail message must not be null");
        }
        if (mMailSender == null) {
            mMailSender = new MailSender(mApplicationConfiguration.GetSettingAsString("email"),
                    mProvider,
                    mUsername,
                    mPassword,
                    mPassiveClientId,
                    mUserIdentity,
                    mApplicationIdentity);
            mMailSender.mUserIdentity = this.mUserIdentity;
            mMailSender.setStrictSSL(!StrictSSL);
        }

        mMailSender.Send(mail, callback);
    }

    public void SendEmail(Mail mail) throws TimeoutException, SynchronousException {
        if (mMailSender == null) {
            try {
                mMailSender = new MailSender(mApplicationConfiguration.GetSettingAsString("email"),
                        mProvider,
                        mUsername,
                        mPassword,
                        mPassiveClientId,
                        mUserIdentity,
                        mApplicationIdentity);
            } catch (JSONException e) {
                throw new IllegalArgumentException("Could not get email endpoint");
            }
            mMailSender.mUserIdentity = this.mUserIdentity;
            mMailSender.setStrictSSL(!StrictSSL);
        }

        SyncHelper<String> helper = new SyncHelper(mMailSender, "Send", Mail.class, ServiceEventListener.class);
        helper.Invoke(new Object[]{ mail });
        if (helper.getStatusCode() != HttpStatus.SC_CREATED) throw new SynchronousException(helper.getError().toString());
    }

    /**
     * Writes an string entry in the Logging service
     *
     * @param message  the message to write
     * @param data     the raw data to write
     * @param level    LogLevelVerbose, LogLevelInfo, LogLevelWarning, LogLevelError, LogLevelCritical
     * @param callback The callback with the result of the service call
     * @throws Exception
     */
    public void WriteLog(String message, String data, LogLevel level, ServiceEventListener callback) throws Exception {
        if (level == null) {
            throw new Exception("Level must not be null");
        }
        checkApplicationLog();
        mApplicationLog.Write(message, data, level, callback);
    }

    public void WriteLog(String message, String data, LogLevel level) throws TimeoutException, SynchronousException {
        try { checkApplicationLog();}
        catch (Exception e) { throw new SynchronousException(e.getMessage());}

        mApplicationLog.Write(message,data,level);
    }


    /**
     * Writes an integer entry in the Logging service
     *
     * @param message  the message to write
     * @param data     the raw data to write
     * @param level    LogLevelVerbose, LogLevelInfo, LogLevelWarning, LogLevelError, LogLevelCritical
     * @param callback The callback with the result of the service call
     * @throws Exception
     */
    public void WriteLog(String message, Integer data, LogLevel level, ServiceEventListener callback) throws Exception {
        if (level == null) {
            throw new Exception("Level must not be null");
        }
        checkApplicationLog();
        mApplicationLog.Write(message, data, level, callback);
    }

    public void WriteLog(String message, Integer data, LogLevel level) throws TimeoutException, SynchronousException {
        try { checkApplicationLog();}
        catch (Exception e) { throw new SynchronousException(e.getMessage());}

        mApplicationLog.Write(message,data,level);
    }


    /**
     * Executes a Query against the Logging service
     *
     * @param query
     * @param callback
     * @throws Exception
     */
    public void QueryLog(String query, ServiceEventListener callback) throws Exception {
        if (query == null) {
            throw new Exception("query paramter must not be null");
        }
        checkApplicationLog();
        mApplicationLog.Query(query, callback);
    }

    public JSONArray QueryLog(String query) throws TimeoutException, SynchronousException {
        try { checkApplicationLog();}
        catch (Exception e) { throw new SynchronousException(e.getMessage());}

        return mApplicationLog.Query(query);
    }

    /**
     * Writes an ArrayList entry in the Logging service
     *
     * @param message  the message to write
     * @param data     the raw data to write
     * @param level    LogLevelVerbose, LogLevelInfo, LogLevelWarning, LogLevelError, LogLevelCritical
     * @param callback The callback with the result of the service call
     * @throws Exception
     */
    public void WriteLog(String message, ArrayList data, LogLevel level, ServiceEventListener callback) throws Exception {
        if (level == null) {
            throw new Exception("Level must not be null");
        }
        checkApplicationLog();
        mApplicationLog.Write(message, data, level, callback);
    }

    public void WriteLog(String message, ArrayList data, LogLevel level) throws TimeoutException, SynchronousException {
        try { checkApplicationLog();}
        catch (Exception e) { throw new SynchronousException(e.getMessage());}

        mApplicationLog.Write(message,data,level);
    }

    /**
     * Writes an string entry in the Logging service
     *
     * @param message  the message to write
     * @param data     the raw data to write
     * @param level    LogLevelVerbose, LogLevelInfo, LogLevelWarning, LogLevelError, LogLevelCritical
     * @param callback The callback with the result of the service call
     * @throws Exception
     */
    public void WriteLog(String message, JSONObject data, LogLevel level, ServiceEventListener callback) throws Exception {
        if (level == null) {
            throw new Exception("Level must not be null");
        }
        checkApplicationLog();
        mApplicationLog.Write(message, data, level, callback);
    }

    public void WriteLog(String message, JSONObject data, LogLevel level) throws TimeoutException, SynchronousException {
        try { checkApplicationLog();}
        catch (Exception e) { throw new SynchronousException(e.getMessage());}

        mApplicationLog.Write(message,data,level);
    }


    /**
     * Writes an Map entry in the Logging service
     *
     * @param message  the message to write
     * @param data     the raw data to write
     * @param level    LogLevelVerbose, LogLevelInfo, LogLevelWarning, LogLevelError, LogLevelCritical
     * @param callback The callback with the result of the service call
     * @throws Exception
     */
    public void WriteLog(String message, Map data, LogLevel level, ServiceEventListener callback) throws Exception {
        if (level == null) {
            throw new Exception("Level must not be null");
        }
        checkApplicationLog();
        mApplicationLog.Write(message, data, level, callback);
    }

    public void WriteLog(String message, Map data, LogLevel level) throws TimeoutException, SynchronousException {
        try { checkApplicationLog();}
        catch (Exception e) { throw new SynchronousException(e.getMessage());}

        mApplicationLog.Write(message,data,level);
    }

    private boolean hasErrorsAfterCallingInitialization = false;
    private void checkApplicationLog() throws Exception {
        //Logging can be use without auth, so we must be sure the application has been initialized and authenticated with a valid Key
        if (mUserIdentity==null && mApplicationIdentity ==null) {
            System.out.println("mUser or mApp is null ... initializing");
            final CountDownLatch cdl = new CountDownLatch(1);
            this.Initialize(new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    cdl.countDown();
                    hasErrorsAfterCallingInitialization = (e.StatusCode!=HttpStatus.SC_OK);
                }
            });
        }
        if (hasErrorsAfterCallingInitialization) throw new Exception("could not initialize Logging");
        if (mApplicationLog == null) {
            mApplicationLog = new Logging(
                    mApplicationConfiguration.GetSettingAsString("logging-v3"),
                    mProvider,
                    mUsername,
                    mPassword,
                    mPassiveClientId,
                    mUserIdentity,
                    mApplicationIdentity);
            mApplicationLog.mUserIdentity = this.mUserIdentity;
            mApplicationLog.setStrictSSL(!StrictSSL);
        }
    }

    /**
     * Clears the KZApplication log
     *
     * @param callback The callback with the result of the service call
     */
    public void ClearLog(ServiceEventListener callback) throws Exception {
        checkApplicationLog();
        mApplicationLog.Clear(callback);
    }

    public boolean ClearLog() throws SynchronousException, TimeoutException {
        try { checkApplicationLog();}
        catch (Exception e) { throw new SynchronousException(e.getMessage());}

        return  mApplicationLog.Clear();
    }

    /**
     * Returns all the messages from the Application log
     *
     * @param callback The callback with the result of the service call
     */
    public void AllLogMessages(ServiceEventListener callback) throws Exception {
        checkApplicationLog();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(AUTHORIZATION_HEADER, this.mUserIdentity.Token);
        mApplicationLog.All(callback);
    }

    public JSONArray AllLogMessages() throws TimeoutException, SynchronousException {
        try { checkApplicationLog();}
        catch (Exception e) { throw new SynchronousException(e.getMessage());}

        return mApplicationLog.All();
    }

    /**
     * Creates a new File Storage object
     *
     * @return a new File Storage object instance
     * @throws Exception
     */
    public Files FileStorage() throws Exception {
        Files files = new Files(mApplicationConfiguration.GetSettingAsString("files"),
                mProvider,
                mUsername,
                mPassword,
                mPassiveClientId,
                mUserIdentity,
                mApplicationIdentity);
        files.mUserIdentity = this.mUserIdentity;
        files.setStrictSSL(!StrictSSL);
        return files;
    }

    private void checkMethodParameters(String name) throws IllegalArgumentException {
        if ( name == null || name.isEmpty() ) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
    }


    /**
     * Creates a new LOBService object
     *
     * @param name The name that references the LOBService instance
     * @return a new LOBService object instance
     * @throws Exception
     */
    public Service LOBService(String name) throws Exception {
        checkMethodParameters(name);
        Service service = new Service(mApplicationConfiguration.GetSettingAsString("url"), name,
                mProvider,
                mUsername,
                mPassword,
                mPassiveClientId,
                mUserIdentity,
                mApplicationIdentity);

        service.mUserIdentity = this.mUserIdentity;
        service.setStrictSSL(!StrictSSL);
        return service;
    }

    /**
     * Creates a new DataSource object
     *
     * @param name The name that references the DataSource instance
     * @return a new DataSource object instance
     * @throws Exception
     */
    public DataSource DataSource(String name) throws Exception {
        checkMethodParameters(name);
        DataSource service = new DataSource(mApplicationConfiguration.GetSettingAsString("datasource"),
                name,
                mProvider,
                mUsername,
                mPassword,
                mPassiveClientId,
                mUserIdentity,
                mApplicationIdentity);
        service.mUserIdentity = this.mUserIdentity;
        service.setStrictSSL(!StrictSSL);
        return service;
    }

    //Authentication

    /**
     *
     * @param callback
     * @throws InitializationException
     */
    public void Initialize(final ServiceEventListener callback) throws InitializationException {
        String url = mTenantMarketPlace + Constants.PUBLICAPI_PATH + getApplicationName();
        try {
            if (!mApplicationConfiguration.IsValid) throw new InitializationException("Invalid application settings. Please check the application name.");
            // chains the getSettings callback with the keyAuth callback
            mApplicationConfiguration.Setup(new InitializationWithKeyCallback(callback), this.StrictSSL);
            mApplicationConfiguration.execute(url).get();
            mReportingUrl = mApplicationConfiguration.GetSettingAsString("url");
            mPassiveClientId = mApplicationConfiguration.GetSettingAsString("name");
        }
        catch (InitializationException ie) {
            throw ie;
        } catch (InterruptedException e) {
            throw new InitializationException("Could not get application configuration",e);
        } catch (ExecutionException e) {
            throw new InitializationException("Could not get application configuration",e);
        } catch (JSONException e) {
            throw new InitializationException("Could not get a required application configuration setting",e);
        } catch (Exception e) {
            throw new InitializationException(e.getMessage(),e);
        }
    }


    private String getUserUniqueIdentifier() {
        String userUniqueIdentifier = "";
        if (mUserIdentity!=null)
            userUniqueIdentifier = mUserIdentity.Claims.get("http://schemas.kidozen.com/userid").toString();
        return userUniqueIdentifier;
    }


    /**
     * Sign outs the current user
     */
    public void SignOut() {
        if (mUserIdentity != null) {
            IdentityManager.getInstance().SignOut(mUserIdentity.HashKey);
        }
        if (mApplicationIdentity != null) {
            IdentityManager.getInstance().SignOut(mApplicationIdentity.HashKey);
        }
        UserIsAuthenticated = false;
    }

    /**
     * Authenticates a user using the specified provider
     *
     * @param username    The user account
     * @param password    The password for the user
     * @param providerKey The key that identifies the Provider
     * @param callback    The callback with the result of the service call
     * @throws InitializationException
     */
    public void Authenticate(final String username, final String password, final String providerKey, final ServiceEventListener callback)  throws InitializationException {
        if (!mApplicationConfiguration.IsInitialized)
            this.Initialize(new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    InvokeActiveAuthentication(providerKey, username, password, callback);
                }
            });
        else
            InvokeActiveAuthentication(providerKey, username, password, callback);
    }

    public void Authenticate(final String providerKey, final String username, final String password, final ServiceResponseHandler callback) throws InitializationException {
        this.Authenticate(providerKey,username, password, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                Utilities.DispatchServiceResponseListener(e,callback);
            }
        });
    }

    private void InvokeActiveAuthentication(final String providerKey, final String username, final String password, final ServiceEventListener callback) {
        try {
            JSONObject authConfig = mApplicationConfiguration.GetSettingAsJObject("authConfig");
            authConfig.put("domain", mApplicationConfiguration.GetSettingAsString("domain"));
            IdentityManager.getInstance().Setup(authConfig, StrictSSL, mApplicationKey);

            IdentityManager.getInstance().Setup(authConfig, StrictSSL, mApplicationKey);
            IdentityManager.getInstance().Authenticate(providerKey, username, password, new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    if (e.StatusCode < HttpStatus.SC_BAD_REQUEST) {
                        mProvider = providerKey;
                        mUsername = username;
                        mPassword = password;
                        UserIsAuthenticated = true;
                        SetKidoZenUser((KidoZenUser) e.Response);
                        if (mUserIdentity.HasExpired()) {
                            Log.w(Constants.LOG_CAT_TAG, "There is a mismatch between your device date and the KidoZen authentication service.\nThe expiration time from the service is lower than the device date.\nThe OnSessionExpirationRun method will be ignored");
                        }
                    }
                    if (callback != null) callback.onFinish(e);
                }
            });
        } catch (Exception e) {
            callback.onFinish(new ServiceEvent(this, HttpStatus.SC_BAD_REQUEST, e.getMessage(), e));
        }
    }

    /**
     * Calls Good authentication.
     *
     * @param context Android context instance
     * @param callback The callback with the result of the service call
     * @throws InitializationException
     */
    public void AuthenticateGood(final Context context, final ServiceEventListener callback) throws InitializationException {
        if (!mApplicationConfiguration.IsInitialized)
            this.Initialize(new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    InvokeGoodAuthentication(context, callback);
                }
            });
        else
            InvokeGoodAuthentication(context, callback);
    }

    private void InvokeGoodAuthentication(Context context, final ServiceEventListener callback) {
        try {
            JSONObject authConfig = mApplicationConfiguration.GetSettingAsJObject("authConfig");
            authConfig.put("domain", mApplicationConfiguration.GetSettingAsString("domain"));
            IdentityManager.getInstance().Setup(authConfig, StrictSSL, mApplicationKey);
            IdentityManager.getInstance().AuthenticateGood(context, new ServiceEventListener()   {
                @Override
                public void onFinish(ServiceEvent e) {
                    SetKidoZenUser((KidoZenUser) e.Response);
                    if (callback != null) callback.onFinish(e);
                }
            });
        }
        catch(Exception e)
        {
            if (callback!=null)
                callback.onFinish(new ServiceEvent(this, HttpStatus.SC_BAD_REQUEST,e.getMessage(), e));
        }
    }

    /**
     * Enables passive Authentication
     *
     * @param context Android context instance
     * @param callback The callback with the result of the service call
     * @throws InitializationException
     */
    public void Authenticate(final Context context, final ServiceEventListener callback) throws InitializationException {
        if (!mApplicationConfiguration.IsInitialized)
            this.Initialize(new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    InvokePassiveAuthentication(context,true, callback, KZPassiveAuthTypes.PASSIVE_AUTHENTICATION_USERID);
                }
            });
        else
            InvokePassiveAuthentication(context,true, callback, KZPassiveAuthTypes.PASSIVE_AUTHENTICATION_USERID);
    }

    /**
     * Enables passive Authentication
     *
     * @param context Android context instance
     * @param forceLogin cleans previous session and forces to display the username and password login screen
     * @param callback The callback with the result of the service call
     * @throws InitializationException
     */
    public void Authenticate(final Context context, final Boolean forceLogin, final ServiceEventListener callback) throws InitializationException {
        if (!mApplicationConfiguration.IsInitialized)
            this.Initialize(new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    InvokePassiveAuthentication(context, forceLogin, callback, KZPassiveAuthTypes.PASSIVE_AUTHENTICATION_USERID);
                }
            });
        else
            InvokePassiveAuthentication(context,forceLogin, callback, KZPassiveAuthTypes.PASSIVE_AUTHENTICATION_USERID);
    }

    private void InvokePassiveAuthentication(Context context, Boolean cleanCookies, final ServiceEventListener callback, KZPassiveAuthTypes passiveAuthType) {
        try {
            JSONObject authConfig = mApplicationConfiguration.GetSettingAsJObject("authConfig");
            authConfig.put("domain", mApplicationConfiguration.GetSettingAsString("domain"));
            IdentityManager.getInstance().Setup(authConfig, StrictSSL, mApplicationKey);
            IdentityManager.getInstance().Authenticate(context, cleanCookies, passiveAuthType, new ServiceEventListener()   {
                @Override
                public void onFinish(ServiceEvent e) {
                    SetKidoZenUser((KidoZenUser) e.Response);
                    if (callback != null) callback.onFinish(e);
                }
            });
        }
        catch(Exception e)
        {
            if (callback!=null)
                callback.onFinish(new ServiceEvent(this, HttpStatus.SC_BAD_REQUEST,e.getMessage(), e));
        }
    }

    public void Authenticate(Context context, final ServiceResponseHandler callback) throws InitializationException{
        this.Authenticate(context, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                Utilities.DispatchServiceResponseListener(e,callback);
            }
        });
    }

    /*
    * Custom Authentication
    * */
    public void Authenticate(final BaseIdentityProvider ip, final ServiceResponseHandler callback) throws InitializationException {
        this.Authenticate(ip, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                Utilities.DispatchServiceResponseListener(e,callback);
            }
        });
    }

    public void Authenticate(final BaseIdentityProvider ip,final ServiceEventListener callback) throws InitializationException {
        if (!mApplicationConfiguration.IsInitialized)
            this.Initialize(new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    InvokeCustomAuthentication(ip, callback);
                }
            });
        else
            InvokeCustomAuthentication(ip, callback);
    }

    private void InvokeCustomAuthentication(final BaseIdentityProvider ip, final ServiceEventListener callback) {
        try {
            IdentityManager.getInstance().Setup(StrictSSL, mApplicationKey);
            IdentityManager.getInstance().Authenticate(ip, new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    if (e.StatusCode < HttpStatus.SC_BAD_REQUEST) {
                        UserIsAuthenticated = true;
                        SetKidoZenUser((KidoZenUser) e.Response);
                        if (mUserIdentity.HasExpired()) {
                            Log.w(Constants.LOG_CAT_TAG, "There is a mismatch between your device date and the KidoZen authentication service.\nThe expiration time from the service is lower than the device date.\nThe OnSessionExpirationRun method will be ignored");
                        }
                    }
                    if (callback != null) callback.onFinish(e);
                }
            });
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            callback.onFinish(new ServiceEvent(this, HttpStatus.SC_BAD_REQUEST, e.getMessage(), e));
        }
    }

    /**
     * Enables G+ OAuth Authentication
     *
     * @param context Android context instance
     * @param callback The callback with the result of the service call
     * @throws InitializationException
     */
    public void AuthenticateWithGPlus(final Context context, final ServiceEventListener callback) throws InitializationException {
        if (!mApplicationConfiguration.IsInitialized)
            this.Initialize(new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    InvokePassiveAuthentication(context,true, callback, KZPassiveAuthTypes.GPLUS_AUTHENTICATION_USERID);
                }
            });
        else
            InvokePassiveAuthentication(context,true, callback, KZPassiveAuthTypes.GPLUS_AUTHENTICATION_USERID);
    }
    /**
     * Enables passive Authentication
     *
     * @param context Android context instance
     * @param forceLogin cleans previous session and forces to display the username and password login screen
     * @param callback The callback with the result of the service call
     * @throws InitializationException
     */
    public void AuthenticateWithGPlus(final Context context, final Boolean forceLogin, final ServiceEventListener callback) throws InitializationException {
        if (!mApplicationConfiguration.IsInitialized)
            this.Initialize(new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    InvokePassiveAuthentication(context, forceLogin, callback, KZPassiveAuthTypes.GPLUS_AUTHENTICATION_USERID);
                }
            });
        else
            InvokePassiveAuthentication(context,forceLogin, callback, KZPassiveAuthTypes.GPLUS_AUTHENTICATION_USERID);
    }

    public void AuthenticateWithGPlus(Context context, final ServiceResponseHandler callback) throws InitializationException{
        this.AuthenticateWithGPlus(context, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                Utilities.DispatchServiceResponseListener(e,callback);
            }
        });
    }

    public void SignOutFromGPlus(Context context) throws InitializationException {
        IdentityManager.getInstance().SignOut(context, KZPassiveAuthTypes.GPLUS_AUTHENTICATION_USERID);
    }

    public void RevokeAccessFromGPlus(Context context) throws InitializationException {
        IdentityManager.getInstance().RevokeAccessFromGPlus(context);
    }


    /**
     * Returns the current application name
     * @return
     */
    public String getApplicationName() {
        return mApplicationName;
    }

    public void FinishAnalyticsSession() {
        mAnalytics.StopSession();
    }

    public void StartAnalyticsSession() {
        mAnalytics.StartSession();
    }

    private class InitializationWithKeyCallback implements ServiceEventListener {
        ServiceEventListener mServiceEventListenerCallback;
        public InitializationWithKeyCallback(ServiceEventListener cb) {
            mServiceEventListenerCallback = cb;
        }
        @Override
        public void onFinish(ServiceEvent e) {
            JSONObject authConfig = null;
            try {
                authConfig = mApplicationConfiguration.GetSettingAsJObject("authConfig");
                authConfig.put("domain", mApplicationConfiguration.GetSettingAsString("domain"));

                IdentityManager.getInstance().Setup(authConfig, StrictSSL, mApplicationKey);
                IdentityManager.getInstance().Authenticate(mApplicationKey, new ServiceEventListener() {
                    @Override
                    public void onFinish(ServiceEvent e) {
                        if (e.StatusCode== HttpStatus.SC_OK) {
                            mApplicationIdentity = (KidoZenUser) e.Response;
                        }
                        if (mServiceEventListenerCallback !=null) mServiceEventListenerCallback.onFinish(e);
                    }
                });
            } catch (JSONException e1) {
                e1.printStackTrace(); //TODO: <<=== y ese printstacktrace ???!!!
            }
        }
    }
}

