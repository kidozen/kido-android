package kidozen.client.dataviz;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Hashtable;

import kidozen.client.KZHttpMethod;
import kidozen.client.internal.SNIConnectionManager;
import kidozen.client.internal.Utilities;

/**
 * This Activity will display a webView that will load the local data visualization file.
 */
public class DataVisualizationActivity extends Activity {
    private Configuration currentConfiguration;
    private WebView webView;
    private String applicationName;
    private String domain;
    private String dataVizName;
    private Boolean strictSSL;
    private String authHeaderValue;
    private SNIConnectionManager connectionManager;
    private String authenticationResponse;
    private String tenantMarketPlace;
    private String username;
    private String password;
    private String provider;

    private ProgressDialog progressDialog;

    /**
     * This class is in charge of downloading the zip file that contains the visualization file.
     */
    private class DataVisualizationZipDownloader extends AsyncTask<String, Void, Void> {
        private String mLastErrorMessage = null;

        protected Void doInBackground(String... params) {
            try {
                ByteArrayOutputStream os = (ByteArrayOutputStream)connectionManager.ExecuteHttpAsStream(KZHttpMethod.GET);
                FileOutputStream fos = new FileOutputStream (new File(Environment.getExternalStorageDirectory().getAbsolutePath(), dataVizName + ".zip"));
                os.writeTo(fos);
                os.close();
                fos.close();
                if (  Utilities.unpackZip(destinationDirectory(), zipFilePath())) {
                    this.replacePlaceholders();
                }
                else {
                    mLastErrorMessage = "Could not unzip file " + dataVizName + ".zip";
                }
            } catch (Exception e) {
                e.printStackTrace();
                mLastErrorMessage = e.getCause().getMessage();
            }
            return null;
        }

        /**
         * The loadUrl method call should be done in the main thread, this is why it's being done
         * here in the onPostExecute callback.
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            if (mLastErrorMessage==null) webView.loadUrl("file://" + indexFilePath());
            progressDialog.dismiss();
        }

        /**
         * The index.html file contains placeholders that need to be replaced. These placeholders
         * are required for the kido-js SDK to authenticate.
         */
        private void replacePlaceholders() {
            String indexString = Utilities.getStringFromFile(indexFilePath());
            indexString = indexString.replace("{{:options}}", optionsString());
            indexString = indexString.replace("{{:marketplace}}", "\""+ tenantMarketPlace +"\"");
            indexString = indexString.replace("{{:name}}", "\"" + applicationName + "\"");
            Utilities.writeStringToFile(indexString, indexFilePath());
        }

        /**
         * This method returns the string that will replace the {{:options}} placeholder that is
         * contained in the index.html file.
         *
         * @return the options string that will replace the {{:options}} placeholder.
         */
        private String optionsString() {

            // Doing this hackish thingy because I don't want anything to be escaped.
            if (username != null && provider != null && password != null) {
                String options = "{\"token\":" + authenticationResponse +
                        ", \"username\":" + "\"" + username + "\"" +
                        ", \"provider\":" + "\"" + provider + "\"" +
                        ", \"password\":" + "\"" +password + "\"" + "}";
                return options;

            } else {
                String options = "{\"token\":" + authenticationResponse + "}";
                return options;
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading visualizations");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setProgress(0); // set percentage completed to 0%
        progressDialog.show();

        this.configureParams();
        this.addWebView(this);
        this.downloadZipFileAndLoadInWebView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.removeTempFiles();
    }

    private void removeTempFiles() {
        File d= new File(destinationDirectory());
        deleteRecursive(d);

        File zipFile = new File(zipFilePath());
        zipFile.delete();
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    private String zipFilePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + dataVizName + ".zip";
    }

    private String indexFilePath() {
        return destinationDirectory() + "/index.html";
    }

    private String destinationDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + dataVizName;
    }


    private void configureParams() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.applicationName = extras.getString(DataVisualizationActivityConstants.APPLICATION_NAME);
            this.domain = extras.getString(DataVisualizationActivityConstants.DOMAIN);
            this.dataVizName = extras.getString(DataVisualizationActivityConstants.DATAVIZ_NAME);
            this.strictSSL = extras.getBoolean(DataVisualizationActivityConstants.STRICT_SSL);
            String token = extras.getString(DataVisualizationActivityConstants.AUTH_HEADER);
            this.authHeaderValue = String.format("WRAP access_token=\"%s\"", token);
            this.authenticationResponse = extras.getString(DataVisualizationActivityConstants.AUTH_RESPONSE);
            this.tenantMarketPlace = extras.getString(DataVisualizationActivityConstants.TENANT_MARKET_PLACE);

            if (tenantMarketPlace.endsWith("/")) {
                tenantMarketPlace = tenantMarketPlace.substring(0,tenantMarketPlace.length()-1);
            }


            this.username = extras.getString(DataVisualizationActivityConstants.USERNAME);
            this.password = extras.getString(DataVisualizationActivityConstants.PASSWORD);
            this.provider = extras.getString(DataVisualizationActivityConstants.PROVIDER);
        }
    }


    private void downloadZipFileAndLoadInWebView() {

        HashMap<String, String> params = new HashMap<String, String>();

        Hashtable<String, String> headers = new Hashtable<String, String>();
        headers.put("Authorization", this.authHeaderValue);
        String url = String.format("https://%s.%s/api/v2/visualizations/%s/app/download?type=mobile",this.applicationName,this.domain,this.dataVizName);
        //String url =   "https://" + this.applicationName + "." + this.domain + "/api/v2/visualizations/" + this.dataVizName + "/app/download?type=mobile";
        this.connectionManager = new SNIConnectionManager(url, "", headers, params, true);

        // Will effectively download the zip, after that, unzip it and replace the placeholders with
        // the corresponding values.
        new DataVisualizationZipDownloader().execute();

    }

    private void addWebView(Context context) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setPadding(0, 0, 0, 0);

        FrameLayout.LayoutParams frame = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        webView = new WebView(context);
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //TODO: Integrate with 'StrictSSL'
                if(!strictSSL)
                    handler.proceed(); // Ignore SSL certificate errors
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setLayoutParams(frame);

        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        // DEBUG.
        //webView.setWebContentsDebuggingEnabled(true);

        mainLayout.addView(webView);

        setContentView(mainLayout,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
    }

}
