package kidozen.client;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.acra.util.JSONReportBuilder;
import org.json.JSONObject;

/**
 * Created by christian on 3/18/14.
 */
public class CrashReporter extends KZService {
    private Application _hostApplication;
    private String _endpoint;
    private KidoZenCrashSender _crashSender;
    public CrashReporter ACRAReport;
    public JSONObject ErrorContent;

    public CrashReporter(Application application, String endpoint){
        _hostApplication = application;
        _endpoint = endpoint;
        _crashSender = new KidoZenCrashSender(_endpoint);
        ACRA.init(_hostApplication);
        ACRA.getErrorReporter().setReportSender(_crashSender);
    }

 }
