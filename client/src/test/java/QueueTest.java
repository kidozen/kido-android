import org.apache.http.HttpStatus;
import org.json.JSONObject;
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
import kidozen.client.Queue;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.ServiceResponseListener;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


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

public class QueueTest {

    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    public static final String DATA_VALUE_KEY = "value";
    public static final String QUEUE_INTEGRATION_TESTS = "QueueIntegrationTests";
    KZApplication kidozen = null;

    @Before
    public void Setup()
    {
        try {
            final CountDownLatch signal = new CountDownLatch(1);
            kidozen = new KZApplication(AppSettings.KZ_TENANT, AppSettings.KZ_APP, AppSettings.KZ_KEY, false);
            kidozen.Authenticate(AppSettings.KZ_PROVIDER, AppSettings.KZ_USER, AppSettings.KZ_PASS, kidoAuthCallback(signal));
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

    @Test
    public void ShouldEnqueueWithSvcResponse() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,this.CreateRandomValue());
        Queue q = kidozen.Queue(QUEUE_INTEGRATION_TESTS);
        q.Enqueue (data, new ServiceResponseListener() {
            @Override
            public void onError(int statusCode, String response) {
                fail();
            }
            @Override
            public void onSuccess(int statusCode, String response) {
                assertThat(statusCode, equalTo( HttpStatus.SC_CREATED));
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldDequeueWithSvcResponse() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,this.CreateRandomValue());
        final Queue q = kidozen.Queue(QUEUE_INTEGRATION_TESTS);
        q.Enqueue(data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                q.Dequeue(new ServiceResponseListener() {
                    @Override
                    public void onError(int statusCode, String response) {
                        fail();
                    }
                    @Override
                    public void onSuccess(int statusCode, JSONObject response) {
                        assertThat(statusCode, equalTo( HttpStatus.SC_OK));
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
        return AppSettings.CreateRandomValue();

    }

}

