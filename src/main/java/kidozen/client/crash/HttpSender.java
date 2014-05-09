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

import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.Constants;
import kidozen.client.KZHttpMethod;
import kidozen.client.KZService;
import kidozen.client.SNIConnectionManager;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.authentication.IdentityManager;
import kidozen.client.authentication.KidoZenUser;

import org.apache.http.HttpStatus;

import static kidozen.client.crash.CrashReporter.LOG_TAG;

public class HttpSender extends KZService implements ReportSender {
    private String _endpoint;
    private SNIConnectionManager _sniManager;
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String CONTENT_TYPE = "content-type";
    private long DEFAULT_TIMEOUT = 2;
    private String mApplicationKey = "none";
    private String mToken = "empty";

    public HttpSender(String formUri, String token) {
        mApplicationKey = token;
        _endpoint = formUri;
    }

    @Override
    public void send(CrashReportData report) throws ReportSenderException {
        try
        {
            final CountDownLatch cdl = new CountDownLatch(1);
            Log.d(LOG_TAG, String.format("About to call IdentityManager.getInstance, Token: %s"
                    , mApplicationKey));

            IdentityManager.getInstance().GetRawToken(mApplicationKey, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                mToken = ((KidoZenUser) e.Response).Token;
                Log.d(LOG_TAG, String.format("Token in Crash HTTPSender: %s ", mToken));

                cdl.countDown();
                }
            });
            cdl.await(DEFAULT_TIMEOUT, TimeUnit.MINUTES);
            String authHeaderValue = String.format("WRAP access_token=\"%s\"", mToken);

            Log.d(LOG_TAG, String.format("About to send log to Log V3 service: %s ", _endpoint));

            Hashtable<String, String> headers = new Hashtable<String, String>();
            headers.put(Constants.AUTHORIZATION_HEADER,authHeaderValue);
            headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
            headers.put(Constants.ACCEPT, Constants.APPLICATION_JSON);

            _sniManager = new SNIConnectionManager(_endpoint, report.toJSON().toString(), headers, null, true);
            Hashtable<String, String> response = _sniManager.ExecuteHttp(KZHttpMethod.POST);
            String body = response.get("responseBody");
            Integer statusCode = Integer.parseInt(response.get("statusCode"));
            if (statusCode>= HttpStatus.SC_MULTIPLE_CHOICES) {
                String exceptionMessage = (body!=null ? body : "Unexpected HTTP Status Code: " + statusCode);
                throw new Exception(exceptionMessage);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();

            throw new ReportSenderException("Timeout trying to send report to KidoZen services." , e);
        }

        catch (Exception e) {
            e.printStackTrace();

            throw new ReportSenderException("Error while sending  report to KidoZen services." , e);
        }


    }
}