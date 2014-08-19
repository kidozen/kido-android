package kidozen.client;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.AbstractMap;
import java.util.HashMap;

import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.SyncHelper;


/**
 * SMS  service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class Files extends KZService {
    private static final String TAG = "SMSSender";
    public static final String CONNECTION_HEADER = "Connection";
    public static final String X_FILE_NAME_HEADER = "x-file-name";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String APPLICATION_OCTET_STREAM_HEADER_VALUE = "application/octet-stream";
    public static final String KEEP_ALIVE_HEADER_VALUE = "Keep-Alive";
    public static final String PRAGMA_HEADER = "Pragma";
    public static final String CACHE_CONTROL_HEADER = "Cache-Control";
    public static final String NO_CACHE = "no-cache";
    private final Files mSelf;


    /**
	 * Constructor
	 *
	 * You should not create a new instances of this constructor. Instead use the SMSSender["number"] method of the KZApplication object.
	 * @param endpoint The service endpoint
	 */
	public Files(String filestorage, String provider , String username, String pass, String clientId, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(filestorage, "", provider, username, pass, clientId, userIdentity, applicationIdentity);
        mSelf = this;
    }

	/**
	 * Upload a file
	 *
     * @param inputStream an input stream that represents the file to upload.
	 * @param path The full path of the file. This includes the filename ("/myfolder/foo.txt")
	 * @param callback The callback with the result of the service call
	 */
	public void Upload(final InputStream inputStream, final String path, final ServiceEventListener callback)
    {
        if (path.isEmpty() || path==null)
            throw new IllegalArgumentException("path");
        AbstractMap.SimpleEntry<String, String> nameAndPath = getNameAndPath(path);

        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(CONNECTION_HEADER, KEEP_ALIVE_HEADER_VALUE);
        headers.put(X_FILE_NAME_HEADER, nameAndPath.getKey());
        headers.put(CONTENT_TYPE_HEADER, APPLICATION_OCTET_STREAM_HEADER_VALUE);

        String url = mEndpoint + nameAndPath.getValue();
        mSelf.setmProcessAsStream(true);
        new KZServiceAsyncTask(KZHttpMethod.POST,params,headers,inputStream,callback, getStrictSSL()).execute(url);
    }

    public JSONObject Upload(InputStream inputStream, String path) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONObject>(this, "Upload", InputStream.class, String.class, ServiceEventListener.class)
                .Invoke(new Object[]{inputStream, path});
    }

    /**
	 * Download a file
	 *
	 * @param fullFilePath The file path
	 * @param callback The callback with the result of the service call
    * */
    public void Download(final String filePath, final ServiceEventListener callback)
    {
        String fullFilePath = filePath;
        if (fullFilePath.isEmpty() || fullFilePath==null)
            throw new IllegalArgumentException("fullFilePath");
        if (!fullFilePath.startsWith("/")) {
            fullFilePath = "/" + fullFilePath;
        }

        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(PRAGMA_HEADER, NO_CACHE);
        headers.put(CACHE_CONTROL_HEADER, NO_CACHE);

        String url = mEndpoint + fullFilePath;
        mSelf.setmProcessAsStream(true);
        KZServiceAsyncTask at = new KZServiceAsyncTask(KZHttpMethod.GET,params,headers, callback, getStrictSSL());
        at.execute(url);
    }

    public JSONObject Download(String path) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONObject>(this, "Download", String.class, ServiceEventListener.class)
                .Invoke(new Object[]{ path});
    }
    /**
     * Deletes a file
     *
     * @param path The file path
     * @param callback The callback with the result of the service call
     * */
    public void Delete(final String path, final ServiceEventListener callback)
    {
        String fullPath = path;
        if (path.isEmpty() || path==null) throw new IllegalArgumentException("path");
        if (!path.startsWith("/")) fullPath = "/" + path;

        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(PRAGMA_HEADER, NO_CACHE);
        headers.put(CACHE_CONTROL_HEADER, NO_CACHE);

        String url = mEndpoint + fullPath;
        new KZServiceAsyncTask(KZHttpMethod.DELETE, params, headers, callback, getStrictSSL()).execute(url);
    }

    public boolean Delete(String path) throws TimeoutException, SynchronousException {
        SyncHelper<String> helper = new SyncHelper<String>(this, "Delete", String.class, ServiceEventListener.class);
        helper.Invoke(new Object[]{path});
        return (helper.getStatusCode() == HttpStatus.SC_OK);
    }
    /**
     * Browse the folder
     *
     * @param path Folder's URL. The URL must ends with character '/'
     * @param callback The callback with the result of the service call
     * */
    public void Browse(final String path,final ServiceEventListener callback)
    {
        String fullPath = path;
        if (path.isEmpty() || path==null) throw new IllegalArgumentException("path");
        if (path.lastIndexOf("/") + 1 != path.length() && path.length() > 1) throw new IllegalArgumentException("path must finish with '/'");
        if (!path.startsWith("/")) fullPath = "/" + path;

        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(PRAGMA_HEADER, NO_CACHE);
        headers.put(CACHE_CONTROL_HEADER, NO_CACHE);

        String url = mEndpoint + fullPath;
        new KZServiceAsyncTask(KZHttpMethod.GET, params, headers, callback, getStrictSSL()).execute(url);
    }

    public JSONObject Browse(String path) throws TimeoutException, SynchronousException {
        return new SyncHelper<JSONObject>(this, "Browse", String.class, ServiceEventListener.class)
                .Invoke(new Object[]{ path});
    }

    private AbstractMap.SimpleEntry<String, String> getNameAndPath(String fullFilePath)
    {
        String[] paths = fullFilePath.split("/");
        String file = paths[paths.length-1];
        String path = fullFilePath.replace("/" + file, "");

        return  new AbstractMap.SimpleEntry<String, String>(file,path);
    }
}
