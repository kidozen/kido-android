package kidozen.client;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.Observable;

import kidozen.client.authentication.AuthenticationManager;
import kidozen.client.authentication.KidoZenUser;

import org.json.JSONObject;

import android.util.Log;

import com.netiq.websocket.WebSocketClient;
/**
 * Publish and subscribe service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class PubSubChannel extends WebSocketClient implements Observer{
	private static final String TAG = "PubSubChannel";
	String _wsendpoint;
	String _endpoint;
	String _name;

	private KZAction<JSONObject> _onMessage;
	private KZAction<Exception> _onError;
	private KidoZenUser _kidozenUser;
	public Boolean _bypassSSLVerification;
	protected static final String ACCEPT = "Accept";
	protected static final String APPLICATION_JSON = "application/json";
	protected static final String CONTENT_TYPE = "content-type";
	protected static final String AUTHORIZATION_HEADER = "Authorization";
    private String _provider, _username, _password, _tenantMarketPlace, _application, _applicationScope, _authServiceScope, _authServiceEndpoint, _ipEndpoint;
    private Map<String, JSONObject> _identityProviders;
    private AuthenticationManager am;
    private ObservableUser tokenUpdater;
    private ServiceEventListener _authenticateCallback;
    KZService _underlyingKZService = new KZService();

    /**
	 * Constructor
	 * 
	 * You should not create a new instances of this constructor. Instead use the PubSubChannel[""] method of the KZApplication object. 
	 * @param wsEndpoint The websocket endpoint
	 * @param endpoint The service endpoint
	 * @param name The name of the publish and subscribe channel to be created
	 * @param user The user identity
	 * @param bypassSSLVerification Allows to bypass the SSL validation, use it only for development purposes
	 * @throws Exception The latest exception is there was any
	 */
	public PubSubChannel(String wsEndpoint, String endpoint, String name, KidoZenUser user, Boolean bypassSSLVerification) throws Exception
	{
		super(new URI(wsEndpoint));
		_kidozenUser = user;
		_wsendpoint = wsEndpoint;
		_endpoint=endpoint;
		_name = name;
		_bypassSSLVerification = bypassSSLVerification;
	}

    /**
     * Publish a message into the channel
     *
     * @param message The message to publish
     * @param callback The callback with the result of the service call
     */
    public void Publish(final JSONObject message, final ServiceEventListener callback)
    {
        this.Publish(message,true, callback);
    }

	/**
	 * Publish a message into the channel
	 * 
	 * @param message The message to publish
     * @param isPrivate marks the object as private (true) / publc (false)
	 * @param callback The callback with the result of the service call
	 */
	public void Publish(final JSONObject message, final boolean isPrivate, final ServiceEventListener callback)
	{
        _underlyingKZService.KidozenUser = this._kidozenUser;
        CloneCredentials();

        String  url = _endpoint + "/" + _name;
		HashMap<String, String> params = new HashMap<String, String>();
        params.put("isPrivate", (isPrivate ? "true" : "false"));;
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(AUTHORIZATION_HEADER, _underlyingKZService.CreateAuthHeaderValue());
		headers.put(CONTENT_TYPE,APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);

        _underlyingKZService.ExecuteTask(url, KZHttpMethod.POST, params, headers, callback, message, _bypassSSLVerification);
	}

	/**
	 * Unsubscribe from the channel.Stops receiving messages
	 */
	public void Unsubscribe ()
	{
		try {
			this.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Subscribe to the channel. Start receiving messages
	 * 
	 * @param onMessage The callback with the message from the channel
	 * @param onError The callback with the Exception if there was any
	 */
	public void Subscribe( KZAction<JSONObject> onMessage, KZAction<Exception> onError)
	{
		this.connect();
		_onMessage = onMessage;
		_onError = onError;
	}

	/**
	 * Subscribe to the channel. Start receiving messages
	 * 
	 * @param onMessage The callback with the message from the channel
	 * @param onError The callback with the Exception if there was any
	 * @param onConnect The callback with the result of the connection: True if it was successful
	 */
	public void Subscribe(KZAction<JSONObject> onMessage, KZAction<Exception> onError, KZAction<Boolean> onConnect)
	{
		this._onConnect = onConnect;
		this.Subscribe( onMessage, onError);
	}

	@Override
	public void onClose() {
		Log.d(TAG, "onClose");
	}

	@Override
	public void onIOError(IOException arg0) {
		try {
			_onError.onServiceResponse(arg0);
		} catch (Exception e) {
			this.throwError(e);
		}
	}

	@Override
	public void onMessage(String arg0){
		try {
			int start= arg0.indexOf("::") + 2;
			String message = arg0.substring(start);
			JSONObject jsonMessage = new JSONObject(message);
			_onMessage.onServiceResponse(jsonMessage);
		} catch (Exception e) {
			this.throwError(e);
		}
	}

	private KZAction<Boolean> _onConnect = new KZAction<Boolean>() {
		public void onServiceResponse(Boolean response) throws Exception {
			response = false;
		}
	};

	private void throwError(Exception e) {
		try {
			_onError.onServiceResponse(e);
		} catch (Exception e1) {
			e.printStackTrace();
			this.throwError(e);
		}
	}

	@Override
	public void onOpen() {
		String command = "bindToChannel::{\"application\":\"local\", \"channel\":\"" + _name + "\"}";
		try {
			this.send(command);
			_onConnect.onServiceResponse(true);
		} 
		catch (Exception e) {
			this.throwError(e);	
		}
	}
	
	public  void update(Observable arg0, Object data) {
		Log.d("channel", "token updated");
		_kidozenUser = (KidoZenUser) data;
	}

	public KidoZenUser getKidozenUser() {
		return _kidozenUser;
	}

	public void setKidozenUser(KidoZenUser _kidozenUser) {
		this._kidozenUser = _kidozenUser;
	}

    private void CloneCredentials() {
        _underlyingKZService.SetCredentials(_provider, _username, _password, null);
        _underlyingKZService.SetAuthenticateParameters(_tenantMarketPlace, _application, _identityProviders, _applicationScope, _authServiceScope, _authServiceEndpoint, _ipEndpoint);
    }

    protected void SetAuthenticateParameters(String marketplace, String application, Map<String, JSONObject> providers, String scope, String authScope, String authServiceEndpoint, String ipEndpoint) {
        _identityProviders = providers;
        _tenantMarketPlace = marketplace;
        _applicationScope = scope;
        _authServiceScope = authScope;
        _authServiceEndpoint= authServiceEndpoint;
        _ipEndpoint = ipEndpoint;
        _application = application;

        am = new AuthenticationManager(marketplace, application, providers, scope,  authScope,  authServiceEndpoint,  ipEndpoint, this.tokenUpdater);
        am.bypassSSLValidation = _bypassSSLVerification;
    }

    public void SetCredentials(String providerKey, String username, String password, ServiceEventListener e ){
        this._provider = providerKey;
        this._username = username;
        this._password = password;
        this._authenticateCallback = e;
    }
}