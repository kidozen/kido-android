import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
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
public class StorageTest {

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
    @Test
    public void ShouldCreateMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        storage.Create(data, createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Test
    public void ShouldCreatePrivateMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        storage.Create(data,true, createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Test
    public void ShouldCreatePublicMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        storage.Create(data, false, createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Test
    public void ShouldDeleteMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = AppSettings.CreateRandomValue();
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,expected);
        StorageEventListener cb = createObjectForStorage(data);

        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);
        JSONObject obj =(JSONObject) cb.Event.Response;
        _storage.Delete(obj.getString("_id"), new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_OK));
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));

    }
    @Test
    public void ShouldGetMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = AppSettings.CreateRandomValue();
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,expected);
        StorageEventListener cb = createObjectForStorage(data);

        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);
        JSONObject obj =(JSONObject) cb.Event.Response;
        String id = obj.getString("_id");

        _storage.Get(id, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                try {
                    String value = ((JSONObject) e.Response).getString(DATA_VALUE_KEY);
                    assertEquals(expected, value);
                    lcd.countDown();
                } catch (JSONException e1) {
                    fail();
                }
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
        toDrop.Create(data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_CREATED);
                lcd.countDown();
            }
        });
        //Assert
        toDrop.Drop(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                try {
                    assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
                    lcd.countDown();
                } catch (Exception e1) {
                    fail();
                }
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Test
    public void ShouldGetAllObjects() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = AppSettings.CreateRandomValue();
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,expected);
        StorageEventListener cb = createObjectForStorage(data);
        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);

        //Assert
        _storage.All(new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                try {
                    assertTrue(((JSONArray)e.Response).length()>0);
                    lcd.countDown();
                } catch (Exception e1) {
                    fail();
                }
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Test
    public void ShouldUpdateObject() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = "updated";
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY, AppSettings.CreateRandomValue());
        StorageEventListener cb = createObjectForStorage(data);

        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);
        JSONObject updatedObject =(JSONObject) cb.Event.Response;

        updatedObject.put(DATA_VALUE_KEY, expected);

        //Assert
        _storage.Update(updatedObject.getString("_id"),updatedObject, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_OK);
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }

    @Test
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
    @Test
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

    @Test
    public void ShouldQueryObjectAndReturnRequestedValues() throws Exception
    {
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
                    //System.out.println("msg = " + msg);

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

    @Test(expected = IllegalArgumentException.class)
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
    @Test
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
    @Test
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
    @Test
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

    //
    private ServiceEventListener createCallback(final CountDownLatch signal) {
        return  new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                signal.countDown();
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_CREATED));
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

}