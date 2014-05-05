import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZApplication;
import kidozen.client.Mail;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.Storage;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

//import static org.junit.Assert.assertTrue;



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

    public static final int TEST_TIMEOUT_IN_MINUTES = 3;
    public static final String DATA_VALUE_KEY = "value";

    KZApplication kidozen = null;
    Storage _storage;

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
    public void ShouldSendEmail() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        Mail mail = new Mail();
        mail.to(IntegrationTestConfiguration.KZ_EMAIL_TO);
        mail.from(IntegrationTestConfiguration.KZ_EMAIL_FROM);
        mail.subject(this.CreateRandomValue());
        mail.textBody(this.CreateRandomValue());

        kidozen.SendEmail(mail, sendCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    public void ShouldAttachFiles() throws Exception {
        List<String> attachs = new ArrayList<String>();
        attachs.add(IntegrationTestConfiguration.KZ_EMAIL_ATTACH);

        final CountDownLatch lcd = new CountDownLatch(1);
        Mail mail = new Mail();
        mail.to(IntegrationTestConfiguration.KZ_EMAIL_TO);
        mail.from(IntegrationTestConfiguration.KZ_EMAIL_FROM);
        mail.subject(this.CreateRandomValue());
        mail.textBody(this.CreateRandomValue());
        mail.attachments(attachs);

        kidozen.SendEmail(mail, sendCallback(lcd));
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    public void ShouldReturnInternalServerError() throws Exception {
        List<String> attachs = new ArrayList<String>();
        attachs.add("/folder/nofile.txt");

        final CountDownLatch lcd = new CountDownLatch(1);
        Mail mail = new Mail();
        mail.to(IntegrationTestConfiguration.KZ_EMAIL_TO);
        mail.from(IntegrationTestConfiguration.KZ_EMAIL_FROM);
        mail.subject(this.CreateRandomValue());
        mail.textBody(this.CreateRandomValue());
        mail.attachments(attachs);

        kidozen.SendEmail(mail, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_INTERNAL_SERVER_ERROR));
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    public void ShouldSendEmailWithMultipleRecipients() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        Mail mail = new Mail();
        mail.to(IntegrationTestConfiguration.KZ_EMAIL_TO + "," +  IntegrationTestConfiguration.KZ_EMAIL_FROM);
        mail.from(IntegrationTestConfiguration.KZ_EMAIL_FROM);
        mail.subject(this.CreateRandomValue());
        mail.textBody(this.CreateRandomValue());

        kidozen.SendEmail(mail, sendCallback(lcd));

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

