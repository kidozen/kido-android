package kidozen.client.authentication;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.io.UnsupportedEncodingException;

/**
 * Created by christian on 6/17/14.
 */
public class PassiveAuthenticationActivity extends Activity {
    public static final int REQUEST_COMPLETE = 100;
    public static final int REQUEST_FAILED = 1000;
    public static final String ERROR_DESCRIPTION = "";
    public static final String ERROR_FAILING_URL = "";
    public static final int ERROR_CODE = 0;

    public static final String AUTHENTICATION_RESULT = "PASSIVE_AUTHENTICATION_RESULT";
    public static final String AUTH_SERVICE_PAYLOAD = "AUTH_SERVICE_PAYLOAD";

    private static final String mPrefix="Success payload=";
    private static final String mFailPrefix="Error message=";

    private WebView webView;
    private Boolean mStrictSSL = true;
    private WebChromeClient webChromeClient;
    private ProgressDialog progressDialog;


    private class AuthenticationWebViewClient extends WebViewClient {
        private static final java.lang.String GET_TITLE_FN = "javascript:( function () { window.HTMLOUT.getTitleCallback(document.title); } ) ()";
        protected final String TAG = AuthenticationWebViewClient.class.getSimpleName();

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            Intent broadcastIntent = createResponseReceiverIntent(REQUEST_FAILED);
            //broadcastIntent.putExtra(ERROR_CODE, errorCode);
            broadcastIntent.putExtra(ERROR_DESCRIPTION, description);
            broadcastIntent.putExtra(ERROR_FAILING_URL, failingUrl);
            sendBroadcast(broadcastIntent);
            PassiveAuthenticationActivity.this.finish();
            progressDialog.dismiss();
            webView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //TODO: Integrate with 'StrictSSL'
            if(!mStrictSSL)
                handler.proceed(); // Ignore SSL certificate errors
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String payload = view.getTitle();
            if ( payload.indexOf(mPrefix) > -1 ) {
                webView.loadUrl(GET_TITLE_FN);

            } else if ( payload.indexOf(mFailPrefix) > -1 ) {
                Intent broadcastIntent = createResponseReceiverIntent(REQUEST_FAILED);
                broadcastIntent.putExtra(ERROR_DESCRIPTION, "error description");
                sendBroadcast(broadcastIntent);
                PassiveAuthenticationActivity.this.finish();
            }
            progressDialog.dismiss();

        }

    }

    private Intent createResponseReceiverIntent(int value) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(PassiveAuthenticationResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(AUTHENTICATION_RESULT, value);

        return broadcastIntent;
    }

    WebViewClient getWebViewClient() {
        return new AuthenticationWebViewClient();
    }

    WebChromeClient getWebChromeClient() {
        return new AuthenticationWebChromeClient();
    }

    private class AuthenticationWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int progress) {
            progressDialog.setProgress(progress);
        }
    }

    private class AuthenticationJavaScriptInterface {
        @JavascriptInterface
        public void getTitleCallback(String jsResult) {

            Intent broadcastIntent = createResponseReceiverIntent(REQUEST_COMPLETE);
            String payload = jsResult.replace(mPrefix,"");
            byte[] data = Base64.decode(payload, Base64.DEFAULT);
            String jsonPayload = null;
            try {
                jsonPayload = new String(data,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            broadcastIntent.putExtra(AUTH_SERVICE_PAYLOAD,jsonPayload);
            sendBroadcast(broadcastIntent);
            PassiveAuthenticationActivity.this.finish();

        }
    }

    AuthenticationJavaScriptInterface getJavaScriptInterface() {
        return new AuthenticationJavaScriptInterface();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String signInUrl = intent.getStringExtra(kidozen.client.authentication.IdentityManager.PASSIVE_SIGNIN_URL);
        mStrictSSL = Boolean.parseBoolean(intent.getStringExtra(kidozen.client.authentication.IdentityManager.PASSIVE_STRICT_SSL)) ;
        Context context = this;
        // no window title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setPadding(0, 0, 0, 0);

        FrameLayout.LayoutParams frame = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setProgress(0); // set percentage completed to 0%


        webView = new WebView(context);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(getWebViewClient());
        webView.setWebChromeClient(getWebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setLayoutParams(frame);
        webView.getSettings().setSavePassword(false);
        webView.loadUrl(signInUrl);

        webView.addJavascriptInterface(getJavaScriptInterface(), "HTMLOUT");

        mainLayout.addView(webView);

        setContentView(mainLayout,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        progressDialog.show();

    }
}
