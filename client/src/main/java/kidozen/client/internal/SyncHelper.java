package kidozen.client.internal;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZService;
import kidozen.client.ServiceEventListener;
import kidozen.client.SynchronousException;
import kidozen.client.TimeoutException;

/**
 * Created by christian on 8/6/14.
 */
public class SyncHelper<T> {
    private String mMethodName ;
    private KZService mServiceInstance;
    private Class<?>[] mParamTypes;
    private int mStatusCode;

    public SyncHelper(KZService serviceInstance, String methodName, Class<?>... paramsTypes){
        mServiceInstance = serviceInstance;
        mMethodName = methodName;
        mParamTypes = paramsTypes;
    }

    public T Invoke(Object[] paramsValues) throws SynchronousException, TimeoutException {
        CountDownLatch latch = new CountDownLatch(1);
        SyncServiceEventListener listener = new SyncServiceEventListener(latch);
        try {
            mServiceInstance.getClass().getMethod(mMethodName, mParamTypes)
                .invoke(mServiceInstance, appendValue(paramsValues, listener));
            latch.await(mServiceInstance.getDefaultServiceTimeoutInSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new TimeoutException();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        finally {
            mStatusCode = listener.getStatusCode();
            System.out.println("Sync STATUS Code: " + String.valueOf(mStatusCode));
            System.out.println("Sync RESPONSE: " + listener.getStringNResponse());
            System.out.println("Sync ERROR: " + listener.getError());
            if (listener.getError()!=null)
                throw new SynchronousException();
            else
                return  (T)listener.getServiceResponse();
        }
    }

    private static Object[] appendValue(Object[] obj, Object newObj) {
        ArrayList<Object> temp = new ArrayList<Object>(Arrays.asList(obj));
        temp.add(newObj);
        return temp.toArray();
    }

    public int getStatusCode() {
        return mStatusCode;
    }
}
