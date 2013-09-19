import kidozen.client.*;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
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
public class QueueIntegrationTest {

    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    public static final String DATA_VALUE_KEY = "value";
    public static final String QUEUE_INTEGRATION_TESTS = "StorageIntegrationTests";
    KZApplication kidozen = null;
    Storage _storage;

    @Before
    public void Setup()
    {
        try {
            final CountDownLatch signal = new CountDownLatch(2);
            kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, true, kidoInitCallback(signal));
            kidozen.Authenticate(IntegrationTestConfiguration.KZ_PROVIDER, IntegrationTestConfiguration.KZ_USER, IntegrationTestConfiguration.KZ_PASS, kidoAuthCallback(signal));
            signal.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        }
        catch (Exception e)
        {
            fail();
        }
    }
    @Test
    public void ShouldEnqueue() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,this.CreateRandomValue());
        Queue q = kidozen.Queue(QUEUE_INTEGRATION_TESTS);
        q.Enqueue (data, sendCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    public void ShouldDequeue() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,this.CreateRandomValue());
        final Queue q = kidozen.Queue(QUEUE_INTEGRATION_TESTS);
        q.Enqueue(data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
               q.Dequeue(new ServiceEventListener() {
                   @Override
                   public void onFinish(ServiceEvent e) {
                       assertThat(e.StatusCode, equalTo( HttpStatus.SC_OK));
                       lcd.countDown();
                   }
               });
            }
        } );

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    //
    private ServiceEventListener sendCallback(final CountDownLatch signal) {
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

