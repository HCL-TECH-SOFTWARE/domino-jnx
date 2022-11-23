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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.freebusy.FreeBusy;
import com.hcl.domino.freebusy.ScheduleOptions;
import com.hcl.domino.freebusy.Schedules;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNAFreeBusyAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.gc.handles.ReadUtil;
import com.hcl.domino.jna.internal.structs.NotesTimeDatePairStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ShortByReference;

/**
 * Class to access the free-busy information collected by the Domino server
 * 
 * @author Tammo Riedinger
 */
public class JNAFreeBusy extends BaseJNAAPIObject<JNAFreeBusyAllocations> implements FreeBusy {
	public JNAFreeBusy(IAPIObject<?>parent) {
		super(parent);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNAFreeBusyAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		return new JNAFreeBusyAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	@Override
	public List<DominoDateRange> freeTimeSearch(String apptUnid, TemporalAccessor apptOrigDate,
			boolean findFirstFit, TemporalAccessor from, TemporalAccessor until, int duration, Collection<String> names) {
		
		NotesUniversalNoteIdStruct unidStruct = apptUnid==null ? null : NotesUniversalNoteIdStruct.fromString(apptUnid);
		NotesTimeDateStruct apptOrigDateStruct = apptOrigDate==null ? null : NotesTimeDateStruct.newInstance(new JNADominoDateTime(apptOrigDate).getInnards());
		
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
		
    List<String> namesCanonical = names
        .stream()
        .filter(StringUtil::isNotEmpty)
        .map(NotesNamingUtils::toCanonicalName)
        .collect(Collectors.toList());

    if (namesCanonical.isEmpty()) {
      throw new IllegalArgumentException("No usernames specified to retrieve schedules.");
    }
		
		short result;
				
		DHANDLE.ByReference rethList = DHANDLE.newInstanceByReference();
		ShortByReference retListSize = new ShortByReference();

		result = NotesCAPI.get().ListAllocate((short) 0, 
				(short) 0,
				0, rethList, null, retListSize);
		
		NotesErrorUtils.checkResult(result);

		try {
			List<DominoDateRange> decodedRangesList = LockUtil.lockHandle(rethList, (hListByVal) -> {
				Mem.OSUnlockObject(hListByVal);
				
				for (int i=0; i<namesCanonical.size(); i++) {
					String currName = namesCanonical.get(i);
					Memory currNameMem = NotesStringUtils.toLMBCS(currName, false);
	
	        if (currNameMem!=null && currNameMem.size() > 65535) {
	          throw new DominoException(MessageFormat.format("List item at position {0} exceeds max lengths of 65535 bytes", i));
	        }

	        char textSize = currNameMem==null ? 0 : (char) currNameMem.size();

					short res = NotesCAPI.get().ListAddEntry(hListByVal, 0, retListSize, (char) i, currNameMem,
					    textSize);
					
					NotesErrorUtils.checkResult(res);
				}
	
				Pointer valuePtr = Mem.OSLockObject(hListByVal);
				DHANDLE.ByReference rethRange = DHANDLE.newInstanceByReference();
				short res;
				
				res = NotesCAPI.get().SchFreeTimeSearch(unidStruct, apptOrigDateStruct, (short) (findFirstFit ? 1 : 0), 0, intervalPair,
						(short) (duration & 0xffff), valuePtr, rethRange);

				NotesErrorUtils.checkResult(res);
			
				return ReadUtil.readDateRange(rethRange, true);
			});
			
			if (decodedRangesList==null) {
				return Collections.emptyList();
			}
			
			return decodedRangesList;
		}
		finally {
			result = LockUtil.lockHandle(rethList, (hListByVal) -> {
				Mem.OSUnlockObject(hListByVal);
				
				return Mem.OSMemFree(hListByVal);
			});
			
			NotesErrorUtils.checkResult(result);
		}
	}
	
	@Override
	public Schedules retrieveSchedules(String apptUnid, Collection<ScheduleOptions> options,
			TemporalAccessor from, TemporalAccessor until, Collection<String> names) {

		NotesUniversalNoteIdStruct unidStruct = apptUnid==null ? null : NotesUniversalNoteIdStruct.fromString(apptUnid);

		Objects.requireNonNull(from, "from date cannot be null");
		Objects.requireNonNull(until, "until date cannot be null");
		
		JNADominoDateTime jnaFrom = JNADominoDateTime.from(from);
		JNADominoDateTime jnaUntil = JNADominoDateTime.from(until);
		
		NotesTimeDatePairStruct intervalPair = NotesTimeDatePairStruct.newInstance();
		intervalPair.Lower = NotesTimeDateStruct.newInstance(jnaFrom.getInnards());
		intervalPair.Upper = NotesTimeDateStruct.newInstance(jnaUntil.getInnards());
		intervalPair.write();

		List<String> namesCanonical = names
		    .stream()
		    .filter(StringUtil::isNotEmpty)
		    .map(NotesNamingUtils::toCanonicalName)
		    .collect(Collectors.toList());
		
		if (namesCanonical.isEmpty()) {
		  throw new IllegalArgumentException("No usernames specified to retrieve schedules.");
		}
		
		//make sure we always get the extended schedule container
		final int SCHRQST_EXTFORMAT = 0x0020;
		
		short result;

		int optionsAsInt = ScheduleOptions.toBitMaskInt(options) | SCHRQST_EXTFORMAT;

		DHANDLE.ByReference rethList = DHANDLE.newInstanceByReference();
		ShortByReference retListSize = new ShortByReference();

		result = NotesCAPI.get().ListAllocate((short) 0, 
				(short) 0,
				0, rethList, null, retListSize);
		
		NotesErrorUtils.checkResult(result);
		
		try {
			return LockUtil.lockHandle(rethList, (hListByVal) -> {
				Mem.OSUnlockObject(hListByVal);
				
				for (int i=0; i<namesCanonical.size(); i++) {
					String currName = namesCanonical.get(i);
					Memory currNameMem = NotesStringUtils.toLMBCS(currName, false);
	
	         if (currNameMem!=null && currNameMem.size() > 65535) {
	            throw new DominoException(MessageFormat.format("List item at position {0} exceeds max lengths of 65535 bytes", i));
	          }

	          char textSize = currNameMem==null ? 0 : (char) currNameMem.size();

					short res = NotesCAPI.get().ListAddEntry(hListByVal, 0, retListSize, (char) i, currNameMem,
					    textSize);
					
					NotesErrorUtils.checkResult(res);
				}
				
				Pointer valuePtr = Mem.OSLockObject(hListByVal);
				DHANDLE.ByReference rethCntnr = DHANDLE.newInstanceByReference();
				short res;
				
				res = NotesCAPI.get().SchRetrieve(unidStruct, null, optionsAsInt, intervalPair, valuePtr, rethCntnr,
						null, null,null);
				
				NotesErrorUtils.checkResult(res);
				
				return new JNASchedules(getParent(), rethCntnr,
						apptUnid, options,
						from,  until, names);
			});
		}
		finally {
			result = LockUtil.lockHandle(rethList, (hListByVal) -> {
				Mem.OSUnlockObject(hListByVal);
				
				return Mem.OSMemFree(hListByVal);
			});
			
			NotesErrorUtils.checkResult(result);
		}
	}
}
