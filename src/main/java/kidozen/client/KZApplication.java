package kidozen.client;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import kidozen.client.authentication.*;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * @author kidozen
 * @version 1.00, April 2013
 * 
 * Main KidoZen application object
 *
 */
public class KZApplication extends KZService {
    public static final String APPLICATION_NOT_FOUND = "Application not found";
    public static final String TOKEN_CHANGE_INTENT = "kidozen.client.intent.TOKEN_CHANGE";

    public Boolean Initialized = false;
	private Map<String, JSONObject> ips= new HashMap<String, JSONObject>();
    private Map<String, AsyncTask> tasks = new HashMap<String, AsyncTask>();
    public Boolean Authenticated = false;
	public Map<String, String> IdentityProvidersKeys = new HashMap<String, String>();
	public Boolean BypassSSLVerification = false;
	
	private static final String LOGTAG = "KZApplication";
	private Map<String, JSONObject> _identityProviders;
	private String _emailEndpoint;
	private String _queueEndpoint;
	private String _storageEndpoint;
    private String _lobEndpoint;
	private String _smsEndpoint;
	private String _configurationEndpoint;
	private String _wsSubscriberEndpoint;
	private String _publisherEndpoint;
	private String _logEndpoint;
	private String _notificationEndpoint;
    private String _filesEndpoint;
	private static JSONArray _allApplicationLogEvents;
	private int initializedStatusCode=0;
	private Object initializedResponse;
	private String initializedBody;

	JSONObject _authConfig;
//	String _providerKey="";
//	String _username="";
//	String _password="";

    private Logging _applicationLog;
    private MailSender _mailSender;

    private HandlerThread expirationThread = new HandlerThread("HandlerThread");
    private Handler sessionExpiresHandler ;

	/**
	 * Returns the current KidoZen identity
	 * 
	 * @return The current KidoZen User
	 */
	public KidoZenUser GetKidozenUser() {
		return KidozenUser;
	}
	
	/**
	 * Allows to change the current KidoZen user identity
	 * 
	 * @param kidozenUser the new identity
	 */
	public void SetKidozenUser(KidoZenUser _kidozenUser) {
		this.KidozenUser = _kidozenUser;
	}

	/**
	 * Constructor
	 * 
	 * @param tenantMarketPlace The url of the KidoZen marketplace
	 * @param application The application name
	 * @param bypassSSLVerification Allows to bypass the SSL validation, use it only for development purposes
	 * @param callback The ServiceEventListener callback with the operation results
	 * @throws Exception The latest exception is there was any
	 */
    public KZApplication(String tenantMarketPlace, String application, Boolean bypassSSLVerification, final ServiceEventListener callback) throws Exception{
        super();
        BypassSSLVerification = bypassSSLVerification;
        _tenantMarketPlace = tenantMarketPlace;
        _application= application;
        _initializationCallback = callback;
        if (!_tenantMarketPlace.endsWith("/")) {
            _tenantMarketPlace = _tenantMarketPlace + "/";
        }
        String url = _tenantMarketPlace + "publicapi/apps?name=" + _application;
        HashMap<String, String> params = null;
        HashMap<String, String> headers = new HashMap<String, String>();

        this.ExecuteTask(url, KZHttpMethod.GET, params, headers, configurationCallback, BypassSSLVerification);
    }

    private ServiceEventListener _initializationCallback = null;

    private ServiceEventListener configurationCallback = new ServiceEventListener() {
        @Override
        public void onFinish(ServiceEvent e) {
                ServiceEvent se = new ServiceEvent(this, e.StatusCode, e.Body, e.Response);
                try {
                    parseApplicationConfiguration(e.Body);
                    initializedStatusCode = e.StatusCode;
                    initializedBody = e.Body;
                    initializedResponse = e.Response;
                    Initialized = (e.Exception == null);
                } catch (JSONException je) {
                    se.StatusCode = HttpStatus.SC_NOT_FOUND;
                    se.Body = APPLICATION_NOT_FOUND;
                    se.Response = APPLICATION_NOT_FOUND;
                    se.Exception = je;
                } catch (InvalidParameterException ipe) {
                    se.StatusCode = HttpStatus.SC_NOT_FOUND;
                    se.Body = APPLICATION_NOT_FOUND;
                    se.Response = APPLICATION_NOT_FOUND;
                    se.Exception = ipe;
                } finally {
                    if (_initializationCallback != null)
                        _initializationCallback.onFinish(se);
                }
            }
    };



    private void parseApplicationConfiguration(String response) throws JSONException, InvalidParameterException {
        JSONArray cfg = new JSONArray(response);
        JSONObject wrapper = cfg.getJSONObject(0);
        if (wrapper==null) {
            throw new InvalidParameterException("Application not found.");
        }
        _lobEndpoint =  wrapper.get("url").toString();
        _authConfig =  wrapper.getJSONObject("authConfig");
        _emailEndpoint = wrapper.get("email").toString();
        _queueEndpoint = wrapper.get("queue").toString();
        _storageEndpoint =  wrapper.get("storage").toString();
        _smsEndpoint =  wrapper.get("sms").toString();
        _configurationEndpoint= wrapper.get("config").toString();
        _wsSubscriberEndpoint=wrapper.get("ws").toString();
        _publisherEndpoint = wrapper.get("pubsub").toString();
        _logEndpoint= wrapper.get("logging").toString();
        _notificationEndpoint = wrapper.get("notification").toString();
        _filesEndpoint = wrapper.get("files").toString();

        Log.d(LOGTAG, "Getting provider configuration");
        _identityProviders = new HashMap<String, JSONObject>();
        String jsonconfig  = _authConfig.toString();
        JSONObject configurations  = new JSONObject(jsonconfig);
        JSONObject identityProviders = configurations.getJSONObject("identityProviders");
        authServiceEndpoint = configurations.get("authServiceEndpoint").toString();
        applicationScope =configurations.get("applicationScope").toString();
        authServiceScope = configurations.get("authServiceScope").toString();

        @SuppressWarnings("unchecked")
        Iterator<String> providersIterator = identityProviders.keys();
        while(providersIterator.hasNext()){
            String ipName = providersIterator.next();
            JSONObject ip = identityProviders.getJSONObject(ipName);
            String endpoint =  ip.getString("endpoint");
            String protocol = ip.getString("protocol");

            JSONObject kzip = new JSONObject();
            kzip.put("authServiceScope", authServiceScope);
            kzip.put("authServiceEndpoint", endpoint);
            kzip.put("applicationScope", applicationScope);
            kzip.put("key", ipName);
            kzip.put("endpoint", endpoint);
            kzip.put("protocol", protocol);
            //TODO: replace with factory
            IIdentityProvider ipinstance =null;
            if (protocol.toLowerCase().equals("wrapv0.9")) {
                ipinstance= new WRAPv09IdentityProvider();
                ((WRAPv09IdentityProvider)ipinstance).bypassSSLValidation=BypassSSLVerification;
            }
            else {
                ipinstance=new ADFSWSTrustIdentityProvider();
                ((ADFSWSTrustIdentityProvider)ipinstance).bypassSSLValidation=BypassSSLVerification;
            }

            //
            kzip.put("instance", ipinstance);
            _identityProviders.put(ipName, kzip );
            IdentityProvidersKeys.put(ipName, protocol);
        }
        return;
    }


    public KZApplication( Map<String, JSONObject> ips, Map<String,AsyncTask> tasks) {

        this.ips = ips;
        this.tasks = tasks;
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
		this(tenantMarketPlace,application,false,callback);
	}

	/**
	 * Creates a new PubSubChannel object
	 * 
	 * @param name The name that references the channel instance
	 * @return A new PubSubChannel object
	 * @throws Exception
	 */
	public PubSubChannel PubSubChannel(String name) throws Exception{
		checkMethodParameters(name);
		PubSubChannel channel = new PubSubChannel(_wsSubscriberEndpoint, _publisherEndpoint, name, this.KidozenUser, this.BypassSSLVerification);
        channel.setKidozenUser(this.KidozenUser);
        channel._bypassSSLVerification = this.BypassSSLVerification;

        channel.SetCredentials(_provider, _username, _password, null);
        channel.SetAuthenticateParameters(_tenantMarketPlace, _application, _identityProviders, applicationScope, authServiceScope, authServiceEndpoint, _ipEndpoint);
        tokenUpdater.addObserver(channel);

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
		Notification notification= new Notification( _notificationEndpoint, _application);
		notification.KidozenUser = this.KidozenUser;
		notification.BypassSSLVerification = this.BypassSSLVerification;
		tokenUpdater.addObserver(notification);
        CloneCredentials(notification);
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
		Configuration configuration =new  Configuration(_configurationEndpoint, name);
		configuration.KidozenUser = this.KidozenUser;
		configuration.BypassSSLVerification = this.BypassSSLVerification;
		tokenUpdater.addObserver(configuration);
        CloneCredentials(configuration);
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
		Queue queue= new Queue(_queueEndpoint, name);
		queue.KidozenUser = this.KidozenUser;
		queue.BypassSSLVerification = this.BypassSSLVerification;
		tokenUpdater.addObserver(queue);
        CloneCredentials(queue);
		return queue;
	}

	/**
	 * Creates a new Storage object
	 * 
	 * @param name The name that references the Storage instance
	 * @return a new Storage object
	 * @throws Exception
	 */
	public Storage Storage(String name) throws Exception{
		checkMethodParameters(name);
		Storage storage = new Storage(_storageEndpoint, name);
		storage.KidozenUser = this.KidozenUser;
		storage.BypassSSLVerification = this.BypassSSLVerification;
		tokenUpdater.addObserver(storage);
        CloneCredentials(storage);
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
		SMSSender sender = new SMSSender(_smsEndpoint, number);
		sender.KidozenUser = this.KidozenUser;
		sender.BypassSSLVerification = this.BypassSSLVerification;
		tokenUpdater.addObserver(sender);
        CloneCredentials(sender);
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
            _mailSender = new MailSender(_emailEndpoint);
            _mailSender.KidozenUser = this.KidozenUser;
            _mailSender.BypassSSLVerification = this.BypassSSLVerification;
            tokenUpdater.addObserver(_mailSender);
            CloneCredentials(_mailSender);
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

    private void checkApplicationLog() {
        if (_applicationLog==null)
        {
            _applicationLog = new Logging(_logEndpoint);
            _applicationLog.KidozenUser = this.KidozenUser;
            _applicationLog.BypassSSLVerification = this.BypassSSLVerification;
            tokenUpdater.addObserver(_applicationLog);
            CloneCredentials(_applicationLog);
        }
    }

    /**
	 * Clears the KZApplication log
	 */
	public void ClearLog()  {
        checkApplicationLog();
        _applicationLog.Clear(null);
	}

	/**
	 * Clears the KZApplication log
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void ClearLog(ServiceEventListener callback)  {
        checkApplicationLog();
        _applicationLog.Clear(callback);
	}

	/**
	 * Returns all the messages from the KZApplication log
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void AllLogMessages(ServiceEventListener callback)
	{
        checkApplicationLog();
        HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER,this.KidozenUser.Token);
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
     * @param name The name that references the Storage instance
     * @return a new Storage object
     * @throws Exception
     */
    public Files FileStorage() throws Exception{
        Files files = new Files(_filesEndpoint);
        files.KidozenUser = this.KidozenUser;
        files.BypassSSLVerification = this.BypassSSLVerification;
        tokenUpdater.addObserver(files);
        CloneCredentials(files);
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
	public void Authenticate(final String providerKey,final String username, final String password)
	{
		this.Authenticate(providerKey, username, password,null);
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
	public void Authenticate(final String providerKey,final String username, final String password,final ServiceEventListener callback) 
	{
		_provider= providerKey;
		_username=username;
		_password=password;


        if (!Initialized) {
            if (callback!=null) {
                callback.onFinish(new ServiceEvent(this,HttpStatus.SC_CONFLICT,"The application is not initialized", null));
            }
            return;
        }
		if (_identityProviders.get(providerKey)==null) {
			if (callback!=null) {
				callback.onFinish(new ServiceEvent(this,HttpStatus.SC_BAD_REQUEST,"The specified provider does not exists", null));
			}
		}
		else {
            super.SetAuthenticateParameters(_tenantMarketPlace, _application, _identityProviders, applicationScope, authServiceScope, authServiceEndpoint, _ipEndpoint);
            super.Authenticate(providerKey, username, password, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                if (e.StatusCode<HttpStatus.SC_BAD_REQUEST)
                {
                    long delay =  ((KidoZenUser)e.Response).GetExpirationInMiliseconds();
                    if (delay<0)
                    {
                        Log.e(LOGTAG, "There is a mismatch between your device date and the kidozen authentication service.\nThe expiration time from the service is lower than the device date.\nThe OnSessionExpirationRun method will be ignored");
                    }
                }
                if (callback!=null) {
                    callback.onFinish(e);
                }
            }});
        }
	}

	/**
	 * Sets a new runnable to handle Token Expiration. Use it if you want to override de default behavior
	 * 
	 * @param onSessionExpirationRunnable a custom Runnable to invoke
	 */
	public void OnSessionExpirationRunnable(Runnable onSessionExpirationRunnable) {
        expirationThread.start();
        sessionExpiresHandler = new Handler(expirationThread.getLooper());
        sessionExpiresHandler.postDelayed(onSessionExpirationRunnable,KidozenUser.GetExpirationInMiliseconds());
	}
	
	private final Runnable defaultSessionExpirationEvent() {
		return new Runnable() {
			public void run() {
					Authenticate(_provider, _username, _password);
			}
		};
	}

	/**
	 * Allows to register a new Identity provider.
	 * 
	 * @param key The key that will identify the new provider
	 * @param protocol The protocol that implements
	 * @param endpoint The Identity Provider endpoint
	 * @param provider  An instance of an object that implements the IIdentityProvider interface
	 * @throws Exception
	 */
	public void RegisterIdentityProvider(String key, String protocol, String endpoint, IIdentityProvider provider) throws Exception
	{
		if (!_identityProviders.containsKey(key)) {
			JSONObject kzip = new JSONObject();
			kzip.put("authServiceScope", authServiceScope);
			kzip.put("authServiceEndpoint", authServiceEndpoint);
			kzip.put("applicationScope", applicationScope);
			kzip.put("key", key);
			kzip.put("endpoint", endpoint);
			kzip.put("protocol", protocol);
			provider.Configure(kzip);
			kzip.put("instance", provider);
			_identityProviders.put(key, kzip );
		}
	}

    /**
     * Creates a new Storage object
     *
     * @param name The name that references the Storage instance
     * @return a new Storage object
     * @throws Exception
     */
    public Service LOBService(String name) {
        checkMethodParameters(name);
        Service service = new Service(_lobEndpoint, name);
        service.KidozenUser = this.KidozenUser;
        service.BypassSSLVerification = this.BypassSSLVerification;
        tokenUpdater.addObserver(service);
        return service;
    }

    public void RenewAuthentication(final ServiceEventListener callback) {
        super.RenewAuthenticationToken(callback);
    }

    private void CloneCredentials(KZService service) {
        if (_ipEndpoint==null)
            try {
                _ipEndpoint = _identityProviders.get(_provider).getString("endpoint");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        service.SetCredentials(_provider, _username, _password, null);
        service.SetAuthenticateParameters(_tenantMarketPlace, _application, _identityProviders, applicationScope, authServiceScope, authServiceEndpoint, _ipEndpoint);
    }

}

