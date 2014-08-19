package kidozen.client.authentication;

import java.net.URI;

import kidozen.client.internal.KZAction;

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
public interface IIdentityProvider{
	
	/**
	 * Use it to pass some configuration data to use later in your provider
	 * 
	 * @param configuration
	 */
	public void Configure(Object configuration);
	
	/**
	 * Initialization step
	 * 
	 * @param username The user name of the user to be authenticate
	 * @param password The password for the user 
	 * @param scope The identity scope
	 * @throws Exception 
	 */
	public void Initialize(String username, String password, String scope) throws Exception;

	/**
	 * Implement this method to execute actions before the token request to the Identity Provider
	 * @param params
	 */
	public void BeforeRequestToken(Object[] params);
	
	/**
	 * This method executes a request to the Identity Provider
	 * 
	 * @param identityProviderUrl The identity provider endpoint
	 * @param action
	 * @throws Exception
	 */
	public void RequestToken (URI identityProviderUrl, KZAction<String> action) throws Exception;
	
	/**
	 * The following method is invoked after the token request to the Identity Provider
	 * 
	 * @param params
	 */
	public void AfterRequestToken(Object[] params);

}
