import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
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
public class ApplicationIntegrationTests {

    public static final int TIMEOUT = 3000;
    private static final String INVALIDAPP = "NADA";
    KZApplication kidozen = null;

    @Before
    public void Setup()
    {
    }

    @Test
    public void ShouldGetApplicationConfiguration() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(IntegrationTestConfiguration.TENANT, IntegrationTestConfiguration.APP, true, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode,HttpStatus.SC_OK);
                lcd.countDown();
            }
        });
        lcd.await(TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Test
    public void ShouldReturnInvalidApplicationName() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(IntegrationTestConfiguration.TENANT, INVALIDAPP, true, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_NOT_FOUND));
                lcd.countDown();
            }
        });
        lcd.await(TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Test
    public void ShouldAuthenticateUsingDefaultSettings() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(IntegrationTestConfiguration.TENANT, IntegrationTestConfiguration.APP, true, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
            assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            lcd.countDown();
            }
        });
        lcd.await(TIMEOUT, TimeUnit.MILLISECONDS);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(IntegrationTestConfiguration.PROVIDER, IntegrationTestConfiguration.USR, IntegrationTestConfiguration.PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                alcd.countDown();
            }
        });
        alcd.await(TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Test
    public void ShouldReturnApplicationIsNotInitialized() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(IntegrationTestConfiguration.TENANT, INVALIDAPP, true, null);
        kidozen.Authenticate(IntegrationTestConfiguration.PROVIDER, IntegrationTestConfiguration.USR, IntegrationTestConfiguration.PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_CONFLICT));
                lcd.countDown();
            }
        });
        lcd.await(TIMEOUT, TimeUnit.MILLISECONDS);
    }

}