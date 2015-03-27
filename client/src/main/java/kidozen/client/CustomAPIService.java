package kidozen.client;

import org.json.JSONObject;
import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;

public class CustomAPIService extends KZService {

    public CustomAPIService(String baseURL, String name, String provider, String username, String pass, String clientId,
                            KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(baseURL, name, provider,  username, pass, clientId, userIdentity, applicationIdentity);
    }

    public void executeCustomAPI(JSONObject message, final ServiceEventListener callback) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);

        this.ExecuteTask(this.mEndpoint + "/" + this.mName, KZHttpMethod.POST, null, headers, callback, message, getStrictSSL());
    }

}
