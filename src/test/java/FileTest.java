import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
@Ignore
public class FileTest {
    public static final int TEST_TIMEOUT_IN_MINUTES = 10;
    public static final String FOTOS_TESTFILE_TXT = "/Users/christain/hosts";
    public static final String FOTOS_TESTDIR = "/fotos/";
    KZApplication kidozen = null;
    public static final String FILE_CONTENT = "This is a String ~ GoGoGo";

    @Before
    public void Setup()
    {
        try {
            final CountDownLatch signal = new CountDownLatch(2);
            kidozen = new KZApplication(TestConfiguration.KZ_TENANT, TestConfiguration.KZ_APP, false, kidoInitCallback(signal));
            kidozen.Authenticate(TestConfiguration.KZ_PROVIDER, TestConfiguration.KZ_USER, TestConfiguration.KZ_PASS, kidoAuthCallback(signal));
            signal.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    @Test
    public void ShouldAddInputStreamToFiles() throws Exception {
        InputStream is = new ByteArrayInputStream(FILE_CONTENT.getBytes());
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen.FileStorage().Upload(is, FOTOS_TESTFILE_TXT, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    public void ShouldGetFile() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen.FileStorage().Download(FOTOS_TESTFILE_TXT, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                ByteArrayOutputStream response = (ByteArrayOutputStream) e.Response;
                String fileResponse = response.toString();
                assertThat(fileResponse, equalTo(FILE_CONTENT));
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    public void ShouldBrowseFiles() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen.FileStorage().Browse(FOTOS_TESTDIR, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    public void ShouldRemoveFile() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen.FileStorage().Delete(FOTOS_TESTFILE_TXT, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_NO_CONTENT));
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    //
    private ServiceEventListener sendCallback(final CountDownLatch signal) {
        return  new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_CREATED));
                signal.countDown();
            }
        };
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

