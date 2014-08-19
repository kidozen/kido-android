package kidozen.client.internal;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;
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

    private Object mServiceEventResponse = null;
    private int mStatusCode;


    public SyncServiceEventListener(CountDownLatch latch) {
        mLatch = latch;
    }

    @Override
    public void onFinish(ServiceEvent e) {
        //System.out.println("SyncServiceEventListener, Status, " + String.valueOf(e.StatusCode));
        //System.out.println("SyncServiceEventListener, Body, " + String.valueOf(e.Body));

        mStatusCode = e.StatusCode;
        if (mStatusCode >= HttpStatus.SC_BAD_REQUEST ) {
            mStringResponse = e.Body;
            mError = e.Exception;
        }
        else
        {
            mServiceEventResponse = e.Response;
            if (e.Response instanceof JSONObject)
                mJObjectResponse = (JSONObject) e.Response;
            else if (e.Response instanceof JSONArray)
                mJArrayResponse = (JSONArray) e.Response;
            else if (e.Response instanceof String)
                mStringResponse = e.Response.toString();
        }
        mLatch.countDown();
    }
    public Object getServiceResponse(){
        return mServiceEventResponse;
    }
    public JSONObject getJSONResponse() {
        return mJObjectResponse;
    }
    public JSONArray getJARRAYResponse() {
        return mJArrayResponse;
    }
    public String getStringResponse() {

        return mStringResponse;
    }
    public Exception getError() {
        return mError;
    }

    public int getStatusCode() {
        return mStatusCode;
    }
}
