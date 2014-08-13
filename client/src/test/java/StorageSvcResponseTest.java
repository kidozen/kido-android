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
import kidozen.client.ServiceResponseListener;
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
public class StorageSvcResponseTest {

    private static final String KZ_STORAGE_SERVICE_ID = "StorageWithServiceResponseTestCollection";
    public static final int TEST_TIMEOUT_IN_SECONDS = 20;
    public static final String DATA_VALUE_KEY = "value";
    KZApplication kidozen = null;
    JSONObject mDefaultObjectToInsert;

    Storage _storage;

    @Before
    public void Setup()
    {
        try {
            mDefaultObjectToInsert = new JSONObject().put(DATA_VALUE_KEY, AppSettings.CreateRandomValue());

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

    private ServiceEventListener kidoAuthCallback(final CountDownLatch signal) {
        return new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                signal.countDown();
                assertThat(e.StatusCode, equalTo(HttpStatus.SC_OK));
            }
        };
    }

    @Test
    public void ShouldCreateMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(2);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        storage.Create(data, defaultCreateListenerAsJObject(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Test
    public void ShouldCreatePrivateMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(2);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        storage.Create(data,true, defaultCreateListenerAsJObject(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Test
    public void ShouldCreatePublicMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(2);
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        storage.Create(data, false, defaultCreateListenerAsJObject(lcd));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void ShouldDeleteMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(2);
        final CountDownLatch createCdl = new CountDownLatch(1);
        createDefaultObjectToInsert(createCdl);

        _storage.Delete(mDefaultObjectToInsert.getString("_id"),new ServiceResponseHandler() {
            @Override
            public void onStart() {
                lcd.countDown();
            }

            @Override
            public void onSuccess(int statusCode, String response) {
                assertThat(statusCode, equalTo( HttpStatus.SC_OK));
                lcd.countDown();
            }

            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                fail();
            }

            @Override
            public void onSuccess(int statusCode, JSONArray response) {
                fail();
            }

            @Override
            public void onError(int statusCode, String response) {
                fail();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));

    }
    @Test
    public void ShouldGetMessage() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(2);
        final CountDownLatch createCdl = new CountDownLatch(1);
        createDefaultObjectToInsert(createCdl);
        String id = mDefaultObjectToInsert.getString("_id");

        _storage.Get(id, new ServiceResponseHandler() {
            @Override
            public void onStart() {
                lcd.countDown();
            }

            @Override
            public void onSuccess(int statusCode, String response) {
                fail();
            }

            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                assertThat(statusCode, equalTo(HttpStatus.SC_OK));
                lcd.countDown();
            }

            @Override
            public void onSuccess(int statusCode, JSONArray response) {
                fail();
            }

            @Override
            public void onError(int statusCode, String response) {
                fail();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Test
    public void ShouldDropCollection() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(2);
        JSONObject mDefaultObjectToInsert = new JSONObject().put(DATA_VALUE_KEY, AppSettings.CreateRandomValue());

        Storage toDrop = kidozen.Storage("toDrop");
        toDrop.Create(mDefaultObjectToInsert,defaultCreateListenerAsJObject(lcd));
        //Assert
        toDrop.Drop(new ServiceResponseHandler() {
            @Override
            public void onStart() {
                lcd.countDown();
            }

            @Override
            public void onSuccess(int statusCode, String response) {
                assertThat(statusCode,equalTo(HttpStatus.SC_OK));
                lcd.countDown();
            }

            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                fail();
            }

            @Override
            public void onSuccess(int statusCode, JSONArray response) {
                fail();
            }

            @Override
            public void onError(int statusCode, String response) {
                fail();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }
    @Test
    public void ShouldGetAllObjects() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(2);
        final CountDownLatch createCdl = new CountDownLatch(1);
        createDefaultObjectToInsert(createCdl);

        _storage.All(defaultCreateListenerAsJArray(lcd));
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }


    @Ignore
    public void ShouldUpdateObject() throws Exception {
        final CountDownLatch createCdl = new CountDownLatch(1);
        final String expected = "updated";
        createDefaultObjectToInsert(createCdl);
        createCdl.await(TEST_TIMEOUT_IN_SECONDS,TimeUnit.SECONDS);
        mDefaultObjectToInsert.put(DATA_VALUE_KEY, expected);

        //Assert
        final CountDownLatch lcd = new CountDownLatch(1);
        _storage.Update(mDefaultObjectToInsert.getString("_id"), mDefaultObjectToInsert, new ServiceResponseListener() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                lcd.countDown();
                assertThat(statusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void UpdateObjectShouldReturnConflict() throws Exception {
        final CountDownLatch createCdl = new CountDownLatch(1);
        final String expected = "updated";
        createDefaultObjectToInsert(createCdl);
        createCdl.await(TEST_TIMEOUT_IN_SECONDS,TimeUnit.SECONDS);
        mDefaultObjectToInsert.put(DATA_VALUE_KEY, expected);


        JSONObject updatedObject = new JSONObject(mDefaultObjectToInsert.toString());
        updatedObject.put(DATA_VALUE_KEY, expected);
        JSONObject metadata = updatedObject.getJSONObject("_metadata");
        metadata.put("sync","-1");
        updatedObject.put("_metadata", metadata);

        final CountDownLatch lcd = new CountDownLatch(1);
        _storage.Update(updatedObject.getString("_id"),updatedObject, new ServiceResponseListener() {
            @Override
            public void onError(int statusCode, String response) {
                //System.out.println(response);
                assertEquals(statusCode, HttpStatus.SC_CONFLICT);
                lcd.countDown();
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void ShouldQueryObject() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = AppSettings.CreateRandomValue();
        final JSONObject data = new JSONObject().put(DATA_VALUE_KEY,expected);

        _storage.Create(data, new ServiceResponseListener() {
                @Override
                public void onSuccess(int statusCode, JSONObject response) {
                    String query = data.toString();
                    _storage.Query(query, new ServiceResponseListener() {
                        @Override
                        public void onSuccess(int statusCode, JSONArray response)
                        {
                            assertEquals(statusCode, HttpStatus.SC_OK);
                            lcd.countDown();
                        }
                        @Override
                        public void onError(int statusCode, String response)
                        {
                            fail();
                        }
                    });
                }
            }
        );
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }


    @Test
    public void ShouldQueryObjectAndReturnOnlyRequestedValues() throws Exception
    {
        //Robolectric.addHttpResponseRule();
        final CountDownLatch lcd = new CountDownLatch(1);
        final String expected = AppSettings.CreateRandomValue();
        final String KEY2="additional";
        final JSONObject data = new JSONObject()
                .put(DATA_VALUE_KEY, expected)
                .put(KEY2, AppSettings.CreateRandomValue());

        final String query = data.toString();
        final String values = new JSONObject().put(DATA_VALUE_KEY,true).toString();

        _storage.Create(data, new ServiceResponseListener() {
                    @Override
                    public void onSuccess(int statusCode, JSONObject response) {

                        _storage.Query(query, values, "{}" , new ServiceResponseListener() {
                            @Override
                            public void onSuccess(int statusCode, JSONArray response)
                            {
                                assertEquals(statusCode, HttpStatus.SC_OK);
                                try {
                                    JSONObject obj = response.getJSONObject(0);
                                    assertNotNull(obj.getString(DATA_VALUE_KEY));
                                    // The following call should throw an JSONException, because the Query overload should only return the value for 'DATA_VALUE_KEY'
                                    String fail = obj.getString(KEY2);
                                }
                                catch (JSONException je) {
                                    String msg = je.getMessage();
                                    //System.out.println("msg = " + msg);

                                    String expectedMessage = "No value for additional";
                                    assertEquals(expectedMessage, msg);
                                }
                                catch (Exception e) {
                                    fail();
                                }
                                finally {
                                    lcd.countDown();
                                }
                            }
                            @Override
                            public void onError(int statusCode, String response)
                            {
                                fail();
                            }
                        });
                    }
                }
        );

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void CreateShouldThrowInvalidArgument() throws Exception
    {
        final CountDownLatch lcd = new CountDownLatch(1);
        JSONObject data = new JSONObject()
                .put(DATA_VALUE_KEY,"ShouldCreateMessage")
                .put("_id","abc");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        storage.Create(data, new ServiceResponseListener() {
            @Override
            public void onError(int status, String response) {
                assertThat(status, equalTo(HttpStatus.SC_CONFLICT));
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
        storage.Save(data, new ServiceResponseListener() {
            @Override
            public void onSuccess(int status, JSONObject response) {
                assertThat(status, equalTo(HttpStatus.SC_CREATED));
                lcd.countDown();
            }
        });

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void ShouldUpdateObjectWhenCallSave() throws Exception {

        final CountDownLatch createCdl = new CountDownLatch(1);
        final String expected = "updated";
        createDefaultObjectToInsert(createCdl);
        createCdl.await(TEST_TIMEOUT_IN_SECONDS,TimeUnit.SECONDS);
        mDefaultObjectToInsert.put(DATA_VALUE_KEY, expected);

        //Assert
        final CountDownLatch lcd = new CountDownLatch(1);
        _storage.Save( mDefaultObjectToInsert, new ServiceResponseListener() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                lcd.countDown();
                assertThat(statusCode, equalTo(HttpStatus.SC_OK));
            }
        });
        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));

        assertTrue(lcd.await(TEST_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
    }


    private ServiceResponseHandler defaultCreateListenerAsJObject(final CountDownLatch lcd) {
        return new ServiceResponseHandler() {
            @Override
            public void onStart() {
                //System.out.println("onStart");
                lcd.countDown();
            }

            @Override
            public void onSuccess(int statusCode, String response) {
                //System.out.println("onSuccess String");
                fail();
            }

            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                //System.out.println("onSuccess JSONObject");
                lcd.countDown();
            }

            @Override
            public void onSuccess(int statusCode, JSONArray response) {
                //System.out.println("onSuccess JSONArray");
                fail();
            }

            @Override
            public void onError(int statusCode, String response) {
                //System.out.println("onError");
                fail();
            }
        };
    }

    private ServiceResponseHandler defaultCreateListenerAsJArray(final CountDownLatch lcd) {
        return new ServiceResponseHandler() {
            @Override
            public void onStart() {
                //System.out.println("onStart");
                lcd.countDown();
            }

            @Override
            public void onSuccess(int statusCode, String response) {
                //System.out.println("onSuccess String");
                fail();
            }

            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                //System.out.println("onSuccess JSONObject");
                fail();
            }

            @Override
            public void onSuccess(int statusCode, JSONArray response) {
                //System.out.println("onSuccess JSONArray");
                lcd.countDown();
            }

            @Override
            public void onError(int statusCode, String response) {
                //System.out.println("onError");
                fail();
            }

        };
    }

    private void createDefaultObjectToInsert(final CountDownLatch createCdl) {
        _storage.Create(mDefaultObjectToInsert, new ServiceResponseListener() {
            @Override
            public void onError(int statusCode, String response) {
                fail();
            }

            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                mDefaultObjectToInsert = response;
                createCdl.countDown();
            }
        });
    }

}