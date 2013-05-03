package kidozen.client;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;

import kidozen.client.authentication.ADFSWSTrustIdentityProvider;
import kidozen.client.authentication.AuthenticationManager;
import kidozen.client.authentication.IIdentityProvider;
import kidozen.client.authentication.KidoZenUser;
import kidozen.client.authentication.WRAPv09IdentityProvider;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

/**
 * @author kidozen
 * @version 1.00, April 2013
 * 
 * Main KidoZen application object
 *
 */
public class KZApplication extends KZService {
	public static Boolean Initialized = false;
	public static final String TOKEN_CHANGE_INTENT = "kidozen.client.intent.TOKEN_CHANGE";
	public Boolean Authenticated = false;
	public Map<String, String> IdentityProvidersKeys = new HashMap<String, String>();
	public Boolean BypassSSLVerification = false;
	
	private static final String LOGTAG = "KZApplication";
	private Map<String, JSONObject> _identityProviders;
	private String _emailEndpoint;
	private String _queueEndpoint;
	private String _storageEndpoint;
	private String _smsEndpoint;
	private String _configurationEndpoint;
	private String _wsSubscriberEndpoint;
	private String _publisherEndpoint;
	private String _logEndpoint;
	private MailSender _mailSender;
	private String _notificationEndpoint;
	private Logging _applicationLog;
	private static JSONArray _allApplicationLogEvents;
	ObservableUser tokenUpdater = new ObservableUser();
	private int initializedStatusCode=0;
	private Object initializedResponse;
	private String initializedBody;

	JSONObject _authConfig;
	String _providerKey="";
	String _username="";
	String _password="";
	private Runnable _onSessionExpirationRunnable;
	private final Handler sessionExpiresHandler = new Handler(); 

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
	public KZApplication(String tenantMarketPlace, String application, Boolean bypassSSLVerification, ServiceEventListener callback) throws Exception{
		super();
		BypassSSLVerification = bypassSSLVerification;
		_tennantMarketPlace= tenantMarketPlace;
		_application= application;
		
		AsyncTask<Void, Void, Void> _init = getApplicationConfiguration();
		_init.execute().get();
		if (callback!=null) {
			callback.onFinish(new ServiceEvent(this, initializedStatusCode, initializedBody, initializedResponse));
		}	
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
		tokenUpdater.addObserver(channel);
		channel.setKidozenUser(this.KidozenUser);
		
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
		_mailSender.KidozenUser = this.KidozenUser;
		_mailSender.BypassSSLVerification = this.BypassSSLVerification;
		tokenUpdater.addObserver(_mailSender);
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
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put("message", message);
		_applicationLog.KidozenUser = this.KidozenUser;
		_applicationLog.BypassSSLVerification = this.BypassSSLVerification;
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
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put("message", message);
		_applicationLog.KidozenUser = this.KidozenUser;
		_applicationLog.BypassSSLVerification = this.BypassSSLVerification;
		_applicationLog.Write(new JSONObject(msg), level, callback);
	}

	/**
	 * Clears the KZApplication log
	 */
	public void ClearLog()  {
		_applicationLog.KidozenUser = this.KidozenUser;
		_applicationLog.BypassSSLVerification = this.BypassSSLVerification;
		_applicationLog.Clear(null);
	}

	/**
	 * Clears the KZApplication log
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void ClearLog(ServiceEventListener callback)  {
		_applicationLog.KidozenUser = this.KidozenUser;
		_applicationLog.BypassSSLVerification = this.BypassSSLVerification;
		_applicationLog.Clear(callback);
	}

	/**
	 * Returns all the messages from the KZApplication log
	 * 
	 * @param callback The callback with the result of the service call
	 */
	public void AllLogMessages(ServiceEventListener callback)
	{
		_applicationLog.KidozenUser = this.KidozenUser;
		_applicationLog.BypassSSLVerification = this.BypassSSLVerification;
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
		_applicationLog.KidozenUser = this.KidozenUser;
		_applicationLog.BypassSSLVerification = this.BypassSSLVerification;
		_applicationLog.All(new ServiceEventListener() {
			public void onFinish(ServiceEvent e) {
				_allApplicationLogEvents = (JSONArray) e.Response;
			}
		});
		return _allApplicationLogEvents;
	}

	private void checkMethodParameters(String name) throws Exception {
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
	public void Authenticate(final String providerKey,final String username, final String password) throws Exception
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
		_providerKey= providerKey;
		_username=username;
		_password=password;
		
		if (_identityProviders.get(providerKey)==null) {
			if (callback!=null) {
				callback.onFinish(new ServiceEvent(this,HttpStatus.SC_BAD_REQUEST,"The specified provider does not exists", null));
			}
		}
		else {
			AuthenticationManager am = new AuthenticationManager(_tennantMarketPlace, _application, _identityProviders, applicationScope,  authServiceScope,  authServiceEndpoint,  ipEndpoint, tokenUpdater);
			am.bypassSSLValidation = BypassSSLVerification;
		
			this.KidozenUser = am.Authenticate(providerKey, username, password, new ServiceEventListener() {
				@Override
				public void onFinish(ServiceEvent e) {
					if (e.StatusCode<HttpStatus.SC_BAD_REQUEST) {
						long delay =  ((KidoZenUser)e.Response).GetExpirationInMiliseconds();
						if (delay<0) {
							Log.e(LOGTAG, "There is a mismatch between your device date and the kidozen authentication service.\nThe expiration time from the service is lower than the device date.\nThe OnSessionExpirationRun method will be ignored");
						}
						else {
							sessionExpiresHandler.postDelayed(defaultSessionExpirationEvent(),delay);
						}					}
					callback.onFinish(e);
				}});
			}
	}

	/**
	 * Sets a new runnable to handle Token Expiration. Use it if you want to override de default behavior
	 * 
	 * @param onSessionExpirationRunnable a custom Runnable to invoke
	 */
	public void OnSessionExpirationRunnable(Runnable onSessionExpirationRunnable) {
		this._onSessionExpirationRunnable = onSessionExpirationRunnable;
		this.sessionExpiresHandler.postDelayed(this._onSessionExpirationRunnable, this.KidozenUser.GetExpirationInMiliseconds());
	}
	
	private final Runnable defaultSessionExpirationEvent() {
		return new Runnable() {
			public void run() {
				try {
					Authenticate(_providerKey, _username, _password);
				} catch (Exception e) {
					e.printStackTrace();
				}
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
	 *@author kidozen
	 * @version 1.00, April 2013
	 * 
	 * For internal use only. Do not override or use 
	 */
	public class ObservableUser extends Observable
	{
		public void TokenUpdated(KidoZenUser kzuser) {
			setChanged();
			notifyObservers(kzuser);
		}
	}
	

	private AsyncTask<Void, Void, Void> getApplicationConfiguration() {
		return new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					if (!_tennantMarketPlace.endsWith("/")) {
						_tennantMarketPlace = _tennantMarketPlace + "/";
					}
					String url = _tennantMarketPlace + "publicapi/apps?name=" + _application; 

					Hashtable<String, String> response = Utilities.ExecuteHttpGet(url,null,null, BypassSSLVerification);
					String body = response.get("responseBody");
					JSONObject wrapper = parseApplicationConfiguration(body);

					initializedStatusCode = Integer.parseInt(response.get("statusCode"));
					initializedBody = body;
					Initialized = (wrapper!=null);
					initializedResponse = response.get("responseMessage");
				} 
				catch (Exception e) {
					initializedStatusCode = (initializedStatusCode==0 ? HttpStatus.SC_BAD_REQUEST : initializedStatusCode);
					Initialized = false;
					initializedBody= e.getMessage();
					initializedResponse = e;
				}
				return null;
			}

			private JSONObject parseApplicationConfiguration(String response)
					throws JSONException, Exception {
				JSONArray cfg = new JSONArray(response);
				JSONObject wrapper = cfg.getJSONObject(0);
				if (wrapper==null) {
					throw new InvalidParameterException("Application not found.");
				}
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
				_applicationLog = new Logging(_logEndpoint);
				_mailSender = new MailSender(_emailEndpoint);

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
				return wrapper;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
			}
		};
	}
}

