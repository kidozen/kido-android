import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;

public class AppSettings {
    public static  String KZ_KEY ;
    public static  String KZ_TENANT;
    public static  String KZ_APP;
    public static  String KZ_USER;

    public static  String KZ_PASS;
    public static  String KZ_PROVIDER;

    public static  String KZ_SERVICEID;

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

                BufferedReader br = new BufferedReader(new FileReader(current_directory + "/" + file_settings));
                StringBuilder fileData = new StringBuilder();
                String data ;
                while ((data=br.readLine())!=null) {
                    fileData.append(data);
                }

                JSONObject settings = new JSONObject(fileData.toString());

                KZ_TENANT = settings.get("appKey").toString();
                KZ_TENANT = settings.get("tenant").toString();
                KZ_APP = settings.get("app").toString();
                KZ_USER = settings.get("user").toString();
                KZ_PASS = settings.get("pass").toString();
                KZ_PROVIDER= settings.get("provider").toString();
                KZ_SERVICEID = settings.get("serviceid").toString();
                KZ_EMAIL_FROM = settings.get("emailFrom").toString();
                KZ_EMAIL_TO = settings.get("emailTo").toString();
                KZ_EMAIL_ATTACH = settings.get("emailPathAttach").toString();

                System.out.print("==================================================================\n");
                System.out.print("Tenant: " + KZ_TENANT + "\n");
                System.out.print("Application: " + KZ_APP + "\n");
                System.out.print("User: " + KZ_USER + "\n");
                System.out.print("Password: " + KZ_PASS + "\n");
                System.out.print("ShareFile Service ID: " + KZ_SERVICEID + "\n");
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

        KZ_KEY =                "o0vV8ZGZf6ZPrsWan3OrnZvJHuoCJym/o8W0t9pAwNI=";
        KZ_TENANT =             "https://contoso.local.kidozen.com";
        KZ_APP =                "androide";
        KZ_USER =               "contoso@kidozen.com";
        KZ_PASS =               "pass";
        KZ_SERVICEID =          "Weather";
        KZ_EMAIL_FROM =         "contoso@kidozen.com.com";
        KZ_EMAIL_TO =           "chris@kidozen.com";
        KZ_EMAIL_ATTACH =       "/Users/christian/hosts";
        KZ_PROVIDER=            "Kidozen";
    }
}

/*
        //tests.qa
        KZ_KEY =                "GZJQetc+VH9JLWoHnLEwlk7tw+XPSniMUSuIzK9kDxE=";
        KZ_TENANT =             "https://tests.qa.kidozen.com";
        KZ_APP =                "tasks";
        KZ_USER =               "tests@kidozen.com";
        KZ_PASS =               "pass";
*/

/*
        //att
        KZ_KEY =                "LJHHZdGaFzssi34IGS+wDygiqAPJyansNPLKAUljVmQ=";
        KZ_TENANT =             "https://";
        KZ_APP =                "contacts";
        KZ_USER =               "att@kidozen.com";
        KZ_PASS =               "pass";

* */