package kidozen.client;

import android.util.Log;

import org.apache.http.HttpStatus;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    public static final String MESSAGE_CALLBACK_NOT_SET = "Message Callback has not been set. Check if you are using 'GetMessages' method in your application ";
    private String _wsEndpoint;
    private ServiceEventListener _messagesCallback;
    private ServiceEventListener _apiCallback;

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
        _wsEndpoint = wsEndpoint;
    }

    String ep = "wss://christianmbair.localhost.com:8080";//"wss://kidowebsocket-tasks-testssl.kidocloud.com"; //"wss://echo.websocket.org"; //
    List<BasicNameValuePair> extraHeaders = Arrays.asList(
            new BasicNameValuePair("Cookie", "session=abcd")
    );

    final WebSocketClient client = new WebSocketClient(URI.create(ep), new WebSocketClient.Listener() {
        @Override
        public void onConnect() {
            System.out.println("Connected!");
            _apiCallback.onFinish(new ServiceEvent(this,200,"200",this));
        }

        @Override
        public void onMessage(String message) {
            System.out.println(String.format("Got string message! %s", message));
        }

        @Override
        public void onMessage(byte[] data) {
            System.out.println("Got binary message!");
        }

        @Override
        public void onDisconnect(int code, String reason) {
            System.out.println( String.format("Disconnected! Code: %d Reason: %s", code, reason));
        }

        @Override
        public void onError(Exception error) {
            System.out.println("Error!" + error.getMessage().toString());
        }
    }, extraHeaders);



    /**
     * Subscribes the specified callback in the current channel
     *
     * @param callback
     * @throws URISyntaxException
     */
    public void Subscribe(final ServiceEventListener callback) throws URISyntaxException {
        connectWebSocket(ep);
        _apiCallback = callback;

    }

    private void connectWebSocket(String ep) {

        WebSocketClient.setTrustManagers(new TrustManager[] {
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
        });

        client.connect();
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
        client.send("test message");
        new KZService.KZServiceAsyncTask(KZHttpMethod.POST, params, headers, message, callback, getStrictSSL()).execute(url);
    }

    public boolean Publish(JSONObject message, boolean isPrivate) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper<String>(this, "Publish", JSONObject.class, boolean.class, ServiceEventListener.class);
        helper.Invoke(new Object[]{message, isPrivate});
        return (helper.getStatusCode() == HttpStatus.SC_CREATED);
    }


    public void GetMessages(ServiceEventListener callback) {
        _messagesCallback = callback;
    }

}
