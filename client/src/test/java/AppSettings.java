import java.util.Random;

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

        KZ_EMAIL_ATTACH =       "/Users/christian/attach.txt";
        KZ_PROVIDER=            "Kidozen";
/*
        KZ_SERVICE_ID =         "weather";
        KZ_EMAIL_FROM =         "contoso@kidozen.com.com";
        KZ_EMAIL_TO =           "you@kidozen.com";


        KZ_KEY =     "XhIBjFxY23T3c9u7ZCPWbP67ec802eXgbyWCag/7Fw4=";
        KZ_APP =     "tasks";
        KZ_TENANT =  "https://contoso.local.kidozen.com";
        KZ_USER =    "contosos@kidozen.com";
        KZ_PASS =  "pass";
        */


    }

    static String CreateRandomValue()
    {
        Random rng= new Random();
        String characters ="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        char[] text = new char[10];
        for (int i = 0; i < 10; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);

    }
}