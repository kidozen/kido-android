import com.kidozen.client.BuildConfig;

import java.util.Random;

public class AppSettings {
    public static  String KZ_TENANT     = BuildConfig.test_marketplace;
    public static  String KZ_KEY        = BuildConfig.test_app_key;
    public static  String KZ_APP        = BuildConfig.test_application;
    public static  String KZ_PROVIDER   = BuildConfig.test_provider;
    public static  String KZ_USER       = BuildConfig.test_username;
    public static  String KZ_PASS       = BuildConfig.test_password;

    public static  String KZ_SERVICE_ID = BuildConfig.test_service;

    public static  String KZ_EMAIL_TO   = BuildConfig.test_email_to;
    public static  String KZ_EMAIL_FROM = "contoso@kidozen.com";
    public static  String KZ_EMAIL_ATTACH = BuildConfig.test_email_attach;

    static {
        System.out.println("### Tenant: " + KZ_TENANT);
        System.out.println("### Key: " + KZ_KEY);
        System.out.println("### App: " + KZ_APP);
        System.out.println("### Provider: " + KZ_PROVIDER);
        System.out.println("### User: " + KZ_USER);
        System.out.println("### Pass: " + KZ_PASS);
        System.out.println("### Service: " + KZ_SERVICE_ID);
        System.out.println("### EmailTo: " + KZ_EMAIL_TO);
        System.out.println("### EmailAttach: " + KZ_EMAIL_ATTACH);
    }

    static String CreateRandomValue() {
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