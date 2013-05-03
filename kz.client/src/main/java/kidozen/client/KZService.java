package kidozen.client;

import kidozen.client.authentication.AuthenticationManager;
import kidozen.client.authentication.KidoZenUser;

public class KZService 
{
	protected static final String ACCEPT = "Accept";
	protected static final String APPLICATION_JSON = "application/json";
	protected static final String CONTENT_TYPE = "content-type";
	protected static final String AUTHORIZATION_HEADER = "Authorization";
	public Boolean BypassSSLVerification = false;
	public KidoZenUser KidozenUser = new KidoZenUser();
	protected String _application="";
	protected String _tennantMarketPlace;
	String ipEndpoint;
	String applicationScope ;
	String authServiceScope ;
	String authServiceEndpoint ;

	public void AddSecurityToken(String key, KidoZenUser tokens)
	{
		AuthenticationManager._securityTokens.put(key, tokens);
	}
	
	public KidoZenUser GetSecurityToken(String key)
	{
		KidoZenUser user = AuthenticationManager._securityTokens.get(key);
		return user;
	}

	public String GetKidoZenToken() {
		return KidozenUser.Token;
	}

	public String CreateAuthHeaderValue()
	{
		return "WRAP access_token=\"" + KidozenUser.Token +"\"";
	}

}

