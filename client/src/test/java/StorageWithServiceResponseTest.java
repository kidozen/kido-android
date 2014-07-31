import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.ServiceResponseHandler;
import kidozen.client.Storage;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 5/20/13
 * Time: 2:30 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)
public class StorageWithServiceResponseTest {

    private static final String KZ_STORAGE_SERVICE_ID = "StorageIntegrationTestsCollection";
    public static final int TEST_TIMEOUT_IN_SECONDS = 10;
    public static final String DATA_VALUE_KEY = "value";
    KZApplication kidozen = null;

    Storage _storage;

    @Before
    public void Setup()
    {
        try {
            final CountDownLatch signalInit = new CountDownLatch(1);
            kidozen = new KZApplication(AppSettings.KZ_TENANT, AppSettings.KZ_APP, AppSettings.KZ_KEY, false);
            signalInit.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            final CountDownLatch signalAuth = new CountDownLatch(1);
            kidozen.Authenticate(AppSettings.KZ_PROVIDER, AppSettings.KZ_USER, AppSettings.KZ_PASS, kidoAuthCallback(signalAuth));
            signalAuth.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            _storage = kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        }
        catch (Exception e)
        {
            fail();
        }
    }
    @Ignore
    public void ShouldCreateMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(2);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        storage.Create(data, defaultCreateListenerAsJObject(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Ignore
    public void ShouldCreatePrivateMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(2);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        storage.Create(data,true, defaultCreateListenerAsJObject(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Ignore
    public void ShouldCreatePublicMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(2);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        storage.Create(data, false, defaultCreateListenerAsJObject(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }

    private ServiceResponseHandler defaultCreateListenerAsJObject(final CountDownLatch lcd) {
        return new ServiceResponseHandler() {
            @Override
            public void OnStart() {
                System.out.println("OnStart");
                lcd.countDown();
            }

            @Override
            public void OnSuccess(int statusCode, String response) {
                System.out.println("OnSuccess String");
                fail();
            }

            @Override
            public void OnSuccess(int statusCode, JSONObject response) {
                System.out.println("OnSuccess JSONObject");
                lcd.countDown();
            }

            @Override
            public void OnSuccess(int statusCode, JSONArray response) {
                System.out.println("OnSuccess JSONArray");
                fail();
            }

            @Override
            public void OnError(int statusCode, String response) {
                System.out.println("OnError");
                fail();
            }
        };
    }

    private ServiceResponseHandler defaultCreateListenerAsJArray(final CountDownLatch lcd) {
        return new ServiceResponseHandler() {
            @Override
            public void OnStart() {
                System.out.println("OnStart");
                lcd.countDown();
            }

            @Override
            public void OnSuccess(int statusCode, String response) {
                System.out.println("OnSuccess String");
                fail();
            }

            @Override
            public void OnSuccess(int statusCode, JSONObject response) {
                System.out.println("OnSuccess JSONObject");
                fail();
            }

            @Override
            public void OnSuccess(int statusCode, JSONArray response) {
                System.out.println("OnSuccess JSONArray");
                lcd.countDown();
            }

            @Override
            public void OnError(int statusCode, String response) {
                System.out.println("OnError");
                fail();
            }
        };
    }

    @Ignore
    public void ShouldDeleteMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(2);
        final String expected = AppSettings.CreateRandomValue();
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,expected);
        StorageEventListener cb = createObjectForStorage(data);

        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);
        JSONObject obj =(JSONObject) cb.Event.Response;
        _storage.Delete(obj.getString("_id"),new ServiceResponseHandler() {
            @Override
            public void OnStart() {
                lcd.countDown();
            }

            @Override
            public void OnSuccess(int statusCode, String response) {
                assertThat(statusCode, equalTo( HttpStatus.SC_OK));
                lcd.countDown();
            }

            @Override
            public void OnSuccess(int statusCode, JSONObject response) {
                fail();
            }

            @Override
            public void OnSuccess(int statusCode, JSONArray response) {
                fail();
            }

            @Override
            public void OnError(int statusCode, String response) {
                fail();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));

    }
    @Ignore
    public void ShouldGetMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(2);
        final String expected = AppSettings.CreateRandomValue();
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,expected);
        StorageEventListener cb = createObjectForStorage(data);

        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);
        JSONObject obj =(JSONObject) cb.Event.Response;
        String id = obj.getString("_id");

        _storage.Get(id, new ServiceResponseHandler() {
            @Override
            public void OnStart() {
                lcd.countDown();
            }

            @Override
            public void OnSuccess(int statusCode, String response) {
                fail();
            }

            @Override
            public void OnSuccess(int statusCode, JSONObject response) {
                assertEquals(statusCode,equalTo(HttpStatus.SC_OK));
                lcd.countDown();
            }

            @Override
            public void OnSuccess(int statusCode, JSONArray response) {
                fail();
            }

            @Override
            public void OnError(int statusCode, String response) {
                fail();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Test
    public void ShouldDropCollection() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(2);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY, AppSettings.CreateRandomValue());

        Storage toDrop = kidozen.Storage("toDrop");
        toDrop.Create(data,defaultCreateListenerAsJObject(lcd));
        //Assert
        toDrop.Drop(new ServiceResponseHandler() {
            @Override
            public void OnStart() {
                lcd.countDown();
            }

            @Override
            public void OnSuccess(int statusCode, String response) {
                assertThat(statusCode,equalTo(HttpStatus.SC_OK));
                lcd.countDown();
            }

            @Override
            public void OnSuccess(int statusCode, JSONObject response) {
                fail();
            }

            @Override
            public void OnSuccess(int statusCode, JSONArray response) {
                fail();
            }

            @Override
            public void OnError(int statusCode, String response) {
                fail();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Ignore
    public void ShouldGetAllObjects() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(2);
        final String expected = AppSettings.CreateRandomValue();
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,expected);
        StorageEventListener cb = createObjectForStorage(data);
        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);

        _storage.All(defaultCreateListenerAsJArray(lcd));
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Ignore
    public void ShouldUpdateObject() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(2);
        final String expected = "updated";
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY, AppSettings.CreateRandomValue());
        StorageEventListener cb = createObjectForStorage(data);

        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);
        JSONObject updatedObject =(JSONObject) cb.Event.Response;

        updatedObject.put(DATA_VALUE_KEY, expected);

        //Assert
        _storage.Update(updatedObject.getString("_id"),updatedObject, defaultCreateListenerAsJObject(lcd));
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Ignore
    public void UpdateObjectShouldReturnException() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = "updated";
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY, AppSettings.CreateRandomValue());
        StorageEventListener cb = createObjectForStorage(data);

        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);
        JSONObject obj =(JSONObject) cb.Event.Response;

        data.put(DATA_VALUE_KEY,expected);

        //Assert
        _storage.Update(obj.getString("_id"),data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertNotNull(e.Exception);
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Ignore
    public void UpdateObjectShouldReturnConflict() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = "updated";
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY, AppSettings.CreateRandomValue());
        StorageEventListener cb = createObjectForStorage(data);

        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);
        JSONObject updatedObject =(JSONObject) cb.Event.Response;

        updatedObject.put(DATA_VALUE_KEY, expected);
        JSONObject metadata = updatedObject.getJSONObject("_metadata");
        metadata.put("sync","-1");
        updatedObject.put("_metadata", metadata);

        //Assert
        _storage.Update(updatedObject.getString("_id"),updatedObject, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_CONFLICT);
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Ignore
    public void ShouldQueryObject() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = AppSettings.CreateRandomValue();
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,expected);
        StorageEventListener cb = createObjectForStorage(data);

        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);
        String query = data.toString();
        //Assert
        _storage.Query(query, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_OK);
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }

    @Ignore
    public void ShouldQueryObjectAndReturnRequestedValues() throws Exception
    {
        //Robolectric.addHttpResponseRule();
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = AppSettings.CreateRandomValue();
        final String KEY2="additional";
        JSONObject data = new JSONObject()
                .put(DATA_VALUE_KEY, expected)
                .put(KEY2, AppSettings.CreateRandomValue());

        StorageEventListener cb = createObjectForStorage(data);

        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);
        String query = data.toString();
        String values = new JSONObject().put(DATA_VALUE_KEY,true).toString();

        //Assert
        _storage.Query(query, values, "{}", new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                try {
                    assertEquals(e.StatusCode, HttpStatus.SC_OK);
                    JSONObject obj = ((JSONArray) e.Response).getJSONObject(0);
                    assertNotNull(obj.getString(DATA_VALUE_KEY));
                    String fail = obj.getString(KEY2);
                } catch (JSONException je) {
                    String msg = je.getMessage();
                    System.out.println("msg = " + msg);

                    String expectedMessage = "No value for additional";
                    assertEquals(expectedMessage, msg);
                    lcd.countDown();
                } catch (Exception e1) {
                    fail();
                }
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }

    @Ignore
    public void CreateShouldThrowInvalidField() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject()
                .put(DATA_VALUE_KEY,"ShouldCreateMessage")
                .put("_id","abc");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        storage.Create(data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_CONFLICT));
                assertNotNull(e.Exception);
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));

    }

    @Ignore
    public void ShouldCreateObjectWhenCallSAVE() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject()
                .put(DATA_VALUE_KEY, "ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        storage.Save(data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_CREATED));
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }

    @Ignore
    public void ShouldCreatePublicObjectWhenCallSAVE() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject()
                .put(DATA_VALUE_KEY, "ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        storage.Save(data, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_CREATED));
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }

    @Ignore
    public void ShouldUpdateObjectWhenCallSave() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = "updated";
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY, AppSettings.CreateRandomValue());
        StorageEventListener cb = createObjectForStorage(data);

        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);
        JSONObject updatedObject =(JSONObject) cb.Event.Response;

        updatedObject.put(DATA_VALUE_KEY, expected);
        //Assert
        _storage.Save(updatedObject, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_OK);

                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }

    //
    private StorageEventListener createObjectForStorage(JSONObject d) throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        StorageEventListener cb = new StorageEventListener(lcd);
        _storage.Create(d, cb);
        lcd.await();
        return cb;
    }

    private class StorageEventListener implements ServiceEventListener {
        private CountDownLatch _count = new CountDownLatch(1);
        public ServiceEvent Event;

        public StorageEventListener() {}
        public StorageEventListener(CountDownLatch lcd) {
            _count = lcd;
        }

        @Override
        public void onFinish(ServiceEvent e) {
            this.Event =e;
            _count.countDown();
        }
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

}