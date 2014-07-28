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
        KZ_TENANT =             "https://loadtests.qa.kidozen.com";
        KZ_APP =                "integration-tests";
        KZ_KEY =                "1iezHjBY61cLXaDKSlLXszzCStZvYqiU7axVrNIGTrU=";
        KZ_USER =               "loadtests@kidozen.com";
        KZ_PASS =               "pass";

        KZ_SERVICE_ID =         "weather";
        KZ_EMAIL_FROM =         "contoso@kidozen.com.com";
        KZ_EMAIL_TO =           "you@kidozen.com";
        KZ_EMAIL_ATTACH =       "/Users/christian/attach.txt";
        KZ_PROVIDER=            "Kidozen";
    }
}