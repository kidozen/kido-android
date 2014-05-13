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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZApplication;
import kidozen.client.Service;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 5/20/13
 * Time: 2:30 PM
 * To change this template use File | Settings | File Templates.
*/
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)
@Ignore
public class EApiTest {
    private static final String KZ_SERVICE_METHODID = "get";
    private static final String KZ_SERVICE_INVALID_METHODID = "Invalid";
    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    private JSONObject data = new JSONObject();
    KZApplication kidozen = null;

    @Before
    public void Setup()
    {
        try {
            final CountDownLatch signal = new CountDownLatch(2);
            kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, false, kidoInitCallback(signal));
            kidozen.Authenticate(TestConfiguration.KZ_PROVIDER, TestConfiguration.KZ_USER, TestConfiguration.KZ_PASS,kidoAuthCallback(signal));
            signal.await();
            data.put("path","?q=buenos aires,ar");
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    //@Test
    public void CallInvalidAgentShouldReturnException() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        Service service = kidozen.LOBService(TestConfiguration.KZ_SERVICEID);
        service.InvokeMethod(KZ_SERVICE_METHODID, data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_BAD_REQUEST);
                assertEquals(e.Body, "There aren't any agent online that handles invocations to service 'ShareFile'");
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    public void ShouldInvokeMethod() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        Service service = kidozen.LOBService(TestConfiguration.KZ_SERVICEID);
        service.InvokeMethod(KZ_SERVICE_METHODID, data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_OK);
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    public void InvokeMethodShouldReturnException() throws Exception{
        final CountDownLatch lcd = new CountDownLatch(1);
        Service service = kidozen.LOBService(TestConfiguration.KZ_SERVICEID);
        service.InvokeMethod(KZ_SERVICE_INVALID_METHODID, data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertNotNull(e.Exception);
                assertEquals(e.StatusCode, HttpStatus.SC_NOT_FOUND);
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));

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