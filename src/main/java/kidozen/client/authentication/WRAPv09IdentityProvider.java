package kidozen.client.authentication;

import kidozen.client.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * WRAP V 09 Identity Provider
 * 
 * @author KidoZen
 * @version 1.00, April 2013
 */
public class WRAPv09IdentityProvider implements IIdentityProvider {
	private String _wrapName, _wrapPassword, _wrapScope;
	public Boolean bypassSSLValidation;

	public WRAPv09IdentityProvider()
	{

	}

	public void Configure(Object configuration) {
		// TODO Auto-generated method stub

	}

	public void Initialize(String username, String password, String scope)
			throws Exception {
		this._wrapName=  username;
		this._wrapPassword = password;
		this._wrapScope = scope;
	}

	public void BeforeRequestToken(Object[] params) {
		// TODO Auto-generated method stub

	}

	public void AfterRequestToken(Object[] params) {
		// TODO Auto-generated method stub
	}

	public void RequestToken(URI identityProviderUrl,final KZAction<String> action) throws Exception {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("wrap_name", _wrapName));
		nameValuePairs.add(new BasicNameValuePair("wrap_password", _wrapPassword));
		nameValuePairs.add(new BasicNameValuePair("wrap_scope", _wrapScope));

        try {
            String url = identityProviderUrl.toString();
            String message = Utilities.getQuery(nameValuePairs);

            SNIConnectionManager sniManager = new SNIConnectionManager(url, message, null, null, bypassSSLValidation);
            Hashtable<String, String>  authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);

            String body = authResponse.get("responseBody");

            if (body != null) {
                int startOfAssertion = body.indexOf("<Assertion ");
                int endOfAssertion = body.indexOf("</Assertion>") + "</Assertion>".length();
                body = body.substring(startOfAssertion, endOfAssertion);
                action.onServiceResponse(body);
            }
        }
		catch(StringIndexOutOfBoundsException e) // wrong user, password or scope
		{
			//client.getConnectionManager().shutdown();
			throw e;
		}
		catch (Exception e) {
			//client.getConnectionManager().shutdown();
			throw e;
		}
	}


}
