package kidozen.client;

import org.apache.http.NameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Utilities {
	static String charset = "UTF-8";
	public static  String convertStreamToString(java.io.InputStream instream) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				instream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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

    /*
	public static Hashtable<String, String> ExecuteHttpDelete(String urlAsString,  
			Hashtable<String, String> requestProperties, 
			HashMap<String,String> params,
			boolean developerMode) throws Exception
			{
		HttpURLConnection con = CreateConnectionThatHandlesRedirects(urlAsString, "DELETE", requestProperties, params, developerMode);
		Hashtable<String, String> retVal = getExecutionResponse(con);
		return retVal;
			}

	public static Hashtable<String, String> ExecuteHttpGet(String urlAsString,  
			Hashtable<String, String> requestProperties, 
			HashMap<String,String> params,
			boolean developerMode) throws Exception
			{
		HttpURLConnection con = CreateConnectionThatHandlesRedirects(urlAsString, "GET", requestProperties, params, developerMode);

		Hashtable<String, String> retVal = getExecutionResponse(con);
		return retVal;
			}

	public static Hashtable<String, String> ExecuteHttpPost(String urlAsString, 
			String bodyAsString,  
			Hashtable<String, String> requestProperties, 
			HashMap<String,String> params,
			boolean developerMode) throws Exception {

		HttpURLConnection con = CreateConnectionThatHandlesRedirects(urlAsString, "POST", requestProperties, params, developerMode);
        con.setDoOutput(true);

		if (bodyAsString!=null) {
			OutputStream os = con.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(bodyAsString);
			writer.close();
			os.close();
		}

		Hashtable<String, String> retVal = getExecutionResponse(con);
		return retVal;
	}

	public static Hashtable<String, String> ExecuteHttpPut(String urlAsString,
			String bodyAsString, Hashtable<String, String> requestProperties,
			HashMap<String, String> params,
			boolean developerMode) throws Exception {
		HttpURLConnection con = CreateConnectionThatHandlesRedirects(urlAsString, "PUT", requestProperties, params, developerMode);

		if (bodyAsString!=null) {
			OutputStream os = con.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(bodyAsString);
			writer.close();
			os.close();
		}

		Hashtable<String, String> retVal = getExecutionResponse(con);

		return retVal;
	}
    */

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

    /*
	public static HttpURLConnection CreateConnectionThatHandlesRedirects(
			String urlAsString, String method, Hashtable<String, String> requestProperties,
			HashMap<String, String> params,
			boolean developerMode) throws MalformedURLException, IOException {
		int responseCode;
		if(params!=null)
			urlAsString = urlAsString + "?" + getQuery(params);
		URL url = new URL(urlAsString);
		HttpURLConnection con = null;

		if (url.getProtocol().toLowerCase().equals("https")) {
			con = CreateSNIConnection(url,method, requestProperties, developerMode);
		} else {
			con = (HttpURLConnection) url.openConnection();
			responseCode = con.getResponseCode();
			if (responseCode==302) {
				urlAsString = con.getHeaderField("Location");
				url = new URL(urlAsString);
				con = CreateSNIConnection(url, method, requestProperties, developerMode);
			}
		}
		return con;
	}

	public static HttpURLConnection CreateSNIConnection(URL url, String method, Hashtable<String, String> requestProperties, boolean developerMode) throws IOException {
        if (developerMode) {
            trustAllHosts();
        }
		HttpsURLConnection secureConnection = (HttpsURLConnection) url.openConnection();
		secureConnection.setRequestMethod(method);
		if (requestProperties!=null)
			for(Entry<String, String> entry : requestProperties.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				secureConnection.setRequestProperty(key, value);
			}
		//con = secureConnection;
		return secureConnection;
	}

	public static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }};

        // Ignore differences between given hostname and certificate hostname
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) { return true; }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
        }
        catch (Exception e) {

        }
    }

	private static Hashtable<String, String> getExecutionResponse(
			HttpURLConnection con) throws IOException {
		Hashtable<String, String> retVal = new Hashtable<String, String>(); 
		int responseCode = con.getResponseCode();
        InputStream in = con.getInputStream();
		retVal.put("statusCode", String.valueOf(responseCode));
		retVal.put("responseMessage", con.getResponseMessage());

		if (responseCode>=HttpStatus.SC_BAD_REQUEST) 			
			retVal.put("responseBody", convertStreamToString(con.getErrorStream()));
		else
			retVal.put("responseBody", convertStreamToString(con.getInputStream()));
		return retVal;
	}
	*/

}
