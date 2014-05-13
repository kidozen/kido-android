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

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 5/20/13
 * Time: 2:30 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)
@Ignore
public class ApplicationTest {
    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    private static final String INVALIDAPP = "NADA";
    KZApplication kidozen = null;

    @Before
    public void Setup()
    {
    }
    @Test
    public void ShouldGetApplicationConfiguration() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertEquals(HttpStatus.SC_OK,e.StatusCode);
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void ShouldReturnInvalidApplicationName() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(TestConfiguration.KZ_TENANT, INVALIDAPP, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_NOT_FOUND));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void ShouldAuthenticateUsingDefaultSettingsWithoutAuthCallback() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(TestConfiguration.KZ_PROVIDER, TestConfiguration.KZ_USER, TestConfiguration.KZ_PASS);
        assertEquals(true, kidozen.Authenticated);
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }


    @Test
    public void ShouldAuthenticateUsingDefaultSettings() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
            lcd.countDown();
            assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(TestConfiguration.KZ_PROVIDER, TestConfiguration.KZ_USER, TestConfiguration.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
            alcd.countDown();
            assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        assertEquals(true, kidozen.Authenticated);
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void AuthenticationShouldFail() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
            lcd.countDown();
            assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(TestConfiguration.KZ_PROVIDER, "none@kidozen.com", TestConfiguration.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
            alcd.countDown();
            assertThat(e.StatusCode, equalTo(HttpStatus.SC_BAD_REQUEST));
            }
        });
        assertEquals(false, kidozen.Authenticated);
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void AuthenticationShouldFailWithInvalidUser() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
            lcd.countDown();
            assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(TestConfiguration.KZ_PROVIDER, "none@kidozen.com", TestConfiguration.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
            alcd.countDown();
            assertThat(e.StatusCode, equalTo(HttpStatus.SC_BAD_REQUEST));
            }
        });
        assertEquals(false, kidozen.Authenticated);
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void ShouldReturnApplicationIsNotInitialized() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(TestConfiguration.KZ_TENANT, INVALIDAPP, false, null);
        kidozen.Authenticate(TestConfiguration.KZ_PROVIDER, TestConfiguration.KZ_USER, TestConfiguration.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
            lcd.countDown();
            assertThat(e.StatusCode, equalTo(HttpStatus.SC_CONFLICT));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void ShouldReturnClaimsUsingDefaultSettings() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
            lcd.countDown();
            assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(TestConfiguration.KZ_PROVIDER, TestConfiguration.KZ_USER, TestConfiguration.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
            alcd.countDown();
            assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        assertEquals(true, kidozen.Authenticated);
        Hashtable<String,String> claims; // TODO FIX THIS: = kidozen.mUserIdentity.Claims;

        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void ShouldSignOut() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
            lcd.countDown();
            assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(TestConfiguration.KZ_PROVIDER, TestConfiguration.KZ_USER, TestConfiguration.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
            alcd.countDown();
            assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            System.out.print("Authenticate");
            }
        });
        kidozen.SignOut();
        System.out.print("SignOut");

        assertEquals(false, kidozen.Authenticated);

        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

}