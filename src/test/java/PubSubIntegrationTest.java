import org.apache.http.HttpStatus;
import org.json.JSONObject;
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

import kidozen.client.KZAction;
import kidozen.client.KZApplication;
import kidozen.client.PubSubChannel;
import kidozen.client.Queue;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.Storage;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
@Ignore
public class PubSubIntegrationTest {

    public static final int TIMEOUT = 15000;
    public static final String DATA_VALUE_KEY = "value";
    public static final String PUBSUB_INTEGRATION_TESTS = "PubSubChannelIntegrationTests";
    KZApplication kidozen = null;

    @Before
    public void Setup()
    {
        try {
            final CountDownLatch signal = new CountDownLatch(2);
            kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, true, kidoInitCallback(signal));
            kidozen.Authenticate(IntegrationTestConfiguration.KZ_PROVIDER, IntegrationTestConfiguration.KZ_USER, IntegrationTestConfiguration.KZ_PASS, kidoAuthCallback(signal));
            signal.await(TIMEOUT, TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            fail();
        }
    }

    @Test
    public void ShouldSubscribeAndReceiveMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,this.CreateRandomValue());
        final PubSubChannel q = kidozen.PubSubChannel(PUBSUB_INTEGRATION_TESTS);

        KZAction<JSONObject> onMessage = new KZAction<JSONObject>() {
            @Override
            public void onServiceResponse(JSONObject response) throws Exception {
                lcd.countDown();
            }
        };

        KZAction<Exception> onError = new KZAction<Exception>() {
            @Override
            public void onServiceResponse(Exception response) throws Exception {
                fail();
            }
        };

        q.Subscribe(onMessage, onError);
        Thread.sleep(2000); // gives some time to channels
        q.Publish(data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_CREATED));
            }
        });
        assertTrue(lcd.await(TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void ShouldSubscribeAndReceiveMessageRenewingToken() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,this.CreateRandomValue());
        final PubSubChannel q = kidozen.PubSubChannel(PUBSUB_INTEGRATION_TESTS);

        KZAction<JSONObject> onMessage = new KZAction<JSONObject>() {
            @Override
            public void onServiceResponse(JSONObject response) throws Exception {
                lcd.countDown();
            }
        };

        KZAction<Exception> onError = new KZAction<Exception>() {
            @Override
            public void onServiceResponse(Exception response) throws Exception {
                fail();
            }
        };

        q.Subscribe(onMessage, onError);

        int AUTH_TIMEOUT = 300000;
        Thread.sleep(AUTH_TIMEOUT);

        q.Publish(data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_CREATED));
            }
        });
        assertTrue(lcd.await(TIMEOUT + AUTH_TIMEOUT, TimeUnit.MILLISECONDS));
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

