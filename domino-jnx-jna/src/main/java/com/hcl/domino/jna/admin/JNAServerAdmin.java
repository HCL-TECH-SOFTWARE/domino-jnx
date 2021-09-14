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
package com.hcl.domino.jna.admin;

import java.lang.ref.ReferenceQueue;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.OpenDatabase;
import com.hcl.domino.DominoException;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.admin.DirectoryAssistance;
import com.hcl.domino.admin.IConsoleLine;
import com.hcl.domino.admin.ServerAdmin;
import com.hcl.domino.commons.admin.ConsoleLine;
import com.hcl.domino.commons.admin.DefaultDominoSSOToken;
import com.hcl.domino.commons.admin.DefaultMultiDatabaseAccessInfo;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.structs.ISSOTokenStruct;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.dbdirectory.DatabaseData;
import com.hcl.domino.dbdirectory.FileType;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.data.JNAAcl;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.data.JNAUserNamesList;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.ItemDecoder;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.Mem.LockedMemory;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesNamingUtils.Privileges;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.Win32NotesCallbacks;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNAServerAdminAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.KFM_PASSWORDStruct;
import com.hcl.domino.jna.internal.structs.NotesConsoleEntryStruct;
import com.hcl.domino.jna.internal.structs.NotesSSOTokenStruct32;
import com.hcl.domino.jna.internal.structs.NotesSSOTokenStruct64;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.structs.WinNotesSSOTokenStruct32;
import com.hcl.domino.jna.internal.structs.WinNotesSSOTokenStruct64;
import com.hcl.domino.jna.server.JNAServerStatusLine;
import com.hcl.domino.jna.utils.JNADominoUtils;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.Pair;
import com.hcl.domino.security.AclAccess;
import com.hcl.domino.server.ServerStatusLine;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * JNA implementation of {@link ServerAdmin} with server administration APIs.
 * 
 * @author Karsten Lehmann
 */
public class JNAServerAdmin extends BaseJNAAPIObject<JNAServerAdminAllocations> implements ServerAdmin {
	
	public JNAServerAdmin(IAPIObject<?> parent) {
		super(parent);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNAServerAdminAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		return new JNAServerAdminAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public void markInService(String server, String filePath) {
		DisposableMemory dbPathMem = JNADominoUtils.constructNetPath(getParentDominoClient(), server, filePath);
		try {
			short result = NotesCAPI.get().NSFDbMarkInService(dbPathMem);
			NotesErrorUtils.checkResult(result);
		}
		finally {
			dbPathMem.dispose();
		}
	}

	@Override
	public void markOutOfService(String server, String filePath) {
		DisposableMemory dbPathMem = JNADominoUtils.constructNetPath(getParentDominoClient(), server, filePath);
		try {
			short result = NotesCAPI.get().NSFDbMarkOutOfService(dbPathMem);
			NotesErrorUtils.checkResult(result);
		}
		finally {
			dbPathMem.dispose();
		}
	}

	@Override
	public String sendConsoleCommand(String server, String command) {
		if (StringUtil.isEmpty(server)) {
			//NSFRemoteConsole returns an error if server is empty
			server = getParentDominoClient().getIDUserName();
		}
		Memory serverMem = NotesStringUtils.toLMBCS(server, true);
		Memory commandMem = NotesStringUtils.toLMBCS(command, true);

		DHANDLE.ByReference hResponseText = DHANDLE.newInstanceByReference();

		short result = NotesCAPI.get().NSFRemoteConsole(serverMem, commandMem, hResponseText);
		NotesErrorUtils.checkResult(result);

		if (hResponseText.isNull()) {
			return ""; //$NON-NLS-1$
		}
		
		return LockUtil.lockHandle(hResponseText, (hResponseTextByVal) -> {
			Pointer ptr = Mem.OSLockObject(hResponseTextByVal);
			try {
				String txt = NotesStringUtils.fromLMBCS(ptr, -1);
				return txt;
			}
			finally {
				Mem.OSUnlockObject(hResponseTextByVal);
				Mem.OSMemFree(hResponseTextByVal);
			}
		});
	}

	@Override
	public DominoSSOToken generateSSOToken(String orgName, String configName, String userName,
			TemporalAccessor creationDate, TemporalAccessor expirationDate, boolean enableRenewal) {

		NotesTimeDateStruct creationDateStruct = creationDate==null ? null : NotesTimeDateStruct.newInstance(creationDate);
		NotesTimeDateStruct expirationDateStruct = expirationDate==null ? null : NotesTimeDateStruct.newInstance(expirationDate);
		
		Memory orgNameMem = NotesStringUtils.toLMBCS(orgName, true);
		Memory configNameMem = NotesStringUtils.toLMBCS(configName, true);

		String userNameCanonical = NotesNamingUtils.toCanonicalName(userName);
		Memory userNameCanonicalMem = NotesStringUtils.toLMBCS(userNameCanonical, true);

		NotesTimeDateStruct renewalDate = enableRenewal ? NotesTimeDateStruct.newInstance() : null;
		
		IntByReference retmhToken = new IntByReference();
		retmhToken.setValue(0);
		
		short result = NotesCAPI.get().SECTokenGenerate(null, orgNameMem, configNameMem, userNameCanonicalMem,
				creationDateStruct, expirationDateStruct,
				retmhToken, enableRenewal ? NotesConstants.fSECToken_EnableRenewal : 0, renewalDate==null ? null : renewalDate.getPointer());
		NotesErrorUtils.checkResult(result);
		
		if (renewalDate!=null) {
			renewalDate.read();
		}

		int hToken = retmhToken.getValue();
		if (hToken==0) {
			throw new IllegalStateException("SECTokenGenerate returned null value for the SSO token");
		}

		try (LockedMemory mem = Mem.OSMemoryLock(hToken);) {
			Pointer ptr = mem.getPointer();

			String name=null;
			List<String> domains;
			String data=null;
			boolean isSecureOnly;
			
			ISSOTokenStruct tokenData;

			if (PlatformUtils.is64Bit()) {
				if (PlatformUtils.isWindows()) {
					tokenData = WinNotesSSOTokenStruct64.newInstance(ptr);
					((WinNotesSSOTokenStruct64)tokenData).read();
				}
				else {
					tokenData = NotesSSOTokenStruct64.newInstance(ptr);
					((NotesSSOTokenStruct64)tokenData).read();
				}
			}
			else {
				if (PlatformUtils.isWindows()) {
					tokenData = WinNotesSSOTokenStruct32.newInstance(ptr);
					((WinNotesSSOTokenStruct32)tokenData).read();
				}
				else {
					tokenData = NotesSSOTokenStruct32.newInstance(ptr);
					((NotesSSOTokenStruct32)tokenData).read();
				}
			}
			
			//decode name
			if (tokenData.getNameHandle()!=0) {
				try (LockedMemory nameMem = Mem.OSMemoryLock(tokenData.getNameHandle())) {
					Pointer ptrName = nameMem.getPointer();
					name = NotesStringUtils.fromLMBCS(ptrName, -1);
				}
			}
			
			//decode domain list
			if (tokenData.getNumDomains()>0 && tokenData.getDomainListHandle()!=0) {
				try (LockedMemory domainsMem = Mem.OSMemoryLock(tokenData.getDomainListHandle())) {
					Pointer ptrDomains = domainsMem.getPointer();
					domains = NotesStringUtils.fromLMBCSStringList(ptrDomains, tokenData.getNumDomains() & 0xffff);
				}
			}
			else {
				domains = Collections.emptyList();
			}
			
			if (tokenData.getDataHandle()!=0) {
				try (LockedMemory dataMem = Mem.OSMemoryLock(tokenData.getDataHandle())) {
					Pointer ptrData = dataMem.getPointer();
					data = NotesStringUtils.fromLMBCS(ptrData, -1);
				}
			}
			isSecureOnly = tokenData.getSecureOnlyVal() != 0;

			DefaultDominoSSOToken ssoToken = new DefaultDominoSSOToken(name, domains, isSecureOnly, data, renewalDate==null ? null : new JNADominoDateTime(renewalDate));
			return ssoToken;
		
		}
		finally {
			//frees SSO_TOKEN and its members
			Mem.OSMemoryFree(hToken);
		}
	}

	@Override
	public UserNamesList getUserNamesList(String server, String username) {
		return NotesNamingUtils.buildNamesList(getParentDominoClient(), server, username);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getServerClusterMates(String serverName, ClusterLookup lookupMode) {
		Memory serverNameCanonical = serverName==null ? null : NotesStringUtils.toLMBCS(NotesNamingUtils.toCanonicalName(serverName), true);

		DHANDLE.ByReference phList = DHANDLE.newInstanceByReference();

		short result = NotesCAPI.get().NSGetServerClusterMates(serverNameCanonical, lookupMode==null ? 0 : lookupMode.getValue(), phList);
		
		if (result == 2078) {
			return Collections.emptyList();
		}
		NotesErrorUtils.checkResult(result);

		if (phList.isNull()) {
			return Collections.emptyList();
		}

		return LockUtil.lockHandle(phList, (hListByVal) -> {
			Pointer pList = Mem.OSLockObject(hListByVal);
			try {
				@SuppressWarnings("rawtypes")
				List clusterMates = ItemDecoder.decodeTextListValue(pList, false);
				return (List<String>) clusterMates;
			}
			finally {
				Mem.OSUnlockObject(hListByVal);
				Mem.OSMemFree(hListByVal);
			}
		});
	}
	
	@Override
	public MultiDatabaseAccessInfo computeDatabaseAccess(String server,
			String username, boolean forceNewScan) {
		
		JNAUserNamesList usernamesList = NotesNamingUtils.buildNamesList(getParentDominoClient(), server, username);
		return computeDatabaseAccess(server, usernamesList, forceNewScan);
	}

	@Override
	public MultiDatabaseAccessInfo computeDatabaseAccess(String server,
			UserNamesList user, boolean forceNewScan) {

		if (server==null) {
			server = ""; //$NON-NLS-1$
		}
		JNAServerAdminAllocations allocations = getAllocations();

		DominoClient client = getParentDominoClient();
		
		if (forceNewScan || !allocations.isACLCacheInitialized(server)) {
			AtomicInteger dbCount = new AtomicInteger();
			
			allocations.flushAclCache(server);

			String idUsername = client.getIDUserName();
			
			//check if we can get full access rights on the server
			boolean hasFullAccess;
			{
				JNAUserNamesList namesListFullAccess = NotesNamingUtils.buildNamesList((JNADominoClient) client, server, idUsername);
				NotesNamingUtils.setPrivileges(namesListFullAccess, EnumSet.of(Privileges.Authenticated, Privileges.FullAdminAccess));

				try (JNADatabase db = new JNADatabase(getParentDominoClient(), server, "names.nsf", //$NON-NLS-1$
						EnumSet.noneOf(OpenDatabase.class), namesListFullAccess);) {
					
					hasFullAccess = db.hasFullAccess();
				}
				catch (DominoException e) {
					hasFullAccess = false;
				}
			}
			
			boolean fHasFullAccess = hasFullAccess;
			
//			long t0=System.currentTimeMillis();
			
			//scan database directory and collect cloned ACLs
			client
			.openDbDirectory()
			.query()
			.withServer(server)
			.withFileTypes(EnumSet.of(FileType.DBANY))
			.stream()
			.forEach((fileEntry) -> {
				if (fileEntry instanceof DatabaseData) {
					DatabaseData dbData = (DatabaseData) fileEntry;

					//try to open the DB with full access
					JNAUserNamesList namesListFullAccess = null;
					JNADatabase db = null;
					DominoException dbOpenException = null;
					if (fHasFullAccess) {
						try {
							namesListFullAccess = NotesNamingUtils.buildNamesList((JNADominoClient) client, dbData.getServer(), idUsername);
							NotesNamingUtils.setPrivileges(namesListFullAccess, EnumSet.of(Privileges.Authenticated, Privileges.FullAdminAccess));

							db = new JNADatabase(getParentDominoClient(), dbData.getServer(), dbData.getFilePath(),
									EnumSet.noneOf(OpenDatabase.class), namesListFullAccess);
						}
						catch (DominoException e) {
							db = null;
							dbOpenException = e;
							if (namesListFullAccess!=null) {
								namesListFullAccess.dispose();
							}
						}
					}

					//if opening failed, try a regular DB open with the current user rights
					if (db==null && (dbOpenException==null || (dbOpenException!=null && dbOpenException.getId()==582))) { // not authorized
						try {
							db = new JNADatabase(getParentDominoClient(), dbData.getServer(), dbData.getFilePath(),
									EnumSet.noneOf(OpenDatabase.class));
						}
						catch (DominoException e) {
//							System.out.println("ERR "+e.getId()+" opening database "+dbData.getServer()+"!!"+dbData.getFilePath());
//							e.printStackTrace();
						}
					}

					if (db!=null) {
						try {
							JNAAcl acl = (JNAAcl) db.getACL();
							JNAAcl aclClone = acl.cloneDetached();

							allocations.addACLToCache(dbData, aclClone);
							dbCount.incrementAndGet();
						}
						catch (DominoException e) {
							System.err.println(MessageFormat.format("ERR {0} opening database {1}!!{2}", e.getId(), dbData.getServer(), dbData.getFilePath()));
							e.printStackTrace();
						}
						finally {
							db.close();
						}
					}
				}
			});
			
//			long t1=System.currentTimeMillis();
//			System.out.println("Collected "+dbCount+" ACLs in "+(t1-t0)+"ms");
		}

		
		List<Pair<DatabaseData, AclAccess>> pairs = allocations.computeDatabaseAccess(server, user);
		return new DefaultMultiDatabaseAccessInfo(server, user, pairs);
	}

	@Override
	public void registerCrossCertificate(String certFilePath, String certPW, String certLogPath,
			DominoDateTime expirationDateTime, String regServer, Path idFilePath, String comment,
			RegistrationMessageHandler msgHandler) {
		
		checkDisposed();
		
		Memory certPWMem = NotesStringUtils.toLMBCS(certPW, true);
		
		KFM_PASSWORDStruct.ByReference kfmPwd = KFM_PASSWORDStruct.newInstanceByReference();
		NotesCAPI.get().SECKFMCreatePassword(certPWMem, kfmPwd);
		
		Memory certFilePathMem = NotesStringUtils.toLMBCS(certFilePath, true);
		Memory certLogPathMem = NotesStringUtils.toLMBCS(certLogPath, true);
		
		JNADominoDateTime jnaExpirationDateTime = JNADominoDateTime.from(expirationDateTime);
		NotesTimeDateStruct.ByReference tdStructByRef = NotesTimeDateStruct.newInstanceByReference();
		tdStructByRef.Innards = jnaExpirationDateTime.getInnards();
		tdStructByRef.write();
		
		Memory retCertNameMem = new Memory(NotesConstants.MAXUSERNAME);
		HANDLE.ByReference rethKfmCertCtx = HANDLE.newInstanceByReference();
		ShortByReference retfIsHierarchical = new ShortByReference();
		ShortByReference retwFileVersion = new ShortByReference();
		
		String regServerCanonical = NotesNamingUtils.toCanonicalName(regServer);
		Memory regServerCanonicalMem = NotesStringUtils.toLMBCS(regServerCanonical, true);
		
		short result = NotesCAPI.get().SECKFMGetCertifierCtx(certFilePathMem, kfmPwd, certLogPathMem, tdStructByRef, retCertNameMem,
				rethKfmCertCtx, retfIsHierarchical, retwFileVersion);
		NotesErrorUtils.checkResult(result);
		try {
			Memory locationMem = null;
			Memory forwardAddressMem = null;
			
			NotesCallbacks.REGSIGNALPROC statusCallback;
			if (PlatformUtils.isWin32()) {
				statusCallback = ptrMessage -> {
					if (msgHandler!=null) {
						String msg = NotesStringUtils.fromLMBCS(ptrMessage, -1);
						msgHandler.messageReceived(msg);
					}
				};
			}
			else {
				statusCallback = ptrMessage -> {
					if (msgHandler!=null) {
						String msg = NotesStringUtils.fromLMBCS(ptrMessage, -1);
						msgHandler.messageReceived(msg);
					}
				};
			}
			
			Memory idFilePathMem = NotesStringUtils.toLMBCS(idFilePath.toString(), true);
			Memory errorPathNameMem = new Memory(NotesConstants.MAXPATH);
			Memory commentMem = NotesStringUtils.toLMBCS(comment, true);
			
			result = LockUtil.lockHandle(rethKfmCertCtx, (rethKfmCertCtxByVal) -> {
				return NotesCAPI.get().REGCrossCertifyID(rethKfmCertCtxByVal, (short) 0, regServerCanonicalMem,
						idFilePathMem,
						locationMem, commentMem, forwardAddressMem, (short) 0, statusCallback, errorPathNameMem);
				
			});
			NotesErrorUtils.checkResult(result);
		}
		finally {
			if (!rethKfmCertCtx.isNull()) {
				LockUtil.lockHandle(rethKfmCertCtx, (rethKfmCertCtxByVal) -> {
					NotesCAPI.get().SECKFMFreeCertifierCtx(rethKfmCertCtxByVal);
					
					return 0;
				});
			}
			
		}
	}

	@Override
	public ServerStatusLine createServerStatusLine(String taskName) {
		long hDesc = NotesCAPI.get().AddInCreateStatusLine(NotesStringUtils.toLMBCS(taskName, true));
		return new JNAServerStatusLine(getParentDominoClient(), hDesc);
	}

	@Override
	public void openServerConsole(String serverName, ConsoleHandler handler) {
		String serverNameCanonical = StringUtil.isEmpty(serverName) ? getParentDominoClient().getIDUserName() : NotesNamingUtils.toCanonicalName(serverName);
		Memory serverNameCanonicalMem = NotesStringUtils.toLMBCS(serverNameCanonical, true);
		
		{
			//for simplicity we send an empty synchronous remote console command
			//to check for the error "You are not authorized to use the remote console on this server"
			//otherwise that error status is just returned asynchronously via ASYNC_CONTEXT and the required
			//data structure is quite complex
			DHANDLE.ByReference hResponseText = DHANDLE.newInstanceByReference();

			short result = NotesCAPI.get().NSFRemoteConsole(serverNameCanonicalMem, null, hResponseText);
			NotesErrorUtils.checkResult(result);

			if (!hResponseText.isNull()) {
				LockUtil.lockHandle(hResponseText, (hResponseTextByVal) -> {
					Mem.OSMemFree(hResponseTextByVal);
					
					return null;
				});
			}
		}
		
		String cmd = null; //"sh ta";
		Memory cmdMem = StringUtil.isEmpty(cmd) ? null : NotesStringUtils.toLMBCS(cmd, true);
		
		PointerByReference pAsyncCtx = new PointerByReference();
		DHANDLE.ByReference hAsyncQueue = DHANDLE.newInstanceByReference();
		DHANDLE.ByReference hAsyncBuffer = DHANDLE.newInstanceByReference();

		ShortByReference wSignals = new ShortByReference();
		IntByReference dwConsoleBuffID = new IntByReference();

		short result = NotesCAPI.get().QueueCreate(hAsyncQueue);
		NotesErrorUtils.checkResult(result);
		
		boolean asyncIOInitDone = false;

		try {
			NotesCallbacks.ASYNCNOTIFYPROC callback;
			if (PlatformUtils.isWin32()) {
				callback = new Win32NotesCallbacks.ASYNCNOTIFYPROCWin32() {
					
					@Override
					public void invoke(Pointer p1, Pointer p2) {
					}
				};
			}
			else {
				callback = new NotesCallbacks.ASYNCNOTIFYPROC() {
					
					@Override
					public void invoke(Pointer p1, Pointer p2) {
					}
				};
			}

			result = LockUtil.lockHandle(hAsyncQueue, (hAsyncQueueByVal) -> {
				return NotesCAPI.get().NSFRemoteConsoleAsync(serverNameCanonicalMem, cmdMem,
						NotesConstants.REMCON_GET_CONSOLE | NotesConstants.REMCON_GET_CONSOLE_META,
						hAsyncBuffer,
						null, null, wSignals, dwConsoleBuffID, hAsyncQueueByVal, callback, null, pAsyncCtx);
			});
			NotesErrorUtils.checkResult(result);
			
			while (!Thread.currentThread().isInterrupted()) {
				if (handler.shouldStop()) {
					break;
				}

				NotesCAPI.get().NSFAsyncNotifyPoll(new Pointer(0), null, null);
				NotesCAPI.get().NSFUpdateAsyncIOStatus(pAsyncCtx.getValue());
				asyncIOInitDone = true;

				short hasData = LockUtil.lockHandle(hAsyncQueue, (hAsyncQueueByVal) -> {
					return NotesCAPI.get().QueueGet(hAsyncQueueByVal, hAsyncBuffer);
				});
				
				if (hasData==0) {
					LockUtil.lockHandle(hAsyncBuffer, (hAsyncBufferByVal) -> {
						Pointer ptr = Mem.OSLockObject(hAsyncBufferByVal);
						try {
							NotesConsoleEntryStruct consoleEntry = NotesConsoleEntryStruct.newInstance(ptr);
							consoleEntry.read();

							int len = consoleEntry.length;
							if (consoleEntry.type == 1) {
								String lineEncoded = NotesStringUtils.fromLMBCS(ptr.share(consoleEntry.size()),
										len);
								IConsoleLine consoleLine = ConsoleLine.parseConsoleLine(lineEncoded, 0);
								handler.messageReceived(consoleLine);
							}
						}
						finally {
							Mem.OSUnlockObject(hAsyncBufferByVal);
							Mem.OSMemFree(hAsyncBufferByVal);
						}
						return null;
					});
				}
				else if(hasData==INotesErrorConstants.ERR_QUEUE_EMPTY) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						break;
					}
				} else {
					NotesErrorUtils.checkResult(hasData);
				}
			}
		}
		finally {
			if (asyncIOInitDone) {
				if (pAsyncCtx.getValue()!=null) {
					NotesCAPI.get().NSFCancelAsyncIO(pAsyncCtx.getValue());
				}
			}
		
			LockUtil.lockHandle(hAsyncQueue, (hAsyncQueueByVal) -> {
				return NotesCAPI.get().QueueDelete(hAsyncQueueByVal);
			});
		}
		
	}

	@Override
	public void logMessage(String messageText) {
		Memory lmbcs = NotesStringUtils.toLMBCS(messageText, true);
		NotesCAPI.get().AddInLogMessageText(lmbcs, (short)0, new Object[0]);
	}
	
	@Override
	public DirectoryAssistance getDirectoryAssistance(String serverName, String dirAssistDBName) {
	  if(StringUtil.isEmpty(serverName)) {
	    throw new IllegalArgumentException("serverName cannot be empty");
	  }
	  if(StringUtil.isEmpty(dirAssistDBName)) {
      throw new IllegalArgumentException("dirAssistDBName cannot be empty");
	  }
	  return new JNADirectoryAssistance(serverName, dirAssistDBName);
	}
	
	
}
