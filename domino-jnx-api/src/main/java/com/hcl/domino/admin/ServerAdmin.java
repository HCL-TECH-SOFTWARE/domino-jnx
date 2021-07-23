/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
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

import java.nio.file.Path;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.stream.Stream;

import com.hcl.domino.DominoClient;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.dbdirectory.DatabaseData;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.Pair;
import com.hcl.domino.security.AclAccess;
import com.hcl.domino.security.AclLevel;
import com.hcl.domino.server.ServerStatusLine;

/**
 * Server administration features and console access
 */
public interface ServerAdmin {

	/**
	 * This function marks a cluster database in service by clearing the database option flag OUT_OF_SERVICE, if set.<br>
	 * <br>
	 * When a call to {@link #markInService(String, String)} is successful, the Cluster Manager enables
	 * users to access the database again by removing the "out of service" access restriction.<br>
	 * <br>
	 * Traditional Domino database access control list (ACL) privileges apply under all circumstances.
	 * In order to use {@link #markInService(String, String)} on a database in a cluster, the remote Notes
	 * user must have at least designer access privileges for the specified database.
	 * If a user does not have the proper privileges, a database access error is returned.<br>
	 * <br>
	 * The {@link #markInService(String, String)} function only affects databases within a Lotus Domino Server cluster.<br>
	 * <br>
	 * For more information, see the Domino Administration Help database.

	 * @param server db server
	 * @param filePath db filepath
	 */
	void markInService(String server, String filePath);
	
	/**
	 * This function marks a cluster database out of service for remote user sessions by modifying
	 * the database option flags to include OUT_OF_SERVICE.<br>
	 * <br>
	 * When this operation is successful, the Cluster Manager denies any new user sessions for this database.<br>
	 * This restriction is in addition to any restrictions set forth in the database access control list (ACL).<br>
	 * The purpose of this function is allow the system administrator to perform maintenance on a database
	 * without requiring a server shutdown or having to use the database ACL to restrict access.<br>
	 * <br>
	 * In order to use {@link #markOutOfService(String, String)} with a database on a clustered server, the remote
	 * Notes user must have at least designer access privileges.<br>
	 * <br>
	 * If a user's privilege level is insufficient, a database access error is returned.<br>
	 * The {@link #markOutOfService(String, String)} function affects only databases that reside on
	 * Domino clusters.<br>
	 * You can mark a database back in service by calling the {@link #markInService(String, String)} function.<br>
	 * <br>
	 * For more information, see the Domino Administration Help database.
	 * 
	 * @param server db server
	 * @param filePath db filepath
	 */
	void markOutOfService(String server, String filePath);
	
	/**
	 * Sends a console command to a Domino server
	 * 
	 * @param server server to contact, use "" for current server
	 * @param command command to execute
	 * @return response
	 */
	String sendConsoleCommand(String server, String command);
	
	/**
	 * Generates a new SSO token on the current server
	 * 
	 * @param orgName organization that the server belongs to, use null if not using Internet Site views for configuration
	 * @param configName name of Web SSO configuration to use
	 * @param userName Notes name to encode in the token, use either canonical or abbreviated format
	 * @param creationDate creation time to set for the token, use null for the current time
	 * @param expirationDate expiration time to set for the token, use null to use expiration from specified Web SSO configuration
	 * @param enableRenewal if true, the generated token contains the {@link DominoDateTime} where the token expires (for Domino only)
	 * @return token
	 */
	DominoSSOToken generateSSOToken(String orgName, String configName,
			String userName, TemporalAccessor creationDate, TemporalAccessor expirationDate, boolean enableRenewal);

	/**
	 * Compute the name variants and groups of this user on the provided server. Does not contain
	 * roles as they are database-specific.
	 * 
	 * @param server the server to query, use "" for current server
	 * @param username username
	 * @return a {@link UserNamesList} of user names that describe the user
	 */
	UserNamesList getUserNamesList(String server, String username);
	
	public enum AclLevelRelation { HasLessThanLevel, HasLevel, HasLevelGreaterThan }
	
	/**
	 * Using this method you can efficiently scan a server directory and compute the {@link AclAccess}
	 * of users on the databases. While the first invocation may take a while to populate
	 * an internal cache of DB ACL copies, following calls should be pretty fast, because
	 * no databases have to be opened anymore. The cache is stored in the scope of this
	 * {@link ServerAdmin} object. So getting a new instance via {@link DominoClient#getServerAdmin()}
	 * requires rebuilding the cache.
	 * 
	 * @param server the server to query, use "" for current server
	 * @param user {@link UserNamesList} of user to compute the effective database access level
	 * @param forceNewScan true to enforce a new directory scan (slow, I/O heavy); if false we return cached results on the second call
	 * @return {@link MultiDatabaseAccessInfo} providing access to database/access pairs
	 */
	MultiDatabaseAccessInfo computeDatabaseAccess(String server,
			UserNamesList user, boolean forceNewScan);
	
	/**
	 * Using this method you can efficiently scan a server directory and compute the {@link AclAccess}
	 * of users on the databases. While the first invocation may take a while to populate
	 * an internal cache of DB ACL copies, following calls should be pretty fast, because
	 * no databases have to be opened anymore. The cache is stored in the scope of this
	 * {@link ServerAdmin} object. So getting a new instance via {@link DominoClient#getServerAdmin()}
	 * requires rebuilding the cache.
	 * 
	 * @param server the server to query, use "" for current server
	 * @param username username to compute the effective database access level
	 * @param forceNewScan true to enforce a new directory scan (slow, I/O heavy); if false we return cached results on the second call
	 * @return {@link MultiDatabaseAccessInfo} providing access to database/access pairs
	 */
	MultiDatabaseAccessInfo computeDatabaseAccess(String server,
			String username, boolean forceNewScan);
	
	public interface MultiDatabaseAccessInfo {
		
		String getServer();
		
		UserNamesList getUser();

		Stream<Pair<DatabaseData, AclAccess>> allEntries();

		default Stream<Pair<DatabaseData, AclAccess>> entriesWithMinLevel(AclLevel level) {
			return allEntries().filter((pair) -> {
				return pair.getValue2().getAclLevel().getValue() >= level.getValue();
			});
		}
		
		default Stream<Pair<DatabaseData, AclAccess>> entriesWithMaxLevel(AclLevel level) {
			return allEntries().filter((pair) -> {
				return pair.getValue2().getAclLevel().getValue() <= level.getValue();
			});
		}

	}
	
	/**
	 * SSO Token for a Domino username and server 
	 */
	public interface DominoSSOToken extends IAdaptable {
		
		/**
		 * Get username element from SSO Token
		 * 
		 * @return name, e.g. John Doe
		 */
		String getName();
		
		/**
		 * Domains the SSO token applies to
		 * 
		 * @return list of domains
		 */
		List<String> getDomains();
		
		/**
		 * Whether the SSO token is for HTTPS only
		 * 
		 * @return true for HTTPS only
		 */
		boolean isSecureOnly();
		
		/**
		 * TODO: Not sure what the Javadoc for this should be
		 * 
		 * @return the storable token data
		 */
		String getData();
		
		/**
		 * Gives a date and time when the SSO token will expire and needs to be renewed
		 * 
		 * @return date/time when the SSO token needs renewing
		 */
		DominoDateTime getRenewalDate();
		
		/**
		 * Produces a string to be used for the "LtpaToken" cookie for
		 * every domain
		 * 
		 * @return cookie strings
		 */
		List<String> toHTTPCookieStrings();
	}
	
	/**
	 * This function retrieves a list of server names that belong to the
	 * same cluster as the specified server.<br>
	 * If the <code>serverName</code> parameter is NULL then the function retrieves the cluster
	 * members of the user's home server.<br>
	 * <br>
	 * The <code>lookupMode</code> parameter controls how the information is retrieved.<br>
	 * If the {@link ClusterLookup#LOOKUP_NOCACHE} flag is specified then the information is
	 * retrieved using a NameLookup on the server only.<br>
	 * <br>
	 * If the {@link ClusterLookup#LOOKUP_CACHEONLY} flag is specified then the information is
	 * retrieved using the client's cluster name cache.<br>
	 * <br>
	 * If no flag (a value of NULL) is specified, then the information is retrieved first
	 * through the client's cluster name cache and if that is not successful, then through
	 * a NameLookup on the server.<br>
	 * Note that the list returned does not include the input server name (or home server
	 * name if NULL was specified).<br>
	 * <br>
	 * {@link #getServerClusterMates(String, ClusterLookup)} uses the Address book specified by the user's location record.<br>
	 * Unless cascading Address books or Directory Assistance is enabled, the Notes mail
	 * domain field in the user's location record must be set to the domain name for the
	 * server(s) in the cluster and the Home/mail server field must be set to a server in this domain.<br>
	 * <br>
	 * If the target server is in a different domain than specified in the user's location record
	 * then in order for {@link #getServerClusterMates(String, ClusterLookup)} to succeed, you must have cascading Address
	 * books or Directory Assistance enabled and the target domain's Address book must be in the
	 * list of Address books to be searched.

	 * @param serverName The name of the Lotus Domino Server where the lookup will be performed (canonical or abbreviated format). Specify a value of NULL if the client's home server is to be used for the lookup.
	 * @param lookupMode lookup mode or null for "first local cache, then remote lookup"
	 * @return server list
	 */
	List<String> getServerClusterMates(String serverName, ClusterLookup lookupMode);
	
	/**
	 * These values are used as input to the {@link ServerAdmin#getServerClusterMates(String, ClusterLookup)}
	 * function.<br>
	 * <br>
	 * When you specify the {@link ClusterLookup#LOOKUP_NOCACHE} value, the call retrieves the input server's
	 * cluster member list through a NameLookup on the input server.<br>
	 * <br>
	 * The client cluster cache is not used for determining this information.<br>
	 * <br>
	 * When you specify the {@link ClusterLookup#LOOKUP_CACHEONLY} value, the call is forced to retrieve
	 * the server's cluster member list from the local client cluster cache.<br>
	 * <br>
	 * There is no NameLookup performed on the server in this case.
	 */
	public enum ClusterLookup implements INumberEnum<Integer> {
		/**
		 * Instructs the NSGetServerClusterMates function to not use the cluster name cache and forces
		 * a lookup on the target server instead
		 * */
		LOOKUP_NOCACHE(0x00000001),
		
		/**
		 * Instructs the NSGetServerClusterMates function to only use the cluster name cache and
		 * restricts lookup to the workstation cache
		 */
		LOOKUP_CACHEONLY(0x00000002);
		
		private Integer m_val;
		
		ClusterLookup(Integer val) {
			m_val = val;
		}
		
		@Override
		public Integer getValue() {
			return m_val;
		}

		@Override
		public long getLongValue() {
			return m_val.longValue();
		}

	}

	public interface RegistrationMessageHandler {
		
		void messageReceived(String msg);
		
	}

	/**
	 * This function will cross-certify an ID with another organization's hierarchy.<br>
	 * <br>
	 * It will open the Domino Directory (Server's Address book) on the specified registration
	 * server, verify write access to the book, and add a new Cross Certificate entry.
	 * 
	 * @param certFilePath Pathname of the certifier id file
	 * @param certPW certifier password
	 * @param certLogPath Pathname of the certifier's log file.  This parameter is required for Domino installations that use the Administration Process so that when using other C API function that require a Certifier Context, the proper entries will be entered in the Certification Log file (CERTLOG.NSF) .  If your Domino installation does not include a Certification Log (does not use the Administration Process), then you should pass in NULL for this parameter.
	 * @param expirationDateTime certificate expiration date for the entity that is being certified
	 * @param regServer name of the Lotus Domino registration server containing the Domino Directory.  The string should be no longer than MAXUSERNAME (256 bytes). If you want to specify "no server" (the local machine), pass "".
	 * @param idFilePath the pathname of the ID file to be cross-certified
	 * @param comment Any additional identifying information to be stored in the Domino Directory
	 * @param msgHandler optional callback to receive status messages or null
	 */
	void registerCrossCertificate(String certFilePath, String certPW, String certLogPath,
			DominoDateTime expirationDateTime,
			String regServer, Path idFilePath, String comment, RegistrationMessageHandler msgHandler);

	
	/**
	 * Creates a new status line for the given name.
	 * 
	 * <p>This method only applies when running on a Domino server locally.</p>
	 * 
	 * @param taskName the name of the task to show in the line
	 * @return a newly-created status line object
	 * @since 1.0.11
	 */
	ServerStatusLine createServerStatusLine(String taskName);
	
	/**
	 * Handler to receive a server console line with text and meta data
	 */
	public interface ConsoleHandler {

		/**
		 * Method is called by {@link ServerAdmin#openServerConsole(String, ConsoleHandler)}
		 * to check if we should stop listening for console messages.
		 * 
		 * @return true to stop
		 */
		public boolean shouldStop();

		/**
		 * Method to receive the console line with text and meta data like the pid/tid/executable
		 * name
		 * 
		 * @param line console line
		 */
		void messageReceived(IConsoleLine line);
		
	}
	
	/**
	 * Opens a remote console for the specified server
	 * 
	 * @param serverName server name (abbreviated or canonical format) or empty string for local server
	 * @param handler handler to receive the console messages
	 */
	void openServerConsole(String serverName, ConsoleHandler handler);
	
	/**
	 * Logs a message to the server using the Addin API.
	 * 
	 * @param messageText the text to log to the server
	 * @since 1.0.20
	 */
	void logMessage(String messageText);
	
}
