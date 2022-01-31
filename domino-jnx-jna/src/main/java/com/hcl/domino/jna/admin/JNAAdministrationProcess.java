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
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.hcl.domino.DominoException;
import com.hcl.domino.Name;
import com.hcl.domino.admin.AdministrationProcess;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.CollectionType;
import com.hcl.domino.data.DatabaseOption;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.DominoUniversalNoteId;
import com.hcl.domino.data.Find;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.data.JNADominoCollection;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.data.JNADominoUniversalNoteId;
import com.hcl.domino.jna.data.JNAUserNamesList;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesNamingUtils.Privileges;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.NotesStringUtils.LineBreakConversion;
import com.hcl.domino.jna.internal.adminp.AdminpDocumentItems;
import com.hcl.domino.jna.internal.adminp.AdminpRequestProfileEntries;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNAAdministrationProcessAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADocumentAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NotesStringDescStruct;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.NotesConstants.EProvidingAuthor;
import com.hcl.domino.misc.NotesConstants.EUsePABsAdminServer;
import com.hcl.domino.naming.Names;
import com.hcl.domino.security.Acl;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

public class JNAAdministrationProcess extends BaseJNAAPIObject<JNAAdministrationProcessAllocations> implements AdministrationProcess {
	private String m_serverNameCanonical;
	private JNADatabase m_dbNAB;
	private JNADatabase m_dbProxy;

	private DominoDateTime m_certExpire;
	private String m_certAuthorityOrg;
	private String m_certIdPath;
	private String m_certPwd;
	private boolean m_useCertAuth;

	public JNAAdministrationProcess(IAPIObject<?> parent, String server) {
		super(parent);
		m_serverNameCanonical = server==null ? "" : NotesNamingUtils.toCanonicalName(server);
		m_certExpire = JNADominoDateTime.from(LocalDateTime.now().plusYears(2).toInstant(ZoneOffset.UTC));
	}

	@SuppressWarnings("rawtypes")
  @Override
	protected JNAAdministrationProcessAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		return new JNAAdministrationProcessAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public String getServer() {
		return m_serverNameCanonical;
	}

	@Override
	public String getCertificateAuthorityOrg() {
		return m_certAuthorityOrg;
	}

	@Override
	public void setCertificateAuthorityOrg(String caa) {
		m_certAuthorityOrg = caa;
	}

	@Override
	public DominoDateTime getCertificateExpiration() {
		return m_certExpire;
	}

	@Override
	public void setCertificateExpiration(Temporal ce) {
		m_certExpire = JNADominoDateTime.from(Objects.requireNonNull(ce));
	}

	@Override
	public String getCertifierFile() {
		return m_certIdPath;
	}

	@Override
	public void setCertifierFile(String cf) {
		m_certIdPath = cf;
	}

	@Override
	public String getCertifierPassword() {
		return m_certPwd;
	}

	@Override
	public void setCertifierPassword(String cf) {
		m_certPwd = cf;
	}

	@Override
	public boolean isCertificateAuthorityAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUseCertificateAuthority() {
		return m_useCertAuth;
	}

	@Override
	public void setUseCertificateAuthority(boolean uca) {
		m_useCertAuth = uca;
	}

	@Override
	public Optional<Integer> addGroupMembers(String group, Collection<String> members) {
		if (StringUtil.isEmpty(group)) {
			throw new IllegalArgumentException("Group name is null or empty");
		}
		if (members.isEmpty()) {
			throw new IllegalArgumentException("Member list cannot be empty");
		}

		// get proxy db
		JNADatabase dbProxy = openProxyDB();

		AdminpRequestProfileEntries arp = new AdminpRequestProfileEntries(dbProxy);
		List<String> viewLookupKeys = new ArrayList<>();
		AdminpDocumentItems docItems = new AdminpDocumentItems(dbProxy);

		// preparation for AdminpFindProxyDBEntry 
		viewLookupKeys.add(NotesConstants.ADMINP);
		viewLookupKeys.add(NotesConstants.ADMINP_ALL_SERVERS);
		viewLookupKeys.add(NotesConstants.AdminpAddGroup);
		viewLookupKeys.add(group);

		// group name				
		arp.add(NotesConstants.ADMINP_NAME_LIST_ITEM,
				EnumSet.of(ItemFlag.SIGNED, ItemFlag.PROTECTED, ItemFlag.SUMMARY), group);

		arp.add(NotesConstants.ADMINP_DOC_MEMBER_LIST_ITEM,
				EnumSet.of(ItemFlag.SIGNED, ItemFlag.PROTECTED, ItemFlag.SUMMARY), new ArrayList<>(members));

		if (members.size() > 1) {
			docItems.add(NotesConstants.ADMINP_DOC_MEMBER_LIST_ITEM, new ArrayList<>(members));
		}
		else {
			docItems.add(NotesConstants.ADMINP_DOC_MEMBER_LIST_ITEM, members.iterator().next());
		}

		String proxyAuthor = getParentDominoClient().getEffectiveUserName();
		arp.add(NotesConstants.ADMINP_DOC_AUTHOR_ITEM,
				EnumSet.of(ItemFlag.SIGNED, ItemFlag.PROTECTED, ItemFlag.SUMMARY), proxyAuthor);

		docItems.add(NotesConstants.ADMINP_DOC_AUTHOR_ITEM, proxyAuthor);

		arp.add(NotesConstants.ADMINP_ORG_NAME_ITEM,
				EnumSet.of(ItemFlag.SIGNED, ItemFlag.PROTECTED, ItemFlag.SUMMARY), proxyAuthor);

		docItems.add(NotesConstants.ADMINP_ORG_NAME_ITEM, proxyAuthor);

		JNADatabaseAllocations dbProxyAllocations = (JNADatabaseAllocations) dbProxy.getAdapter(APIObjectAllocations.class);
		short result = LockUtil.lockHandle(dbProxyAllocations.getDBHandle(), (hdbProxyByVal) -> {
			Memory adminpAddGroupMem = NotesStringUtils.toLMBCS(NotesConstants.AdminpAddGroup, true);
			short wItem = (short) (arp.size() & 0xffff);
			DisposableMemory arpMem = arp.toStruct();
			try {
				return NotesCAPI.get().AdminpCreateRequest(hdbProxyByVal, adminpAddGroupMem,
						wItem, arpMem, EProvidingAuthor.AuthorIsProvided.getValue(),
						EUsePABsAdminServer.UsePABsAdminServer.getValue(), null, (short) 0);
			}
			finally {
				arp.dispose();
				arpMem.dispose();
			}
		});
		NotesErrorUtils.checkResult(result);

		return findProxyDbEntry(dbProxy, viewLookupKeys, docItems);
	}

	/**
	 * Tries to find a request document in admin4.nsf that matches the specified
	 * lookup keys / item names.
	 * 
	 * @param dbProxy admin4.nsf database
	 * @param viewLookupKeys lookup keys for view lookup
	 * @param docItems document items to compare document
	 * @return note id if found
	 */
	private Optional<Integer> findProxyDbEntry(JNADatabase dbProxy, List<String> viewLookupKeys, AdminpDocumentItems docItems) {
		JNADominoCollection requestsView = (JNADominoCollection) dbProxy.openCollection(NotesConstants.ADMINP_REQUESTS_VIEW)
				.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Required view {0} not found in database {1}",
						NotesConstants.ADMINP_REQUESTS_VIEW, dbProxy.getServer()+"!!"+dbProxy.getRelativeFilePath())));

		if (viewLookupKeys.isEmpty()) {
			throw new IllegalArgumentException("Lookup keys cannot be empty");
		}

		Collection<Object> lkKeysAsCol = new ArrayList<>();
		lkKeysAsCol.addAll(viewLookupKeys);

		Set<Integer> matchingIds = requestsView.getAllIdsByKey(
				EnumSet.of(Find.CASE_INSENSITIVE, Find.UPDATE_IF_NOT_FOUND),
				lkKeysAsCol);
		Integer[] matchingIdsArr = matchingIds.toArray(new Integer[matchingIds.size()]);

		for (int i=matchingIdsArr.length-1; i>=0; i--) {
			int currNoteId = matchingIdsArr[i];

			//process in reverse order
			Optional<Document> doc = dbProxy.getDocumentById(currNoteId);
			if (!doc.isPresent() && doc.get() instanceof JNADocument) {
				JNADocument jnaDoc = (JNADocument) doc.get();

				if (docItems.isMatch(doc.get())) {
					return Optional.of(currNoteId);
				}
				jnaDoc.close();
			}
		}

		return Optional.empty();
	}

	private JNADatabase openNAB() {
		checkDisposed();

		if (m_dbNAB==null || m_dbNAB.isDisposed()) {
			Memory serverNameMem = NotesStringUtils.toLMBCS(m_serverNameCanonical, true);
			HANDLE.ByReference rethDb = HANDLE.newInstanceByReference();

			short result = NotesCAPI.get().SECOpenAddressBookOnServer(serverNameMem, rethDb);
			NotesErrorUtils.checkResult(result);

			JNAUserNamesList serverNamesList = NotesNamingUtils.buildNamesList(getParent(), getParentDominoClient().getIDUserName());
			NotesNamingUtils.setPrivileges(serverNamesList, EnumSet.of(Privileges.Authenticated, Privileges.FullAdminAccess));

			m_dbNAB = new JNADatabase(getParentDominoClient(), new IAdaptable() {

				@Override
				@SuppressWarnings("unchecked")
				public <T> T getAdapter(Class<T> clazz) {
					if (HANDLE.class.equals(clazz)) {
						return (T) rethDb;
					}
					else if (JNAUserNamesList.class.equals(clazz)) {
						return (T) serverNamesList;
					}
					else {
						return null;
					}
				}
			});
		}
		return m_dbNAB;
	}

	private JNADatabase openProxyDB() {
		checkDisposed();

		if (m_dbProxy==null || m_dbProxy.isDisposed()) {
			Memory serverNameMem = NotesStringUtils.toLMBCS(m_serverNameCanonical, true);
			HANDLE.ByReference rethDb = HANDLE.newInstanceByReference();

			short result = NotesCAPI.get().AdminpProxyDbOpen(serverNameMem, rethDb);
			NotesErrorUtils.checkResult(result);

			JNAUserNamesList serverNamesList = NotesNamingUtils.buildNamesList(getParent(), getParentDominoClient().getIDUserName());
			NotesNamingUtils.setPrivileges(serverNamesList, EnumSet.of(Privileges.Authenticated, Privileges.FullAdminAccess));

			m_dbProxy = new JNADatabase(getParentDominoClient(), new IAdaptable() {

				@Override
				@SuppressWarnings("unchecked")
				public <T> T getAdapter(Class<T> clazz) {
					if (HANDLE.class.equals(clazz)) {
						return (T) rethDb;
					}
					else if (JNAUserNamesList.class.equals(clazz)) {
						return (T) serverNamesList;
					}
					else {
						return null;
					}
				}
			});
		}
		return m_dbProxy;
	}

	@Override
	public Optional<Integer> changeHTTPPassword(String username, String oldpassword, String newpassword) {
		checkDisposed();

		if (StringUtil.isEmpty(username)) {
			throw new IllegalArgumentException("Username cannot be empty");
		}

		if (StringUtil.isEmpty(oldpassword)) {
			throw new IllegalArgumentException("Old password cannot be empty");
		}

		if (StringUtil.isEmpty(newpassword)) {
			throw new IllegalArgumentException("New password cannot be empty");
		}

		NotesStringDescStruct.ByReference name = NotesStringDescStruct.newInstanceByReference();
		Memory usernameMem = NotesStringUtils.toLMBCS(username, false);
		if (name.size() > 0xffff) {
			throw new IllegalArgumentException("Username exceeds max length");
		}
		name.pText = usernameMem;
		name.wSize = (short) (usernameMem.size() & 0xffff);

		NotesStringDescStruct.ByReference oldPW = NotesStringDescStruct.newInstanceByReference();
		Memory oldpasswordMem = NotesStringUtils.toLMBCS(oldpassword, false);
		if (oldpasswordMem.size() > 0xffff) {
			throw new IllegalArgumentException("Old password exceeds max length");
		}
		oldPW.pText = oldpasswordMem;
		oldPW.wSize = (short) (oldpasswordMem.size() & 0xffff);

		NotesStringDescStruct.ByReference newPW = NotesStringDescStruct.newInstanceByReference();
		Memory newpasswordMem = NotesStringUtils.toLMBCS(newpassword, false);
		if (newpasswordMem.size() > 0xffff) {
			throw new IllegalArgumentException("New password exceeds max length");
		}
		newPW.pText = newpasswordMem;
		newPW.wSize = (short) (newpasswordMem.size() & 0xffff);

		IntByReference retNoteid = new IntByReference();

		Memory proxyDbServerMem = NotesStringUtils.toLMBCS(m_serverNameCanonical, true);

		short result = NotesCAPI.get().SECMakeProxyEntry((short) 17, null, proxyDbServerMem, name, null,
				null, newPW, oldPW, null, retNoteid);
		NotesErrorUtils.checkResult(result);

		return retNoteid.getValue()==0 ? Optional.empty() : Optional.of(retNoteid.getValue());
	}

	@Override
	public int renameGroup(String group, String newgroup) {
		checkDisposed();

		if (StringUtil.isEmpty(group)) {
			throw new IllegalArgumentException("Old group name cannot be empty");
		}

		if (StringUtil.isEmpty(newgroup)) {
			throw new IllegalArgumentException("New group name cannot be empty");
		}

		String groupCanonical = NotesNamingUtils.toCanonicalName(group);

		Optional<Document> nabDoc = getNABDoc(NotesConstants.REGISTER_GROUPSNAMESPACE, groupCanonical,
				AdminPOperation.RENAMEGROUP);
		if (!nabDoc.isPresent()) {
			throw new DominoException(MessageFormat.format("No group document found for: {0}", groupCanonical));
		}

		JNADatabase dbNAB = openNAB();

		AdminWorkItemCtx workCtx = new AdminWorkItemCtx();
		workCtx.setMethod(AdminPOperation.RENAMEGROUP);
		workCtx.setCanonAdminpServer(m_serverNameCanonical);
		workCtx.setDbNAB(dbNAB);
		workCtx.setNABDoc(nabDoc.get());
		workCtx.setNewGroup(newgroup);

		int noteId = doAdminWorkItem(workCtx);

		nabDoc.get().autoClosable().close();

		return noteId;
	}

	private enum AdminPOperation {
		CONFIGUREMAILAGENT,
		CONFIGUREMAILAGENTEXT,
		SETUSERPASSWORDSETTINGS,
		ADDSERVERTOCLUSTER,
		REMOVESERVERFROMCLUSTER,
		SETSERVERDIRECTORYASSISTANCESETTINGS,
		RENAMEGROUP
	}

	/**
	 * Opens a specified users person doc on the adminp server. Caller is
	 * responsible for disposing the document.
	 * 
	 * @param entryNameSpace name space of entry to be returned, may be {@link NotesConstants#USERNAMESSPACE} or {@limk NotesConstants#SERVERNAMESSPACE}
	 * @param szCanonEntryName canonical name of entry to lookup
	 * @param op when called by {@link AdminPOperation#CONFIGUREMAILAGENT} it uses a different method of obtaining DirEntryID which can handle extended directory
	 * @return entry doc if found
	 */
	private Optional<Document> getNABDoc(String entryNameSpace, String szCanonEntryName, AdminPOperation op) {
		return getNABDoc(openNAB(), entryNameSpace, szCanonEntryName, op);
	}
	
	/**
	 * Opens a specified users person doc on the adminp server. Caller is
	 * responsible for disposing the document.
	 * 
	 * @param dbNAB NAB database
	 * @param entryNameSpace name space of entry to be returned, may be USERNAMESSPACE or SERVERNAMESSPACE
	 * @param canonEntryName canonical name of entry to lookup
	 * @param op when called by {@link AdminPOperation#CONFIGUREMAILAGENT} it uses a different method of obtaining DirEntryID which can handle extended directory
	 * @return entry doc
	 */
	private Optional<Document> getNABDoc(JNADatabase dbNAB, String entryNameSpace, String canonEntryName,
			AdminPOperation op) {
		boolean bIsVirtualized = false;
		if (getParentDominoClient().isOnServer() && NotesCAPI.get().NetIsVirtualizedDirectory()) {
			bIsVirtualized = true;
		}

		String serverName = dbNAB.getServer();
		Memory serverNameMem = NotesStringUtils.toLMBCS(serverName, true);
		
		Memory canonEntryNameMem = NotesStringUtils.toLMBCS(canonEntryName, true);
		
		short result;
		
		if (NotesConstants.USERNAMESSPACE.equalsIgnoreCase(entryNameSpace)) {
			IntByReference wType = new IntByReference();
			wType.setValue(NotesConstants.DIRENTRY_TYPE_UNKNOWN);
			
			DHANDLE.ByReference hDirCtx = DHANDLE.newInstanceByReference();
			result = NotesCAPI.get().DirCtxAlloc2(serverNameMem, null, hDirCtx);
			NotesErrorUtils.checkResult(result);

			IntByReference rethDirEntry = new IntByReference();
			try {
				if (bIsVirtualized) {
					result = LockUtil.lockHandle(hDirCtx, (hDirCtxByVal) -> {
						return NotesCAPI.get().DirCtxSetFlags(hDirCtxByVal, NotesConstants.DIR_VIRTUALIZE);
					});
					NotesErrorUtils.checkResult(result);
				}

				//load dir entry id
				{
					DisposableMemory retDirEntryIDMem = new DisposableMemory(NotesConstants.MAXDIRENTRYID);
					retDirEntryIDMem.clear();
					try {
						/* pthn7s7mm9: 
					   for below methods only, lookup must be aware of Directory Assistance, and Extended Directory Configuration.
					   we will use a customized version of the oooapi method hijacked from misc/oooapi.c.
					   The reason this function is needed is that iNotes users may not be stored in the primary
					   directory, we need to obtain information from extended directory */
						if (op == AdminPOperation.CONFIGUREMAILAGENT ||
								op == AdminPOperation.CONFIGUREMAILAGENTEXT) {
							result = NotesCAPI.get().LookupUserDirID(canonEntryNameMem, serverNameMem,
									null, retDirEntryIDMem);
						}
						else {
							result = LockUtil.lockHandle(hDirCtx, (hDirCtxByVal) -> {
								return NotesCAPI.get().REGSearchByFullnameOrInternetAddress(hDirCtxByVal,
										canonEntryNameMem,
										true, retDirEntryIDMem);
							});
						}
						NotesErrorUtils.checkResult(result);

						/* Get the handle to the DirEntryID */

						result = LockUtil.lockHandle(hDirCtx, (hDirCtxByVal) -> {
							DisposableMemory itemNamesMem = NotesStringUtils.toLMBCSNoCache(
									NotesConstants.DIR_ITEMS_ALL_DOMINO + "\nObjectGUID", // \n will be converted to \0 //$NON-NLS-1$
									true, LineBreakConversion.NULL);

							short resultGetEntry = NotesCAPI.get().DirCtxGetEntryByID(hDirCtxByVal, retDirEntryIDMem,
									itemNamesMem, (short) 2, rethDirEntry);
							itemNamesMem.dispose();
							return resultGetEntry;
						});
						NotesErrorUtils.checkResult(result);
					}
					finally {
						retDirEntryIDMem.dispose();
					}
				}

				if (rethDirEntry.getValue() == 0) {
					return Optional.empty();
				}
				
				/* Verify the entry is the correct type, in this case DIRENTRY_TYPE_DOMINO_PERSON */
				result = NotesCAPI.get().DirEntryGetType(rethDirEntry.getValue(), wType);
				NotesErrorUtils.checkResult(result);
				
				if (wType.getValue() != NotesConstants.DIRENTRY_TYPE_DOMINO_PERSON) {
					return Optional.empty();
				}
				
				/* Create the virtual hNote */
				DHANDLE.ByReference retnhEntryDoc = DHANDLE.newInstanceByReference();
				result = NotesCAPI.get().DirEntryNoteGet(rethDirEntry.getValue(), retnhEntryDoc);
				NotesErrorUtils.checkResult(result);
				
				LockUtil.lockHandle(retnhEntryDoc, (retnhEntryDocByVal) -> {
					this.checkUserGUID(rethDirEntry.getValue(), retnhEntryDocByVal);
					return null;
				});
				
				JNADocument docNAB = new JNADocument(dbNAB, retnhEntryDoc, false);
				return Optional.of(docNAB);

			}
			finally {
				if (!hDirCtx.isNull()) {
					//dispose directory
					LockUtil.lockHandle(hDirCtx, (hDirCtxByVal) -> {
						NotesCAPI.get().DirCtxFree(hDirCtxByVal);
						return (short) 0;
					});
				}


				if (rethDirEntry.getValue()!=0) {
					NotesCAPI.get().DirEntryFree(rethDirEntry.getValue());
				}
			}
		}
		else {
			IntByReference nidEntryDoc = new IntByReference();
			Memory entryNameSpaceMem = NotesStringUtils.toLMBCS(entryNameSpace, true);
			
			final boolean bIsVirtualizedFinal = bIsVirtualized;
			
			JNADatabaseAllocations dbNABAllocations = (JNADatabaseAllocations) dbNAB.getAdapter(APIObjectAllocations.class);
			result = LockUtil.lockHandle(dbNABAllocations.getDBHandle(), (hDbNABByVal) -> {
				return NotesCAPI.get().REGFindAddressBookEntryExtended(hDbNABByVal, entryNameSpaceMem,
						canonEntryNameMem, true, nidEntryDoc, bIsVirtualizedFinal);
			});
			NotesErrorUtils.checkResult(result);
			
			if (nidEntryDoc.getValue() == 0) {
				return Optional.empty();
			}
			else {
				return dbNAB.getDocumentById(nidEntryDoc.getValue());
			}
		}
	}
	
	/**
	 * Adds the item {@link NotesConstants#MAIL_NETUSERNAME_ITEM} to the
	 * person document if it is missing
	 * 
	 * @param hDirEntry directory entry
	 * @param hUserNoteByVal handle to user document
	 */
	private void checkUserGUID(int hDirEntry, DHANDLE.ByValue hUserNoteByVal) {
		ShortByReference wDataType = new ShortByReference();
		IntByReference dwDataLen = new IntByReference();
		DHANDLE.ByReference hGUID = DHANDLE.newInstanceByReference();
		short result;

		DisposableMemory netUserNameBuffer = new DisposableMemory(NotesConstants.MAXUSERNAME);
		try {
			Memory itmNameNetUserName = NotesStringUtils.toLMBCS(NotesConstants.MAIL_NETUSERNAME_ITEM, true);

			/* if the person entry does not have a NetUserName field */
			if (NotesCAPI.get().NSFItemGetText(hUserNoteByVal,
					itmNameNetUserName,
					netUserNameBuffer, (short) (netUserNameBuffer.size() & 0xffff)) == 0) {
				
				/* see if the DIRENTRY has a GUID */
				if (
						((NotesCAPI.get().DirEntryGetItemByName(hDirEntry,
						NotesStringUtils.toLMBCS("ObjectGUID", true), //$NON-NLS-1$
						wDataType, hGUID, dwDataLen) & NotesConstants.ERR_MASK) == INotesErrorConstants.NOERROR) && 
						!hGUID.isNull() &&
						wDataType.getValue() == ((short) (ItemDataType.TYPE_USERDATA.getValue() & 0xffff)) &&
						dwDataLen.getValue() > 1 /* sizeof (BYTE) */) {

					/* convert to our internal string representation of the GUI */
					result = Mem.OSLockObject(hUserNoteByVal, (pByte) -> {
						// Skip format-name length
						byte tagLen = pByte.getByte(0);
						pByte = pByte.share(1);
						int idwDataLen = dwDataLen.getValue();
						idwDataLen -= 1;

						if (idwDataLen > tagLen) {
							// Skip format-name
							pByte = pByte.share(tagLen);
							idwDataLen -= tagLen;
							
							Formatter formatter = new Formatter();
							
							for (int i=0; i<idwDataLen; i++) {
								formatter.format("%02x", pByte.getByte(i)); //$NON-NLS-1$
							}
							
							String formattedId = formatter.toString().toUpperCase();
							formatter.close();

							Memory formattedIdMem = NotesStringUtils.toLMBCS(formattedId, false);
							short formattedIdLen = (short) (formattedIdMem.size() & 0xffff);
							
							/* add it to the person entry for later */
							return NotesCAPI.get().NSFItemSetText(hUserNoteByVal, itmNameNetUserName,
									formattedIdMem, formattedIdLen);
						}
						else {
							return INotesErrorConstants.NOERROR;
						}
					});
					NotesErrorUtils.checkResult(result);
				}
			}
		}
		finally {
			if (!hGUID.isNull()) {
				result = LockUtil.lockHandle(hGUID, (hGUIDByVal) -> {
					return Mem.OSMemFree(hGUIDByVal);
				});
				NotesErrorUtils.checkResult(result);
			}
			netUserNameBuffer.dispose();
		}
	}

	private static class AdminWorkItemCtx {
		private AdminPOperation method;
		private String szCanonAdminpServer;
		private JNADatabase dbNAB;
		private Document docNAB;

		// SetUserPasswordSettings
		private String chProxyDataCheck;
		private Number nProxyDataInterval;
		private Number nProxyDataGrace;
		private String szProxyDataWebOpts;

		private String szSubjectServer;
		private String szCluster;
		private String szDBFile;
		private String szNewGroup;

		public AdminPOperation getMethod() {
			return method;
		}

		public void setMethod(AdminPOperation method) {
			this.method = method;
		}

		public String getProxyDataCheck() {
			return chProxyDataCheck;
		}

		public void setProxyDataCheck(String chProxyDataCheck) {
			this.chProxyDataCheck = chProxyDataCheck;
		}

		public Number getProxyDataInterval() {
			return nProxyDataInterval;
		}

		public void setProxyDataInterval(Number nProxyDataInterval) {
			this.nProxyDataInterval = nProxyDataInterval;
		}

		public Number getProxyDataGrace() {
			return nProxyDataGrace;
		}

		public void setProxyDataGrace(Number nProxyDataGrace) {
			this.nProxyDataGrace = nProxyDataGrace;
		}

		public String getProxyDataWebOpts() {
			return szProxyDataWebOpts;
		}

		public void setProxyDataWebOpts(String szProxyDataWebOpts) {
			this.szProxyDataWebOpts = szProxyDataWebOpts;
		}

		public String getSubjectServer() {
			return szSubjectServer;
		}

		public void setSubjectServer(String szSubjectServer) {
			this.szSubjectServer = szSubjectServer;
		}

		public String getCluster() {
			return szCluster;
		}

		public void setCluster(String szCluster) {
			this.szCluster = szCluster;
		}

		public String getDBFile() {
			return szDBFile;
		}

		public void setDBFile(String szDBFile) {
			this.szDBFile = szDBFile;
		}

		public String getNewGroup() {
			return szNewGroup;
		}

		public void setNewGroup(String szNewGroup) {
			this.szNewGroup = szNewGroup;
		}

		public String getCanonAdminpServer() {
			return szCanonAdminpServer;
		}

		public void setCanonAdminpServer(String szCanonAdminpServer) {
			this.szCanonAdminpServer = szCanonAdminpServer;
		}

		public JNADatabase getDbNAB() {
			return dbNAB;
		}

		public void setDbNAB(JNADatabase dbhNAB) {
			this.dbNAB = dbhNAB;
		}

		public Document getNABDoc() {
			return docNAB;
		}

		public void setNABDoc(Document hNABNote) {
			this.docNAB = hNABNote;
		}

	}

	private int doAdminWorkItem(AdminWorkItemCtx data) {
		String szAdminpAction;
		String szAdminpDocServerItem;
		String szTypeField;
		String szNameType;

		switch (data.getMethod()) {
		case SETUSERPASSWORDSETTINGS:
			szAdminpAction = NotesConstants.adminpSetPasswordFields;
			szAdminpDocServerItem = "*"; //$NON-NLS-1$
			szTypeField = NotesConstants.ADMINP_PERSON;
			szNameType = NotesConstants.ADMINP_FULLNAME;

			break;
		case ADDSERVERTOCLUSTER:
			szAdminpAction = NotesConstants.adminpServerClusterAdd;
			szAdminpDocServerItem = "*"; //$NON-NLS-1$
			szTypeField = NotesConstants.ADMINP_SERVER;
			szNameType = NotesConstants.ADMINP_SERVERNAME;

			break;
		case REMOVESERVERFROMCLUSTER:
			szAdminpAction = NotesConstants.adminpServerClusterRemove;
			szAdminpDocServerItem = "*"; //$NON-NLS-1$
			szTypeField = NotesConstants.ADMINP_SERVER;
			szNameType = NotesConstants.ADMINP_SERVERNAME;
			break;
		case SETSERVERDIRECTORYASSISTANCESETTINGS:
			szAdminpAction = NotesConstants.adminpSetMABField;
			szAdminpDocServerItem = "*"; //$NON-NLS-1$
			szTypeField = NotesConstants.ADMINP_SERVER;
			szNameType = NotesConstants.ADMINP_SERVERNAME;
			break;
		case RENAMEGROUP:
			szAdminpAction = NotesConstants.adminpRenameGroupInNAB;
			szAdminpDocServerItem = "*"; //$NON-NLS-1$
			szTypeField = NotesConstants.ADMINP_GROUP;
			szNameType = NotesConstants.ADMINP_LISTNAME;
			break;
		default:
			throw new IllegalArgumentException(MessageFormat.format("Unsupported method: {0}", data.getMethod().toString()));
		}

		JNADatabase dbProxy = openProxyDB();
		JNADocument doc = (JNADocument) dbProxy.createDocument();
		doc.replaceItemValue(NotesConstants.FIELD_FORM, NotesConstants.ADMINP_PROXY_DOCUMENT);
		doc.replaceItemValue(NotesConstants.FIELD_TYPE_TYPE, EnumSet.of(ItemFlag.SUMMARY, ItemFlag.PROTECTED),
				NotesConstants.ADMINP_PROXY_DOCUMENT);
		doc.replaceItemValue(NotesConstants.ADMINP_ACTION_ITEM, EnumSet.of(ItemFlag.SUMMARY, ItemFlag.PROTECTED,
				ItemFlag.SIGNED), szAdminpAction);
		doc.replaceItemValue(NotesConstants.ADMINP_DOC_PROCESS_ITEM, EnumSet.of(ItemFlag.SUMMARY, ItemFlag.PROTECTED,
				ItemFlag.SIGNED), NotesConstants.ADMINP);
		doc.replaceItemValue(NotesConstants.ADMINP_DOC_SERVER_ITEM, EnumSet.of(ItemFlag.SUMMARY, ItemFlag.NAMES,
				ItemFlag.SIGNED), szAdminpDocServerItem);
		doc.replaceItemValue(NotesConstants.ADMINP_DOC_AUTHOR_ITEM, EnumSet.of(ItemFlag.SUMMARY, ItemFlag.NAMES,
				ItemFlag.SIGNED), getParentDominoClient().getEffectiveUserName());

		switch (data.getMethod()) {
		case SETUSERPASSWORDSETTINGS:
			doc.replaceItemValue(NotesConstants.ADMINP_DOC_PASS_CHECK_PASSWORD_ITEM, EnumSet.of(ItemFlag.SUMMARY,
					ItemFlag.SIGNED), data.getProxyDataCheck());
			doc.replaceItemValue(NotesConstants.ADMINP_DOC_PASS_GRACE_PERIOD_ITEM, EnumSet.of(ItemFlag.SUMMARY,
					ItemFlag.SIGNED), data.getProxyDataGrace());
			doc.replaceItemValue(NotesConstants.ADMINP_DOC_PASS_CHANGE_INTERVAL_ITEM, EnumSet.of(ItemFlag.SUMMARY,
					ItemFlag.SIGNED), data.getProxyDataInterval());
			doc.replaceItemValue(NotesConstants.ADMINP_DOC_PASS_HTTP_OPTIONS_ITEM, EnumSet.of(ItemFlag.SUMMARY,
					ItemFlag.SIGNED), data.getProxyDataWebOpts());
			break;

		case ADDSERVERTOCLUSTER:
			doc.replaceItemValue(NotesConstants.ADMINP_DOC_CLUSTER_NAME_ITEM, EnumSet.of(ItemFlag.SUMMARY, ItemFlag.NAMES,
					ItemFlag.SIGNED), data.getCluster());
			DominoDateTime repid = JNADominoDateTime.createMinimumDateTime();
			doc.replaceItemValue(NotesConstants.ADMINP_DOC_REPLICA_ID_ITEM, EnumSet.of(ItemFlag.SUMMARY, ItemFlag.SIGNED),
					repid);
			break;

		case SETSERVERDIRECTORYASSISTANCESETTINGS:
			doc.replaceItemValue(NotesConstants.ADMINP_DOC_DATABASE_PATH_ITEM, EnumSet.of(ItemFlag.SUMMARY, ItemFlag.SIGNED),
					data.getDBFile());
			break;

		case RENAMEGROUP:
			doc.replaceItemValue(NotesConstants.ADMINP_NEW_GROUP_NAME_ITEM, EnumSet.of(ItemFlag.SUMMARY, ItemFlag.SIGNED),
					data.getNewGroup());
			break;
		case REMOVESERVERFROMCLUSTER:
		default:
			//
			break;
		}

		adminpSetDirectoryInfo(doc, data.getDbNAB(), data.getCanonAdminpServer(), data.getNABDoc());

		String chBuf = data.getNABDoc().get(szNameType, String.class, ""); //$NON-NLS-1$
		if (StringUtil.isEmpty(chBuf)) {
			throw new DominoException(MessageFormat.format("Missing required item value {0} in NAB document with UNID {1}", szNameType, data.getNABDoc().getUNID()));
		}

		doc.replaceItemValue(NotesConstants.ADMINP_NAME_LIST_ITEM, EnumSet.of(ItemFlag.SUMMARY, ItemFlag.NAMES,
				ItemFlag.SIGNED), chBuf);

		this.adminpInitlOriginalRequest(dbProxy, /* Proxy Database */
				doc, /* New Proxy Request */
				null, /* parent request, or Response */
				null /* NAB object handle (likely person doc) */
				);

		JNADocumentAllocations docAllocations = (JNADocumentAllocations) doc.getAdapter(APIObjectAllocations.class);
		
		LockUtil.lockHandle(docAllocations.getNoteHandle(), (hDocByVal) -> {
			short result = NotesCAPI.get().NSFNoteExpand(hDocByVal);
			NotesErrorUtils.checkResult(result);

			 /* Sign the new note */
			result = NotesCAPI.get().NSFNoteSignUsingCtx(hDocByVal, null, null, (short) 0xffff, null);
			NotesErrorUtils.checkResult(result);

			result = NotesCAPI.get().NSFNoteContract(hDocByVal);
			NotesErrorUtils.checkResult(result);

			return 0;
		});
		
		doc.save();
		int noteId = doc.getNoteID();
		doc.autoClosable().close();
		
		return noteId;
	}

	/**
	 * Routine to put tag items into the Adminp requests which will be used later to
	 * track groups of cascaded requests.<br>
	 * <br>
	 * Want to save away:<br>
	 * <br>
	 * The original Author ID (ADMINP_ORIGINATING_REQUEST_AUTHORID)<br>
	 * The UNID of a newly created composite note (ADMINP_ORIGINATING_UNID)<br>
	 * The Internet Domain of the user affected by this call (ADMINP_ORIGINATING_REQUEST_INTERNET_DOMAIN)<br>
	 *		Get it from (LOCATION_IMAIL_INTERNET_DOMAIN)<br>
	 * The ONBEHALFOF item used so agents will work properly. ( ASSIST_ONBEHALFOF )<br>
	 * The Fullname item ( ADMINP_ORIGINATING_REQUEST_FULLNAME )<br>
	 * The organization item ( ADMINP_ORIGINATING_REQUEST_ORG )<br>
	 * 
	 * @param dbProxy Proxy Database
	 * @param docProxy the new request
	 * @param docOld parent request if available
	 * @param docNAB NAB object if available
	 */
	private void adminpInitlOriginalRequest(JNADatabase dbProxy, JNADocument docProxy,
			JNADocument docOld, JNADocument docNAB) {

		boolean bOriginatingRequest = false;


		if (docProxy==null) {
			return;
		}

		/*
		 ** Lets check to see if we even need to get
		 ** information here.  We might be working on 
		 ** a response to a document, a parent request,
		 ** or a request generated by either the client, or
		 ** remote procedure call.
		 */

		if (docOld!=null && /* was a parent note provided? */
				docOld.hasItem(NotesConstants.ADMINP_ORIGINATING_UNID) /* ... and did it have an UNID? */
				) {
			Optional<Item> origUnidItem = docOld.getFirstItem(NotesConstants.ADMINP_ORIGINATING_UNID);
			if (!origUnidItem.isPresent()) {
				throw new IllegalStateException(MessageFormat.format("Required item {0} could not be found", NotesConstants.ADMINP_ORIGINATING_UNID));
			}

			/*
			 ** Then copy it
			 ** to the new request
			 */
			origUnidItem.get().copyToDocument(docProxy, false);
		}
		else {
			/*
			 ** Indicate that this is the originating request
			 ** so that we might check later
			 */
			bOriginatingRequest = true;

			/*
			 ** Get the UNID for the new proxy note ...
			 */
			String docProxyUnidStr = docProxy.getUNID();

			/*
			 ** Make sure we don't duplicate the Item.
			 */
			docProxy.removeItem(NotesConstants.ADMINP_ORIGINATING_UNID);
			/* 
			 ** Store away the UNID of the composite note
			 */
			DominoUniversalNoteId docProxyUnid = getParentDominoClient().createUNID(docProxyUnidStr);
			docProxy.replaceItemValue(NotesConstants.ADMINP_ORIGINATING_UNID,
					EnumSet.of(ItemFlag.SUMMARY),
					docProxyUnid);

		}

		if (docOld!=null /* is this a cascading note ... */
				&& docOld.hasItem(NotesConstants.ADMINP_ORIGINATING_REQUEST_AUTHORID) /* and is there an Author ID item ? */
				) {
			Optional<Item> authorIdItem = docOld.getFirstItem(NotesConstants.ADMINP_ORIGINATING_REQUEST_AUTHORID);
			if (!authorIdItem.isPresent()) {
				throw new IllegalStateException(MessageFormat.format("Required item {0} could not be found", NotesConstants.ADMINP_ORIGINATING_REQUEST_AUTHORID));
			}

			/*
			 ** then copy it over to the new request
			 */
			authorIdItem.get().copyToDocument(docProxy, false);
		}
		else {
			/*
			 ** Make sure we don't duplicate the Item.
			 */
			docProxy.removeItem(NotesConstants.ADMINP_ORIGINATING_REQUEST_AUTHORID);
			String idUserName = getParentDominoClient().getIDUserName();
			docProxy.replaceItemValue(NotesConstants.ADMINP_ORIGINATING_REQUEST_AUTHORID,
					EnumSet.of(ItemFlag.SIGNED, ItemFlag.READWRITERS, ItemFlag.SUMMARY), idUserName);

		}

		if (docOld!=null && /* is this a cascading note ... */
				docOld.hasItem(NotesConstants.ADMINP_ORIGINATING_TIMEDATE_ITEM) /* and is there an timedate item ? */
				) {
			Optional<Item> origTimeDateItem = docOld.getFirstItem(NotesConstants.ADMINP_ORIGINATING_TIMEDATE_ITEM);
			if (!origTimeDateItem.isPresent()) {
				throw new IllegalStateException(MessageFormat.format("Required item {0} could not be found", NotesConstants.ADMINP_ORIGINATING_TIMEDATE_ITEM));
			}

			/*
			 ** then copy it over to the new request
			 */
			origTimeDateItem.get().copyToDocument(docProxy, false);
		}
		else {
			JNADominoDateTime tdCurrent = JNADominoDateTime.from(Instant.now());

			/*
			 ** Make sure we don't duplicate the Item.
			 */
			docProxy.removeItem(NotesConstants.ADMINP_ORIGINATING_TIMEDATE_ITEM);
			docProxy.replaceItemValue(NotesConstants.ADMINP_ORIGINATING_TIMEDATE_ITEM,
					EnumSet.of(ItemFlag.SUMMARY, ItemFlag.SIGNED), tdCurrent);
		}

		/*
		 ** Secondary directory requests field check ...
		 */
		if (docOld!=null && /* is this a cascading request ... */
				docOld.hasItem(NotesConstants.ADMINP_DOC_DIRECTORY_SERVER_NAME_ITEM) /* and was there a directory server name */
				) {
			Optional<Item> docDirServerNameItem = docOld.getFirstItem(NotesConstants.ADMINP_DOC_DIRECTORY_SERVER_NAME_ITEM);
			if (!docDirServerNameItem.isPresent()) {
				throw new IllegalStateException(MessageFormat.format("Required item {0} could not be found", NotesConstants.ADMINP_DOC_DIRECTORY_SERVER_NAME_ITEM));
			}

			docDirServerNameItem.get().copyToDocument(docProxy, false);
		}

		if (docOld!=null &&
				docOld.hasItem(NotesConstants.ADMINP_DOC_SECNAB_PATH_ITEM) /* check for the directory path */
				) {
			Optional<Item> docSecNABPathItem = docOld.getFirstItem(NotesConstants.ADMINP_DOC_SECNAB_PATH_ITEM);
			if (!docSecNABPathItem.isPresent()) {
				throw new IllegalStateException(MessageFormat.format("Required item {0} could not be found", NotesConstants.ADMINP_DOC_SECNAB_PATH_ITEM));
			}

			docSecNABPathItem.get().copyToDocument(docProxy, false);
		}

		if (docOld!=null &&
				docOld.hasItem(NotesConstants.ADMINP_DOC_SECNAB_NAME_ITEM) /* and the directory name */
				) {
			Optional<Item> docSecNABNameItem = docOld.getFirstItem(NotesConstants.ADMINP_DOC_SECNAB_NAME_ITEM);
			if (!docSecNABNameItem.isPresent()) {
				throw new IllegalStateException(MessageFormat.format("Required item {0} could not be found", NotesConstants.ADMINP_DOC_SECNAB_NAME_ITEM));
			}

			docSecNABNameItem.get().copyToDocument(docProxy, false);
		}

		if (docOld!=null &&
				docOld.hasItem(NotesConstants.ADMINP_DOC_DIRECTORY_REPLICA_ID_ITEM) /* and the directory replica id */
				) {
			Optional<Item> docDirReplicaIdItem = docOld.getFirstItem(NotesConstants.ADMINP_DOC_DIRECTORY_REPLICA_ID_ITEM);
			if (!docDirReplicaIdItem.isPresent()) {
				throw new IllegalStateException(MessageFormat.format("Required item {0} could not be found", NotesConstants.ADMINP_DOC_DIRECTORY_REPLICA_ID_ITEM));
			}

			docDirReplicaIdItem.get().copyToDocument(docProxy, false);				
		}

		if (docOld!=null &&
				docOld.hasItem(NotesConstants.ADMINP_DOC_DIRECTORY_NOTE_UNID_ITEM)) {

			Optional<Item> docDirNoteUnidItem = docOld.getFirstItem(NotesConstants.ADMINP_DOC_DIRECTORY_NOTE_UNID_ITEM);
			if (!docDirNoteUnidItem.isPresent()) {
				throw new IllegalStateException(MessageFormat.format("Required item {0} could not be found", NotesConstants.ADMINP_DOC_DIRECTORY_NOTE_UNID_ITEM));
			}

			docDirNoteUnidItem.get().copyToDocument(docProxy, false);
		}

		/*
		 ** On Behalf of field check ...
		 */
		if (docOld!=null && docOld.hasItem(NotesConstants.ASSIST_ONBEHALFOF)) {
			Optional<Item> onBehalfItem = docOld.getFirstItem(NotesConstants.ASSIST_ONBEHALFOF);
			if (!onBehalfItem.isPresent()) {
				throw new IllegalStateException(MessageFormat.format("Required item {0} could not be found", NotesConstants.ASSIST_ONBEHALFOF));
			}

			onBehalfItem.get().copyToDocument(docProxy, false);
		}
		else {
			String idUserName = getParentDominoClient().getEffectiveUserName();

			/*
			 ** Make sure we don't duplicate the Item.
			 */
			docProxy.removeItem(NotesConstants.ASSIST_ONBEHALFOF);
			docProxy.replaceItemValue(NotesConstants.ASSIST_ONBEHALFOF,
					EnumSet.of(ItemFlag.SIGNED, ItemFlag.READWRITERS, ItemFlag.SUMMARY),
					idUserName);
		}

		/*
		 ** On request fullname field check ...
		 */
		if (docOld!=null &&
				docOld.hasItem(NotesConstants.ADMINP_ORIGINATING_REQUEST_FULLNAME)) {

			Optional<Item> fullnameItem = docOld.getFirstItem(NotesConstants.ADMINP_ORIGINATING_REQUEST_FULLNAME);
			if (!fullnameItem.isPresent()) {
				throw new IllegalStateException(MessageFormat.format("Required item {0} could not be found", NotesConstants.ADMINP_ORIGINATING_REQUEST_FULLNAME));
			}

			fullnameItem.get().copyToDocument(docProxy, false);
		}
		else {
			/*
			 ** Try to get the fullname
			 */
			String authorName = docProxy.get(NotesConstants.ADMINP_DOC_AUTHOR_ITEM, String.class, ""); //$NON-NLS-1$

			if (StringUtil.isNotEmpty(authorName)) {
				docProxy.removeItem(NotesConstants.ADMINP_ORIGINATING_REQUEST_FULLNAME);
				docProxy.replaceItemValue(NotesConstants.ADMINP_ORIGINATING_REQUEST_FULLNAME,
						EnumSet.of(ItemFlag.SIGNED, ItemFlag.READWRITERS, ItemFlag.SUMMARY),
						authorName);

			}
		}

		/*
		 ** On request Organization field check ...
		 */
		if (docOld!=null && docOld.hasItem(NotesConstants.ADMINP_ORIGINATING_REQUEST_ORG)) {
			Optional<Item> origReqOrgItem = docOld.getFirstItem(NotesConstants.ADMINP_ORIGINATING_REQUEST_ORG);
			if (!origReqOrgItem.isPresent()) {
				throw new IllegalStateException(MessageFormat.format("Required item {0} could not be found", NotesConstants.ADMINP_ORIGINATING_REQUEST_ORG));
			}

			origReqOrgItem.get().copyToDocument(docProxy, false);
		}
		else {
			/*
			 ** Try to get the Organization
			 */
			String org = this.adminpGetRequestOrg(docProxy);

			if (StringUtil.isNotEmpty(org)) {
				docProxy.removeItem(NotesConstants.ADMINP_ORIGINATING_REQUEST_ORG);
				docProxy.replaceItemValue(NotesConstants.ADMINP_ORIGINATING_REQUEST_ORG,
						EnumSet.of(ItemFlag.SIGNED, ItemFlag.READWRITERS, ItemFlag.SUMMARY),
						org);
			}
		}

		/*
		 ** Internet domain check.
		 */
		if (docOld!=null && docOld.hasItem(NotesConstants.ADMINP_ORIGINATING_REQUEST_INTERNET_DOMAIN)) {
			Optional<Item> origReqInternetDomainItem = docOld.getFirstItem(NotesConstants.ADMINP_ORIGINATING_REQUEST_INTERNET_DOMAIN);
			if (!origReqInternetDomainItem.isPresent()) {
				throw new IllegalStateException(MessageFormat.format("Required item {0} could not be found", NotesConstants.ADMINP_ORIGINATING_REQUEST_INTERNET_DOMAIN));
			}

			origReqInternetDomainItem.get().copyToDocument(docProxy, false);
		}
		else {
			if (docNAB!=null && docNAB.hasItem(NotesConstants.LOCATION_IMAIL_INTERNET_DOMAIN)) {
				docProxy.removeItem(NotesConstants.ADMINP_ORIGINATING_REQUEST_INTERNET_DOMAIN);
				String internetDomain = docNAB.get(NotesConstants.ADMINP_ORIGINATING_REQUEST_INTERNET_DOMAIN,
						String.class, ""); //$NON-NLS-1$
				docProxy.replaceItemValue(NotesConstants.ADMINP_ORIGINATING_REQUEST_INTERNET_DOMAIN,
						EnumSet.of(ItemFlag.SIGNED, ItemFlag.READWRITERS, ItemFlag.SUMMARY), internetDomain);

			}
		}

		if (bOriginatingRequest) {
			/*
			 ** Try to get the request value out of the request
			 */
			String proxyAction = docProxy.get(NotesConstants.ADMINP_ACTION_ITEM, String.class, ""); //$NON-NLS-1$
			if (StringUtil.isEmpty(proxyAction)) {
				/*
				 ** The length of the proxy action was 0, it likely
				 ** wasn't stored yet.
				 **
				 ** Use the one provided ... for now, simply exit
				 */
				return;
			}

			this.adminpBuildXpectAndPossiblLists(dbProxy, docProxy, proxyAction);
		}
		else {
			/*
			 ** we are working on a cascaded request.
			 ** we should check to see if nopurge should be set
			 ** and set it if required.
			 */
			this.adminpSetCascadedNoPurge (dbProxy, docProxy);
		}
	}

	/**
	 * Routine to return the organization information for the request
	 * the organization name will be based on the target item name if
	 * one is found.  Otherwise, it will be based on the organization
	 * of the requestor ( Originating Author )<br>
	 * <br>
	 * 	If neither is found, then no organization will be returned.
	 * 
	 * @param docProxy new doc
	 * @return organization
	 */
	private String adminpGetRequestOrg(JNADocument docProxy) {
		/*
		** make sure the parameters provided are ok
		*/
		if (docProxy==null) {
			return ""; //$NON-NLS-1$
		}
		
		/*
		** Look in the ProxyNameList for a name with an organization
		*/
		String nameStr = docProxy.get(NotesConstants.ADMINP_NAME_LIST_ITEM, String.class, ""); //$NON-NLS-1$
		if (StringUtil.isNotEmpty(nameStr)) {
			Name name = Names.createName(nameStr);
			
			String org = name.getOrganisation();
			if (StringUtil.isNotEmpty(org)) {
				/*
				** we appear to have an organization
				*/
				return org;
			}
		}

		/*
		** If we got here, then we are still looking for a valid
		** organization.  check the Originating Author
		*/
		String authorId = docProxy.get(NotesConstants.ADMINP_ORIGINATING_REQUEST_AUTHORID, String.class, "");
		if (StringUtil.isNotEmpty(authorId)) {
			Name name = Names.createName(authorId);
			String org = name.getOrganisation();
			if (StringUtil.isNotEmpty(org)) {
				/*
				** we appear to have an organization
				*/
				return org;
			}
		}
		
		return ""; //$NON-NLS-1$
	}
	
	private void adminpBuildXpectAndPossiblLists(JNADatabase dbProxy, JNADocument docProxy,
			String proxyAction) {
		
		/*
		** check to see if we should even bother to
		** build these lists based on the existence of the
		** clean up view.
		*/
		int viewNoteId = dbProxy.findCollectionId(NotesConstants.ADMINP_ORIG_UNID_VIEW, CollectionType.View);
		
		int iProxyAction = 0;
		
		if (viewNoteId!=0) {
			/*
			** if we find the view, then we can initialize for the switch statement.
			*/
			iProxyAction = Integer.parseInt(proxyAction);
		}
		
		boolean bSetNoPurge = false;
		String[] lpchXpectAndPossListPtr = null;
		String[] lpchCommonXListPtr = null;
		
		switch (iProxyAction) {
		case NotesConstants.AdminpCheckMailServersAccessWord:
			/*
			 ** Indicates the beginning of a Request to move a mail file
			 */
			bSetNoPurge = true;
			lpchXpectAndPossListPtr = NotesConstants.lpchMoveMailFile;
			lpchCommonXListPtr = null;

			break;
		case NotesConstants.AdminpCheckRoamingServerAccessWord:
			/*
			 ** Indicates the beginning of a Request to move Roaming files
			 */
			bSetNoPurge = true;
			lpchXpectAndPossListPtr = NotesConstants.lpchMoveRoamingFiles;
			lpchCommonXListPtr = null;
			break;

		case NotesConstants.AdminpDeletePersonInNABWord:
			/*
			 ** Indicates the beginning of a Request to Delete a User
			 */
			bSetNoPurge = true;
			lpchXpectAndPossListPtr = NotesConstants.lpchDeleteUser;
			/*
			 ** Only want to include this part of the list
			 ** if a user is being deleted, and there are
			 ** indications in the request that a mail file
			 ** exists.
			 */
			if (!docProxy.hasItem(NotesConstants.ADMINP_DOC_DELETE_MAIL_FILE_ITEM)) {
				lpchCommonXListPtr = NotesConstants.lpchCommonXNoFilesSupplied;
			}
			else {
				lpchCommonXListPtr = NotesConstants.lpchCommonXForDeleteUser;
			}

			break;

		case NotesConstants.AdminpRenameCommonNameUserInNABWord:
			/*
			 ** Indicate the beginning of a Request to Rename a User
			 */
			bSetNoPurge = true;
			lpchXpectAndPossListPtr = NotesConstants.lpchRenameCommonNameUser;
			lpchCommonXListPtr = NotesConstants.lpchCommonXForRename;

			break;

		case NotesConstants.AdminpInitiateNABChangeWord:
			/*
			 ** Indicate the beginning of a Request to Rename a User
			 */
			bSetNoPurge = true;
			lpchXpectAndPossListPtr = NotesConstants.lpchRenameUser;
			lpchCommonXListPtr = NotesConstants.lpchCommonXForRename;

			break;

		case NotesConstants.AdminpMoveUserInHierWord:
			/*
			Indicate the beginning of a request to move to a new certifier
			 */
			bSetNoPurge = true;
			lpchXpectAndPossListPtr = NotesConstants.lpchMoveCertifier;
			lpchCommonXListPtr = NotesConstants.lpchCommonXForRename;

			break;

		case NotesConstants.AdminpDeleteInTheACLWord:
			/*
			 ** Indicate the beginning of a request to delete a user
			 ** or group (Immediate)
			 */
			bSetNoPurge = true;
			lpchXpectAndPossListPtr = NotesConstants.lpchDeleteUserImm;
			/*
			 ** Only want to include this part of the list
			 ** if a user is being deleted, and there are
			 ** indications in the request that a mail file
			 ** exists.
			 */
			if (!docProxy.hasItem(NotesConstants.ADMINP_DOC_DELETE_MAIL_FILE_ITEM)) {
				lpchCommonXListPtr = NotesConstants.lpchCommonXNoFilesSupplied;
			}
			else {
				lpchCommonXListPtr = NotesConstants.lpchCommonXForDeleteUser;
			}

			break;

		case NotesConstants.AdminpDeleteGroupInNABWord:
			/*
			 ** Indicate the beginning of a request to delete a group
			 ** in the secondary address book (deferred)
			 */
			bSetNoPurge = true;
			lpchXpectAndPossListPtr = NotesConstants.lpchDeleteGroupNab;
			lpchCommonXListPtr = NotesConstants.lpchCommonXNoFilesSupplied;

			break;

		case NotesConstants.AdminpRenameGroupInNABWord:
			/*
			 ** Indicate the beginning of a request to rename a group
			 ** in the primary address book (deferred)
			 */
			bSetNoPurge = true;
			lpchXpectAndPossListPtr = NotesConstants.lpchRenameGroup;
			lpchCommonXListPtr = NotesConstants.lpchCommonXForRenameGroup;

			break;

		case NotesConstants.AdminpCreateRoamingReplStubsWord:
			/*
			 ** Indicates the beginning of a Request to Upgrade a non-roaming
			 ** user to a roaming state.
			 */
			bSetNoPurge = true;
			lpchXpectAndPossListPtr = NotesConstants.lpchUpgradeToRoamn;
			lpchCommonXListPtr = null;

			break;

			/*
			 ** Requests which do not have cascading requests associated with them
			 */
		case NotesConstants.AdminpCreateMailFileWord:
			/*
			 ** Nothing to do but get out
			 */
			lpchXpectAndPossListPtr = null;
			lpchCommonXListPtr = null;

			break;
		case 0:
		default:
			/*
			 ** nothing to do because the admin4 database isn't
			 ** up to date.
			 */
			break;
		}

		/*
		 ** If we have a real list of expected and possible requests,
		 ** then we can build a text list for the originating request
		 */
		if (lpchXpectAndPossListPtr!=null) {
			/*
			 ** Loop through the array pointer provided
			 ** to build the lists
			 */
			boolean bPossList = false;
			
			for (String lpchProxyAction : lpchXpectAndPossListPtr) {

				if (NotesConstants.ListSeperator.equals(lpchProxyAction)) {
					bPossList = true;
					continue;
				}
				
				if (bPossList) {
					docProxy.appendToTextList(NotesConstants.ADMINP_ORIGINATING_REQUEST_POSSIBLE_LIST,
							lpchProxyAction, false);
				}
				else {
					docProxy.appendToTextList(NotesConstants.ADMINP_ORIGINATING_REQUEST_EXPECTED_LIST,
							lpchProxyAction, false);
				}
				
			}

		}
		
		/*
		 ** check to see if we have common stuff to add to the lists
		 */
		if (lpchCommonXListPtr!=null) {
			boolean bPossList = false;

			for (String lpchProxyAction : lpchCommonXListPtr) {
				if (NotesConstants.ListSeperator.equals(lpchProxyAction)) {
					bPossList = true;
					continue;
				}

				if (bPossList) {
					docProxy.appendToTextList(NotesConstants.ADMINP_ORIGINATING_REQUEST_POSSIBLE_LIST, lpchProxyAction, false);
				}
				else {
					docProxy.appendToTextList(NotesConstants.ADMINP_ORIGINATING_REQUEST_EXPECTED_LIST, lpchProxyAction, false);
				}
			}
		}

		/*
		 ** check to see if we should indicate that this request shouldn't
		 ** be purged.
		 */
		if (bSetNoPurge) {
			docProxy.replaceItemValue(NotesConstants.FIELD_NOPURGE, NotesConstants.ADMINP_ORIG_REQ_CASCADE);
		}

	
	}

	/**
	 * Routine to check to see if the request being accessed is
	 * cascaded and requires that a NoPurge field be set.
	 * if both of these parameters are true, then the nopurge
	 * field will be set on the request handle provided.
	 * Otherwize nothing will be done and the function
	 * will return.
	 * 
	 * @param dbProxy proxy db
	 * @param docRequest proxy doc
	 */
	public void adminpSetCascadedNoPurge(JNADatabase dbProxy, JNADocument docRequest) {
		/*
		** Check for valid data
		*/
		if (dbProxy==null || docRequest==null) {
			return;
		}
		
		Optional<Document> optionalDocOrig = this.adminpOpenOriginatingReq(dbProxy, docRequest);

		/*
		** go to the originating note ...
		** check to see if it includes the
		** list of expected requests.
		*/
		if (optionalDocOrig.isPresent()) {
			Document docOrig = optionalDocOrig.get();
			
			if (docOrig.hasItem(NotesConstants.ADMINP_ORIGINATING_REQUEST_EXPECTED_LIST)) {
				/*
				** indicate that this request shouldn't be purged.
				*/
				docRequest.replaceItemValue(NotesConstants.FIELD_NOPURGE, NotesConstants.ADMINP_ORIG_REQ_CASCADE);
			}
			
			docOrig.autoClosable().close();
		}

	}

	/*
	** Routine to open the originating request of a cascaded 
	** request.  If no originating request if found, then return
	** with a null handle.
	*/

	/**
	 * Routine to open the originating request of a cascaded 
	 * request.  If no originating request if found, then return
	 * with a null handle.
	 * 
	 * @param dbProxy proxy db
	 * @param docRequest request doc
	 * @return
	 */
	private Optional<Document> adminpOpenOriginatingReq(JNADatabase dbProxy, JNADocument docRequest) {
		/*
		** Get the UNID from the adminp request provided
		*/
		if (!docRequest.hasItem(NotesConstants.ADMINP_ORIGINATING_UNID)) {
			return Optional.empty();
		}
		
		String unid = docRequest.get(NotesConstants.ADMINP_ORIGINATING_UNID, String.class, "");
		if (StringUtil.isEmpty(unid)) {
			return Optional.empty();
		}
		
		return dbProxy.getDocumentByUNID(unid);
	}

	private void adminpSetDirectoryInfo(JNADocument newRequest, JNADatabase dbDir, String serverName,
			Document docNAB) {

		JNADatabase dbDirLocal = null;
		
		String dirEntryUNID = null;
		String dirDomain = null;
		String dirEntryID = null;
		
		/* for virtual NOTEHANDLES get the backing Domino Directory */
		if (docNAB!=null && docNAB.hasItem(NotesConstants.ITEM_DIRENTRYID)) {
			dirEntryID = docNAB.get(NotesConstants.ITEM_DIRENTRYID, String.class, ""); //$NON-NLS-1$
			dirDomain = docNAB.get(NotesConstants.ITEM_DIRDOMAIN, String.class, ""); //$NON-NLS-1$
			String dirEntryServer = docNAB.get(NotesConstants.ITEM_DIRSERVER, String.class, ""); //$NON-NLS-1$
			serverName = dirEntryServer;

			/* make the directory entry ID safe for persisting */
			Memory dirEntryIDMem = NotesStringUtils.toLMBCS(dirEntryID, true);
			short result = NotesCAPI.get().DirEntryIDTrim (dirEntryIDMem, NotesConstants.DIRENTRYIDTRIM_LOCAL);
			NotesErrorUtils.checkResult(result);
			dirEntryID = NotesStringUtils.fromLMBCS(dirEntryIDMem, -1);


			/* foreign directory entries don't have UNID's */
			if (NotesCAPI.get().DirEntryIDGetType (dirEntryIDMem) != NotesConstants.DIRENTRYID_TYPE_NOTES) {
				NotesUniversalNoteIdStruct unidStruct = NotesUniversalNoteIdStruct.newInstance();
				unidStruct.File.Innards = new int[] {0, 0};
				unidStruct.Note.Innards = new int[] {0, 0};
				dirEntryUNID = unidStruct.toString();
			}

			/* get the config portion (NSF) of the domain*/
			Memory dirEntryServerMem = NotesStringUtils.toLMBCS(dirEntryServer, true);
			Memory dirDomainMem = NotesStringUtils.toLMBCS(dirDomain, true);

			Memory retNABPath = new Memory(NotesConstants.MAXPATH);
			/* get the config portion (NSF) of the domain*/
			result = NotesCAPI.get().DirDomainGetInfo(dirEntryServerMem, dirDomainMem,
					NotesConstants.DIR_DOMAIN_INFO_TYPE_DIRECTORY_PATH, retNABPath);
			NotesErrorUtils.checkResult(result);

			String nabPath = NotesStringUtils.fromLMBCS(retNABPath, -1);
			dbDirLocal = (JNADatabase) getParentDominoClient().openDatabase(nabPath);
			dbDir = dbDirLocal;
		}


		if (dbDir!=null) {
			if (dbDir.getOption(DatabaseOption.IS_ADDRESS_BOOK)) {
				ShortByReference wEntryLen = new ShortByReference();
				ShortByReference wCount = new ShortByReference();
				DHANDLE.ByReference hReturn = DHANDLE.newInstanceByReference();

				DisposableMemory chPrimaryNamePtr = new DisposableMemory(NotesConstants.MAXPATH);

				DisposableMemory chDirectoryPathPtr = new DisposableMemory(NotesConstants.MAXPATH);
				DisposableMemory chDirectoryNamePtr = new DisposableMemory(NotesConstants.MAXPATH);
				
				try {
					Memory serverNameMem = NotesStringUtils.toLMBCS(serverName, true);
					short result = NotesCAPI.get().NAMEGetAddressBooks(serverNameMem,
							NotesConstants.NAME_GET_AB_FIRSTONLY,
							wCount, wEntryLen, hReturn);
					NotesErrorUtils.checkResult(result);

					if (wCount.getValue()!=0) {
						LockUtil.lockHandle(hReturn, (hReturnByVal) -> {
							return Mem.OSLockObject(hReturnByVal, (pszReturn) -> {
								short resultPathParse = NotesCAPI.get().OSPathNetParse(pszReturn, null,
										null, chPrimaryNamePtr);
								NotesErrorUtils.checkResult(resultPathParse);

								short wNameLen = NotesCAPI.get().Cstrlen(chPrimaryNamePtr);
								ShortByReference wTypePos = new ShortByReference();
								wTypePos.setValue(wNameLen);

								short resultFileType = NotesCAPI.get().OSPathFileType(chPrimaryNamePtr, wTypePos);
								NotesErrorUtils.checkResult(resultFileType);

								if (wNameLen == wTypePos.getValue()) {
									/* no file type specified */
									Memory dbTypeMem = NotesStringUtils.toLMBCS(NotesConstants.DBTYPE, true);
									NotesCAPI.get().Cstrncat(chPrimaryNamePtr, dbTypeMem, (NotesConstants.MAXPATH-1));
								}
								NotesCAPI.get().OSLocalizePath(chPrimaryNamePtr);

								return 0;
							});
						});
					}
				}
				finally {
					if (!hReturn.isNull()) {
						short result = LockUtil.lockHandle(hReturn, (hReturnByVal) -> {
							return Mem.OSMemFree(hReturnByVal);
						});
						NotesErrorUtils.checkResult(result);
					}
				}
				
				if (dbDir!=null) {
					JNADatabaseAllocations dbDirAllocations = (JNADatabaseAllocations) dbDir.getAdapter(APIObjectAllocations.class);
					HANDLE dbDirHandle = dbDirAllocations.getDBHandle();

					short result = LockUtil.lockHandle(dbDirHandle, (dbDirHandleByVal) -> {
						return NotesCAPI.get().NSFDbPathGet(dbDirHandleByVal, chDirectoryPathPtr, null);
					});
					NotesErrorUtils.checkResult(result);

					result = NotesCAPI.get().OSPathNetParse (chDirectoryPathPtr, null, null, chDirectoryNamePtr);
					NotesErrorUtils.checkResult(result);
					
					short wNameLen = NotesCAPI.get().Cstrlen(chDirectoryNamePtr);
					ShortByReference wTypePos = new ShortByReference();
					wTypePos.setValue(wNameLen);

					result = NotesCAPI.get().OSPathFileType(chDirectoryNamePtr, wTypePos);
					NotesErrorUtils.checkResult(result);

					if (wNameLen == wTypePos.getValue()) {
						/* no file type specified */
						Memory dbTypeMem = NotesStringUtils.toLMBCS(NotesConstants.DBTYPE, true);
						NotesCAPI.get().Cstrncat(chDirectoryNamePtr, dbTypeMem, (NotesConstants.MAXPATH-1));
					}
					NotesCAPI.get().OSLocalizePath(chDirectoryNamePtr);

					if (!NotesCAPI.get().IntlTextEqualCaseInsensitive(chPrimaryNamePtr, chDirectoryNamePtr,
							(short) 0xffff, false)) {
						
						String chDirectoryTitle = dbDir.getTitle();
						String chDirReplicaId = dbDir.getReplicaID();
						String chDirectoryAdminServer = dbDir.getACL().getAdminServer();

						newRequest.removeItem(NotesConstants.ADMINP_DOC_DIRECTORY_SERVER_NAME_ITEM);
						newRequest.replaceItemValue(NotesConstants.ADMINP_DOC_DIRECTORY_SERVER_NAME_ITEM,
								EnumSet.of(ItemFlag.PROTECTED, ItemFlag.SUMMARY, ItemFlag.SIGNED), chDirectoryAdminServer);
						
						newRequest.removeItem(NotesConstants.ADMINP_DOC_SECNAB_PATH_ITEM);
						String chDirectoryName = NotesStringUtils.fromLMBCS(chDirectoryNamePtr, -1);
						newRequest.replaceItemValue(NotesConstants.ADMINP_DOC_SECNAB_PATH_ITEM,
								EnumSet.of(ItemFlag.PROTECTED, ItemFlag.SUMMARY, ItemFlag.SIGNED),
								chDirectoryName);
						
						newRequest.removeItem(NotesConstants.ADMINP_DOC_SECNAB_NAME_ITEM);
						newRequest.replaceItemValue(NotesConstants.ADMINP_DOC_SECNAB_NAME_ITEM,
								EnumSet.of(ItemFlag.PROTECTED, ItemFlag.SUMMARY, ItemFlag.SIGNED),
								chDirectoryTitle);
						
						newRequest.removeItem(NotesConstants.ADMINP_DOC_DIRECTORY_REPLICA_ID_ITEM);
						JNADominoDateTime dirReplicaIdDateTime = new JNADominoDateTime(NotesStringUtils.replicaIdToInnards(chDirReplicaId));
						newRequest.replaceItemValue(NotesConstants.ADMINP_DOC_DIRECTORY_REPLICA_ID_ITEM,
								EnumSet.of(ItemFlag.PROTECTED, ItemFlag.SUMMARY, ItemFlag.SIGNED),
								dirReplicaIdDateTime);

						if (docNAB!=null) {
							/* Prepare to store away the UNID in a NOTEREF_LIST type. */
							if (dirEntryUNID==null) {
								dirEntryUNID = docNAB.getUNID();
							}

							newRequest.removeItem(NotesConstants.ADMINP_DOC_DIRECTORY_NOTE_UNID_ITEM);
							DominoUniversalNoteId dirEntryUNIDObj = new JNADominoUniversalNoteId(dirEntryUNID);
							newRequest.replaceItemValue(NotesConstants.ADMINP_DOC_DIRECTORY_NOTE_UNID_ITEM,
									EnumSet.of(ItemFlag.PROTECTED, ItemFlag.SUMMARY, ItemFlag.SIGNED),
									dirEntryUNIDObj);
							
						}
						
						if (StringUtil.isNotEmpty(dirDomain) && StringUtil.isNotEmpty(dirEntryID)) {
							newRequest.removeItem(NotesConstants.ADMINP_DOC_DIRECTORY_DOMAIN_ITEM);
							newRequest.replaceItemValue(NotesConstants.ADMINP_DOC_DIRECTORY_DOMAIN_ITEM,
									EnumSet.of(ItemFlag.PROTECTED, ItemFlag.SUMMARY, ItemFlag.SIGNED),
									dirDomain);
							
							newRequest.removeItem(NotesConstants.ADMINP_DOC_DIRECTORY_ENTRYID_ITEM);
							newRequest.replaceItemValue(NotesConstants.ADMINP_DOC_DIRECTORY_ENTRYID_ITEM,
									EnumSet.of(ItemFlag.PROTECTED, ItemFlag.SUMMARY, ItemFlag.SIGNED),
									dirEntryID);
						}
					}
				}
				
			}


		}
		
		if (dbDirLocal!=null) {
			dbDirLocal.close();
		}
	}

	@Override
	public int addServerToCluster(String server, String cluster) {
		checkDisposed();

		if (server == null) {
			throw new IllegalArgumentException("Server name cannot be null");
		}

		if (StringUtil.isEmpty(cluster)) {
			throw new IllegalArgumentException("Cluster name cannot be empty");
		}

		if (StringUtil.isEmpty(server)) {
			if (getParentDominoClient().isOnServer()) {
				server = getParentDominoClient().getIDUserName();
			}
			else {
				throw new IllegalArgumentException("Server name cannot be empty in a remote call");
			}
		}
		
		String szCanonSubjectServer = NotesNamingUtils.toCanonicalName(server);

		Optional<Document> nabDoc = getNABDoc(NotesConstants.SERVERNAMESSPACE, szCanonSubjectServer,
				AdminPOperation.ADDSERVERTOCLUSTER);
		if (!nabDoc.isPresent()) {
			throw new DominoException(MessageFormat.format("No server document found for: {0}", szCanonSubjectServer));
		}

		JNADatabase dbNAB = openNAB();

		// setup ctx and do proxy note creation
		AdminWorkItemCtx workCtx = new AdminWorkItemCtx();
		workCtx.setMethod(AdminPOperation.ADDSERVERTOCLUSTER);

		workCtx.setCanonAdminpServer(m_serverNameCanonical);
		workCtx.setDbNAB(dbNAB);
		workCtx.setNABDoc(nabDoc.get());
		workCtx.setSubjectServer(szCanonSubjectServer);
		workCtx.setCluster(cluster);
		
		int noteId = doAdminWorkItem(workCtx);
		
		nabDoc.get().autoClosable().close();

		return noteId;
	}

	@Override
	public int removeServerFromCluster(String server) {
		checkDisposed();

		if (server == null) {
			throw new IllegalArgumentException("Server name cannot be null");
		}

		if (StringUtil.isEmpty(server)) {
			if (getParentDominoClient().isOnServer()) {
				server = getParentDominoClient().getIDUserName();
			}
			else {
				throw new IllegalArgumentException("Server name cannot be empty in a remote call");
			}
		}
		
		String szCanonSubjectServer = NotesNamingUtils.toCanonicalName(server);

		Optional<Document> nabDoc = getNABDoc(NotesConstants.SERVERNAMESSPACE, szCanonSubjectServer,
				AdminPOperation.REMOVESERVERFROMCLUSTER);
		if (!nabDoc.isPresent()) {
			throw new DominoException(MessageFormat.format("No server document found for: {0}", szCanonSubjectServer));
		}

		JNADatabase dbNAB = openNAB();

		// setup ctx and do proxy note creation
		AdminWorkItemCtx workCtx = new AdminWorkItemCtx();
		workCtx.setMethod(AdminPOperation.REMOVESERVERFROMCLUSTER);

		workCtx.setCanonAdminpServer(m_serverNameCanonical);
		workCtx.setDbNAB(dbNAB);
		workCtx.setNABDoc(nabDoc.get());
		workCtx.setSubjectServer(szCanonSubjectServer);
		
		int noteId = doAdminWorkItem(workCtx);
		
		nabDoc.get().autoClosable().close();

		return noteId;
	}

	/**
	 * Tries to find the mailserver/filepath for the specified user
	 * 
	 * @param userName user
	 * @return
	 */
	private Optional<String[]> getMailServer(String userName) {
		checkDisposed();
		
		if (StringUtil.isEmpty(userName)) {
			throw new IllegalArgumentException("Username cannot be null");
		}
		
		String szCanonUserName = NotesNamingUtils.toCanonicalName(userName);
		
		HANDLE.ByReference dbhLocalNAB = HANDLE.newInstanceByReference();
		Database dbLocalNAB = null;
		
		AtomicBoolean bDisconnected = new AtomicBoolean();
		
		try {
			// check location doc for disconnected
			if (NotesCAPI.get().NetOpenLocDB(dbhLocalNAB) == INotesErrorConstants.NOERROR) {
				dbLocalNAB = new JNADatabase(getParentDominoClient(), dbhLocalNAB);
				
				IntByReference nidLoc = new IntByReference();

				if (NotesCAPI.get().NetGetCurrentLocNoteID(nidLoc) == INotesErrorConstants.NOERROR) {
					if (nidLoc.getValue() != 0) {
						Optional<Document> docLocation = dbLocalNAB.getDocumentById(nidLoc.getValue());
						if (docLocation.isPresent()) {
							String locationType = docLocation.get().get(NotesConstants.LOCATION_TYPE, String.class, ""); //$NON-NLS-1$
							if (NotesConstants.LOCTYPE_NONE.equals(locationType)) {
								bDisconnected.set(true);
							}
							docLocation.get().autoClosable().close();
						}
					}
				}
			}

			// try to get the proxy db handle, but
			// DWIN5CUQ68
			// we specifically don't care if the the db opened in this method
			// because AdminpCreateRequestExt() will do the right thing if the user doesn't have access
			if (!bDisconnected.get()) {
				try {
					openProxyDB();
				}
				catch (DominoException e) {
					bDisconnected.set(true);
				}
			}

			Optional<Document> docNAB = Optional.empty();

			// try to get NAB person doc
			if (!bDisconnected.get()) {
				/* pthn7s7mm9: rig to use enhanced user lookup DA & EDC aware for this method */
				try {
					docNAB = getNABDoc(NotesConstants.USERNAMESSPACE, szCanonUserName,
							AdminPOperation.CONFIGUREMAILAGENT);
				}
				catch (DominoException e) {
					bDisconnected.set(true);
				}

			}

			// get mailserver & file from person doc if connected 
			if (!bDisconnected.get()) {
				if (docNAB.isPresent()) {
					String mailServer = docNAB.get().get(NotesConstants.MAIL_MAILSERVER_ITEM, String.class, ""); //$NON-NLS-1$
					String mailFilePath = docNAB.get().get(NotesConstants.MAIL_MAILFILE_ITEM, String.class, ""); //$NON-NLS-1$
					return Optional.of(new String[] {mailServer, mailFilePath});
				}
				else {
					return Optional.empty();
				}
			}
			else {
				// but if disconnected, get the adminserver from the local mailfile
				if (dbLocalNAB!=null) {
					Acl acl = dbLocalNAB.getACL();
					String adminServer = acl.getAdminServer();
					
					if (!StringUtil.isEmpty(adminServer)) {
						return Optional.of(new String[] {adminServer, ""}); //$NON-NLS-1$
					}
				}

				return Optional.empty();
			}
		}
		finally {
			if (dbLocalNAB!=null) {
				dbLocalNAB.close();
			}
		}
	}
}
