    package kidozen.client;

    import android.util.Log;

    import com.github.nkzawa.emitter.Emitter;
    import com.github.nkzawa.engineio.client.Transport;
    import com.github.nkzawa.engineio.parser.Packet;
    import com.github.nkzawa.socketio.client.IO;
    import com.github.nkzawa.socketio.client.Manager;
    import com.github.nkzawa.socketio.client.Socket;

    import org.apache.http.HttpStatus;
    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

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
        private static final String KIDO_BIND_ACCEPTED = "bindAccepted";
        private static final String KIDO_BIND_TO_CHANNEL = "bindToChannel";

        private String mPubSubEndpoint;
        private ServiceEventListener mMessagesCallback;
        private ServiceEventListener mApiCallback;
        private Socket mSocket = null;

        private String mChannel = "";
        private String mKidoApplicationName = "";
        private String mResponseChannelName = "*none*";

        /**
         * Constructor
         * You should not create a new instances of this constructor. Instead use the PubSubChannel() method of the KZApplication object.
         *
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
        public PubSubChannel(String ep, String wsEndpoint, String name, String provider, String username, String pass, String clientId, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
            super(ep, name, provider, username, pass, clientId, userIdentity, applicationIdentity);
            mChannel = name;
            mPubSubEndpoint = ep;
        }

        /**
         * Subscribes the specified callback in the current channel
         *
         * @param callback
         * @throws URISyntaxException
         */
        public void Subscribe(final ServiceEventListener callback) throws URISyntaxException {
            String ep = mPubSubEndpoint.replace("/local", "");
            mSocket = IO.socket(ep);
            mApiCallback = callback;

            mSocket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Transport transport = (Transport) args[0];
                    transport.on(Transport.EVENT_OPEN, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            //Map<String, String> headers = (Map<String, String>) args[0];
                            //headers.put(Constants.AUTHORIZATION_HEADER, mUserIdentity.Token);
                            String bindMessage = "{ \"application\": \"" + mKidoApplicationName + "\", \"channel\": \"" + mChannel + "\" }";
                            mSocket.emit(KIDO_BIND_TO_CHANNEL, bindMessage);

                        }
                    });
                    transport.on(Transport.EVENT_PACKET, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Packet data = (Packet) args[0];
                            if (data.type.equals("message") && data.data != null) {

                                JSONObject messageAsJson = parseMessage(data.data);
                                if (messageAsJson != null) {
                                    ServiceEvent event = new ServiceEvent(this, HttpStatus.SC_OK, messageAsJson.toString(), messageAsJson);
                                    mMessagesCallback.onFinish(event);
                                }
                            }
                            //Log.i(TAG, String.valueOf(data.data));
                        }
                    });
                }
            });

            mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, Socket.EVENT_DISCONNECT);
                }

            });

            mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (mApiCallback != null)
                        mApiCallback.onFinish(new ServiceEvent(this, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Socket connection error.", null));
                }
            });

            mSocket.on(KIDO_BIND_ACCEPTED, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject jResponse = (JSONObject) args[0];
                    try {
                        mResponseChannelName = jResponse.getString("responseChannelName");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            mSocket.connect();
            mApiCallback = callback;
        }

        /*
        * The  packet has the following format:
        *
        * 2/pubsub,["channel name (stored in mResponseChannelName) ", JSONObject]
        *
        * */
        private JSONObject parseMessage(Object data) {
            int messageInData = data.toString().lastIndexOf(mResponseChannelName);

            if (messageInData > 0) {
                int startArrayPosition = data.toString().indexOf("[");
                int closeArrayPosition = data.toString().lastIndexOf("]") +1;
                String rawMessage = data.toString().substring(startArrayPosition, closeArrayPosition);
                try {
                    JSONArray messageAsArray = new JSONArray(rawMessage);
                    // POSITION 0 has ChannelName
                    // POSITION 1 has the message
                    return messageAsArray.getJSONObject(1);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else {
                return null;
            }
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
            String  url = mEndpoint + "/" + mChannel;
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

        public void Unsusbcribe() {
            mSocket.disconnect();
        }

        public void SetChannelMessageListener(ServiceEventListener callback) {
            mMessagesCallback = callback;
        }

        public void setApplicationName(String applicationName) {
            mKidoApplicationName = applicationName;
        }
    }
