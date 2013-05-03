package kidozen.client;

import java.util.concurrent.atomic.AtomicReference;

import kidozen.client.authentication.KidoZenUser;

public class AuthenticationHandler implements KZAction<KidoZenUser>{
	private AtomicReference<KidoZenUser> KidoZenUser;
	
	public KidoZenUser getKidoZenUser()
	{
		return KidoZenUser.get();
	}
	public AuthenticationHandler(AtomicReference<KidoZenUser> _kidozenUser ) {
		KidoZenUser = _kidozenUser;
	}
	
	public void onServiceResponse(KidoZenUser response) throws Exception {
		this.KidoZenUser.set(response);
	}
}
