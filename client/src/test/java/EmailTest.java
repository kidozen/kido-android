import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZApplication;
import kidozen.client.Mail;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.ServiceResponseListener;
import kidozen.client.SynchronousException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;


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
public class EmailTest {

    public static final int TEST_TIMEOUT_IN_MINUTES = 3;

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
    public void ShouldSendEmail() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        Mail mail = new Mail();
        mail.to(AppSettings.KZ_EMAIL_TO);
        mail.from(AppSettings.KZ_EMAIL_FROM);
        mail.subject(this.CreateRandomValue());
        mail.textBody(this.CreateRandomValue());

        kidozen.SendEmail(mail, sendCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldAttachFiles() throws Exception {
        List<String> attachs = new ArrayList<String>();
        attachs.add(AppSettings.KZ_EMAIL_ATTACH);

        final CountDownLatch lcd = new CountDownLatch(1);
        Mail mail = new Mail();
        mail.to(AppSettings.KZ_EMAIL_TO);
        mail.from(AppSettings.KZ_EMAIL_FROM);
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
        mail.to(AppSettings.KZ_EMAIL_TO);
        mail.from(AppSettings.KZ_EMAIL_FROM);
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
        mail.to(AppSettings.KZ_EMAIL_TO + "," +  AppSettings.KZ_EMAIL_FROM);
        mail.from(AppSettings.KZ_EMAIL_FROM);
        mail.subject(this.CreateRandomValue());
        mail.textBody(this.CreateRandomValue());

        kidozen.SendEmail(mail, sendCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }

    @Test
    public void ShouldSendMailWithServiceResponse() throws Exception, SynchronousException {
        final CountDownLatch lcd = new CountDownLatch(1);

        Mail mail = new Mail();
        mail.to(AppSettings.KZ_EMAIL_TO);
        mail.from(AppSettings.KZ_EMAIL_FROM);
        mail.subject(this.CreateRandomValue());
        mail.textBody(this.CreateRandomValue());

        kidozen.SendEmail(mail, new ServiceResponseListener() {
            @Override
            public void onError(int statusCode, String response) {
                fail();
            }

            @Override
            public void onSuccess(int statusCode, String response) {
                assertEquals(HttpStatus.SC_CREATED, statusCode);
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.SECONDS));
    }

    @Test
    public void ShouldSendMailSync() throws Exception, SynchronousException {
        Mail mail = new Mail();
        mail.to(AppSettings.KZ_EMAIL_TO);
        mail.from(AppSettings.KZ_EMAIL_FROM);
        mail.subject(this.CreateRandomValue());
        mail.textBody(this.CreateRandomValue());

        kidozen.SendEmail(mail);
        assertTrue(true);
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
        return AppSettings.CreateRandomValue();

    }

}

