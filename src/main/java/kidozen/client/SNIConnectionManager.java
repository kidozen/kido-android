package kidozen.client;

import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 5/20/13
 * Time: 10:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class SNIConnectionManager
{
    public boolean ProcessAsStream = false;
    public int LastResponseCode;
    public String LastResponseMessage;
    public String LastResponseBody;

    String _urlAsString;
    Hashtable<String, String> _requestProperties;
    HashMap<String,String> _params;
    boolean _developerMode;
    String _bodyAsString;
    InputStream _bodyAsStream;

    public SNIConnectionManager (String urlAsString, String message, Hashtable<String, String> requestProperties, HashMap<String,String> params, boolean developerMode)
    {
        _urlAsString = urlAsString;
        _requestProperties = requestProperties;
        _params = params;
        _developerMode = developerMode;
        _bodyAsString = message;
    }

    public SNIConnectionManager (String urlAsString, InputStream message, Hashtable<String, String> requestProperties, HashMap<String,String> params, boolean developerMode)
    {
        _urlAsString = urlAsString;
        _requestProperties = requestProperties;
        _params = params;
        _developerMode = developerMode;
        _bodyAsString = null;
        _bodyAsStream = message;
    }

    public OutputStream ExecuteHttpAsStream(KZHttpMethod method) throws Exception
    {
        HttpURLConnection con = CreateConnectionThatHandlesRedirects(method);
        if (method ==KZHttpMethod.POST || method ==KZHttpMethod.PUT && _bodyAsStream!=null) {
            con.setDoOutput(true);

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1*1024*1024;

            DataOutputStream dos = new DataOutputStream( con.getOutputStream() );
            bytesAvailable = _bodyAsStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            bytesRead = _bodyAsStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0)
            {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = _bodyAsStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = _bodyAsStream.read(buffer, 0, bufferSize);
            }
            _bodyAsStream.close();
            dos.flush();
            dos.close();
        }


        LastResponseCode = con.getResponseCode();
        LastResponseMessage = con.getResponseMessage();

        if (LastResponseCode>= HttpStatus.SC_BAD_REQUEST)
        {
            LastResponseBody = Utilities.convertStreamToString(con.getErrorStream());
            return null;
        }
        else
        {
            int dataread = 0;
            int count = 0;
            int CHUNK_SIZE = 8192;                   // TCP/IP packet size
            byte[] dataChunk = new byte[CHUNK_SIZE]; // byte array for storing temporary data.

            OutputStream fos = new ByteArrayOutputStream();
            //BufferedOutputStream bos = new BufferedOutputStream(fos);

            BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
            InputStreamReader isr = new InputStreamReader( bis );
            //BufferedReader br = new BufferedReader( isr );
            while (dataread >= 0)
            {
                count++;
                dataread = bis.read(dataChunk,0,CHUNK_SIZE);
                System.out.print(".");
                // only write out if there is data to be read
                if ( dataread > 0 )
                    fos.write(dataChunk,0,dataread);
            }
            bis.close();
            fos.close();
            return fos;
        }
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


    protected HttpURLConnection CreateConnectionThatHandlesRedirects(KZHttpMethod method) throws  IOException, NoSuchAlgorithmException, KeyManagementException {
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

    protected HttpURLConnection CreateSNIConnection(String url, KZHttpMethod method) throws IOException, NoSuchAlgorithmException, KeyManagementException {
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

    protected void trustAllHosts() throws NoSuchAlgorithmException, KeyManagementException{
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
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
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
