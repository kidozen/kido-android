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

    public CrashReporter(Application application, String endpoint, final ServiceEventListener callback){
        _hostApplication = application;
        _endpoint = endpoint;
        _crashSender = new KidoZenCrashSender(
                this,
                _endpoint,
                callback
        );
        ACRA.init(_hostApplication);
        ACRA.getErrorReporter().addReportSender(_crashSender);
    }

    public CrashReporter(Application application,final ServiceEventListener callback) {
        _hostApplication = application;
        _endpoint = ACRA.getNewDefaultConfig(_hostApplication).formUri();
        ACRA.init(_hostApplication);
        _crashSender = new KidoZenCrashSender(
                this,
                _endpoint,
                callback
        );
        ACRA.getErrorReporter().addReportSender(_crashSender);
    }
 }
