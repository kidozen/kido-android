public class AppSettings {
    public static  String KZ_KEY ;
    public static  String KZ_TENANT;
    public static  String KZ_APP;
    public static  String KZ_USER;

    public static  String KZ_PASS;
    public static  String KZ_PROVIDER;

    public static  String KZ_SERVICE_ID;

    public static  String KZ_EMAIL_TO;
    public static  String KZ_EMAIL_FROM;
    public static  String KZ_EMAIL_ATTACH;

    static {
        KZ_TENANT =             "https://tenant.kidocloud.com";
        KZ_APP =                "app";
        KZ_KEY =                "key";
        KZ_USER =               "user@kidozen.com";
        KZ_PASS =               "supersecret";

        KZ_SERVICE_ID =         "weather";
        KZ_EMAIL_FROM =         "contoso@kidozen.com.com";
        KZ_EMAIL_TO =           "you@kidozen.com";
        KZ_EMAIL_ATTACH =       "/Users/you/attach.txt";
        KZ_PROVIDER=            "Kidozen";
    }
}