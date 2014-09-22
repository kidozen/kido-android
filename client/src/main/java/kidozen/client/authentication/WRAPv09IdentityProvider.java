package kidozen.client.authentication;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import kidozen.client.KZHttpMethod;
import kidozen.client.internal.SNIConnectionManager;
import kidozen.client.internal.Utilities;

/**
 * WRAP V 09 Identity Provider
 * 
 * @author KidoZen
 * @version 1.00, April 2013
 */
public class WRAPv09IdentityProvider extends BaseIdentityProvider {
	private String mWrapName,
            mWrapPassword,
            mWrapScope,
            mIpEndpoint;
	public Boolean StrictSSL = true;

    public WRAPv09IdentityProvider(String username, String password, String endpoint, String scope) {
        this.mWrapName =  username;
        this.mWrapPassword = password;
        this.mIpEndpoint = endpoint;
        this.mWrapScope = scope;
    }

    public String RequestToken() throws Exception {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("wrap_name", mWrapName));
		nameValuePairs.add(new BasicNameValuePair("wrap_password", mWrapPassword));
		nameValuePairs.add(new BasicNameValuePair("wrap_scope", mWrapScope));
        String statusCode = "";
        String body = "";
        try {
            String message = Utilities.getQuery(nameValuePairs);
            //System.out.println("IP Message: " + message);
            SNIConnectionManager sniManager = new SNIConnectionManager(mIpEndpoint, message, null, null, StrictSSL);
            Hashtable<String, String>  authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
            body = authResponse.get("responseBody");
            statusCode =  authResponse.get("statusCode");
            if (body != null) {
                //Parse response to check soap Faults. Throws an exception
                Utilities.CheckFaultsInResponse(body);

                int startOfAssertion = body.indexOf("<Assertion ");
                int endOfAssertion = body.indexOf("</Assertion>") + "</Assertion>".length();
                body = body.substring(startOfAssertion, endOfAssertion);
                return body;
            }
        }
		catch(StringIndexOutOfBoundsException e) // wrong user, password or scope
		{
			throw new Exception(String.format("Invalid user, password or scope (Http Status Code = %s) . Body : ", statusCode, body));
		}
		catch (Exception e) {
			throw e;
		}
        return null;
	}


}
