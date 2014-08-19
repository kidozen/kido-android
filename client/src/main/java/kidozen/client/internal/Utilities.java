package kidozen.client.internal;

import android.os.Looper;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import kidozen.client.ServiceEvent;
import kidozen.client.ServiceResponseHandler;

public class Utilities {

    private static final String TAG = "Utilities";

    public static  String convertStreamToString(java.io.InputStream instream) throws IOException{

		BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
            instream.close();
		}
        catch (IOException e) {
			throw e;
		}
		return sb.toString();
	}

	public static String createHash(String value) {
		String hash = null;
		byte[] bytes = value.getBytes();
		Checksum crc32 = new CRC32();
		crc32.reset();
		crc32.update(bytes, 0, bytes.length);
		hash = Long.toHexString(crc32.getValue()).toUpperCase();
		return hash;
	}

	public static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (NameValuePair pair : params)
		{
			if (first)
				first = false;
			else
				result.append("&");
			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}

	public static String getQuery(HashMap<String, String> params) throws UnsupportedEncodingException
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;
		Iterator<Entry<String, String>> it = params.entrySet().iterator();
		while (it.hasNext()) {
			if (first)
				first = false;
			else
				result.append("&");
			Entry<String, String> pair = it.next();
			result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
			it.remove();
		}
		return result.toString();
	}

    public static void CheckFaultsInResponse(final String response) throws Exception {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput( new StringReader( response ) );
        int eventType = xpp.getEventType();
        boolean faultBegin = false;
        boolean faultEnd = false;
        String faultMessage = "Identity Provider Error.\n";
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_TAG && !faultBegin) {
                faultBegin = xpp.getName().toLowerCase().contains("s:fault");
            } else if(eventType == XmlPullParser.END_TAG && faultBegin) {
                faultEnd = xpp.getName().toLowerCase().contains("s:fault");
            }
            else if(eventType == XmlPullParser.TEXT && faultBegin && !faultEnd) {
                faultMessage += xpp.getText() + ".";
            }
            eventType = xpp.next();
        }

        if (faultBegin)
            throw new IllegalArgumentException(faultMessage);
    }

    public static String MapAsQueryString(Map<String, Object> map, Boolean isChild, String parentKey) {
        StringBuilder retVal = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                retVal.append(MapAsQueryString((Map) value, true, entry.getKey()));
            }
            else {
                if (!isChild)
                    retVal.append(entry.getKey()).append("=").append(EncodeUTF8(value.toString())).append("&");
                else
                    retVal.append(parentKey).append("[").append(entry.getKey()).append("]=").append(EncodeUTF8(value.toString())).append("&");
            }
        }
        return retVal.toString();
    }

    private static String EncodeUTF8(String value) {
        String retVal = value;
        try {
            retVal = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, e.getMessage());
        }
        return retVal;
    }

    public static void DispatchServiceResponseListener(final ServiceEvent e,final ServiceResponseHandler callback) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (e.StatusCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
                    callback.onError(e.StatusCode, e.Body);
                }
                else {
                    if (e.Response instanceof JSONObject) {
                        JSONObject o = (JSONObject) e.Response;
                        callback.onSuccess(e.StatusCode, o);

                    } else if (e.Response instanceof JSONArray) {
                        JSONArray o = (JSONArray) e.Response;
                        callback.onSuccess(e.StatusCode, o);
                    } else
                        callback.onSuccess(e.StatusCode, e.Body);
                }
            }
        };
        dispatchOnThread(r);
    }

    public static void DispatchServiceStartListener(final ServiceResponseHandler callback) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                callback.onStart();
            }
        };
        dispatchOnThread(r);
    }

    private static void dispatchOnThread(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new Thread(r).start();
        } else {
            r.run();
        }
    }

    public static String GetInvalidParameterMessage(String arg) {
        return String.format("invalid '%s' value",arg);
    }
}
