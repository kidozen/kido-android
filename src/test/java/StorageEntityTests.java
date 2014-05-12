import com.google.gson.Gson;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Created by christian on 5/11/14.
 */
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)
public class StorageEntityTests {
    private static final String KZ_STORAGE_SERVICEID = "StorageIntegrationTestsCollection";
    public static final int TEST_TIMEOUT_IN_SECONDS = 10;
    public static final String DATA_VALUE_KEY = "value";
    KZApplication kidozen = null;

    Storage _storage;

    //@Before
    public void Setup()
    {
        try {
            final CountDownLatch signalInit = new CountDownLatch(1);
            kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, false, kidoInitCallback(signalInit));
            signalInit.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            final CountDownLatch signalAuth = new CountDownLatch(1);
            kidozen.Authenticate(IntegrationTestConfiguration.KZ_PROVIDER, IntegrationTestConfiguration.KZ_USER, IntegrationTestConfiguration.KZ_PASS, kidoAuthCallback(signalAuth));
            signalAuth.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            _storage = kidozen.Storage(KZ_STORAGE_SERVICEID);
        }
        catch (Exception e)
        {
            fail();
        }
    }
    @Test
    public void ShouldCreateMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        //Gson gson = new Gson();

        MyComplexObject obj = new MyComplexObject();
        obj.setMyIntegerValue(3);
        obj.MyPublicListOFStrings = new ArrayList<String>();
        obj.MyPublicListOFStrings.add("1");

        MySerializer x = new MySerializer();

        //String json = gson.toJson(obj);
        String json = x.toJsonString("ddsee");
        //MyComplexObject one = gson.fromJson(json, MyComplexObject.class);
        String one = x.fromJson(json, String.class);

        //System.out.println(one.toString());

    }


    private ServiceEventListener kidoInitCallback(final CountDownLatch signal) {
        return new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                signal.countDown();
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_OK));
            }
        };
    }

    private ServiceEventListener kidoAuthCallback(final CountDownLatch signal) {
        return new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                signal.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        };
    }


    class MyComplexObject {
        public String PublicStringMember;
        private int _myIntegerValue;
/*
        public int getMyIntegerValue() {
            return _myIntegerValue;
        }
*/
        public void setMyIntegerValue(int _myIntegerValue) {
            this._myIntegerValue = _myIntegerValue;
        }

        public MyComplexObject(int x) {
            super();
            _myIntegerValue = x;
        }

        private MyComplexObject() {
            _myIntegerValue = -1;
        }

        public List<String> MyPublicListOFStrings;
    }


    class MySerializer {
        Gson gson = new Gson();

        public String toJsonString(Object obj) {
            return gson.toJson(obj);
        }

        @SuppressWarnings("unchecked")
        public <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
            if (json == null) {
                return null;
            }
            StringReader reader = new StringReader(json);
            T target = (T) gson.fromJson(reader, typeOfT);
            return target;
        }
    }
}


