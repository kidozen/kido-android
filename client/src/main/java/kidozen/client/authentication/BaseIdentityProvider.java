package kidozen.client.authentication;

import java.net.URI;

/**
 * Identity Provider interface
 * 
 * Implement this interface in order to create customs identity providers
 * Kidozen implements a template method pattern to call the methods in the following order:
 * 1-Initialize
 * 2-BeforeRequestToken
 * 3-RequestToken
 * 4-AfterRequestToken
 * 
 * @author kidozen
 * @version 1.00, April 2013
 *
 */
public abstract class BaseIdentityProvider {

    /**
	 * Initialization step
	 *
	 * @param scope The identity scope
	 * @throws Exception
	 */
	public abstract void Initialize(String scope) throws Exception;

    /**
	 * This method executes a request to the Identity Provider
	 * 
	 * @param identityProviderUrl The identity provider endpoint
	 * @throws Exception
	 */
	public abstract String RequestToken(URI identityProviderUrl) throws Exception;

}
