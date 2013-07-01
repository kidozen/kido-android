import kidozen.client.*;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
public class EMailIntegrationTest {

    public static final int TIMEOUT = 3000;
    public static final String DATA_VALUE_KEY = "value";

    KZApplication kidozen = null;
    Storage _storage;

    @Before
    public void Setup()
    {
        try {
            final CountDownLatch signal = new CountDownLatch(2);
            kidozen = new KZApplication(IntegrationTestConfiguration.TENANT, IntegrationTestConfiguration.APP, true, kidoInitCallback(signal));
            kidozen.Authenticate(IntegrationTestConfiguration.PROVIDER, IntegrationTestConfiguration.USR, IntegrationTestConfiguration.PASS, kidoAuthCallback(signal));
            signal.await(TIMEOUT, TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            fail();
        }
    }
    @Test
    public void ShouldSendEmail() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        Mail mail = new Mail();
        mail.to(IntegrationTestConfiguration.EMAIL_TO);
        mail.from(IntegrationTestConfiguration.EMAIL_FROM);
        mail.subject(this.CreateRandomValue());
        mail.textBody(this.CreateRandomValue());

        kidozen.SendEmail(mail, sendCallback(lcd));

        assertTrue(lcd.await(TIMEOUT, TimeUnit.MILLISECONDS));
    }
    @Test
    public void ShouldSendEmailWithMultipleRecipients() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        Mail mail = new Mail();
        mail.to("christian.carnero@gmail.com,christian.carnero@tellago.com");
        mail.from("chris@kidozen.com");
        mail.subject(this.CreateRandomValue());
        mail.textBody(this.CreateRandomValue());

        kidozen.SendEmail(mail, sendCallback(lcd));

        assertTrue(lcd.await(TIMEOUT, TimeUnit.MILLISECONDS));
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

