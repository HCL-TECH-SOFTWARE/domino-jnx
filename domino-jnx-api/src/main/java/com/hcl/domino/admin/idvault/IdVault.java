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
package com.hcl.domino.admin.idvault;

import java.nio.file.Path;
import java.util.Set;

import com.hcl.domino.DominoException;
import com.hcl.domino.data.Document;

/**
 * Access to all id and cert operations
 */
public interface IdVault {

	/**
	 * Will contact the server and locate a vault for <code>userName</code>.<br>
	 * Then extract the ID file from the vault and write it to <code>idPath</code>.<br>
	 * <br>
	 * If successful returns with the vault server name.
	 * 
	 * @param userName Name of user whose ID is being put into vault - either abbreviated or canonical format
	 * @param password Password to id file being uploaded to the vault
	 * @param idPath Path to where the download ID file should be created or overwritten
	 * @param serverName Name of server to contact
	 * @return the vault server name
	 * @throws DominoException in case of problems, e.g. ERR 22792 Wrong Password
	 */
	String extractUserIdFromVault(String userName, String password, Path idPath, String serverName);

	/**
	 * Will contact the server and locate a vault for <code>userName</code>.<br>
	 * Then downloads the ID file from the vault and store it in memory.<br>
	 * 
	 * @param userName Name of user whose ID is being put into vault - either abbreviated or canonical format
	 * @param password Password to id file being uploaded to the vault
	 * @param serverName Name of server to contact
	 * @return the in-memory user id
	 * @throws DominoException in case of problems, e.g. ERR 22792 Wrong Password
	 */
	UserId getUserIdFromVault(String userName, String password, String serverName);
	
	/**
	 * Will contact the server and retrieves the {@link UserId} associated with the provided
	 * implementation-specific token.
	 * 
	 * @param token the token to use to retrieve the ID. The class of the token depends on the available
	 *       vault provider implementations
	 * @param serverName the name of the server to contact
	 * @return the in-memory user ID
	 * @throws UnsupportedOperationException when no provider can be found to handle {@code token}
	 * @since 1.0.19
	 */
	UserId getUserIdWithToken(Object token, String serverName);
	
	/**
	 * Will open the ID file name provided, locate a vault server for user <code>userName</code>,
	 * upload the ID file contents to the vault, then return with the vault server name.<br>
	 * 
	 * @param userName Name of user whose ID is being put into vault - either abbreviated or canonical format
	 * @param password Password to id file being uploaded to the vault
	 * @param idPath Path to where the download ID file should be created or overwritten
	 * @param serverName Name of server to contact
	 * @return the vault server name
	 * @throws DominoException in case of problems, e.g. ERR 22792 Wrong Password
	 */
	String putUserIdIntoVault(String userName, String password, Path idPath, String serverName);
	
	/**
	 * Will locate a vault server for user <code>userName</code> and
	 * upload the specified ID contents to the vault, then return with the vault server name.<br>
	 * 
	 * @param userName Name of user whose ID is being put into vault - either abbreviated or canonical format
	 * @param password Password to id file being uploaded to the vault
	 * @param userId user id
	 * @param serverName Name of server to contact
	 * @return the vault server name
	 * @throws DominoException in case of problems, e.g. ERR 22792 Wrong Password
	 */
	String putUserIdIntoVault(String userName, String password, UserId userId, String serverName);
	
	/**
	 * Will open the ID file name provided, locate a vault server, synch the ID file contents to the vault,
	 * then return the synched content. If successful the vault server name is returned.

	 * @param userName Name of user whose ID is being put into vault - either abbreviated or canonical format
	 * @param password Password to id file being uploaded to the vault
	 * @param idPath Path to where the download ID file should be created or overwritten
	 * @param serverName Name of server to contact
	 * @return sync result
	 */
	SyncResult syncUserIdWithVault(String userName, String password, Path idPath, String serverName);
	
	/**
	 * Resets an ID password. This password is required by the user when they recover their ID file from the ID vault.
	 * 
	 * @param server Name of server to contact to request the password reset. Can be NULL if executed from a program or agent on a server. Does NOT have to be a vault server. But must be running Domino 8.5 or later. 
	 * @param userName Name of user to reset their vault id file password.
	 * @param password New password to set in the vault record for pUserName.
	 * @param downloadCount (max. 65535) If this user's effective policy setting document has "allow automatic ID downloads" set to no, then this parameter specifies how many downloads the user can now perform. If downloads are automatic this setting should be zero.
	 */
	void resetUserPasswordInVault(String server, String userName, String password, int downloadCount);
	
	/**
	 * This function changes the password in the specified ID file.<br>
	 * You can use this function to change the password in a user's id, a server's id, or a certifier's id.<br>
	 * <br>
	 * Multiple passwords are not supported.
	 * 
	 * @param idPath path to the ID file whose password should be changed
	 * @param oldPassword old password in the ID file.  This parameter can only be NULL if there is no old password.  If this parameter is set to "", then ERR_BSAFE_NULLPARAM is returned
	 * @param newPassword new password on the ID file. If this parameter is NULL, the password is cleared.  If the specified ID file requires a password and this parameter is NULL, then ERR_BSAFE_PASSWORD_REQUIRED is returned.  If this parameter is set to "", then ERR_BSAFE_NULLPARAM is returned.  If the specified ID file is set for a minimum password length and this string contains less than that minimum, then ERR_REG_MINPSWDCHARS is returned.
	 */
	void changeIdPassword(Path idPath, String oldPassword, String newPassword);
	
	/**
	 * The method tries to open the ID with the specified password. If the password is
	 * not correct, the method tries a {@link DominoException}
	 * 
	 * @param idPath id path
	 * @param password password
	 * @throws DominoException e.g. ERR 6408 if password is incorrect
	 */
	void checkIdPassword(Path idPath, String password);
	
	/**
	 * This function will extract the username from an ID file.
	 *
	 * @param idPath id path
	 * @return canonical username
	 */
	String getUsernameFromId(Path idPath);
	
	/**
	 * Opens an ID file and returns an in-memory handle for signing ({@link Document#sign(UserId, boolean)})
	 * and using note encrypting ({@link Document#copyAndEncrypt(UserId, java.util.Collection)} /
	 * {@link Document#decrypt(UserId)}).
	 * 
	 * @param <T> optional result type
	 * 
	 * @param idPath id path on disk
	 * @param password id password
	 * @param callback callback code to access the opened ID; we automatically close the ID file when the callback invocation is done
	 * @return optional computation result
	 */
	<T> T openUserIdFile(Path idPath, String password, IDAccessCallback<T> callback);
	
	/**
	 * Checks if the ID vault on the specified server contains an ID for a user
	 * 
	 * @param userName user to check
	 * @param server server
	 * @return true if ID is in vault
	 */
	boolean isIdInVault(String userName, String server);
	
	public interface SyncResult {
		
		String getVaultServer();
		
		boolean isIdSyncDone();
		
		boolean isIdFoundInVault();
		
	}
	
	/**
	 * Callback interface to work with an opened ID
	 * 
	 * @param <T> computation result type
	 */
	@FunctionalInterface
	public interface IDAccessCallback<T> {
		
		/**
		 * Implement this method to work with the passed user id. <b>Do not store it anywhere, since it is disposed right after the method call!</b>.
		 * 
		 * @param id id
		 * @return optional computation result
		 */
		T accessId(UserId id);
		
	}

	/**
	 * Returns flags for the ID that is active for the current process
	 * 
	 * @return flags
	 */
	Set<IdFlag> getIdFlags();
	
	/**
	 * Returns flags for the specified ID file
	 * 
	 * @param userId user id, use null for the ID that is active for the current process
	 * @return flags
	 */
	Set<IdFlag> getIDFlags(UserId userId);
	
	/**
	 * Flags of a User ID
	 */
	public enum IdFlag {
		
		/** File is password protected */
		PASSWORD(0x0001),
		
		/** File password is required. */
		PASSWORD_REQUIRED (0x0002),
		
		/** Password may be shared by all processes */
		PASSWORD_SHAREABLE(0x0008),
		
		/** ID file has an extra that descibes special password features (eg, 128 bit key) */
		PASSWORD_EXTRA(0x0200),
		
		/** Must prompt user before automatically accepting a name change */ 
		CHANGE_NAME_PROMPT(0x0400),
		
		/**
		 * For mailed in requests to certifier usually using a "safe-copy".<br>
		 * This flags says that the requestor does not need a response via Mail -- usually because the response
		 * will be detected during authentication with a server whose Address Book has been
		 * updated with a new certificate for the requestor.
		 */ 
		DONT_REPLY_VIA_MAIL(0x80000000 | 0x0001),
		
		/** Admin has locked down the value of this field. See fIDFH_PWShareable */ 
		PASSWORD_SHAREABLE_LOCKDOWN(0x8000000 | 0x0004);

		private int m_val;
		
		IdFlag(int val) {
			m_val = val;
		}
		
		public int getValue() {
			return m_val;
		}
	}

}
