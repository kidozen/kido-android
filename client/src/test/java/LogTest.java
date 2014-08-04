import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZApplication;
import kidozen.client.LogLevel;
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

public class LogTest {
    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    public static final String DATA_VALUE_KEY = "value";
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
            fail(e.getMessage());
        }
    }

    @Test
    public void ShouldTruncateLog() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);

        kidozen.ClearLog(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_NO_CONTENT));
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldLogArrayOfIntegers() throws Exception {
        ArrayList<Integer> intArray = new ArrayList<Integer>() ;
        intArray.add(1);
        intArray.add(2);
        intArray.add(3);
        intArray.add(4);
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen.WriteLog("ShouldLogArrayOfIntegers",
                intArray,
                LogLevel.LogLevelCritical,
                createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldLogDictionaryOfStrings() throws Exception {
        Map<String, String> intArray = new HashMap<String, String>() ;
        intArray.put("a", "1");
        intArray.put("b", "2");
        intArray.put("c", "3");
        intArray.put("d", "4");
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen.WriteLog("ShouldLogDictionaryOfStrings",
                intArray,
                LogLevel.LogLevelCritical,
                createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldLogArrayOfStrings() throws Exception {
        ArrayList<String> intArray = new ArrayList<String>() ;
        intArray.add("a");
        intArray.add("b");
        intArray.add("c");
        intArray.add("d");
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen.WriteLog("ShouldLogArrayOfStrings",
                intArray,
                LogLevel.LogLevelCritical,
                createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldLogInteger() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen.WriteLog(null,
                365,
                LogLevel.LogLevelCritical,
                createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    public void ShouldLogIntegerWithMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen.WriteLog("ShouldLogInteger",
                365,
                LogLevel.LogLevelCritical,
                createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldLogStringWithMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        String data = "string content";
        kidozen.WriteLog("ShouldLogString",
                data,
                LogLevel.LogLevelCritical,
                createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldLogJSONObjectWithMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject().put("myProperty", 128);
        kidozen.WriteLog("ShouldLogJSONObject",
                data,
                LogLevel.LogLevelCritical,
                createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldGetAllLog() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(1);

        kidozen.AllLogMessages(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_OK));
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void QueryShouldReturnInvalidQuery() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(1);

        kidozen.QueryLog("{fail}",new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_BAD_REQUEST));
                assertTrue(e.Body.indexOf("Invalid query. It must be compliant to elasticsearch")>-1);
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    //@Test
    public void ShouldWriteMessageUsingKey() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);

        KZApplication k = new KZApplication(AppSettings.KZ_TENANT,
                AppSettings.KZ_APP,
                AppSettings.KZ_KEY,
                false);

        k.WriteLog("ShouldWriteMessageUsingKey","LoggingIntegrationTests",LogLevel.LogLevelCritical, createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));

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

