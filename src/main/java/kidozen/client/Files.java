package kidozen.client;

import android.util.Log;

import com.sun.swing.internal.plaf.metal.resources.metal_sv;

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
	public Files(String filestorage, String provider , String username, String pass, KidoZenUser userIdentity, KidoZenUser applicationIdentity) {
        super(filestorage, "", provider, username, pass, userIdentity, applicationIdentity);
        mSelf = this;
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


        CreateAuthHeaderValue(_provider,_username,_password,new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {

                AbstractMap.SimpleEntry<String, String> nameAndPath = getNameAndPath(fullDestinationPath);

                HashMap<String, String> params = new HashMap<String, String>();
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER, token);
                headers.put(CONNECTION_HEADER, KEEP_ALIVE_HEADER_VALUE);
                headers.put(X_FILE_NAME_HEADER, nameAndPath.getKey());
                headers.put(CONTENT_TYPE_HEADER, APPLICATION_OCTET_STREAM_HEADER_VALUE);

                String url = mEndpoint + nameAndPath.getValue();
                mSelf.ProcessAsStream = true;
                new KZServiceAsyncTask(KZHttpMethod.POST,params,headers,fileStream,callback, StrictSSL).execute(url);
            }
    });

}

    /**
	 * Download a file
	 *
	 * @param fullFilePath The file path
	 * @param callback The callback with the result of the service call
    * */
    public void Download(final String filePath, final ServiceEventListener callback)
    {
        CreateAuthHeaderValue(_provider,_username,_password,new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {

                String fullFilePath = filePath;
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
                //this.ProcessAsStream = true;
                //this.ExecuteTask(url, KZHttpMethod.GET, params, headers, callback, StrictSSL);
                mSelf.ProcessAsStream = true;
                KZServiceAsyncTask at = new KZServiceAsyncTask(KZHttpMethod.GET,params,headers, callback, StrictSSL);
                at.execute(url);
            }
        });

    }
    /**
     * Deletes a file
     *
     * @param path The file path
     * @param callback The callback with the result of the service call
     * */
    public void Delete(final String path, final ServiceEventListener callback)
    {
        CreateAuthHeaderValue(_provider,_username,_password,new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {

                String fullpath = path;
                if (path.isEmpty() || path==null)
                    throw new IllegalArgumentException("path");
                if (!path.startsWith("/"))
                    fullpath = "/" + path;

                HashMap<String, String> params = new HashMap<String, String>();
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());
                headers.put(PRAGMA_HEADER, NO_CACHE);
                headers.put(CACHE_CONTROL_HEADER, NO_CACHE);

                String url = mEndpoint + fullpath;
                //this.ExecuteTask(url, KZHttpMethod.DELETE, params, headers, callback, StrictSSL);
                new KZServiceAsyncTask(KZHttpMethod.DELETE, params, headers, callback, StrictSSL).execute(url);
            }
        });

    }
    /**
     * Browse the folder
     *
     * @param path Folder's URL. The URL must ends with character '/'
     * @param callback The callback with the result of the service call
     * */
    public void Browse(final String path,final ServiceEventListener callback)
    {
        CreateAuthHeaderValue(_provider,_username,_password,new KZServiceEvent<String>() {
            @Override
            public void Fire(String token) {
                String fullpath = path;
                if (path.isEmpty() || path==null)
                    throw new IllegalArgumentException("path");
                if (path.lastIndexOf("/") + 1 != path.length() && path.length() > 1)
                    throw new IllegalArgumentException("path must finish with '/'");
                if (!path.startsWith("/"))
                    fullpath = "/" + path;

                HashMap<String, String> params = new HashMap<String, String>();
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constants.AUTHORIZATION_HEADER,CreateAuthHeaderValue());
                headers.put(PRAGMA_HEADER, NO_CACHE);
                headers.put(CACHE_CONTROL_HEADER, NO_CACHE);

                String url = mEndpoint + fullpath;
//        this.ExecuteTask(url, KZHttpMethod.GET, params, headers, callback, StrictSSL);
                new KZServiceAsyncTask(KZHttpMethod.GET, params, headers, callback, StrictSSL).execute(url);
            }
        });

    }

    private AbstractMap.SimpleEntry<String, String> getNameAndPath(String fullFilePath)
    {
        String[] paths = fullFilePath.split("/");
        String file = paths[paths.length-1];
        String path = fullFilePath.replace("/" + file, "");

        return  new AbstractMap.SimpleEntry<String, String>(file,path);
    }
}
