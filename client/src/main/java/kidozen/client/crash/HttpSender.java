/*
 *  Copyright 2010 Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kidozen.client.crash;

import android.util.Log;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.KZHttpMethod;
import kidozen.client.KZService;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.authentication.IdentityManager;
import kidozen.client.authentication.KidoZenUser;
import kidozen.client.internal.Constants;
import kidozen.client.internal.SNIConnectionManager;

import static kidozen.client.crash.CrashReporter.LOG_TAG;

public class HttpSender extends KZService implements ReportSender {
    private String mCrashEndpoint;
    private SNIConnectionManager mSniManager;
    private long DEFAULT_TIMEOUT = 2;
    private String mApplicationKey = "none";
    private String mToken = "empty";
    private ArrayList<String> mBreadCrumbs;
    private static final String APPLICATION_BREADCRUMB = "APPLICATION_BREADCRUMB";
    private ServiceEvent mEvent = null;


    public HttpSender(String crashEndpoint, String token) {
        mApplicationKey = token;
        mCrashEndpoint = crashEndpoint;
        mBreadCrumbs = new ArrayList<String>();
    }

    public void AddBreadCrumb(String value) {
        mBreadCrumbs.add(value);
    }

    @Override
    public void send(CrashReportData report) throws ReportSenderException {
        try
        {
            final CountDownLatch cdl = new CountDownLatch(1);
            IdentityManager.getInstance().GetRawToken(mApplicationKey, new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    mEvent = e;
                    cdl.countDown();
                }
            });
            cdl.await(DEFAULT_TIMEOUT, TimeUnit.MINUTES);
            if (mEvent.Exception!=null || mEvent.StatusCode >= HttpStatus.SC_BAD_REQUEST) throw new ReportSenderException(mEvent.Body);
            mToken = ((KidoZenUser) mEvent.Response).Token;

            String authHeaderValue = String.format("WRAP access_token=\"%s\"", mToken);

            Log.d(LOG_TAG, String.format("About to send log to Log V3 service: %s ", mCrashEndpoint));
            JSONObject reportAsJson = report.toJSON();

            String bc = new JSONArray(mBreadCrumbs).toString();
            reportAsJson.put(APPLICATION_BREADCRUMB, bc);

            Hashtable<String, String> headers = new Hashtable<String, String>();
            headers.put(Constants.AUTHORIZATION_HEADER,authHeaderValue);
            headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
            headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

            mSniManager = new SNIConnectionManager(mCrashEndpoint, reportAsJson.toString(), headers, null, true);
            Hashtable<String, String> response = mSniManager.ExecuteHttp(KZHttpMethod.POST);
            String body = response.get("responseBody");
            Integer statusCode = Integer.parseInt(response.get("statusCode"));
            if (statusCode>= HttpStatus.SC_MULTIPLE_CHOICES) {
                String exceptionMessage = (body!=null ? body : "Unexpected HTTP Status Code: " + statusCode);
                throw new Exception(exceptionMessage);
            }
        }
        catch (InterruptedException e) {
            throw new ReportSenderException("Timeout trying to send report to KidoZen services." , e);
        }
        catch (ReportSenderException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ReportSenderException("Error while sending  report to KidoZen services." , e);
        }
    }
}