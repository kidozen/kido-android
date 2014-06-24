import org.apache.http.HttpStatus;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;


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
        kidozen = new KZApplication(AppConfig.KZ_TENANT, AppConfig.KZ_APP,  AppConfig.KZ_KEY, false, new ServiceEventListener() {
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
        kidozen = new KZApplication(AppConfig.KZ_TENANT, INVALIDAPP, AppConfig.KZ_KEY,  false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_NOT_FOUND));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void ShouldReturnApplicationIsNotInitialized() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(AppConfig.KZ_TENANT, INVALIDAPP, AppConfig.KZ_KEY, false, null);
        kidozen.Authenticate(AppConfig.KZ_PROVIDER, AppConfig.KZ_USER, AppConfig.KZ_PASS, new ServiceEventListener() {
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
        kidozen = new KZApplication(AppConfig.KZ_TENANT, AppConfig.KZ_APP,  AppConfig.KZ_KEY,false, new ServiceEventListener() {
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
        Hashtable<String,String> claims; // TODO FIX THIS: = kidozen.mUserIdentity.Claims;

        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }
}