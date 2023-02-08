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
package com.hcl.domino.jna.admin;

import java.lang.ref.ReferenceQueue;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.hcl.domino.DominoClient.OpenDatabase;
import com.hcl.domino.DominoException;
import com.hcl.domino.admin.idvault.UserId;
import com.hcl.domino.commons.admin.IDefaultIdVault;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.exception.IncompatibleImplementationException;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNAIdVaultAllocations;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

public class JNAIdVault extends BaseJNAAPIObject<JNAIdVaultAllocations> implements IDefaultIdVault {

	public JNAIdVault(IAPIObject<?> parent) {
		super(parent);
		
		setInitialized();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNAIdVaultAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		return new JNAIdVaultAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	@Override
	public String extractUserIdFromVault(String userName, String password, Path idPath, String serverName) {
		checkDisposed();
		
		return _getUserIdFromVault(userName, password, idPath, null, serverName);
	}

	@Override
	@SuppressWarnings("unchecked")
	public UserId getUserIdFromVault(String userName, String password, String serverName) {
		checkDisposed();
		
		PointerByReference rethKFC = new PointerByReference();
		_getUserIdFromVault(userName, password, null, rethKFC, serverName);
		
		//according to core dev, calling SECKFMClose is not required / not used in core platform code.
		boolean noDispose = true;
		return new JNAUserId(this, new IAdaptable() {
			
			@Override
			public <T> T getAdapter(Class<T> clazz) {
				if (PointerByReference.class == clazz) {
					return (T) rethKFC;
				} else if(Long.class.equals(clazz)) {
					return (T)Long.valueOf(Pointer.nativeValue(rethKFC.getPointer()));
				}

				return null;
			}
		}, noDispose);
	}

	@Override
	public String putUserIdIntoVault(String userName, String password, Path idPath, String serverName) {
		checkDisposed();
		
		return _putUserIdIntoVault(userName, password, idPath, null, serverName);
	}

	@Override
	public String putUserIdIntoVault(String userName, String password, UserId userId, String serverName) {
		checkDisposed();
		
		if (!(userId instanceof JNAUserId)) {
			throw new IncompatibleImplementationException(userId, JNAUserId.class);
		}
		PointerByReference phKFC = null;
		
		if (userId!=null) {
			phKFC = userId.getAdapter(PointerByReference.class);
		}

		if (phKFC==null) {
			throw new DominoException("Could not retrieve required user id handle");
		}

		return _putUserIdIntoVault(userName, password, null, phKFC, serverName);
	}

	@Override
	public SyncResult syncUserIdWithVault(String userName, String password, Path idPath, String serverName) {
		checkDisposed();
		
		String userNameCanonical = NotesNamingUtils.toCanonicalName(userName);
		Memory userNameCanonicalMem = NotesStringUtils.toLMBCS(userNameCanonical, true);
		Memory passwordMem = NotesStringUtils.toLMBCS(password, true);
		Memory idPathMem = idPath==null ? null : NotesStringUtils.toLMBCS(idPath.toString(), true);
		Memory serverNameMem = new Memory(NotesConstants.MAXPATH);
		{
			Memory serverNameParamMem = NotesStringUtils.toLMBCS(serverName, true);
			if (serverNameParamMem!=null && (serverNameParamMem.size() > NotesConstants.MAXPATH)) {
				throw new IllegalArgumentException(MessageFormat.format("Servername length cannot exceed MAXPATH ({0} characters)", NotesConstants.MAXPATH));
			}
			if (serverNameParamMem!=null) {
				byte[] serverNameParamArr = serverNameParamMem.getByteArray(0, (int) serverNameParamMem.size());
				serverNameMem.write(0, serverNameParamArr, 0, serverNameParamArr.length);
			}
			else {
				serverNameMem.setByte(0, (byte) 0);
			}
		}
		
		PointerByReference phKFC = new PointerByReference();
		IntByReference retdwFlags = new IntByReference();
		
		short result = NotesCAPI.get().SECKFMOpen (phKFC, idPathMem, passwordMem, NotesConstants.SECKFM_open_All, 0, null);
		NotesErrorUtils.checkResult(result);
		
		try {
			result = NotesCAPI.get().SECidfSync(userNameCanonicalMem, passwordMem, idPathMem, phKFC, serverNameMem, 0, (short) 0, null, retdwFlags);
			NotesErrorUtils.checkResult(result);
		}
		finally {
			result = NotesCAPI.get().SECKFMClose(phKFC, NotesConstants.SECKFM_close_WriteIdFile, 0, null);
			NotesErrorUtils.checkResult(result);
		}
		
		int vaultServerNameLength = 0;
		for (int i=0; i<serverNameMem.size(); i++) {
			vaultServerNameLength = i;
			if (serverNameMem.getByte(i) == 0) {
				break;
			}
		}
		
		String vaultServerName = NotesStringUtils.fromLMBCS(serverNameMem, vaultServerNameLength);
		
		SyncResult syncResult = new JNASyncResult(vaultServerName, retdwFlags.getValue());
		return syncResult;
	}

	@Override
	public void resetUserPasswordInVault(String server, String userName, String password, int downloadCount) {
		checkDisposed();
		
		String userNameCanonical = NotesNamingUtils.toCanonicalName(userName);
		Memory userNameCanonicalMem = NotesStringUtils.toLMBCS(userNameCanonical, true);
		Memory passwordMem = NotesStringUtils.toLMBCS(password, true);
		Memory serverNameMem = NotesStringUtils.toLMBCS(server, true);

		short result = NotesCAPI.get().SECidvResetUserPassword(serverNameMem, userNameCanonicalMem, passwordMem, (short) (downloadCount & 0xffff), 0, null); 
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public void changeIdPassword(Path idPath, String oldPassword, String newPassword) {
		checkDisposed();
		
		Memory idPathMem = idPath==null ? null : NotesStringUtils.toLMBCS(idPath.toString(), true);
		Memory oldPasswordMem = NotesStringUtils.toLMBCS(oldPassword, true);
		Memory newPasswordMem = NotesStringUtils.toLMBCS(newPassword, true);

		short result = NotesCAPI.get().SECKFMChangePassword(idPathMem, oldPasswordMem, newPasswordMem);
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public void checkIdPassword(Path idPath, String password) {
		checkDisposed();
		
		Memory idPathMem = idPath==null ? null : NotesStringUtils.toLMBCS(idPath.toString(), true);
		Memory passwordMem = NotesStringUtils.toLMBCS(password, true);
		
		short result;

		PointerByReference phKFC = new PointerByReference();
		result = NotesCAPI.get().SECKFMOpen(phKFC, idPathMem, passwordMem, 0, 0, null);
		NotesErrorUtils.checkResult(result);

		result = NotesCAPI.get().SECKFMClose(phKFC, 0, 0, null);
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public String getUsernameFromId(Path idPath) {
		checkDisposed();
		
		Memory idPathMem = idPath==null ? null : NotesStringUtils.toLMBCS(idPath.toString(), true);
		
		String name = getIDInfoAsString(idPathMem, NotesConstants.REGIDGetName, NotesConstants.MAXUSERNAME+1);
		return name;
	}

	@Override
	public <T> T openUserIdFile(Path idPath, String password, IDAccessCallback<T> callback) {
		checkDisposed();
		
		Memory idPathMem = idPath==null ? null : NotesStringUtils.toLMBCS(idPath.toString(), true);
		Memory passwordMem = NotesStringUtils.toLMBCS(password, true);
		
		//open the id file
		PointerByReference phKFC = new PointerByReference();
		
		short result = NotesCAPI.get().SECKFMOpen (phKFC, idPathMem, passwordMem, NotesConstants.SECKFM_open_All, 0, null);
		NotesErrorUtils.checkResult(result);
		
		JNAUserId id = null;
		try {
			id = new JNAUserId(this, new IAdaptable() {
				
				@SuppressWarnings("unchecked")
				@Override
				public <U> U getAdapter(Class<U> clazz) {
					if (PointerByReference.class == clazz) {
						return (U) phKFC;
					}

					return null;
				}
			}, true);
			
			//invoke callback code
			return callback.accessId(id);
		}
		finally {
			//and close the ID file afterwards
			if (id!=null) {
				id.dispose();
			}
		}
	}

	@Override
	public boolean isIdInVault(String userName, String server) {
		checkDisposed();
		
		String serverCanonical = NotesNamingUtils.toCanonicalName(server);
		String usernameCanonical = NotesNamingUtils.toCanonicalName(userName);
		
		Memory serverCanonicalMem = NotesStringUtils.toLMBCS(serverCanonical, true);
		Memory usernameCanonicalMem = NotesStringUtils.toLMBCS(usernameCanonical, true);
		
		short result = NotesCAPI.get().SECidvIsIDInVault(serverCanonicalMem, usernameCanonicalMem);
		if ((result & NotesConstants.ERR_MASK) == 16372) {
			return false;
		}
		NotesErrorUtils.checkResult(result);

		return result == 0;
	}

	/**
	 * Internal helper method to fetch the ID from the ID vault.
	 * 
	 * @param userName Name of user whose ID is being put into vault - either abbreviated or canonical format
	 * @param password Password to id file being uploaded to the vault
	 * @param idPath if not null, path to where the download ID file should be created or overwritten
	 * @param rethKFC if not null, returns the hKFC handle to the in-memory id
	 * @param serverName Name of server to contact
	 * @return the vault server name
	 * @throws NotesError in case of problems, e.g. ERR 22792 Wrong Password
	 */
	private String _getUserIdFromVault(String userName, String password, Path idPath,
			PointerByReference rethKFC, String serverName) {
		
		String userNameCanonical = NotesNamingUtils.toCanonicalName(userName);
		Memory userNameCanonicalMem = NotesStringUtils.toLMBCS(userNameCanonical, true);
		Memory passwordMem = NotesStringUtils.toLMBCS(password, true);
		Memory idPathMem = idPath==null ? null : NotesStringUtils.toLMBCS(idPath.toString(), true);
		Memory serverNameMem = new Memory(NotesConstants.MAXPATH);
		{
			Memory serverNameParamMem = NotesStringUtils.toLMBCS(serverName, true);
			if (serverNameParamMem!=null && (serverNameParamMem.size() > NotesConstants.MAXPATH)) {
				throw new IllegalArgumentException(MessageFormat.format("Servername length cannot exceed MAXPATH ({0} characters)", NotesConstants.MAXPATH));
			}
			if (serverNameParamMem!=null) {
				byte[] serverNameParamArr = serverNameParamMem.getByteArray(0, (int) serverNameParamMem.size());
				serverNameMem.write(0, serverNameParamArr, 0, serverNameParamArr.length);
			}
			else {
				serverNameMem.setByte(0, (byte) 0);
			}
		}
		
		short result = NotesCAPI.get().SECidfGet(userNameCanonicalMem, passwordMem, idPathMem, rethKFC,
				serverNameMem, 0, (short) 0, null);
		NotesErrorUtils.checkResult(result);
		
		int vaultServerNameLength = 0;
		for (int i=0; i<serverNameMem.size(); i++) {
			vaultServerNameLength = i;
			if (serverNameMem.getByte(i) == 0) {
				break;
			}
		}
		
		String vaultServerName = NotesStringUtils.fromLMBCS(serverNameMem, vaultServerNameLength);
		return vaultServerName;
	}
	
	/**
	 * Will open the ID file name provided, locate a vault server for user <code>userName</code>,
	 * upload the ID file contents to the vault, then return with the vault server name.<br>
	 * 
	 * @param userName Name of user whose ID is being put into vault - either abbreviated or canonical format
	 * @param password Password to id file being uploaded to the vault
	 * @param idPath Path to where the download ID file should be created or overwritten or null to use the in-memory id
	 * @param phKFC handle to the in-memory id or null to use an id file on disk for 64 bit
	 * @param serverName Name of server to contact
	 * @return the vault server name
	 * @throws NotesError in case of problems, e.g. ERR 22792 Wrong Password
	 */
	private String _putUserIdIntoVault(String userName, String password, Path idPath,
			PointerByReference phKFC, String serverName) {
		//opening any database on the server is required before putting the id fault, according to the
		//C API documentation and sample "idvault.c"
		
	  String primaryDirectory = getParentDominoClient().openUserDirectory(serverName).getPrimaryDirectoryPath()
	      .orElseThrow(() -> new IllegalStateException("Unable to identify primary directory path"));
		try (JNADatabase anyServerDb = new JNADatabase(getParentDominoClient(), serverName, primaryDirectory, EnumSet.noneOf(OpenDatabase.class));) {
			String userNameCanonical = NotesNamingUtils.toCanonicalName(userName);
			Memory userNameCanonicalMem = NotesStringUtils.toLMBCS(userNameCanonical, true);
			Memory passwordMem = NotesStringUtils.toLMBCS(password, true);
			Memory idPathMem = idPath==null ? null : NotesStringUtils.toLMBCS(idPath.toString(), true);
			Memory serverNameMem = new Memory(NotesConstants.MAXPATH);
			{
				Memory serverNameParamMem = NotesStringUtils.toLMBCS(serverName, true);
				if (serverNameParamMem!=null && (serverNameParamMem.size() > NotesConstants.MAXPATH)) {
					throw new IllegalArgumentException(MessageFormat.format("Servername length cannot exceed MAXPATH ({0} characters)", NotesConstants.MAXPATH));
				}
				if (serverNameParamMem!=null) {
					byte[] serverNameParamArr = serverNameParamMem.getByteArray(0, (int) serverNameParamMem.size());
					serverNameMem.write(0, serverNameParamArr, 0, serverNameParamArr.length);
				}
				else {
					serverNameMem.setByte(0, (byte) 0);
				}
			}
			
			short result = NotesCAPI.get().SECKFMOpen (phKFC, idPathMem, passwordMem,
					NotesConstants.SECKFM_open_All, 0, null);
			NotesErrorUtils.checkResult(result);
			
			try {
				result = NotesCAPI.get().SECidfPut(userNameCanonicalMem, passwordMem, idPathMem, phKFC,
						serverNameMem, 0, (short) 0, null);
				NotesErrorUtils.checkResult(result);
			}
			finally {
				result = NotesCAPI.get().SECKFMClose(phKFC, NotesConstants.SECKFM_close_WriteIdFile, 0, null);
				NotesErrorUtils.checkResult(result);
			}
					
			int vaultServerNameLength = 0;
			for (int i=0; i<serverNameMem.size(); i++) {
				if (serverNameMem.getByte(i) == 0) {
					break;
				}
				else {
					vaultServerNameLength = i;
				}
			}
			
			String vaultServerName = NotesStringUtils.fromLMBCS(serverNameMem, vaultServerNameLength);
			return vaultServerName;
		}
	}
	
	/**
	 * Container for the ID vault sync result data
	 * 
	 * @author Karsten Lehmann
	 */
	public static class JNASyncResult implements SyncResult {
		private String m_vaultServer;
		private int m_flags;
		
		private JNASyncResult(String vaultServer, int flags) {
			m_vaultServer = vaultServer;
			m_flags = flags;
		}
		
		@Override
		public String getVaultServer() {
			return m_vaultServer;
		}
		
		@Override
		public boolean isIdSyncDone() {
			return (m_flags & 1) == 1;
		}
		
		@Override
		public boolean isIdFoundInVault() {
			if ((m_flags & 2) == 2) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	/**
	 * Helper method that reads an ID info field as string
	 * 
	 * @param notesAPI api
	 * @param idPathMem Memory with id path
	 * @param infoType info type
	 * @param initialBufferSize initial buffer size
	 * @return result
	 */
	private static String getIDInfoAsString(Memory idPathMem, short infoType, int initialBufferSize) {
		Memory retMem = new Memory(initialBufferSize);
		ShortByReference retActualLen = new ShortByReference();
		
		short result = NotesCAPI.get().REGGetIDInfo(idPathMem, infoType, retMem, (short) retMem.size(), retActualLen);
		if (result == INotesErrorConstants.ERR_VALUE_LENGTH) {
			int requiredLen = retActualLen.getValue() & 0xffff;
			retMem = new Memory(requiredLen);
			result = NotesCAPI.get().REGGetIDInfo(idPathMem, infoType, retMem, (short) retMem.size(), retActualLen);
		}
		
		NotesErrorUtils.checkResult(result);
		String data = NotesStringUtils.fromLMBCS(retMem, (retActualLen.getValue() & 0xffff)-1);
		return data;
	}
	
	@Override
	public Set<IdFlag> getIdFlags() {
		return getIDFlags(null);
	}

	@Override
	public Set<IdFlag> getIDFlags(UserId userId) {
		checkDisposed();
		
		if (userId!=null && !(userId instanceof JNAUserId)) {
			throw new IllegalArgumentException("User id must be a JNAUserID");
		}
		
		try(DisposableMemory idFlagsMem = new DisposableMemory(4);
		    DisposableMemory idFlags1Mem = new DisposableMemory(4)) {
	    idFlagsMem.clear();
	    idFlags1Mem.clear();
			short result = NotesCAPI.get().SECKFMAccess(NotesConstants.KFM_access_GetIDFHFlags, userId==null ? null : userId.getAdapter(Pointer.class), idFlagsMem, idFlags1Mem);
			NotesErrorUtils.checkResult(result);
			
			Set<IdFlag> retFlags = new HashSet<>();
			
			short idFlags = idFlagsMem.getShort(0);
			short idFlags1 = idFlags1Mem.getShort(0);

			for (IdFlag currFlag : IdFlag.values()) {
				int currFlagVal = currFlag.getValue();
				
				if ((currFlagVal & 0x8000000) == 0x8000000) {
					short currFlag1ValShort = (short) (currFlagVal & 0xffff);
					
					if ((idFlags1 & currFlag1ValShort) == currFlag1ValShort) {
						retFlags.add(currFlag);
					}
				}
				else {
					short currFlagValShort = (short) (currFlagVal & 0xffff);
					
					if ((idFlags & currFlagValShort) == currFlagValShort) {
						retFlags.add(currFlag);
					}
				}
			}
			
			return retFlags;
		}
	}
	
}
