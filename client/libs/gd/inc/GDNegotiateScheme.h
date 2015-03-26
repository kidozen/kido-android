/*
 * (c) 2014 Good Technology Corporation. All rights reserved.
 *
 */

#ifndef GDNEGOTIATESCHEME_H_
#define GDNEGOTIATESCHEME_H_
extern "C"{
#include <gssapi.h>
}

/** Generate SPNEGO header (C++).
 * This C++ class contains a \ss_function for generating a Generic Security
 * Services Application Program Interface (GSS-API) header for the Simple and
 * Protected GSS-API Negotiation Mechanism (SPNEGO).
 *
 * Use of SPNEGO in Good Dynamics secure communication is dependant on use of
 * Kerberos authentication. See the \link GDKerberosHandlerWrapper
 * GDKerberosHandlerWrapper class reference\endlink for the C++ API for Kerberos
 * authentication. See the \ss_communication_link for an introduction to the
 * Good Dynamics secure communication feature.
 *
 * \see <A HREF="https://tools.ietf.org/html/rfc4178" target="_blank"
 * >RFC 4178 - The Simple and Protected Generic Security Service Application
 * Program Interface (GSS-API) Negotiation Mechanism</A
 * > on the ietf.org website.
 * \see <A HREF="https://tools.ietf.org/html/rfc4559" target="_blank"
 * >RFC 4559 - SPNEGO-based Kerberos and NTLM HTTP Authentication in Microsoft
 * Windows</A
 * > on the ietf.org website.
 */
class NegotiateData {

public:
        /** Constructor.
         * Constructor.
         */
	NegotiateData();
	virtual ~NegotiateData();

public:
 
	/** Generate a GSS-API header for SPNEGO.
         * Call this \ss_function to generate a GSS-API header for SPNEGO. This
         * \ss_function can be called after a Kerberos ticket has been
         * requested, see \link
         * GDKerberosHandlerWrapper::setUpKerberosTicket\endlink.
         *
         * Check the GSS-API status code to determine the success or failure of
         * the operation.
         * 
	 * \param token <TT>char*</TT> containing the Base64-decoded initial 
         *              token from the server challenge, and a null terminator.
         *              
	 * \param hostname <TT>char*</TT> containing the server address and port
         *                 number of the authentication host, and a null
         *                 terminator. Format the value as:\n
         *                 <EM>server address</EM>:<EM>port number</EM>\n
         *                 The server address part is the fully qualified domain
         *                 name (FQDN) of the host.\n
         *                 The port specifier can be omitted if the port number
         *                 is 80 for an HTTP service, or 443 for an HTTPS
         *                 service.\n
         *                 The value would be the host and port portion of a
         *                 service principal name (SPN) formatted as:
         *                 (HTTP\@&lt;host&gt;:&lt;port&gt;)
         *
	 * \param allow_delegation <TT>bool</TT> flag for whether Kerberos
	 *                         delegation is allowed.
	 *
         * \return <TT>char*</TT> containing a Base64-encoded token that can be
         *         used to finalize SPNEGO authentication, and a null
         *         terminator.\n
         *         The returned value is a pointer to memory that has been
         *         allocated from the heap. The caller of this \ss_function is
         *         responsible for releasing the memory.
	 */ 
         char* generateGssApiData(const char* token, const char* hostname, const bool allow_delegation);
         
         /** Get the GSS-API major status code.
          * \return Numeric GSS-API major status code. Zero (0) indicates no
          *         error.
          */
         OM_uint32 GetStatus();
         
         /** Get the GSS-API context.
          * \return GSS-API context handle for this instance.
          */
         gss_ctx_id_t GetContext();

private:
    void process_GSS_data(gss_buffer_desc *input_token_ptr,
    		gss_buffer_desc *output_token_ptr,
    		const char* hostname,
    		const bool allow_delegation);
    int  get_gss_name(const char* hostname);
	void cleanup_negotiate_data();

     bool            gss;
     OM_uint32       status;
	 OM_uint32 	     minor_status;
	 gss_ctx_id_t    context;
	 gss_name_t      server_gss_name;
};

//convenience method, allows for atomic deletion and creation of Negotiate data without the need for the
//caller to utilize the HeimdalGlobalLock, exported for use in JNI
#ifdef __cplusplus
extern "C" {
#endif
void  clearNegotiateData(NegotiateData **negData, bool resetOnly);
#ifdef __cplusplus
}
#endif

#endif /* GDNEGOTIATESCHEME_H_ */
