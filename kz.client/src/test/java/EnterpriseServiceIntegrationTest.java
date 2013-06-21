import kidozen.client.KZApplication;
import kidozen.client.Service;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

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
public class EnterpriseServiceIntegrationTest {

    private static final String KZ_SHAREFILE_GETAUTHID_METHODID = "getAuthID";
    private static final String KZ_SHAREFILE_SERVICEID = "sharefile";
    private static final String KZ_SHAREFILE_INVALID_METHODID = "Invalid";
    public static final int TIMEOUT = 3000;


    KZApplication kidozen = null;
    private JSONObject data;


    @Before
    public void Setup()
    {
        try {
            data = new JSONObject();
            data.put("username","your username");
            data.put("password","your password");

            final CountDownLatch signal = new CountDownLatch(2);
            kidozen = new KZApplication(IntegrationTestConfiguration.TENANT, IntegrationTestConfiguration.APP, true, kidoInitCallback(signal));
            kidozen.Authenticate(IntegrationTestConfiguration.PROVIDER, IntegrationTestConfiguration.USR, IntegrationTestConfiguration.PASS,kidoAuthCallback(signal));
            signal.await();
        }
        catch (Exception e)
        {
            fail();
        }
    }

    @Test
    public void CallInvalidAgentShouldReturnException() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        Service fileshare = kidozen.LOBService(KZ_SHAREFILE_SERVICEID);
        fileshare.InvokeMethod(KZ_SHAREFILE_GETAUTHID_METHODID, data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_BAD_REQUEST);
                assertEquals(e.Body,"There aren't any agent online that handles invocations to service 'ShareFile'");
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TIMEOUT, TimeUnit.MILLISECONDS));
    }
    @Test
    public void ShouldInvokeMethod() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        Service fileshare = kidozen.LOBService(KZ_SHAREFILE_SERVICEID);
        fileshare.InvokeMethod(KZ_SHAREFILE_GETAUTHID_METHODID, data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_OK);
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TIMEOUT, TimeUnit.MILLISECONDS));
    }
    @Test
    public void InvokeMethodShouldReturnException() throws Exception{
        final CountDownLatch lcd = new CountDownLatch(1);
        Service fileshare = kidozen.LOBService(KZ_SHAREFILE_SERVICEID);
        fileshare.InvokeMethod(KZ_SHAREFILE_INVALID_METHODID, data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertNotNull(e.Exception);
                assertEquals(e.StatusCode, HttpStatus.SC_NOT_FOUND);
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TIMEOUT, TimeUnit.MILLISECONDS));

    }

    //
    private ServiceEventListener kidoInitCallback(final CountDownLatch signal) {
        return new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(200));
                signal.countDown();
            }
        };
    }

    private ServiceEventListener kidoAuthCallback(final CountDownLatch signal) {
        return new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(200));
                signal.countDown();
            }
        };
    }

}