import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import kidozen.client.SynchronousException;

import static junit.framework.Assert.assertNotNull;
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

public class LogSyncTest {
    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
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

    @Ignore
    public void ShouldTruncateLog() throws Exception, SynchronousException {
        assertTrue(kidozen.ClearLog());
    }

    @Test
    public void ShouldLogArrayOfIntegers() throws Exception {
        ArrayList<Integer> intArray = new ArrayList<Integer>() ;
        intArray.add(1);
        intArray.add(2);
        intArray.add(3);
        intArray.add(4);

        try {
            kidozen.WriteLog("ShouldLogArrayOfIntegers Sync",
                    intArray,
                    LogLevel.LogLevelCritical);
            assertTrue(true);
        } catch (SynchronousException e) {
            fail();
        }
    }

    @Test
    public void ShouldLogDictionaryOfStrings() throws Exception {
        Map<String, String> intArray = new HashMap<String, String>() ;
        intArray.put("a", "1");
        intArray.put("b", "2");
        intArray.put("c", "3");
        intArray.put("d", "4");
        try {
            kidozen.WriteLog("ShouldLogDictionaryOfStringsSync",
                    intArray,
                    LogLevel.LogLevelCritical);
            assertTrue(true);
        } catch (SynchronousException e) {
            fail();
        }
    }

    @Test
    public void ShouldLogArrayOfStrings() throws Exception {
        ArrayList<String> intArray = new ArrayList<String>() ;
        intArray.add("a");
        intArray.add("b");
        intArray.add("c");
        intArray.add("d");

        try {
            kidozen.WriteLog("ShouldLogArrayOfIntegersSync",
                    intArray,
                    LogLevel.LogLevelCritical);
            assertTrue(true);
        } catch (SynchronousException e) {
            fail();
        }
    }


    @Test
    public void ShouldLogIntegerWithMessage() throws Exception {
        try {
            kidozen.WriteLog("ShouldLogInteger",
                    365,
                    LogLevel.LogLevelCritical);
            assertTrue(true);
        } catch (SynchronousException e) {
            fail();
        }
    }

    @Test
    public void ShouldLogStringWithMessage() throws Exception {
        try {
            kidozen.WriteLog("ShouldLogString",
                    "string content",
                    LogLevel.LogLevelCritical);
            assertTrue(true);
        } catch (SynchronousException e) {
            fail();
        }
    }

    @Test
    public void ShouldLogJSONObjectWithMessage() throws Exception {
        JSONObject data = new JSONObject().put("myProperty", 128);
        try {
            kidozen.WriteLog("ShouldLogJSONObject",
                    data,
                    LogLevel.LogLevelCritical);
            assertTrue(true);
        } catch (SynchronousException e) {
            fail();
        }
    }

    @Test
    public void ShouldGetAllLog() throws Exception
    {
        try {
            JSONArray result = kidozen.AllLogMessages();

            assertNotNull(result);
        } catch (SynchronousException e) {
            fail();
        }

    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void QueryShouldReturnInvalidQuery() throws Exception, SynchronousException {
        expectedEx.expect(SynchronousException.class);
        expectedEx.expectMessage("Invalid query. It must be compliant to elasticsearch.");
        JSONArray result = kidozen.QueryLog("{fail}");
        assertNotNull(result);
    }

    //

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

