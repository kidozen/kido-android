package kidozen.client.internal;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.ServiceResponseHandler;

/**
* Created by christian on 8/14/14.
*/
public class EAPIEventListener implements ServiceEventListener {
    private ServiceEventListener mOriginalCaller;
    public EAPIEventListener(ServiceEventListener originalCaller) {
        mOriginalCaller = originalCaller;
    }

    @Override
    public void onFinish(ServiceEvent e) {
        if (mOriginalCaller!=null) {
            ServiceEvent event = null;
            if (e.StatusCode >= HttpStatus.SC_BAD_REQUEST) {
                event = createErrorEvent(e, e.Body);
                if (mOriginalCaller instanceof ServiceResponseHandler)
                    dispatchServiceResponseListener(event, (ServiceResponseHandler) mOriginalCaller);
                else
                    mOriginalCaller.onFinish(event);
            }
            else {
                JSONObject response = (JSONObject) e.Response;
                String body = response.optString("data");
                if (body != null) {
                    event = createServiceEvent(e, body);
                } else {
                    body = response.optString("error");
                    event = createErrorEvent(e, body);
                }

                if (mOriginalCaller instanceof ServiceResponseHandler)
                    dispatchServiceResponseListener(event, (ServiceResponseHandler) mOriginalCaller);
                else
                    mOriginalCaller.onFinish(event);
            }
        }
    }

    private ServiceEvent createErrorEvent (ServiceEvent e, String body) {
        ServiceEvent event = new ServiceEvent(e.Body);

        if (body.isEmpty() || body==null) {
            String error = "'data' or 'error' property not found. Please contact service administrator";
            event = new ServiceEvent(this, HttpStatus.SC_BAD_REQUEST, null, error, new Exception(error));
        }
        else {
            try {
                Object json = new JSONTokener(body).nextValue();
                if (json instanceof JSONObject) {
                    //System.out.println("JsonEventListener, Setting a new JSONObject" );
                    JSONObject theObject = new JSONObject(body);
                    event = new ServiceEvent(this, e.StatusCode, body, theObject);
                } else if (json instanceof String) {
                    //System.out.println("JsonEventListener, Setting a new String" );
                    event = new ServiceEvent(this, e.StatusCode, body, body);
                }
            } catch (JSONException ex) {
                event = new ServiceEvent(this, HttpStatus.SC_BAD_REQUEST, null, ex.getMessage(), ex);
            }
        }
        return event;
    }

    private ServiceEvent createServiceEvent(ServiceEvent e, String body) {
        ServiceEvent event = new ServiceEvent(e.Response);
        try {
            Object json = new JSONTokener(body).nextValue();
            if (json instanceof JSONObject) {
                //System.out.println("JsonEventListener, doInBackground,  Setting a new JSONObject" );
                JSONObject theObject = new JSONObject(body);
                event = new ServiceEvent(this, e.StatusCode, body, theObject);
            }
            else if (json instanceof JSONArray) {
                //System.out.println("JsonEventListener, doInBackground,  Setting a new JSONArray" );
                JSONArray theObject = new JSONArray(body);
                event = new ServiceEvent(this, e.StatusCode, body, theObject);
            }
        }
        catch(JSONException ex) {
            event = new ServiceEvent(this, HttpStatus.SC_BAD_REQUEST, null, ex.getMessage(), ex);
        }
        return event;
    }

    private void dispatchServiceResponseListener(final ServiceEvent e,final ServiceResponseHandler callback) {
        if (e.StatusCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
            callback.onError(e.StatusCode, e.Body);
        }
        else {
            try {
                Object json = new JSONTokener(e.Body).nextValue();
                if (json instanceof JSONObject) {
                    JSONObject theObject = new JSONObject(e.Body);
                    callback.onSuccess(e.StatusCode, theObject);
                }
                else if (json instanceof JSONArray) {
                    JSONArray o = (JSONArray) e.Response;
                    callback.onSuccess(e.StatusCode, o);
                }
            } catch (JSONException e1) {
                callback.onError(e.StatusCode, e1.getMessage());
            }
        }
    }


}
