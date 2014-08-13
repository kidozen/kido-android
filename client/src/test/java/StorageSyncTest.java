import junit.framework.Assert;

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
import kidozen.client.SynchronousException;
import kidozen.client.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by christian on 8/5/14.
 */
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(manifest= Config.NONE)
public class StorageSyncTest {

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
    public void ShouldCreateMessage() throws JSONException{
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");
        try {
            Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
            JSONObject result = storage.Create(data, true);
            Assert.assertNotNull(result);
            Assert.assertNotNull(result.getJSONObject("_metadata"));
        } catch (SynchronousException e) {
            fail();
        } catch (InterruptedException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void ShouldUpdateMessage() throws JSONException{
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");
        try {
            Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
            JSONObject result = storage.Create(data, true);
            Assert.assertNotNull(result);
            Assert.assertNotNull(result.getJSONObject("_metadata"));

            result.put(DATA_VALUE_KEY, "expected");

            JSONObject updated = storage.Update(result.getString("_id"), result);
            Assert.assertNotNull(updated);
            Assert.assertNotNull(updated.getJSONObject("_metadata"));

        } catch (SynchronousException e) {
            fail();
        } catch (InterruptedException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void ShouldDeleteMessage() throws JSONException{
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");
        try {
            Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
            JSONObject result = storage.Create(data, true);
            Assert.assertNotNull(result);
            Assert.assertNotNull(result.getJSONObject("_metadata"));

            result.put(DATA_VALUE_KEY, "expected");

            boolean deleted = storage.Delete(result.getString("_id"));
            assertTrue(deleted);

        } catch (SynchronousException e) {
            fail();
        } catch (InterruptedException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void ShouldGetMessage() throws JSONException{
        String expectedValue = AppSettings.CreateRandomValue();
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,expectedValue);
        try {
            Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
            JSONObject result = storage.Create(data, true);
            Assert.assertNotNull(result);
            Assert.assertNotNull(result.getJSONObject("_metadata"));

            result.put(DATA_VALUE_KEY, "expected");

            JSONObject lastcreated = storage.Get(result.getString("_id"));
            assertEquals(expectedValue, lastcreated.getString((DATA_VALUE_KEY)));

        } catch (SynchronousException e) {
            fail();
        } catch (InterruptedException e) {
            fail();
        } catch (TimeoutException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void ShouldGetAllObjects() throws JSONException{
        try {
            Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);

            JSONArray objects = storage.All();
            assertTrue(objects.length()>0);

        } catch (SynchronousException e) {
            fail();
        } catch (InterruptedException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void ShouldQueryObjects() throws JSONException{
        try {
            Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);

            final String expected = AppSettings.CreateRandomValue();
            JSONObject message = new JSONObject().put(DATA_VALUE_KEY,expected);
            storage.Create(message);
            String query = message.toString();

            JSONArray objects = storage.Query(query);
            assertEquals(expected,objects.getJSONObject(0).getString(DATA_VALUE_KEY));

        } catch (SynchronousException e) {
            fail();
        } catch (InterruptedException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void ShouldCreateObjectWhenCallSAVE() throws Exception {
        JSONObject data = new JSONObject()
                .put(DATA_VALUE_KEY, "ShouldCreateObjectWhenCallSAVE");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
        try {
            JSONObject result = storage.Save(data);
            assertNotNull(result);
        } catch (SynchronousException e) {
            fail();
        }
    }

    @Test
    public void ShouldCreatePublicObjectWhenCallSAVE() throws Exception {
        JSONObject data = new JSONObject()
                .put(DATA_VALUE_KEY, "ShouldCreateMessage");

        Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);

        try {
            JSONObject result  = storage.Save(data,false);
            assertNotNull(result);
        } catch (SynchronousException e) {
            fail();
        }
    }

    @Test
    public void ShouldUpdateObjectWhenCallSave() throws Exception {
        final String expected = "updated";
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY, AppSettings.CreateRandomValue());
        try {
            JSONObject newObject = _storage.Create(data);
            assertNotNull(newObject);
            newObject.put(DATA_VALUE_KEY, expected);

            JSONObject savedResult = _storage.Save(newObject);

            JSONObject getResult = _storage.Get(savedResult.getString("_id"));

            assertEquals(getResult.getString(DATA_VALUE_KEY), expected);
        } catch (SynchronousException e) {
            fail();
        }
    }

    @Test(expected = SynchronousException.class)
    public void ShouldTryToUpdateMessageAndReturnSynchronousEx() throws JSONException, SynchronousException {
        JSONObject data = new JSONObject().put(DATA_VALUE_KEY,"ShouldCreateMessage");

        try {
            Storage storage= kidozen.Storage(KZ_STORAGE_SERVICE_ID);
            JSONObject result = storage.Create(data, true);
            Assert.assertNotNull(result);
            Assert.assertNotNull(result.getJSONObject("_metadata"));

            JSONObject updated = storage.Update(result.getString("_id"),data);
            Assert.assertNotNull(updated);
            Assert.assertNotNull(updated.getJSONObject("_metadata"));
        } catch (TimeoutException e) {
            fail();
        }  catch (Exception e) {
            e.printStackTrace();
        }

    }

}
