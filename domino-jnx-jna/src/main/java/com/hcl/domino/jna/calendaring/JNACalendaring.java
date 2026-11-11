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
package com.hcl.domino.jna.calendaring;

import static com.hcl.domino.commons.data.types.JNXTypeUtil.word;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.hcl.domino.DominoException;
import com.hcl.domino.calendar.CalendarActionData;
import com.hcl.domino.calendar.CalendarActionOptions;
import com.hcl.domino.calendar.CalendarDocumentOpen;
import com.hcl.domino.calendar.CalendarProcess;
import com.hcl.domino.calendar.CalendarRangeRepeat;
import com.hcl.domino.calendar.CalendarRead;
import com.hcl.domino.calendar.CalendarReadRange;
import com.hcl.domino.calendar.CalendarWrite;
import com.hcl.domino.calendar.Calendaring;
import com.hcl.domino.commons.data.types.TypedConstants;
import com.hcl.domino.commons.data.types.WordByReference;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.types.STATUS;
import com.hcl.domino.data.types.WORD;
import com.hcl.domino.exception.IncompatibleImplementationException;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.Mem.LockedMemory;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNACalendaringAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NotesCalendarActionDataStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Class to access calendaring and scheduling information of Domino
 * 
 * @author Tammo Riedinger
 */
public class JNACalendaring extends BaseJNAAPIObject<JNACalendaringAllocations> implements Calendaring {
	public JNACalendaring(IAPIObject<?>parent) {
		super(parent);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNACalendaringAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		return new JNACalendaringAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public String createCalendarEntry(Database dbMail, String iCal, Collection<CalendarWrite> flags) {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);
		
		Memory icalMem = NotesStringUtils.toLMBCS(iCal, true, false);
		
		int dwFlags = flags==null ? 0 : CalendarWrite.toBitMask(flags);
		
		DHANDLE.ByReference hRetUID = DHANDLE.newInstanceByReference();
		
		LockUtil.lockHandle(dbAllocations.getDBHandle(),
			(hDBMail) ->
				NotesCAPI.get().CalCreateEntry(hDBMail, icalMem, dwFlags, hRetUID, null)
		).orElseThrow();

		if (hRetUID!=null) {
			try (LockedMemory m = Mem.OSMemoryLock(hRetUID, true)) {
				Pointer retUIDPtr=m.getPointer();
				
				if (retUIDPtr!=null) {
					return NotesStringUtils.fromLMBCS(retUIDPtr, -1);
				}
			}
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public void updateCalendarEntry(Database dbMail, String iCal, String uid, String recurId, String comments,
			Collection<CalendarWrite> flags) {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);

		Memory icalMem = NotesStringUtils.toLMBCS(iCal, true, false);
		Memory uidMem = NotesStringUtils.toLMBCS(uid, true);
		Memory recurIdMem = NotesStringUtils.toLMBCS(recurId, true);
		Memory commentsMem = NotesStringUtils.toLMBCS(comments, true);
		
		int dwFlags = flags==null ? 0 : CalendarWrite.toBitMask(flags);
		
		LockUtil.lockHandle(dbAllocations.getDBHandle(),
				(hDBMail) ->
			NotesCAPI.get().CalUpdateEntry(hDBMail, icalMem, uidMem, recurIdMem, commentsMem, dwFlags, null)
		).orElseThrow();
	}

	@Override
	public String getUIDfromDocument(Document document) {
		return getUIDfromNoteID(document.getParentDatabase(), document.getNoteID());
	}

	@Override
	public String getUIDfromNoteID(Database dbMail, int noteId) throws DominoException {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);
		
		try(DisposableMemory retUID = new DisposableMemory(NotesConstants.MAXPATH)) {
			LockUtil.lockHandle(dbAllocations.getDBHandle(),
					(hDBMail) ->
				NotesCAPI.get().CalGetUIDfromNOTEID(hDBMail, noteId, retUID, TypedConstants.MAXPATH,
						null, 0, null)
			).orElseThrow();
			
			return NotesStringUtils.fromLMBCS(retUID, -1);
		}
	}

	@Override
	public String getUIDFromUNID(Database dbMail, String unid) throws DominoException {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);
		
		NotesUniversalNoteIdStruct.ByValue unidObj = NotesUniversalNoteIdStruct.ByValue.newInstance();
		unidObj.setUnid(unid);
		
		try(DisposableMemory retUID = new DisposableMemory(NotesConstants.MAXPATH)) {
			LockUtil.lockHandle(dbAllocations.getDBHandle(),
					(hDBMail) ->
				NotesCAPI.get().CalGetUIDfromUNID(hDBMail, unidObj, retUID,
						TypedConstants.MAXPATH, null, 0, null)
			).orElseThrow();
			
			return NotesStringUtils.fromLMBCS(retUID, -1);
		}
	}

	@Override
	public String getApptUnidFromUID(String uid) {
		Memory uidMem = NotesStringUtils.toLMBCS(uid, true);
		
		try(DisposableMemory retApptUnidMem = new DisposableMemory(NotesConstants.MAXPATH)) {
			NotesCAPI.get().CalGetApptunidFromUID(uidMem, retApptUnidMem, 0, null)
			  .orElseThrow();
			
			return NotesStringUtils.fromLMBCS(retApptUnidMem, -1);
		}
	}

	@Override
	public Document openCalendarEntryDocument(Database dbMail, String uid, String recurId,
			Collection<CalendarDocumentOpen> flags) {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);
		
		Memory uidMem = NotesStringUtils.toLMBCS(uid, true);
		Memory recurIdMem = NotesStringUtils.toLMBCS(recurId, true);
		
		int dwFlags = flags==null ? 0 : CalendarDocumentOpen.toBitMask(flags);
		
		DHANDLE.ByReference rethNote = DHANDLE.newInstanceByReference();

		LockUtil.lockHandle(dbAllocations.getDBHandle(),
				(hDBMail) ->
			NotesCAPI.get().CalOpenNoteHandle(hDBMail, uidMem, recurIdMem, rethNote, dwFlags, null)
		).orElseThrow();
		
		return new JNADocument(toJNADatabase(dbMail), rethNote);
	}

	@Override
	public String readCalendarEntry(Database dbMail, String uid, String recurId, Collection<CalendarRead> flags) {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);
		
		Memory uidMem = NotesStringUtils.toLMBCS(uid, true);
		Memory recurIdMem = NotesStringUtils.toLMBCS(recurId, true);
		
		int dwFlags = flags==null ? 0 : CalendarRead.toBitMask(flags);
		
		String retIcal = null;
		
		DHANDLE.ByReference hRetCalData = DHANDLE.newInstanceByReference();
		
		LockUtil.lockHandle(dbAllocations.getDBHandle(),
				(hDBMail) ->
			NotesCAPI.get().CalReadEntry(hDBMail, uidMem, recurIdMem, hRetCalData, null, dwFlags, null)
		).orElseThrow();
		
		try (LockedMemory m = Mem.OSMemoryLock(hRetCalData, true)) {
			Pointer retUIDPtr=m.getPointer();
			
			if (retUIDPtr!=null) {
				retIcal = NotesStringUtils.fromLMBCS(retUIDPtr, (int)Math.min(Integer.MAX_VALUE, m.getSize()));
			}
		}
		
		return retIcal;
	}

	@Override
	public void readRange(Database dbMail, TemporalAccessor start, TemporalAccessor end, Appendable retICal,
			List<String> retUIDs) throws IOException {
		readRange(dbMail, start, end, 0, Integer.MAX_VALUE, null, retICal, retUIDs);
	}

	@Override
	public void readRange(Database dbMail, TemporalAccessor start, TemporalAccessor end, int skipCount, int maxRead,
			Appendable retICal, List<String> retUIDs) throws IOException {
		readRange(dbMail, start, end, skipCount, maxRead, null, retICal, retUIDs);
	}

	@Override
	public void readRange(Database dbMail, TemporalAccessor start, TemporalAccessor end, int skipCount, int maxRead,
			Collection<CalendarReadRange> readMask, Appendable retICal, List<String> retUIDs) throws IOException {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);
		
		NotesTimeDateStruct.ByValue startStruct = start==null ? null : NotesTimeDateStruct.ByValue.newInstance(JNADominoDateTime.from(start).getInnards());
		NotesTimeDateStruct.ByValue endStruct = end==null ? null : NotesTimeDateStruct.ByValue.newInstance(JNADominoDateTime.from(end).getInnards());

		int dwReturnMask = CalendarReadRange.toBitMask(readMask);
		int dwReturnMaskExt = CalendarReadRange.toBitMask2(readMask);

		//variables to collect the whole lookup result
		StringBuilder sbIcalAllData = retICal==null ? null : new StringBuilder();
		List<String> uidAllData = retUIDs==null ? null : new ArrayList<>();
		
		while (true) {
			AtomicInteger currSkipCount = new AtomicInteger(skipCount);
			AtomicInteger remainingToRead = new AtomicInteger(maxRead);
			boolean hasMoreToDo;
			boolean hasConflict;
			
			//clear current lookup result, while(true) loop may be run multiple times
			//if lookup view changes
			if (sbIcalAllData!=null) {
				sbIcalAllData.setLength(0);
			}
			if (uidAllData!=null) {
				uidAllData.clear();
			}
			
			do {
				DHANDLE.ByReference hRetCalData = retICal==null ? null : DHANDLE.newInstanceByReference();
				DHANDLE.ByReference hRetUIDData = retUIDs==null ? null : DHANDLE.newInstanceByReference();
				WordByReference retCalBufferLength = new WordByReference();
				WordByReference retSignalFlags = new WordByReference();
				IntByReference retNumEntriesProcessed = new IntByReference();
				
				STATUS result = LockUtil.lockHandle(dbAllocations.getDBHandle(),
						(hDBMail) -> {
					return NotesCAPI.get().CalReadRange(hDBMail, startStruct, endStruct, currSkipCount.get(),
							remainingToRead.get(), dwReturnMask, dwReturnMaskExt, null, hRetCalData,
							retCalBufferLength, hRetUIDData, retNumEntriesProcessed, retSignalFlags, 0, null);
				});
				
				if (result.getMaskedValue()==INotesErrorConstants.ERR_NOT_FOUND) { //no data found
					return;
				}
				result.orElseThrow();
				
				int numEntriesProcessed = retNumEntriesProcessed.getValue();
				currSkipCount.addAndGet(numEntriesProcessed);
				remainingToRead.addAndGet(-1 * numEntriesProcessed);
				
				if (hRetCalData!=null && retICal!=null && sbIcalAllData!=null) {
					//decode iCalendar
					int iCalBufLength = retCalBufferLength.getValue();
					if (iCalBufLength>0) {
						try (LockedMemory m = Mem.OSMemoryLock(hRetCalData, true)) {
							Pointer retUIDPtr=m.getPointer();
							
							if (retUIDPtr!=null) {
								String currICal = NotesStringUtils.fromLMBCS(retUIDPtr, iCalBufLength);
								sbIcalAllData.append(currICal);
							}
						}
					}
				}
				
				if (hRetUIDData!=null && retUIDs!=null && uidAllData!=null) {
					//decode UID list
					try (LockedMemory m = Mem.OSMemoryLock(hRetUIDData, true)) {
						Pointer pUIDData=m.getPointer();
						
						if (pUIDData!=null) {
							WordByReference retTextLength = new WordByReference();
							PointerByReference retTextPointer = new PointerByReference();
							
							int numEntriesAsInt = Short.toUnsignedInt(NotesCAPI.get().ListGetNumEntries(pUIDData, 0));
							for (int i=0; i<numEntriesAsInt; i++) {
								NotesCAPI.get().ListGetText(pUIDData, false, word(i), retTextPointer, retTextLength).orElseThrow();
								
								String currUID = NotesStringUtils.fromLMBCS(retTextPointer.getValue(), retTextLength.getValue());
								uidAllData.add(currUID);
							}
						}
					}
				}
				
				WORD signalFlags = retSignalFlags.getWord();
				hasMoreToDo = signalFlags.hasFlag(NotesConstants.SIGNAL_MORE_TO_DO);
				hasConflict = signalFlags.hasFlag(NotesConstants.SIGNAL_ANY_CONFLICT);
			
			}
			while (hasMoreToDo && remainingToRead.get()>0);
			
			if (!hasConflict) {
				//no read conflict in view, we are done
				break;
			}
			else {
				//retry the whole lookup
				continue;
			}
		}
		
		//return what we have read
		if (retICal!=null && sbIcalAllData!=null) {
			retICal.append(sbIcalAllData.toString());
		}
		if (retUIDs!=null && uidAllData!=null) {
			retUIDs.addAll(uidAllData);
		}
	}

	@Override
	public String getRecurrenceID(TemporalAccessor td) {
		NotesTimeDateStruct.ByValue tdByVal = NotesTimeDateStruct.ByValue.newInstance(JNADominoDateTime.from(td).getInnards());
		try(DisposableMemory retRecurId = new DisposableMemory(NotesConstants.MAXPATH)) {
			NotesCAPI.get().CalGetRecurrenceID(tdByVal, retRecurId, word(retRecurId.size()-1))
			  .orElseThrow();
			
			String recurId = NotesStringUtils.fromLMBCS(retRecurId, -1);
			return recurId;
		}
	}

	@Override
	public int getUnappliedNotices(Database dbMail, String uid, List<Integer> retNoteIds, List<String> retUNIDs) {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);

		Memory uidMem = NotesStringUtils.toLMBCS(uid, true);
		
		WordByReference retNumNotices = new WordByReference();
		
		DHANDLE.ByReference phRetNOTEIDs = retNoteIds==null ? null : DHANDLE.newInstanceByReference();
		DHANDLE.ByReference phRetUNIDs = retUNIDs==null ? null : DHANDLE.newInstanceByReference();
		
		LockUtil.lockHandle(dbAllocations.getDBHandle(),
				(hDBMail) ->
			NotesCAPI.get().CalGetUnappliedNotices(hDBMail, uidMem,
				retNumNotices, phRetNOTEIDs, phRetUNIDs, null, 0, null)
		).orElseThrow();
		
		int numNotices = retNumNotices.getValue();
		if (numNotices>0) {
			if (retNoteIds!=null) {
				try (LockedMemory m = Mem.OSMemoryLock(phRetNOTEIDs, true)) {
	                Pointer ptrNoteIds=m.getPointer();
	                
	                if (ptrNoteIds!=null) {
	                	for (int i=0; i<numNotices; i++) {
							int currNoteId = ptrNoteIds.share(4*i).getInt(0);
							retNoteIds.add(currNoteId);
						}
	                }
	            }
			}
			
			if (retUNIDs!=null) {
				try (LockedMemory m = Mem.OSMemoryLock(phRetUNIDs, true)) {
	                Pointer ptrUNIDs=m.getPointer();
	                
	                if (ptrUNIDs!=null) {
	                	for (int i=0; i<numNotices; i++) {
							NotesUniversalNoteIdStruct currUnidStruct = NotesUniversalNoteIdStruct.newInstance(ptrUNIDs.share(i*JNANotesConstants.notesUniversalNoteIdSize));
							String currUnid = currUnidStruct.toString();
							retUNIDs.add(currUnid);
						}
	                }
	            }
			}
		}
		return numNotices;
	}

	@Override
	public int getNewInvitations(Database dbMail, TemporalAccessor tdStart, String uid, TemporalAccessor tdSince,
			AtomicReference<DominoDateTime> retUntil, List<Integer> retNoteIds, List<String> retUNIDs) {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);

		Memory uidMem = NotesStringUtils.toLMBCS(uid, true);

		NotesTimeDateStruct tdStartStruct = tdStart==null ? null : NotesTimeDateStruct.newInstance(JNADominoDateTime.from(tdStart).getInnards());
		NotesTimeDateStruct tdSinceStruct = tdSince==null ? null : NotesTimeDateStruct.newInstance(JNADominoDateTime.from(tdSince).getInnards());
		
		NotesTimeDateStruct retTdUntilStruct = (retUntil==null || retUntil.get()==null) ? null : NotesTimeDateStruct.newInstance();
		
		WordByReference retNumInvites = new WordByReference();
		
		DHANDLE.ByReference phRetNOTEIDs = retNoteIds==null ? null : DHANDLE.newInstanceByReference();
		DHANDLE.ByReference phRetUNIDs = retUNIDs==null ? null : DHANDLE.newInstanceByReference();
		
		LockUtil.lockHandle(dbAllocations.getDBHandle(),
				(hDBMail) ->
			NotesCAPI.get().CalGetNewInvitations(hDBMail, tdStartStruct,
					uidMem, tdSinceStruct,
					retTdUntilStruct, retNumInvites, phRetNOTEIDs, phRetUNIDs, null, 0, null)
		).orElseThrow();
		
		if (retUntil!=null && retUntil.get()!=null) {
			retTdUntilStruct.read();
			retUntil.set(new JNADominoDateTime(retTdUntilStruct.Innards));
		}
		
		int numInvites = retNumInvites.getValue();
		if (numInvites>0) {
			if (retNoteIds!=null) {
				try (LockedMemory m = Mem.OSMemoryLock(phRetNOTEIDs, true)) {
	                Pointer ptrNoteIds=m.getPointer();
	                
	                if (ptrNoteIds!=null) {
	                	for (int i=0; i<numInvites; i++) {
							int currNoteId = ptrNoteIds.share(4*i).getInt(0);
							retNoteIds.add(currNoteId);
						}
	                }
	            }
			}

			if (retUNIDs!=null) {
				try (LockedMemory m = Mem.OSMemoryLock(phRetUNIDs, true)) {
	                Pointer ptrUNIDs=m.getPointer();
	                
	                if (ptrUNIDs!=null) {
	                	for (int i=0; i<numInvites; i++) {
							NotesUniversalNoteIdStruct currUnidStruct = NotesUniversalNoteIdStruct.newInstance(ptrUNIDs.share(i*JNANotesConstants.notesUniversalNoteIdSize));
							String currUnid = currUnidStruct.toString();
							retUNIDs.add(currUnid);
						}
	                }
	            }
			}
		}
		return numInvites;
	}

	@Override
	public String readNotice(Database dbMail, int noteId, Collection<CalendarRead> flags) {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);

		int dwFlags = flags==null ? 0 : CalendarRead.toBitMask(flags);
		
		String retIcal = null;
		
		DHANDLE.ByReference hRetCalData = DHANDLE.newInstanceByReference();
		
		LockUtil.lockHandle(dbAllocations.getDBHandle(),
				(hDBMail) ->
			NotesCAPI.get().CalReadNotice(hDBMail, noteId, hRetCalData, null, dwFlags, null)
		).orElseThrow();
		
		try (LockedMemory m = Mem.OSMemoryLock(hRetCalData, true)) {
            Pointer retUIDPtr=m.getPointer();
            
            if (retUIDPtr!=null) {
            	retIcal = NotesStringUtils.fromLMBCS(retUIDPtr, -1);
            }
        }
		
		return retIcal;
	}

	@Override
	public String readNotice(Database dbMail, String unid, Collection<CalendarRead> flags) {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);
		
		NotesUniversalNoteIdStruct.ByValue unidObj = NotesUniversalNoteIdStruct.ByValue.newInstance();
		unidObj.setUnid(unid);

		int dwFlags = flags==null ? 0 : CalendarRead.toBitMask(flags);
		
		String retIcal = null;
		
		DHANDLE.ByReference hRetCalData = DHANDLE.newInstanceByReference();
		
		LockUtil.lockHandle(dbAllocations.getDBHandle(),
				(hDBMail) ->
			NotesCAPI.get().CalReadNoticeUNID(hDBMail, unidObj, hRetCalData, null, dwFlags, null)
		).orElseThrow();
		
		try (LockedMemory m = Mem.OSMemoryLock(hRetCalData, true)) {
            Pointer retUIDPtr=m.getPointer();
            
            if (retUIDPtr!=null) {
            	retIcal = NotesStringUtils.fromLMBCS(retUIDPtr, -1);
            }
        }
		
		return retIcal;
	}

	@Override
	public void entryAction(Database dbMail, String uid, String recurId, Collection<CalendarProcess> action,
			CalendarRangeRepeat scope, String comment, CalendarActionData data, Collection<CalendarActionOptions> flags) {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);
		
		NotesCalendarActionDataStruct dataStruct = data==null ? null : data.getAdapter(NotesCalendarActionDataStruct.class);

		Memory uidMem = NotesStringUtils.toLMBCS(uid, true);
		Memory recurIdMem = NotesStringUtils.toLMBCS(recurId, true);
		int dwFlags = CalendarActionOptions.toBitMask(flags);
		int dwAction = CalendarProcess.toBitMask(action);
		int dwRange = scope.getValue();
		Memory commentMem = NotesStringUtils.toLMBCS(comment, true);
		
		LockUtil.lockHandle(dbAllocations.getDBHandle(),
				(hDBMail) ->
			NotesCAPI.get().CalEntryAction(hDBMail, uidMem, recurIdMem,
					dwAction, dwRange, commentMem, dataStruct, dwFlags, null)
		).orElseThrow();
	}

	@Override
	public void noticeAction(Database dbMail, int noteId, Collection<CalendarProcess> action, String comment,
			CalendarActionData data, Collection<CalendarActionOptions> flags) {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);
		
		NotesCalendarActionDataStruct dataStruct = data==null ? null : data.getAdapter(NotesCalendarActionDataStruct.class);
		
		int dwFlags = CalendarActionOptions.toBitMask(flags);
		int dwAction = CalendarProcess.toBitMask(action);
		Memory commentMem = NotesStringUtils.toLMBCS(comment, true);
		
		LockUtil.lockHandle(dbAllocations.getDBHandle(),
				(hDBMail) ->
			NotesCAPI.get().CalNoticeAction(hDBMail, noteId, dwAction,
					commentMem, dataStruct, dwFlags, null)
		).orElseThrow();
	}

	@Override
	public void noticeAction(Database dbMail, String unid, Collection<CalendarProcess> action, String comment,
			CalendarActionData data, Collection<CalendarActionOptions> flags) {
		checkDisposed();
		
		JNADatabaseAllocations dbAllocations = getMailDBAllocations(dbMail);
		
		NotesCalendarActionDataStruct dataStruct = data==null ? null : data.getAdapter(NotesCalendarActionDataStruct.class);
		
		NotesUniversalNoteIdStruct.ByValue unidObj = NotesUniversalNoteIdStruct.ByValue.newInstance();
		unidObj.setUnid(unid);
		
		int dwFlags = CalendarActionOptions.toBitMask(flags);
		int dwAction = CalendarProcess.toBitMask(action);
		Memory commentMem = NotesStringUtils.toLMBCS(comment, true);
		
		LockUtil.lockHandle(dbAllocations.getDBHandle(),
				(hDBMail) ->
			NotesCAPI.get().CalNoticeActionUNID(hDBMail, unidObj, dwAction,
					commentMem, dataStruct, dwFlags, null)
		).orElseThrow();
	}
	
	@Override
	public CalendarActionData buildActionData() {
		return new JNACalendarActionData();
	}
	
	private JNADatabaseAllocations getMailDBAllocations(Database dbMail) throws DominoException {
		JNADatabase jnaMailDb = toJNADatabase(dbMail);
		
		if (jnaMailDb.isDisposed()) {
			throw new ObjectDisposedException(jnaMailDb);
		}
		
		return (JNADatabaseAllocations) jnaMailDb.getAdapter(APIObjectAllocations.class);
	}
	
	private JNADatabase toJNADatabase(Database db) throws DominoException {
		if (db instanceof JNADatabase) {
			return (JNADatabase) db;
		}
		throw new IncompatibleImplementationException(db, JNADatabase.class);
	}
}
