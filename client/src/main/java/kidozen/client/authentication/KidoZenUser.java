package kidozen.client.authentication;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import kidozen.client.internal.Constants;

/**
 * The kidozen user identity
 * 
 * @author KidoZen
 * @version 1.00, April 2013
 *
 */
public class KidoZenUser {
    /**
     * The Hash key that we can use to find it in the IdentityManager token's cache
     */
    public String HashKey;
    /**
     * The user authentication token
     */
    public String Token;
    /**
     * The user refresh token
     */
    public String RefreshToken;

    /**
     *
     * @return The hash that identifies unambiguously each user
     */
    public String getUserHash() {
        return Claims.get("http://schemas.kidozen.com/userid").toString();
    }
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

    public Boolean HasExpired() {
        return new Date().after(new Date(_expiration * Constants.ONE_THOUSAND));
    }
}
