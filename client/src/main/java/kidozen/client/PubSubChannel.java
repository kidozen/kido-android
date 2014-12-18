package kidozen.client;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Transport;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

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


    /**
     * Subscribes the specified callback in the current channel
     *
     * @param callback
     * @throws URISyntaxException
     */
    public void Subscribe(final ServiceEventListener callback) throws URISyntaxException {
        String ep = _wsEndpoint.replace("wss://", "https://")  + mName;
        final Socket socket = IO.socket(ep);

        socket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Transport transport = (Transport)args[0];
                transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Map<String, String> headers = (Map<String, String>) args[0];
                        headers.put(Constants.AUTHORIZATION_HEADER, mUserIdentity.Token);
                    }
                });
            }
        });

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                socket.emit("foo", "hi");
                socket.disconnect();
            }

        }).on("event", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d(TAG,args.toString());
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d(TAG,args.toString());
            }

        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG,args.toString());
            }
        });
        socket.connect();
        _apiCallback = callback;
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
        _messagesCallback = callback;
    }

}
