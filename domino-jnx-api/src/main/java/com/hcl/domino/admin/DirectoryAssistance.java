package com.hcl.domino.admin;

import com.hcl.domino.DominoException;

/**
 * Represents the Directory Assistance configuration for a given server and provides
 * methods for querying and manipulating it.
 * 
 * @since 1.0.41
 */
public interface DirectoryAssistance {

  /**
   * Create the directory assistance for the provided LDAP connection information.
   *
   * @param updateServerDoc Set to TRUE if the server doc needs to be updated with given DA db name
   * @param domainName      Should be unique and not match the primary Domino domain
   * @param companyName     Name of the company associated with the directory
   * @param searchOrder     order in which the directory is searched
   * @param hostName        DNS hostname or IP address of LDAP server
   * @param ldapVendor      Specify the LDAP directory service provider
   * @param userName        the user name to use to connect for simple bindings
   * @param password        the password to use to connect
   * @param dnSearch        a DN search base
   * @param useSSL          whether the connection should use TLS/SSL
   * @param port            the port to use to connect to the server
   * @param acceptExpiredCerts whether to allow expired TLS certificates
   * @param verifyRemoteSrvCert whether to verify the validity of the remote TLS certificate
   * @param timeout         Maximum number of seconds before a search is terminated
   * @param maxEntriesReturned Max number of entries a single search can return
   *
   * @throws DominoException if the DA cannot be created. The specific exception details
   *         will include the reason for failure
   * @since 1.0.39
   */
  void createDAConfig(boolean updateServerDoc, String domainName, String companyName, short searchOrder,
        String hostName, short ldapVendor, String userName, String password, String dnSearch, boolean useSSL, short port,  boolean acceptExpiredCerts,
          boolean verifyRemoteSrvCert,  short timeout, short maxEntriesReturned);


  /**
   * Enable or disable the directory assistance.
   *
   * @param domainName      Should be unique and not match the primary Domino domain
   * @param enableDomain    whether the DA should be enabled or disabled
   *
   * @throws DominoException if the DA cannot be enabled/disabled. The specific exception details
   *         will include the reason for failure
   * @since 1.0.39
   */
  void enableDisableDA(String domainName, boolean enableDomain);
  
  /**
   * Update the directory assistance for the provided LDAP connection information.
   *
   * @param docUNID         UNID of the Document to be updated
   * @param domainName      Should be unique and not match the primary Domino domain
   * @param companyName     Name of the company associated with the directory
   * @param hostName        DNS hostname or IP address of LDAP server
   * @param ldapVendor      Specify the LDAP directory service provider
   * @param userName        the user name to use to connect for simple bindings
   * @param password        the password to use to connect
   * @param useSSL          whether the connection should use TLS/SSL
   * @param port            the port to use to connect to the server
   *
   * @throws DominoException if the DA cannot be updated. The specific exception details
   *         will include the reason for failure
   * @since 1.0.39
   */
  void updateDAConfig( String docUNID, String domainName, String companyName,
      String hostName, short ldapVendor, String userName, String password, boolean useSSL, short port);
}
