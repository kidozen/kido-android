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

import kidozen.client.DataSource;
import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by christian on 2/28/14.
 */
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)

public class DsTest {
    public static final int TEST_TIMEOUT_IN_MINUTES = 5;
    private static final String INVOKE_DATA_SOURCE_NAME = "InvokeCityWeather";
    private static final String QUERY_DATA_SOURCE_NAME = "GetCityWeather";
    KZApplication kidozen = null;
    JSONObject data;

    @Before
    public void Setup()
    {
        try {
            data = new JSONObject().put("city", "London");
            final CountDownLatch signal = new CountDownLatch(1);
            kidozen = new KZApplication(AppSettings.KZ_TENANT, AppSettings.KZ_APP, AppSettings.KZ_KEY, false);
            kidozen.Authenticate(AppSettings.KZ_PROVIDER, AppSettings.KZ_USER, AppSettings.KZ_PASS, kidoAuthCallback(signal));
            signal.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void ShouldExecuteInvokeWithParameters() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        DataSource dataSource = kidozen.DataSource(INVOKE_DATA_SOURCE_NAME);
        dataSource.Invoke(data,new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                //System.out.println(e.Body);
                //System.out.println(String.valueOf( e.StatusCode));

                assertEquals(HttpStatus.SC_OK, e.StatusCode);
                assertTrue(e.Body.indexOf("London")>-1);
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldExecuteInvokeWithDefaults() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        DataSource dataSource = kidozen.DataSource(INVOKE_DATA_SOURCE_NAME);

        dataSource.Invoke(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                //System.out.println(e.Body);
                //System.out.println(String.valueOf( e.StatusCode));

                assertEquals(HttpStatus.SC_OK, e.StatusCode);
                assertTrue(e.Body.indexOf("Buenos Aires")>-1);
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldExecuteQueryWithParameters() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        DataSource dataSource = kidozen.DataSource(QUERY_DATA_SOURCE_NAME);
        dataSource.Query(data,new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                //System.out.println(e.Body);
                //System.out.println(String.valueOf( e.StatusCode));

                assertTrue(e.Body.indexOf("London")>-1);
                assertEquals(HttpStatus.SC_OK, e.StatusCode);
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldExecuteQueryWithDefaults() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        DataSource dataSource = kidozen.DataSource(QUERY_DATA_SOURCE_NAME);

        dataSource.Query(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                //System.out.println(e.Body);
                //System.out.println(String.valueOf( e.StatusCode));

                assertTrue(e.Body.indexOf("Buenos Aires")>-1);
                assertEquals(HttpStatus.SC_OK, e.StatusCode);
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
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

}
