package kidozen.client;

import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import java.util.HashMap;

/**
 * Created by christian on 3/18/14.
 */
public class KidoZenCrashSender implements ReportSender {
    private KZService _service;
    private String _endpoint;
    private ServiceEventListener _callback ;
    private CrashDefaultServiceEventListener _defaultEventListener = new CrashDefaultServiceEventListener()  ;

    public ServiceEvent GetEventInfo() {
        return  _defaultEventListener.EventInfo;
    }

    public KidoZenCrashSender (KZService client, String endpoint, ServiceEventListener callback) {
        if (!endpoint.endsWith("/")) {
            endpoint = endpoint + "/";
        }
        _endpoint  = endpoint + "api/v3/logging/crash/android/dump";
        _service = client;
        if (callback!=null)
        {
            _callback = callback;
        }
        else
        {
            _callback = _defaultEventListener;
        }
    }

    @Override
    public void send(CrashReportData errorContent) throws ReportSenderException {
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        try {
            _service.ExecuteTask(_endpoint, KZHttpMethod.POST, params, headers, _callback, errorContent.toJSON(), true);
        }
        catch (Exception e) {
            throw new ReportSenderException("Error while sending  report to KidoZen services." , e);
        }
    }

    private static class CrashDefaultServiceEventListener implements ServiceEventListener {
        public ServiceEvent EventInfo;
        public CrashDefaultServiceEventListener() {
        }
        @Override
        public void onFinish(ServiceEvent e) {
            EventInfo  = e;
        }
    }
}
