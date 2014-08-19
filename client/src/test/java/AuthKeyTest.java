import org.apache.http.HttpStatus;
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

import static org.junit.Assert.assertEquals;

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
public class AuthKeyTest {
    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    int actualStatusCode = 0;
    KZApplication kidozen = null;

    @Before
    public void Setup()
    {
    }

    @Test
    public void ShouldAuthenticateUsingApplicationKey() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);

        kidozen = new KZApplication(AppSettings.KZ_TENANT, AppSettings.KZ_APP, AppSettings.KZ_KEY, false);
        kidozen.Initialize( new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                actualStatusCode = e.StatusCode;
                lcd.countDown();
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        assertEquals( HttpStatus.SC_OK,actualStatusCode);
    }

    @Test
    public void ShouldFailUsingApplicationKey() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(AppSettings.KZ_TENANT, AppSettings.KZ_APP, "fail", false);
        kidozen.Initialize( new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                actualStatusCode = e.StatusCode;
                lcd.countDown();
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        assertEquals(HttpStatus.SC_BAD_REQUEST,actualStatusCode);
    }


}