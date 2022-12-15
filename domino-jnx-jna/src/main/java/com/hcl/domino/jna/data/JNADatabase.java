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
package com.hcl.domino.jna.data;

import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hcl.domino.BuildVersionInfo;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.DominoClient.IBreakHandler;
import com.hcl.domino.DominoClient.NotesReplicationStats;
import com.hcl.domino.DominoClient.OpenDatabase;
import com.hcl.domino.DominoClient.ReplicationStateListener;
import com.hcl.domino.DominoException;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.admin.replication.ReplicaInfo;
import com.hcl.domino.commons.constants.UpdateNote;
import com.hcl.domino.commons.data.AccessInfoImpl;
import com.hcl.domino.commons.data.BuildVersionInfoImpl;
import com.hcl.domino.commons.data.EncryptionInfoImpl;
import com.hcl.domino.commons.data.NSFVersionInfoImpl;
import com.hcl.domino.commons.design.DesignUtil;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.CAPIGarbageCollector;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.commons.util.StringTokenizerExt;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.commons.views.IItemTableData;
import com.hcl.domino.constants.OpenCollection;
import com.hcl.domino.crypt.DatabaseEncryptionState;
import com.hcl.domino.data.Agent;
import com.hcl.domino.data.AutoCloseableDocument;
import com.hcl.domino.data.DBQuery;
import com.hcl.domino.data.DQLQueryResult;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.DatabaseOption;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DocumentSelection;
import com.hcl.domino.data.DocumentSummaryQueryResult;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.DominoCollectionInfo;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.DominoOriginatorId;
import com.hcl.domino.data.FTIndex;
import com.hcl.domino.data.FTIndexStats;
import com.hcl.domino.data.FTQuery;
import com.hcl.domino.data.FTQueryResult;
import com.hcl.domino.data.FormulaQueryResult;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.NoteIdWithScore;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.design.RichTextBuilder;
import com.hcl.domino.dql.DQL.DQLTerm;
import com.hcl.domino.dql.QueryResultsProcessor;
import com.hcl.domino.exception.DocumentDeletedException;
import com.hcl.domino.exception.IncompatibleImplementationException;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.design.JNADbDesign;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.FTSearchResultsDecoder;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.Mem.LockedMemory;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesNamingUtils.Privileges;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.Win32NotesCallbacks;
import com.hcl.domino.jna.internal.capi.INotesCAPI;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.capi.NotesCAPI1201;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAIDTableAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAUserNamesListAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.search.NotesSearch;
import com.hcl.domino.jna.internal.search.NotesSearch.JNASearchMatch;
import com.hcl.domino.jna.internal.structs.NamedObjectEntryStruct;
import com.hcl.domino.jna.internal.structs.NotesBuildVersionStruct;
import com.hcl.domino.jna.internal.structs.NotesFTIndexStatsStruct;
import com.hcl.domino.jna.internal.structs.NotesItemDefinitionTableExt;
import com.hcl.domino.jna.internal.structs.NotesItemDefinitionTableLock;
import com.hcl.domino.jna.internal.structs.NotesOriginatorIdStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.hcl.domino.jna.internal.views.JNADominoCollectionInfo;
import com.hcl.domino.jna.richtext.JNARichTextBuilder;
import com.hcl.domino.jna.utils.JNADominoUtils;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.Loop;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.Ref;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

public class JNADatabase extends BaseJNAAPIObject<JNADatabaseAllocations> implements Database {
	static final String NAMEDNOTES_APPLICATION_PREFIX = "$app_"; //$NON-NLS-1$

	private static class LoopImpl extends Loop {
		
		public void next() {
			super.setIndex(getIndex()+1);
		}
		
		@Override
		public void setIsLast() {
			super.setIsLast();
		}
	}

	private String m_server;
	private String m_filePath;
	private String m_replicaID;
	@SuppressWarnings("unused")
	private Set<OpenDatabase> m_options;
	private boolean m_openAsIdUser;
	private List<String> m_namesStringList;
	private EnumSet<Privileges> m_namesListPrivileges;
	private String[] m_paths;
	private DbMode m_dbMode;
	private JNAAcl m_acl;
	private Boolean m_hasLargeItemSupport;
	
	public JNADatabase(IGCDominoClient<?> parent, String server, String filePath, Set<OpenDatabase> options) {
		this(parent, server, filePath, options, (JNAUserNamesList) null);
	}

	public JNADatabase(IGCDominoClient<?> parent, String server, String filePath, Set<OpenDatabase> options,
			JNAUserNamesList namesListOverride) {
		super(parent);
		
		init(server, filePath, options, namesListOverride);
		
		setInitialized();
	}

	public JNADatabase(IGCDominoClient<?> parent, IAdaptable adaptable) {
		super(parent);
		
		HANDLE hdl = adaptable.getAdapter(HANDLE.class);
		if (hdl==null) {
			throw new IllegalArgumentException("Missing DB handle");
		}
		
		JNADatabaseAllocations allocations = getAllocations();
		allocations.setDBHandle(hdl);
		
		JNAUserNamesList namesList = adaptable.getAdapter(JNAUserNamesList.class);
		if (namesList==null) {
			//try to retrieve the NAMES_LIST from the DB handle
			DHANDLE.ByReference rethNamesList = DHANDLE.newInstanceByReference();
			
			short result = LockUtil.lockHandle(hdl, (hDbByVal) -> {
				return NotesCAPI.get().NSFDbGetNamesList(hDbByVal, 0, rethNamesList);
			});
			NotesErrorUtils.checkResult(result);
			
			if (!rethNamesList.isNull()) {
				namesList = new JNAUserNamesList(this, rethNamesList);
			}
		}
		
		allocations.setNamesList(namesList);
		
		JNADominoClient parentClient = getParentDominoClient();
		List<String> builderNames = parentClient.getBuilderNamesList();

		if (namesList==null) {
			m_openAsIdUser = true;
		}
		else if (builderNames.isEmpty()) {
			m_openAsIdUser = NotesNamingUtils.equalNames(namesList.getPrimaryName(), parentClient.getIDUserName());
		}
		else {
			m_openAsIdUser = false;
		}
		
		m_namesStringList = namesList==null ? null : namesList.toList();
		m_namesListPrivileges = namesList==null ? null : NotesNamingUtils.getPrivileges(namesList);

		setInitialized();
	}
	
	@Override
	public JNADominoClient getParentDominoClient() {
		return (JNADominoClient)super.getParentDominoClient();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNADatabaseAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNADatabaseAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	private void init(String server, String filePath, Set<OpenDatabase> options,
			JNAUserNamesList namesListOverride) {
		
		if (namesListOverride!=null) {
			if (namesListOverride.isDisposed()) {
				throw new ObjectDisposedException(this);
			}
		}
		
		JNADominoClient parentClient = getParentDominoClient();
		boolean isOnServer = parentClient.isOnServer();
		
		// TODO figure out if there's a safe way to do this. Though it should be beneficial
		//   in that it queues database access properly, it fails very commonly with "server
		//   is not responding" and similar connection problems
//		if (StringUtil.isEmpty(server) && isOnServer) {
//			server = getParentDominoClient().getIDUserName();
//		}
		if (server==null) {
			server = ""; //$NON-NLS-1$
		}
		
		m_server = server;
		m_filePath = filePath;
		m_options = options;

		List<String> builderNames = parentClient.getBuilderNamesList();
		
		
//		if (!"".equals(m_server)) { //$NON-NLS-1$
//			if (isOnServer) {
//				String serverCN = NotesNamingUtils.toCommonName(server);
//				String currServerCanonical = parentClient.getIDUserName();
//				String currServerCN = NotesNamingUtils.toCommonName(currServerCanonical);
//				
//				if (serverCN.equalsIgnoreCase(currServerCN)) {
//					//switch to "" as servername if server points to the server the API is running on
//					m_server = ""; //$NON-NLS-1$
//				}
//			}
//		}
		
		DisposableMemory retFullNetPath = JNADominoUtils.constructNetPath(getParentDominoClient(), server, filePath);
		try {
			short openOptions = DominoEnumUtil.toBitField(OpenDatabase.class, options);

			JNAUserNamesList namesList = namesListOverride!=null ? namesListOverride : (JNAUserNamesList) getParentDominoClient().getEffectiveUserNamesList(server);
			
			if (namesListOverride!=null) {
				m_openAsIdUser = NotesNamingUtils.equalNames(namesListOverride.getPrimaryName(), parentClient.getIDUserName());
			}
			else {
				if (builderNames.isEmpty()) {
					m_openAsIdUser = NotesNamingUtils.equalNames(namesList.getPrimaryName(), parentClient.getIDUserName());
				}
				else {
					m_openAsIdUser = false;
				}
			}
			
			m_namesStringList = namesList==null ? null : namesList.toList();
			m_namesListPrivileges = namesList==null ? null : NotesNamingUtils.getPrivileges(namesList);

			JNADatabaseAllocations allocations = getAllocations();
			allocations.setNamesList(namesList);

			DHANDLE namesListHandle = namesList==null ? null : namesList.getAdapter(DHANDLE.class);
			if (namesList!=null && (namesListHandle==null || namesListHandle.isNull())) {
				throw new DominoException(0, "Could not read expected names list handle");
			}
			
			LockUtil.lockHandle(namesListHandle, (namesListHandleByVal) -> {
				if (namesList != null && namesList.isDisposed()) {
					throw new ObjectDisposedException(namesList);
				}
				
				HANDLE.ByReference retHDB = HANDLE.newInstanceByReference();
				NotesTimeDateStruct retDataModified = NotesTimeDateStruct.newInstance();
				NotesTimeDateStruct retNonDataModified = NotesTimeDateStruct.newInstance();
				
				// TODO support ModifiedTime
				short localResult;
				if (m_openAsIdUser && (m_namesListPrivileges==null || !m_namesListPrivileges.contains(Privileges.FullAdminAccess))) {
					localResult = NotesCAPI.get().NSFDbOpenExtended(retFullNetPath, openOptions, null,
							null, retHDB, retDataModified, retNonDataModified);
				}
				else {
					localResult = NotesCAPI.get().NSFDbOpenExtended(retFullNetPath, openOptions, namesListHandleByVal,
							null, retHDB, retDataModified, retNonDataModified);
				}

				if ((localResult & NotesConstants.ERR_MASK)==582 &&
						StringUtil.isEmpty(m_server) && !parentClient.isOnServer() && m_openAsIdUser) {
					localResult = NotesCAPI.get().NSFDbOpenExtended(retFullNetPath, openOptions, (DHANDLE.ByValue) null,
							null, retHDB, retDataModified, retNonDataModified);
				}
				
				if ((localResult & NotesConstants.ERR_MASK) == 259) { // File does not exist
					
					//try to find this database in the folder configured via SharedDataDirectory
					//in the Notes.ini
					boolean hasShared = StringUtil.isNotEmpty(getParentDominoClient().getDominoRuntime().getPropertyString("SharedDataDirectory")); //$NON-NLS-1$
					
					if(hasShared) {
						if (m_openAsIdUser && (m_namesListPrivileges==null || !m_namesListPrivileges.contains(Privileges.FullAdminAccess))) {
							localResult = NotesCAPI.get().NSFDbOpenTemplateExtended(retFullNetPath, openOptions,
									null,
									null, retHDB, retDataModified, retNonDataModified);
						}
						else {
							localResult = NotesCAPI.get().NSFDbOpenTemplateExtended(retFullNetPath, openOptions,
									namesListHandleByVal,
									null, retHDB, retDataModified, retNonDataModified);
						}
					}
				}
				
				if ((localResult & NotesConstants.ERR_MASK) == INotesErrorConstants.ERR_NOEXIST) {
					throw NotesErrorUtils.toNotesError(INotesErrorConstants.ERR_NOEXIST, MessageFormat.format("No database found on server ''{0}'' with filepath {1}", m_server, m_filePath)).get();
				}
				
				NotesErrorUtils.checkResult(localResult);
				
				getAllocations().setDBHandle(retHDB);
				
				loadPaths();
				return 0;
			});
			
		}
		finally {
			retFullNetPath.dispose();
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected <T> T getAdapterLocal(Class<T> clazz) {
		if (JNAUserNamesList.class.equals(clazz) || UserNamesList.class.equals(clazz)) {
			return (T) getAllocations().getNamesList();
		}
		else if(HANDLE.class.isAssignableFrom(clazz)) {
			return (T)getAllocations().getDBHandle();
		}
		
		return null;
	}
	
	@Override
	public String getServer() {
		loadPaths();
		return m_server;
	}

	/**
	 * Loads the path information from Notes
	 */
	private void loadPaths() {
		if (m_paths==null) {
			checkDisposed();
			JNADatabaseAllocations allocations = getAllocations();

			DisposableMemory retCanonicalPathName = new DisposableMemory(NotesConstants.MAXPATH);
			DisposableMemory retExpandedPathName = new DisposableMemory(NotesConstants.MAXPATH);
			try {
				short result = LockUtil.lockHandle(allocations.getDBHandle(), (handleByVal) -> {
					return NotesCAPI.get().NSFDbPathGet(handleByVal, retCanonicalPathName, retExpandedPathName);
				});
				NotesErrorUtils.checkResult(result);
				
				String canonicalPathName = NotesStringUtils.fromLMBCS(retCanonicalPathName, NotesStringUtils.getNullTerminatedLength(retCanonicalPathName));
				String expandedPathName = NotesStringUtils.fromLMBCS(retExpandedPathName, NotesStringUtils.getNullTerminatedLength(retExpandedPathName));
				String relDbPath;
				String absDbPath;

				int iPos = canonicalPathName.indexOf("!!"); //$NON-NLS-1$
				if (iPos==-1) {
					//local db
					m_server = ""; //$NON-NLS-1$
					relDbPath = canonicalPathName;
				}
				else {
					m_server = canonicalPathName.substring(0, iPos);
					relDbPath = canonicalPathName.substring(iPos+2);
				}
				iPos = expandedPathName.indexOf("!!"); //$NON-NLS-1$
				if (iPos==-1) {
					absDbPath = expandedPathName;
				}
				else {
					absDbPath = expandedPathName.substring(iPos+2);
				}
				m_paths = new String[] {relDbPath, absDbPath};
			}
			finally {
				retCanonicalPathName.dispose();
				retExpandedPathName.dispose();
			}
		}
	}
	
	@Override
	public String getRelativeFilePath() {
		loadPaths();
		return m_paths[0];
	}

	@Override
	public String getAbsoluteFilePath() {
		loadPaths();
		return m_paths[1];
	}

	@Override
	public String getReplicaID() {
		if (m_replicaID==null) {
			ReplicaInfo replInfo = getParentDominoClient().getReplication().getReplicaInfo(this);
			m_replicaID = replInfo.getReplicaID();
		}
		return m_replicaID;
	}

	public void _resetCachedReplicaId() {
		m_replicaID = null;
	}
	
	/**
	 * This function gets the database information buffer of a Domino database.<br>
	 * <br>
	 * The information buffer is a NULL terminated string and consists of one or more of the
	 * following pieces of information:<br>
	 * <ul>
	 * <li>database title</li>
	 * <li>categories</li>
	 * <li>class</li>
	 * <li>and design class</li>
	 * </ul>
	 * <br>
	 * Use NSFDbInfoParse to retrieve any one piece of information from the buffer.<br>
	 * <br>
	 * Database information appears in the Notes UI, in the File, Database, Properties InfoBox.<br>
	 * Clicking the Basics tab displays the Title field with the database title.<br>
	 * <br>
	 * Selecting the Design tab opens the Design tabbed page. The database class is displayed in the
	 * Database is a template/Template Name field and the database design class is displayed in the
	 * Inherit design from template/Template Name field. The Categories field displays the database
	 * categories.<br>
	 * <br>
	 * Database categories are different than view categories.<br>
	 * Database categories are keywords specified for the database.<br>
	 * Each server's database catalog (CATALOG.NSF) contains a view, called Databases by Category,
	 * which lists only the categorized databases.<br>
	 * <br>
	 * The database title also appears on the Notes Desktop below each database icon.
	 * 
	 * @return buffer
	 */
	private DisposableMemory getDbInfoBuffer() {
		checkDisposed();
		JNADatabaseAllocations allocations = getAllocations();
		
		DisposableMemory infoBuf = new DisposableMemory(NotesConstants.NSF_INFO_SIZE);
		short result = LockUtil.lockHandle(allocations.getDBHandle(), (handleByVal) -> {
			return NotesCAPI.get().NSFDbInfoGet(handleByVal, infoBuf);
		});
		NotesErrorUtils.checkResult(result);
		
		return infoBuf;
	}
	
	@Override
	public String getTitle() {
		checkDisposed();
		DisposableMemory infoBuf = getDbInfoBuffer();
		DisposableMemory titleMem = new DisposableMemory(NotesConstants.NSF_INFO_SIZE - 1);

		try {
			NotesCAPI.get().NSFDbInfoParse(infoBuf, NotesConstants.INFOPARSE_TITLE, titleMem, (short) (titleMem.size() & 0xffff));
			return NotesStringUtils.fromLMBCS(titleMem, -1);
		}
		finally {
			titleMem.dispose();
			infoBuf.dispose();
		}
	}

	/**
	 * Returns the icon document
	 * 
	 * @return an {@link Optional} describing the icon document or an empty one if not found
	 */
	public Optional<Document> openIconDocument() {
		checkDisposed();
		JNADatabaseAllocations allocations = getAllocations();

		IntByReference retNoteID = new IntByReference();
		short result = LockUtil.lockHandle(allocations.getDBHandle(), (handleByVal) -> {
			return NotesCAPI.get().NSFDbGetSpecialNoteID(handleByVal, (short) ((NotesConstants.SPECIAL_ID_NOTE | NotesConstants.NOTE_CLASS_ICON) & 0xffff), retNoteID);
		});
		
		if ((result & NotesConstants.ERR_MASK)==1028) { //not found
			return Optional.empty();
		}
		NotesErrorUtils.checkResult(result);
		int noteId = retNoteID.getValue();
		if (noteId==0) {
			return Optional.empty();
		}
		return getDocumentById(noteId);
	}
	
	/**
	 * Writes the modified db info buffer
	 * 
	 * @param infoBuf info buffer
	 */
	private void writeDbInfoBuffer(Memory infoBuf) {
		checkDisposed();
		JNADatabaseAllocations allocations = getAllocations();
		
		short result = LockUtil.lockHandle(allocations.getDBHandle(), (handleByVal) -> {
			return NotesCAPI.get().NSFDbInfoSet(handleByVal, infoBuf);
		});
		NotesErrorUtils.checkResult(result);
		
		//as documented in NSFDbInfoSet, we need to update the icon document as well
		try {
			JNADocument iconDoc = (JNADocument) openIconDocument().get();
			if (iconDoc.hasItem("$TITLE")) { //$NON-NLS-1$
				DHANDLE iconNoteHandle = iconDoc.getAdapter(DHANDLE.class);
				result = LockUtil.lockHandle(iconNoteHandle, (iconNoteHandleByVal) -> {
					return NotesCAPI.get().NSFItemSetText(iconNoteHandleByVal, NotesStringUtils.toLMBCS("$TITLE",  true), infoBuf, NotesConstants.MAXWORD); //$NON-NLS-1$
				});
				NotesErrorUtils.checkResult(result);

				iconDoc.save();
			}
			iconDoc.dispose();
		}
		catch (DominoException e) {
			if (e.getId() != 578) {
				throw e;
			}
		}
	}
	
	@Override
	public void setTitle(String title) {
		checkDisposed();
		DisposableMemory infoBuf = getDbInfoBuffer();
		try {
			Memory newTitleMem = NotesStringUtils.toLMBCS(title, true);

			NotesCAPI.get().NSFDbInfoModify(infoBuf, NotesConstants.INFOPARSE_TITLE, newTitleMem);

			writeDbInfoBuffer(infoBuf);
		}
		finally {
			infoBuf.dispose();
		}
	}

	@Override
	public Acl getACL() {
		checkDisposed();
		
		synchronized(this) {
			if (m_acl==null || m_acl.isDisposed()) {
				JNADatabaseAllocations allocations = getAllocations();
				DHANDLE.ByReference rethACL=DHANDLE.newInstanceByReference();
				
				short result = LockUtil.lockHandle(allocations.getDBHandle(), (handleByVal) -> {
					return NotesCAPI.get().NSFDbReadACL(handleByVal, rethACL);
				});
				
				NotesErrorUtils.checkResult(result);
				
				m_acl=new JNAAcl(this, rethACL);
			}
			return m_acl;
		}
	}

	@Override
	public Optional<DominoCollection> openDefaultCollection() {
		checkDisposed();
		
		short result;
		IntByReference retNoteID = new IntByReference();
		
		result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFDbGetSpecialNoteID(hDbByVal, (short) ((NotesConstants.SPECIAL_ID_NOTE | NotesConstants.NOTE_CLASS_VIEW) & 0xffff), retNoteID);
		});
		
		if ((result & NotesConstants.ERR_MASK)==1028) { //not found
			return Optional.empty();
		}
		
		NotesErrorUtils.checkResult(result);
		int noteId = retNoteID.getValue();
		if (noteId==0) {
			return Optional.empty();
		}
		return openCollection(noteId, (EnumSet<OpenCollection> ) null);
	}
	
	@Override
	public Optional<DominoCollection> openCollectionByUNID(String unid) {
		return openCollectionByUNID(unid, (EnumSet<OpenCollection>) null);
	}

	public Optional<DominoCollection> openCollectionByUNID(String unid, Set<OpenCollection> openFlagSet) {
		int viewNoteId = toNoteId(unid);
		if (viewNoteId==0) {
			return Optional.empty();
		}
		return openCollection(viewNoteId, openFlagSet);
	}

	@Override
	public Optional<DominoCollection> openCollection(String collectionName, Set<OpenCollection> openFlagSet) {
		checkDisposed();
		
		int viewNoteId = findCollectionId(collectionName, CollectionType.Both);
		if (viewNoteId==0) {
			return Optional.empty();
		}
		return openCollection(viewNoteId, openFlagSet);
	}

	public Optional<DominoCollection> openCollection(int viewNoteId, Set<OpenCollection> openFlagSet) {
	  return openCollection(viewNoteId, openFlagSet, this);
	}

  @Override
  public Optional<DominoCollection> openCollection(int viewNoteId, Database dbData) {
    return openCollection(viewNoteId, (EnumSet<OpenCollection> ) null, dbData);
  }

	private Optional<DominoCollection> openCollection(int viewNoteId, Set<OpenCollection> openFlagSet, Database dbData) {
		checkDisposed();
		
		if (viewNoteId==0) {
		  return Optional.empty();
		}
		
		if (dbData!=null && dbData instanceof JNADatabase==false) {
		  throw new IllegalArgumentException("Database to read data must be of type JNADatabase");
		}
		
		JNADatabase jnaDbData = dbData==null ? this : (JNADatabase) dbData;
		
		jnaDbData.checkDisposed();
		
		JNADatabaseAllocations dbViewAllocations = getAllocations();
    JNADatabaseAllocations dbDataAllocations = jnaDbData.getAllocations();

		DisposableMemory retViewUNID = new DisposableMemory(16);
		JNAIDTable unreadTable = new JNAIDTable(getParentDominoClient());
		
		short openFlags = DominoEnumUtil.toBitField(OpenCollection.class, openFlagSet);
    //always enforce reopening; funny things can happen on a Domino server
    //without this flag like sharing collections between users resulting in
    //users seeing the wrong data *sometimes*...
		openFlags |= (short) NotesConstants.OPEN_REOPEN_COLLECTION;
		
		JNAUserNamesList namesList = dbViewAllocations.getNamesList();
		
		if (namesList.isDisposed()) {
			throw new ObjectDisposedException(namesList);
		}
		
		DHANDLE.ByReference rethCollection = DHANDLE.newInstanceByReference();
		rethCollection.clear();
		DHANDLE.ByReference rethCollapsedList = DHANDLE.newInstanceByReference();
		rethCollapsedList.clear();
		DHANDLE.ByReference rethSelectedList = DHANDLE.newInstanceByReference();
		rethSelectedList.clear();
		
		JNAUserNamesListAllocations namesListAllocations = (JNAUserNamesListAllocations) namesList.getAdapter(APIObjectAllocations.class);
		JNAIDTableAllocations unreadTableAllocations = (JNAIDTableAllocations) unreadTable.getAdapter(APIObjectAllocations.class);
		
		short fOpenFlags = openFlags;
		
		short result = LockUtil.lockHandles(
				namesListAllocations.getHandle(),
				unreadTableAllocations.getIdTableHandle(),
				dbViewAllocations.getDBHandle(),
				dbDataAllocations.getDBHandle(),
				
				(namesListHandleByVal, unreadTableHandleByVal, dbViewHandleByVal, dbDataHandleByVal) -> {
					short localResult = NotesCAPI.get().NIFOpenCollectionWithUserNameList(dbViewHandleByVal,
							dbDataHandleByVal,
							viewNoteId, fOpenFlags, unreadTableHandleByVal,
							rethCollection, null, retViewUNID, rethCollapsedList,
							rethSelectedList, namesListHandleByVal);
					
					return localResult;
				});
		
		NotesErrorUtils.checkResult(result);
		
		String sViewUNID = toUNID(retViewUNID);
		retViewUNID.dispose();
		
		JNAIDTable collapsedList = new JNAIDTable(getParentDominoClient(), rethCollapsedList, true);
		JNAIDTable selectedList = new JNAIDTable(getParentDominoClient(), rethSelectedList, true);
		
		return Optional.of(new JNADominoCollection(this, jnaDbData, rethCollection, viewNoteId,
				sViewUNID, collapsedList,
				selectedList, unreadTable));
	}
	
	/**
	 * Converts bytes in memory to a UNID
	 * 
	 * @param buf memory
	 * @return unid
	 */
	private static String toUNID(Memory buf) {
		Formatter formatter = new Formatter();
		ByteBuffer data = buf.getByteBuffer(0, buf.size()).order(ByteOrder.LITTLE_ENDIAN);
		formatter.format("%016x", data.getLong()); //$NON-NLS-1$
		formatter.format("%016x", data.getLong()); //$NON-NLS-1$
		String unid = formatter.toString().toUpperCase();
		formatter.close();
		return unid;
	}
	
	@Override
	public Optional<Document> getDocumentById(int noteId) {
		return getDocumentById(noteId, EnumSet.noneOf(OpenDocumentMode.class));
	}

	public int _toDocumentOpenOptions(Set<OpenDocumentMode> flags) {
		int options = 0;
		
		if (flags!=null) {
			if (flags.contains(OpenDocumentMode.SUMMARY_ONLY)) {
				options = options | NotesConstants.OPEN_SUMMARY;
			}
			
			if (flags.contains(OpenDocumentMode.MARK_READ)) {
				options = options | NotesConstants.OPEN_MARK_READ;
			}
			
			if (flags.contains(OpenDocumentMode.ABSTRACT_ONLY)) {
				options = options | NotesConstants.OPEN_ABSTRACT;
			}
			
			if (flags.contains(OpenDocumentMode.LOAD_RESPONSES)) {
				options = options | NotesConstants.OPEN_RESPONSE_ID_TABLE;
			}

			if (flags.contains(OpenDocumentMode.CACHE)) {
				options = options | NotesConstants.OPEN_CACHE;
			}
			
			if (flags.contains(OpenDocumentMode.NOOBJECTS)) {
				options = options | NotesConstants.OPEN_NOOBJECTS;
			}
		}
		
		// we negated the following two OPEN_XXX constants, so we keep
		// the items in their native format if conversion is not explicitly requested
		
		if (flags==null || !flags.contains(OpenDocumentMode.CONVERT_RFC822_TO_TEXT_AND_TIME)) {
			options = options | NotesConstants.OPEN_RAW_RFC822_TEXT;
		}

		if (flags==null || !flags.contains(OpenDocumentMode.CONVERT_MIME_TO_RICHTEXT)) {
			options = options | NotesConstants.OPEN_RAW_MIME_PART;
		}

		return options;
	}
	
	@Override
	public Optional<Document> getDocumentById(int noteId, Set<OpenDocumentMode> flags) {
		checkDisposed();
		JNADatabaseAllocations allocations = getAllocations();

		int openOptions = _toDocumentOpenOptions(flags);
		
		DHANDLE.ByReference rethNote = DHANDLE.newInstanceByReference();
		
		short result = LockUtil.lockHandle(allocations.getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().NSFNoteOpenExt(dbHandleByVal, noteId, openOptions, rethNote);
		});
		
		if ((result & NotesConstants.ERR_MASK)==INotesErrorConstants.ERR_NOT_FOUND) {
			return Optional.empty();
		}
		NotesErrorUtils.checkResult(result);
		
		return Optional.of(new JNADocument(this, rethNote));
	}

	@Override
	public Optional<Document> getSoftDeletedDocumentById(int noteId) {
		checkDisposed();
		JNADatabaseAllocations allocations = getAllocations();

		DHANDLE.ByReference rethNote = DHANDLE.newInstanceByReference();
		
		short result = LockUtil.lockHandle(allocations.getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().NSFNoteOpenSoftDelete(dbHandleByVal, noteId, 0, rethNote);
		});
		
		if ((result & NotesConstants.ERR_MASK)==1028) {
			return Optional.empty();
		}
		NotesErrorUtils.checkResult(result);
		
		return Optional.of(new JNADocument(this, rethNote));
	}
	
	@Override
	public Optional<Document> getSoftDeletedDocumentByUNID(String unid) {
		int noteId = toNoteId(unid) & ~0x80000000; // remove RRV_DELETED bit
		if (noteId==0) {
			return Optional.empty();
		}
		return getSoftDeletedDocumentById(noteId);
	}
	
	@Override
	public Optional<Document> getDocumentByUNID(String unid) {
		return getDocumentByUNID(unid, EnumSet.noneOf(OpenDocumentMode.class));
	}

	@Override
	public Optional<Document> getDocumentByUNID(String unid, Set<OpenDocumentMode> flags) {
		checkDisposed();
		JNADatabaseAllocations allocations = getAllocations();

		int openOptions = _toDocumentOpenOptions(flags);
		NotesUniversalNoteIdStruct unidObj = NotesUniversalNoteIdStruct.fromString(unid);
		
		DHANDLE.ByReference rethNote = DHANDLE.newInstanceByReference();
		short result = LockUtil.lockHandle(allocations.getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().NSFNoteOpenByUNIDExtended(dbHandleByVal, unidObj, openOptions, rethNote);
		});
		if ((result & NotesConstants.ERR_MASK)==1028) {
			return Optional.empty();
		}
		NotesErrorUtils.checkResult(result);
		
		return Optional.of(new JNADocument(this, rethNote));
	}

	@Override
	public void deleteDocument(int noteId) {
		if (noteId==0) {
			return;
		}
		
		deleteDocument(noteId, EnumSet.noneOf(UpdateNote.class));
	}

	void deleteDocument(int noteId, Set<UpdateNote> flags) {
		checkDisposed();
		JNADatabaseAllocations allocations = getAllocations();
		
		int flagsAsInt = DominoEnumUtil.toBitField(UpdateNote.class, flags);
		
		short result = LockUtil.lockHandle(allocations.getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().NSFNoteDeleteExtended(dbHandleByVal, noteId, flagsAsInt);
		});
		NotesErrorUtils.checkResult(result);
	}
	
	@Override
	public void deleteDocument(String unid) {
		deleteDocument(unid, EnumSet.noneOf(UpdateNote.class));
	}

	void deleteDocument(String unid, Set<UpdateNote> flags) {
		int noteId = toNoteId(unid);
		if (noteId!=0) {
			deleteDocument(noteId, flags);
		}
	}

	@Override
	public void deleteDocuments(Collection<Integer> noteIds) {
		checkDisposed();
		JNADatabaseAllocations allocations = getAllocations();
		
		JNAIDTable idTable;
		boolean disposeIDTable;
		
		if (noteIds instanceof JNAIDTable) {
			idTable = (JNAIDTable) noteIds;
			disposeIDTable = false;
		}
		else {
			idTable = new JNAIDTable(getParentDominoClient(), noteIds);
			disposeIDTable = true;
		}

		try {
			DHANDLE idTableHandle = idTable.getAdapter(DHANDLE.class);
			
			short result = LockUtil.lockHandles(
					allocations.getDBHandle(),
					idTableHandle,
					(dbHandleByVal, idTableHandleByVal) -> {

						return NotesCAPI.get().NSFDbDeleteNotes(dbHandleByVal, idTableHandleByVal, null);
					});

			NotesErrorUtils.checkResult(result);
		}
		finally {
			if (disposeIDTable) {
				idTable.dispose();
			}
		}
	}

	@Override
	public void deleteDocumentsByUNID(Collection<String> unids) {
		Map<String,Integer> resolvedNoteIDsByUNID = new HashMap<>();
		Set<String> unresolvedUNIDs = new HashSet<>();
		
		toNoteIds(unids, resolvedNoteIDsByUNID, unresolvedUNIDs);
		deleteDocuments(resolvedNoteIDsByUNID.values());
	}

	@Override
	public Document createDocument() {
		return createDocument(Collections.emptySet());
	}

	@Override
	public Document createDocument(Set<CreateFlags> flags) {
		checkDisposed();
		JNADatabaseAllocations allocations = getAllocations();
		
		DHANDLE.ByReference retNoteHandle = DHANDLE.newInstanceByReference();
		short result = LockUtil.lockHandle(allocations.getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().NSFNoteCreate(dbHandleByVal, retNoteHandle);
		});
		NotesErrorUtils.checkResult(result);
		
		JNADocument doc = new JNADocument(this, retNoteHandle);
		if (flags!=null && flags.contains(CreateFlags.HIDE_FROM_VIEWS)) {
			doc.setHiddenFromViews(true);
		}
		return doc;
	}
	
	@Override
	public Optional<Document> getProfileDocument(String profileName) {
		return getProfileDocument(profileName, (String) null);
	}

	@Override
	public Optional<Document> getProfileDocument(String profileName, String userName) {
		checkDisposed();
		JNADatabaseAllocations allocations = getAllocations();
		
		Memory profileNameMem = NotesStringUtils.toLMBCS(profileName, false);
		Memory userNameMem = StringUtil.isEmpty(userName) ? null : NotesStringUtils.toLMBCS(userName, false);
		
		DHANDLE.ByReference rethProfileNote = DHANDLE.newInstanceByReference();

		short result = LockUtil.lockHandle(allocations.getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().NSFProfileOpen(dbHandleByVal, profileNameMem,
					(short) (profileNameMem.size() & 0xffff), userNameMem,
					(short) (userNameMem==null ? 0 : (userNameMem.size() & 0xffff)), (short) 1, rethProfileNote);
		});
		
		if ((result & NotesConstants.ERR_MASK)==1028) {
			return Optional.empty();
		}
		NotesErrorUtils.checkResult(result);
		
		return Optional.of(new JNADocument(this, rethProfileNote));
	}

	private static class ProfileNoteInfo {
		private String m_profileName;
		private String m_username;
		private int m_noteId;
		
		private ProfileNoteInfo(String profileName, String username, int noteId) {
			m_profileName = profileName;
			m_username = username;
			m_noteId = noteId;
		}
		
		public int getNoteId() {
			return m_noteId;
		}
		
		@SuppressWarnings("unused")
		public String getProfileName() {
			return m_profileName;
		}
		
		public String getUserName() {
			return m_username;
		}
		
		@Override
		public String toString() {
			return MessageFormat.format(
				"ProfileNoteInfo [profileName={0}, username={1}, noteId={2}]", //$NON-NLS-1$
				m_profileName, m_username, m_noteId
			);
		}
	}
	
	/**
	 * Returns infos about the profile notes with the specified name in the database
	 * 
	 * @param profileName Name of the profile. To enumerate all profile documents within a database, use null
	 * @return list of  profile note infos
	 */
	private List<ProfileNoteInfo> getProfileNoteInfos(String profileName) {
		checkDisposed();

		List<ProfileNoteInfo> retNoteInfos = new ArrayList<>();

		Memory profileNameMem = StringUtil.isEmpty(profileName) ? null : NotesStringUtils.toLMBCS(profileName, false);

		NotesCallbacks.NSFPROFILEENUMPROC callback;

		if (PlatformUtils.isWin32()) {
			callback = (Win32NotesCallbacks.NSFPROFILEENUMPROCWin32) (hDB, ctx, profileNameMem1, profileNameLength, usernameMem, usernameLength, noteId) -> {
				String profileName1 = ""; //$NON-NLS-1$
				if (profileName1 != null) {
					profileName1 = NotesStringUtils.fromLMBCS(profileNameMem1, ((profileNameLength & 0xffff)));
				}
				String userName = ""; //$NON-NLS-1$
				if (usernameMem != null) {
					userName = NotesStringUtils.fromLMBCS(usernameMem, ((profileNameLength & 0xffff)));
				}

				retNoteInfos.add(new ProfileNoteInfo(profileName1, userName, noteId));
				return 0;
			};
		} else {
			callback = (hDB, ctx, profileNameMem1, profileNameLength, usernameMem, usernameLength, noteId) -> {
				String profileName1 = ""; //$NON-NLS-1$
				if (profileName1 != null) {
					profileName1 = NotesStringUtils.fromLMBCS(profileNameMem1, ((profileNameLength & 0xffff)));
				}
				String userName = ""; //$NON-NLS-1$
				if (usernameMem != null) {
					userName = NotesStringUtils.fromLMBCS(usernameMem, ((usernameLength & 0xffff)));
				}

				retNoteInfos.add(new ProfileNoteInfo(profileName1, userName, noteId));
				return 0;
			};
		}

		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().NSFProfileEnum(dbHandleByVal, profileNameMem,
					profileNameMem == null ? (short) 0 : (short) (profileNameMem.size() & 0xffff), callback, null, 0);
		});
		NotesErrorUtils.checkResult(result);

		return retNoteInfos;
	}
	
	@Override
	public void forEachProfileDocument(BiConsumer<Document, Loop> consumer) {
		forEachProfileDocument(null, null, consumer);
	}

	@Override
	public void forEachProfileDocument(String profileName, BiConsumer<Document, Loop> consumer) {
		forEachProfileDocument(profileName, null, consumer);
	}

	@Override
	public void forEachProfileDocument(String profileName, String userName, BiConsumer<Document, Loop> consumer) {
		Stream<Document> docs = getProfileNoteInfos(profileName)
		.stream()
		.filter((profileInfo) -> {
			boolean result = StringUtil.isEmpty(userName) || NotesNamingUtils.equalNames(profileInfo.getUserName(), userName);
			return result;
		})
		.map((profileInfo) -> {
			checkDisposed();
			JNADatabaseAllocations allocations = getAllocations();

			int openOptions = 0;
			
			DHANDLE.ByReference rethNote = DHANDLE.newInstanceByReference();
			
			short result = LockUtil.lockHandle(allocations.getDBHandle(), (dbHandleByVal) -> {
				return NotesCAPI.get().NSFNoteOpenExt(dbHandleByVal, profileInfo.getNoteId(), openOptions, rethNote);
			});
			if ((result & NotesConstants.ERR_MASK)==1028) {
				return null;
			}
			NotesErrorUtils.checkResult(result);
			
			Document doc = new JNADocument(this, rethNote);
			return doc;
		})
		.filter((profileDoc) -> {
			boolean result = profileDoc!=null;
			return result;
		});
		
		LoopImpl loop = new LoopImpl();
		
		Iterator<Document> docsIt = docs.iterator();
		while (docsIt.hasNext()) {
			Document doc = docsIt.next();

			boolean isLast = !docsIt.hasNext();
			
			if (isLast) {
				loop.setIsLast();
			}
			consumer.accept(doc, loop);
			
			if (loop.isStopped()) {
				break;
			}
			loop.next();
		}
	}

	@Override
	public NotesReplicationStats replicate(String serverName) {
		return replicate(serverName, Collections.emptySet(), 0, null);
	}

	@Override
	public NotesReplicationStats replicate(String serverName, Set<ReplicateOption> options, int timeLimitMin,
			ReplicationStateListener progressListener) {
		
		String dbPathWithServer;
		
		String server = getServer();
		if (!StringUtil.isEmpty(server)) {
			dbPathWithServer = server + "!!" + getRelativeFilePath(); //$NON-NLS-1$
		}
		else {
			dbPathWithServer = getAbsoluteFilePath();
		}
		return getParentDominoClient().replicateDbsWithServer(serverName, options, Arrays.asList(dbPathWithServer), timeLimitMin, progressListener);
	}

	@Override
	public String toUNID(int noteId) {
		Map<Integer,String> retUnidsByNoteId = new HashMap<>(1);
		Set<Integer> retNoteIdsNotFound = new HashSet<>(1);
		toUNIDs(Arrays.asList(noteId), retUnidsByNoteId, retNoteIdsNotFound);
		return retUnidsByNoteId.get(noteId);
	}

	@Override
	public int toNoteId(String unid) {
		Map<String,Integer> retNoteIdsByUnid = new HashMap<>(1);
		Set<String> retNoteUnidsNotFound = new HashSet<>(1);
		toNoteIds(Arrays.asList(unid), retNoteIdsByUnid, retNoteUnidsNotFound);
		
		Integer noteId = retNoteIdsByUnid.get(unid);
		return noteId!=null ? noteId : 0;
	}

	@Override
	public void toUNIDs(Collection<Integer> noteIds, Map<Integer, String> resolvedUNIDsByNoteId,
			Set<Integer> unresolvedNoteIds) {
		
		int[] noteIDsArr = new int[noteIds.size()];
		int idx=0;
		for (Integer currNoteId : noteIds) {
			noteIDsArr[idx++] = currNoteId;
		}
		
		DocInfo[] infoArr = getMultiDocumentInfo(noteIDsArr);
		for (int i=0; i<noteIDsArr.length; i++) {
			DocInfo currInfo = infoArr[i];
			if (currInfo.exists()) {
				resolvedUNIDsByNoteId.put(noteIDsArr[i], currInfo.getUnid());
			}
			else {
				unresolvedNoteIds.add(noteIDsArr[i]);
			}
		}
	}

	@Override
	public void toNoteIds(Collection<String> unids, Map<String, Integer> resolvedNoteIDsByUNID,
			Set<String> unresolvedUNIDs) {
		
		String[] unidsArr = new String[unids.size()];
		int idx=0;
		for (String currUnids : unids) {
			unidsArr[idx++] = currUnids;
		}
		
		DocInfo[] infoArr = getMultiDocumentInfo(unidsArr);
		for (int i=0; i<unidsArr.length; i++) {
			DocInfo currInfo = infoArr[i];
			if (currInfo.exists()) {
				resolvedNoteIDsByUNID.put(unidsArr[i], currInfo.getNoteId());
			}
			else {
				if (unresolvedUNIDs!=null) {
					unresolvedUNIDs.add(unidsArr[i]);
				}
			}
		}
	}

	@Override
	public boolean isDocumentLockingEnabled() {
		return getOption(DatabaseOption.IS_LOCK_DB);
	}

	@Override
	public void setDocumentLockingEnabled(boolean b) {
		setOption(DatabaseOption.IS_LOCK_DB, b);
	}

	@Override
	public boolean isLargeSummaryEnabled() {
		checkDisposed();

		return LockUtil.lockHandle(getAllocations().getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().NSFDbLargeSummaryEnabled(dbHandleByVal) == 1;
		});
	}

	@Override
	public Set<DatabaseOption> getOptions() {
		checkDisposed();
		
		DisposableMemory retDbOptions = new DisposableMemory(4 * 4); //DWORD[4]
		try {
			short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
				return NotesCAPI.get().NSFDbGetOptionsExt(hDbByVal, retDbOptions);
			});
			NotesErrorUtils.checkResult(result);
			
			byte[] dbOptionsArr = retDbOptions.getByteArray(0, 4 * 4);

			Set<DatabaseOption> dbOptions = EnumSet.noneOf(DatabaseOption.class);
			
			for (DatabaseOption currOpt : DatabaseOption.values()) {
				int optionBit = currOpt.getValue();
				int byteOffsetWithBit = optionBit / 8;
				byte byteValueWithBit = dbOptionsArr[byteOffsetWithBit];
				int bitToCheck = (int) Math.pow(2, optionBit % 8);
				
				boolean enabled = (byteValueWithBit & bitToCheck) == bitToCheck;
				if (enabled) {
					dbOptions.add(currOpt);
				}
			}
			
			return dbOptions;
		}
		finally {
			retDbOptions.dispose();
		}
	}

	@Override
	public boolean getOption(DatabaseOption option) {
		checkDisposed();
		
		Memory retDbOptions = new Memory(4 * 4); //DWORD[4]
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().NSFDbGetOptionsExt(dbHandleByVal, retDbOptions);
		});
		NotesErrorUtils.checkResult(result);
		
		byte[] dbOptionsArr = retDbOptions.getByteArray(0, 4 * 4);

		int optionBit = option.getValue();
		int byteOffsetWithBit = optionBit / 8;
		byte byteValueWithBit = dbOptionsArr[byteOffsetWithBit];
		int bitToCheck = (int) Math.pow(2, optionBit % 8);
		
		boolean enabled = (byteValueWithBit & bitToCheck) == bitToCheck;
		return enabled;
	}
	
	@Override
	public void setOption(DatabaseOption option, boolean flag) {
		checkDisposed();
		
		int optionBit = option.getValue();
		int byteOffsetWithBit = optionBit / 8;
		int bitToCheck = (int) Math.pow(2, optionBit % 8);

		byte[] optionsWithBitSetArr = new byte[4*4];
		optionsWithBitSetArr[byteOffsetWithBit] = (byte) (bitToCheck & 0xff);
		
		Memory dbOptionsWithBitSetMem = new Memory(4 * 4);
		dbOptionsWithBitSetMem.write(0, optionsWithBitSetArr, 0, 4*4);
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (dbHandleByVal) -> {
			if (flag) {
				//use dbOptionsMem both for the new value and for the bitmask, since the new value is 1
				return NotesCAPI.get().NSFDbSetOptionsExt(dbHandleByVal, dbOptionsWithBitSetMem, dbOptionsWithBitSetMem);
			}
			else {
				Memory nullBytesMem = new Memory(4 * 4);
				nullBytesMem.clear();
				return NotesCAPI.get().NSFDbSetOptionsExt(dbHandleByVal, nullBytesMem, dbOptionsWithBitSetMem);
			}
		});
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public boolean isFTIndex() {
		return getLastFTIndexTime() != null;
	}

	@Override
	public Optional<DominoDateTime> getLastFTIndexTime() {
		checkDisposed();
		
		return LockUtil.lockHandle(getAllocations().getDBHandle(), (dbHandleByVal) -> {
			NotesTimeDateStruct retTime = NotesTimeDateStruct.newInstance();
			
			short result = NotesCAPI.get().FTGetLastIndexTime(dbHandleByVal, retTime);
			if (result == INotesErrorConstants.ERR_FT_NOT_INDEXED) {
				return Optional.empty();
			}
			NotesErrorUtils.checkResult(result);
			retTime.read();
			
			DominoDateTime retTimeWrap = new JNADominoDateTime(retTime);
			return Optional.of(retTimeWrap);
		});
	}
	
	@Override
	public void deleteFTIndex() {
		checkDisposed();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().FTDeleteIndex(dbHandleByVal);
		});
		NotesErrorUtils.checkResult(result);
		
		setOption(DatabaseOption.FT_INDEX, false);
	}
	
	@Override
	public FTIndexStats ftIndex(Set<FTIndex> options) {
		checkDisposed();
		
		short optionsBitMask = DominoEnumUtil.toBitField(FTIndex.class, options);
		
		NotesFTIndexStatsStruct retStats = NotesFTIndexStatsStruct.newInstance();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().FTIndex(hDbByVal, optionsBitMask, null, retStats);
		});
		NotesErrorUtils.checkResult(result);
		retStats.read();
		
		return new JNAFTIndexStats(getServer(), getRelativeFilePath(),
				retStats.DocsAdded, retStats.DocsUpdated, retStats.DocsDeleted,
				retStats.BytesIndexed);
	}

	private class JNAFTIndexStats implements FTIndexStats {
		private String m_server;
		private String m_filePath;
		private int m_docsAdded;
		private int m_docsUpdated;
		private int m_docsDeleted;
		private int m_bytesIndexed;
		
		private JNAFTIndexStats(String server, String filePath,
				int docsAdded, int docsUpdated, int docsDeleted, int bytesIndexed) {
			m_server = server;
			m_filePath = filePath;
			m_docsAdded = docsAdded;
			m_docsUpdated = docsUpdated;
			m_docsDeleted = docsDeleted;
			m_bytesIndexed = bytesIndexed;
		}
		
		@Override
		public String getServer() {
			return m_server;
		}

		@Override
		public String getFilePath() {
			return m_filePath;
		}
		
		@Override
		public int getDocsAdded() {
			return m_docsAdded;
		}

		@Override
		public int getDocsUpdated() {
			return m_docsUpdated;
		}

		@Override
		public int getDocsDeleted() {
			return m_docsDeleted;
		}

		@Override
		public int getBytesIndexed() {
			return m_bytesIndexed;
		}

		@Override
		public String toString() {
			return MessageFormat.format(
				"JNAFTIndexStats [server={0}, filePath={1}, docsAdded={2}, docsUpdated={3}, docsDeleted={4}, bytesIndexed={5}]", //$NON-NLS-1$
				m_server, m_filePath, m_docsAdded, m_docsUpdated, m_docsDeleted, m_bytesIndexed
			);
		}
		
		
	}
	
	@Override
	public void ftIndexRequest() {
		checkDisposed();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().ClientFTIndexRequest(hDbByVal);
		});
		NotesErrorUtils.checkResult(result);
	}
	
	@Override
	public boolean isLocallyEncrypted() {
		checkDisposed();
		
		IntByReference retVal = new IntByReference();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().NSFDbIsLocallyEncrypted(dbHandleByVal, retVal);
		});
		NotesErrorUtils.checkResult(result);

		return retVal.getValue() == 1;
	}

	@Override
	public String generateUNID() {
		return generateOID().getUNID();
	}

	@Override
	public String getCategories() {
		checkDisposed();
		
		Memory infoBuf = getDbInfoBuffer();
		Memory categoriesMem = new Memory(NotesConstants.NSF_INFO_SIZE - 1);
		
		NotesCAPI.get().NSFDbInfoParse(infoBuf, NotesConstants.INFOPARSE_CATEGORIES, categoriesMem, (short) (categoriesMem.size() & 0xffff));
		return NotesStringUtils.fromLMBCS(categoriesMem, -1);
	}

	@Override
	public void setCategories(String categories) {
		checkDisposed();
		
		Memory infoBuf = getDbInfoBuffer();
		Memory newCategoriesMem = NotesStringUtils.toLMBCS(categories, true);
		
		NotesCAPI.get().NSFDbInfoModify(infoBuf, NotesConstants.INFOPARSE_CATEGORIES, newCategoriesMem);
		
		writeDbInfoBuffer(infoBuf);
	}

	@Override
	public DocInfo[] getMultiDocumentInfo(int[] noteIds) {
		checkDisposed();
		
		int entrySize = 4 /* note id */ + JNANotesConstants.oidSize;
		//not more than 32767 entries and output buffer cannot exceed 64k
		final int ENTRIESBYCALL = Math.min(65535, 64000 / entrySize);

		if (noteIds.length < ENTRIESBYCALL) {
			return _getMultiDocumentInfo(noteIds);
		}
		
		//work around C API limit of max 65535 entries per call
		DocInfo[] noteInfos = new DocInfo[noteIds.length];
		
		int startOffset = 0;
		
		while (startOffset < noteIds.length) {
			int endOffsetExclusive = Math.min(noteIds.length, startOffset + ENTRIESBYCALL);
			int[] currNoteIds = new int[endOffsetExclusive - startOffset];
			System.arraycopy(noteIds, startOffset, currNoteIds, 0, endOffsetExclusive - startOffset);
			
			DocInfo[] currNoteInfos = _getMultiDocumentInfo(currNoteIds);
			System.arraycopy(currNoteInfos, 0, noteInfos, startOffset, currNoteInfos.length);
			startOffset += ENTRIESBYCALL;
		}
		
		return noteInfos;
	}

	/**
	 * This method can be used to get information for a number documents in a
	 * database from their note ids in a single call.<br>
	 * The data returned by this method is the note id, {@link NotesOriginatorIdStruct}, which contains
	 * the UNID of the document, the sequence number and the sequence time ("Modified initially" time).<br>
	 * <br>
	 * In addition, the method checks whether a document exists or has been deleted.<br>
	 * <br>
	 * Please note that the method can only handle max. 65535 note ids, because it's
	 * using a WORD / short datatype for the count internally to call the C API.
	 * 
	 * @param noteIds array of note ids
	 * @return lookup results, same size and order as <code>noteIds</code> array
	 * @throws IllegalArgumentException if note id array has too many entries (more than 65535)
	 */
	private DocInfo[] _getMultiDocumentInfo(int[] noteIds) {
		if (noteIds.length ==0) {
			return new DocInfo[0];
		}
		
		if (noteIds.length > 65535) {
			throw new IllegalArgumentException("Max 65535 note ids are supported");
		}
		
		DHANDLE.ByReference retHandle = DHANDLE.newInstanceByReference();
		short result = Mem.OSMemAlloc((short) 0, noteIds.length * 4, retHandle);
		NotesErrorUtils.checkResult(result);

		JNADatabaseAllocations allocations = getAllocations();
		
		return LockUtil.lockHandles(allocations.getDBHandle(), retHandle, (dbHandleByVal, retHandleByVal) -> {
			boolean inMemHandleLocked = false;
			
			try {
				Pointer inBufPtr = Mem.OSLockObject(retHandleByVal);
				inMemHandleLocked = true;
				
				Pointer currInBufPtr = inBufPtr;
				int offset = 0;
				
				for (int i=0; i<noteIds.length; i++) {
					currInBufPtr.setInt(0, noteIds[i]);
					offset += 4;
					currInBufPtr = inBufPtr.share(offset);
				}
				
				Mem.OSUnlockObject(retHandleByVal);
				inMemHandleLocked = false;
				
				IntByReference retSize = new IntByReference();
				DHANDLE.ByReference rethOutBuf = DHANDLE.newInstanceByReference();
				short options = NotesConstants.fINFO_OID | NotesConstants.fINFO_ALLOW_HUGE | NotesConstants.fINFO_NOTEID;
				
				short resultGetInfo = NotesCAPI.get().NSFDbGetMultNoteInfo(dbHandleByVal, (short) (noteIds.length & 0xffff), options, retHandleByVal, retSize, rethOutBuf);
				NotesErrorUtils.checkResult(resultGetInfo);

				if (rethOutBuf.isNull()) {
					throw new IllegalStateException("Returned result handle is 0");
				}
				
				return LockUtil.lockHandle(rethOutBuf, (rethOutBufByVal) -> {
					//decode return buffer
					int entrySize = 4 /* note id */ + JNANotesConstants.oidSize;
					long retSizeLong = retSize.getValue();
					if (retSizeLong != noteIds.length*entrySize) {
						throw new IllegalStateException(
							MessageFormat.format(
								"Unexpected size of return data. Expected {0} bytes for data of {1} ids, got {2} bytes",
								noteIds.length*entrySize, noteIds.length, retSizeLong
							)
						);
					}
					
					Pointer outBufPtr = Mem.OSLockObject(rethOutBufByVal);
					try {
						DocInfo[] infos = decodeMultiNoteLookupData(noteIds.length, outBufPtr);
						return infos;
					}
					finally {
						Mem.OSUnlockObject(rethOutBufByVal);
						short resultLocal1 = Mem.OSMemFree(rethOutBufByVal);
						NotesErrorUtils.checkResult(resultLocal1);
					}
				});
			}
			finally {
				if (inMemHandleLocked) {
					Mem.OSUnlockObject(retHandleByVal);
				}
				short resultMemFree = Mem.OSMemFree(retHandleByVal);
				NotesErrorUtils.checkResult(resultMemFree);
			}
		});
	}
	
	@Override
	public DocInfo[] getMultiDocumentInfo(String[] noteUNIDs) {
		checkDisposed();

		int entrySize = 4 /* note id */ + JNANotesConstants.oidSize;
		//not more than 32767 entries and output buffer cannot exceed 64k
		final int ENTRIESBYCALL = Math.min(32767, 64000 / entrySize);
		
		if (noteUNIDs.length < ENTRIESBYCALL) {
			return _getMultiDocumentInfo(noteUNIDs);
		}
		
		//work around C API limit of max 32767 entries per call
		DocInfo[] noteInfos = new DocInfo[noteUNIDs.length];
		
		int startOffset = 0;
		
		while (startOffset < noteUNIDs.length) {
			int endOffsetExclusive = Math.min(noteUNIDs.length, startOffset + ENTRIESBYCALL);
			String[] currNoteUNIDs = new String[endOffsetExclusive - startOffset];
			System.arraycopy(noteUNIDs, startOffset, currNoteUNIDs, 0, endOffsetExclusive - startOffset);
			
			DocInfo[] currNoteInfos = _getMultiDocumentInfo(currNoteUNIDs);
			System.arraycopy(currNoteInfos, 0, noteInfos, startOffset, currNoteInfos.length);
			startOffset += ENTRIESBYCALL;
		}
		
		return noteInfos;
	}

	/**
	 * This method can be used to get information for a number documents in a
	 * database from their document unids in a single call.<br>
	 * The data returned by this method is the note id, {@link NotesOriginatorIdStruct}, which contains
	 * the UNID of the document, the sequence number and the sequence time ("Modified initially" time).<br>
	 * <br>
	 * In addition, the method checks whether a document exists or has been deleted.<br>
	 * <br>
	 * Please note that the method can only handle max. 32767 note ids in one call.
	 * 
	 * @param unids array of note unids
	 * @return lookup results, same size and order as <code>unids</code> array
	 * @throws IllegalArgumentException if note unid array has too many entries (more than 32767)
	 */
	private DocInfo[] _getMultiDocumentInfo(String[] unids) {
		if (unids.length ==0) {
			return new DocInfo[0];
		}
		
		if (unids.length > 32767) {
			throw new IllegalArgumentException("Max 32767 note ids are supported");
		}
		
		DHANDLE.ByReference retHandle = DHANDLE.newInstanceByReference();
		short result = Mem.OSMemAlloc((short) 0, unids.length * 16, retHandle);
		NotesErrorUtils.checkResult(result);

		JNADatabaseAllocations allocations = getAllocations();
		
		DocInfo[] retNoteInfo = LockUtil.lockHandles(allocations.getDBHandle(), retHandle,
				(dbHandleByVal, retHandleByVal) -> {
			
			boolean inMemHandleLocked = false;
			try {
				Pointer inBufPtr = Mem.OSLockObject(retHandleByVal);
				inMemHandleLocked = true;
				
				Pointer currInBufPtr = inBufPtr;
				int offset = 0;
				
				for (int i=0; i<unids.length; i++) {
					NotesStringUtils.unidToPointer(unids[i], currInBufPtr);
					offset += 16;
					currInBufPtr = inBufPtr.share(offset);
				}
				
				Mem.OSUnlockObject(retHandleByVal);
				inMemHandleLocked = false;
				
				IntByReference retSize = new IntByReference();
				DHANDLE.ByReference rethOutBuf = DHANDLE.newInstanceByReference();
				
				short options = NotesConstants.fINFO_OID | NotesConstants.fINFO_ALLOW_HUGE | NotesConstants.fINFO_NOTEID;
				
				short resultGetInfo = NotesCAPI.get().NSFDbGetMultNoteInfoByUNID(dbHandleByVal, (short) (unids.length & 0xffff),
						options, retHandleByVal, retSize, rethOutBuf);

				NotesErrorUtils.checkResult(resultGetInfo);
				
				DocInfo[] infos = LockUtil.lockHandle(rethOutBuf, (rethOutBufByVal) -> {
					//decode return buffer
					int entrySize = 4 /* note id */ + JNANotesConstants.oidSize;
					long retSizeLong = retSize.getValue();
					if (retSizeLong != unids.length*entrySize) {
						throw new IllegalStateException(
							MessageFormat.format(
								"Unexpected size of return data. Expected {0} bytes for data of {1} ids, got {2} bytes",
								unids.length*entrySize, unids.length, retSizeLong
							)
						);
					}
					
					Pointer outBufPtr = Mem.OSLockObject(rethOutBufByVal);
					try {
						return decodeMultiNoteLookupData(unids.length, outBufPtr);
					}
					finally {
						Mem.OSUnlockObject(rethOutBufByVal);
						short resultMemFree = Mem.OSMemFree(rethOutBufByVal);
						NotesErrorUtils.checkResult(resultMemFree);
					}
				});
				
				return infos;
			}
			finally {
				if (inMemHandleLocked) {
					Mem.OSUnlockObject(retHandleByVal);
				}
				short resultMemFree = Mem.OSMemFree(retHandleByVal);
				NotesErrorUtils.checkResult(resultMemFree);
			}
		});
		
		return retNoteInfo;
	}

	/**
	 * Helper method to extract the return data of method {@link #getMultiNoteInfo(int[])} or {@link #getMultiNoteInfo(String[])}
	 * 
	 * @param nrOfElements number of list elements
	 * @param outBufPtr buffer pointer
	 * @return array of note info objects
	 */
	private DocInfo[] decodeMultiNoteLookupData(int nrOfElements, Pointer outBufPtr) {
		DocInfo[] retNoteInfo = new DocInfo[nrOfElements];
		
		Pointer entryBufPtr = outBufPtr;
		
		for (int i=0; i<nrOfElements; i++) {
			int offsetInEntry = 0;
			
			int currNoteId = entryBufPtr.getInt(0);

			offsetInEntry += 4;

			Pointer fileTimeDatePtr = entryBufPtr.share(offsetInEntry);
			
			String unid = NotesStringUtils.pointerToUnid(fileTimeDatePtr);
			
			offsetInEntry += 8; //skip "file" field
			offsetInEntry += 8; // skip "note" field
			
			int sequence = entryBufPtr.getInt(offsetInEntry);

			offsetInEntry += 4;
			
			Pointer sequenceTimePtr = entryBufPtr.share(offsetInEntry);
			int[] sequenceTimeInnards = sequenceTimePtr.getIntArray(0, 2);
			DominoDateTime sequenceTime = new JNADominoDateTime(sequenceTimeInnards);
			
			offsetInEntry += 8;
			
			entryBufPtr = entryBufPtr.share(offsetInEntry);
			
			boolean isDeleted = (currNoteId & NotesConstants.NOTEID_RESERVED) == NotesConstants.NOTEID_RESERVED;
			boolean isNotPresent = currNoteId==0;
			retNoteInfo[i] = new DocInfoImpl(currNoteId, unid, sequenceTime, sequence, isDeleted, isNotPresent);
		}
		return retNoteInfo;
	}

	/**
	 * Data container that stores the lookup result for note info
	 * 
	 * @author Karsten Lehmann
	 */
	private static class DocInfoImpl implements DocInfo {
		private int m_noteId;
		private int m_sequence;
		private DominoDateTime m_sequenceTime;
		private String m_unid;
		private boolean m_isDeleted;
		private boolean m_notPresent;
		
		private DocInfoImpl(int noteId, String unid, DominoDateTime sequenceTime, int sequence,
				boolean isDeleted, boolean notPresent) {
			
			m_noteId = noteId;
			m_unid = unid;
			m_sequenceTime = sequenceTime;
			m_sequence = sequence;
			m_isDeleted = isDeleted;
			m_notPresent = notPresent;
		}
		
		/**
		 * Returns the note id
		 * 
		 * @return note id or 0 if the note could not be found
		 */
		@Override
		public int getNoteId() {
			return m_noteId;
		}
		
		/**
		 * Returns the sequence number
		 * 
		 * @return sequence number or 0 if the note could not be found
		 */
		@Override
		public int getSequence() {
			return m_sequence;
		}
		
		/**
		 * Returns the sequence time ( = "Modified (initially)")
		 * 
		 * @return sequence time or null if the note could not be found
		 */
		@Override
		public Optional<DominoDateTime> getSequenceTime() {
			return Optional.ofNullable(m_sequenceTime);
		}
		
		/**
		 * Returns the UNID as hex string
		 * 
		 * @return UNID or null if the note could not be found
		 */
		@Override
		public String getUnid() {
			return StringUtil.toString(m_unid);
		}
		
		/**
		 * Returns true if the note has already been deleted
		 * 
		 * @return true if deleted
		 */
		@Override
		public boolean isDeleted() {
			return m_isDeleted;
		}
		
		/**
		 * Returns true if the note currently exists in the database
		 * 
		 * @return true if note exists
		 */
		@Override
		public boolean exists() {
			return !m_notPresent;
		}
	}

	/**
	 * Extension of {@link DocInfoImpl} with additional note lookup data
	 * 
	 * @author Karsten Lehmann
	 */
	private static class DocInfoExtImpl extends DocInfoImpl implements DocInfoExt {
		private NotesTimeDateStruct m_modified;
		private short m_noteClass;
		private NotesTimeDateStruct m_addedToFile;
		private short m_responseCount;
		private int m_parentNoteId;
		
		private DocInfoExtImpl(int noteId, String unid, DominoDateTime sequenceTime, int sequence,
				boolean isDeleted, boolean notPresent,
				NotesTimeDateStruct modified, short noteClass, NotesTimeDateStruct addedToFile, short responseCount,
				int parentNoteId) {
			
			super(noteId, unid, sequenceTime, sequence, isDeleted, notPresent);
			
			m_modified = modified;
			m_noteClass = noteClass;
			m_addedToFile = addedToFile;
			m_responseCount = responseCount;
			m_parentNoteId = parentNoteId;
		}
		
		@Override
		public Optional<DominoDateTime> getModified() {
			return Optional.ofNullable(m_modified==null ? null : new JNADominoDateTime(m_modified));
		}
		
		@Override
		public Set<DocumentClass> getNoteClass() {
			Set<DocumentClass> docClass = EnumSet.noneOf(DocumentClass.class);
			
			if (m_noteClass==0) {
				docClass.add(DocumentClass.NONE);
			}
			else {
				for (DocumentClass currClass : DocumentClass.values()) {
					if (currClass.getValue()!=0) {
						if ((m_noteClass & currClass.getValue()) == currClass.getValue()) {
							docClass.add(currClass);
						}
					}
				}
			}
			return docClass;
		}
		
		@Override
		public Optional<DominoDateTime> getAddedToFile() {
			return Optional.ofNullable(m_addedToFile==null ? null : new JNADominoDateTime(m_addedToFile));
		}
		
		@Override
		public short getResponseCount() {
			return m_responseCount;
		}
		
		@Override
		public int getParentNoteId() {
			return m_parentNoteId;
		}
	}
	
	@Override
	public DocInfoExt getDocumentInfo(int noteId) {
		checkDisposed();

		JNADatabaseAllocations allocations = getAllocations();
		
		return LockUtil.lockHandle(allocations.getDBHandle(), (dbHandleByVal) -> {
			NotesOriginatorIdStruct retNoteOID = NotesOriginatorIdStruct.newInstance();
			NotesTimeDateStruct retModified = NotesTimeDateStruct.newInstance();
			ShortByReference retNoteClass = new ShortByReference();
			NotesTimeDateStruct retAddedToFile = NotesTimeDateStruct.newInstance();
			ShortByReference retResponseCount = new ShortByReference();
			IntByReference retParentNoteID = new IntByReference();
			boolean isDeleted = false;
			//not sure if we can check this via error code:
			boolean notPresent = false;
			
			short result = NotesCAPI.get().NSFDbGetNoteInfoExt(dbHandleByVal, noteId, retNoteOID, retModified, retNoteClass, retAddedToFile, retResponseCount, retParentNoteID);
			if (result==INotesErrorConstants.ERR_NOTE_DELETED) {
				isDeleted = true;
			}
			else if (result==INotesErrorConstants.ERR_INVALID_NOTE) {
				notPresent = true;
			}
			else {
				NotesErrorUtils.checkResult(result);
			}
			
			String unid = retNoteOID.getUNIDAsString();
			DominoDateTime sequenceTime = new JNADominoDateTime(retNoteOID.SequenceTime.Innards);
			int sequence = retNoteOID.Sequence;
			
			DocInfoExt info = new DocInfoExtImpl(noteId, unid, sequenceTime, sequence,
					isDeleted, notPresent, retModified,
					retNoteClass.getValue(), retAddedToFile, retResponseCount.getValue(), retParentNoteID.getValue());
			
			return info;
		});
	}

	@Override
	public Optional<Agent> getAgent(String agentName) {
		checkDisposed();

		Memory agentNameLMBCS = NotesStringUtils.toLMBCS(agentName, true);

		IntByReference retAgentNoteID = new IntByReference();
		retAgentNoteID.setValue(0);
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal)-> {
			return NotesCAPI.get().NIFFindDesignNoteExt(hDbByVal, agentNameLMBCS,
					NotesConstants.NOTE_CLASS_FILTER,
					NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_AGENTSLIST, true),
					retAgentNoteID,
					NotesConstants.DGN_STRIPUNDERS);
		});
		
		if ((result & NotesConstants.ERR_MASK)==1028) {
			//Entry not found in index
			return Optional.empty();
		}
		
		//throws an error if agent cannot be found:
		NotesErrorUtils.checkResult(result);
		
		int agentNoteId = retAgentNoteID.getValue();
		if (agentNoteId==0) {
			throw new DominoException(MessageFormat.format("Agent not found in database: {0}", agentName));
		}
		
		DHANDLE.ByReference rethAgent = DHANDLE.newInstanceByReference();
		
		result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal)-> {
			return NotesCAPI.get().AgentOpen(hDbByVal, agentNoteId, rethAgent);
		});
		
		NotesErrorUtils.checkResult(result);
		
		JNAAgent agent = new JNAAgent(this, agentNoteId, rethAgent);
		
		return Optional.of(agent);
	}

	@Override
	public Optional<Document> getAgentSavedData(String agentName) {
		checkDisposed();

		Memory agentNameLMBCS = NotesStringUtils.toLMBCS(agentName, true);

		IntByReference retAgentNoteID = new IntByReference();
		retAgentNoteID.setValue(0);
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal)-> {
			return NotesCAPI.get().NIFFindDesignNoteExt(hDbByVal, agentNameLMBCS,
					NotesConstants.NOTE_CLASS_FILTER,
					NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_AGENTSLIST, true),
					retAgentNoteID, NotesConstants.DGN_STRIPUNDERS);
		});
		
		if ((result & NotesConstants.ERR_MASK)==1028) {
			//Entry not found in index
			return Optional.empty();
		}
		
		//throws an error if agent cannot be found:
		NotesErrorUtils.checkResult(result);
		
		int agentNoteId = retAgentNoteID.getValue();
		if (agentNoteId==0) {
			throw new DominoException(MessageFormat.format("Agent not found in database: {0}", agentName));
		}

		NotesUniversalNoteIdStruct.ByReference retUNID = NotesUniversalNoteIdStruct.newInstanceByReference();
		
		result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().AssistantGetLSDataNote(hDbByVal, agentNoteId, retUNID);
		});
		NotesErrorUtils.checkResult(result);
		
		String unid = retUNID.toString();
		if (StringUtil.isEmpty(unid) || "00000000000000000000000000000000".equals(unid)) { //$NON-NLS-1$
			return Optional.empty();
		}
		else {
			return getDocumentByUNID(unid);
		}
	}

	@Override
	public DominoOriginatorId generateOID() {
		NotesOriginatorIdStruct oidStruct = generateOIDStruct();
		return new JNADominoOriginatorId(oidStruct);
	}
	
	NotesOriginatorIdStruct generateOIDStruct() {
		checkDisposed();
		JNADatabaseAllocations allocations = getAllocations();

		NotesOriginatorIdStruct retOIDStruct = NotesOriginatorIdStruct.newInstance();
		
		short result = LockUtil.lockHandle(allocations.getDBHandle(), (handleByVal) -> {
			return NotesCAPI.get().NSFDbGenerateOID(handleByVal, retOIDStruct);
		});
		NotesErrorUtils.checkResult(result);

		retOIDStruct.read();

		return retOIDStruct;
	}
	
	public enum DbMode {
		/** internal db handle refers to a "directory" and not a file */
		DIRECTORY,
		/** internal db handle refers to a normal database file */
		DATABASE
		}

	/**
	 * Use this function to find out whether the {@link JNADatabase} is a database or a directory.
	 * (The C API uses the db handle also to scan directory contents)
	 * 
	 * @return mode
	 */
	public DbMode getMode() {
		if (m_dbMode==null) {
			checkDisposed();

			ShortByReference retMode = new ShortByReference();
			short result;

			JNADatabaseAllocations allocations = getAllocations();
			result = LockUtil.lockHandle(allocations.getDBHandle(), (handleByVal) -> {
				return NotesCAPI.get().NSFDbModeGet(handleByVal, retMode);
			});

			NotesErrorUtils.checkResult(result);

			if (retMode.getValue() == NotesConstants.DB_LOADED) {
				m_dbMode = DbMode.DATABASE;
			}
			else {
				m_dbMode = DbMode.DIRECTORY;
			}
		}
		return m_dbMode;
	}

	/**
	 * Checks whether a database is located on a remote server
	 * 
	 * @return true if remote
	 */
	@Override
	public boolean isRemote() {
		checkDisposed();
		
		short isRemote = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFDbIsRemote(hDbByVal);
		});
		
		return isRemote==1;
	}
	
	@Override
	public boolean hasFullAccess() {
		checkDisposed();
		
		short hasFullAccess = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFDbHasFullAccess(hDbByVal);
		});
		
		return hasFullAccess==1;
	}
	
	@Override
	public Database reopen() {
		checkDisposed();
		
		HANDLE.ByReference rethNewDb = HANDLE.newInstanceByReference();
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFDbReopen(hDbByVal, rethNewDb);
		});
		NotesErrorUtils.checkResult(result);
		
		JNAUserNamesList userNamesList = NotesNamingUtils.writeNewNamesList(getParent(), m_namesStringList);
		return new JNADatabase(getParentDominoClient(), new IAdaptable() {

			@Override
			@SuppressWarnings("unchecked")
			public <T> T getAdapter(Class<T> clazz) {
				if (HANDLE.class.equals(clazz)) {
					return (T) rethNewDb;
				}
				else if (JNAUserNamesList.class.equals(clazz)) {
					return (T) userNamesList;
				}
				else {
					return null;
				}
			}
		});
	}
	
	@Override
	public Optional<Document> getDocumentByPrimaryKey(String category, String objectId) {
		String fullNodeName = getApplicationNoteName(category, objectId);
		int rrv = getNamedObjectRRV(fullNodeName, NotesConstants.NONS_NAMED_NOTE);
		try {
			return rrv==0 ? Optional.empty() : getDocumentById(rrv);
		} catch(DocumentDeletedException e) {
			return Optional.empty();
		}
	}

	/**
	 * Computes a $name value from category/objectkey similar to the name
	 * of profile notes, e.g. "$app_015calcolorprofile_" or "$app_015calcolorprofile_myobjectname"
	 * 
	 * @param category category part of primary key
	 * @param objectKey object key part of primary key
	 * @return note name
	 */
	static String getApplicationNoteName(String category, String objectKey) {
		// use a format similar to profile docs $name value,
		// e.g. "$app_015calcolorprofile_" or "$app_015calcolorprofile_myobjectname"
		String fullNodeName = (NAMEDNOTES_APPLICATION_PREFIX +
				StringUtil.pad(Integer.toString(category.length()), 3, '0', false) + category + "_" + //$NON-NLS-1$
				objectKey)
				.toLowerCase(Locale.ENGLISH);
		
		return fullNodeName;
	}

	/**
	 * Parses a string like "$app_015calcolorprofile_myobjectname" into
	 * category and object id
	 * 
	 * @param name name
	 * @return array of [category,objectid] or null if unsupported format
	 */
	static String[] parseApplicationNamedNoteName(String name) {
		if (!name.startsWith(NAMEDNOTES_APPLICATION_PREFIX)) {
			return null;
		}
		
		String remainder = name.substring(5); //"$app_".length()
		if (remainder.length()<3) {
			return null;
		}
		
		String categoryNameLengthStr = remainder.substring(0, 3);
		int categoryNameLength = Integer.parseInt(categoryNameLengthStr);
		
		remainder = remainder.substring(3);
		String category = remainder.substring(0, categoryNameLength);
		
		remainder = remainder.substring(categoryNameLength+1);
		
		String objectKey = remainder;
		
		return new String[] {category, objectKey};
	}

	/**
	 * Returns the RRV of a named object in the database
	 * 
	 * @param name name to look for
	 * @return rrv or 0 if not found
	 */
	private int getNamedObjectRRV(String name, short namespace) {
		checkDisposed();
		
		if (StringUtil.isEmpty(name)) {
			throw new IllegalArgumentException("Name cannot be empty");
		}

    Memory nameMem = NotesStringUtils.toLMBCS(name, false);
    
    IntByReference rtnObjectID = new IntByReference();
    
    short nsWithNoAssignBit = (short) (namespace | NotesConstants.NONS_NOASSIGN); //prevent assigning a new RRV if not found
    
    short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (dbHandleByVal) -> {
      return NotesCAPI.get().NSFDbGetNamedObjectID(dbHandleByVal, nsWithNoAssignBit, nameMem,
          (short) (nameMem.size() & 0xffff), rtnObjectID);
    });
    if ((result & NotesConstants.ERR_MASK)==578) { //special database object cannot be located
      return 0;
    }
    NotesErrorUtils.checkResult(result);

    return rtnObjectID.getValue();
	}

	@Override
	public Map<String,Map<String,Integer>> getAllDocumentsByPrimaryKey() {
		Map<String,Map<String,Integer>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
    getNamedObjects(NotesConstants.NONS_NAMED_NOTE)
    .forEach((entry) -> {
      int noteId = entry.getNoteID();
      
      if (noteId!=0) {
        String currName = entry.getNameOfDocument();
        if (StringUtil.startsWithIgnoreCase(currName, NAMEDNOTES_APPLICATION_PREFIX)) {
          String[] parsedNamedNoteInfos = parseApplicationNamedNoteName(currName);
          if (parsedNamedNoteInfos!=null) {
            String category = parsedNamedNoteInfos[0];
            String objectKey = parsedNamedNoteInfos[1];
            
            Map<String,Integer> entriesForCategory = result.get(category);
            if (entriesForCategory==null) {
              entriesForCategory = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
              result.put(category, entriesForCategory);
            }
            
            entriesForCategory.put(objectKey, noteId);
          }
        }
      }
      
    });
    
    return result;
	}
	
	@Override
	public Map<String,Integer> getAllDocumentsByPrimaryKey(String category) {
		if (StringUtil.isEmpty(category)) {
			throw new IllegalArgumentException("Category cannot be empty");
		}
		String prefix = getApplicationNoteName(category, ""); //$NON-NLS-1$
		
		Map<String,Integer> result = new HashMap<>();
		
    getNamedObjects(NotesConstants.NONS_NAMED_NOTE)
    .forEach((entry) -> {
      int noteId = entry.getNoteID();
      
      if (noteId!=0) {
        String currName = entry.getNameOfDocument();
        if (StringUtil.startsWithIgnoreCase(currName, prefix)) {
          String objectKey = currName.substring(prefix.length()).toLowerCase(Locale.ENGLISH);
          result.put(objectKey, noteId);
        }
      }
      
    });
    
    return result;
	}

	@Override
	public DominoCollection openDesignCollection() {
		checkDisposed();
		
		try {
			DominoCollection col = openCollection(NotesConstants.NOTE_ID_SPECIAL | NotesConstants.NOTE_CLASS_DESIGN, (EnumSet<OpenCollection>) null).get();
			return col;
		}
		catch (DominoException e) {
			//ignore, we call DesignOpenCollection next which creates the design collection
		}
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			DHANDLE.ByReference rethCollection = DHANDLE.newInstanceByReference();
			IntByReference retCollectionNoteID = new IntByReference();
			
			short openResult = NotesCAPI.get().DesignOpenCollection(hDbByVal, false, (short) 0, rethCollection, retCollectionNoteID);
			if (openResult==0) {
				LockUtil.lockHandle(rethCollection, (hDesignColByVal) -> {
					NotesErrorUtils.checkResult(NotesCAPI.get().NIFCloseCollection(hDesignColByVal));
					return 0;
				});
			}
			return openResult;
		});
		NotesErrorUtils.checkResult(result);
		
		//try again:
		DominoCollection col = openCollection(NotesConstants.NOTE_ID_SPECIAL | NotesConstants.NOTE_CLASS_DESIGN, (EnumSet<OpenCollection> ) null).get();
		return col;
	}
	
	@Override
	public Stream<DominoCollectionInfo> getAllCollections() {
		// This goes the route of doing an NSFSearch in order to avoid trouble when the design collection
		//   has not yet been initialized.
		// Additionally, it doesn't pull $TITLE, etc. from the summary buffer, as this can sometimes be
		//   errantly empty.
		List<Integer> noteIds = new ArrayList<>();
		NotesSearch.search(this, null, "@True", "-", EnumSet.noneOf(SearchFlag.class), EnumSet.of(DocumentClass.VIEW), null, new NotesSearch.SearchCallback() { //$NON-NLS-1$ //$NON-NLS-2$
			@Override
			public Action noteFound(JNADatabase parentDb, JNASearchMatch searchMatch, IItemTableData summaryBufferData) {
				noteIds.add(searchMatch.getNoteID());
				return Action.Continue;
			}
		});
		return noteIds.stream()
		    .map((id) -> { return getDocumentById(id).orElse(null); } )
		    .filter(Objects::nonNull)
		    .map((doc) -> {
		      try (AutoCloseableDocument closeableDoc = doc.autoClosable()) {
		        JNADominoCollectionInfo info = new JNADominoCollectionInfo(this);
		        List<String> titles = closeableDoc.getAsList(NotesConstants.FIELD_TITLE, String.class, Collections.emptyList());
		        titles = DesignUtil.toTitlesList(titles);
		        String title = titles.isEmpty() ? "" : titles.get(0); //$NON-NLS-1$
		        info.setTitle(title);
		        List<String> aliases = titles.size() < 2 ? Collections.emptyList() : titles.subList(1, titles.size());
		        info.setAliases(aliases);
		        info.setComment(closeableDoc.getAsText(NotesConstants.FILTER_COMMENT_ITEM, ' '));
		        info.setFlags(closeableDoc.getAsText(NotesConstants.DESIGN_FLAGS, ' '));
		        info.setLanguage(closeableDoc.getAsText(NotesConstants.FIELD_LANGUAGE, ' '));
		        info.setNoteID(closeableDoc.getNoteID());
		        info.setUNID(closeableDoc.getUNID());
		        return info;
		      }
		    });
	}

	@Override
	public void forEachCollection(BiConsumer<DominoCollectionInfo, Loop> consumer) {
		LoopImpl loop = new LoopImpl();
		
		Iterator<DominoCollectionInfo> it = getAllCollections().iterator();
		
		while (it.hasNext()) {
			DominoCollectionInfo currInfo = it.next();
			
			if (!it.hasNext()) {
				loop.setIsLast();
			}
			consumer.accept(currInfo, loop);
			
			if (loop.isStopped()) {
				break;
			}
			
			loop.next();
		}
	}
	
	@Override
	public String toStringLocal() {
		if (isDisposed()) {
			return MessageFormat.format("JNADatabase [disposed, server={0}, filepath={1}]", m_server, m_filePath); //$NON-NLS-1$
		}
		else {
			return MessageFormat.format("JNADatabase [handle={0}, server={1}, filepath={2}]", getAllocations().getDBHandle(), getServer(), getRelativeFilePath()); //$NON-NLS-1$
		}
	}
	
	@Override
	public void close() {
		if(!isDisposed()) {
			CAPIGarbageCollector.dispose(this);
		}
	}

	@Override
	public String getTemplateName() {
		checkDisposed();
		
		Memory infoBuf = getDbInfoBuffer();
		Memory templateNameMem = new Memory(NotesConstants.NSF_INFO_SIZE - 1);
		
		NotesCAPI.get().NSFDbInfoParse(infoBuf, NotesConstants.INFOPARSE_CLASS, templateNameMem, (short) (templateNameMem.size() & 0xffff));
		return NotesStringUtils.fromLMBCS(templateNameMem, -1);
	}
	
	@Override
	public void setTemplateName(String newTemplateName) {
		checkDisposed();
		
		Memory infoBuf = getDbInfoBuffer();
		Memory newTemplateNameMem = NotesStringUtils.toLMBCS(newTemplateName, true);
		
		NotesCAPI.get().NSFDbInfoModify(infoBuf, NotesConstants.INFOPARSE_CLASS, newTemplateNameMem);
		
		writeDbInfoBuffer(infoBuf);
	}
	
	@Override
	public String getDesignTemplateName() {
		checkDisposed();
		
		Memory infoBuf = getDbInfoBuffer();
		Memory designTemplateNameMem = new Memory(NotesConstants.NSF_INFO_SIZE - 1);
		
		NotesCAPI.get().NSFDbInfoParse(infoBuf, NotesConstants.INFOPARSE_DESIGN_CLASS, designTemplateNameMem, (short) (designTemplateNameMem.size() & 0xffff));
		return NotesStringUtils.fromLMBCS(designTemplateNameMem, -1);
	}
	
	@Override
	public void setDesignTemplateName(String newDesignTemplateName) {
		checkDisposed();
		
		Memory infoBuf = getDbInfoBuffer();
		Memory newDesignTemplateNameMem = NotesStringUtils.toLMBCS(newDesignTemplateName, true);
		
		NotesCAPI.get().NSFDbInfoModify(infoBuf, NotesConstants.INFOPARSE_DESIGN_CLASS, newDesignTemplateNameMem);
		
		writeDbInfoBuffer(infoBuf);
	}
	
	@Override
	public void updateDQLDesignCatalog(boolean rebuild) {
		checkDisposed();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFDesignHarvest(hDbByVal, rebuild);
		});
		NotesErrorUtils.checkResult(result);
	}
	
	@Override
	public DQLQueryResult queryDQL(DQLTerm query) {
		return queryDQL(query.toString(), null, 0, 0,
				0);
	}

	@Override
	public DQLQueryResult queryDQL(DQLTerm query, Set<DBQuery> flags) {
		return queryDQL(query, flags, 0, 0, 0);
	}
	
	@Override
	public DQLQueryResult queryDQL(DQLTerm query, Set<DBQuery> flags,
			int maxDocsScanned, int maxEntriesScanned, int maxMsecs) {
		
		return queryDQL(query.toString(), flags, maxDocsScanned, maxEntriesScanned,
				maxMsecs);
	}
	
	@Override
	public DQLQueryResult queryDQL(String query) {
		return queryDQL(query, null, 0, 0, 0);
	}
	
	@Override
	public DQLQueryResult queryDQL(String query, Set<DBQuery> flags) {
		return queryDQL(query, flags, 0, 0, 0);
	}

	@Override
	public DQLQueryResult queryDQL(String query, Set<DBQuery> flags,
			int maxDocsScanned, int maxEntriesScanned, int maxMsecs) {
		
		checkDisposed();
		
		Memory queryMem = NotesStringUtils.toLMBCS(query, true);
		int flagsAsInt = flags==null ? 0 : DominoEnumUtil.toBitField(DBQuery.class, flags);
		
		JNAIDTable idTable = null;
		String errorTxt = ""; //$NON-NLS-1$
		String explainTxt = ""; //$NON-NLS-1$
		
		IntByReference retError = new IntByReference();
		retError.setValue(0);
		IntByReference retExplain = new IntByReference();
		retExplain.setValue(0);
		
		long t0=System.currentTimeMillis();

		DHANDLE.ByReference retResults = DHANDLE.newInstanceByReference();
		retResults.clear();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hdbByVal) -> {
			return NotesCAPI.get().NSFQueryDB(hdbByVal, queryMem,
					flagsAsInt, maxDocsScanned, maxEntriesScanned, maxMsecs, retResults, 
					retError, retExplain);
		});
		
		if (retError.getValue()!=0) {
			try (LockedMemory m = Mem.OSMemoryLock(retError.getValue())) {
				errorTxt = NotesStringUtils.fromLMBCS(m.getPointer(), -1);
			}
			finally {
				Mem.OSMemoryFree(retError.getValue());
			}
		}

		if (result!=0) {
			if (!StringUtil.isEmpty(errorTxt)) {
				throw new DominoException(result, errorTxt, NotesErrorUtils.toNotesError(result).orElse(null));
			}
			else {
				NotesErrorUtils.checkResult(result);
			}
		}
		
		if (!retResults.isNull()) {
			idTable = new JNAIDTable(getParentDominoClient(), retResults, false);
		}
		
		if (retExplain.getValue()!=0) {
			try (LockedMemory m = Mem.OSMemoryLock(retExplain.getValue())) {
				explainTxt = NotesStringUtils.fromLMBCS(m.getPointer(), -1);
			}
			finally {
				Mem.OSMemoryFree(retExplain.getValue());
			}
		}
	
		long t1=System.currentTimeMillis();
		
		return new JNADQLQueryResult(this, query, idTable, explainTxt, t1-t0);
	}
	
	@Override
	public FTQueryResult queryFTIndex(String query, int maxResults, Set<FTQuery> options,
			Set<Integer> filterIds, int start, int count) {
		
		checkDisposed();

		if (maxResults<0 || maxResults>65535) {
			throw new IllegalArgumentException("MaxResults must be between 0 and 65535");
		}

		if (count>65535) {
			throw new IllegalArgumentException("Count must be between 0 and 65535");
		}

		EnumSet<FTQuery> searchOptionsToUse = EnumSet.noneOf(FTQuery.class);
		searchOptionsToUse.addAll(options);
		
		if (filterIds!=null) {
			//automatically set refine option if id table is not null
			searchOptionsToUse.add(FTQuery.REFINE);
		}
		int searchOptionsBitMask = DominoEnumUtil.toBitField(FTQuery.class, searchOptionsToUse);
		
		short limitAsShort = (short) (maxResults & 0xffff); 
		
		JNAIDTable filterIDTable;
		if (filterIds instanceof JNAIDTable) {
			filterIDTable = (JNAIDTable) filterIds;
		}
		else if (filterIds!=null) {
			filterIDTable = new JNAIDTable(getParentDominoClient(), filterIds);
		}
		else {
			filterIDTable = null;
		}
		
		DHANDLE filterIDTableHandle = filterIDTable==null ? null : filterIDTable.getAdapter(DHANDLE.class);
		
		List<String> builderNames = getParentDominoClient().getBuilderNamesList();

		DHANDLE hNamesList = null;
		UserNamesList namesList = getAdapter(UserNamesList.class);
		if (namesList!=null) {
			boolean openAsIdUser;
			
			if (builderNames.isEmpty()) {
				openAsIdUser = NotesNamingUtils.equalNames(namesList.getPrimaryName(), getParentDominoClient().getIDUserName());
			}
			else {
				openAsIdUser = false;
			}

			if (openAsIdUser) {
				hNamesList = null;
			}
			else {
				JNAUserNamesListAllocations namesListAllocations = (JNAUserNamesListAllocations) namesList.getAdapter(APIObjectAllocations.class);
				hNamesList = namesListAllocations.getHandle();
			}
		}
		else {
			hNamesList = null;
		}

		return LockUtil.lockHandles(getAllocations().getDBHandle(), filterIDTableHandle, hNamesList, (hDbByVal,
				filterIDTableHandleByVal, hNamesListByVal) -> {
			long t0=System.currentTimeMillis();

			DHANDLE.ByReference rethSearch = DHANDLE.newInstanceByReference();
			
			short result = NotesCAPI.get().FTOpenSearch(rethSearch);
			NotesErrorUtils.checkResult(result);
			
			Memory queryLMBCS = NotesStringUtils.toLMBCS(query, true);
			IntByReference retNumDocs = new IntByReference();
			DHANDLE.ByReference rethResults = DHANDLE.newInstanceByReference();
			
			DHANDLE.ByReference rethStrings = DHANDLE.newInstanceByReference();
			IntByReference retNumHits = new IntByReference();
			short arg = 0;
			DHANDLE.ByValue hColl = null;

			short countAsShort = (short) count;
			
			result = NotesCAPI.get().FTSearchExt(hDbByVal, 
					rethSearch, hColl,
					queryLMBCS, searchOptionsBitMask,
					limitAsShort,
					filterIDTableHandleByVal,
							retNumDocs, rethStrings, rethResults, retNumHits, start, countAsShort, arg, hNamesListByVal);
			
			long t1=System.currentTimeMillis();

			if (result==3874) { //no documents found
				return LockUtil.lockHandle(rethSearch, (rethSearchByVal) -> {
					short closeResult = NotesCAPI.get().FTCloseSearch(rethSearchByVal);
					NotesErrorUtils.checkResult(closeResult);
					return new JNAFTQueryResult(JNADatabase.this, new JNAIDTable(JNADatabase.this.getParentDominoClient()), 0, 0, null, null, t1-t0);
				});
			}
			NotesErrorUtils.checkResult(result);

			List<String> highlightStrings = null;


			if (searchOptionsToUse.contains(FTQuery.RETURN_HIGHLIGHT_STRINGS)) {
				//decode highlights
				if (!rethStrings.isNull()) {
					highlightStrings = LockUtil.lockHandle(rethStrings, (rethStringsByVal) -> {
						if (rethStringsByVal!=null) {
							Pointer ptr = Mem.OSLockObject(rethStringsByVal);
							try {
								short varLength = ptr.getShort(0);
								ptr = ptr.share(2);
								short flags = ptr.getShort(0);
								ptr = ptr.share(2);

								String strHighlights = NotesStringUtils.fromLMBCS(ptr, varLength & 0xffff);

								List<String> strings = new ArrayList<>();
								
								StringTokenizerExt st = new StringTokenizerExt(strHighlights, "\n"); //$NON-NLS-1$
								while (st.hasMoreTokens()) {
									String currToken = st.nextToken();
									if (!StringUtil.isEmpty(currToken)) {
										strings.add(currToken);
									}
								}
								
								return strings;
							}
							finally {
								Mem.OSUnlockObject(rethStringsByVal);
								Mem.OSMemFree(rethStringsByVal);
							}
						}
						return null;
					});
				}
			}
			
			JNAIDTable resultsIdTable = null;
			List<NoteIdWithScore> matchesWithScore = null;
			
			if (searchOptionsToUse.contains(FTQuery.RETURN_IDTABLE)) {
				if (!rethResults.isNull()) {
					resultsIdTable = LockUtil.lockHandle(rethResults, (rethResultsByVal) -> {
						return new JNAIDTable(JNADatabase.this.getParentDominoClient(), rethResultsByVal, false);
					});
				}
				else {
					resultsIdTable = new JNAIDTable(JNADatabase.this.getParentDominoClient());
				}
			}
			else {
				if (!rethResults.isNull()) {
					matchesWithScore = LockUtil.lockHandle(rethResults, (rethResultsByVal) -> {
						Pointer ptr = Mem.OSLockObject(rethResultsByVal);
						try {
							return FTSearchResultsDecoder.decodeNoteIdsWithStoreSearchResult(ptr, searchOptionsToUse);
						}
						finally {
							Mem.OSUnlockObject(rethResultsByVal);
							Mem.OSMemFree(rethResultsByVal);
						}
					});
				}
			}
			
			result = LockUtil.lockHandle(rethSearch, (rethSearchByVal) -> {
				return NotesCAPI.get().FTCloseSearch(rethSearchByVal);
			});
			NotesErrorUtils.checkResult(result);
			
			return new JNAFTQueryResult(JNADatabase.this, resultsIdTable, retNumDocs.getValue(), retNumHits.getValue(), highlightStrings,
					matchesWithScore, t1-t0);
		});
	}

	@Override
	public IDTable getAllNoteIds(Set<DocumentClass> docClasses, boolean includeDeletions) {
		return getModifiedNoteIds(docClasses, null, includeDeletions);
	}
	
	public static class JNAModifiedNoteInfo implements ModifiedNoteInfo {
		private int noteId;
		private String unid;
		private DominoDateTime seqTime;
		private String threadRootUnid;
		private boolean isDeleted;
		private byte[] entryData;
		
		@Override
		public int getNoteId() {
			return noteId;
		}
		
		@Override
		public String getUNID() {
			return unid;
		}
		
		@Override
		public DominoDateTime getSeqTime() {
			return seqTime;
		}
		
		@Override
		public String getThreadRootUNID() {
			return threadRootUnid;
		}
		
		@Override
		public boolean isDeleted() {
			return isDeleted;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public <T> T getAdapter(Class<T> clazz) {
			if (byte[].class == clazz) {
				return (T) entryData;
			}

			return null;
		}
		
		@Override
		public String toString() {
			return MessageFormat.format(
				"JNAModifiedNoteInfo [noteId={0}, unid={1}, seqTime={2}, threadRootUnid={3}, isDeleted={4}]", //$NON-NLS-1$
				noteId, unid, seqTime, threadRootUnid, isDeleted
			);
		}
		
	}

	@Override
	public ModifiedNoteInfos getModifiedNotesInfo(Set<DocumentClass> docClasses, 
			Set<ModifiedNotesInfoFlags> infoRequested,
			final TemporalAccessor sinceParam, boolean includeDeletions, TemporalAccessor optUntilParam) {
		
		checkDisposed();
		
		List<ModifiedNoteInfo> retList = new ArrayList<>();
		
		short docClassMask = DominoEnumUtil.toBitField(DocumentClass.class, docClasses);
		int infoRequestedMask = DominoEnumUtil.toBitField(ModifiedNotesInfoFlags.class, infoRequested);
		
		DominoDateTime since;
		if (sinceParam==null) {
			if (includeDeletions) {
				since = JNADominoDateTime.createWildcardDateTime();
			}
			else {
				since = JNADominoDateTime.createMinimumDateTime();
			}
		}
		else {
			since = JNADominoDateTime.from(sinceParam);
		}

		NotesTimeDateStruct sinceStruct = NotesTimeDateStruct.newInstance(since.getAdapter(int[].class));
		NotesTimeDateStruct.ByValue sinceStructByVal = NotesTimeDateStruct.ByValue.newInstance();
		sinceStructByVal.Innards[0] = sinceStruct.Innards[0];
		sinceStructByVal.Innards[1] = sinceStruct.Innards[1];

		IntByReference retArrayCount = new IntByReference();
		NotesTimeDateStruct.ByReference retUntilStruct = NotesTimeDateStruct.newInstanceByReference();
		if (optUntilParam!=null) {
			infoRequestedMask |= NotesConstants.DB_GET_MODIFIED_NOTES_INFO_USE_UNTIL;
			retUntilStruct.Innards = JNADominoDateTime.from(optUntilParam).getInnards();
			retUntilStruct.write();
		}
		else {
			retUntilStruct.Innards[0] = 0;
			retUntilStruct.Innards[1] = 0;
		}
		
		IntByReference retmhArray = new IntByReference();

		int fInfoRequestedMask = infoRequestedMask;
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFDbGetModifiedNotesInfo(hDbByVal, docClassMask, fInfoRequestedMask,
					sinceStructByVal, retArrayCount, retUntilStruct, retmhArray);
		});
		NotesErrorUtils.checkResult(result);
		
		try (LockedMemory mem = Mem.OSMemoryLock(retmhArray.getValue());) {
			Pointer ptr = mem.getPointer();
			int arrayCount = retArrayCount.getValue();
			
			for (int i=0; i < arrayCount; i++) {
				byte[] entryData = ptr.getByteArray(0, 80);
				
				NotesTimeDateStruct tdStruct = NotesTimeDateStruct.newInstance(ptr);
				tdStruct.read();
				DominoDateTime td = new JNADominoDateTime(tdStruct.Innards);
				ptr = ptr.share(JNANotesConstants.timeDateSize);
				
				NotesUniversalNoteIdStruct unidStruct = NotesUniversalNoteIdStruct.newInstance(ptr);
				unidStruct.read();
				String unid = unidStruct.toString();
				ptr = ptr.share(JNANotesConstants.notesUniversalNoteIdSize);
				
				short bDeleted = ptr.getShort(0);
				ptr = ptr.share(2);
				
				String threadRootUnid = null;
				
				if (infoRequested.contains(ModifiedNotesInfoFlags.THREAD_ROOT_UNID)) {
					NotesUniversalNoteIdStruct threadRootUnidStruct = NotesUniversalNoteIdStruct.newInstance(ptr);
					threadRootUnidStruct.read();
					threadRootUnid = threadRootUnidStruct.toString();
					ptr = ptr.share(JNANotesConstants.notesUniversalNoteIdSize);
				}
				else {
					threadRootUnid = null;
				}
				
				int noteID;
				if (infoRequested.contains(ModifiedNotesInfoFlags.NOTEID)) {
					noteID = ptr.getInt(0);
					ptr = ptr.share(4);
					
					/* set high bit on deletions if necessary */
					if (bDeleted==1 && !infoRequested.contains(ModifiedNotesInfoFlags.NODELETED_BIT)) {
						noteID |= NotesConstants.NOTEID_RESERVED;
					}
				}
				else {
					noteID = 0;
				}
				
				JNAModifiedNoteInfo newEntry = new JNAModifiedNoteInfo();
				newEntry.noteId = noteID;
				newEntry.isDeleted = bDeleted==1;
				newEntry.unid = unid;
				newEntry.seqTime = td;
				newEntry.threadRootUnid = threadRootUnid;
				
				newEntry.entryData = entryData;
				
				retList.add(newEntry);
			}
		}
		finally {
			Mem.OSMemoryFree(retmhArray.getValue());
		}

		DominoDateTime until = new JNADominoDateTime(retUntilStruct.Innards);
		
		return new ModifiedNoteInfos() {
			@Override
			public List<ModifiedNoteInfo> getInfos() {
				return retList;
			}
			
			@Override
			public DominoDateTime getUntil() {
				return until;
			}
			
			@Override
			public String toString() {
				return MessageFormat.format("JNAModifiedNoteInfos [size={0}, until={1}]", retList.size(), until); //$NON-NLS-1$
			}
		};
	}
	
	@Override
	public IDTable getModifiedNoteIds(Set<DocumentClass> docClasses, final TemporalAccessor sinceParam, boolean includeDeletions) {
		checkDisposed();
		
		short docClassMask = DominoEnumUtil.toBitField(DocumentClass.class, docClasses);

		DominoDateTime since;
		if (sinceParam==null) {
			if (includeDeletions) {
				since = JNADominoDateTime.createWildcardDateTime();
			}
			else {
				since = JNADominoDateTime.createMinimumDateTime();
			}
		}
		else {
			since = JNADominoDateTime.from(sinceParam);
		}
		
		NotesTimeDateStruct.ByReference retUntilStruct = NotesTimeDateStruct.newInstanceByReference();
		
		NotesTimeDateStruct sinceStruct = NotesTimeDateStruct.newInstance(since.getAdapter(int[].class));
		NotesTimeDateStruct.ByValue sinceStructByVal = NotesTimeDateStruct.ByValue.newInstance();
		sinceStructByVal.Innards[0] = sinceStruct.Innards[0];
		sinceStructByVal.Innards[1] = sinceStruct.Innards[1];
		
		sinceStructByVal.write();

		return LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			DHANDLE.ByReference rethTable = DHANDLE.newInstanceByReference();
			rethTable.clear();
			
			short result = NotesCAPI.get().NSFDbGetModifiedNoteTable(hDbByVal, docClassMask, sinceStructByVal, 
					retUntilStruct, rethTable);
			
			if (result == INotesErrorConstants.ERR_NO_MODIFIED_NOTES) {
				return new JNAIDTable(JNADatabase.this.getParentDominoClient());
			}
			else {
				NotesErrorUtils.checkResult(result);
			}

			retUntilStruct.read();
			
			if (rethTable.isNull()) {
				return getParentDominoClient().createIDTable();
			}
			
			JNAIDTable idTable = new JNAIDTable(JNADatabase.this.getParentDominoClient(), rethTable, false);
			
			if (sinceParam==null) {
				DominoDateTime retUntil = new JNADominoDateTime(retUntilStruct.Innards);
				idTable.setDateTime(retUntil);
				
				//all documents requested; we already handle returning delete note ids with wildcard/minimum date
				return idTable;
			}
			else {
				if (includeDeletions) {
					DominoDateTime retUntil = new JNADominoDateTime(retUntilStruct.Innards);
					idTable.setDateTime(retUntil);
					
					//returning deletions as well is ok
					return idTable;
				}
				else {
					//check highest noteids for deletion bit
					int lastId = idTable.getLastId();
					if ((lastId & IDTable.NOTEID_FLAG_DELETED) != IDTable.NOTEID_FLAG_DELETED) {
						//no deletions anyway
						return idTable;
					}
					else {
						//remove deleted notes from the idtable
						JNAIDTable idTableNoDeletions = new JNAIDTable(JNADatabase.this.getParentDominoClient());
						for (Integer currNoteId : idTable) {
							if ((currNoteId & IDTable.NOTEID_FLAG_DELETED) != IDTable.NOTEID_FLAG_DELETED) {
								idTableNoDeletions.add(currNoteId);
							}
						}
						idTable.dispose();
						
						DominoDateTime retUntil = new JNADominoDateTime(retUntilStruct.Innards);
						idTableNoDeletions.setDateTime(retUntil);

						return idTableNoDeletions;
					}
				}
			}
		});

	}
	
	@Override
	public FormulaQueryResult queryFormula(String selectionFormula, IDTable filter, Set<SearchFlag> searchFlags,
			TemporalAccessor since, Set<DocumentClass> docClass) {

		JNAIDTable filterIdTable;
		if (filter==null) {
			filterIdTable = null;
		}
		else if (filter instanceof JNAIDTable) {
			filterIdTable = (JNAIDTable) filter;
		}
		else {
			filterIdTable = new JNAIDTable(getParentDominoClient(), filter);
		}
		
		JNAIDTable matchesIDTable = new JNAIDTable(getParentDominoClient());
		List<SearchMatch> matches = new ArrayList<>();
		List<SearchMatch> nonMatches = new ArrayList<>();
		List<SearchMatch> deletions = new ArrayList<>();
		
		JNADominoDateTime until = NotesSearch.search(this, filterIdTable, selectionFormula, "-", searchFlags, //$NON-NLS-1$
				docClass, JNADominoDateTime.from(since), new NotesSearch.SearchCallback() {

			@Override
			public Action noteFound(JNADatabase parentDb, JNASearchMatch searchMatch, IItemTableData summaryBufferData) {
				matchesIDTable.add(searchMatch.getNoteID());
				matches.add(searchMatch);

				return Action.Continue;
			}

			@Override
			public Action noteFoundNotMatchingFormula(JNADatabase parentDb, JNASearchMatch searchMatch,
					IItemTableData summaryBufferData) {
				nonMatches.add(searchMatch);

				return Action.Continue;
			}

			@Override
			public Action deletionStubFound(JNADatabase parentDb, JNASearchMatch searchMatch,
					IItemTableData summaryBufferData) {

				deletions.add(searchMatch);

				return Action.Continue;
			}

		});

		matchesIDTable.setDateTime(until);
		
		return new JNAFormulaQueryResult(this, matchesIDTable, matches, nonMatches, deletions);
	}
	
	@Override
	public DominoDateTime queryFormula(String selectionFormula, Set<Integer> filter, Set<SearchFlag> searchFlags,
			TemporalAccessor since, Set<DocumentClass> docClass, Map<String, String> computeValues,
			FormulaQueryCallback callback) {

		JNAIDTable filterIdTable;
		if (filter==null) {
			filterIdTable = null;
		}
		else if (filter instanceof JNAIDTable) {
			filterIdTable = (JNAIDTable) filter;
		}
		else {
			filterIdTable = new JNAIDTable(getParentDominoClient(), filter);
		}
		
		return NotesSearch.search(this, filterIdTable, selectionFormula, computeValues, "-", searchFlags, //$NON-NLS-1$
				docClass, JNADominoDateTime.from(since), new NotesSearch.SearchCallback() {

			@Override
			public Action noteFound(JNADatabase parentDb, JNASearchMatch searchMatch, IItemTableData summaryBufferData) {
				return callback.matchFound(JNADatabase.this, searchMatch, summaryBufferData);
			}

			@Override
			public Action noteFoundNotMatchingFormula(JNADatabase parentDb, JNASearchMatch searchMatch,
					IItemTableData summaryBufferData) {
				
				return callback.nonMatchFound(JNADatabase.this, searchMatch, summaryBufferData);
			}

			@Override
			public Action deletionStubFound(JNADatabase parentDb, JNASearchMatch searchMatch,
					IItemTableData summaryBufferData) {

				return callback.deletionFound(JNADatabase.this, searchMatch, summaryBufferData);
			}

		});
	}
	
	@Override
	public int findCollectionId(String name, CollectionType type) {
		checkDisposed();
		
		String findFlags;
		switch (type) {
		case Both:
			findFlags = NotesConstants.DFLAGPAT_VIEWS_AND_FOLDERS_DESIGN;
			break;
		case Folder:
			findFlags = NotesConstants.DFLAGPAT_FOLDER_DESIGN;
			break;
		case View:
			findFlags = NotesConstants.DFLAGPAT_VIEW_DESIGN;
			break;
			default:
				throw new IllegalArgumentException(MessageFormat.format("Unknown collection type: {0}", type));
		}
		
		Memory viewNameLMBCS = NotesStringUtils.toLMBCS(name, true);

		IntByReference collectionNoteID = new IntByReference();
		collectionNoteID.setValue(0);
		JNADatabaseAllocations allocations = getAllocations();
		
		short result = LockUtil.lockHandle(allocations.getDBHandle(), (handleByVal) -> {
			return NotesCAPI.get().NIFFindDesignNoteExt(handleByVal, viewNameLMBCS, NotesConstants.NOTE_CLASS_VIEW, NotesStringUtils.toLMBCS(findFlags, true), collectionNoteID, 0);
		});
		
		if ((result & NotesConstants.ERR_MASK)==1028) { //view not found
			return 0;
		}
		
		//throws an error if view cannot be found:
		NotesErrorUtils.checkResult(result);

		return collectionNoteID.getValue();
	}
	
	@Override
	public int createFolder(String newFolderName) {
		return createFolder((Database) null, (String) null, newFolderName);
	}

	@Override
	public int createFolder(String formatFolderName, String newFolderName) {
		return createFolder((Database) null, formatFolderName, newFolderName);
	}

	@Override
	public int createFolder(int formatFolderNoteId, String newFolderName) {
		return createFolder((Database) null, formatFolderNoteId, newFolderName);
	}

	@Override
	public int createFolder(Database formatDb, String formatFolderName, String newFolderName) {
		if (StringUtil.isEmpty(formatFolderName)) {
			return createFolder(formatDb, 0, newFolderName);
		}
		
		int formatNoteId = formatDb==null ? findCollectionId(formatFolderName, CollectionType.Folder) : formatDb.findCollectionId(formatFolderName, CollectionType.Folder);
		if (formatNoteId==0) {
			formatNoteId = formatDb==null ? findCollectionId(formatFolderName, CollectionType.View) : formatDb.findCollectionId(formatFolderName, CollectionType.View);
		}
		if (formatNoteId==0) {
			throw new DominoException(1028, MessageFormat.format("No format view/folder found with name {0}", formatFolderName));
		}
		
		return createFolder(formatDb, formatNoteId, newFolderName);
	}

	@Override
	public int createFolder(Database formatDb, int formatFolderNoteId, String newFolderName) {
		checkDisposed();
		
		if (formatDb!=null) {
			if (!(formatDb instanceof JNADatabase)) {
				throw new IncompatibleImplementationException(formatDb, JNADatabase.class);
			}
			
			JNADatabase jnaFormatDb = (JNADatabase) formatDb;
			if (jnaFormatDb.isDisposed()) {
				throw new ObjectDisposedException(jnaFormatDb);
			}
		}
		
		Memory newFolderNameMem = NotesStringUtils.toLMBCS(newFolderName, false);
		if (newFolderNameMem.size() > NotesConstants.DESIGN_FOLDER_MAX_NAME) {
			throw new IllegalArgumentException(MessageFormat.format("Folder name too long (max {0} bytes, found {1} bytes)", NotesConstants.DESIGN_FOLDER_MAX_NAME, newFolderNameMem.size()));
		}

		short newFolderNameLength = (short) (newFolderNameMem.size() & 0xffff);
		
		IntByReference retNoteId = new IntByReference();
		
		HANDLE formatDbHandle = formatDb==null ? null : formatDb.getAdapter(HANDLE.class);
		
		short result = LockUtil.lockHandles(getAllocations().getDBHandle(),
				formatDbHandle, (hDbByVal, hFormatDbByVal) -> {
					
			return NotesCAPI.get().FolderCreate(hDbByVal, hDbByVal, formatFolderNoteId,
					hFormatDbByVal, newFolderNameMem, newFolderNameLength,
							NotesConstants.DESIGN_TYPE_SHARED, 0, retNoteId);
		});
		NotesErrorUtils.checkResult(result);

		return retNoteId.getValue();
	}

	@Override
	public void deleteFolder(String folderName) {
		int folderNoteId = findCollectionId(folderName, CollectionType.Folder);
		if (folderNoteId==0) {
			throw new DominoException(1028, MessageFormat.format("No source folder found with name {0}", folderName));
		}
		deleteFolder(folderNoteId);
	}

	@Override
	public void deleteFolder(int folderNoteId) {
		if (folderNoteId==0) {
			throw new IllegalArgumentException("Folder note id cannot be 0");
		}
		
		checkDisposed();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().FolderDelete(hDbByVal, hDbByVal, folderNoteId, 0);
		});
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public void moveFolder(String folderName, String newParentFolderName) {
		int folderNoteId = findCollectionId(folderName, CollectionType.Folder);
		if (folderNoteId==0) {
			throw new DominoException(1028, MessageFormat.format("No folder found with name {0}", folderName));
		}

		int newParentFolderNoteId = findCollectionId(newParentFolderName, CollectionType.Folder);
		if (newParentFolderNoteId==0) {
			throw new DominoException(1028, MessageFormat.format("No folder found with name {0}", newParentFolderName));
		}

		moveFolder(folderNoteId, newParentFolderNoteId);
	}

	@Override
	public void moveFolder(int folderNoteId, int newParentFolderNoteId) {
		if (folderNoteId==0) {
			throw new IllegalArgumentException("Folder note id cannot be 0");
		}
		if (newParentFolderNoteId==0) {
			throw new IllegalArgumentException("Target folder note id cannot be 0");
		}

		checkDisposed();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().FolderMove(hDbByVal, null, folderNoteId, null,
					newParentFolderNoteId, 0);
		});
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public void renameFolder(String oldFolderName, String newFolderName) {
		int folderNoteId = findCollectionId(oldFolderName, CollectionType.Folder);
		if (folderNoteId==0) {
			throw new DominoException(1028, MessageFormat.format("No folder found with name {0}", oldFolderName));
		}
		
		renameFolder(folderNoteId, newFolderName);
	}

	@Override
	public void renameFolder(int oldFolderNoteId, String newFolderName) {
		if (oldFolderNoteId==0) {
			throw new IllegalArgumentException("Folder note id cannot be 0");
		}
		
		checkDisposed();
		
		Memory pszName = NotesStringUtils.toLMBCS(newFolderName, false);
		if (pszName.size() > NotesConstants.DESIGN_FOLDER_MAX_NAME) {
			throw new IllegalArgumentException(MessageFormat.format("Folder name too long (max {0} bytes, found {1} bytes)", NotesConstants.DESIGN_FOLDER_MAX_NAME, pszName.size()));
		}
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().FolderRename(hDbByVal, null, oldFolderNoteId, pszName, (short) pszName.size(), 0);
		});
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public int copyFolder(String sourceFolderName, String newFolderName) {
		int sourceFolderNoteId = findCollectionId(sourceFolderName, CollectionType.Folder);
		if (sourceFolderNoteId==0) {
			throw new DominoException(1028, MessageFormat.format("No source folder found with name {0}", sourceFolderName));
		}
		return copyFolder(sourceFolderNoteId, newFolderName);
	}

	@Override
	public int copyFolder(int sourceFolderNoteId, String newFolderName) {
		if (sourceFolderNoteId==0) {
			throw new IllegalArgumentException("Source folder note id cannot be 0");
		}
		
		checkDisposed();
		
		Memory newFolderNameMem = NotesStringUtils.toLMBCS(newFolderName, false);
		if (newFolderNameMem.size() > NotesConstants.DESIGN_FOLDER_MAX_NAME) {
			throw new IllegalArgumentException(MessageFormat.format("Folder name too long (max {0} bytes, found {1} bytes)", NotesConstants.DESIGN_FOLDER_MAX_NAME, newFolderNameMem.size()));
		}

		short newFolderNameLength = (short) (newFolderNameMem.size() & 0xffff);

		IntByReference retNewNoteId = new IntByReference();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().FolderCopy(hDbByVal, hDbByVal, sourceFolderNoteId,
					newFolderNameMem, newFolderNameLength, 0, retNewNoteId);
		});
		NotesErrorUtils.checkResult(result);
		
		return retNewNoteId.getValue();
	}

	@Override
	public void addToFolder(String folderName, Collection<Integer> noteIds) {
		checkDisposed();
		
		int folderNoteId = findCollectionId(folderName, CollectionType.Folder);
		if (folderNoteId==0) {
			throw new DominoException(1028, MessageFormat.format("No folder found with name {0}", folderName));
		}

		addToFolder(folderNoteId, noteIds);
	}

	@Override
	public void addToFolder(int folderNoteId, Collection<Integer> noteIds) {
		checkDisposed();
		
		JNAIDTable idTable;
		boolean disposeTable = false;
		
		if (noteIds instanceof JNAIDTable) {
			idTable = (JNAIDTable) noteIds;
			if (idTable.isDisposed()) {
				throw new ObjectDisposedException(idTable);
			}
		}
		else {
			idTable = getParentDominoClient().createIDTable();
			idTable.addAll(noteIds);
			disposeTable = true;
		}
		
		JNAIDTableAllocations idTableAllocations = (JNAIDTableAllocations) idTable.getAdapter(APIObjectAllocations.class);

		short result = LockUtil.lockHandles(
				getAllocations().getDBHandle(), idTableAllocations.getIdTableHandle(),
				(hDbByVal, hIdTableByVal) -> {
					return NotesCAPI.get().FolderDocAdd(hDbByVal, (HANDLE.ByValue) null, folderNoteId,
							hIdTableByVal, 0);
				}
				);

		NotesErrorUtils.checkResult(result);
		
		if (disposeTable) {
			idTable.dispose();
		}
	}

	@Override
	public void removeFromFolder(String folderName, Collection<Integer> idTable) {
		checkDisposed();
		
		int folderNoteId = findCollectionId(folderName, CollectionType.Folder);
		if (folderNoteId==0) {
			throw new DominoException(1028, MessageFormat.format("No folder found with name {0}", folderName));
		}
		removeFromFolder(folderNoteId, idTable);
	}

	@Override
	public void removeFromFolder(int folderNoteId, Collection<Integer> noteIds) {
		checkDisposed();
		
		JNAIDTable idTable;
		boolean disposeTable = false;
		
		if (noteIds instanceof JNAIDTable) {
			idTable = (JNAIDTable) noteIds;
			if (idTable.isDisposed()) {
				throw new ObjectDisposedException(idTable);
			}
		}
		else {
			idTable = getParentDominoClient().createIDTable();
			idTable.addAll(noteIds);
			disposeTable = true;
		}

		JNAIDTableAllocations idTableAllocations = (JNAIDTableAllocations) idTable.getAdapter(APIObjectAllocations.class);

		short result = LockUtil.lockHandles(
				getAllocations().getDBHandle(), idTableAllocations.getIdTableHandle(),
				(hDbByVal, hIdTableByVal) -> {

					return NotesCAPI.get().FolderDocRemove(hDbByVal, null, folderNoteId, hIdTableByVal, 0);
				}
				);

		NotesErrorUtils.checkResult(result);

		if (disposeTable) {
			idTable.dispose();
		}
	}

	@Override
	public void removeAllFromFolder(int folderNoteId) {
		checkDisposed();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().FolderDocRemoveAll(hDbByVal, null, folderNoteId, 0);
		});
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public void removeAllFromFolder(String folderName) {
		checkDisposed();
		
		int folderNoteId = findCollectionId(folderName, CollectionType.Folder);
		if (folderNoteId==0) {
			throw new DominoException(1028, MessageFormat.format("No folder found with name {0}", folderName));
		}
		removeAllFromFolder(folderNoteId);
	}

	@Override
	public int getFolderDocCount(String folderName) {
		checkDisposed();
		
		int folderNoteId = findCollectionId(folderName, CollectionType.Folder);
		if (folderNoteId==0) {
			throw new DominoException(1028, MessageFormat.format("No folder found with name {0}", folderName));
		}
		return getFolderDocCount(folderNoteId);
	}

	@Override
	public int getFolderDocCount(int folderNoteId) {
		checkDisposed();
		
		IntByReference pdwNumDocs = new IntByReference();

		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().FolderDocCount(hDbByVal, null, folderNoteId, 0, pdwNumDocs);
		});
		
		NotesErrorUtils.checkResult(result);
		return pdwNumDocs.getValue();
	}

	@Override
	public IDTable getIDTableForFolder(String folderName, boolean validateIds) {
		checkDisposed();
		
		int folderNoteId = findCollectionId(folderName, CollectionType.Folder);
		if (folderNoteId==0) {
			throw new DominoException(1028, MessageFormat.format("No folder found with name {0}", folderName));
		}

		return getIDTableForFolder(folderNoteId, validateIds);
	}
	
	@Override
	public IDTable getIDTableForFolder(int folderNoteId, boolean validateIds) {
		checkDisposed();

		DHANDLE.ByReference rethTable = DHANDLE.newInstanceByReference();

		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFFolderGetIDTable(hDbByVal, hDbByVal, folderNoteId, validateIds ? NotesConstants.DB_GETIDTABLE_VALIDATE : 0, rethTable);
		});
		NotesErrorUtils.checkResult(result);
		
		return new JNAIDTable(getParentDominoClient(), rethTable, false); 
	}
	
	@Override
	public JNADbDesign getDesign() {
		checkDisposed();
		
		return new JNADbDesign(this);
	}

	private String m_cachedUnreadTableUsernameCanonical;
	private JNAIDTable m_cachedUnreadTable;
	
	@Override
	public boolean isDocumentUnread(String userNameParam, int noteId) {
		String userName = StringUtil.isEmpty(userNameParam) ? getParentDominoClient().getEffectiveUserName() : userNameParam;
		String userNameCanonical = NotesNamingUtils.toCanonicalName(userName);
		
		if (
				StringUtil.isEmpty(m_cachedUnreadTableUsernameCanonical) ||
				m_cachedUnreadTable==null ||
				m_cachedUnreadTable.isDisposed() ||
				!NotesNamingUtils.equalNames(userNameCanonical, m_cachedUnreadTableUsernameCanonical)) {
		
			if (m_cachedUnreadTable!=null) {
				m_cachedUnreadTable.dispose();
				m_cachedUnreadTable = null;
			}
			
			m_cachedUnreadTable = (JNAIDTable) getUnreadDocumentTable(userNameCanonical, true, true).orElse(null);
			m_cachedUnreadTableUsernameCanonical = userNameCanonical;
		}
		
		return m_cachedUnreadTable != null && m_cachedUnreadTable.contains(noteId);
	}
	
	@Override
	public Optional<IDTable> getUnreadDocumentTable(String userNameParam, boolean createIfNotAvailable, boolean updateUnread) {
		checkDisposed();
		
		String userName = StringUtil.isEmpty(userNameParam) ? getParentDominoClient().getEffectiveUserName() : userNameParam;
		String userNameCanonical = NotesNamingUtils.toCanonicalName(userName);
		
		Memory userNameCanonicalMem = NotesStringUtils.toLMBCS(userNameCanonical, false);
		if (userNameCanonicalMem.size() > 65535) {
			throw new IllegalArgumentException("Username exceeds max length of 65535 bytes");
		}
		short userNameLength = (short) (userNameCanonicalMem.size() & 0xffff);
		
		DHANDLE.ByReference rethUnreadList = DHANDLE.newInstanceByReference();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFDbGetUnreadNoteTable2(hDbByVal, userNameCanonicalMem, userNameLength,
					createIfNotAvailable, updateUnread, rethUnreadList);
			
		});
		NotesErrorUtils.checkResult(result);
		
		if (rethUnreadList.isNull()) {
			return Optional.empty();
		}
		else {
			//make the cached ID table reflect the latest DB changes
			result = LockUtil.lockHandles(getAllocations().getDBHandle(), rethUnreadList,
					(hDbByVal, rethUnreadListByVal) -> {
				return NotesCAPI.get().NSFDbUpdateUnread(hDbByVal, rethUnreadListByVal);
			});
			NotesErrorUtils.checkResult(result);
			
			return Optional.of(new JNAIDTable(getParentDominoClient(), rethUnreadList, false));
		}
	}
	
	@Override
	public void updateUnreadDocumentTable(String userNameParam, Set<Integer> noteIdToMarkRead,
			Set<Integer> noteIdsToMarkUnread) {
		
		checkDisposed();
		
		String userName = StringUtil.isEmpty(userNameParam) ? getParentDominoClient().getEffectiveUserName() : userNameParam;
		String userNameCanonical = NotesNamingUtils.toCanonicalName(userName);

		Memory userNameCanonicalMem = NotesStringUtils.toLMBCS(userNameCanonical, false);
		if (userNameCanonicalMem.size() > 65535) {
			throw new IllegalArgumentException("Username exceeds max length of 65535 bytes");
		}
		short userNameLength = (short) (userNameCanonicalMem.size() & 0xffff);

		JNAIDTable unreadTable = (JNAIDTable) getUnreadDocumentTable(userNameCanonical, true, true).orElse(null);
		JNAIDTable unreadTableOrig;
		
		JNAIDTableAllocations unreadTableAllocations;
		
		if (unreadTable != null) {
			unreadTableAllocations = (JNAIDTableAllocations) unreadTable.getAdapter(APIObjectAllocations.class);
			unreadTableAllocations.checkDisposed();

			//make the cached ID table reflect the latest DB changes
			short result = LockUtil.lockHandles(getAllocations().getDBHandle(), unreadTableAllocations.getIdTableHandle(),
					(hDbByVal, hUnreadTableByVal) -> {
				return NotesCAPI.get().NSFDbUpdateUnread(hDbByVal, hUnreadTableByVal);
			});
			NotesErrorUtils.checkResult(result);
					
			unreadTableOrig = (JNAIDTable) unreadTable.clone();
		}
		else {
			unreadTable = new JNAIDTable(getParentDominoClient());
			unreadTableAllocations = (JNAIDTableAllocations) unreadTable.getAdapter(APIObjectAllocations.class);
			unreadTableAllocations.checkDisposed();

			unreadTableOrig = new JNAIDTable(getParentDominoClient());
		}
		
		if (noteIdToMarkRead != null && !noteIdToMarkRead.isEmpty()) {
			unreadTable.removeAll(noteIdToMarkRead);
		}
		
		if (noteIdsToMarkUnread != null && !noteIdsToMarkUnread.isEmpty()) {
			unreadTable.addAll(noteIdsToMarkUnread);
		}
		
		
		JNAIDTableAllocations unreadOrigTableAllocations = (JNAIDTableAllocations) unreadTableOrig.getAdapter(APIObjectAllocations.class);
		unreadOrigTableAllocations.checkDisposed();
		
		short result = LockUtil.lockHandles(
				getAllocations().getDBHandle(), unreadOrigTableAllocations.getIdTableHandle(), unreadTableAllocations.getIdTableHandle(),
				(hDbByVal, hUnreadTableOrigByVal, hUnreadTableByVal) -> {
					
			return NotesCAPI.get().NSFDbSetUnreadNoteTable(hDbByVal, userNameCanonicalMem, userNameLength,
					true, hUnreadTableOrigByVal, hUnreadTableByVal);
			
		});
		NotesErrorUtils.checkResult(result);
		
		unreadTable.dispose();
		unreadTableOrig.dispose();
		
		//remove cached unread table for this user that is used by isDocumentUnread
		if (m_cachedUnreadTable!=null &&
				NotesNamingUtils.equalNames(userNameCanonical, m_cachedUnreadTableUsernameCanonical)) {
			m_cachedUnreadTable.dispose();
			m_cachedUnreadTable = null;
			m_cachedUnreadTableUsernameCanonical = null;
		}
	}
	
	@Override
	public List<String> queryAccessRoles(String userName) {
		JNAUserNamesList namesList = null;
		try {
			namesList = NotesNamingUtils.buildNamesList(getParentDominoClient(), getServer(), userName);
			List<String> roles = getACL().lookupAccess(namesList).getRoles();
			return roles;
		}
		catch (DominoException e) {
			throw new DominoException(e.getId(), MessageFormat.format("Error computing roles for {0} on server \"{1}\"", userName, m_server), e);
		}
		finally {
			if (namesList!=null) {
				namesList.dispose();
			}
		}
	}
	
	
	@Override
	public void refreshDesign(String server) {
		refreshDesign(server, true, true, null);
	}
	
	
	@Override
	public void refreshDesign(String server, boolean force, boolean errIfTemplateNotFound, 
			final IBreakHandler abortHandler) {
		checkDisposed();
		
		Memory serverMem = NotesStringUtils.toLMBCS(server, true);
		
		NotesCallbacks.ABORTCHECKPROC abortProc;
		if (abortHandler!=null) {
			if (PlatformUtils.isWin32()) {
				abortProc = (Win32NotesCallbacks.ABORTCHECKPROCWin32) () -> {
					if (abortHandler.shouldInterrupt()==Action.Stop) {
						return INotesErrorConstants.ERR_CANCEL;
					}
					return 0;
				};
			}
			else {
				abortProc = () -> {
					if (abortHandler.shouldInterrupt()==Action.Stop) {
						return INotesErrorConstants.ERR_CANCEL;
					}
					return 0;
				};
			}
		}
		else {
			abortProc = null;
		}
		
		int dwFlags = 0;
		if (force) {
			dwFlags |= NotesConstants.DESIGN_FORCE;
		}
		if (errIfTemplateNotFound) {
			dwFlags |= NotesConstants.DESIGN_ERR_TMPL_NOT_FOUND;
		}
		
		int fDwFlags = dwFlags;
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().DesignRefresh(serverMem, hDbByVal, fDwFlags, abortProc, null);
		});
		NotesErrorUtils.checkResult(result);
	}
	
	@Override
	public void hardDeleteDocument(int softDelNoteId) {
		checkDisposed();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFNoteHardDelete(hDbByVal, softDelNoteId, 0);
		});
		NotesErrorUtils.checkResult(result);
	}
	
	@Override
	public void getModifiedTime(Ref<DominoDateTime> retDataModified, Ref<DominoDateTime> retNonDataModified) {
		checkDisposed();
		
		NotesTimeDateStruct.ByReference retDataModifiedStruct = NotesTimeDateStruct.newInstanceByReference();
		NotesTimeDateStruct.ByReference retNonDataModifiedStruct = NotesTimeDateStruct.newInstanceByReference();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFDbModifiedTime(hDbByVal, retDataModifiedStruct, retNonDataModifiedStruct);
		});
		NotesErrorUtils.checkResult(result);
		
		if (retDataModified!=null) {
			retDataModified.set(new JNADominoDateTime(retDataModifiedStruct.Innards));
		}
		if (retNonDataModified!=null) {
			retNonDataModified.set(new JNADominoDateTime(retNonDataModifiedStruct.Innards));
		}
	}
	
	@Override
	public DocumentSummaryQueryResult queryDocuments() {
		IDTable ids = getAllNoteIds(EnumSet.of(DocumentClass.DATA), false);
		return queryDocuments(ids);
	}

	@Override
	public DocumentSummaryQueryResult queryDocuments(Collection<Integer> noteIds) {
		return new JNADocumentSummaryQueryResult(this, noteIds);
	}

	@Override
	public NavigableMap<String,ItemDataType> getItemDefinitionTable() {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getAllocations();
		
		//we return a map with sorted case-insensitive keys that contain the item names:
		
		return LockUtil.lockHandle(dbAllocations.getDBHandle(), (hDbByVal) -> {
			INotesCAPI api = NotesCAPI.get();
			
			HANDLE.ByReference retItemNameTable = HANDLE.newInstanceByReference();
			short openTableResult = api.NSFDbItemDefTableExt(hDbByVal, retItemNameTable);
			NotesErrorUtils.checkResult(openTableResult);
			
			return LockUtil.lockHandle(retItemNameTable, (hItemNameTableByVal) -> {
				NavigableMap<String,ItemDataType> table = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
				
				Pointer pItemDefExt = Mem.OSLockObject(hItemNameTableByVal);
				try {
					NotesItemDefinitionTableExt itemDefTableExt = NotesItemDefinitionTableExt.newInstance(pItemDefExt);
					itemDefTableExt.read();
					
					NotesItemDefinitionTableLock itemDefTableLock = NotesItemDefinitionTableLock.newInstance();
					
					short lockTableResult = api.NSFItemDefExtLock(pItemDefExt, itemDefTableLock);
					NotesErrorUtils.checkResult(lockTableResult);
					try {
						IntByReference retNumEntries = new IntByReference();
						short accessTableDataResult = api.NSFItemDefExtEntries(itemDefTableLock, retNumEntries);
						NotesErrorUtils.checkResult(accessTableDataResult);
						
						int iNumEntries = retNumEntries.getValue();
						
						ShortByReference retItemType = new ShortByReference();
						ShortByReference retItemNameLength = new ShortByReference();
						//implemented this like the sample code for NSFDbItemDefTableExt
						//provided in the C API documentation, but it looks like the following
						//128 byte buffer is never used, instead the NSFItemDefExtGetEntry
						//call redirects the pointer stored in retItemNamePtr to
						//the item name in memory
						DisposableMemory retItemName = new DisposableMemory(128);
						DisposableMemory retItemNamePtr = new DisposableMemory(Native.POINTER_SIZE);
						retItemNamePtr.setPointer(0, retItemName);
						
						try {
							for (int i=0; i<iNumEntries; i++) {
								retItemName.clear();
								
								accessTableDataResult = api.NSFItemDefExtGetEntry(itemDefTableLock, i, retItemType,
										retItemNameLength, retItemNamePtr);
								NotesErrorUtils.checkResult(accessTableDataResult);
								
								//grab the current pointer stored in retItemNamePtr;
								//using retItemName here did not work, because the
								//value of retItemNamePtr got redirected
								Pointer ptr = retItemNamePtr.getPointer(0);
								String currItemName = NotesStringUtils.fromLMBCS(
										ptr,
										retItemNameLength.getValue() & 0xffff);
								
								short itemTypeAsShort = (short) (retItemType.getValue() & 0xffff);
								ItemDataType itemType = DominoEnumUtil.valueOf(ItemDataType.class, itemTypeAsShort)
									.orElse(ItemDataType.TYPE_UNAVAILABLE);
								table.put(currItemName, itemType);
							}
						}
						finally {
							retItemName.dispose();
							retItemNamePtr.dispose();
						}
					}
					finally {
						short unlockResult = api.NSFItemDefExtUnlock(itemDefTableExt, itemDefTableLock);
						NotesErrorUtils.checkResult(unlockResult);
					}
				}
				finally {
					Mem.OSUnlockObject(hItemNameTableByVal);
					Mem.OSMemFree(hItemNameTableByVal);
				}
				
				return table;
			});
		});
	}

	@Override
	public DocumentSelection createDocumentSelection() {
		return new JNADocumentSelection(getParentDominoClient(), this);
	}

	@Override
	public QueryResultsProcessor createQueryResultsProcessor() {
		return new JNAQueryResultsProcessor(getParentDominoClient(), this);
	}
	
	@Override
	public EncryptionInfo getLocalEncryptionInfo() {
		checkDisposed();
		
		IntByReference state = new IntByReference();
		IntByReference strength = new IntByReference();
		NotesErrorUtils.checkResult(LockUtil.lockHandle(getAllocations().getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().NSFDbLocalSecInfoGetLocal(dbHandleByVal, state, strength);
		}));
		
		return new EncryptionInfoImpl(
			DominoEnumUtil.valueOf(DatabaseEncryptionState.class, state.getValue()),
			DominoEnumUtil.valueOf(Encryption.class, strength.getValue())
		);
	}

	@Override
  public void setLocalEncryptionInfo(Encryption encryption, String userName) {
    checkDisposed();
    
    HANDLE hDb = getAllocations().getDBHandle();
    
    if (userName==null) {
      userName = ""; //$NON-NLS-1$
    }
    String userNameCanonical = NotesNamingUtils.toCanonicalName(userName);
    Memory userNameCanonicalMem = NotesStringUtils.toLMBCS(userNameCanonical, true);

    short option = NotesConstants.LSECINFOSET_MODIFY;
    if (encryption == Encryption.None) {
      option = NotesConstants.LSECINFOSET_CLEAR;
    }
    
    byte strengthAsByte = (byte) (encryption.getValue() & 0xff);
    
    short fOption = option;
    
    short result = LockUtil.lockHandle(hDb, (hDbByVal) -> {
      return NotesCAPI.get().NSFDbLocalSecInfoSet(hDbByVal, fOption, strengthAsByte, userNameCanonicalMem);
    });
    NotesErrorUtils.checkResult(result);
  }
  
	@Override
	public AccessInfo getEffectiveAccessInfo() {
		checkDisposed();
		
		ShortByReference retAccessLevel = new ShortByReference();
		ShortByReference retAccessFlag = new ShortByReference();
		LockUtil.lockHandle(getAllocations().getDBHandle(), (dbHandleByVal) -> {
			NotesCAPI.get().NSFDbAccessGet(dbHandleByVal, retAccessLevel, retAccessFlag);
			return null;
		});
		
		return new AccessInfoImpl(
			DominoEnumUtil.valueOf(AclLevel.class, retAccessLevel.getValue())
				.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Cannot identify access level for {0}", retAccessLevel.getValue()))),
			DominoEnumUtil.valuesOf(AclFlag.class, retAccessFlag.getValue())
		);
	}

	@Override
	public NSFVersionInfo getNSFVersionInfo() {
		checkDisposed();
		
		ShortByReference retMajorVersion = new ShortByReference();
		ShortByReference retMinorVersion = new ShortByReference();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().NSFDbMajorMinorVersionGet(dbHandleByVal, retMajorVersion, retMinorVersion);
		});
		NotesErrorUtils.checkResult(result);
		
		return new NSFVersionInfoImpl((int) (retMajorVersion.getValue() & 0xffff),
				(int) (retMinorVersion.getValue() & 0xffff));
	}
	
	@Override
	public BuildVersionInfo getBuildVersionInfo() {
		checkDisposed();

		NotesBuildVersionStruct retVersion = NotesBuildVersionStruct.newInstance();
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI.get().NSFDbGetMajMinVersion(dbHandleByVal, retVersion);
		});
		NotesErrorUtils.checkResult(result);
		
    ShortByReference retBuildNumber = new ShortByReference();
    
    result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
      return NotesCAPI.get().NSFDbGetBuildVersion(hDbByVal, retBuildNumber);
    });
    NotesErrorUtils.checkResult(result);
    
		retVersion.read();
		
		return new BuildVersionInfoImpl(retVersion.MajorVersion,
				retVersion.MinorVersion, retVersion.QMRNumber, retVersion.QMUNumber,
				retVersion.HotfixNumber, retVersion.Flags, retVersion.FixpackNumber,
				retBuildNumber.getValue() & 0xffff);
	}
	
	@Override
	public Optional<UserNamesList> getNamesList() {
		checkDisposed();
		
		DHANDLE.ByReference rethNamesList = DHANDLE.newInstanceByReference();
		
		short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFDbGetNamesList(hDbByVal, 0, rethNamesList);
		});
		NotesErrorUtils.checkResult(result);
		
		if (rethNamesList.isNull()) {
			return Optional.empty();
		}
		else {
			JNAUserNamesList userNamesList = new JNAUserNamesList(this, rethNamesList);
			return Optional.of(userNamesList);
		}
	}

  @Override
  public int getSpecialNoteId(DocumentClass documentClass) {
    Objects.requireNonNull(documentClass, "documentClass cannot be null");
    
    IntByReference retNoteID = new IntByReference();
    short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
      return NotesCAPI.get().NSFDbGetSpecialNoteID(hDbByVal, (short)(NotesConstants.SPECIAL_ID_NOTE | documentClass.getValue()), retNoteID);
    });
    NotesErrorUtils.checkResult(result);
    return retNoteID.getValue();
  }
	
  @Override
  public RichTextBuilder getRichTextBuilder() {
	 return new JNARichTextBuilder(this);
  }

  /**
   * Computes the $name value for ghost notes as they are written since 12.0.1 by the Java/LS API ("named documents")
   * 
   * @param name name
   * @param username username
   * @return $name value
   */
  private String computeNamedNoteValue(String name, String username) {
    if (name==null) {
      throw new IllegalArgumentException("Name parameter cannot be null");
    }
    else if (StringUtil.isEmpty(name)) {
      throw new IllegalArgumentException("Name parameter cannot be empty");
    }
    else if (name.contains("*")) { //$NON-NLS-1$
      throw new IllegalArgumentException(MessageFormat.format("Invalid character '*' in name parameter: {0}", name));
    }

    String usernameNotNull = username==null ? "" : username; //$NON-NLS-1$
    if (usernameNotNull.contains("*")) { //$NON-NLS-1$
      throw new IllegalArgumentException(MessageFormat.format("Invalid character '*' in username parameter: {0}", usernameNotNull));
    }

    return "$DGHST_" + name + "*" + usernameNotNull; //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Parses a string like "$DGHST_nameddoc*user" into
   * name/username
   * 
   * @param name name
   * @return array of [name,username] if provided name format is supported
   */
  static Optional<String[]> parseLegacyAPINamedNoteName(String name) {
    if (!name.startsWith("$DGHST_")) { //$NON-NLS-1$
      return Optional.empty();
    }
    
    String remainder = name.substring(7); //"$DGHST_".length()
    
    int iPos = remainder.indexOf("*"); //$NON-NLS-1$
    if (iPos==-1) {
      return Optional.empty();
    }
    
    String namePart = remainder.substring(0, iPos);
    String userNamePart = remainder.substring(iPos+1);
    return Optional.of(new String[] {namePart, userNamePart});
  }

  @Override
  public Document getNamedDocument(final String name, final String username) {
    if (name==null) {
      throw new IllegalArgumentException("Name parameter cannot be null");
    }
    else if (name.contains("*")) { //$NON-NLS-1$
      throw new IllegalArgumentException(MessageFormat.format("Invalid character '*' in name parameter: {0}", name));
    }
    
    String usernameNotNull = username==null ? "" : username; //$NON-NLS-1$
    if (usernameNotNull.contains("*")) { //$NON-NLS-1$
      throw new IllegalArgumentException(MessageFormat.format("Invalid character '*' in username parameter: {0}", usernameNotNull));
    }
    
    String nameAndUser = "$DGHST_" + name + "*" + usernameNotNull; //$NON-NLS-1$ //$NON-NLS-2$
    int rrv = getNamedObjectRRV(nameAndUser, NotesConstants.NONS_NAMED_NOTE);
    if (rrv!=0) {
      Document doc = getDocumentById(rrv).orElse(null);
      if (doc!=null) {
        return doc;
      }
    }
    
    Document newDoc = createDocument(EnumSet.of(CreateFlags.HIDE_FROM_VIEWS));
    newDoc.replaceItemValue("$name", nameAndUser); //$NON-NLS-1$
    return newDoc;
  }
  
  /**
   * Reads infos about named notes. Implementation is private, because there's currently no
   * usage for other namespaces than NONS_NAMED_NOTE and the risk is high to break internal NSF stuff
   * 
   * @param namespace namespace
   * @return named object infos
   */
  private List<NamedObjectInfoImpl> getNamedObjects(short namespace) {
    checkDisposed();
    
    IntByReference rethBuffer = new IntByReference();
    IntByReference retBufferLength = new IntByReference();
    
    short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
      return NotesCAPI.get().NSFGetNamedObjects(hDbByVal, namespace, rethBuffer, retBufferLength);
    });

    NotesErrorUtils.checkResult(result);
    
    int hBuffer = rethBuffer.getValue();
    if (hBuffer==0) {
      return Collections.emptyList();
    }

    try (LockedMemory lockedMem = Mem.OSMemoryLock(rethBuffer.getValue(), true);) {
      Pointer ptr = lockedMem.getPointer();
      
      int numEntries = Short.toUnsignedInt(ptr.getShort(0));
      if (numEntries==0) {
        return Collections.emptyList();
      }
      
      ptr = ptr.share(2);
      
      NamedObjectInfoImpl[] objInfos = new NamedObjectInfoImpl[numEntries];
      
      for (int i=0; i<numEntries; i++) {
        NamedObjectInfoImpl objInfo = new NamedObjectInfoImpl();
        objInfos[i] = objInfo;
        
        NamedObjectEntryStruct namedObjStruct = NamedObjectEntryStruct.newInstance(ptr);
        namedObjStruct.read();
        int structSize = namedObjStruct.size();
        
        int nameLength = Short.toUnsignedInt(namedObjStruct.NameLength);
        objInfo.setNameLength(nameLength);
        
        short currNamespace = namedObjStruct.NameSpace;
        objInfo.setNamespace(currNamespace);
        
        int noteId = namedObjStruct.NoteID;
        objInfo.setNoteId(noteId);
        
        ptr = ptr.share(structSize);
      }
      
      for (int i=0; i<numEntries; i++) {
        NamedObjectInfoImpl objInfo = objInfos[i];
        int nameLength = objInfo.getNameLength();
        String name = NotesStringUtils.fromLMBCS(ptr, nameLength);
        objInfo.setRawName(name);
        ptr = ptr.share(nameLength);
      }
      
      return Arrays.asList(objInfos);
    }
  }
  
  private static class NamedObjectInfoImpl implements NamedObjectInfo {
    private String rawName;
    private Optional<String[]> parsedName;
    private int nameLength;
    private short namespace;
    private int noteId;
    
    public String getRawName() {
      return rawName;
    }
    
    private void setRawName(String name) {
      this.rawName = name;
    }
    
    @Override
    public int getNoteID() {
      return noteId;
    }
    
    private void setNoteId(int noteId) {
      this.noteId = noteId;
    }

    public short getNamespace() {
      return namespace;
    }

    private void setNamespace(short namespace) {
      this.namespace = namespace;
    }

    private int getNameLength() {
      return nameLength;
    }

    private void setNameLength(int nameLength) {
      this.nameLength = nameLength;
    }

    @Override
    public String getNameOfDocument() {
      if (parsedName==null) {
        parsedName = parseLegacyAPINamedNoteName(getRawName());
      }
      return parsedName.map((v) -> { return v[0]; }).orElse(""); //$NON-NLS-1$
    }
    
    @Override
    public String getUserNameOfDocument() {
      if (parsedName==null) {
        parsedName = parseLegacyAPINamedNoteName(getRawName());
      }
      return parsedName.map((v) -> { return v[1]; }).orElse(""); //$NON-NLS-1$
    }
    
    @Override
    public String toString() {
      return MessageFormat.format("NamedObjectInfo [name={0}, username={1}, noteId={2}]", getNameOfDocument(), getUserNameOfDocument(), getNoteID());
    }
    
  }

  @Override
  public Collection<NamedObjectInfo> getNamedDocumentInfos(String name) {
    String prefix = StringUtil.isEmpty(name) ? "$DGHST_": computeNamedNoteValue(name, ""); //$NON-NLS-1$ //$NON-NLS-2$
    
    return getNamedObjects(NotesConstants.NONS_NAMED_NOTE)
        .stream()
        .filter((entry) -> {
          return StringUtil.startsWithIgnoreCase(entry.getRawName(), prefix);
        })
        .collect(Collectors.toList());
  }

  /**
   * Caches the database option LARGE_ITEMS_ENABLED internally to improve performance
   * 
   * @return true if large items are supported
   */
  boolean hasLargeItemSupport() {
    if (m_hasLargeItemSupport==null) {
      m_hasLargeItemSupport = getOption(DatabaseOption.LARGE_ITEMS_ENABLED);
    }
    return m_hasLargeItemSupport;
  }

  @Override
  public void createIndex(String name, Collection<String> fields, boolean isvisible, boolean nobuild) {
    checkDisposed();
    
    if (StringUtil.isEmpty(name)) {
      throw new IllegalArgumentException("Index name cannot be empty");
    }
    
    List<Memory> fieldsMem = new ArrayList<>();
    
    for (String currField : fields) {
      if (StringUtil.isEmpty(currField)) {
        throw new IllegalArgumentException(MessageFormat.format("Method does not support empty field names: {0}", fields));
      }
      
      Memory currFieldMem = NotesStringUtils.toLMBCS(currField, true);
      if (currFieldMem.size() > (NotesConstants.DESIGN_NAME_MAX-1)) {
        throw new IllegalArgumentException(MessageFormat.format("Field exceeds max length of {0}: {1}", NotesConstants.DESIGN_NAME_MAX-1, currField));
      }
      fieldsMem.add(currFieldMem);
    }

    IntByReference hdsgncmd = new IntByReference();
    
    int v0 = hdsgncmd.getValue();
    
    for (Memory currFieldMem : fieldsMem) {
      short result = NotesCAPI1201.get().NSFDesignCommandAddComponent(currFieldMem,
          NotesConstants.DESIGN_COMPONENT_ATTR.VALS_ASCENDING.getValue(),
          hdsgncmd);
      
      int v1 = hdsgncmd.getValue();
      
      NotesErrorUtils.checkResult(result);
    }

    Memory nameMem = NotesStringUtils.toLMBCS(name, true);
    
    IntByReference hretval = new IntByReference();
    IntByReference hreterror = new IntByReference();
    
    int dwFlags = 0;
    if (isvisible) {
      dwFlags |= NotesConstants.CREATE_INDEX_NOHIDE;
    }
    
    if (nobuild) {
      dwFlags |= NotesConstants.CREATE_INDEX_NOBUILD;
    }
    
    int dwFlagsFinal = dwFlags;
    
    short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
      return NotesCAPI1201.get().NSFDesignCommand(hDbByVal,
          NotesConstants.DESIGN_COMMAND_TYPE.CREATE_INDEX.getValue(),
          dwFlagsFinal, nameMem,
          hretval, hreterror, hdsgncmd.getValue());
  
    });
    
    String errorTxt = ""; //$NON-NLS-1$
    if (hreterror.getValue()!=0) {
      try (LockedMemory m = Mem.OSMemoryLock(hreterror.getValue())) {
        errorTxt = NotesStringUtils.fromLMBCS(m.getPointer(), -1);
      }
      finally {
        Mem.OSMemoryFree(hreterror.getValue());
      }
    }

    if (result!=0) {
      if (!StringUtil.isEmpty(errorTxt)) {
        throw new DominoException(result, errorTxt, NotesErrorUtils.toNotesError(result).orElse(null));
      }
      else {
        NotesErrorUtils.checkResult(result);
      }
    }
    
  }

  @Override
  public void removeIndex(String name) {
    checkDisposed();
    
    if (StringUtil.isEmpty(name)) {
      throw new IllegalArgumentException("Index name cannot be empty");
    }

    Memory nameMem = NotesStringUtils.toLMBCS(name, true);
    
    IntByReference hidx = new IntByReference();
    IntByReference hreterror = new IntByReference();

    short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
      return NotesCAPI1201.get().NSFDesignCommand(hDbByVal,
          NotesConstants.DESIGN_COMMAND_TYPE.DELETE_INDEX.getValue(), 0, nameMem,
          hidx, hreterror, 0);
    });
    
    if (hidx.getValue()!=0) {
      Mem.OSMemoryFree(hidx.getValue());
    }
    
    if ((result & NotesConstants.ERR_MASK)==1028) { //index view not found
      if (hreterror.getValue()!=0) {
        Mem.OSMemoryFree(hreterror.getValue());
      }
      return;
    }

    String errorTxt = ""; //$NON-NLS-1$
    if (hreterror.getValue()!=0) {
      try (LockedMemory m = Mem.OSMemoryLock(hreterror.getValue())) {
        errorTxt = NotesStringUtils.fromLMBCS(m.getPointer(), -1);
      }
      finally {
        Mem.OSMemoryFree(hreterror.getValue());
      }
    }

    if (result!=0) {
      if (!StringUtil.isEmpty(errorTxt)) {
        throw new DominoException(result, errorTxt, NotesErrorUtils.toNotesError(result).orElse(null));
      }
      else {
        NotesErrorUtils.checkResult(result);
      }
    }
  }

  @Override
  public String listIndexes() {
    checkDisposed();
    
    IntByReference hret = new IntByReference();
    IntByReference hreterror = new IntByReference();

    short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (hDbByVal) -> {
      return NotesCAPI1201.get().NSFDesignCommand(hDbByVal,
          NotesConstants.DESIGN_COMMAND_TYPE.LIST_INDEXES.getValue(), 0, null,
          hret, hreterror, 0);
    });
    
    String errorTxt = ""; //$NON-NLS-1$
    if (hreterror.getValue()!=0) {
      try (LockedMemory m = Mem.OSMemoryLock(hreterror.getValue())) {
        errorTxt = NotesStringUtils.fromLMBCS(m.getPointer(), -1);
      }
      finally {
        Mem.OSMemoryFree(hreterror.getValue());
      }
    }
    
    if (result!=0) {
      if (!StringUtil.isEmpty(errorTxt)) {
        throw new DominoException(result, errorTxt, NotesErrorUtils.toNotesError(result).orElse(null));
      }
      else {
        NotesErrorUtils.checkResult(result);
      }
    }
    
    String retTxt = ""; //$NON-NLS-1$
    if (hret.getValue()!=0) {
      try (LockedMemory m = Mem.OSMemoryLock(hret.getValue())) {
        retTxt = NotesStringUtils.fromLMBCS(m.getPointer(), -1);
      }
      finally {
        Mem.OSMemoryFree(hret.getValue());
      }
    }
    
    return retTxt;
  }
  
  
}
