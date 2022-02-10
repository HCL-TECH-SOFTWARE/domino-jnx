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

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hcl.domino.calendar.CalendarActionData;
import com.hcl.domino.calendar.CalendarProcess;
import com.hcl.domino.calendar.Calendaring;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.structs.NotesCalendarActionDataStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.sun.jna.Memory;

/**
 * Additional data required to perform some actions in {@link Calendaring},
 * e.g. adding names to meetings and sending counter proposals.
 * 
 * @author Karsten Lehmann
 */
public class JNACalendarActionData implements CalendarActionData {
	private String delegateTo;
	private JNADominoDateTime changeToStart;
	private JNADominoDateTime changeToEnd;
	private boolean keepInformed;
	private List<String> addNamesReq;
	private List<String> addNamesOpt;
	private List<String> addNamesFYI;
	private List<String> removeNames;

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz==NotesCalendarActionDataStruct.ByValue.class || clazz==NotesCalendarActionDataStruct.class) {
			NotesCalendarActionDataStruct struct;
			if (clazz==NotesCalendarActionDataStruct.ByValue.class) {
				struct = NotesCalendarActionDataStruct.ByValue.newInstance();
			}
			else {
				struct = NotesCalendarActionDataStruct.newInstance();
			}

			if (this.delegateTo!=null) {
				struct.pszDelegateTo = NotesStringUtils.toLMBCS(NotesNamingUtils.toCanonicalName(this.delegateTo), true);
			}
			else {
				struct.pszDelegateTo = null;
			}

			if (this.changeToStart!=null) {
				struct.ptdChangeToStart = new Memory(JNANotesConstants.timeDateSize);
				NotesTimeDateStruct tdStruct = NotesTimeDateStruct.newInstance(struct.ptdChangeToStart);
				tdStruct.Innards = this.changeToStart.getInnards();
				tdStruct.write();
			}

			if (this.changeToEnd!=null) {
				struct.ptdChangeToEnd = new Memory(JNANotesConstants.timeDateSize);
				NotesTimeDateStruct tdStruct = NotesTimeDateStruct.newInstance(struct.ptdChangeToEnd);
				tdStruct.Innards = this.changeToEnd.getInnards();
				tdStruct.write();
			}

			Memory keepInformed = new Memory(4);
			keepInformed.setInt(0, (this.keepInformed ? 1 : 0));
			struct.pfKeepInformed = keepInformed;

			if (this.addNamesReq!=null && !this.addNamesReq.isEmpty()) {
				struct.pAddNamesReq = createNamesList(this.addNamesReq);
			}
			else {
				struct.pAddNamesReq = null;
			}
			if (this.addNamesOpt!=null && !this.addNamesOpt.isEmpty()) {
				struct.pAddNamesOpt = createNamesList(this.addNamesOpt);
			}
			else {
				struct.pAddNamesOpt = null;
			}
			if (this.addNamesFYI!=null && !this.addNamesFYI.isEmpty()) {
				struct.pAddNamesFYI = createNamesList(this.addNamesFYI);
			}
			else {
				struct.pAddNamesFYI = null;
			}
			if (this.removeNames!=null && !this.removeNames.isEmpty()) {
				struct.pRemoveNames = createNamesList(this.removeNames);
			}
			else {
				struct.pRemoveNames = null;
			}
			struct.write();
			return (T) struct;
		}
		return null;
	}

	private static Memory createNamesList(List<String> names) {
		if (names.size()> 65535) {
			throw new IllegalArgumentException("Max 65535 entries are allowed");
		}

		int totalSize = 2; //WORD for entries in the list
		List<Memory> namesMemList = new ArrayList<>();

		totalSize += 2*names.size(); //WORD indicating the name length for each entry

		for (String currName : names) {
			String currNameCanonical = NotesNamingUtils.toCanonicalName(currName);
			Memory currNameCanonicalMem = NotesStringUtils.toLMBCS(currNameCanonical, false);
			long currNameCanonicalMemSize = currNameCanonicalMem.size();
			if (currNameCanonicalMemSize>65535) {
				throw new IllegalArgumentException("List entry can only be max 65535 chars long: "+currName);
			}
			totalSize += currNameCanonicalMem.size(); //length of actual data
			namesMemList.add(currNameCanonicalMem);
		}

		Memory retMem = new Memory(totalSize);
		int offset = 0;
		retMem.setShort(offset, (short) (names.size() & 0xffff));
		offset+= 2;

		for (Memory currNameMem : namesMemList) {
			retMem.setShort(offset, (short) (currNameMem.size() & 0xffff));
			offset += 2;
		}

		for (Memory currNameMem : namesMemList) {
			byte[] currNameData = currNameMem.getByteArray(0, (int) currNameMem.size());
			retMem.write(offset, currNameData, 0, currNameData.length);
			offset += currNameData.length;
		}
		return retMem;
	}

	@Override
	public String getDelegateTo() {
		return delegateTo;
	}

	/**
	 * Sets the name of the delegated user if {@link CalendarProcess#DELEGATE} is used
	 * 
	 * @param delegateTo name either in abbreviated or canonical format
	 */
	@Override
	public CalendarActionData withDelegateTo(String delegateTo) {
		this.delegateTo = delegateTo;
		return this;
	}

	@Override
	public Temporal getChangeToStart() {
		return changeToStart==null ? null : changeToStart;
	}

	/**
	 * Sets the new start time for {@link CalendarProcess#COUNTER}
	 * 
	 * @param changeToStart new start time
	 */
	@Override
	public CalendarActionData withChangeToStart(Temporal changeToStart) {
		this.changeToStart = JNADominoDateTime.from(changeToStart);
		return this;
	}

	@Override
	public Temporal getChangeToEnd() {
		return changeToEnd==null ? null : changeToEnd.toTemporal().orElse(null);
	}

	/**
	 * Sets the new end time for {@link CalendarProcess#COUNTER}
	 * 
	 * @param changeToEnd new end time
	 */
	@Override
	public CalendarActionData withChangeToEnd(Temporal changeToEnd) {
		this.changeToEnd = JNADominoDateTime.from(changeToEnd);
		return this;
	}

	@Override
	public boolean isKeepInformed() {
		return keepInformed;
	}

	/**
	 * Sets whether the users wants to be kept informed, e.g. when cancelling
	 * an invivation via {@link CalendarProcess#CANCEL}
	 * 
	 * @param keepInformed true to be kept informed
	 */
	@Override
	public CalendarActionData withKeepInformed(boolean keepInformed) {
		this.keepInformed = keepInformed;
		return this;
	}

	@Override
	public List<String> getAddNamesRequired() {
		return addNamesReq;
	}

	/**
	 * Sets a new list of required attendees
	 * 
	 * @param addNamesReq attendees, either in canonical or abbreviated format
	 */
	@Override
	@SuppressWarnings("unchecked")
	public CalendarActionData withAddNamesRequired(String... addNamesReq) {
		this.addNamesReq = addNamesReq==null ? Collections.EMPTY_LIST :Arrays.asList(addNamesReq);
		return this;
	}

	@Override
	public List<String> getAddNamesOptional() {
		return addNamesOpt;
	}

	/**
	 * Sets a new list of optional attendees
	 * 
	 * @param addNamesOpt attendees, either in canonical or abbreviated format
	 */
	@Override
	@SuppressWarnings("unchecked")
	public CalendarActionData withAddNamesOptional(String... addNamesOpt) {
		this.addNamesOpt = addNamesOpt==null ? Collections.EMPTY_LIST : Arrays.asList(addNamesOpt);
		return this;
	}

	@Override
	public List<String> getAddNamesFYI() {
		return addNamesFYI;
	}

	/**
	 * Sets a new list of FYI attendees
	 * 
	 * @param addNamesFYI attendees, either in canonical or abbreviated format
	 */
	@Override
	@SuppressWarnings("unchecked")
	public CalendarActionData withAddNamesFYI(String... addNamesFYI) {
		this.addNamesFYI = addNamesFYI==null ? Collections.EMPTY_LIST : Arrays.asList(addNamesFYI);
		return this;
	}

	@Override
	public List<String> getRemoveNames() {
		return removeNames;
	}

	/**
	 * Sets a new list of attendees to be removed
	 * 
	 * @param removeNames attendees, either in canonical or abbreviated format
	 */
	@Override
	@SuppressWarnings("unchecked")
	public CalendarActionData withRemoveNames(String... removeNames) {
		this.removeNames = removeNames==null ? Collections.EMPTY_LIST : Arrays.asList(removeNames);
		return this;
	}

}
