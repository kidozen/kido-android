import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.authentication.IdentityManager;
import kidozen.client.authentication.KidoZenUser;
import kidozen.client.authentication.KidoZenUserIdentityType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 1/08/14
 * Time: 11:00 AM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)
public class IdentityManagerTests {
    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    private JSONObject cfg, cfgKey;
    private static final String KZ_KEY = "jHf9GxVw2VwQcLYIrkvPcb+Swlh4M2wcd53WcxhdMsU=";
    private static final String KZ_TENANT = "https://contoso.local.kidozen.com";
    private static final String KZ_APP = "ioscrashapp";
    String provider = "kidozen";
    String user = "contoso@kidozen.com";
    String pass = "pass";

    @Before
    public void Setup() throws JSONException
    {
        cfg = new JSONObject("{\n" +
                "    \"applicationScope\": \"http://ioscrashapp.contoso.local.kidozen.com/\",\n" +
                "    \"authServiceScope\": \"https://kido-contoso.accesscontrol.windows.net/\",\n" +
                "    \"authServiceEndpoint\": \"https://contoso.local.kidozen.com/auth/v1/WRAPv0.9\",\n" +
                "    \"oauthTokenEndpoint\": \"https://contoso.local.kidozen.com/auth/v1/oauth/token\",\n" +
                "    \"identityProviders\": {\n" +
                "        \"Kidozen\": {\n" +
                "            \"protocol\": \"WRAPv0.9\",\n" +
                "            \"endpoint\": \"https://identity.kidozen.com/WRAPv0.9\"\n" +
                "        }\n" +
                "    }\n" +
                "}");
        cfgKey = cfg.put("domain", "contoso.local.kidozen.com");
    }
    @Test
    public void ShouldFailAuthenticateUser() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        IdentityManager im = IdentityManager.getInstance();
        im.Setup(cfg,false);
        im.Authenticate(provider,user,"", new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_BAD_REQUEST));
                lcd.countDown();
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }
    @Test
    public void ShouldAuthenticateUser() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        IdentityManager im = IdentityManager.getInstance();
        im.Setup(cfg,false);
        im.Authenticate(provider,user,pass, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                KidoZenUser usr = (KidoZenUser)e.Response;
                assertThat(usr.PulledFromCache, equalTo(false));
                assertThat(usr.IdentityType, equalTo(KidoZenUserIdentityType.USER_IDENTITY));
                lcd.countDown();
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }
    @Test
    public void ShouldPullUserIdentityFromCache() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        IdentityManager im = IdentityManager.getInstance();
        im.Setup(cfg,false);

        im.Authenticate(provider,user,pass, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                KidoZenUser usr = (KidoZenUser)e.Response;
                assertThat(usr.PulledFromCache, equalTo(false));
                lcd.countDown();
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch lcd2 = new CountDownLatch(1);
        im.Authenticate(provider,user,pass, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                KidoZenUser usr = (KidoZenUser)e.Response;
                assertThat(usr.PulledFromCache, equalTo(true));
                lcd2.countDown();
            }
        });
        lcd2.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }
    @Test
    public void ShouldAuthenticateApplication() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        IdentityManager im = IdentityManager.getInstance();
        im.Setup(cfg,false);
        im.Authenticate(KZ_KEY,new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                KidoZenUser usr = (KidoZenUser)e.Response;
                assertThat(usr.PulledFromCache, equalTo(false));
                assertThat(usr.IdentityType, equalTo(KidoZenUserIdentityType.APPLICATION_IDENTITY));
                lcd.countDown();
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }
    @Test
    public void ShouldFailAuthenticateApplication() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        IdentityManager im = IdentityManager.getInstance();
        im.Setup(cfg,false);
        im.Authenticate("",new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_BAD_REQUEST));
                lcd.countDown();
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }
    @Test
    public void ShouldPullApplicationIdentityFromCache() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        IdentityManager im = IdentityManager.getInstance();
        im.Setup(cfg,false);
        im.Authenticate(KZ_KEY, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                KidoZenUser usr = (KidoZenUser)e.Response;
                assertThat(usr.PulledFromCache, equalTo(false));
                lcd.countDown();
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch lcd2 = new CountDownLatch(1);
        im.Authenticate(KZ_KEY, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                KidoZenUser usr = (KidoZenUser)e.Response;
                assertThat(usr.PulledFromCache, equalTo(true));
                lcd2.countDown();
            }
        });
        lcd2.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

}
