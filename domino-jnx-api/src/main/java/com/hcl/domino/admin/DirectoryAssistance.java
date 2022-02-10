/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
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
   * @param searchOrder	Order in which the directory is searched, DEFAULT - 0
   * @param dnSearch Name where the directory naming tree searches should start(server name)
   * @param acceptExpiredCerts Force SSL to accept remote certificates which have expired. DEFAULT FALSE
   * @param verifyRemoteSrvCert DEFAULT : FALSE
   * @param timeout Maximum number of seconds before a search is terminated. DEFAULT : 60
   * @param maxEntriesReturned MAx number of entries a single search can return. DEFAULT : 100
   *
   * @throws DominoException if the DA cannot be updated. The specific exception details
   *         will include the reason for failure
   * @since 1.0.39
   */
  void updateDAConfig( String docUNID, String domainName, String companyName, short searchOrder,
	        String hostName, short ldapVendor, String userName, String password, String dnSearch, boolean useSSL, short port,  boolean acceptExpiredCerts,
	          boolean verifyRemoteSrvCert,  short timeout, short maxEntriesReturned);
}
