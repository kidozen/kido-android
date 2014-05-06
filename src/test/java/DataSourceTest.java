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

import kidozen.client.DataSource;
import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by christian on 2/28/14.
 */
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)

public class DataSourceTest {
    public static final int TEST_TIMEOUT_IN_MINUTES = 5;
    private static final String OPERATION_DATASOURCE_NAME = "WeatherBsAs";
    private static final String QUERY_DATASOURCE_NAME = "WeatherBsAs";
    private static final String OPERATION_PARAMS_DATASOURCE_NAME = "test-operation-params";
    private static final String QUERY_PARAMS_DATASOURCE_NAME = "test-query-params";
    KZApplication kidozen = null;

    @Before
    public void Setup()
    {
        try {
            final CountDownLatch signal = new CountDownLatch(2);
            kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, false, kidoInitCallback(signal));
            kidozen.Authenticate(IntegrationTestConfiguration.KZ_PROVIDER, IntegrationTestConfiguration.KZ_USER, IntegrationTestConfiguration.KZ_PASS, kidoAuthCallback(signal));
            signal.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void ShouldCallOperationInDataSource() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        DataSource dataSource = kidozen.DataSource(OPERATION_DATASOURCE_NAME);

        dataSource.Invoke(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(HttpStatus.SC_OK, e.StatusCode);

                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    @Ignore
    public void ShouldCallQueryInDataSourceWithParams() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        DataSource dataSource = kidozen.DataSource(QUERY_PARAMS_DATASOURCE_NAME);
        JSONObject data = new JSONObject().put("path","?k=kidozen");
        dataSource.Query(data,new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(HttpStatus.SC_OK, e.StatusCode);

                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    @Ignore
    public void ShouldCallOperationInDataSourceWithParams() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        DataSource dataSource = kidozen.DataSource(OPERATION_PARAMS_DATASOURCE_NAME);
        JSONObject data = new JSONObject().put("path","?k=kidozen");
        dataSource.Invoke(data,new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(HttpStatus.SC_OK, e.StatusCode);

                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    @Ignore
    public void ShouldCallQueryInDataSource() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        DataSource dataSource = kidozen.DataSource(QUERY_DATASOURCE_NAME);

        dataSource.Query(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
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
