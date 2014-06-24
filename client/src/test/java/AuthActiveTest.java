import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
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
public class AuthActiveTest {
    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    public static final int TEST_TIMEOUT_IN_SECONDS = 5;

    KZApplication kidozen = null;

    @Before
    public void Setup()
    {
    }

    @Test
    public void ShouldAuthenticateUsingDefaultSettingsWithoutCallback() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(AppSettings.KZ_TENANT, AppSettings.KZ_APP, AppSettings.KZ_KEY,false,  new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(AppSettings.KZ_PROVIDER, AppSettings.KZ_USER, AppSettings.KZ_PASS);
        assertEquals(true, kidozen.UserIsAuthenticated);
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void ShouldAuthenticateUsingDefaultSettings() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(AppSettings.KZ_TENANT, AppSettings.KZ_APP, AppSettings.KZ_KEY, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(AppSettings.KZ_PROVIDER, AppSettings.KZ_USER, AppSettings.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                alcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        assertEquals(true, kidozen.UserIsAuthenticated);
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void AuthenticationShouldFailWithInvalidUser() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(AppSettings.KZ_TENANT, AppSettings.KZ_APP, AppSettings.KZ_KEY, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(AppSettings.KZ_PROVIDER, "none@kidozen.com", AppSettings.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                alcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_BAD_REQUEST));
            }
        });
        assertEquals(false, kidozen.UserIsAuthenticated);
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }


    @Test
    public void ShouldSignOutUser() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(AppSettings.KZ_TENANT, AppSettings.KZ_APP, AppSettings.KZ_KEY,false,  new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(AppSettings.KZ_PROVIDER, AppSettings.KZ_USER, AppSettings.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                alcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                System.out.print("Authenticate");
            }
        });
        kidozen.SignOut();
        System.out.print("SignOut");

        assertEquals(false, kidozen.UserIsAuthenticated);

        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void AuthenticationShouldFailAndReturnMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(AppSettings.KZ_TENANT, AppSettings.KZ_APP, AppSettings.KZ_KEY, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(AppSettings.KZ_PROVIDER, AppSettings.KZ_USER, "1", new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                alcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_BAD_REQUEST));
                assertTrue(e.Body.toLowerCase().contains("Error trying to call KidoZen Authentication Service Endpoint".toLowerCase()));
            }
        });
        assertEquals(false, kidozen.UserIsAuthenticated);
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void ShouldReturnClaimsUsingDefaultSettings() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(AppSettings.KZ_TENANT, AppSettings.KZ_APP,  AppSettings.KZ_KEY,false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(AppSettings.KZ_PROVIDER, AppSettings.KZ_USER, AppSettings.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                alcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        assertEquals(true, kidozen.UserIsAuthenticated);
        Hashtable<String,String> claims; // TODO FIX THIS: = kidozen.mUserIdentity.Claims;

        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }
}