import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.FixMethodOrder;
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

public class FileTest {
    public static final int TEST_TIMEOUT_IN_MINUTES = 10;
    public static final String PICTURE_FILE = "/Users/christian/Pictures/mono.png";
    public static final String PICTURE_TEST_DIR = "/pictures/";
    KZApplication kidozen = null;
    public static final String FILE_CONTENT = "This is a String ~ GoGoGo";

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
    public void ShouldAddInputStreamToFiles() throws Exception {
        InputStream is = new ByteArrayInputStream(FILE_CONTENT.getBytes());
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen.FileStorage().Upload(is, PICTURE_FILE, new ServiceEventListener() {
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
        kidozen.FileStorage().Download(PICTURE_FILE, new ServiceEventListener() {
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
        kidozen.FileStorage().Browse(PICTURE_TEST_DIR, new ServiceEventListener() {
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
        kidozen.FileStorage().Delete(PICTURE_FILE, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_NO_CONTENT));
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
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

