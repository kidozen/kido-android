package kidozen.client;

import android.app.Application;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.HashMap;

import kidozen.client.authentication.IdentityManager;
import kidozen.client.authentication.KidoZenUser;

/**
 * @author kidozen
 * @version 1.2.0, April 2014
 * 
 * Main KidoZen application object
 *
 */
public class KZApplication  {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static CrashReporter _crashReporter;
    public Boolean Authenticated = false;
	public Boolean StrictSSL = true;

	private static JSONArray _allApplicationLogEvents;
    private Logging _applicationLog;
    private MailSender _mailSender;

    KidoAppSettings _applicationConfiguration;

    private String _applicationKey;
    private String _tenantMarketPlace;
    private String _applicationName;

    public static void EnableCrashReporter (Application application, String url) {
        if (_crashReporter==null)
            _crashReporter = new CrashReporter(application,url);
    }
    //
    //New variables because of refactor
    //
    KidoZenUser userIdentity;
    KidoZenUser applicationIdentity;
    /**
	 * Returns the current KidoZen identity
	 * 
	 * @return The current KidoZen User
	 */
	public KidoZenUser GetKidoZenUser() {
		return userIdentity;
	}
	private KZApplication() {}
	/**
	 * Allows to change the current KidoZen user identity
	 * 
	 * @param user the new identity
	 */
	public void SetKidoZenUser(KidoZenUser user) {
		this.userIdentity = user;
	}

    /**
     * Constructor
     *
     * @param tenantMarketPlace The url of the KidoZen marketplace
     * @param application The application name
     * @param callback The ServiceEventListener callback with the operation results
     * @throws Exception The latest exception is there was any
     */
    public KZApplication(String tenantMarketPlace, String application, ServiceEventListener callback) throws Exception{
        this(tenantMarketPlace,application,Constants.UNSET_APPLICATION_KEY,false,callback);
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
     * @param applicationKey The application key for anonymous logging
     * @param callback The ServiceEventListener callback with the operation results
     * @throws Exception The latest exception is there was any
     */
    public KZApplication(String tenantMarketPlace, String application,  Boolean strictSSL, ServiceEventListener callback) throws Exception{
        this(tenantMarketPlace, application, Constants.UNSET_APPLICATION_KEY, strictSSL, callback);
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
    public KZApplication(String tenantMarketPlace, String application, String applicationKey, Boolean strictSSL, final ServiceEventListener callback) throws Exception {
        this.StrictSSL = !strictSSL;
        this._applicationKey = applicationKey;
        _tenantMarketPlace = tenantMarketPlace;
        _applicationName = application;
        if (!_tenantMarketPlace.endsWith("/")) _tenantMarketPlace = _tenantMarketPlace + "/";

        String url = _tenantMarketPlace + Constants.PUBLICAPI_PATH + _applicationName;

        if (applicationKey != Constants.UNSET_APPLICATION_KEY) {
            // chains the getSettings callback with the keyAuth callback
            _applicationConfiguration = KidoAppSettings.getInstance(new InitializationWithKeyCallback(callback), this.StrictSSL);
        }
        else {
            _applicationConfiguration = KidoAppSettings.getInstance(callback, this.StrictSSL);
        }
        _applicationConfiguration.execute(url).get();
    }


	/**
     * TODO: Revisar cuidadosamente esto
	 * Creates a new PubSubChannel object
	 * 
	 * @param name The name that references the channel instance
	 * @return A new PubSubChannel object
	 * @throws Exception

	public PubSubChannel PubSubChannel(String name) throws Exception{
		checkMethodParameters(name);
		PubSubChannel channel = new PubSubChannel(_wsSubscriberEndpoint, _publisherEndpoint, name, this.userIdentity, !StrictSSL);
        channel.setKidozenUser(this.userIdentity);
        channel._bypassSSLVerification = !StrictSSL;
        channel.SetCredentials(_provider, _username, _password, null);
        channel.SetAuthenticateParameters(_tenantMarketPlace, _applicationName, _identityProviders, _applicationScope, _authServiceScope, _authServiceEndpoint, _ipEndpoint);
		return channel;
	}
     */
	/**
	 * Push notification service main entry point
	 * 
	 * @return The Push notification object that allows to interact with the Google Cloud Messaging Services (GCM)
	 * @throws Exception
	 */
	public Notification Notification () throws Exception
	{
		Notification notification= new Notification( _applicationConfiguration.GetSettingAsString("notification"), _applicationName);
		notification.KidozenUser = this.userIdentity;
		notification.BypassSSLVerification = !StrictSSL;
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
		Configuration configuration =new  Configuration(_applicationConfiguration.GetSettingAsString("configuration"), name);
		configuration.KidozenUser = this.userIdentity;
		configuration.BypassSSLVerification = !StrictSSL;
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
		Queue queue= new Queue(_applicationConfiguration.GetSettingAsString("queue"), name);
		queue.KidozenUser = this.userIdentity;
		queue.BypassSSLVerification = !StrictSSL;
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
		Storage storage = new Storage(_applicationConfiguration.GetSettingAsString("storage"), name);
		storage.KidozenUser = this.userIdentity;
		storage.BypassSSLVerification = !StrictSSL;
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
		SMSSender sender = new SMSSender(_applicationConfiguration.GetSettingAsString("sms"), number);
		sender.KidozenUser = this.userIdentity;
		sender.BypassSSLVerification = !StrictSSL;
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
        if (_mailSender==null)
        {
            _mailSender = new MailSender(_applicationConfiguration.GetSettingAsString("email"));
            _mailSender.KidozenUser = this.userIdentity;
            _mailSender.BypassSSLVerification = !StrictSSL;
        }

		_mailSender.Send( mail,  callback) ;
	}


	/**
	 * Creates a new entry in the KZApplication log
	 * 
	 * @param message The message to write
	 * @param level The log level: Verbose, Information, Warning, Error, Critical
	 * @throws Exception
	 */
	public void WriteLog(String message, LogLevel level) throws Exception  {
		checkMethodParameters(message);
		if (level==null) {
			throw new Exception("Level must not be null");
		}
        checkApplicationLog();
        HashMap<String, String> msg = new HashMap<String, String>();
        msg.put("message", message);
        _applicationLog.Write(new JSONObject(msg), level, null);
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
        HashMap<String, String> msg = new HashMap<String, String>();
		msg.put("message", message);
		_applicationLog.Write(new JSONObject(msg), level, callback);
	}

    private void checkApplicationLog() throws Exception {
    if (_applicationLog==null)
        {
            _applicationLog = new Logging(_applicationConfiguration.GetSettingAsString("logging"));
            _applicationLog.KidozenUser = this.userIdentity;
            _applicationLog.BypassSSLVerification = !StrictSSL;
        }
    }

    /**
	 * Clears the KZApplication log
	 */
	public void ClearLog() throws Exception  {
        checkApplicationLog();
        _applicationLog.Clear(null);
	}

	/**
	 * Clears the KZApplication log
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void ClearLog(ServiceEventListener callback)  throws Exception {
        checkApplicationLog();
        _applicationLog.Clear(callback);
	}

	/**
	 * Returns all the messages from the KZApplication log
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void AllLogMessages(ServiceEventListener callback) throws Exception {
        checkApplicationLog();
        HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER, this.userIdentity.Token);
		_applicationLog.All(callback);
	}

	/**
	 * Returns all the messages from the KZApplication log
	 * 
	 * @return a JSONArray with all the log entries
	 * @throws Exception
	 */
	public JSONArray AllLogMessages() throws Exception {
        checkApplicationLog();
        _applicationLog.All(new ServiceEventListener() {
			public void onFinish(ServiceEvent e) {
				_allApplicationLogEvents = (JSONArray) e.Response;
			}
		});
		return _allApplicationLogEvents;
	}
    /**
     * Creates a new Storage object
     *
     * @return a new Storage object
     * @throws Exception
     */
    public Files FileStorage() throws Exception{
        Files files = new Files(_applicationConfiguration.GetSettingAsString("files"));
        files.KidozenUser = this.userIdentity;
        files.BypassSSLVerification = !StrictSSL;
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
        //TODO: super.SignOut();
		Authenticated = false;
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
            IdentityManager.getInstance().Setup(authConfig, StrictSSL);
            IdentityManager.getInstance().Authenticate(providerKey, username, password, new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    if (e.StatusCode < HttpStatus.SC_BAD_REQUEST) {
                        Authenticated = true;
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
            callback.onFinish(new ServiceEvent(this,HttpStatus.SC_BAD_REQUEST,e.getMessage(), e));
        }

	}

	/**
     * TODO: Sirve? Aplica?
     *
	 * Sets a new runnable to handle Token Expiration. Use it if you want to override de default behavior
	 * 
	 * @param onSessionExpirationRunnable a custom Runnable to invoke
	 */
	//public void OnSessionExpirationRunnable(Runnable onSessionExpirationRunnable) {

	//private final Runnable defaultSessionExpirationEvent() {
	//	return new Runnable() {
	//		public void run() {
	//				//Authenticate(_provider, _username, _password);
	//		}
	//	};
	//}

	/**
     * * TODO: Sirve? Aplica?
	 * Allows to register a new Identity provider.
	 *
	 * @param key The key that will identify the new provider
	 * @param protocol The protocol that implements
	 * @param endpoint The Identity Provider endpoint
	 * @param provider  An instance of an object that implements the IIdentityProvider interface
	 * @throws Exception
	 */

    /**
     * Creates a new LOBService object
     *
     * @param name The name that references the LOBService instance
     * @return a new LOBService object
     * @throws Exception
     */
    public Service LOBService(String name) throws Exception {
        checkMethodParameters(name);
        Service service = new Service(_applicationConfiguration.GetSettingAsString("url"), name);
        service.KidozenUser = this.userIdentity;
        service.BypassSSLVerification = !StrictSSL;
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
        DataSource service = new DataSource(_applicationConfiguration.GetSettingAsString("datasource"), name);
        service.KidozenUser = this.userIdentity;
        service.BypassSSLVerification = !StrictSSL;
        return service;
    }


    private class InitializationWithKeyCallback implements ServiceEventListener {
        ServiceEventListener _callback;
        public InitializationWithKeyCallback(ServiceEventListener cb) {
            _callback = cb;
        }
        @Override
        public void onFinish(ServiceEvent e) {
            JSONObject authConfig = null;
            try {
                authConfig = _applicationConfiguration.GetSettingAsJObject("authConfig");
                authConfig.put("domain", _applicationConfiguration.GetSettingAsString("domain"));
                IdentityManager.getInstance().Setup(authConfig, StrictSSL);
                IdentityManager.getInstance().Authenticate(_applicationKey, new ServiceEventListener() {
                    @Override
                    public void onFinish(ServiceEvent e) {
                        if (e.StatusCode== HttpStatus.SC_OK) {
                            Authenticated = true;
                            applicationIdentity = (KidoZenUser) e.Response;
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

