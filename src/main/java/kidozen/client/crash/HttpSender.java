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
import java.util.Hashtable;
import kidozen.client.KZHttpMethod;
import kidozen.client.SNIConnectionManager;

import org.apache.http.HttpStatus;

public class HttpSender implements ReportSender {
    private String _endpoint;
    private SNIConnectionManager _sniManager;
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String CONTENT_TYPE = "content-type";

    public HttpSender(String formUri) {
        _endpoint = formUri;
    }

    @Override
    public void send(CrashReportData report) throws ReportSenderException {
        try {
            Hashtable<String, String> headers = new Hashtable<String, String>();
            headers.put(CONTENT_TYPE, APPLICATION_JSON);
            _sniManager = new SNIConnectionManager(_endpoint, report.toJSON().toString(), headers, null, true);
            Hashtable<String, String> response = _sniManager.ExecuteHttp(KZHttpMethod.POST);
            String body = response.get("responseBody");
            Integer statusCode = Integer.parseInt(response.get("statusCode"));
            if (statusCode>= HttpStatus.SC_MULTIPLE_CHOICES) {
                String exceptionMessage = (body!=null ? body : "Unexpected HTTP Status Code: " + statusCode);
                throw new Exception(exceptionMessage);
            }
        }
        catch (Exception e) {
            throw new ReportSenderException("Error while sending  report to KidoZen services." , e);
        }
    }
}