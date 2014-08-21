import java.util.Random;

public class AppSettings {
    public static  String KZ_TENANT     = "https://your-tenant.kidocloud.com";
    public static  String KZ_KEY        = "...";
    public static  String KZ_APP        = "tasks";
    public static  String KZ_PROVIDER   = "Kidozen";
    public static  String KZ_USER       = "you@kidozen.com";
    public static  String KZ_PASS       = "supersecret";

    public static  String KZ_SERVICE_ID = "weather";

    public static  String KZ_EMAIL_TO   = "you@kidozen.com";
    public static  String KZ_EMAIL_FROM = "contoso@kidozen.com";
    public static  String KZ_EMAIL_ATTACH = "/Users/you/attach.txt";

    static {
        KZ_TENANT =        (System.getenv("KZ_TENANT")==null ? KZ_TENANT : System.getenv("KZ_TENANT"));
        KZ_APP =           (System.getenv("KZ_APP")==null ? KZ_APP : System.getenv("KZ_APP"));
        KZ_KEY =           (System.getenv("KZ_KEY")==null ? KZ_KEY : System.getenv("KZ_KEY"));
        KZ_PROVIDER =      (System.getenv("KZ_PROVIDER")==null ? KZ_PROVIDER : System.getenv("KZ_PROVIDER"));
        KZ_USER =          (System.getenv("KZ_USER")==null ? KZ_USER : System.getenv("KZ_USER"));
        KZ_PASS =          (System.getenv("KZ_PASS")==null ? KZ_PASS : System.getenv("KZ_PASS"));

        KZ_SERVICE_ID =    (System.getenv("KZ_SERVICE_ID")==null ? KZ_SERVICE_ID : System.getenv("KZ_SERVICE_ID"));

        KZ_EMAIL_FROM =    (System.getenv("KZ_EMAIL_FROM")==null ? KZ_EMAIL_FROM : System.getenv("KZ_EMAIL_FROM"));
        KZ_EMAIL_TO =      (System.getenv("KZ_EMAIL_TO")==null ? KZ_EMAIL_TO : System.getenv("KZ_EMAIL_TO"));
        KZ_EMAIL_ATTACH =  (System.getenv("KZ_EMAIL_ATTACH")==null ? KZ_EMAIL_ATTACH : System.getenv("KZ_EMAIL_ATTACH"));

        System.out.println("****** KZ_TENANT" + KZ_TENANT);
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