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
package com.hcl.domino.jna.freebusy;

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.freebusy.Schedule;
import com.hcl.domino.freebusy.ScheduleEntry;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNAScheduleAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNASchedulesAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.gc.handles.ReadUtil;
import com.hcl.domino.jna.internal.structs.NotesSchedEntryExtStruct;
import com.hcl.domino.jna.internal.structs.NotesSchedEntryStruct;
import com.hcl.domino.jna.internal.structs.NotesScheduleListStruct;
import com.hcl.domino.jna.internal.structs.NotesScheduleStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDatePairStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Schedule object to read busy and free time information for a single Domino user
 * 
 * @author Tammo Riedinger
 */
public class JNASchedule extends BaseJNAAPIObject<JNAScheduleAllocations> implements Schedule {
	private NotesScheduleStruct m_scheduleData;
	private String m_owner;
	
	JNASchedule(JNASchedules parent, NotesScheduleStruct scheduleData, String owner, int hSchedule) {
		super(parent);
		
		m_scheduleData = scheduleData;
		m_owner = owner;
		
		getAllocations().setScheduleHandle(hSchedule);
		
		setInitialized();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNAScheduleAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNAScheduleAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public String getOwner() {
		return m_owner;
	}
	
	@Override
	public String getDbReplicaId() {
		NotesTimeDateStruct replicaId = m_scheduleData==null ? null : m_scheduleData.dbReplicaID;
		String replicaIdStr = replicaId==null ? null : NotesStringUtils.innardsToReplicaId(replicaId.Innards);
		return replicaIdStr;
	}
	
	@Override
	public Optional<DominoDateTime> getFrom() {
		NotesTimeDatePairStruct tdPair = m_scheduleData==null ? null : m_scheduleData.Interval;
		NotesTimeDateStruct lower = tdPair==null ? null : tdPair.Lower;
		return Optional.of(lower==null ? null : new JNADominoDateTime(lower));
	}
	
	@Override
	public Optional<DominoDateTime> getUntil() {
		NotesTimeDatePairStruct tdPair = m_scheduleData==null ? null : m_scheduleData.Interval;
		NotesTimeDateStruct upper = tdPair==null ? null : tdPair.Upper;
		return Optional.of(upper==null ? null : new JNADominoDateTime(upper));
	}

	@Override
	public Optional<DominoException> getError() {
		short err = m_scheduleData==null ? 0 : m_scheduleData.error;
		return NotesErrorUtils.toNotesError(err);
	}
	
	/**
	 * Retrieve the handle of the schedules container
	 * 
	 * @return		the handle or null
	 */
	protected DHANDLE getSchedulesHandle() {
		return ((JNASchedulesAllocations)getParent().getAdapter(APIObjectAllocations.class)).getSchedulesHandle();
	}
	
	/**
	 * Retrieve the handle to the schedule
	 * 
	 * @return	the handle or 0
	 */
	protected int getScheduleHandle() {
		Integer handle = getAllocations().getScheduleHandle();
		
		return (handle!=null) ? handle : 0;
	}
	
	@Override
	protected void checkDisposedLocal() {
		if (!isInitialized()) {
			return;
		}
		
		DHANDLE hSchedules = getSchedulesHandle();
		if (hSchedules==null) {
			throw new DominoException(0, "Schedules handle is null");
		}

		Integer hSchedule = getAllocations().getScheduleHandle();
		if (hSchedule==null) {
			throw new DominoException(0, "Schedule handle is null");
		}
	}
	
	@Override
	public List<DominoDateRange> extractBusyTimeRange(String unidIgnore, TemporalAccessor from, TemporalAccessor until) {
		checkDisposed();
		
		NotesUniversalNoteIdStruct unidStruct = unidIgnore==null ? null : NotesUniversalNoteIdStruct.fromString(unidIgnore);
		Objects.requireNonNull(from, "from date cannot be null");
		Objects.requireNonNull(until, "until date cannot be null");
		
		JNADominoDateTime jnaFrom = JNADominoDateTime.from(from);
		JNADominoDateTime jnaUntil = JNADominoDateTime.from(until);
		
		NotesTimeDatePairStruct intervalPair = NotesTimeDatePairStruct.newInstance();
		intervalPair.Lower = NotesTimeDateStruct.newInstance(jnaFrom.getInnards());
		intervalPair.Upper = NotesTimeDateStruct.newInstance(jnaUntil.getInnards());
		intervalPair.write();

		IntByReference retdwSize = new IntByReference();
		IntByReference rethMoreCtx = new IntByReference();
		
		return LockUtil.lockHandle(getSchedulesHandle(), (hSchedulesByVal) -> {
			DHANDLE.ByReference rethRange = DHANDLE.newInstanceByReference();
			
			// read first part of busy time
			short res = NotesCAPI.get().Schedule_ExtractBusyTimeRange(hSchedulesByVal, getScheduleHandle(),
				unidStruct, intervalPair,
				retdwSize, rethRange, rethMoreCtx);
			
			NotesErrorUtils.checkResult(res);
			
			List<DominoDateRange> allRanges = new ArrayList<>();
			
			allRanges.addAll(
				ReadUtil.readDateRange(rethRange, true)
			);
			
			boolean hasMoreData = rethMoreCtx.getValue()!=0;
			while (hasMoreData) {
				//read more data
				rethRange = DHANDLE.newInstanceByReference();
				
				res = NotesCAPI.get().Schedule_ExtractMoreBusyTimeRange(hSchedulesByVal, rethMoreCtx.getValue(), unidStruct,
						intervalPair,
						retdwSize, rethRange, rethMoreCtx);
				
				NotesErrorUtils.checkResult(res);
				
				allRanges.addAll(
					ReadUtil.readDateRange(rethRange, true)
				);
				
				hasMoreData = rethMoreCtx.getValue()!=0;
			}
			
			return allRanges;
		});
	}
	
	@Override
	public List<DominoDateRange> extractFreeTimeRange(String unidIgnore,
			boolean findFirstFit, TemporalAccessor from, TemporalAccessor until, int duration) {

		checkDisposed();
		
		NotesUniversalNoteIdStruct unidStruct = unidIgnore==null ? null : NotesUniversalNoteIdStruct.fromString(unidIgnore);
		Objects.requireNonNull(from, "from date cannot be null");
		Objects.requireNonNull(until, "until date cannot be null");
		
		JNADominoDateTime jnaFrom = JNADominoDateTime.from(from);
		JNADominoDateTime jnaUntil = JNADominoDateTime.from(until);
		
		NotesTimeDatePairStruct intervalPair = NotesTimeDatePairStruct.newInstance();
		intervalPair.Lower = NotesTimeDateStruct.newInstance(jnaFrom.getInnards());
		intervalPair.Upper = NotesTimeDateStruct.newInstance(jnaUntil.getInnards());
		intervalPair.write();

		if (duration > 65535) {
			throw new IllegalArgumentException(MessageFormat.format("Duration can only have a short value ({0}>65535)", duration));
		}

		return LockUtil.lockHandle(getSchedulesHandle(), (hSchedulesByVal) -> {
			DHANDLE.ByReference rethRange = DHANDLE.newInstanceByReference();
			IntByReference retdwSize = new IntByReference();
			
			short result = NotesCAPI.get().Schedule_ExtractFreeTimeRange(hSchedulesByVal, getScheduleHandle(),
					unidStruct, (short) (findFirstFit ? 1 : 0), (short) (duration & 0xffff),
					intervalPair, retdwSize, rethRange);
			NotesErrorUtils.checkResult(result);
			
			return ReadUtil.readDateRange(rethRange, true);
		});
	}
	
	/**
	 * Internal method to read schedule list entries
	 * 
	 * @param listPtr memory pointer
	 * @return entries
	 */
	private List<ScheduleEntry> readSchedList(Pointer listPtr) {
		List<ScheduleEntry> decodedEntries = new ArrayList<>();
		
		NotesScheduleListStruct schedList = NotesScheduleListStruct.newInstance(listPtr);
		schedList.read();
		
		Pointer entriesPtr = listPtr.share(JNANotesConstants.schedListSize);
		for (int i=0; i<schedList.NumEntries; i++) {
			if (schedList.Spare==0) {
				//pre-R6
				NotesSchedEntryStruct entryStruct = NotesSchedEntryStruct.newInstance(entriesPtr);
				entryStruct.read();
				
				ScheduleEntry entry = new JNAScheduleEntry(entryStruct);
				decodedEntries.add(entry);
				
				entriesPtr = entriesPtr.share(JNANotesConstants.schedEntrySize);
			}
			else {
				//extended data structure
				NotesSchedEntryExtStruct entryStruct = NotesSchedEntryExtStruct.newInstance(entriesPtr);
				entryStruct.read();
				
				ScheduleEntry entry = new JNAScheduleEntry(entryStruct);
				decodedEntries.add(entry);
				
				entriesPtr = entriesPtr.share(JNANotesConstants.schedEntryExtSize);
			}
		}
		return decodedEntries;
	}
	
	/**
	 * This retrieves the schedule list from a schedule. A schedule list contains more
	 * appointment details than just from/until times that can be read via {@link #extractBusyTimeRange(String, TemporalAccessor, TemporalAccessor)},
	 * e.g. the UNID/ApptUNID of the appointments.
	 * 
	 * @param from specifies the start of the range over which the free time search should be performed. In typical scheduling applications, this might be a range of 1 day or 5 days
	 * @param until specifies the end of the range over which the free time search should be performed. In typical scheduling applications, this might be a range of 1 day or 5 days
	 * @return schedule list
	 */
	@Override
	public List<ScheduleEntry> extractScheduleList(TemporalAccessor from, TemporalAccessor until) {
		checkDisposed();
		
		Objects.requireNonNull(from, "from date cannot be null");
		Objects.requireNonNull(until, "until date cannot be null");
		
		JNADominoDateTime jnaFrom = JNADominoDateTime.from(from);
		JNADominoDateTime jnaUntil = JNADominoDateTime.from(until);
		
		NotesTimeDatePairStruct intervalPair = NotesTimeDatePairStruct.newInstance();
		intervalPair.Lower = NotesTimeDateStruct.newInstance(jnaFrom.getInnards());
		intervalPair.Upper = NotesTimeDateStruct.newInstance(jnaUntil.getInnards());
		intervalPair.write();

		return LockUtil.lockHandle(getSchedulesHandle(), (hSchedulesByVal) -> {
			DHANDLE.ByReference rethSchedList = DHANDLE.newInstanceByReference();
			IntByReference retdwSize = new IntByReference();
			IntByReference rethMore = new IntByReference();
			
			//read first part of schedule list
			short result = NotesCAPI.get().Schedule_ExtractSchedList(hSchedulesByVal, getScheduleHandle(),
					intervalPair, retdwSize, rethSchedList, rethMore);
			
			NotesErrorUtils.checkResult(result);
			
			List<ScheduleEntry> allSchedEntries = new ArrayList<>();
			
			allSchedEntries.addAll(
				ReadUtil.accessMemory(rethSchedList, true, new ReadUtil.MemoryAccess<List<ScheduleEntry>>() {
					@Override
					public List<ScheduleEntry> access(Pointer ptr) {
						return readSchedList(ptr);
					}
					
					@Override
					public List<ScheduleEntry> handleIsNull() {
						return Collections.emptyList();
					}
				})
			);
			
			boolean hasMoreData = rethMore.getValue()!=0;
			
			while (hasMoreData) {
				//read more data
				rethSchedList = DHANDLE.newInstanceByReference();
				result = NotesCAPI.get().Schedule_ExtractMoreSchedList(hSchedulesByVal, rethMore.getValue(),
						intervalPair, retdwSize, rethSchedList, rethMore);
				NotesErrorUtils.checkResult(result);
				
				allSchedEntries.addAll(
					ReadUtil.accessMemory(rethSchedList, true, new ReadUtil.MemoryAccess<List<ScheduleEntry>>() {
						@Override
						public List<ScheduleEntry> access(Pointer ptr) {
							return readSchedList(ptr);
						}
						
						@Override
						public List<ScheduleEntry> handleIsNull() {
							return Collections.emptyList();
						}
					})
				);
				
				hasMoreData = rethMore.getValue()!=0;
			}
			
			return allSchedEntries;
		});
	}
}
