package kidozen.client;

import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;

/**
 * Created by christian on 10/24/14.
 */
public class AnalyticsLog extends KZService {
    private final AnalyticsLog mSelf;

    public AnalyticsLog(String logging, String provider , String username, String pass, String clientId, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(logging,"", provider, username, pass, clientId, userIdentity, applicationIdentity);
        mSelf = this;

    }

    public void Write(final String message,  final ServiceEventListener callback)
    {
        if (mEndpoint.endsWith("/"))
            mEndpoint = mEndpoint.substring(0,mEndpoint.length()-1);
        String logEndpoint = String.format("%s/api/v3/logging/events?level=%s",mEndpoint, LogLevel.LogLevelInfo.ordinal());

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

        new KZServiceAsyncTask(KZHttpMethod.POST, null, headers, message, callback, getStrictSSL()).execute(logEndpoint);
    }

}
