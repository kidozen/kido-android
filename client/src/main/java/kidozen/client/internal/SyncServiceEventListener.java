package kidozen.client.internal;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;

/**
 * Created by christian on 8/5/14.
 */
public class SyncServiceEventListener implements ServiceEventListener {
    private JSONObject mJObjectResponse = null;
    private JSONArray  mJArrayResponse = null;
    private String mStringResponse = null;
    private Exception mError = null;
    private CountDownLatch mLatch = null;

    public SyncServiceEventListener(CountDownLatch latch) {
        mLatch = latch;
    }

    @Override
    public void onFinish(ServiceEvent e) {
        if (e.Response instanceof JSONObject)
            mJObjectResponse = (JSONObject) e.Response;
        else if (e.Response instanceof JSONArray)
            mJArrayResponse = (JSONArray) e.Response;
        else if (e.Response instanceof String)
            mStringResponse = (String) e.Response;
        if (e.Exception != null)
            mError = e.Exception;
        mLatch.countDown();
    }

    public JSONObject getJSONResponse() {
        return mJObjectResponse;
    }
    public JSONArray getJARRAYResponse() {
        return mJArrayResponse;
    }
    public String getStringNResponse() {
        return mStringResponse;
    }
    public Exception getError() {
        return mError;
    }
}
