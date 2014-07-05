import com.kidozen.client.TestHostActivity;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by christian on 2/28/14.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthPassiveTest {
    private TestHostActivity activity;
    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    public static final int TEST_TIMEOUT_IN_SECONDS = 5;

    KZApplication kidozen = null;

    String KZ_KEY = "fbOqR5UVjn6Y+bkp2Z17k0R7TrqHtmeuP758YOE0M/k=";
    String KZ_TENANT = "https://loadtests.qa.kidozen.com";
    String KZ_APP =  "passiveauthpluscrash";
    String KZ_USER =  "loadtests@kidozen.com";
    String KZ_PASS = "pass";

    @Before
    public void setup()  {
        activity = Robolectric.buildActivity(TestHostActivity.class).create().get();
    }

    @Test
    public void checkActivityNotNull() throws Exception {
        assertNotNull(activity);
    }

    @Test
    public void ShouldAuthenticteUsingProvider() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        kidozen = new KZApplication(KZ_TENANT, KZ_APP, KZ_KEY, false,  new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                lcd.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);

        final CountDownLatch alcd = new CountDownLatch(1);
        kidozen.Authenticate(activity, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
System.out.println("Status: " + String.valueOf(e.StatusCode));
System.out.println("Body: " + e.Body);
                alcd.countDown();
            }
        });
        alcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);


    }

}
