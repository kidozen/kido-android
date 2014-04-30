package kidozen.client.authentication;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * The kidozen user identity
 * 
 * @author KidoZen
 * @version 1.00, April 2013
 *
 */
public class KidoZenUser {
    /**
     * Is UserIdentity or ApplicationIdentity
     */
    public KidoZenUserIdentityType IdentityType = KidoZenUserIdentityType.APPLICATION_IDENTITY;
    /**
     * Was taken from Cache?
     */
    public Boolean PulledFromCache = false;
    /**
	 * The Roles of this user
	 */
	public List<String> Roles= new ArrayList<String>();
	/**
	 * The claims of this user
	 */
	public Hashtable<String, String> Claims = new Hashtable<String, String>();

	private Long _expiration;
	public String Token;
    private static final int ONE_SECOND = 1000;


	/**
	 * Checks if the user belongs to the role
	 * @param role
	 * @return True if the user in in the specified role
	 */
	public Boolean IsInRole(String role)
	{
		Boolean returnValue = false;
		for (int i = 0; i < Roles.size() ; i++) {
			if (Roles.get(i).toString().toLowerCase().trim().equalsIgnoreCase(role.trim().toLowerCase())) {
				return true;
			}
		}
		return returnValue;
	}
	
	/**
	 * Returns the expiration time
	 * @return the token expiration time
	 */
	public Long GetExpiration() {
		return _expiration;
	}

	/**
	 * Sets the expiration time
	 * @param expiration
	 */
	public void SetExpiration(Long expiration) {
		_expiration=expiration;
	}

	/**
	 * Get the expiration time in miliseconds
	 * @return the token expiration time in miliseconds
	 */
	public Long GetExpirationInMilliseconds() {
        Date later = new Date(_expiration * ONE_SECOND); // segundos
		Date now = new Date();
		Long diffInMis = later.getTime()  - now.getTime();
		return diffInMis;//TimeUnit.MILLISECONDS.toSeconds(diffInMis);
	}

    public Boolean HasExpired() {
        return GetExpirationInMilliseconds()<=0;
    }
}
