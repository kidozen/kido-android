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
import kidozen.client.PubSubChannel;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;

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

public class PubSubTest {

    public static final int TEST_TIMEOUT_IN_MINUTES = 2;
    public static final String DATA_VALUE_KEY = "value";
    public static final String PUBSUB_INTEGRATION_TESTS = "PubSubChannelIntegrationTests3";
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
    public void ShouldSubscribeAndReceiveMessage() throws Exception {
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,this.CreateRandomValue());
        final PubSubChannel q = kidozen.PubSubChannel(PUBSUB_INTEGRATION_TESTS);

        //Subscribes to channel
        final CountDownLatch lcdSubscribe = new CountDownLatch(1);
        q.Subscribe(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                //System.out.println("Subscribe:" +  e.Body);
                lcdSubscribe.countDown();
            }
        });

        //Set ups listener
        final CountDownLatch lcdGetMessage = new CountDownLatch(1);
        q.GetMessages(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                //System.out.println("The message!? :" +  e.Body);
                lcdGetMessage.countDown();
            }
        });

        //Push a message in channel
        final CountDownLatch lcdPublish = new CountDownLatch(1);
        q.Publish(data,true, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                //System.out.println("Publish:" +  e.Body);
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_CREATED));
            }
        });
        assertTrue(lcdPublish.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));

        //Listenner await
        //assertTrue(lcdGetMessage.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.MINUTES));
        assertTrue(lcdSubscribe.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));

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

