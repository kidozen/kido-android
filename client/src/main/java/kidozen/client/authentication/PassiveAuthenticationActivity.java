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

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(PassiveAuthenticationResponseReceiver.ACTION_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.REQUEST_CODE, KZPassiveAuthBroadcastConstants.REQUEST_FAILED_CODE);
            broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.ERROR_DESCRIPTION, description);
            broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.ERROR_FAILING_URL, failingUrl);
            sendBroadcast(broadcastIntent);

            PassiveAuthenticationActivity.this.finish();
            progressDialog.dismiss();
            webView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
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
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(PassiveAuthenticationResponseReceiver.ACTION_RESP);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.REQUEST_CODE, KZPassiveAuthBroadcastConstants.REQUEST_FAILED_CODE);
                broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.ERROR_DESCRIPTION, payload);

                sendBroadcast(broadcastIntent);

                PassiveAuthenticationActivity.this.finish();
            }
            progressDialog.dismiss();

        }

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

            String payload = jsResult.replace(mPrefix,"");
            byte[] data = Base64.decode(payload, Base64.DEFAULT);

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(PassiveAuthenticationResponseReceiver.ACTION_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

            try {
                String jsonPayload = new String(data,"UTF-8");

                if (jsonPayload.contains(Constants.USER_SOURCE_AUTHORIZATION_CLAIM)) {
                    broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.REQUEST_CODE, KZPassiveAuthBroadcastConstants.REQUEST_COMPLETE_CODE);
                    broadcastIntent.putExtra(AUTH_SERVICE_PAYLOAD,jsonPayload);
                }
                else {
                    broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.REQUEST_CODE, KZPassiveAuthBroadcastConstants.REQUEST_FAILED_CODE);
                    broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.ERROR_DESCRIPTION, "unauthorized");
                }

            } catch (UnsupportedEncodingException e) {
                broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.REQUEST_CODE, KZPassiveAuthBroadcastConstants.REQUEST_FAILED_CODE);
                broadcastIntent.putExtra(KZPassiveAuthBroadcastConstants.ERROR_DESCRIPTION, e.getMessage());
            }
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
