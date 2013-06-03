package kidozen.client;

import org.apache.http.HttpStatus;

import javax.net.ssl.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 5/20/13
 * Time: 10:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class SNIConnectionManager
{
    String _urlAsString;
    Hashtable<String, String> _requestProperties;
    HashMap<String,String> _params;
    boolean _developerMode;
    String _bodyAsString=null;

    public SNIConnectionManager (String urlAsString, String bodyAsString, Hashtable<String, String> requestProperties, HashMap<String,String> params, boolean developerMode)
    {
        _urlAsString = urlAsString;
        _requestProperties = requestProperties;
        _params = params;
        _developerMode = developerMode;
        _bodyAsString = bodyAsString;
    }

    public Hashtable<String, String> ExecuteHttp(KZHttpMethod method) throws Exception
    {
        HttpURLConnection con = CreateConnectionThatHandlesRedirects(method);
        if (method ==KZHttpMethod.POST || method ==KZHttpMethod.PUT && _bodyAsString!=null) {
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(_bodyAsString);
            writer.close();
            os.close();
        }

        return getExecutionResponse(con);
    }


    protected HttpURLConnection CreateConnectionThatHandlesRedirects(KZHttpMethod method) throws MalformedURLException, IOException {
        if(_params!=null) {
            _urlAsString = _urlAsString + "?" + Utilities.getQuery(_params);
        }

        URL url = new URL(_urlAsString);

        if (url.getProtocol().toLowerCase().equals("https")) {
            return CreateSNIConnection(_urlAsString, method);
        }

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        if (con.getResponseCode()==302) {
            con = CreateSNIConnection(con.getHeaderField("Location"), method);
        }

        return con;
    }

    protected HttpURLConnection CreateSNIConnection(String url, KZHttpMethod method) throws IOException {
        if (_developerMode) {
            trustAllHosts();
        }
        URL connectionUrl = new URL(url);
        HttpsURLConnection secureConnection = (HttpsURLConnection) connectionUrl.openConnection();
        secureConnection.setRequestMethod(method.toString());

        if (_requestProperties!=null) {
            for (Map.Entry<String, String> entry : _requestProperties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                secureConnection.setRequestProperty(key, value);
            }
        }
        return secureConnection;
    }

    protected void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager()
                {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[] {};
                    }

                    public void checkClientTrusted(X509Certificate[] chain,String authType) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] chain,String authType) throws CertificateException {
                    }
                }
        };
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Hashtable<String, String> getExecutionResponse( HttpURLConnection con) throws IOException {
        Hashtable<String, String> retVal = new Hashtable<String, String>();
        int responseCode = con.getResponseCode();
        retVal.put("statusCode", String.valueOf(responseCode));
        retVal.put("responseMessage", con.getResponseMessage());

        if (responseCode>= HttpStatus.SC_BAD_REQUEST)
            retVal.put("responseBody", Utilities.convertStreamToString(con.getErrorStream()));
        else
            retVal.put("responseBody", Utilities.convertStreamToString(con.getInputStream()));
        return retVal;
    }
}
