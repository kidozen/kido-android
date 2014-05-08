import org.apache.http.HttpStatus;
import org.junit.Assert;
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
public class AuthenticationIntegrationTest {
    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    public static final int TEST_TIMEOUT_IN_SECONDS = 5;

    private static final String KZ_KEY = "jHf9GxVw2VwQcLYIrkvPcb+Swlh4M2wcd53WcxhdMsU=";
    KZApplication kidozen = null;

    @Before
    public void Setup()
    {
    }

    @Test
    public void ShouldAuthenticateUsingApplicationKey() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);

        kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, KZ_KEY, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {

                lcd.countDown();
                assertEquals(e.StatusCode, HttpStatus.SC_OK);
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        assertEquals(true, kidozen.Authenticated);
    }

    @Test
    public void ShouldFailUsingApplicationKey() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);

        kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, "fail", false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {

                lcd.countDown();
                assertEquals(e.StatusCode, HttpStatus.SC_BAD_REQUEST);
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        assertEquals(false, kidozen.Authenticated);
    }

    @Test
    public void ShouldAuthenticateUser() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, false , new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                System.out.print("call init");

                assertEquals(e.StatusCode, HttpStatus.SC_OK);
            }
        });
        lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        final CountDownLatch alcd = new CountDownLatch(1);
        kidozen.Authenticate(IntegrationTestConfiguration.KZ_PROVIDER, IntegrationTestConfiguration.KZ_USER, IntegrationTestConfiguration.KZ_PASS, new ServiceEventListener() {
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
        kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, false , new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                System.out.print("call init");

                lcd.countDown();
                assertEquals(e.StatusCode, HttpStatus.SC_OK);
            }
        });
        lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        final CountDownLatch alcd = new CountDownLatch(1);
        kidozen.Authenticate(IntegrationTestConfiguration.KZ_PROVIDER, "none@kidozen.com", IntegrationTestConfiguration.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                System.out.print("call auth");

                alcd.countDown();
                assertEquals(e.StatusCode, HttpStatus.SC_BAD_REQUEST);
            }
        });
        assertEquals(false, kidozen.Authenticated);
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    public void AuthenticationShouldFailAndReturnMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(IntegrationTestConfiguration.KZ_PROVIDER,IntegrationTestConfiguration.KZ_USER, "1", new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                alcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_BAD_REQUEST));
                assertTrue(e.Body.toLowerCase().contains("Error trying to call KidoZen Authentication Service Endpoint".toLowerCase()));
            }
        });
        assertEquals(false, kidozen.Authenticated);
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }
}