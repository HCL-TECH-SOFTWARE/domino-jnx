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
package com.hcl.domino.jna.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import com.hcl.domino.commons.data.DefaultDominoDateRange;
import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.commons.util.NotesDateTimeUtils;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.structs.NotesNumberPairStruct;
import com.hcl.domino.jna.internal.structs.NotesRangeStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDatePairStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.richtext.records.RecordType;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ShortByReference;

public class ItemDecoder {

	public static double decodeNumber(Pointer ptr, int valueLength) {
		double numVal = ptr.getDouble(0);
		return numVal;
	}
	
	public static Object decodeTextValue(Pointer ptr, int valueLength, boolean convertStringsLazily) {
		if (valueLength<=0) {
			return ""; //$NON-NLS-1$
		}
		
		if (convertStringsLazily) {
			byte[] stringDataArr = new byte[valueLength];
			ptr.read(0, stringDataArr, 0, valueLength);

			LMBCSString lmbcsString = new LMBCSString(stringDataArr);
			return lmbcsString;
		}
		else {
			String txtVal = NotesStringUtils.fromLMBCS(ptr, valueLength);
			return txtVal;
		}
	}
	
	public static List<Object> decodeTextListValue(Pointer ptr, boolean convertStringsLazily) {
		//read a text list item value
		int listCountAsInt = ptr.getShort(0) & 0xffff;
		
		List<Object> listValues = new ArrayList<>(listCountAsInt);
		
		Memory retTextPointer = new Memory(Native.POINTER_SIZE);
		ShortByReference retTextLength = new ShortByReference();
		
		for (short l=0; l<listCountAsInt; l++) {
			short result = NotesCAPI.get().ListGetText(ptr, false, l, retTextPointer, retTextLength);
			NotesErrorUtils.checkResult(result);
			
			//retTextPointer[0] points to the list entry text
			Pointer pointerToTextInMem = retTextPointer.getPointer(0);
			int retTextLengthAsInt = retTextLength.getValue() & 0xffff;
			
			if (retTextLengthAsInt==0) {
				listValues.add(""); //$NON-NLS-1$
			}
			else {
				if (convertStringsLazily) {
					byte[] stringDataArr = new byte[retTextLengthAsInt];
					pointerToTextInMem.read(0, stringDataArr, 0, retTextLengthAsInt);

					LMBCSString lmbcsString = new LMBCSString(stringDataArr);
					listValues.add(lmbcsString);
				}
				else {
					String currListEntry = NotesStringUtils.fromLMBCS(pointerToTextInMem, (short) retTextLengthAsInt);
					listValues.add(currListEntry);
				}
			}
		}
		
		return listValues;
	}
	
	public static DominoDateTime decodeTimeDateAsNotesTimeDate(final Pointer ptr, int valueLength) {
		int[] innards = ptr.getIntArray(0, 2);
		
		return new JNADominoDateTime(innards);
	}
	
	public static Calendar decodeTimeDate(final Pointer ptr, int valueLength) {
		int[] innards = ptr.getIntArray(0, 2);
		@SuppressWarnings("deprecation")
		Calendar calDate = NotesDateTimeUtils.innardsToCalendar(innards);
		return calDate;
	}

	public static List<Object> decodeNumberList(Pointer ptr, int valueLength) {
		NotesRangeStruct range = NotesRangeStruct.newInstance(ptr);
		range.read();
		
		//read number of list and range entries in range
		int listEntriesAsInt = range.ListEntries & 0xffff;
		int rangeEntriesAsInt = range.RangeEntries & 0xffff;
		
		//skip range header
		Pointer ptrAfterRange = ptr.share(JNANotesConstants.rangeSize);
		
		//we create an object list, because number ranges contain double[] array
		//(not sure whether number ranges exist in real life)
		List<Object> numberValues = new ArrayList<>(listEntriesAsInt + rangeEntriesAsInt);
		for (int t=0; t<listEntriesAsInt; t++) {
			double numVal = ptrAfterRange.getDouble(t * 8);
			numberValues.add(numVal);
		}
		//skip list entries part of the buffer
		Pointer ptrAfterListEntries = ptrAfterRange.share(8 * listEntriesAsInt);
		
		for (int t=0; t<rangeEntriesAsInt; t++) {
			Pointer ptrListEntry = ptrAfterListEntries.share(t * JNANotesConstants.numberPairSize);
			NotesNumberPairStruct numPair = NotesNumberPairStruct.newInstance(ptrListEntry);
			numPair.read();
			double lower = numPair.Lower;
			double upper = numPair.Upper;
			
			numberValues.add(new double[] {lower, upper});
		}
		
		return numberValues;
	}
	public static List<Object> decodeTimeDateListAsNotesTimeDate(Pointer ptr) {
		NotesRangeStruct range = NotesRangeStruct.newInstance(ptr);
		range.read();
		
		//read number of list and range entries in range
		int listEntriesAsInt = range.ListEntries & 0xffff;
		int rangeEntriesAsInt = range.RangeEntries & 0xffff;
		
		//skip range header
		Pointer ptrAfterRange = ptr.share(JNANotesConstants.rangeSize);
		
		List<Object> calendarValues = new ArrayList<>(listEntriesAsInt + rangeEntriesAsInt);
		
		for (int t=0; t<listEntriesAsInt; t++) {
			Pointer ptrListEntry = ptrAfterRange.share(t * JNANotesConstants.timeDateSize);
			int[] innards = ptrListEntry.getIntArray(0, 2);
			calendarValues.add(new JNADominoDateTime(innards));
		}
		
		//move position to the range data
		Pointer ptrAfterListEntries = ptrAfterRange.share(listEntriesAsInt * JNANotesConstants.timeDateSize);
		
		for (int t=0; t<rangeEntriesAsInt; t++) {
			Pointer ptrRangeEntry = ptrAfterListEntries.share(t * JNANotesConstants.timeDatePairSize);
			NotesTimeDatePairStruct timeDatePair = NotesTimeDatePairStruct.newInstance(ptrRangeEntry);
			timeDatePair.read();
			
			NotesTimeDateStruct lowerTimeDateStruct = timeDatePair.Lower;
			NotesTimeDateStruct upperTimeDateStruct = timeDatePair.Upper;
			
			int[] lowerTimeDateInnards = lowerTimeDateStruct.Innards;
			int[] upperTimeDateInnards = upperTimeDateStruct.Innards;
			
			DominoDateTime lowerTimeDate = new JNADominoDateTime(lowerTimeDateInnards);
			DominoDateTime upperTimeDate = new JNADominoDateTime(upperTimeDateInnards);
			
			calendarValues.add(new DefaultDominoDateRange(lowerTimeDate, upperTimeDate));
		}
		
		return calendarValues;
	
	}
	
	/**
	 * @deprecated
	 * Use {@link #decodeTimeDateAsNotesTimeDate(Pointer, int)} instead
	 * 
	 * @param ptr a pointer to native time/date values
	 * @return a {@link List} of {@link Calendar} and {@code Calendar[]} objects
	 * 	representing the time/date values
	 */
	@Deprecated
	public static List<Object> decodeTimeDateList(Pointer ptr) {
		NotesRangeStruct range = NotesRangeStruct.newInstance(ptr);
		range.read();
		
		//read number of list and range entries in range
		int listEntriesAsInt = range.ListEntries & 0xffff;
		int rangeEntriesAsInt = range.RangeEntries & 0xffff;
		
		//skip range header
		Pointer ptrAfterRange = ptr.share(JNANotesConstants.rangeSize);
		
		List<Object> calendarValues = new ArrayList<>(listEntriesAsInt + rangeEntriesAsInt);
		
		for (int t=0; t<listEntriesAsInt; t++) {
			Pointer ptrListEntry = ptrAfterRange.share(t * JNANotesConstants.timeDateSize);
			int[] innards = ptrListEntry.getIntArray(0, 2);
			Calendar calDate = NotesDateTimeUtils.innardsToCalendar(innards);
			if (calDate!=null) {
				calendarValues.add(calDate);
			}
			else {
				//invalid TimeDate detected; we produce a "null" value to be able to detect this error
				Calendar nullCal = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
				nullCal.set(Calendar.YEAR, 1);
				nullCal.set(Calendar.MONTH, 1);
				nullCal.set(Calendar.DAY_OF_MONTH, 1);
				nullCal.set(Calendar.HOUR, 0);
				nullCal.set(Calendar.MINUTE, 0);
				nullCal.set(Calendar.SECOND, 0);
				nullCal.set(Calendar.MILLISECOND, 0);
				calendarValues.add(nullCal);
			}
		}
		
		//move position to the range data
		Pointer ptrAfterListEntries = ptrAfterRange.share(listEntriesAsInt * JNANotesConstants.timeDateSize);
		
		for (int t=0; t<rangeEntriesAsInt; t++) {
			Pointer ptrRangeEntry = ptrAfterListEntries.share(t * JNANotesConstants.timeDatePairSize);
			NotesTimeDatePairStruct timeDatePair = NotesTimeDatePairStruct.newInstance(ptrRangeEntry);
			timeDatePair.read();
			
			NotesTimeDateStruct lowerTimeDate = timeDatePair.Lower;
			NotesTimeDateStruct upperTimeDate = timeDatePair.Upper;
			
			int[] lowerTimeDateInnards = lowerTimeDate.Innards;
			int[] upperTimeDateInnards = upperTimeDate.Innards;
			
			Calendar lowerCalDate = NotesDateTimeUtils.innardsToCalendar(lowerTimeDateInnards);
			if (lowerCalDate==null) {
				//invalid TimeDate detected; we produce a "null" value to be able to detect this error
				lowerCalDate = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
				lowerCalDate.set(Calendar.YEAR, 1);
				lowerCalDate.set(Calendar.MONTH, 1);
				lowerCalDate.set(Calendar.DAY_OF_MONTH, 1);
				lowerCalDate.set(Calendar.HOUR, 0);
				lowerCalDate.set(Calendar.MINUTE, 0);
				lowerCalDate.set(Calendar.SECOND, 0);
				lowerCalDate.set(Calendar.MILLISECOND, 0);
			}
			Calendar upperCalDate = NotesDateTimeUtils.innardsToCalendar(upperTimeDateInnards);
			if (upperCalDate==null) {
				//invalid TimeDate detected; we produce a "null" value to be able to detect this error
				upperCalDate = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
				upperCalDate.set(Calendar.YEAR, 0);
				upperCalDate.set(Calendar.MONTH, 0);
				upperCalDate.set(Calendar.DAY_OF_MONTH, 0);
				upperCalDate.set(Calendar.HOUR, 0);
				upperCalDate.set(Calendar.MINUTE, 0);
				upperCalDate.set(Calendar.SECOND, 0);
				upperCalDate.set(Calendar.MILLISECOND, 0);
			}
			
			calendarValues.add(new Calendar[] {lowerCalDate, upperCalDate});
		}
		
		return calendarValues;
	}

	/**
	 * Reads a value from a value pointer in memory.
	 * 
	 * @param ptr a pointer to the data
	 * @param typeVal the type of the item data, corresponding to {@link ItemDataType} values
	 * @param length the length of the data itself
   * @param area the rich-text record category to use when interpreting
   *             composite data
	 * @return a parsed Java object representing the data
	 * @since 1.0.2
	 */
	public static Object readItemValue(Pointer ptr, short typeVal, int length, RecordType.Area area) {
		ItemDataType type = DominoEnumUtil.valueOf(ItemDataType.class, typeVal)
			.orElseThrow(() -> new IllegalArgumentException(MessageFormat.format("Unsupported data type: {0}", typeVal)));
		
		switch(type) {
		case TYPE_TEXT:
			return decodeTextValue(ptr, length, false);
		case TYPE_TEXT_LIST:
			return length==0 ? Collections.emptyList() : decodeTextListValue(ptr, false);
		case TYPE_NUMBER:
			return decodeNumber(ptr, length);
		case TYPE_TIME:
			return decodeTimeDateAsNotesTimeDate(ptr, length);
		case TYPE_NUMBER_RANGE:
			return decodeNumberList(ptr, length);
		case TYPE_TIME_RANGE:
			return decodeTimeDateListAsNotesTimeDate(ptr);
		case TYPE_COMPOSITE:
		  // Add back space for the type val skipped below
		  byte[] data = new byte[length+2];
		  ptr.read(0, data, 2, length);
		  return RichTextUtil.readMemoryRecords(data, area);
		default:
			throw new IllegalArgumentException(MessageFormat.format("Unsupported data type: {0}", type));
		}
	}
	
}
