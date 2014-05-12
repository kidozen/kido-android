import android.app.Application;

import org.apache.http.HttpStatus;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.Robolectric;
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
import static org.junit.Assert.fail;

/**
 * Created by christian on 5/8/14.
 */
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)
public class CrashTest {
    public static final int TEST_TIMEOUT_IN_MINUTES = 5;
    Application AndroidApp = Robolectric.application;

    @Test(expected = IllegalStateException.class)
    public void ShouldThrowKeyIsRequired() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(3);

            KZApplication kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, false, kidoInitCallback(signal));
            kidozen.Authenticate(TestConfiguration.KZ_PROVIDER, TestConfiguration.KZ_USER, TestConfiguration.KZ_PASS, kidoAuthCallback(signal));

            kidozen.EnableCrashReporter(AndroidApp);
            signal.countDown();

        assertTrue(signal.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldCreateCrashInstance() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(2);
        try {
            KZApplication kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, TestConfiguration.KZ_KEY ,false, kidoInitCallback(signal));

            kidozen.EnableCrashReporter(AndroidApp);
            signal.countDown();
        }
        catch (IllegalStateException e)
        {
            fail(e.getMessage());
        }

        assertTrue(signal.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    private ServiceEventListener kidoInitCallback(final CountDownLatch signal) {
        return new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_OK));
                signal.countDown();
            }
        };
    }

    private ServiceEventListener kidoAuthCallback(final CountDownLatch signal) {
        return new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_OK));
                signal.countDown();
            }
        };
    }
}
