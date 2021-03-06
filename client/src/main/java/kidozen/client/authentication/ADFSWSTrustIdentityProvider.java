package kidozen.client.authentication;

import java.net.URI;
import java.util.Hashtable;

import kidozen.client.KZHttpMethod;
import kidozen.client.internal.SNIConnectionManager;
import kidozen.client.internal.Utilities;

/**
 * Active Directory Federation Services Identity Provider
 * 
 * @author kidozen
 * @version 1.00, April 2013
 *
 */
public class ADFSWSTrustIdentityProvider extends BaseIdentityProvider {
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String SOAP_XML = "application/soap+xml;charset=UTF-8";
	private String mMessage,
            mUsername,
            mPassword,
            mEndpoint,
            mScope;
	public Boolean bypassSSLValidation;

    public ADFSWSTrustIdentityProvider()
	{
		mMessage = TEMPLATE;
	}

    public ADFSWSTrustIdentityProvider(String username, String password, String endpoint, String scope) {
        this();
        mUsername = username;
        mPassword = password;
        mEndpoint = endpoint;
        mScope = scope;
    }

	private void createTemplate() throws Exception
	{
		mMessage = TEMPLATE;
		try {
            mMessage = mMessage.replace("[applyTo]", mScope);
			mMessage = mMessage.replace("[Username]", mUsername).toString();
			mMessage = mMessage.replace("[Password]", mPassword);
            mMessage = mMessage.replace("[To]", mEndpoint).toString();
		} catch (Exception e) {
			throw e;
		}
	}

    public String RequestToken() throws Exception {
        this.createTemplate();
        Hashtable<String, String> requestProperties = new Hashtable<String, String>();
        requestProperties.put(CONTENT_TYPE,SOAP_XML);
        try {
            SNIConnectionManager sniManager = new SNIConnectionManager(mEndpoint, mMessage, requestProperties, null, bypassSSLValidation);
            Hashtable<String, String>  authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
            String body = authResponse.get("responseBody");
            if (body != null) {
                //Parse response to check soap Faults. Throws an exception
                Utilities.CheckFaultsInResponse(body);

                int startOfAssertion = body.indexOf("<Assertion ");
                int endOfAssertion = body.indexOf("</Assertion>") + "</Assertion>".length();
                body = body.substring(startOfAssertion, endOfAssertion);
                return body;
                //action.onServiceResponse(body);
            }
        }
        catch (IllegalArgumentException e) // wrong user, password or scope
        {
            throw e;
        }
        catch(StringIndexOutOfBoundsException e)
        {
            throw e;
        }
        catch (Exception e) {
            throw e;
        }
        return null;
    }




    private final String TEMPLATE="<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" "
        + "xmlns:a=\"http://www.w3.org/2005/08/addressing\" xmlns:u=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">"
        + "<s:Header>"
        + "<a:Action s:mustUnderstand=\"1\">http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Issue</a:Action>"
        + "<a:To s:mustUnderstand=\"1\">[To]</a:To>"
        + "<o:Security s:mustUnderstand=\"1\" xmlns:o=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"
        + "  <o:UsernameToken u:Id=\"uuid-6a13a244-dac6-42c1-84c5-cbb345b0c4c4-1\">"
        + "    <o:Username>[Username]</o:Username>"
        + "       <o:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">[Password]</o:Password>"
        + "      </o:UsernameToken>"
        + "    </o:Security>"
        + "    </s:Header>"
        + "    <s:Body>"
        + "    <trust:RequestSecurityToken xmlns:trust=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\">"
        + "     <wsp:AppliesTo xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">"
        + "       <a:EndpointReference>"
        + "         <a:Address>[applyTo]</a:Address>"
        + "       </a:EndpointReference>"
        + "       </wsp:AppliesTo>"
        + "       <trust:KeyType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer</trust:KeyType>"
        + "       <trust:RequestType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue</trust:RequestType>"
        + "        <trust:TokenType>urn:oasis:names:tc:SAML:2.0:assertion</trust:TokenType>"
        + "     </trust:RequestSecurityToken>"
        + "     </s:Body>"
        + "    </s:Envelope>";
}
