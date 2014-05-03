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

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.Storage;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
public class StorageWithKeyIntegrationTest {

    private static final String KZ_STORAGE_SERVICEID = "StorageIntegrationTestsCollection";
    public static final int TEST_TIMEOUT_IN_MINUTES = 1;
    public static final String DATA_VALUE_KEY = "value";
    KZApplication kidozen = null;
    
    Storage _storage;

    @Before
    public void Setup()
    {
        try {
            final CountDownLatch signal = new CountDownLatch(2);
            kidozen = new KZApplication(IntegrationTestConfiguration.KZ_TENANT, IntegrationTestConfiguration.KZ_APP, false, kidoInitCallback(signal));
            signal.await();
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
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICEID);
        storage.Create(data, createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    public void ShouldCreateMessageUsingKey() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICEID);
        storage.Create(data, createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    @Ignore
    public void ShouldCreatePrivateMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICEID);
        storage.Create(data,true, createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    @Ignore
    public void ShouldCreatePublicMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICEID);
        storage.Create(data, false, createCallback(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    @Ignore
    public void ShouldDeleteMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = this.CreateRandomValue();
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
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));

    }
    @Test
    @Ignore
    public void ShouldGetMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = this.CreateRandomValue();
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
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    @Ignore
    public void ShouldDropCollection() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(2);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,this.CreateRandomValue());

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
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    @Ignore
    public void ShouldGetAllObjects() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = this.CreateRandomValue();
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
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    public void ShouldUpdateObject() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = "updated";
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,this.CreateRandomValue());
        StorageEventListener cb = createObjectForStorage(data);

        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);
        JSONObject updatedObject =(JSONObject) cb.Event.Response;

        updatedObject.put(DATA_VALUE_KEY, expected);

        System.out.print("updated: " + updatedObject.toString());

        //Assert
        _storage.Update(updatedObject.getString("_id"),updatedObject, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertEquals(e.StatusCode, HttpStatus.SC_OK);
                System.out.print("updated response: " + e.Body);

                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    @Ignore
    public void UpdateObjectShouldReturnException() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = "updated";
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,this.CreateRandomValue());
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
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    @Ignore
    public void UpdateObjectShouldReturnConflict() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = "updated";
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,this.CreateRandomValue());
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
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    @Ignore
    public void ShouldQueryObject() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = this.CreateRandomValue();
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
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    @Ignore
    public void ShouldQueryObjectAndReturnRequestedValues() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = this.CreateRandomValue();
        final String KEY2="additional";
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,expected).put(KEY2, this.CreateRandomValue());
        StorageEventListener cb = createObjectForStorage(data);

        assertEquals(cb.Event.StatusCode, HttpStatus.SC_CREATED);
        String query = data.toString();
        String values = new JSONObject().put(DATA_VALUE_KEY,true).toString();

        //Assert
        _storage.Query(query,values,"{}", new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                try {
                    assertEquals(e.StatusCode, HttpStatus.SC_OK);
                    JSONObject obj = ((JSONArray) e.Response).getJSONObject(0);
                    assertNotNull(obj.getString(DATA_VALUE_KEY));
                    String fail = obj.getString(KEY2);
                }
                catch (JSONException je)
                {
                    String msg = je.getMessage();
                    String expectedMessage = "JSONObject[\"additional\"] not found.";
                    assertEquals(msg,expectedMessage);
                    lcd.countDown();
                }
                catch (Exception e1)
                {
                    fail();
                }
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    @Ignore
    public void CreateShouldThrowInvalidField() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject()
                .put(DATA_VALUE_KEY,"ShouldCreateMessage")
                .put("_id","abc");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICEID);
        storage.Create(data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo( HttpStatus.SC_CONFLICT));
                assertNotNull(e.Exception);
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));

    }
    @Test
    @Ignore
    public void ShouldCreateObjectWhenCallSAVE() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject()
                .put(DATA_VALUE_KEY, "ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICEID);
        storage.Save(data, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_CREATED));
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    @Ignore
    public void ShouldCreatePublicObjectWhenCallSAVE() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject()
                .put(DATA_VALUE_KEY, "ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICEID);
        storage.Save(data, false, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_CREATED));
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
    }
    @Test
    @Ignore
    public void ShouldUpdateObjectWhenCallSave() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = "updated";
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,this.CreateRandomValue());
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
        assertTrue(lcd.await(TEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES));
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
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
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