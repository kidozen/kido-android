package kidozen.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import kidozen.client.authentication.KidoZenUser;
import android.util.Log;

import org.apache.http.HttpStatus;

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
    String _endpoint;
	String _number;

	public void update(Observable observable, Object data) {
		Log.d(TAG, "token updated");
		this.KidozenUser = (KidoZenUser) data;
	}


	/**
	 * Constructor
	 *
	 * You should not create a new instances of this constructor. Instead use the SMSSender["number"] method of the KZApplication object.
	 * @param endpoint The service endpoint
	 * @param name The sms number to send messages
	 */
	public Files(String endpoint)
	{
		_endpoint=endpoint;
	}

	/**
	 * Upload a file
	 * 
	 * @param fullFilePath The full path of the file. This includes the filename ("/myfolder/foo.txt")
	 * @param callback The callback with the result of the service call
	 */
	public void Upload(final String fullFilePath, final ServiceEventListener callback)
    {
        try {
            if (fullFilePath.isEmpty() || fullFilePath==null)
                throw new IllegalArgumentException("fullFilePath");

            AbstractMap.SimpleEntry<String, String> nameAndPath = getNameAndPath(fullFilePath);

            HashMap<String, String> params = new HashMap<String, String>();
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
            headers.put(CONNECTION_HEADER, KEEP_ALIVE_HEADER_VALUE);
            headers.put(X_FILE_NAME_HEADER, nameAndPath.getKey());
            headers.put(CONTENT_TYPE_HEADER, APPLICATION_OCTET_STREAM_HEADER_VALUE);

            String url = _endpoint + nameAndPath.getValue();
            FileInputStream fileInputStream = new FileInputStream(new File(fullFilePath) );
            this.ProcessAsStream = true;
            this.ExecuteTask(url, KZHttpMethod.POST, params, headers, callback,fileInputStream, BypassSSLVerification);
        } catch (FileNotFoundException e) {
            ServiceEvent fail = new ServiceEvent(this,HttpStatus.SC_NOT_FOUND, e.getMessage(), null, e);
            callback.onFinish(fail);
        }
	}

    /**
	 * Download a file
	 *
	 * @param path The file path
	 * @param callback The callback with the result of the service call
    * */
    public void Download(String fullFilePath, final ServiceEventListener callback)
    {
        if (fullFilePath.isEmpty() || fullFilePath==null)
            throw new IllegalArgumentException("fullFilePath");

        AbstractMap.SimpleEntry<String, String> nameAndPath = getNameAndPath(fullFilePath);

        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(AUTHORIZATION_HEADER,CreateAuthHeaderValue());
        headers.put("Pragma", "no-cache");
        headers.put("Cache-Control", "no-cache");

        String url = _endpoint + fullFilePath;
        this.ProcessAsStream = true;
        this.ExecuteTask(url, KZHttpMethod.GET, params, headers, callback, BypassSSLVerification);
    }
    /**
     * Deletes a file
     *
     * @param path The file path
     * @param callback The callback with the result of the service call
     * */
    public void Delete(String path)
    {

    }
    /**
     * Browse the folder
     *
     * @param path The file path
     * @param callback The callback with the result of the service call
     * */
    public void Browse(String path)
    {

    }
    private AbstractMap.SimpleEntry<String, String> getNameAndPath(String fullFilePath)
    {
        String[] paths = fullFilePath.toLowerCase().split("/");
        String file = paths[paths.length-1].toLowerCase();
        String path = fullFilePath.replace("/" + file,"").toLowerCase();

        return  new AbstractMap.SimpleEntry<String, String>(file,path);
    }
}
