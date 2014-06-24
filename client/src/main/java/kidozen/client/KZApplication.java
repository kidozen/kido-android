package kidozen.client;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.HashMap;

import kidozen.client.authentication.IdentityManager;
import kidozen.client.authentication.KidoZenUser;
import kidozen.client.crash.CrashReporter;
import kidozen.client.internal.Constants;
import kidozen.client.internal.KidoAppSettings;

/**
 * @author kidozen
 * @version 1.2.0, April 2014
 * 
 * Main KidoZen application object
 *
 */
public class KZApplication  {
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
    private String mReportingUrl;
    private Boolean mIsAuthenticatedWithAppKey = false;

    private KidoZenUser mUserIdentity;
    private KidoZenUser mApplicationIdentity;
    private KidoZenUser mPassiveUserIdentity;
    private static JSONArray mAllApplicationLogEvents;
    private kidozen.client.Logging mApplicationLog;
    private kidozen.client.MailSender mMailSender;
    private static CrashReporter mCrashReporter;

    private KidoAppSettings mApplicationConfiguration;

    public void EnableCrashReporter (Application application) throws IllegalStateException {
        if (mApplicationKey == Constants.UNSET_APPLICATION_KEY) {
            throw new IllegalStateException("Crash report feature can only be enabled using an application key.");
        }
        else {
            // EnableCrashReporter is called only when the ctor has the app key,
            // in this case the application has already initialized and has a valid Application token ( see ctor code )
            // so, we can send the mApplicationIdentity with the
            Log.d("Crash", String.format("Sending crash to application: %s", mReportingUrl) );
            mCrashReporter = new CrashReporter(application,mReportingUrl, mApplicationKey);
        }
    }

    public void AddBreadCrumb(String value) {
        mCrashReporter.AddBreadCrumb(value);
    }


    //
    /**
	 * Returns the current KidoZen identity
	 * 
	 * @return The current KidoZen User
	 */
	public KidoZenUser GetKidoZenUser() {
		return mUserIdentity;
	}
	private KZApplication() {}
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
     * @param application The application name
     * @param applicationKey The application key for anonymous logging
     * @param callback The ServiceEventListener callback with the operation results
     * @throws Exception The latest exception is there was any
     */
    public KZApplication(String tenantMarketPlace, String application, String applicationKey, ServiceEventListener callback) throws Exception{
        this(tenantMarketPlace,application, applicationKey, false, callback);
    }

    /**
     * Constructor
     *
     * @param tenantMarketPlace The url of the KidoZen marketplace
     * @param application The application name
     * @param strictSSL Set this value to false to bypass the SSL validation, use it only for development purposes. Otherwise you need to install the KidoZen Certificates in your device
     * @param secretKeyName Secret Key's name to use
     * @param callback The ServiceEventListener callback with the operation results
     * @throws Exception The latest exception is there was any
     */
    public KZApplication(String tenantMarketPlace, String application, String applicationKey, Boolean strictSSL, final ServiceEventListener callback) throws IllegalStateException {
        this.StrictSSL = !strictSSL;
        this.mApplicationKey = applicationKey;
        mTenantMarketPlace = tenantMarketPlace;
        mApplicationName = application;
        if (applicationKey.equals( Constants.UNSET_APPLICATION_KEY) ) {
            throw  new IllegalArgumentException("Application key is required.");
        }

        if (!mTenantMarketPlace.endsWith("/")) mTenantMarketPlace = mTenantMarketPlace + "/";

        try {
            String url = mTenantMarketPlace + Constants.PUBLICAPI_PATH + mApplicationName;
            mApplicationConfiguration = KidoAppSettings.getInstance();
            // chains the getSettings callback with the keyAuth callback
            mApplicationConfiguration.Setup(new InitializationWithKeyCallback(callback), this.StrictSSL);

            mApplicationConfiguration.execute(url).get();
            mReportingUrl = mApplicationConfiguration.GetSettingAsString("url");
        }
        catch (Exception e)
        {
            throw  new IllegalStateException("Initialization failure",e);
        }
    }


	public PubSubChannel PubSubChannel(String name) throws Exception{
		checkMethodParameters(name);
		PubSubChannel channel = new PubSubChannel(
                mApplicationConfiguration.GetSettingAsString("pubsub"),
                mApplicationConfiguration.GetSettingAsString("ws"),
                name,
                mProvider,
                mUsername,
                mPassword,
                mUserIdentity,
                mApplicationIdentity);

        channel.mUserIdentity = this.mUserIdentity;
        channel.StrictSSL = !StrictSSL;

        return channel;
	}

	/**
	 * Push notification service main entry point
	 * 
	 * @return The Push notification object that allows to interact with the Google Cloud Messaging Services (GCM)
	 * @throws Exception
	 */
	public Notification Notification () throws Exception
	{
		Notification notification= new Notification( mApplicationConfiguration.GetSettingAsString("notification"),
                mApplicationName,
                mProvider,
                mUsername,
                mPassword,
                mUserIdentity,
                mApplicationIdentity);

        notification.mUserIdentity = this.mUserIdentity;
		notification.StrictSSL = !StrictSSL;
		return notification;
	}

	/**
	 * Creates a new Configuration object
	 * 
	 * @param name The name that references the Configuration instance
	 * @return a new Configuration object
	 * @throws Exception
	 */
	public Configuration Configuration(String name) throws Exception{
		checkMethodParameters(name);
		Configuration configuration =new  Configuration(mApplicationConfiguration.GetSettingAsString("configuration"),
                name,
                mProvider,
                mUsername,
                mPassword,
                mUserIdentity,
                mApplicationIdentity);

        configuration.mUserIdentity = this.mUserIdentity;
		configuration.StrictSSL = !StrictSSL;
		return configuration;
	}

	/**
	 * Creates a new Queue object
	 * 
	 * @param name The name that references the Queue instance
	 * @return a new Queue object
	 * @throws Exception
	 */
	public Queue Queue (String name) throws Exception
	{
		checkMethodParameters(name);
		Queue queue= new Queue(mApplicationConfiguration.GetSettingAsString("queue"),
                name,
                mProvider,
                mUsername,
                mPassword,
                mUserIdentity,
                mApplicationIdentity);
		queue.mUserIdentity = this.mUserIdentity;
		queue.StrictSSL = !StrictSSL;
		return queue;
	}

	/**
	 * Creates a new Storage object
	 * 
	 * @param name The name that references the Storage instance
	 * @return a new Storage object
	 * @throws Exception
	 */
	public Storage Storage(String name) throws Exception {
		checkMethodParameters(name);
		Storage storage = new Storage(mApplicationConfiguration.GetSettingAsString("storage"),
                name,
                mProvider,
                mUsername,
                mPassword,
                mUserIdentity,
                mApplicationIdentity);
		storage.StrictSSL = StrictSSL;
		return storage;
	}

	/**
	 * Creates a new SMSSender object
	 * 
	 * @param number The phone number to send messages.
	 * @return a new SMSSender object
	 * @throws Exception
	 */
	public SMSSender SMSSender(String number) throws Exception{
		checkMethodParameters(number);
		SMSSender sender = new SMSSender(mApplicationConfiguration.GetSettingAsString("sms"), number, mProvider,
                mUsername,
                mPassword,
                mUserIdentity,
                mApplicationIdentity);

        sender.mUserIdentity = this.mUserIdentity;
		sender.StrictSSL = !StrictSSL;
		return sender;
	}

	/**
	 * Sends an EMail
	 * 
	 * @param mail The mail object with the information needed to send an email
	 * @param callback The callback with the result of the service call
	 * @throws Exception
	 */
	public void SendEmail(Mail mail, ServiceEventListener callback) throws Exception {
		if (mail==null) {
			throw new Exception("Mail message must not be null");
		}
        if (mMailSender ==null)
        {
            mMailSender = new MailSender(mApplicationConfiguration.GetSettingAsString("email"),
                    mProvider,
                    mUsername,
                    mPassword,
                    mUserIdentity,
                    mApplicationIdentity);
            mMailSender.mUserIdentity = this.mUserIdentity;
            mMailSender.StrictSSL = !StrictSSL;
        }

		mMailSender.Send(mail, callback) ;
	}


	/**
	 * Creates a new entry in the KZApplication log
	 * 
	 * @param message The message to write
	 * @param level The log level: Verbose, Information, Warning, Error, Critical
	 * @throws Exception
	 */
	public void WriteLog(String message, LogLevel level) throws Exception  {
        this.WriteLog(message, level, null);
	}

	/**
	 * Creates a new entry in the KZApplication log
	 * 
	 * @param message The message to write
	 * @param level The log level: Verbose, Information, Warning, Error, Critical
	 * @param callback The callback with the result of the service call
	 * @throws Exception
	 */
	public void WriteLog(String message, LogLevel level, ServiceEventListener callback) throws Exception  {
		checkMethodParameters(message);
		if (level==null) {
			throw new Exception("Level must not be null");
		}
        checkApplicationLog();
		mApplicationLog.Write(message, level, callback);
	}

    /**
     * Creates a new entry in the KZApplication log
     *
     * @param message The message to write
     * @param level The log level: Verbose, Information, Warning, Error, Critical
     * @throws Exception
     */
    public void WriteLog(JSONObject jsonObject, LogLevel level) throws Exception  {
        this.WriteLog(jsonObject, level, null);
    }

    public void WriteLog(JSONObject jsonObject, LogLevel level, ServiceEventListener callback) throws Exception {
        checkMethodParameters(String.valueOf(jsonObject));
        if (level==null) {
            throw new Exception("Level must not be null");
        }
        checkApplicationLog();
        mApplicationLog.Write(jsonObject, level, callback);
    }


    private void checkApplicationLog() throws Exception {
        if (mApplicationLog ==null)
        {
            mApplicationLog = new Logging(
                    mApplicationConfiguration.GetSettingAsString("logging"),
                    mProvider,
                    mUsername,
                    mPassword,
                    mUserIdentity,
                    mApplicationIdentity);
            mApplicationLog.mUserIdentity = this.mUserIdentity;
            mApplicationLog.StrictSSL = !StrictSSL;
        }
    }

    /**
	 * Clears the KZApplication log
	 */
	public void ClearLog() throws Exception  {
        checkApplicationLog();
        mApplicationLog.Clear(null);
	}

	/**
	 * Clears the KZApplication log
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void ClearLog(ServiceEventListener callback)  throws Exception {
        checkApplicationLog();
        mApplicationLog.Clear(callback);
	}

	/**
	 * Returns all the messages from the KZApplication log
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void AllLogMessages(ServiceEventListener callback) throws Exception {
        checkApplicationLog();
        HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER, this.mUserIdentity.Token);
		mApplicationLog.All(callback);
	}

	/**
	 * Returns all the messages from the KZApplication log
	 * 
	 * @return a JSONArray with all the log entries
	 * @throws Exception
	 */
	public JSONArray AllLogMessages() throws Exception {
        checkApplicationLog();
        mApplicationLog.All(new ServiceEventListener() {
            public void onFinish(ServiceEvent e) {
                mAllApplicationLogEvents = (JSONArray) e.Response;
            }
        });
		return mAllApplicationLogEvents;
	}
    /**
     * Creates a new Storage object
     *
     * @return a new Storage object
     * @throws Exception
     */
    public Files FileStorage() throws Exception{
        Files files = new Files(mApplicationConfiguration.GetSettingAsString("files"),
                mProvider,
                mUsername,
                mPassword,
                mUserIdentity,
                mApplicationIdentity);
        files.mUserIdentity = this.mUserIdentity;
        files.StrictSSL = !StrictSSL;
        return files;
    }

	private void checkMethodParameters(String name) throws InvalidParameterException {
		if (name.isEmpty() || name == null) {
			throw new InvalidParameterException("name cannot be null or empty");
		}
	}

	//Authentication
	
	/**
	 * Sign outs the current user
	 */
	public void SignOut()
	{
        if (mUserIdentity !=null) {
            IdentityManager.getInstance().SignOut(mUserIdentity.HashKey);
        }
        if (mApplicationIdentity !=null) {
            IdentityManager.getInstance().SignOut(mApplicationIdentity.HashKey);
        }
        UserIsAuthenticated = false;
	}

	
	/**
	 * Authenticates a user using the specified provider
	 * 
	 * @param providerKey The key that identifies the provider
	 * @param username The user account
	 * @param password The password for the user
	 * @throws Exception
	 */
	public void Authenticate(final String providerKey,final String username, final String password) throws JSONException {
		this.Authenticate(providerKey, username, password, null);
	}

	/**
	 * Authenticates a user using the specified provider
	 * 
	 * @param providerKey The key that identifies the provider
	 * @param username The user account
	 * @param password The password for the user
	 * @param callback The callback with the result of the service call
	 * @throws Exception
	 */
	public void Authenticate(final String providerKey,final String username, final String password,final ServiceEventListener callback) {
        if (!KidoAppSettings.getInstance().IsInitialized) {
            if (callback!=null) {
                callback.onFinish(new ServiceEvent(this,HttpStatus.SC_BAD_REQUEST,"The application is not initialized", null));
            }
            return;
        }
        try {
            JSONObject authConfig = KidoAppSettings.getInstance().GetSettingAsJObject("authConfig");
            IdentityManager.getInstance().Setup(authConfig, StrictSSL, mApplicationKey);
            IdentityManager.getInstance().Authenticate(providerKey, username, password, new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    if (e.StatusCode < HttpStatus.SC_BAD_REQUEST) {
                        mProvider = providerKey;
                        mUsername = username;
                        mPassword = password;
                        UserIsAuthenticated = true;
                        long delay = ((KidoZenUser) e.Response).GetExpirationInMilliseconds();
                        SetKidoZenUser((KidoZenUser) e.Response);
                        if (delay < 0) {
                            Log.e(Constants.LOG_CAT_TAG, "There is a mismatch between your device date and the kidozen authentication service.\nThe expiration time from the service is lower than the device date.\nThe OnSessionExpirationRun method will be ignored");
                        }
                    }
                    if (callback != null) callback.onFinish(e);
                }
            });
        }
        catch(Exception e)
        {
            callback.onFinish(new kidozen.client.ServiceEvent(this,HttpStatus.SC_BAD_REQUEST,e.getMessage(), e));
        }

	}

    public void StartPassiveAuthentication(Context context,final kidozen.client.ServiceEventListener callback) {
        String mUserUniqueIdentifier = "";
        if (mUserIdentity!=null)
            mUserUniqueIdentifier = mUserIdentity.Claims.get("http://schemas.kidozen.com/userid").toString();

        if (!KidoAppSettings.getInstance().IsInitialized) {
            if (callback!=null) {
                callback.onFinish(new ServiceEvent(this,HttpStatus.SC_BAD_REQUEST,"The application is not initialized", null));
            }
            return;
        }
        try {
            JSONObject authConfig = KidoAppSettings.getInstance().GetSettingAsJObject("authConfig");
            IdentityManager.getInstance().Setup(authConfig, StrictSSL, mApplicationKey);
            IdentityManager.getInstance().Authenticate(context, mUserUniqueIdentifier, new kidozen.client.ServiceEventListener()   {
                @Override
                public void onFinish(kidozen.client.ServiceEvent e) {
                    SetKidoZenUser((KidoZenUser) e.Response);
                    if (callback != null) callback.onFinish(e);
                }
            });
        }
        catch(Exception e)
        {
            callback.onFinish(new kidozen.client.ServiceEvent(this,HttpStatus.SC_BAD_REQUEST,e.getMessage(), e));
        }
    }

    /**
     * Creates a new LOBService object
     *
     * @param name The name that references the LOBService instance
     * @return a new LOBService object
     * @throws Exception
     */
    public kidozen.client.Service LOBService(String name) throws Exception {
        checkMethodParameters(name);
        kidozen.client.Service service = new kidozen.client.Service(mApplicationConfiguration.GetSettingAsString("url"), name,
                mProvider,
                mUsername,
                mPassword,
                mUserIdentity,
                mApplicationIdentity);

        service.mUserIdentity = this.mUserIdentity;
        service.StrictSSL = !StrictSSL;
        return service;
    }

    /**
     * Creates a new DataSource object
     *
     * @param name The name that references the DataSource instance
     * @return a new DataSource object
     * @throws Exception
     */
    public DataSource DataSource(String name) throws Exception {
        checkMethodParameters(name);
        DataSource service = new DataSource(mApplicationConfiguration.GetSettingAsString("datasource"),
                name,
                mProvider,
                mUsername,
                mPassword,
                mUserIdentity,
                mApplicationIdentity);
        service.mUserIdentity = this.mUserIdentity;
        service.StrictSSL = !StrictSSL;
        return service;
    }

    private class InitializationWithKeyCallback implements kidozen.client.ServiceEventListener {
        kidozen.client.ServiceEventListener _callback;
        public InitializationWithKeyCallback(kidozen.client.ServiceEventListener cb) {
            _callback = cb;
        }
        @Override
        public void onFinish(kidozen.client.ServiceEvent e) {
            JSONObject authConfig = null;
            try {
                authConfig = mApplicationConfiguration.GetSettingAsJObject("authConfig");
                authConfig.put("domain", mApplicationConfiguration.GetSettingAsString("domain"));
                IdentityManager.getInstance().Setup(authConfig, StrictSSL, mApplicationKey);
                IdentityManager.getInstance().Authenticate(mApplicationKey, new kidozen.client.ServiceEventListener() {
                    @Override
                    public void onFinish(kidozen.client.ServiceEvent e) {
                        if (e.StatusCode== HttpStatus.SC_OK) {
                            mIsAuthenticatedWithAppKey = true;
                            mApplicationIdentity = (KidoZenUser) e.Response;
                        }
                        _callback.onFinish(e);
                    }
                });
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
}
