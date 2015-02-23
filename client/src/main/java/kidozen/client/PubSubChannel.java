    package kidozen.client;

    import android.util.Log;

    import com.github.nkzawa.emitter.Emitter;
    import com.github.nkzawa.engineio.client.Transport;
    import com.github.nkzawa.engineio.parser.Packet;
    import com.github.nkzawa.socketio.client.IO;
    import com.github.nkzawa.socketio.client.Manager;
    import com.github.nkzawa.socketio.client.Socket;

    import org.apache.http.HttpStatus;
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
        private static final String KIDO_BINDTOCHANNEL = "bindToChannel";

        private String mPubSubEndpoint;
        private ServiceEventListener mMessagesCallback;
        private ServiceEventListener _apiCallback;
        private Socket mSocket = null;

        private String mChannel = "";
        private String mKidoApplicationName = "";

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
            _apiCallback = callback;

            mSocket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Transport transport = (Transport)args[0];
                    transport.on(Transport.EVENT_OPEN, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            //Map<String, String> headers = (Map<String, String>) args[0];
                            //headers.put(Constants.AUTHORIZATION_HEADER, mUserIdentity.Token);
                            String bindMessage =  "{ \"application\": \"" + mKidoApplicationName + "\", \"channel\": \"" + mChannel + "\" }";
                            mSocket.emit(KIDO_BINDTOCHANNEL,bindMessage);

                        }
                    });
                    transport.on(Transport.EVENT_PACKET, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Packet data = (Packet)args[0];

                            Log.i(TAG, String.valueOf(data.data));

                        }
                    });
                }
            });

            mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.d(TAG, args.toString());
                }

            });

            mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    processConnectionError(args);
                }
            });

            mSocket.on(KIDO_BIND_ACCEPTED, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    processBindAccepted(args);
                }
            });


            mSocket.on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (mMessagesCallback!=null)
                    {
                        ServiceEvent event = new ServiceEvent(this, HttpStatus.SC_OK, args.toString(), args);
                        mMessagesCallback.onFinish(event);
                    }
                }
            });

            mSocket.connect();
            _apiCallback = callback;
        }

        private void processConnectionError(Object[] args) {
            String errorMessage = "Unknown Error";
            if (args.length>0) {
                errorMessage = (String)args[0];
            }
            if (_apiCallback!=null)
                _apiCallback.onFinish(new ServiceEvent(this,HttpStatus.SC_INTERNAL_SERVER_ERROR,errorMessage,null ));
        }

        private void processBindAccepted(Object[] args) {
            if (args.length==1) {
                try {
                    JSONObject jResponse = (JSONObject)args[0];
                    String theResponseChannel = jResponse.getString("responseChannelName");
                } catch (JSONException e) {
                    processConnectionError(new Object[] {e.getMessage()});
                }
                if (_apiCallback!=null)
                    _apiCallback.onFinish(new ServiceEvent(this,HttpStatus.SC_OK, "Connected to channel",null ));
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


        public void SetChannelMessageListener(ServiceEventListener callback) {
            mMessagesCallback = callback;
        }

        public void setApplicationName(String applicationName) {
            mKidoApplicationName = applicationName;
        }
    }
