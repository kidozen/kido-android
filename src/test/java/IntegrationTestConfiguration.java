import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class IntegrationTestConfiguration {
    public static  String KZ_KEY ;
    public static  String KZ_TENANT;
    public static  String KZ_APP;
    public static  String KZ_USER;

    public static  String KZ_PASS;
    public static  String KZ_PROVIDER;

    public static  String KZ_SHAREFILE_SERVICEID;
    public static  String KZ_SHAREFILE_USER;
    public static  String KZ_SHAREFILE_PASS;

    public static  String KZ_EMAIL_TO;
    public static  String KZ_EMAIL_FROM;
    public static  String KZ_EMAIL_ATTACH;

    public static int KZ_TOKEN_EXPIRES_TIMEOUT= (1000 * 60 * 4);

    static {
        try {
            String file_settings = System.getProperty("settings");
            String current_directory = System.getProperty("user.dir");
            if (file_settings !="" && file_settings != null)
            {
                System.out.print("Current dir:" + current_directory + "\n");
                System.out.print("File settings:" + file_settings + "\n");

                JSONParser jp = new JSONParser();
                JSONObject settings =(JSONObject) jp.parse(new FileReader(current_directory + "/" + file_settings));


                KZ_TENANT = settings.get("kz_app_key").toString();
                KZ_TENANT = settings.get("kz_tenant").toString();
                KZ_APP = settings.get("kz_app").toString();
                KZ_USER = settings.get("kz_usr").toString();
                KZ_PASS = settings.get("kz_pass").toString();
                KZ_PROVIDER= settings.get("kz_provider").toString();
                KZ_SHAREFILE_PASS = settings.get("kz_sharefile_pass").toString();
                KZ_SHAREFILE_SERVICEID = settings.get("kz_sharefile_serviceid").toString();
                KZ_SHAREFILE_USER = settings.get("kz_sharefile_user").toString();
                KZ_EMAIL_FROM = settings.get("kz_email_from").toString();
                KZ_EMAIL_TO = settings.get("kz_email_to").toString();
                KZ_EMAIL_ATTACH = settings.get("kz_email_attach").toString();

                System.out.print("==================================================================\n");
                System.out.print("Tenant: " + KZ_TENANT + "\n");
                System.out.print("Application: " + KZ_APP + "\n");
                System.out.print("User: " + KZ_USER + "\n");
                System.out.print("Password: " + KZ_PASS + "\n");
                System.out.print("ShareFile Password: " + KZ_SHAREFILE_PASS + "\n");
                System.out.print("ShareFile Service ID: " + KZ_SHAREFILE_SERVICEID+ "\n");
                System.out.print("ShareFile User: " + KZ_SHAREFILE_USER + "\n");
                System.out.print("Email from: " + KZ_EMAIL_FROM + "\n");
                System.out.print("Email to: " + KZ_EMAIL_TO + "\n");
                System.out.print("Email Attach Path: " + KZ_EMAIL_ATTACH + "\n");
                System.out.print("==================================================================\n");
            }
            else
            {
                usedefaults();
            }
        }
        catch (Exception e)
        {
            System.out.print("ERROR:" + e.getMessage().toString() + "\n");
            usedefaults();
        }

    }

    private static void usedefaults() {
        System.out.print("No settings specified, using defaults\n");

        KZ_KEY = "jHf9GxVw2VwQcLYIrkvPcb+Swlh4M2wcd53WcxhdMsU=";
        KZ_TENANT =             "https://contoso.local.kidozen.com";
        KZ_APP =                "ioscrashapp";
        KZ_USER =               "contoso@kidozen.com";
        KZ_PASS =               "pass";
        KZ_SHAREFILE_PASS =     "your sharefile secret";
        KZ_SHAREFILE_SERVICEID ="sharefile";
        KZ_SHAREFILE_USER =     "your sharefile user";
        KZ_EMAIL_FROM =         "none@email.com";
        KZ_EMAIL_TO =           "none@email.com";
        KZ_EMAIL_ATTACH =       "/path/to/attach.txt";
        KZ_PROVIDER=            "Kidozen";
    }
}