import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
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
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 5/27/13
 * Time: 4:17 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)
public class FileIntegrationTest {

    public static final int TEST_TIMEOUT_IN_MINUTES = 3;
    public static final String DATA_VALUE_KEY = "value";

    KZApplication kidozen = null;
    Storage _storage;

    @Before
    public void Setup()
    {
        try {
            final CountDownLatch signal = new CountDownLatch(2);
            kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, true, kidoInitCallback(signal));
            kidozen.Authenticate(IntegrationTestConfiguration.KZ_PROVIDER, IntegrationTestConfiguration.KZ_USER, IntegrationTestConfiguration.KZ_PASS, kidoAuthCallback(signal));
            signal.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        }
        catch (Exception e)
        {
            fail();
        }
    }
    @Test
    public void ShouldUploadFile() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen.FileStorage().Upload("/Users/christian/upload.rtf", new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    public void ShouldDownloadFile() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen.FileStorage().Download("/users/christian/upload.rtf", new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                try
                {
                    ByteArrayOutputStream response = (ByteArrayOutputStream) e.Response;

                    OutputStream outputStream = new FileOutputStream("/Users/christian/Downloads/download.rtf");
                    response.writeTo(outputStream);

                    assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                }
                catch (Exception ex)
                {
                    fail();
                }
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

    private String CreateRandomValue()
    {
        Random rng= new Random();
        String characters ="qwertyuiopñlkjhgfdsazxcvbnm";
        char[] text = new char[10];
        for (int i = 0; i < 10; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);

    }
}

