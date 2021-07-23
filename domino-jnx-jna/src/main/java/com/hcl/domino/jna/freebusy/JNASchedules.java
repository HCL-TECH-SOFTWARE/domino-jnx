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
package com.hcl.domino.jna.freebusy;

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.freebusy.Schedule;
import com.hcl.domino.freebusy.ScheduleOptions;
import com.hcl.domino.freebusy.Schedules;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNAScheduleAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNASchedulesAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NotesScheduleStruct;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Container holding multiple schedules
 * 
 * @author Tammo Riedinger
 */
public class JNASchedules extends BaseJNAAPIObject<JNASchedulesAllocations> implements Schedules {
	private String apptUnid;
	private Collection<ScheduleOptions> options;
	private TemporalAccessor from;
	private TemporalAccessor until;
	private Collection<String> names;
	
	JNASchedules(IAPIObject<?> parent, DHANDLE hCntnr,
			String apptUnid, Collection<ScheduleOptions> options,
			TemporalAccessor from, TemporalAccessor until, Collection<String> names) {
		super(parent);
		
		getAllocations().setSchedulesHandle(hCntnr);
		
		this.apptUnid = apptUnid;
		this.options = options;
		this.from = from;
		this.until = until;
		this.names = names;
		
		setInitialized();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNASchedulesAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNASchedulesAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public Iterator<Schedule> iterator() {
		AtomicReference<JNASchedule> currSchedule = new AtomicReference<>();
		
		currSchedule.set(getFirstSchedule());
		
		return new Iterator<Schedule>() {
			@Override
			public boolean hasNext() {
				return currSchedule.get()!=null;
			}

			@Override
			public Schedule next() {
				JNASchedule schedule=currSchedule.get();
				
				if (schedule!=null) {
					currSchedule.set(getNextSchedule(schedule));
				}
				return schedule;
			}
		};
	}
	
	/**
	 * Retrieves the handle to the given schedule
	 * 
	 * @param schedule		the schedule
	 * @return			the handle or 0
	 */
	protected int getScheduleHandle(JNASchedule schedule) {
		Integer handle = ((JNAScheduleAllocations)schedule.getAdapter(APIObjectAllocations.class)).getScheduleHandle();
		
		return (handle!=null) ? handle : 0;
	}

	/**
	 * This function is used to get a handle to the first schedule object in a container.
	 * 
	 * @return schedule
	 */
	private JNASchedule getFirstSchedule() {
		if (this.isDisposed()) {
			throw new DominoException(0, "Schedule collection has been disposed");
		}

		DHANDLE hSchedules = getAllocations().getSchedulesHandle();
		
		return LockUtil.lockHandle(hSchedules, (hSchedulesByVal) -> {
			IntByReference rethObj = new IntByReference();
			Memory schedulePtrMem = new Memory(Native.POINTER_SIZE);

			short result = NotesCAPI.get().SchContainer_GetFirstSchedule(hSchedulesByVal, rethObj, schedulePtrMem);
			if (result == INotesErrorConstants.ERR_SCHOBJ_NOTEXIST) {
				return null;
			}
			NotesErrorUtils.checkResult(result);
			
			if (rethObj.getValue()==0) {
				return null;
			}
			
			long peer = schedulePtrMem.getLong(0);
			if (peer==0) {
				return null;
			}
			Pointer schedulePtr = new Pointer(peer);
			NotesScheduleStruct retpSchedule = NotesScheduleStruct.newInstance(schedulePtr);
			retpSchedule.read();
			
			int scheduleSize = JNANotesConstants.scheduleSize;
			if (PlatformUtils.isMac() && PlatformUtils.is64Bit()) {
				//on Mac/64, this structure is 4 byte aligned, other's are not
				int remainder = scheduleSize % 4;
				if (remainder > 0) {
					scheduleSize = 4 * (scheduleSize / 4) + 4;
				}
			}
			
			Pointer pOwner = retpSchedule.getPointer().share(scheduleSize);
			String owner = NotesStringUtils.fromLMBCS(pOwner, (retpSchedule.wOwnerNameSize-1) & 0xffff);
			
			return new JNASchedule(this, retpSchedule, owner, rethObj.getValue());
		});
	}
	
	/**
	 * This routine is used to get a handle to the next schedule object in a container.
	 * 
	 * @param schedule the current schedule
	 * @return next schedule
	 */
	public JNASchedule getNextSchedule(JNASchedule schedule) {
		if (this.isDisposed()) {
			throw new ObjectDisposedException(this);
		}

		if (schedule.isDisposed()) {
			throw new ObjectDisposedException(schedule);
		}
		
		DHANDLE hSchedules = getAllocations().getSchedulesHandle();
		
		int hCurrSchedule = getScheduleHandle(schedule);
		
		if (hCurrSchedule==0) {
			throw new DominoException(0, "Handle of specified schedule is 0");
		}

		return LockUtil.lockHandle(hSchedules, (hSchedulesByVal) -> {
			IntByReference rethNextSchedule = new IntByReference();
	
			Memory schedulePtrMem = new Memory(Native.POINTER_SIZE);
			short result = NotesCAPI.get().SchContainer_GetNextSchedule(hSchedulesByVal, hCurrSchedule, rethNextSchedule,
					schedulePtrMem);
			if (result==INotesErrorConstants.ERR_SCHOBJ_NOTEXIST) {
				return null;
			}
			NotesErrorUtils.checkResult(result);
	
			long peer = schedulePtrMem.getLong(0);
			if (peer==0) {
				return null;
			}
			
			Pointer schedulePtr = new Pointer(peer);
			NotesScheduleStruct retpNextSchedule = NotesScheduleStruct.newInstance(schedulePtr);
			retpNextSchedule.read();
			
			int scheduleSize = JNANotesConstants.scheduleSize;
			if (PlatformUtils.isMac() && PlatformUtils.is64Bit()) {
				//on Mac/64, this structure is 4 byte aligned, other's are not
				int remainder = scheduleSize % 4;
				if (remainder > 0) {
					scheduleSize = 4 * (scheduleSize / 4) + 4;
				}
			}

			Pointer pOwner = retpNextSchedule.getPointer().share(scheduleSize);
			String owner = NotesStringUtils.fromLMBCS(pOwner, (retpNextSchedule.wOwnerNameSize-1) & 0xffff);
	
			return new JNASchedule(this, retpNextSchedule, owner, rethNextSchedule.getValue());
		});
	}

	@Override
	public String toStringLocal() {
		return MessageFormat.format("JNASchedules [apptUnid={0}, options={1}, from={2}, until={3}, names={4}", //$NON-NLS-1$
				apptUnid, options, from, until, names);
	}
	
}
