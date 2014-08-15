import org.apache.http.HttpStatus;
import org.json.JSONException;
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
import kidozen.client.Service;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.ServiceResponseListener;
import kidozen.client.SynchronousException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;


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

public class EApiTest {
    public static final int TEST_TIMEOUT_IN_SECONDS = 10;

    private static final String KZ_SERVICE_METHODID = "get";
    private static final String KZ_SERVICE_INVALID_METHODID = "Invalid";
    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    private JSONObject data = new JSONObject();
    KZApplication kidozen = null;

    @Before
    public void Setup()
    {
        try {
            final CountDownLatch signal = new CountDownLatch(1);
            kidozen = new KZApplication(AppSettings.KZ_TENANT, AppSettings.KZ_APP, AppSettings.KZ_KEY, false);
            kidozen.Authenticate(AppSettings.KZ_PROVIDER, AppSettings.KZ_USER, AppSettings.KZ_PASS, kidoAuthCallback(signal));
            signal.await();
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void ShouldInvokeMethod() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        Service service = kidozen.LOBService(AppSettings.KZ_SERVICE_ID);
        service.InvokeMethod(KZ_SERVICE_METHODID, data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_OK);
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void InvokeMethodShouldReturnException() throws Exception{
        final CountDownLatch lcd = new CountDownLatch(1);
        Service service = kidozen.LOBService("nil");
        service.InvokeMethod(KZ_SERVICE_INVALID_METHODID, data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_BAD_REQUEST);
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));

    }

    @Test
    public void ShouldInvokeMethodSync() throws Exception, SynchronousException {
        Service service = kidozen.LOBService(AppSettings.KZ_SERVICE_ID);
        try {
            JSONObject result = service.InvokeMethod(KZ_SERVICE_METHODID, data);
            assertTrue(result.getInt("status") == 200);
        } catch (SynchronousException e) {
            fail();
        }
    }

    @Test
    public void ShouldInvokeMethodWithServiceResponse() throws Exception, SynchronousException {
        final CountDownLatch lcd = new CountDownLatch(1);

        Service service = kidozen.LOBService(AppSettings.KZ_SERVICE_ID);
        service.InvokeMethod(KZ_SERVICE_METHODID, data, new ServiceResponseListener() {
            @Override
            public void onError(int statusCode, String response)
            {
                fail();
            }
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                assertEquals(HttpStatus.SC_OK, statusCode);
                try {
                    assertTrue(response.getInt("status") == 200);
                } catch (JSONException e) {
                    fail();
                }
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
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