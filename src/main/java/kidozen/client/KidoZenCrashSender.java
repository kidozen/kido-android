package kidozen.client;

import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created by christian on 3/18/14.
 */
public class KidoZenCrashSender implements ReportSender {
    private String _endpoint;
    private SNIConnectionManager _sniManager;
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String CONTENT_TYPE = "content-type";

    public KidoZenCrashSender (String endpoint) {
        if (!endpoint.endsWith("/")) {
            endpoint = endpoint + "/";
        }
        _endpoint  = endpoint + "api/v3/logging/crash/android/dump";
    }

    @Override
    public void send(CrashReportData errorContent) throws ReportSenderException {
        try {
            Hashtable<String, String> headers = new Hashtable<String, String>();
            headers.put(CONTENT_TYPE,APPLICATION_JSON);
            _sniManager = new SNIConnectionManager(_endpoint, errorContent.toJSON().toString(), headers, null, true);
            Hashtable<String, String> response = _sniManager.ExecuteHttp(KZHttpMethod.POST);
            String body = response.get("responseBody");
            Integer statusCode = Integer.parseInt(response.get("statusCode"));
            if (statusCode>= HttpStatus.SC_MULTIPLE_CHOICES) {
                String exceptionMessage = (body!=null ? body : "Unexpected HTTP Status Code: " + statusCode);
                throw new Exception(exceptionMessage);
            }
        }
        catch (Exception e) {
            throw new ReportSenderException("Error while sending  report to KidoZen services." , e);
        }
    }


}
