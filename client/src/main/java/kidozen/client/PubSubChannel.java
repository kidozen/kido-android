package kidozen.client;

import android.util.Log;

import com.netiq.websocket.WebSocketClient;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

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
    private WSClient _wsClient;
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
        helper.Invoke(new Object[]{message , isPrivate});
        return (helper.getStatusCode() == HttpStatus.SC_CREATED);
    }

    /**
     * Subscribes the specified callback in the current channel
     *
     * @param callback
     * @throws URISyntaxException
     */
    public void Subscribe(final ServiceEventListener callback) throws URISyntaxException {
        _wsClient = new WSClient(new URI(_wsEndpoint), mName);
        _wsClient.connect();
        _apiCallback = callback;
    }

    /**
     * Ends the current subscription to the channel
     */
    public void Unsubscribe() {
        try
        {
            _wsClient.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void GetMessages(ServiceEventListener callback) {
        _messagesCallback = callback;
    }


    private class WSClient extends WebSocketClient {
        private String _name;

        public WSClient(URI serverURI, String name) {
            super(serverURI);
            _name = name;
        }

        @Override
        public void onMessage(String wsMessage) {
            try
            {
                int start= wsMessage.indexOf("::") + 2;
                String message = wsMessage.substring(start);
                JSONObject jsonMessage = new JSONObject(message);
                NotifyApiCallback( jsonMessage, _messagesCallback);
            }
            catch (Exception e) {
                ExecuteCallBackWithException(e, _messagesCallback);
            }
        }

        @Override
        public void onIOError(IOException ex) {
            JSONObject jsonMessage = null;
            try
            {
                jsonMessage = new JSONObject()
                        .put("result","IOError")
                        .put("Exception", ex);
                NotifyApiCallback( jsonMessage, _messagesCallback);

            }
            catch (JSONException e) {
                ExecuteCallBackWithException(e, _messagesCallback);
            }
        }

        @Override
        public void onClose() {
            JSONObject jsonMessage = null;
            try
            {
                jsonMessage = new JSONObject()
                        .put("result", "onClose success");
                NotifyApiCallback( jsonMessage, _messagesCallback);
            }
            catch (JSONException e) {
                ExecuteCallBackWithException(e, _messagesCallback);
            }
        }

        @Override
        public void onOpen() {
            String command = String.format("bindToChannel::{\"application\":\"local\", \"channel\":\"%s\"}",_name);
            try
            {
                this.send(command);
                //Notifies API Callback Success
                NotifyApiCallback( new JSONObject().put("result", "onOpen success"), _apiCallback);
            }
            catch (Exception e) {
                ExecuteCallBackWithException(e, _apiCallback);
            }
        }


        private void NotifyApiCallback(JSONObject o, ServiceEventListener apiCallback) {
            if (apiCallback!=null) {
                ServiceEvent evt = new ServiceEvent(this,HttpStatus.SC_OK, o.toString(),o);
                apiCallback.onFinish(evt);
            }
            else {
                Log.i(TAG, MESSAGE_CALLBACK_NOT_SET);
            }
        }

        private void ExecuteCallBackWithException(Exception e, ServiceEventListener apiCallback) {
            e.printStackTrace();
            if (apiCallback!=null) {
                ServiceEvent evt = new ServiceEvent(this, HttpStatus.SC_BAD_REQUEST, e.getMessage(), null ,e);
                apiCallback.onFinish(evt);            }
            else {
                Log.i(TAG, MESSAGE_CALLBACK_NOT_SET);
            }
        }
    }
}
