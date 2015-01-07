package kidozen.client;

import org.apache.http.HttpStatus;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;
import kidozen.client.internal.SyncHelper;

/**
 * Publish and subscribe service interface
 *
 * @author kidozen
 * @version 1.00, April 2013
 */
public class PubSubChannel extends KZService {
    private static final String TAG = "PubSubChannel";
    private String mWsEndpoint;
    private ServiceEventListener mMessagesCallback;
    private ServiceEventListener mSubscribeCallback;
    WebSocketClient mClient;
    private static String mBindCommand  = "bindToChannel::{\"application\":\"local\", \"channel\":\"%s\"}";
    
    
    TrustManager[] mDefaultTrustsManagers = new TrustManager[]{
            new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    System.out.println("checkClientTrusted");
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    System.out.println("checkServerTrusted");
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }
            }
    };
    
    /**
     * Constructor
     * You should not create a new instances of this constructor. Instead use the PubSubChannel() method of the KZApplication object.
     * @param ep
     * @param wsEndpoint
     * @param name
     * @param provider
     * @param username
     * @param pass
     * @param clientId
     * @param userIdentity
     * @param applicationIdentity
     */
    public PubSubChannel(String ep, String wsEndpoint, String name,String provider , String username, String pass, String clientId, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(ep,name, provider, username, pass, clientId, userIdentity, applicationIdentity);
        mWsEndpoint = wsEndpoint;
    }

    /**
     * Subscribes the specified callback in the current channel
     *
     * @param callback
     * @throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException
     */
    public void Subscribe(final ServiceEventListener callback) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException  {
        connectWebSocket(mWsEndpoint);
        mSubscribeCallback = callback;
    }

    private void connectWebSocket(String ep) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        WebSocketClientA.setTrustManagers(mDefaultTrustsManagers);
        SSLContext sslContext = null;
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init( null, mDefaultTrustsManagers, null );
        
        mClient = new WebSocketClient(new URI(ep)) {
            @Override
            public void onOpen(ServerHandshake handShakeData) {
                System.out.println("onOpen");
                String bindMessage = String.format(mBindCommand,mName);
                mClient.send(bindMessage);
                mSubscribeCallback.onFinish(new ServiceEvent(this,HttpStatus.SC_OK,bindMessage,null));
            }

            @Override
            public void onMessage(String message) {
                System.out.println("onMessage: " + message);
                mMessagesCallback.onFinish(new ServiceEvent(this,HttpStatus.SC_OK,message, null));
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("onClose");
            }

            @Override
            public void onError(Exception ex) {
                System.out.println("onError");
                mSubscribeCallback.onFinish(new ServiceEvent(this,HttpStatus.SC_INTERNAL_SERVER_ERROR,"",null));
            }
        };
        mClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));
        mClient.connect();
    }

    /**
     * Publish a new message in the current channel
     *
     * @param message the message to push
     * @param isPrivate mark the message as private
     * @param callback The ServiceEventListener callback with the operation results
     */
    public void Publish(final JSONObject message, final boolean isPrivate, final ServiceEventListener callback)
    {
        String  url = mEndpoint + "/" + mName;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("isPrivate", (isPrivate ? "true" : "false"));
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);
        new KZService.KZServiceAsyncTask(KZHttpMethod.POST, params, headers, message, callback, getStrictSSL()).execute(url);
    }

    public boolean Publish(JSONObject message, boolean isPrivate) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper<String>(this, "Publish", JSONObject.class, boolean.class, ServiceEventListener.class);
        helper.Invoke(new Object[]{message, isPrivate});
        return (helper.getStatusCode() == HttpStatus.SC_CREATED);
    }


    public void GetMessages(ServiceEventListener callback) {
        mMessagesCallback = callback;
    }

}
