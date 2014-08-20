package kidozen.client.authentication;

import java.net.URI;
import java.util.Hashtable;

import kidozen.client.internal.KZAction;
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
public class ADFSWSTrustIdentityProvider implements IIdentityProvider {
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String SOAP_XML = "application/soap+xml;charset=UTF-8";
	private String _message;
	private String _username, _password, _scope, _endpoint;
	public Boolean bypassSSLValidation;
	public ADFSWSTrustIdentityProvider()
	{
		_message = TEMPLATE;
	}
	public void Initialize(String username, String password, String scope) throws Exception
	{
		_message = TEMPLATE;
		_username= username;
		_password = password;
		_scope = scope;
		try {
			_message = _message.replace("[applyTo]", _scope);
			_message = _message.replace("[Username]", _username).toString();
			_message = _message.replace("[Password]", _password);
		} catch (Exception e) {
			throw e;
		}

	}
	public void BeforeRequestToken(Object[] params) {
	}
	
	public void Configure(Object configuration) {
	}

	public void AfterRequestToken(Object[] params) {
	}
	
	public void RequestToken(URI identityProviderUrl, KZAction<String> action) throws Exception {
			_endpoint = identityProviderUrl.toString();
			_message = _message.replace("[To]", _endpoint).toString();
			Hashtable<String, String> requestProperties = new Hashtable<String, String>();
            requestProperties.put(CONTENT_TYPE,SOAP_XML);
			try {
                String url = identityProviderUrl.toString();
                SNIConnectionManager sniManager = new SNIConnectionManager(url, _message, requestProperties, null, bypassSSLValidation);
                Hashtable<String, String>  authResponse = sniManager.ExecuteHttp(KZHttpMethod.POST);
				String body = authResponse.get("responseBody");
				if (body != null) {
                    //Parse response to check soap Faults. Throws an exception
                    Utilities.CheckFaultsInResponse(body);

					int startOfAssertion = body.indexOf("<Assertion ");
					int endOfAssertion = body.indexOf("</Assertion>") + "</Assertion>".length();
					body = body.substring(startOfAssertion, endOfAssertion);
					action.onServiceResponse(body);
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
