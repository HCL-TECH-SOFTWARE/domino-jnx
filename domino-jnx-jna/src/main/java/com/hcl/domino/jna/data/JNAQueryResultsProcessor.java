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
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.hcl.domino.BuildVersionInfo;
import com.hcl.domino.DominoException;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.data.NativeItemCoder.LmbcsVariant;
import com.hcl.domino.dql.QueryResultsProcessor;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.LMBCSStringList;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.Mem.LockedMemory;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.capi.NotesCAPI12;
import com.hcl.domino.jna.internal.capi.NotesCAPI1201;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAIDTableAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNANotesQueryResultsProcessorAllocations;
import com.hcl.domino.jna.internal.gc.allocations.LMBCSStringListAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE32;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE64;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NotesFieldFormulaStruct;
import com.hcl.domino.jna.internal.structs.NotesQueryResultsHandles;
import com.hcl.domino.jna.internal.structs.NotesResultsInfoStruct;
import com.hcl.domino.jna.internal.structs.NotesResultsSortColumnStruct;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * JNA implementation of {@link QueryResultsProcessor} to run multi-database read operations.
 * 
 * @author Karsten Lehmann
 */
public class JNAQueryResultsProcessor extends BaseJNAAPIObject<JNANotesQueryResultsProcessorAllocations> implements QueryResultsProcessor {
	private JNADatabase m_db;
	private int m_totalNotesAdded;
	
	public JNAQueryResultsProcessor(IGCDominoClient<?> dominoClient, JNADatabase db) {
		super(dominoClient);
		m_db = db;

		//the data structures we use were changed between 12.0.0 beta 2 and beta 3,
		//so let's check the build number
    BuildVersionInfo buildVersionInfo = dominoClient.getBuildVersion(""); //$NON-NLS-1$
    
    if (buildVersionInfo.getBuildNumber() <= 461) {
      throw new UnsupportedOperationException("The QueryResultsProcessor requires a R12.0.0 environment or later");
    }
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNANotesQueryResultsProcessorAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		return new JNANotesQueryResultsProcessorAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public QueryResultsProcessor addNoteIds(Database parentDb, Collection<Integer> idTable, String resultsname) {
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

    IntByReference hretError = new IntByReference();
    
		short result = NotesCAPI12.get().NSFQueryAddToResultsList(NotesConstants.QUEP_LISTTYPE.INPUT_RESULTS_LST.getValue(),
				ri.getPointer(), queryResultsHandlesPtr /* &hInResults */, hretError);
		
    if (result!=0 && hretError.getValue()!=0) {
      if (hretError.getValue()!=0) {
        String errorMessage = NotesErrorUtils.errToString(result);
        
        String errorDetails;
        try (LockedMemory memErr = Mem.OSMemoryLock(hretError.getValue(), true)) {
          errorDetails = NotesStringUtils.fromLMBCS(memErr.getPointer(), -1);
        }
        
        throw new DominoException(result, errorMessage + " - " + errorDetails); //$NON-NLS-1$
      }
      else {
        NotesErrorUtils.checkResult(result);
      }
    }
    
		queryResultsProcessorAllocations.getNotesQueryResultsHandles().read();
    m_totalNotesAdded += idTable.size();
		return this;
	}

	 @Override
	 public QueryResultsProcessor addColumn(String colname, String title, String formula, SortOrder sortorder, Hidden ishidden, Categorized iscategorized) {
	    NotesResultsSortColumnStruct rsc = NotesResultsSortColumnStruct.newInstance();
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
	    if (colnameArr.length > NotesConstants.MAX_CMD_VALLEN) {
	      throw new IllegalArgumentException("Column name exceeds max text length in bytes");
	    }
	    System.arraycopy(colnameArr, 0, rsc.name, 0, colnameArr.length);
	    
	    if (!StringUtil.isEmpty(title)) {
	      Memory titleMem = NotesStringUtils.toLMBCS(title, true);
	      byte[] titleArr = titleMem.getByteArray(0, (int) titleMem.size());
	      if (titleArr.length > NotesConstants.MAX_CMD_VALLEN) {
	        throw new IllegalArgumentException("Title exceeds max text length in bytes");
	      }
	      System.arraycopy(titleArr, 0, rsc.title, 0, titleArr.length);
	    }
	    
      IntByReference rethFormulaStr = new IntByReference();
      JNANotesQueryResultsProcessorAllocations allocations = getAllocations();
      
	    if (!StringUtil.isEmpty(formula)) {
	      Charset charset = NativeItemCoder.get().getLmbcsCharset(LmbcsVariant.NULLTERM_KEEPNEWLINES);
	      byte[] formulaStringArr = formula.getBytes(charset);

	      
	      short result = Mem.OSMemoryAllocate(NotesConstants.BLK_MEM_ALLOC, formulaStringArr.length, rethFormulaStr);
	      NotesErrorUtils.checkResult(result);
	      
	      if (rethFormulaStr.getValue()==0) {
          throw new DominoException("Memory allocation for formula failed");
	      }
	      
	      allocations.addFormulaHandleForDispose(rethFormulaStr.getValue());
	      
	      try (LockedMemory mem = Mem.OSMemoryLock(rethFormulaStr.getValue(), false)) {
	        mem.getPointer().write(0, formulaStringArr, 0, formulaStringArr.length);
	      }
	      rsc.hColFormula = rethFormulaStr.getValue();
	    }

	    rsc.sortorder = sortorder.getValue();
	    rsc.bHidden = ishidden == Hidden.TRUE;
	    rsc.bCategorized = iscategorized == Categorized.TRUE;
	    rsc.write();
	    
	    JNANotesQueryResultsProcessorAllocations queryResultsProcessorAllocations = getAllocations();
	    Pointer queryResultsHandlesPtr = queryResultsProcessorAllocations.getNotesQueryResultsHandles().getPointer();
	    Pointer hOutFieldsPtr = queryResultsHandlesPtr.share(4);
	    
	    IntByReference hretError = new IntByReference();
	    
	    short result = NotesCAPI12.get().NSFQueryAddToResultsList(NotesConstants.QUEP_LISTTYPE.SORT_COL_LST.getValue(),
	        rsc.getPointer(), hOutFieldsPtr, hretError);

	    if (result!=0 && hretError.getValue()!=0) {
	      if (hretError.getValue()!=0) {
	        String errorMessage = NotesErrorUtils.errToString(result);
	        
	        String errorDetails;
	        try (LockedMemory memErr = Mem.OSMemoryLock(hretError.getValue(), true)) {
	          errorDetails = NotesStringUtils.fromLMBCS(memErr.getPointer(), -1);
	        }
	        
	        throw new DominoException(result, errorMessage + " - " + errorDetails); //$NON-NLS-1$
	      }
	      else {
	        NotesErrorUtils.checkResult(result);
	      }
	    }
	    
	    return this;
	  }
	 
	@Override
	public QueryResultsProcessor addFormula(String formula, String columnname, String resultsname) {
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
		
    Memory resultsnameMem = NotesStringUtils.toLMBCS(resultsname, true);
    byte[] resultsnameArr = resultsnameMem.getByteArray(0, (int) resultsnameMem.size());
    if (resultsnameArr.length > NotesConstants.MAX_CMD_VALLEN) {
      throw new IllegalArgumentException("Results name exceeds max size in bytes");
    }

    Memory columnnameMem = NotesStringUtils.toLMBCS(columnname, true);
    byte[] columnnameArr = columnnameMem.getByteArray(0, (int) columnnameMem.size());
    if (columnnameArr.length > NotesConstants.MAX_CMD_VALLEN) {
      throw new IllegalArgumentException("Column name exceeds max size in bytes");
    }

    Charset charset = NativeItemCoder.get().getLmbcsCharset(LmbcsVariant.NULLTERM_KEEPNEWLINES);
    byte[] formulaStringArr = formula.getBytes(charset);
		
		NotesFieldFormulaStruct ffStruct = NotesFieldFormulaStruct.newInstance();
    ffStruct.write();

     //write result set name
    System.arraycopy(resultsnameArr, 0, ffStruct.resultsname, 0, resultsnameArr.length);

    //write column name
    System.arraycopy(columnnameArr, 0, ffStruct.columnname, 0, columnnameArr.length);

    //write formula string
    {
      IntByReference rethFormula = new IntByReference();
      short result = Mem.OSMemoryAllocate(NotesConstants.BLK_MEM_ALLOC, formulaStringArr.length, rethFormula);
      NotesErrorUtils.checkResult(result);
      
      if (rethFormula.getValue()==0) {
        throw new DominoException("Memory allocation for formula failed");
      }
      
      try (LockedMemory mem = Mem.OSMemoryLock(rethFormula.getValue());) {
        mem.getPointer().write(0, formulaStringArr, 0, formulaStringArr.length);
      }
      
      ffStruct.hFormula = rethFormula.getValue();
      
      //dispose this later
      JNANotesQueryResultsProcessorAllocations allocations = getAllocations();
      allocations.addFormulaHandleForDispose(ffStruct.hFormula);
    }

    ffStruct.write();
    Pointer ptrFF = ffStruct.getPointer();

		JNANotesQueryResultsProcessorAllocations queryResultsProcessorAllocations = getAllocations();
		Pointer queryResultsHandlesPtr = queryResultsProcessorAllocations.getNotesQueryResultsHandles().getPointer();
		Pointer hFieldRulesPtr = queryResultsHandlesPtr.share(4 + 4);
		
		IntByReference hretError = new IntByReference();
		
		short result = NotesCAPI12.get().NSFQueryAddToResultsList(NotesConstants.QUEP_LISTTYPE.FIELD_FORMULA_LST.getValue(),
		    ptrFF, hFieldRulesPtr, hretError);
		
    if (result!=0 && hretError.getValue()!=0) {
      if (hretError.getValue()!=0) {
        String errorMessage = NotesErrorUtils.errToString(result);
        
        String errorDetails;
        try (LockedMemory memErr = Mem.OSMemoryLock(hretError.getValue(), true)) {
          errorDetails = NotesStringUtils.fromLMBCS(memErr.getPointer(), -1);
        }
        
        throw new DominoException(result, errorMessage + " - " + errorDetails); //$NON-NLS-1$
      }
      else {
        NotesErrorUtils.checkResult(result);
      }
    }
    
    return this;
 	}

	@Override
	public void executeToJSON(Appendable appendable, Set<QRPOptions> options) {
	  if (appendable==null) {
	    throw new IllegalArgumentException("Appendable is null");
	  }

		JNANotesQueryResultsProcessorAllocations allocations = getAllocations();
		allocations.checkDisposed();
		
    if (m_totalNotesAdded==0) {
      //workaround for 12.0.0 / 12.0.1 issue where the produced json string is invalid
      try {
        appendable.append(" {\"StreamResults\" :[]} "); //$NON-NLS-1$
      } catch (IOException e) {
        throw new DominoException(0, "Error writing data to Appendable", e);
      }
    }

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
					String errorMessage = NotesErrorUtils.errToString(result);
				  String errorDetails = NotesStringUtils.fromLMBCS(errMsgMem.getPointer(), -1);
					throw new DominoException(result, errorMessage + " - " + errorDetails); //$NON-NLS-1$
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
		
    if (m_totalNotesAdded==0) {
      //workaround for 12.0.0 / 12.0.1 issue where the produced json string is invalid
      return new StringReader(" {\"StreamResults\" :[]} "); //$NON-NLS-1$
    }

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
		  //NSFProcessResults exists in the 12.0.0 C API:
			return NotesCAPI12.get().NSFProcessResults(dbHandleByVal, viewNameMem,
					fDwFlags, handles.hInResults, handles.hOutFields,
					handles.hFieldRules, handles.hCombineRules, hErrorText, hqueue);
		});
		
		if (result!=0) {
			if (hErrorText.getValue()!=0) {
				try (LockedMemory errMsgMem = Mem.OSMemoryLock(hErrorText.getValue(), true);) {
          String errorMessage = NotesErrorUtils.errToString(result);
          String errorDetails = NotesStringUtils.fromLMBCS(errMsgMem.getPointer(), -1);
          throw new DominoException(result, errorMessage + " - " + errorDetails); //$NON-NLS-1$
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

	@Override
	public int executeToView(String viewName, int hoursUntilExpire, Collection<String> readers) {
	  JNANotesQueryResultsProcessorAllocations allocations = getAllocations();
	  allocations.checkDisposed();

	  NotesQueryResultsHandles handles = allocations.getNotesQueryResultsHandles();
	  handles.read();

	  if (handles.hOutFields == 0) {
	    throw new DominoException("No column has been defined");
	  }

	  JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) m_db.getAdapter(APIObjectAllocations.class);
	  dbAllocations.checkDisposed();

	  Memory viewNameMem = NotesStringUtils.toLMBCS(viewName, true);
	  List<String> readersCanonical = NotesNamingUtils.toCanonicalNames(readers);

	  LMBCSStringList readersList = new LMBCSStringList(this, readersCanonical, false);
	  LMBCSStringListAllocations readersListAllocations = (LMBCSStringListAllocations) readersList.getAdapter(APIObjectAllocations.class);

	  IntByReference hErrorText = new IntByReference();
	  hErrorText.setValue(0);
	  DHANDLE.ByReference hqueue = DHANDLE.newInstanceByReference();

	  final int timeoutsec = 0;
	  final int maxEntries = 0;

	  ShortByReference hviewcoll = new ShortByReference();
	  IntByReference viewnid = new IntByReference();

	  short result = LockUtil.lockHandles(dbAllocations.getDBHandle(), readersListAllocations.getListHandle() , (dbHandleByVal, hListByVal) -> {
      //NSFProcessResultsExt got introduced in the 12.0.1 C API:
	    return NotesCAPI1201.get().NSFProcessResultsExt(
	        dbHandleByVal,
	        viewNameMem,
	        NotesConstants.PROCRES_CREATE_VIEW,
	        handles.hInResults,
	        handles.hOutFields,
	        handles.hFieldRules,
	        handles.hCombineRules,
	        hListByVal,
	        hoursUntilExpire,
	        hErrorText,
	        hqueue,
	        hviewcoll,
	        viewnid,
	        timeoutsec*1000,
	        maxEntries,
	        0
	        );
	  });

	  if (result==0) {
	    return viewnid.getValue();
	  }
	  else {
	    if (hErrorText.getValue()!=0) {
	      try (LockedMemory errMsgMem = Mem.OSMemoryLock(hErrorText.getValue(), true);) {
          String errorMessage = NotesErrorUtils.errToString(result);
          String errorDetails = NotesStringUtils.fromLMBCS(errMsgMem.getPointer(), -1);
          throw new DominoException(result, errorMessage + " - " + errorDetails); //$NON-NLS-1$
	      }
	    }
	    else {
	      NotesErrorUtils.checkResult(result);
	      //unreachable
	      return 0;
	    }
	  }
	}
}
