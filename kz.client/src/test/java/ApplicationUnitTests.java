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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 5/21/13
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)
public class ApplicationUnitTests {

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

        ServiceEventListener callback = new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                lcd.countDown();
            }
        };
        kidozen = new KZApplication(IntegrationTestConfiguration.TENANT, IntegrationTestConfiguration.APP, true, callback);

        lcd.await(TIMEOUT, TimeUnit.MILLISECONDS);
    }

}
