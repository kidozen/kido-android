import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.FixMethodOrder;
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
public class ActiveAuthTest {
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
        kidozen = new KZApplication(AppConfig.KZ_TENANT, AppConfig.KZ_APP, AppConfig.KZ_KEY,false,  new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(AppConfig.KZ_PROVIDER, AppConfig.KZ_USER, AppConfig.KZ_PASS);
        assertEquals(true, kidozen.UserIsAuthenticated);
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void ShouldAuthenticateUsingDefaultSettings() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(AppConfig.KZ_TENANT, AppConfig.KZ_APP, AppConfig.KZ_KEY, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(AppConfig.KZ_PROVIDER, AppConfig.KZ_USER, AppConfig.KZ_PASS, new ServiceEventListener() {
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
        kidozen = new KZApplication(AppConfig.KZ_TENANT, AppConfig.KZ_APP, AppConfig.KZ_KEY, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(AppConfig.KZ_PROVIDER, "none@kidozen.com", AppConfig.KZ_PASS, new ServiceEventListener() {
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
        kidozen = new KZApplication(AppConfig.KZ_TENANT, AppConfig.KZ_APP, AppConfig.KZ_KEY,false,  new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(AppConfig.KZ_PROVIDER, AppConfig.KZ_USER, AppConfig.KZ_PASS, new ServiceEventListener() {
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
        kidozen = new KZApplication(AppConfig.KZ_TENANT, AppConfig.KZ_APP,AppConfig.KZ_KEY, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(AppConfig.KZ_PROVIDER, AppConfig.KZ_USER, "1", new ServiceEventListener() {
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


/*
    @Test
    public void ShouldAuthenticateUser() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, TestConfiguration.KZ_KEY, false , new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                System.out.print("call init");

                assertEquals(e.StatusCode, HttpStatus.SC_OK);
            }
        });
        lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        final CountDownLatch alcd = new CountDownLatch(1);
        kidozen.Authenticate(TestConfiguration.KZ_PROVIDER, TestConfiguration.KZ_USER, TestConfiguration.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                System.out.print("call auth");

                alcd.countDown();
                Assert.assertEquals(e.StatusCode, HttpStatus.SC_OK);
                Assert.assertFalse(kidozen.Authenticated);
            }
        });
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void AuthenticationShouldFailWithInvalidUser() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, TestConfiguration.KZ_KEY, false , new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                System.out.print("call init");

                lcd.countDown();
                assertEquals(e.StatusCode, HttpStatus.SC_OK);
            }
        });
        lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        final CountDownLatch alcd = new CountDownLatch(1);
        kidozen.Authenticate(TestConfiguration.KZ_PROVIDER, "none@kidozen.com", TestConfiguration.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                System.out.print("call auth");

                alcd.countDown();
                assertEquals(e.StatusCode, HttpStatus.SC_BAD_REQUEST);
            }
        });
        assertEquals(false, kidozen.UserIsAuthenticated);
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

*/
}