package kidozen.client.crash;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import kidozen.client.KZService;
import kidozen.client.authentication.IdentityManager;

/**
 * Created by christian on 3/18/14.
 */
public class CrashReporter extends KZService {
    private static CrashReporter INSTANCE;
    private static Application _hostApplication;
    private String _endpoint;
    private static ErrorReporter errorReporterSingleton;

    public static final boolean DEV_LOGGING = false; // Should be false for
    // release.
    public static final String LOG_TAG = CrashReporter.class.getSimpleName();

    public static ACRALog log = new AndroidLogDelegate();

    /**
     * The key of the application default SharedPreference where you can put a
     * 'true' Boolean value to disable CrashReporter.
     */
    public static final String PREF_DISABLE_ACRA = "acra.disable";

    /**
     * Alternatively, you can use this key if you prefer your users to have the
     * checkbox ticked to enable crash reports. If both acra.disable and
     * acra.enable are set, the value of acra.disable takes over the other.
     */
    public static final String PREF_ENABLE_ACRA = "acra.enable";

    /**
     * The key of the SharedPreference allowing the user to disable sending
     * content of logcat/dropbox. System logs collection is also dependent of
     * the READ_LOGS permission.
     */
    public static final String PREF_ENABLE_SYSTEM_LOGS = "acra.syslog.enable";

    /**
     * The key of the SharedPreference allowing the user to disable sending his
     * device id. Device ID collection is also dependent of the READ_PHONE_STATE
     * permission.
     */
    public static final String PREF_ENABLE_DEVICE_ID = "acra.deviceid.enable";

    /**
     * The key of the SharedPreference allowing the user to always include his
     * email address.
     */
    public static final String PREF_USER_EMAIL_ADDRESS = "acra.user.email";

    /**
     * The key of the SharedPreference allowing the user to automatically accept
     * sending reports.
     */
    public static final String PREF_ALWAYS_ACCEPT = "acra.alwaysaccept";

    /**
     * The version number of the application the last time ACRA was started.
     * This is used to determine whether unsent reports should be discarded
     * because they are old and out of date.
     */
    public static final String PREF_LAST_VERSION_NR = "acra.lastVersionNr";

    public CrashReporter(Application application, String endpoint) {
        super();
        _hostApplication = application;
        if (!endpoint.endsWith("/")) {
            endpoint = endpoint + "/";
        }
        _endpoint  = endpoint + "api/v3/logging/crash/android/dump";
        ACRAConfiguration conf = getConfig();
        conf.setFormUri(_endpoint);

        final ErrorReporter errorReporter = new ErrorReporter(_hostApplication,
                _hostApplication.getSharedPreferences(conf.sharedPreferencesName(), conf.sharedPreferencesMode()),
                true);

        // Append ReportSenders.
        errorReporter.setDefaultReportSenders();
        errorReporterSingleton = errorReporter;
    }


    private CrashReporter() {
    }

    private static void createInstance() {
        if (INSTANCE == null) {
            // synchronized to avoid possible  multi-thread issues
            synchronized(IdentityManager.class) {
                // must check for null again
                if (INSTANCE == null) {
                    INSTANCE = new CrashReporter();
                }
            }
        }
    }

    public static CrashReporter getInstance() {
        createInstance();
        return INSTANCE;
    }

    public static ACRAConfiguration getConfig() {
        return new ACRAConfiguration();
    }

    /**
     * Returns true if the application is debuggable.
     *
     * @return true if the application is debuggable.
     */
    public static Application getApplication() {
        return _hostApplication;
    }
    static boolean isDebuggable() {
        PackageManager pm = _hostApplication.getPackageManager();
        try {
            return ((pm.getApplicationInfo(_hostApplication.getPackageName(), 0).flags & ApplicationInfo.FLAG_DEBUGGABLE) > 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


}
