import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZApplication;
import kidozen.client.PubSubChannel;
import kidozen.client.Service;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by christian on 5/6/14.
 */
@Ignore
public class PubsubIntegrationTest {
private static final String KZ_CHANNEL_ID = "testChannelId";
public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    KZApplication kidozen = null;

    @Before
    public void Setup()
    {
        try {
            final CountDownLatch signal = new CountDownLatch(2);
            kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, false, kidoInitCallback(signal));
            kidozen.Authenticate(IntegrationTestConfiguration.KZ_PROVIDER, IntegrationTestConfiguration.KZ_USER, IntegrationTestConfiguration.KZ_PASS,kidoAuthCallback(signal));
            signal.await();
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void ShouldPublishAndSubscribe() throws Exception {
        PubSubChannel service = kidozen.PubSubChannel("mychanel");

        //SUBSCRIBE
        final CountDownLatch subscribe = new CountDownLatch(1);
        service.Subscribe(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_OK);
                subscribe.countDown();
            }
        });
        subscribe.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);

        //Wait For Messages
        final CountDownLatch message = new CountDownLatch(1);
        service.GetMessages(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_OK);
                message.countDown();
            }
        });

        //PUBLISH
        final CountDownLatch publish = new CountDownLatch(1);
        service.Publish(new JSONObject().put("bar", "foo"), false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_CREATED);
                publish.countDown();
            }
        });
        publish.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);

        assertTrue(message.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    //
    private ServiceEventListener kidoInitCallback(final CountDownLatch signal) {
        return new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                signal.countDown();
            }
        };
    }

    private ServiceEventListener kidoAuthCallback(final CountDownLatch signal) {
        return new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                signal.countDown();
            }
        };
    }

}
