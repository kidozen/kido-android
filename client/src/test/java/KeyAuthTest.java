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
public class KeyAuthTest {
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

        kidozen = new KZApplication(AppConfig.KZ_TENANT, AppConfig.KZ_APP, AppConfig.KZ_KEY, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                actualStatusCode = e.StatusCode;
                lcd.countDown();
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        assertEquals(actualStatusCode, HttpStatus.SC_OK);
    }

    @Test
    public void ShouldFailUsingApplicationKey() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(AppConfig.KZ_TENANT, AppConfig.KZ_APP, "fail", false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                actualStatusCode = e.StatusCode;
                lcd.countDown();
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        assertEquals(actualStatusCode, HttpStatus.SC_BAD_REQUEST);
    }


}