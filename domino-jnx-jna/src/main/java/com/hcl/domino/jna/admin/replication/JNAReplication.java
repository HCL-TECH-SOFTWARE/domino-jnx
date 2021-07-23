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
package com.hcl.domino.jna.admin.replication;

import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.hcl.domino.DominoException;
import com.hcl.domino.admin.replication.GetDocumentsMode;
import com.hcl.domino.admin.replication.ReplicaInfo;
import com.hcl.domino.admin.replication.Replication;
import com.hcl.domino.admin.replication.ReplicationHistorySummary;
import com.hcl.domino.admin.replication.ReplicationHistorySummary.ReplicationDirection;
import com.hcl.domino.commons.constants.UpdateNote;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.OpenDocumentMode;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.exception.IncompatibleImplementationException;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADocumentAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAReplicationAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE64;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NotesDbReplicaInfoStruct;
import com.hcl.domino.jna.internal.structs.NotesReplicationHistorySummaryStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class JNAReplication extends BaseJNAAPIObject<JNAReplicationAllocations> implements Replication {

	public JNAReplication(IAPIObject<?> parent) {
		super(parent);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNAReplicationAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNAReplicationAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	private JNADatabase toJNADatabase(Database db) {
		if (db instanceof JNADatabase) {
			return (JNADatabase) db;
		}
		throw new IncompatibleImplementationException(db, JNADatabase.class);
	}
	
	@Override
	public void clearReplicationHistory(Database db) {
		checkDisposed();
		
		JNADatabase jnaDb = toJNADatabase(db);
		if (jnaDb.isDisposed()) {
			throw new ObjectDisposedException(jnaDb);
		}
		
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) jnaDb.getAdapter(APIObjectAllocations.class);
		
		short result = LockUtil.lockHandle(dbAllocations.getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFDbClearReplHistory(hDbByVal, 0);
		});
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public String setNewReplicaID(Database db) {
		ReplicaInfo replicaInfo = getReplicaInfo(db);
		String id = replicaInfo.setNewReplicaId();
		setReplicaInfo(db, replicaInfo);
		
		return id;
	}

	@Override
	public ReplicaInfo getReplicaInfo(Database db) {
		checkDisposed();

		JNADatabase jnaDb = toJNADatabase(db);
		
		if (jnaDb.isDisposed()) {
			throw new ObjectDisposedException(jnaDb);
		}
		
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) jnaDb.getAdapter(APIObjectAllocations.class);

		NotesDbReplicaInfoStruct retReplicationInfo = NotesDbReplicaInfoStruct.newInstance();
		short result = LockUtil.lockHandle(dbAllocations.getDBHandle(), (handleByVal) -> {
			return NotesCAPI.get().NSFDbReplicaInfoGet(handleByVal, retReplicationInfo);
		});
		NotesErrorUtils.checkResult(result);
		return new JNAReplicaInfo(jnaDb, retReplicationInfo);
	}

	@Override
	public void setReplicaInfo(Database db, ReplicaInfo replicaInfo) {
		checkDisposed();
		
		JNADatabase jnaDb = toJNADatabase(db);
		if (jnaDb.isDisposed()) {
			throw new ObjectDisposedException(jnaDb);
		}
		if (!(replicaInfo instanceof JNAReplicaInfo)) {
			throw new IncompatibleImplementationException(replicaInfo, JNAReplicaInfo.class);
		}
		JNAReplicaInfo jnaReplicaInfo = (JNAReplicaInfo) replicaInfo;
		
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) jnaDb.getAdapter(APIObjectAllocations.class);

		short result = LockUtil.lockHandle(dbAllocations.getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFDbReplicaInfoSet(hDbByVal, jnaReplicaInfo.getAdapter(NotesDbReplicaInfoStruct.class));
		});
		NotesErrorUtils.checkResult(result);
		
		//reset cached replicaId
		jnaDb._resetCachedReplicaId();
	}

	@Override
	public void saveDocument(Document doc, boolean force, boolean noRevisionHistory, boolean keepModTime) {
		if (!(doc instanceof JNADocument)) {
			throw new IncompatibleImplementationException(doc, JNADocument.class);
		}
		JNADocument jnaDoc = (JNADocument) doc;
		JNADocumentAllocations docAllocations = (JNADocumentAllocations) jnaDoc.getAdapter(APIObjectAllocations.class);
		
		Set<UpdateNote> updateFlags = EnumSet.noneOf(UpdateNote.class);
		if (force) {
			updateFlags.add(UpdateNote.FORCE);
		}
		
		int updateFlagsBitmask = DominoEnumUtil.toBitField(UpdateNote.class, updateFlags);
		
		if (noRevisionHistory) {
			// Do not maintain revision history
			updateFlagsBitmask |= 0x0100;
		}
		if (keepModTime) {
			// Do not change the modified time on save
			updateFlagsBitmask |= 0x00020000;
		}
		int fUpdateFlagsBitmask = updateFlagsBitmask;
		
		docAllocations.closeAllRichtextWriters();
		
		short result = LockUtil.lockHandle(docAllocations.getNoteHandle(), (hNoteByVal) -> {
			return NotesCAPI.get().NSFNoteUpdateExtended(hNoteByVal, fUpdateFlagsBitmask);
		});
		NotesErrorUtils.checkResult(result);
	}


	@Override
	public void getDocuments(Database db, int[] noteIds, Set<OpenDocumentMode>[] docOpenFlags, int[] sinceSeqNum,
			Set<GetDocumentsMode> controlFlags, Database objectDb, IGetDocumentsCallback getDocumentsCallback,
			IDocumentOpenCallback docOpenCallback, IObjectAllocCallback objectAllocCallback,
			IObjectWriteCallback objectWriteCallback, TemporalAccessor folderSinceTime,
			IFolderAddCallback folderAddCallback) {
		
		checkDisposed();
		
		JNADatabase jnaDb = toJNADatabase(db);
		if (jnaDb.isDisposed()) {
			throw new ObjectDisposedException(jnaDb);
		}
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) jnaDb.getAdapter(APIObjectAllocations.class);

		JNADatabase jnaObjectDb = null;
		JNADatabaseAllocations jnaObjectDbAllocations = null;
		if (objectDb!=null) {
			jnaObjectDb = toJNADatabase(objectDb);
			if (jnaObjectDb.isDisposed()) {
				throw new ObjectDisposedException(jnaObjectDb);
			}
			jnaObjectDbAllocations = (JNADatabaseAllocations) jnaObjectDb.getAdapter(APIObjectAllocations.class);
		}
		
		int controlFlagsBitMask = DominoEnumUtil.toBitField(GetDocumentsMode.class, controlFlags);

		if (noteIds.length==0) {
			return;
		}
		
		if (noteIds.length != docOpenFlags.length) {
			throw new DominoException(0, MessageFormat.format("Size of document open flags array does not match note ids array ({0}!={1})",
					docOpenFlags.length, noteIds.length));
		}
		if (noteIds.length != sinceSeqNum.length) {
			throw new DominoException(0, MessageFormat.format("Size of sinceSeqNum array does not match note ids array ({0}!={1})", sinceSeqNum.length,
					noteIds.length));
		}
		
		final NotesTimeDateStruct folderSinceTimeStruct = folderSinceTime==null ? null : NotesTimeDateStruct.newInstance(JNADominoDateTime.from(folderSinceTime).getInnards());
		
		final DisposableMemory arrNoteIdsMem = new DisposableMemory(4 * noteIds.length);
		for (int i=0; i<noteIds.length; i++) {
			arrNoteIdsMem.setInt(4*i, noteIds[i]);
		}
		final DisposableMemory arrNoteOpenFlagsMem = new DisposableMemory(4 * docOpenFlags.length);
		for (int i=0; i<docOpenFlags.length; i++) {
			arrNoteOpenFlagsMem.setInt(4*i, jnaDb._toDocumentOpenOptions(docOpenFlags[i]));
		}
		final DisposableMemory arrSinceSeqNumMem = new DisposableMemory(4 * sinceSeqNum.length);
		for (int i=0; i<sinceSeqNum.length; i++) {
			arrSinceSeqNumMem.setInt(4*i, sinceSeqNum[i]);
		}
		
		AtomicBoolean stopped = new AtomicBoolean();
		
		final Throwable[] exception = new Throwable[1];
		final NotesCallbacks.NSFGetNotesCallback cGetNotesCallback;
		final NotesCallbacks.NSFFolderAddCallback cFolderAddCallback;

		if (getDocumentsCallback!=null) {
			if (PlatformUtils.isWin32()) {
				cGetNotesCallback = (param, totalSizeLow, totalSizeHigh) -> {
					try {
						long totalSize = (long)totalSizeLow << 32 | totalSizeHigh & 0xFFFFFFFFL;
						Action action = getDocumentsCallback.gettingDocuments(totalSize);
						if (action == Action.STOP) {
							stopped.set(Boolean.TRUE);
							return INotesErrorConstants.ERR_CANCEL;
						}
						else {
							return 0;
						}
					}
					catch (Throwable t) {
						exception[0] = t;
						return INotesErrorConstants.ERR_CANCEL;
					}
				};
			}
			else {
				cGetNotesCallback = (param, totalSizeLow, totalSizeHigh) -> {
					try {
						long totalSize = (long)totalSizeLow << 32 | totalSizeHigh & 0xFFFFFFFFL;
						Action action = getDocumentsCallback.gettingDocuments(totalSize);
						if (action == Action.STOP) {
							stopped.set(Boolean.TRUE);
							return INotesErrorConstants.ERR_CANCEL;
						}
						else {
							return 0;
						}
					}
					catch (Throwable t) {
						exception[0] = t;
						return INotesErrorConstants.ERR_CANCEL;
					}
				};
			}
		}
		else {
			cGetNotesCallback=null;
		}
		
		if (folderAddCallback!=null) {
			if (PlatformUtils.isWin32()) {
				cFolderAddCallback = (param, noteUNID, opBlock, opBlockSize) -> {
					try {
						Action action = folderAddCallback.addedToFolder(noteUNID==null ? null : noteUNID.toString());
						if (action == Action.STOP) {
							stopped.set(Boolean.TRUE);
							return INotesErrorConstants.ERR_CANCEL;
						}
						else {
							return 0;
						}
					}
					catch (Throwable t) {
						exception[0] = t;
						return INotesErrorConstants.ERR_CANCEL;
					}
				};
			}
			else {
				cFolderAddCallback = (param, noteUNID, opBlock, opBlockSize) -> {
					try {
						Action action = folderAddCallback.addedToFolder(noteUNID==null ? null : noteUNID.toString());
						if (action == Action.STOP) {
							stopped.set(Boolean.TRUE);
							return INotesErrorConstants.ERR_CANCEL;
						}
						else {
							return 0;
						}
					}
					catch (Throwable t) {
						exception[0] = t;
						return INotesErrorConstants.ERR_CANCEL;
					}
				};
			}
		}
		else {
			cFolderAddCallback=null;
		}
		
		if (PlatformUtils.is64Bit()) {
			final NotesCallbacks.b64_NSFNoteOpenCallback cNoteOpenCallback;
			final NotesCallbacks.b64_NSFObjectAllocCallback cObjectAllocCallback;
			final NotesCallbacks.b64_NSFObjectWriteCallback cObjectWriteCallback;
			
			if (docOpenCallback!=null) {
				cNoteOpenCallback = (param, hNote, noteId, status) -> {
					JNADocument note;
					if (hNote==0) {
						note = null;
					}
					else {
						@SuppressWarnings("deprecation")
						DHANDLE hNoteObj = new DHANDLE64(hNote);
						note = new JNADocument(jnaDb, hNoteObj, true);
					}
					Optional<DominoException> statusEx = NotesErrorUtils.toNotesError(status);
					try {
						Action action = docOpenCallback.documentOpened(note, noteId, statusEx);
						if (action == Action.STOP) {
							stopped.set(Boolean.TRUE);
							return INotesErrorConstants.ERR_CANCEL;
						}
						else {
							return 0;
						}
					}
					catch (Throwable e) {
						exception[0] = e;
						return INotesErrorConstants.ERR_CANCEL;
					}
				};					
			}
			else {
				cNoteOpenCallback=null;
			}
			
			if (objectAllocCallback!=null) {
				cObjectAllocCallback = (param, hNote, oldRRV, status, objectSize) -> {
					JNADocument note;
					if (hNote==0) {
						note = null;
					}
					else {
						@SuppressWarnings("deprecation")
						DHANDLE hNoteObj = new DHANDLE64(hNote);
						note = new JNADocument(jnaDb, hNoteObj, true);
					}
					Optional<DominoException> statusEx = NotesErrorUtils.toNotesError(status);
					try {
						Action action = objectAllocCallback.objectAllocated(note, oldRRV, statusEx, objectSize);
						if (action == Action.STOP) {
							stopped.set(Boolean.TRUE);
							return INotesErrorConstants.ERR_CANCEL;
						}
						else {
							return 0;
						}

					}
					catch (Throwable e) {
						exception[0] = e;
						return INotesErrorConstants.ERR_CANCEL;
					}
				};
			}
			else {
				cObjectAllocCallback=null;
			}
			
			if (objectWriteCallback!=null) {
				cObjectWriteCallback = (param, hNote, oldRRV, status, buffer, bufferSize) -> {
					JNADocument note;
					if (hNote==0) {
						note = null;
					}
					else {
						@SuppressWarnings("deprecation")
						DHANDLE hNoteObj = new DHANDLE64(hNote);
						note = new JNADocument(jnaDb, hNoteObj, true);
					}
					Optional<DominoException> statusEx = NotesErrorUtils.toNotesError(status);
					
					ByteBuffer byteBuf = buffer.getByteBuffer(0, bufferSize);
					
					try {
						Action action = objectWriteCallback.objectChunkWritten(note, oldRRV, statusEx, byteBuf, bufferSize);
						if (action == Action.STOP) {
							stopped.set(Boolean.TRUE);
							return INotesErrorConstants.ERR_CANCEL;
						}
						else {
							return 0;
						}
					}
					catch (Throwable e) {
						exception[0] = e;
						return INotesErrorConstants.ERR_CANCEL;
					}
				};
			}
			else {
				cObjectWriteCallback=null;
			}
			
			short result = LockUtil.lockHandles(dbAllocations.getDBHandle(),
					(jnaObjectDbAllocations==null ? null : jnaObjectDbAllocations.getDBHandle()),
					(hDbByVal, hObjectDbByVal) -> {
				return NotesCAPI.get().NSFDbGetNotes(hDbByVal, noteIds.length, arrNoteIdsMem, arrNoteOpenFlagsMem,
						arrSinceSeqNumMem, controlFlagsBitMask, hObjectDbByVal,
								null, cGetNotesCallback, cNoteOpenCallback, cObjectAllocCallback, cObjectWriteCallback,
								folderSinceTimeStruct, cFolderAddCallback);
			});
			
			arrNoteIdsMem.dispose();
			arrNoteOpenFlagsMem.dispose();
			arrSinceSeqNumMem.dispose();

			if (exception[0]!=null) {
				throw new DominoException("Error reading documents", exception[0]);
			}
			if (!Boolean.TRUE.equals(stopped.get())) {
				NotesErrorUtils.checkResult(result);
			}
		}
		else {
			final NotesCallbacks.b32_NSFNoteOpenCallback cNoteOpenCallback;
			final NotesCallbacks.b32_NSFObjectAllocCallback cObjectAllocCallback;
			final NotesCallbacks.b32_NSFObjectWriteCallback cObjectWriteCallback;
			
			if (docOpenCallback!=null) {
				if (PlatformUtils.isWin32()) {
					cNoteOpenCallback = (param, hNote, noteId, status) -> {
						JNADocument note;
						if (hNote==0) {
							note = null;
						}
						else {
							@SuppressWarnings("deprecation")
							DHANDLE hNoteObj = new DHANDLE64(hNote);
							note = new JNADocument(jnaDb, hNoteObj, true);
						}
						Optional<DominoException> statusEx = NotesErrorUtils.toNotesError(status);
						try {
							Action action = docOpenCallback.documentOpened(note, noteId, statusEx);
							if (action == Action.STOP) {
								stopped.set(Boolean.TRUE);
								return INotesErrorConstants.ERR_CANCEL;
							}
							else {
								return 0;
							}
						}
						catch (Throwable e) {
							exception[0] = e;
							return INotesErrorConstants.ERR_CANCEL;
						}
					};
				}
				else {
					cNoteOpenCallback = (param, hNote, noteId, status) -> {
						JNADocument note;
						if (hNote==0) {
							note = null;
						}
						else {
							@SuppressWarnings("deprecation")
							DHANDLE hNoteObj = new DHANDLE64(hNote);
							note = new JNADocument(jnaDb, hNoteObj, true);
						}
						Optional<DominoException> statusEx = NotesErrorUtils.toNotesError(status);
						try {
							Action action = docOpenCallback.documentOpened(note, noteId, statusEx);
							if (action == Action.STOP) {
								stopped.set(Boolean.TRUE);
								return INotesErrorConstants.ERR_CANCEL;
							}
							else {
								return 0;
							}
						}
						catch (Throwable e) {
							exception[0] = e;
							return INotesErrorConstants.ERR_CANCEL;
						}
					};
				}
			}
			else {
				cNoteOpenCallback=null;
			}
			
			if (objectAllocCallback!=null) {
				if (PlatformUtils.isWin32()) {
					cObjectAllocCallback = (param, hNote, oldRRV, status, objectSize) -> {
						JNADocument note;
						if (hNote==0) {
							note = null;
						}
						else {
							@SuppressWarnings("deprecation")
							DHANDLE hNoteObj = new DHANDLE64(hNote);
							note = new JNADocument(jnaDb, hNoteObj, true);
						}
						Optional<DominoException> statusEx = NotesErrorUtils.toNotesError(status);
						try {
							Action action = objectAllocCallback.objectAllocated(note, oldRRV, statusEx, objectSize);
							if (action == Action.STOP) {
								stopped.set(Boolean.TRUE);
								return INotesErrorConstants.ERR_CANCEL;
							}
							else {
								return 0;
							}
						}
						catch (Throwable e) {
							exception[0] = e;
							return INotesErrorConstants.ERR_CANCEL;
						}
					};
				}
				else {
					cObjectAllocCallback = (param, hNote, oldRRV, status, objectSize) -> {
						JNADocument note;
						if (hNote==0) {
							note = null;
						}
						else {
							@SuppressWarnings("deprecation")
							DHANDLE hNoteObj = new DHANDLE64(hNote);
							note = new JNADocument(jnaDb, hNoteObj, true);
						}
						Optional<DominoException> statusEx = NotesErrorUtils.toNotesError(status);
						try {
							Action action = objectAllocCallback.objectAllocated(note, oldRRV, statusEx, objectSize);
							if (action == Action.STOP) {
								stopped.set(Boolean.TRUE);
								return INotesErrorConstants.ERR_CANCEL;
							}
							else {
								return 0;
							}
						}
						catch (Throwable e) {
							exception[0] = e;
							return INotesErrorConstants.ERR_CANCEL;
						}
					};
				}
			}
			else {
				cObjectAllocCallback=null;
			}
			
			if (objectWriteCallback!=null) {
				if (PlatformUtils.isWin32()) {
					cObjectWriteCallback = (param, hNote, oldRRV, status, buffer, bufferSize) -> {
						JNADocument note;
						if (hNote==0) {
							note = null;
						}
						else {
							@SuppressWarnings("deprecation")
							DHANDLE hNoteObj = new DHANDLE64(hNote);
							note = new JNADocument(jnaDb, hNoteObj, true);
						}
						Optional<DominoException> statusEx = NotesErrorUtils.toNotesError(status);
						ByteBuffer byteBuf = buffer.getByteBuffer(0, bufferSize);
						
						try {
							Action action = objectWriteCallback.objectChunkWritten(note, oldRRV, statusEx, byteBuf, bufferSize);
							if (action == Action.STOP) {
								stopped.set(Boolean.TRUE);
								return INotesErrorConstants.ERR_CANCEL;
							}
							else {
								return 0;
							}
						}
						catch (Throwable e) {
							exception[0] = e;
							return INotesErrorConstants.ERR_CANCEL;
						}
					};
				}
				else {
					cObjectWriteCallback = (param, hNote, oldRRV, status, buffer, bufferSize) -> {
						JNADocument note;
						if (hNote==0) {
							note = null;
						}
						else {
							@SuppressWarnings("deprecation")
							DHANDLE hNoteObj = new DHANDLE64(hNote);
							note = new JNADocument(jnaDb, hNoteObj, true);
						}
						Optional<DominoException> statusEx = NotesErrorUtils.toNotesError(status);
						ByteBuffer byteBuf = buffer.getByteBuffer(0, bufferSize);
						
						try {
							Action action = objectWriteCallback.objectChunkWritten(note, oldRRV, statusEx, byteBuf, bufferSize);
							if (action == Action.STOP) {
								stopped.set(Boolean.TRUE);
								return INotesErrorConstants.ERR_CANCEL;
							}
							else {
								return 0;
							}
						}
						catch (Throwable e) {
							exception[0] = e;
							return INotesErrorConstants.ERR_CANCEL;
						}
					};
				}
			}
			else {
				cObjectWriteCallback=null;
			}
			
			short result = LockUtil.lockHandles(dbAllocations.getDBHandle(),
					(jnaObjectDbAllocations==null ? null : jnaObjectDbAllocations.getDBHandle()),
					(hDbByVal, hObjectDbByVal) -> {
				return NotesCAPI.get().NSFDbGetNotes(hDbByVal, noteIds.length, arrNoteIdsMem, arrNoteOpenFlagsMem,
						arrSinceSeqNumMem, controlFlagsBitMask, hObjectDbByVal,
								null, cGetNotesCallback, cNoteOpenCallback, cObjectAllocCallback, cObjectWriteCallback,
								folderSinceTimeStruct, cFolderAddCallback);
			});
			
			arrNoteIdsMem.dispose();
			arrNoteOpenFlagsMem.dispose();
			arrSinceSeqNumMem.dispose();

			if (exception[0]!=null) {
				throw new DominoException("Error reading documents", exception[0]);
			}
			if (!Boolean.TRUE.equals(stopped.get())) {
				NotesErrorUtils.checkResult(result);
			}
		}
	
	}

	
	@Override
	public List<ReplicationHistorySummary> getReplicationHistory(Database db, Set<ReplicationHistoryFlags> flags) {
		checkDisposed();
		
		JNADatabase jnaDb = toJNADatabase(db);
		if (jnaDb.isDisposed()) {
			throw new ObjectDisposedException(jnaDb);
		}
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) jnaDb.getAdapter(APIObjectAllocations.class);
		
		DHANDLE.ByReference rethSummary = DHANDLE.newInstanceByReference();
		IntByReference retNumEntries = new IntByReference();
		
		int dwFlags = DominoEnumUtil.toBitField(ReplicationHistoryFlags.class, flags);
		
		short result = LockUtil.lockHandle(dbAllocations.getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().NSFDbGetReplHistorySummary(hDbByVal, dwFlags, rethSummary, retNumEntries);
		});
		NotesErrorUtils.checkResult(result);
		
		return LockUtil.lockHandle(rethSummary, (rethSummaryByVal) -> {
			List<ReplicationHistorySummary> history = new ArrayList<>();
			
			if (!rethSummary.isNull()) {
				int numEntries = retNumEntries.getValue();
				Pointer ptr = Mem.OSLockObject(rethSummaryByVal);
				try {
					Pointer currPosPtr = ptr;
					for (int i=0; i<numEntries; i++) {
						NotesReplicationHistorySummaryStruct struct = NotesReplicationHistorySummaryStruct.newInstance(currPosPtr);
						struct.read();
						
						DominoDateTime replicationTime = struct.ReplicationTime==null ? null : new JNADominoDateTime(struct.ReplicationTime);
						AclLevel aclLevel = DominoEnumUtil.valueOf(AclLevel.class, Short.toUnsignedInt(struct.AccessLevel))
							.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Cannot identify access level for {0}", Short.toUnsignedInt(struct.AccessLevel))));
						
						Set<AclFlag> accessFlags = DominoEnumUtil.valuesOf(AclFlag.class, struct.AccessFlags);
						ReplicationDirection direction;
						if (struct.Direction == ReplicationDirection.SEND.getValue()) {
							direction = ReplicationDirection.SEND;
						}
						else if (struct.Direction == ReplicationDirection.RECEIVE.getValue()) {
							direction = ReplicationDirection.RECEIVE;
						}
						else {
							direction = ReplicationDirection.NEVER;
						}
						
						String serverFilePath = NotesStringUtils.fromLMBCS(ptr.share(struct.ServerNameOffset), -1);
						int iPos = serverFilePath.indexOf("!!"); //$NON-NLS-1$
						String server;
						String filePath;
						if (iPos==-1) {
							server = ""; //$NON-NLS-1$
							filePath = ""; //$NON-NLS-1$
						}
						else {
							server = serverFilePath.substring(0, iPos);
							filePath = serverFilePath.substring(iPos+2);
						}
						
						ReplicationHistorySummary entry = new ReplicationHistorySummary(replicationTime, 
								aclLevel, accessFlags, direction, server, filePath);
						history.add(entry);
						
						currPosPtr = currPosPtr.share(JNANotesConstants.notesReplicationHistorySummaryStructSize);
					}
				}
				finally {
					Mem.OSUnlockObject(rethSummaryByVal);
					short resultFree = Mem.OSMemFree(rethSummaryByVal);
					NotesErrorUtils.checkResult(resultFree);
				}
			}
			
			return history;
		});
	}
	
}
