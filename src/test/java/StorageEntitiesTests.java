import org.apache.http.HttpStatus;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.Storage;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by christian on 5/11/14.
 */
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)
@Ignore
public class StorageEntitiesTests {
    private static final String KZ_STORAGE_SERVICEID = "StorageIntegrationTestsCollection";
    public static final int TEST_TIMEOUT_IN_SECONDS = 10;
    public static final String DATA_VALUE_KEY = "value";
    KZApplication kidozen = null;

    Storage _storage;

    //@Before
    public void Setup()
    {
        try {
            final CountDownLatch signalInit = new CountDownLatch(1);
            kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, false, kidoInitCallback(signalInit));
            signalInit.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            final CountDownLatch signalAuth = new CountDownLatch(1);
            kidozen.Authenticate(TestConfiguration.KZ_PROVIDER, TestConfiguration.KZ_USER, TestConfiguration.KZ_PASS, kidoAuthCallback(signalAuth));
            signalAuth.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            _storage = kidozen.Storage(KZ_STORAGE_SERVICEID);
        }
        catch (Exception e)
        {
            fail();
        }
    }
    /*
    @Test
    public void ShouldCreateMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        //Gson gson = new Gson();

        MyComplexObject obj = new MyComplexObject();
        obj.setMyIntegerValue(3);
        obj.MyPublicListOFStrings = new ArrayList<String>();
        obj.MyPublicListOFStrings.add("1");

        MySerializer x = new MySerializer();

        //String json = gson.toJson(obj);
        String json = x.toJson("ddsee");
        //MyComplexObject one = gson.fromJson(json, MyComplexObject.class);
        String one = x.fromJson(json, String.class);

        //System.out.println(one.toString());

    }
    */

    private ServiceEventListener kidoInitCallback(final CountDownLatch signal) {
        return new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                signal.countDown();
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_OK));
            }
        };
    }

    private ServiceEventListener kidoAuthCallback(final CountDownLatch signal) {
        return new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                signal.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        };
    }
}


