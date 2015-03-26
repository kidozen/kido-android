/*
 * (c) 2014 Good Technology Corporation. All rights reserved.
 *
 */

#ifndef GDKERBEROSAUTHHANDLER_H_
#define GDKERBEROSAUTHHANDLER_H_
#include <string>


class GDKerberosAuthHandler {

public:
	GDKerberosAuthHandler();
	virtual ~GDKerberosAuthHandler();

public:


	/*
	 * Call this function for checking whether Kerberos auth delegation is permitted. 
	 *
         * \param allow <TT>bool</TT> indicates whether kerberos auth delegation is allowed.\n
         * 
         */
	static bool getAllowDelegation();

	/*
	 * Call this function to allow/disallow Kerberos auth delegation.
         * Auth delegation is disabled by default. 
	 *
         * \returns <TT>bool</TT> indicating whether auth delegation is permitted.  
	 * 
         */
	static void setAllowDelegation(bool allow);

	/*
	 * Call this function to clear Kerberos authentication ticket cache.
	 *
	 */ 
        static void clearCache();

	/*
	 * Call this function to setup a Kerberos auth ticket
	 *
         * \param username <TT>const char*</TT> user principle name for Kerberos auth in the form username@realm\n
	 * \param password <TT>const char*</TT> the Kerberos authentication password for user principle\n 
	 * 
	 * \returns <TT>int</TT> indicating any Kerberos errors during ticket creation.  0 indicates no error.  
	 *
	 *
	 */ 
	int setUpKerberosTicket(const char* username, const char* password);
        
        /*
	 * Call this function to setup a Kerberos auth ticket when constrained delegation is available.  
	 *
         * \param host <TT>const char*</TT> fully qualified hostname
	 * \param password <TT>const char*</TT> tcp port on which to extablish communications with the given host 
         * 
	 * NOTE: currently the return value is not reliable and should not be relied upon for error detection, future 
         * releases will address this error.  Normall this method will be used in conjuntion with GDNegotiate 
         * to generate a gss token for access to a web resource, as such any errors returned while attempting to 
         * generate a gss token should be relied upon for indication of an error.
         * 
	 * \returns <TT>int</TT> indicating any Kerberos errors during ticket creation.  0 indicates no error.  
	 *
	 *
	 */ 
        int setUpKerberosTicket(const char* host, int port);
       
	/*
	 * This function indicates whether implicit credentials are allowed (as in the case where constrained
         * delegation is enabled).  In the case they are allowed, kerberos ticket setup can be accomplished 
	 * by utilizing host and port rather than explicit userid@realm and password.  
	 *
         * \param host <TT>const char*</TT> fully qualified hostname
	 * \param password <TT>const char*</TT> tcp port on which to extablish communications with the given host  
	 *  
         * \returns <TT>bool</TT> indicating if implicit credentials are allowed.  
	 *
	 *
	 */ 
        bool isImplicitCredentialsAllowed();

        
    private:
	void *authKerberos;
};

#endif /* GDKERBEROSAUTHHANDLER_H_ */
