import kidozen.client.*;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 5/27/13
 * Time: 4:17 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)

public class LoggingIntegrationTest {

    private static final String KZ_STORAGE_SERVICEID = "StorageIntegrationTestsCollection";
    public static final int TIMEOUT = 3000;
    public static final String DATA_VALUE_KEY = "value";
    KZApplication kidozen = null;
    Storage _storage;

    @Before
    public void Setup()
    {
        try {
            final CountDownLatch signal = new CountDownLatch(2);
            kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, true, kidoInitCallback(signal));
            kidozen.Authenticate(IntegrationTestConfiguration.KZ_PROVIDER, IntegrationTestConfiguration.KZ_USER, IntegrationTestConfiguration.KZ_PASS, kidoAuthCallback(signal));
            signal.await();
            _storage = kidozen.Storage(KZ_STORAGE_SERVICEID);
        }
        catch (Exception e)
        {
            fail();
        }
    }
    @Test
    public void ShouldCreateLogMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);

        kidozen.WriteLog("LoggingIntegrationTests",LogLevel.LogLevelCritical, createCallback(lcd));

        assertTrue(lcd.await(TIMEOUT, TimeUnit.MILLISECONDS));
    }
    @Test
    public void ShouldTruncateLog() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);

        kidozen.ClearLog(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_OK));
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void ShouldGetAllLog() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(1);

        kidozen.AllLogMessages(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_OK));
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TIMEOUT, TimeUnit.MILLISECONDS));
    }

    //
    private ServiceEventListener createCallback(final CountDownLatch signal) {
        return  new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_CREATED));
                signal.countDown();
            }
        };
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

    private String CreateRandomValue()
    {
        Random rng= new Random();
        String characters ="qwertyuiopñlkjhgfdsazxcvbnm";
        char[] text = new char[10];
        for (int i = 0; i < 10; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);

    }
}

