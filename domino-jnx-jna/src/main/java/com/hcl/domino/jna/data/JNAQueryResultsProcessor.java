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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.ReferenceQueue;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.dql.QueryResultsProcessor;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.Mem.LockedMemory;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.capi.NotesCAPI12;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAIDTableAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNANotesQueryResultsProcessorAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE32;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE64;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NotesFieldFormulaStruct;
import com.hcl.domino.jna.internal.structs.NotesQueryResultsHandles;
import com.hcl.domino.jna.internal.structs.NotesResultsInfoStruct;
import com.hcl.domino.jna.internal.structs.NotesResultsSortColumn;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * JNA implementation of {@link QueryResultsProcessor} to run multi-database read operations.
 * 
 * @author Karsten Lehmann
 */
public class JNAQueryResultsProcessor extends BaseJNAAPIObject<JNANotesQueryResultsProcessorAllocations> implements QueryResultsProcessor {
	private JNADatabase m_db;
	
	public JNAQueryResultsProcessor(IGCDominoClient<?> dominoClient, JNADatabase db) {
		super(dominoClient);
		m_db = db;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNANotesQueryResultsProcessorAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		return new JNANotesQueryResultsProcessorAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public void addNoteIds(Database parentDb, Collection<Integer> idTable, String resultsname) {
		checkDisposed();
		
		NotesResultsInfoStruct ri = NotesResultsInfoStruct.newInstance();
		
		String dbServer = NotesNamingUtils.toCommonName(parentDb.getServer());
		String dbPath = StringUtil.isEmpty(dbServer) ? parentDb.getRelativeFilePath() : (dbServer + "!!" + parentDb.getRelativeFilePath()); //$NON-NLS-1$
		Memory dbPathMem = NotesStringUtils.toLMBCS(dbPath, true);
		byte[] dbPathArr = dbPathMem.getByteArray(0, (int) dbPathMem.size());
		
		if (dbPathArr.length > ri.dbPath.length) {
			throw new IllegalArgumentException("Database path length exceeds max size in bytes");
		}
		System.arraycopy(dbPathArr, 0, ri.dbPath, 0, dbPathArr.length);
		
		if (resultsname!=null && resultsname.length()>0) {
			Memory resultsnameMem = NotesStringUtils.toLMBCS(resultsname, true);
			byte[] resultsnameArr = resultsnameMem.getByteArray(0, (int) resultsnameMem.size());
			
			if (resultsnameArr.length > ri.name.length) {
				throw new IllegalArgumentException("Result name exceeds max size in bytes");
			}
			System.arraycopy(resultsnameArr, 0, ri.name, 0, resultsnameArr.length);
		}
		
		JNAIDTable idTableCopy = idTable==null ? new JNAIDTable(getParentDominoClient()) : new JNAIDTable(getParentDominoClient(), idTable);
		JNAIDTableAllocations idTableCopyAllocations = (JNAIDTableAllocations) idTableCopy.getAdapter(APIObjectAllocations.class);
		DHANDLE idTableCopyHandle = idTableCopyAllocations.getIdTableHandle();
		if (idTableCopyHandle instanceof DHANDLE64) {
			ri.hResults = (int) (((DHANDLE64)idTableCopyHandle).hdl & 0xffffffff);
		}
		else if (idTableCopyHandle instanceof DHANDLE32) {
			ri.hResults = ((DHANDLE32)idTableCopyHandle).hdl;
		}
		
		ri.write();
		
		JNANotesQueryResultsProcessorAllocations queryResultsProcessorAllocations = getAllocations();
		Pointer queryResultsHandlesPtr = queryResultsProcessorAllocations.getNotesQueryResultsHandles().getPointer();

		short result = NotesCAPI12.get().NSFQueryAddToResultsList(NotesConstants.QUEP_LISTTYPE.INPUT_RESULTS_LST.getValue(),
				ri.getPointer(), queryResultsHandlesPtr /* &hInResults */, null);
		NotesErrorUtils.checkResult(result);
		
		queryResultsProcessorAllocations.getNotesQueryResultsHandles().read();
	}
	
	@Override
	public void addColumn(String name) {
		addSortColumn(name, null, SortOrder.UNORDERED, Hidden.FALSE, Categorized.FALSE);
	}

	@Override
	public void addSortColumn(String colname, String title, SortOrder sortorder, Hidden ishidden, Categorized iscategorized) {
		NotesResultsSortColumn rsc = NotesResultsSortColumn.newInstance();
		rsc.write();
		
		if (StringUtil.isEmpty(colname)) {
			throw new IllegalArgumentException("Column name cannot be empty");
		}
		
		if (title!=null && title.length()==0) {
			//title is optional
			title = null;
		}
		
		Memory colnameMem = NotesStringUtils.toLMBCS(colname, true);
		byte[] colnameArr = colnameMem.getByteArray(0, (int) colnameMem.size());
		if (colnameArr.length > rsc.name.length) {
			throw new IllegalArgumentException("Column name exceeds max text length in bytes");
		}
		System.arraycopy(colnameArr, 0, rsc.name, 0, colnameArr.length);
		
		if (!StringUtil.isEmpty(title)) {
			Memory titleMem = NotesStringUtils.toLMBCS(title, true);
			byte[] titleArr = titleMem.getByteArray(0, (int) titleMem.size());
			if (titleArr.length > rsc.title.length) {
				throw new IllegalArgumentException("Title exceeds max text length in bytes");
			}
			System.arraycopy(titleArr, 0, rsc.title, 0, titleArr.length);
		}
		
		rsc.sortorder = sortorder.getValue();
		rsc.bHidden = ishidden == Hidden.TRUE;
		rsc.bCategorized = iscategorized == Categorized.TRUE;
		rsc.write();
		
		JNANotesQueryResultsProcessorAllocations queryResultsProcessorAllocations = getAllocations();
		Pointer queryResultsHandlesPtr = queryResultsProcessorAllocations.getNotesQueryResultsHandles().getPointer();
		Pointer hOutFieldsPtr = queryResultsHandlesPtr.share(4);
		
		short result = NotesCAPI12.get().NSFQueryAddToResultsList(NotesConstants.QUEP_LISTTYPE.SORT_COL_LST.getValue(),
				rsc.getPointer(), hOutFieldsPtr, null);
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public void addFormula(String formula, String columnname, String resultsname) {
		checkDisposed();

		if (StringUtil.isEmpty(formula)) {
			throw new IllegalArgumentException("Formula cannot be empty");
		}
		
		if (StringUtil.isEmpty(columnname)) {
			throw new IllegalArgumentException("Column name cannot be empty");
		}

		if (StringUtil.isEmpty(resultsname)) {
			throw new IllegalArgumentException("Results name cannot be empty");
		}
		
		NotesFieldFormulaStruct ff = NotesFieldFormulaStruct.newInstance();
		ff.write();
		
		Memory formulaMem = NotesStringUtils.toLMBCS(formula, true);
		byte[] formulaArr = formulaMem.getByteArray(0, (int) formulaMem.size());
		if (formulaArr.length > ff.formula.length) {
			throw new IllegalArgumentException("Formula exceeds max size in bytes");
		}
		System.arraycopy(formulaArr, 0, ff.formula, 0, formulaArr.length);
		
		Memory columnnameMem = NotesStringUtils.toLMBCS(columnname, true);
		byte[] columnnameArr = columnnameMem.getByteArray(0, (int) columnnameMem.size());
		if (columnnameArr.length > ff.columnname.length) {
			throw new IllegalArgumentException("Column name exceeds max size in bytes");
		}
		System.arraycopy(columnnameArr, 0, ff.columnname, 0, columnnameArr.length);
		
		Memory resultsnameMem = NotesStringUtils.toLMBCS(resultsname, true);
		byte[] resultsnameArr = resultsnameMem.getByteArray(0, (int) resultsnameMem.size());
		if (resultsnameArr.length > ff.resultsname.length) {
			throw new IllegalArgumentException("Results name exceeds max size in bytes");
		}
		System.arraycopy(resultsnameArr, 0, ff.resultsname, 0, resultsnameArr.length);

		ff.write();

		JNANotesQueryResultsProcessorAllocations queryResultsProcessorAllocations = getAllocations();
		Pointer queryResultsHandlesPtr = queryResultsProcessorAllocations.getNotesQueryResultsHandles().getPointer();
		Pointer hFieldRulesPtr = queryResultsHandlesPtr.share(4 + 4);
		
		short result = NotesCAPI12.get().NSFQueryAddToResultsList(NotesConstants.QUEP_LISTTYPE.FIELD_FORMULA_LST.getValue(),
				ff.getPointer(), hFieldRulesPtr, null);
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public void executeToJSON(Appendable appendable, Set<QRPOptions> options) {
		JNANotesQueryResultsProcessorAllocations allocations = getAllocations();
		allocations.checkDisposed();
		
		NotesQueryResultsHandles handles = allocations.getNotesQueryResultsHandles();
		handles.read();
		
		if (handles.hOutFields == 0) {
			throw new DominoException("No column has been defined");
		}
		
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) m_db.getAdapter(APIObjectAllocations.class);
		dbAllocations.checkDisposed();
		
		Memory viewNameMem = null;
		IntByReference hErrorText = new IntByReference();
		hErrorText.setValue(0);
		DHANDLE.ByReference hqueue = DHANDLE.newInstanceByReference();
		
		int dwFlags = NotesConstants.PROCRES_JSON_OUTPUT;
		if (options!=null) {
			if (options.contains(QRPOptions.RETURN_UNID)) {
				dwFlags |= NotesConstants.PROCRES_RETURN_UNID;
			}
			if (options.contains(QRPOptions.RETURN_REPLICAID)) {
				dwFlags |= NotesConstants.PROCRES_RETURN_REPLICAID;
			}
		}
		final int fDwFlags = dwFlags;

		short result = LockUtil.lockHandle(dbAllocations.getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI12.get().NSFProcessResults(dbHandleByVal, viewNameMem,
					fDwFlags, handles.hInResults, handles.hOutFields,
					handles.hFieldRules, handles.hCombineRules, hErrorText, hqueue);
		});
		
		if (result == 0) {
			AtomicReference<DHANDLE.ByReference> hHoldQueueEntry = new AtomicReference<>();
			DHANDLE.ByReference hQueueEntry = DHANDLE.newInstanceByReference();

			try {
				result = LockUtil.lockHandle(hqueue, (hqueueByVal) -> {
					short loopError = 0;
	
					while (loopError == 0) {
						loopError = NotesCAPI.get().QueueGet(hqueueByVal, hQueueEntry);
	
						/* hqueueentry is reused for each segment. last time it is NULLHANDLE,
						so remember the handle for release */
						if (hHoldQueueEntry.get() == null) {
							hHoldQueueEntry.set(hQueueEntry);
						}
						
						if (loopError == 0 & !hQueueEntry.isNull()) {
							LockUtil.lockHandle(hQueueEntry, (hqueueentryByVal) -> {
								Pointer pinbuf = Mem.OSLockObject(hqueueentryByVal);
								try {
									//skip header
									pinbuf = pinbuf.share(NotesConstants.queueEntryHeaderSize);
									pinbuf = pinbuf.share(NotesConstants.resultsStreamBufferHeaderSize);

									String dataStr = NotesStringUtils.fromLMBCS(pinbuf, -1);
									appendable.append(dataStr);
								} catch (IOException e) {
									throw new DominoException("Error writing data to Appendable", e);
								}
								finally {
									Mem.OSUnlockObject(hqueueentryByVal);	/* note: do not free until done with queue */
								}
								return null;
							});
						}
	
						/* not an error condition */
						if (loopError == INotesErrorConstants.ERR_QUEUE_EMPTY) {
							loopError = 0;
							break;
						}
					}
					
					return loopError;
				});
			}
			finally {
				if (hqueue!=null && !hqueue.isNull()) {
					short resultQueueDelete = LockUtil.lockHandle(hqueue, (hqueueByVal)-> {
						return NotesCAPI.get().QueueDelete(hqueueByVal);
					});
					NotesErrorUtils.checkResult(resultQueueDelete);
				}
				
				if (hHoldQueueEntry.get()!=null && !hHoldQueueEntry.get().isNull()) {
					short resultMemFree = LockUtil.lockHandle(hHoldQueueEntry.get(), (hholdqueueentryByVal) -> {
						return Mem.OSMemFree(hholdqueueentryByVal);
					});
					NotesErrorUtils.checkResult(resultMemFree);
				}
			}
		}
		
		if (result!=0) {
			if (hErrorText.getValue()!=0) {
				try (LockedMemory errMsgMem = Mem.OSMemoryLock(hErrorText.getValue(), true);) {
					Pointer errMsgPtr = errMsgMem.getPointer();
					String errMsg = NotesStringUtils.fromLMBCS(errMsgPtr, -1);
					throw new DominoException(result, errMsg);
				}
			}
			else {
				NotesErrorUtils.checkResult(result);
			}
		}
	
	}

	@Override
	public Reader executeToJSON(Set<QRPOptions> options) {
		JNANotesQueryResultsProcessorAllocations allocations = getAllocations();
		allocations.checkDisposed();
		
		NotesQueryResultsHandles handles = allocations.getNotesQueryResultsHandles();
		handles.read();
		
		if (handles.hOutFields == 0) {
			throw new DominoException("No column has been defined");
		}
		
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) m_db.getAdapter(APIObjectAllocations.class);
		dbAllocations.checkDisposed();
		
		Memory viewNameMem = null;
		IntByReference hErrorText = new IntByReference();
		hErrorText.setValue(0);
		DHANDLE.ByReference hqueue = DHANDLE.newInstanceByReference();
		
		int dwFlags = NotesConstants.PROCRES_JSON_OUTPUT;
		if (options!=null) {
			if (options.contains(QRPOptions.RETURN_UNID)) {
				dwFlags |= NotesConstants.PROCRES_RETURN_UNID;
			}
			if (options.contains(QRPOptions.RETURN_REPLICAID)) {
				dwFlags |= NotesConstants.PROCRES_RETURN_REPLICAID;
			}
		}
		final int fDwFlags = dwFlags;
		
		short result = LockUtil.lockHandle(dbAllocations.getDBHandle(), (dbHandleByVal) -> {
			return NotesCAPI12.get().NSFProcessResults(dbHandleByVal, viewNameMem,
					fDwFlags, handles.hInResults, handles.hOutFields,
					handles.hFieldRules, handles.hCombineRules, hErrorText, hqueue);
		});
		
		if (result!=0) {
			if (hErrorText.getValue()!=0) {
				try (LockedMemory errMsgMem = Mem.OSMemoryLock(hErrorText.getValue(), true);) {
					Pointer errMsgPtr = errMsgMem.getPointer();
					String errMsg = NotesStringUtils.fromLMBCS(errMsgPtr, -1);
					throw new DominoException(result, errMsg);
				}
			}
			else {
				NotesErrorUtils.checkResult(result);
			}
		}

		if (hqueue.isNull()) {
			return new StringReader(""); //$NON-NLS-1$
		}
		
		QueryResultsJSONReader reader = new QueryResultsJSONReader(this, hqueue, hErrorText);
		//register this reader to ensure it is closed on GC
		allocations.registerReaderForClose(reader);
		return reader;
	}

	/**
	 * Reader implementation to receive the JSON data from the queue
	 */
	private static class QueryResultsJSONReader extends Reader {
		private JNAQueryResultsProcessor m_processor;
		private DHANDLE.ByReference m_hqueue;
		private DHANDLE.ByReference m_hHoldQueueEntry;
		
		//the last read chunk of JSON data
		private String m_jsonChunk;
		//position of character to return next
		private int m_jsonChunkPos;
		
		private boolean m_eof;
		private boolean m_isClosed;
		private IntByReference m_hErrorText;
		
		public QueryResultsJSONReader(JNAQueryResultsProcessor processor, DHANDLE.ByReference hqueue,
				IntByReference hErrorText) {
			super();
			m_processor = processor;
			m_hqueue = hqueue;
			m_hErrorText = hErrorText;
		}

		@Override
		public void close() throws IOException {
			if (m_isClosed) {
				return;
			}
			
			if (m_hqueue!=null && !m_hqueue.isNull()) {
				short resultQueueDelete = LockUtil.lockHandle(m_hqueue, (hqueueByVal)-> {
					return NotesCAPI.get().QueueDelete(hqueueByVal);
				});
				NotesErrorUtils.checkResult(resultQueueDelete);
				m_hqueue.clear();
			}
			
			if (m_hHoldQueueEntry!=null && !m_hHoldQueueEntry.isNull()) {
				short resultMemFree = LockUtil.lockHandle(m_hHoldQueueEntry, (hholdqueueentryByVal) -> {
					return Mem.OSMemFree(hholdqueueentryByVal);
				});
				NotesErrorUtils.checkResult(resultMemFree);
				m_hHoldQueueEntry.clear();
			}

			JNANotesQueryResultsProcessorAllocations allocations = m_processor.getAllocations();
			allocations.checkDisposed();
			allocations.unregisterReaderForClose(this);
			
			m_isClosed = true;
		}

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			if (cbuf.length==0) {
				return 0;
			}
			
			int numCharsCopied = 0;
			boolean isEOF = false;
			
			for (int i=off; i<(off+len); i++) {
				//read next character
				int currChar = read();
				if (currChar==-1) {
					//no more data
					isEOF = true;
					break;
				}
				cbuf[i] = (char) currChar;
				numCharsCopied++;
			}
			
			if (numCharsCopied==0 && isEOF) {
				return -1;
			}
			
			return numCharsCopied;
		}

		@Override
		public int read(char[] cbuf) throws IOException {
			return read(cbuf, 0, cbuf.length);
		}
		
		@Override
		public int read() throws IOException {
			if (m_jsonChunk==null || m_jsonChunkPos>=m_jsonChunk.length()) {
				m_jsonChunk = readNextChunk();
				m_jsonChunkPos = 0;
			}
			
			if (m_jsonChunkPos>=m_jsonChunk.length()) {
				return -1;
			}
			
			return m_jsonChunk.charAt(m_jsonChunkPos++);
		}
		
		private String readNextChunk() {
			if (m_eof) {
				return ""; //$NON-NLS-1$
			}

			//make sure we have not been disposed
			JNANotesQueryResultsProcessorAllocations allocations = m_processor.getAllocations();
			allocations.checkDisposed();

			return LockUtil.lockHandle(m_hqueue, (m_hqueueByVal) -> {
				DHANDLE.ByReference hQueueEntry = DHANDLE.newInstanceByReference();
				
				short readQueueError = NotesCAPI.get().QueueGet(m_hqueueByVal, hQueueEntry);
				
				/* hqueueentry is reused for each segment. last time it is NULLHANDLE,
				so remember the handle for release */
				if (m_hHoldQueueEntry == null) {
					m_hHoldQueueEntry = hQueueEntry;
				}

				String dataStr = "";
				if (readQueueError == 0 & !hQueueEntry.isNull()) {
					dataStr = LockUtil.lockHandle(hQueueEntry, (hqueueentryByVal) -> {
						Pointer pinbuf = Mem.OSLockObject(hqueueentryByVal);
						try {
							//skip header
							pinbuf = pinbuf.share(NotesConstants.queueEntryHeaderSize);
							pinbuf = pinbuf.share(NotesConstants.resultsStreamBufferHeaderSize);

							return NotesStringUtils.fromLMBCS(pinbuf, -1);
						}
						finally {
							Mem.OSUnlockObject(hqueueentryByVal);	/* note: do not free until done with queue */
						}
					});
					
				}
				
				/* not an error condition */
				if (readQueueError == INotesErrorConstants.ERR_QUEUE_EMPTY) {
					readQueueError = 0;
				}
				
				if (readQueueError!=0) {
					if (m_hErrorText.getValue()!=0) {
						try (LockedMemory errMsgMem = Mem.OSMemoryLock(m_hErrorText.getValue(), true);) {
							Pointer errMsgPtr = errMsgMem.getPointer();
							String errMsg = NotesStringUtils.fromLMBCS(errMsgPtr, -1);
							throw new DominoException(readQueueError, errMsg);
						}
					}
					else {
						NotesErrorUtils.checkResult(readQueueError);
					}
				}
				
				if (dataStr.length()==0) {
					m_eof = true;
				}
				
				return dataStr;
			});
		}

	}
}
