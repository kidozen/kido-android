package kidozen.client.internal;

import org.apache.http.HttpStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZService;
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
    private Exception mError;

    public SyncHelper(KZService serviceInstance, String methodName, Class<?>... paramsTypes){
        mServiceInstance = serviceInstance;
        mMethodName = methodName;
        mParamTypes = paramsTypes;
    }

    public T Invoke(Object[] paramsValues) throws SynchronousException, TimeoutException {
        CountDownLatch latch = new CountDownLatch(1);
        SyncServiceEventListener listener = new SyncServiceEventListener(latch);
        try {
            //System.out.println("SyncHelper, Invoke, " + mServiceInstance.getClass().getName().toString());

            Method m = mServiceInstance.getClass().getMethod(mMethodName, mParamTypes);
            //System.out.println("SyncHelper, Invoke, " + m.getName().toString());

            m.invoke(mServiceInstance, appendValue(paramsValues, listener));

            latch.await(mServiceInstance.getDefaultServiceTimeoutInSeconds(), TimeUnit.SECONDS);

            mStatusCode = listener.getStatusCode();
            //System.out.println("mStatusCode : " + mStatusCode);
            if (mStatusCode>= HttpStatus.SC_BAD_REQUEST)  {
                mError = listener.getError();
                throw new SynchronousException(listener.getStringResponse());
            }
            else return  (T)listener.getServiceResponse();
        } catch (InterruptedException e) {
            //System.out.println("InterruptedException : " + e.getMessage());
            throw new TimeoutException();
        } catch (InvocationTargetException e) {
            //System.out.println("InvocationTargetException : " + e.getMessage());
            throw  new SynchronousException(e.getMessage());
        } catch (NoSuchMethodException e) {
            //System.out.println("NoSuchMethodException : " + e.getMessage());
            throw  new SynchronousException(e.getMessage());
        } catch (IllegalAccessException e) {
            //System.out.println("IllegalAccessException : " + e.getMessage());
            throw  new SynchronousException(e.getMessage());
        }
    }

    private static Object[] appendValue(Object[] obj, Object newObj) {
        ArrayList<Object> temp = new ArrayList<Object>(Arrays.asList(obj));
        temp.add(newObj);
        for (int i = 0; i < temp.size(); i++) {
            //System.out.println("SyncHelper, appendValue, " + temp.get(i).toString());
        }
        return temp.toArray();
    }
    public Throwable getError() {
        return mError;
    }

    public int getStatusCode() {
        return mStatusCode;
    }
}
