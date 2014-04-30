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
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.Storage;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by christian on 9/19/13.
 */
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)
@Ignore
public class TokenRenewTest {
    private static final String KZ_STORAGE_SERVICEID = "StorageIntegrationTestsCollection";
    public static final String PUBSUB_INTEGRATION_TESTS = "PubSubChannelIntegrationTests";
    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    public static final String DATA_VALUE_KEY = "value";
    KZApplication kidozen = null;
    Storage _storage;

    boolean onSessionExpirationRun = false;

    @Test
    @Ignore //Cannot run this test with current robolectric version
    public void ShouldExecuteOnSessionExpirationRunnable() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, true, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                lcd.countDown();
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(IntegrationTestConfiguration.KZ_PROVIDER, IntegrationTestConfiguration.KZ_USER, IntegrationTestConfiguration.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                alcd.countDown();
            }
        });
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);

        Thread.sleep(IntegrationTestConfiguration.KZ_TOKEN_EXPIRES_TIMEOUT);

        final CountDownLatch lcde = new CountDownLatch(1);
        Runnable whenExpires = new Runnable() {
            @Override
            public void run() {
                onSessionExpirationRun = true;
                lcde.countDown();
            }
        };
        //TODO: FIX  kidozen.OnSessionExpirationRunnable(whenExpires);

        lcde.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);

        assertTrue(onSessionExpirationRun);
    }


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
    public void ShouldRenewTokenUsingDefaultSettings() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, true, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                lcd.countDown();
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MILLISECONDS);
        final CountDownLatch alcd = new CountDownLatch(1);

        kidozen.Authenticate(IntegrationTestConfiguration.KZ_PROVIDER, IntegrationTestConfiguration.KZ_USER, IntegrationTestConfiguration.KZ_PASS, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                alcd.countDown();
            }
        });
        alcd.await(TEST_TIMEOUT_IN_MINUTES , TimeUnit.MINUTES);

        //Assert
        final CountDownLatch qcdl = new CountDownLatch(1);

        Thread.sleep(IntegrationTestConfiguration.KZ_TOKEN_EXPIRES_TIMEOUT);

        _storage.Query("{}", new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_OK);
                qcdl.countDown();
            }
        });
        assertTrue(qcdl.await(IntegrationTestConfiguration.KZ_TOKEN_EXPIRES_TIMEOUT * 1000 * 60, TimeUnit.MINUTES));
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
/* TODO: FIX TEST
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

        //Force token expiration
        Thread.sleep(IntegrationTestConfiguration.KZ_TOKEN_EXPIRES_TIMEOUT);

        q.Publish(data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_CREATED));
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES * 1000 * 60 + IntegrationTestConfiguration.KZ_TOKEN_EXPIRES_TIMEOUT, TimeUnit.MILLISECONDS));
    }
    */
}
