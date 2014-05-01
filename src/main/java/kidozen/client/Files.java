package kidozen.client;

import android.util.Log;

import java.io.InputStream;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import kidozen.client.authentication.KidoZenUser;


/**
 * SMS  service interface
 * 
 * @author kidozen
 * @version 1.00, April 2013
 */
public class Files extends KZService  implements Observer {
    private static final String TAG = "SMSSender";
    public static final String CONNECTION_HEADER = "Connection";
    public static final String X_FILE_NAME_HEADER = "x-file-name";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String APPLICATION_OCTET_STREAM_HEADER_VALUE = "application/octet-stream";
    public static final String KEEP_ALIVE_HEADER_VALUE = "Keep-Alive";
    public static final String PRAGMA_HEADER = "Pragma";
    public static final String CACHE_CONTROL_HEADER = "Cache-Control";
    public static final String NO_CACHE = "no-cache";

	public void update(Observable observable, Object data) {
		Log.d(TAG, "token updated");
		this.mUserIdentity = (KidoZenUser) data;
	}


	/**
	 * Constructor
	 *
	 * You should not create a new instances of this constructor. Instead use the SMSSender["number"] method of the KZApplication object.
	 * @param endpoint The service endpoint
	 */
	public Files(String endpoint)
	{
        super();
        mEndpoint =endpoint;
	}

	/**
	 * Upload a file
	 *
     * @param fileStream an input stream that represents the file to upload.
	 * @param fullDestinationPath The full path of the file. This includes the filename ("/myfolder/foo.txt")
	 * @param callback The callback with the result of the service call
	 */
	public void Upload(final InputStream fileStream, final String fullDestinationPath, final ServiceEventListener callback)
    {
        if (fullDestinationPath.isEmpty() || fullDestinationPath==null)
            throw new IllegalArgumentException("fullDestinationPath");

        AbstractMap.SimpleEntry<String, String> nameAndPath = getNameAndPath(fullDestinationPath);

        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());
        headers.put(CONNECTION_HEADER, KEEP_ALIVE_HEADER_VALUE);
        headers.put(X_FILE_NAME_HEADER, nameAndPath.getKey());
        headers.put(CONTENT_TYPE_HEADER, APPLICATION_OCTET_STREAM_HEADER_VALUE);

        String url = mEndpoint + nameAndPath.getValue();

        this.ProcessAsStream = true;
        this.ExecuteTask(url, KZHttpMethod.POST, params, headers, callback,fileStream, BypassSSLVerification);
	}

    /**
	 * Download a file
	 *
	 * @param fullFilePath The file path
	 * @param callback The callback with the result of the service call
    * */
    public void Download(String fullFilePath, final ServiceEventListener callback)
    {
        if (fullFilePath.isEmpty() || fullFilePath==null)
            throw new IllegalArgumentException("fullFilePath");
        if (!fullFilePath.startsWith("/")) {
            fullFilePath = "/" + fullFilePath;
        }

        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());
        headers.put(PRAGMA_HEADER, NO_CACHE);
        headers.put(CACHE_CONTROL_HEADER, NO_CACHE);

        String url = mEndpoint + fullFilePath;
        this.ProcessAsStream = true;
        this.ExecuteTask(url, KZHttpMethod.GET, params, headers, callback, BypassSSLVerification);
    }
    /**
     * Deletes a file
     *
     * @param path The file path
     * @param callback The callback with the result of the service call
     * */
    public void Delete(String path, final ServiceEventListener callback)
    {
        if (path.isEmpty() || path==null)
            throw new IllegalArgumentException("path");
        if (!path.startsWith("/"))
            path = "/" + path;

        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());
        headers.put(PRAGMA_HEADER, NO_CACHE);
        headers.put(CACHE_CONTROL_HEADER, NO_CACHE);

        String url = mEndpoint + path;
        this.ExecuteTask(url, KZHttpMethod.DELETE, params, headers, callback, BypassSSLVerification);
    }
    /**
     * Browse the folder
     *
     * @param path Folder's URL. The URL must ends with character '/'
     * @param callback The callback with the result of the service call
     * */
    public void Browse(String path,final ServiceEventListener callback)
    {
        if (path.isEmpty() || path==null)
            throw new IllegalArgumentException("path");
        if (path.lastIndexOf("/") + 1 != path.length() && path.length() > 1)
            throw new IllegalArgumentException("path must finish with '/'");
        if (!path.startsWith("/"))
            path = "/" + path;

        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());
        headers.put(PRAGMA_HEADER, NO_CACHE);
        headers.put(CACHE_CONTROL_HEADER, NO_CACHE);

        String url = mEndpoint + path;
        this.ExecuteTask(url, KZHttpMethod.GET, params, headers, callback, BypassSSLVerification);
    }

    private AbstractMap.SimpleEntry<String, String> getNameAndPath(String fullFilePath)
    {
        String[] paths = fullFilePath.split("/");
        String file = paths[paths.length-1];
        String path = fullFilePath.replace("/" + file, "");

        return  new AbstractMap.SimpleEntry<String, String>(file,path);
    }
}
