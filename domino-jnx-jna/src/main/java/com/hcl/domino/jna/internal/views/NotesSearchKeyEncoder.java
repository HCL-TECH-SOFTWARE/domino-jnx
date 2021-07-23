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
package com.hcl.domino.jna.internal.views;

import static java.text.MessageFormat.format;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

import com.hcl.domino.commons.util.InnardsConverter;
import com.hcl.domino.commons.util.NotesDateTimeUtils;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jna.data.JNADominoDateRange;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.structs.NotesItemValueTableStruct;
import com.hcl.domino.jna.internal.structs.NotesNumberPairStruct;
import com.hcl.domino.jna.internal.structs.NotesRangeStruct;
import com.hcl.domino.jna.internal.structs.NotesTableItemStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDatePairStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

public class NotesSearchKeyEncoder {
	
	/**
	 * Produces the keybuffer for NIFFindByKey
	 * 
	 * @param keys array of String, Double, Integer, NotesTimeDate, Calendar, Date, NotesDateRange, Calendar[] (with two elements lower/upper), Date[] (with two elements lower/upper)
	 * @return buffer with encoded keys
	 * @throws Exception in case of errors
	 */
	public static Memory encodeKeys(Object[] keys) throws Exception {
		ByteArrayOutputStream metaDataByteOut = new ByteArrayOutputStream();

		ByteArrayOutputStream valueDataByteOut = new ByteArrayOutputStream();
		
		for (int i=0; i<keys.length; i++) {
			Objects.requireNonNull(keys[i], () -> format("Keys cannot be null. keys={0}", Arrays.toString(keys)));
		}
		
		//write placeholder for the total buffer size and the number of items, will be filled later
		metaDataByteOut.write(0);
		metaDataByteOut.write(0);
		
		metaDataByteOut.write(0);
		metaDataByteOut.write(0);
		

		for (int i=0; i<keys.length; i++) {
			Object currKey = keys[i];
			
			if (currKey instanceof String) {
				addStringKey(metaDataByteOut, valueDataByteOut, (String) currKey);
			}
			else if (currKey instanceof Double) {
				addNumberKey(metaDataByteOut, valueDataByteOut, ((Double) currKey));
			}
			else if (currKey instanceof Float) {
				addNumberKey(metaDataByteOut, valueDataByteOut, ((Float) currKey).doubleValue());
			}
			else if (currKey instanceof Integer) {
				addNumberKey(metaDataByteOut, valueDataByteOut, ((Integer) currKey).doubleValue());
			}
			else if (currKey instanceof Long) {
				addNumberKey(metaDataByteOut, valueDataByteOut, ((Long) currKey).doubleValue());
			}
			else if (currKey instanceof Date) {
				Calendar cal = Calendar.getInstance();
				cal.setTime((Date) currKey);
				addCalendarKey(metaDataByteOut, valueDataByteOut, cal);
			}
			else if (currKey instanceof Calendar) {
				addCalendarKey(metaDataByteOut, valueDataByteOut, (Calendar) currKey);
			}
			else if (currKey instanceof JNADominoDateTime) {
				addCalendarKey(metaDataByteOut, valueDataByteOut, (JNADominoDateTime) currKey);
			}
			else if (currKey instanceof TemporalAccessor) {
				addCalendarKey(metaDataByteOut, valueDataByteOut, new JNADominoDateTime((TemporalAccessor)currKey));
			}
			else if (currKey instanceof Date[]) {
				Date[] dateArr = (Date[]) currKey;
				Calendar[] calArr = new Calendar[dateArr.length];
				for (int j=0; j<dateArr.length; j++) {
					calArr[j] = Calendar.getInstance();
					calArr[j].setTime(dateArr[j]);
				}
				
				addCalendarRangeKey(metaDataByteOut, valueDataByteOut, calArr);
			}
			else if (currKey instanceof Calendar[]) {
				//date range
				addCalendarRangeKey(metaDataByteOut, valueDataByteOut, (Calendar[]) currKey);
			}
			else if (currKey instanceof JNADominoDateRange) {
				//date range
				addCalendarRangeKey(metaDataByteOut, valueDataByteOut, (JNADominoDateRange) currKey);
			}
			else if (currKey instanceof double[]) {
				//looks like this does not work (the C API documentation says it does not work either)
				addNumberRangeKey(metaDataByteOut, valueDataByteOut, (double[]) currKey);
			}
			else if (currKey instanceof Double[]) {
				Double[] objArr = (Double[]) currKey;
				double[] doubleArr = new double[objArr.length];
				for (int j=0; j<objArr.length; j++) {
					if (objArr[j] != null) {
						doubleArr[j] = objArr[j];
					}
				}
				//looks like this does not work (the C API documentation says it does not work either)
				addNumberRangeKey(metaDataByteOut, valueDataByteOut, doubleArr);
			}
			else {
				throw new IllegalArgumentException(format("Unknown key type: {0}, class={1}", currKey, (currKey==null ? "null" : currKey.getClass().getName()))); //$NON-NLS-2$
			}
		}		
		
		byte[] metaDataByteArr = metaDataByteOut.toByteArray();
		byte[] valueDataByteArr = valueDataByteOut.toByteArray();
		
		Memory mem = new Memory(metaDataByteArr.length + valueDataByteArr.length);
		
		int offset = 0;
		for (int i=0; i<metaDataByteArr.length; i++) {
			mem.setByte(offset, metaDataByteArr[i]);
			offset++;
		}
		
		//update length value containing the total buffer size
		short totalSize = (short) ((4 + (keys.length * 4) + valueDataByteArr.length)  & 0xffff);
		
		NotesItemValueTableStruct itemTable = NotesItemValueTableStruct.newInstance(mem);
		itemTable.Length = totalSize;
		itemTable.Items = (short) keys.length;
		itemTable.write();
		
		for (int i=0; i<valueDataByteArr.length; i++) {
			mem.setByte(offset, valueDataByteArr[i]);
			offset++;
		}
		
		return mem;
	}

	/**
	 * Writes data for a time search key
	 * 
	 * @param itemOut output stream for ITEM structure
	 * @param valueDataOut output stream for search key value
	 * @param currKey search key
	 * @throws Exception in case of errors
	 */
	private static void addCalendarKey(OutputStream itemOut, OutputStream valueDataOut, JNADominoDateTime td) throws Exception {
		int[] innards = td.getInnards();
		addCalendarKey(itemOut, valueDataOut, innards[0], innards[1]);
	}
	
	/**
	 * Writes data for a time search key
	 * 
	 * @param itemOut output stream for ITEM structure
	 * @param valueDataOut output stream for search key value
	 * @param currKey search key
	 * @throws Exception in case of errors
	 */
	private static void addCalendarKey(OutputStream itemOut, OutputStream valueDataOut, Calendar currKey) throws Exception {
		boolean hasDate = NotesDateTimeUtils.hasDate(currKey);
		boolean hasTime = NotesDateTimeUtils.hasTime(currKey);
		
		int[] innards;
		if(hasDate && hasTime) {
			innards = InnardsConverter.encodeInnards(((GregorianCalendar)currKey).toZonedDateTime());
		} else if(hasDate) {
			LocalDate localDate = LocalDate.of(currKey.get(Calendar.YEAR), currKey.get(Calendar.MONTH), currKey.get(Calendar.DAY_OF_MONTH));
			innards = InnardsConverter.encodeInnards(localDate);
		} else {
			LocalTime localTime = LocalTime.of(currKey.get(Calendar.HOUR_OF_DAY), currKey.get(Calendar.MINUTE), currKey.get(Calendar.SECOND), currKey.get(Calendar.MILLISECOND)*1000*1000);
			innards = InnardsConverter.encodeInnards(localTime);
		}

		addCalendarKey(itemOut, valueDataOut, innards[0], innards[1]);
	}
	
	/**
	 * Writes data for a time search key
	 * 
	 * @param itemOut output stream for ITEM structure
	 * @param valueDataOut output stream for search key value
	 * @param innard0 first innard of date
	 * @param innard1 second innard of date
	 * @throws Exception in case of errors
	 */
	private static void addCalendarKey(OutputStream itemOut, OutputStream valueDataOut, int innard0, int innard1) throws Exception {
		Memory itemMem = new Memory(JNANotesConstants.tableItemSize);
		NotesTableItemStruct item = NotesTableItemStruct.newInstance(itemMem);
		item.NameLength = 0;
		item.ValueLength = (short) (JNANotesConstants.timeDateSize + 2);
		item.write();
		
		for (int i=0; i<JNANotesConstants.tableItemSize; i++) {
			itemOut.write(itemMem.getByte(i));
		}
		
		//write data type
		Memory valueMem = new Memory(2 + 8);
		valueMem.setShort(0, ItemDataType.TYPE_TIME.getValue().shortValue());
		
		Pointer timeDatePtr = valueMem.share(2);
		NotesTimeDateStruct timeDate = NotesTimeDateStruct.newInstance(timeDatePtr);
		timeDate.Innards[0] = innard0;
		timeDate.Innards[1] = innard1;
		timeDate.write();
		
		for (int i=0; i<valueMem.size(); i++) {
			valueDataOut.write(valueMem.getByte(i));
		}
	}
	
	/**
	 * Searching with number range keys is not supported yet (R9), as the 
	 * <a href="http://www-12.lotus.com/ldd/doc/domino_notes/9.0/api90ref.nsf/70cfe734675fd140852561ce00718042/35abe18f9580ca2d8525622e0062c48d?OpenDocument">documentation</a> says.
	 * 
	 * @param itemOut output stream for ITEM structure
	 * @param valueDataOut output stream for search key value
	 * @param currKey search key
	 * @throws Exception in case of errors
	 */
	private static void addNumberRangeKey(OutputStream itemOut, OutputStream valueDataOut, double[] currKey) throws Exception {
		if (currKey.length!=2) {
			throw new IllegalArgumentException(format("Double search key array must have exactly 2 elements. We found {0}", currKey.length));
		}
		
		Memory itemMem = new Memory(JNANotesConstants.tableItemSize);
		NotesTableItemStruct item = NotesTableItemStruct.newInstance(itemMem);
		item.NameLength = 0;
		item.ValueLength = (short) ((JNANotesConstants.rangeSize + JNANotesConstants.numberPairSize + 2) & 0xffff);
		item.write();

		for (int i=0; i<JNANotesConstants.tableItemSize; i++) {
			itemOut.write(itemMem.getByte(i));
		}

		Memory valueMem = new Memory(JNANotesConstants.rangeSize + JNANotesConstants.numberPairSize + 2);
		valueMem.setShort(0, ItemDataType.TYPE_NUMBER_RANGE.getValue().shortValue());

		Pointer rangePtr = valueMem.share(2);
		NotesRangeStruct range = NotesRangeStruct.newInstance(rangePtr);
		range.ListEntries = 0;
		range.RangeEntries = 1;
		range.write();

		Pointer pairPtr = rangePtr.share(JNANotesConstants.rangeSize);
		NotesNumberPairStruct pair = NotesNumberPairStruct.newInstance(pairPtr);
		pair.Lower = currKey[0];
		pair.Upper = currKey[1];
		pair.write();
		
		for (int i=0; i<valueMem.size(); i++) {
			valueDataOut.write(valueMem.getByte(i));
		}
	}
	
	/**
	 * Writes data for a time range search key
	 * 
	 * @param itemOut output stream for ITEM structure
	 * @param valueDataOut output stream for search key value
	 * @param currKey search key, array with two values
	 * @throws Exception in case of errors
	 */
	private static void addCalendarRangeKey(OutputStream itemOut, OutputStream valueDataOut, JNADominoDateRange currKey) throws Exception {
		int[] startInnards = ((JNADominoDateTime)currKey.getStartDateTime()).getInnards();
		int[] endInnards = ((JNADominoDateTime)currKey.getEndDateTime()).getInnards();
		
		addCalendarRangeKey(itemOut, valueDataOut, startInnards[0], startInnards[1], endInnards[0], endInnards[1]);
	}
	
	/**
	 * Writes data for a time range search key
	 * 
	 * @param itemOut output stream for ITEM structure
	 * @param valueDataOut output stream for search key value
	 * @param currKey search key, array with two values
	 * @throws Exception in case of errors
	 */
	private static void addCalendarRangeKey(OutputStream itemOut, OutputStream valueDataOut, Calendar[] currKey) throws Exception {
		if (currKey.length!=2) {
			throw new IllegalArgumentException(format("Calendar search key array must have exactly 2 elements. We found {0}", currKey.length));
		}
		
		int[] startInnards = InnardsConverter.encodeInnards(((GregorianCalendar)currKey[0]).toZonedDateTime());
		int[] endInnards = InnardsConverter.encodeInnards(((GregorianCalendar)currKey[1]).toZonedDateTime());
		
		addCalendarRangeKey(itemOut, valueDataOut, startInnards[0], startInnards[1], endInnards[0], endInnards[1]);
	}
	
	/**
	 * Writes data for a time range search key
	 * 
	 * @param itemOut output stream for ITEM structure
	 * @param valueDataOut output stream for search key value
	 * @param startInnard0 innard 0 of startdatetime
	 * @param startInnard1 innard 0 of startdatetime
	 * @param endInnard0 innard 0 of enddatetime
	 * @param endInnard1 innard 0 of enddatetime
	 * @throws Exception in case of errors
	 */
	private static void addCalendarRangeKey(OutputStream itemOut, OutputStream valueDataOut, int startInnard0, int startInnard1,
			int endInnard0, int endInnard1) throws Exception {
		
		Memory itemMem = new Memory(JNANotesConstants.tableItemSize);
		NotesTableItemStruct item = NotesTableItemStruct.newInstance(itemMem);
		item.NameLength = 0;
		item.ValueLength = (short) ((JNANotesConstants.rangeSize + JNANotesConstants.timeDatePairSize + 2) & 0xffff);
		item.write();

		for (int i=0; i<JNANotesConstants.tableItemSize; i++) {
			itemOut.write(itemMem.getByte(i));
		}

		Memory valueMem = new Memory(JNANotesConstants.rangeSize + JNANotesConstants.timeDatePairSize + 2);
		valueMem.setShort(0, ItemDataType.TYPE_TIME_RANGE.getValue().shortValue());
		
		Pointer rangePtr = valueMem.share(2);
		NotesRangeStruct range = NotesRangeStruct.newInstance(rangePtr);
		range.ListEntries = 0;
		range.RangeEntries = 1;
		range.write();
		
		Pointer pairPtr = rangePtr.share(JNANotesConstants.rangeSize);
		NotesTimeDatePairStruct pair = NotesTimeDatePairStruct.newInstance(pairPtr);
		pair.Lower = NotesTimeDateStruct.newInstance(new int[] {startInnard0, startInnard1});
		pair.Upper = NotesTimeDateStruct.newInstance(new int[] {endInnard0, endInnard1});
		pair.write();
		
		for (int i=0; i<valueMem.size(); i++) {
			valueDataOut.write(valueMem.getByte(i));
		}
	}

	/**
	 * Writes data for a string search key
	 * 
	 * @param itemOut output stream for ITEM structure
	 * @param valueDataOut output stream for search key value
	 * @param currKey search key
	 * @throws Exception in case of errors
	 */
	private static void addStringKey(OutputStream itemOut, OutputStream valueDataOut, String currKey) throws Exception {
		Memory strValueMem = NotesStringUtils.toLMBCS(currKey, false);
		
		Memory itemMem = new Memory(JNANotesConstants.tableItemSize);
		NotesTableItemStruct item = NotesTableItemStruct.newInstance(itemMem);
		item.NameLength = 0;
		item.ValueLength = (short) ((strValueMem.size() + 2) & 0xffff);
		item.write();

		for (int i=0; i<JNANotesConstants.tableItemSize; i++) {
			itemOut.write(itemMem.getByte(i));
		}

		Memory valueMem = new Memory(strValueMem.size() + 2);
		short txtType = ItemDataType.TYPE_TEXT.getValue().shortValue();
		valueMem.setShort(0, txtType);

		Pointer strValuePtr = valueMem.share(2);
		
		for (int i=0; i<strValueMem.size(); i++) {
			strValuePtr.setByte(i, strValueMem.getByte(i));
		}
		
		for (int i=0; i<valueMem.size(); i++) {
			valueDataOut.write(valueMem.getByte(i));
		}
	}

	/**
	 * Writes data for a number search key
	 * 
	 * @param itemOut output stream for ITEM structure
	 * @param valueDataOut output stream for search key value
	 * @param doubleValue search key
	 * @throws Exception in case of errors
	 */
	private static void addNumberKey(OutputStream itemOut, OutputStream valueDataOut, double doubleValue) throws Exception {
		Memory itemMem = new Memory(JNANotesConstants.tableItemSize);
		NotesTableItemStruct item = NotesTableItemStruct.newInstance(itemMem);
		item.NameLength = 0;
		item.ValueLength = (short) (8 + 2);
		item.write();

		for (int i=0; i<JNANotesConstants.tableItemSize; i++) {
			itemOut.write(itemMem.getByte(i));
		}

		Memory valueMem = new Memory(8 + 2);
		valueMem.setShort(0, ItemDataType.TYPE_NUMBER.getValue().shortValue());
		
		Pointer doubleValPtr = valueMem.share(2);
		doubleValPtr.setDouble(0, doubleValue);
		
		for (int i=0; i<valueMem.size(); i++) {
			valueDataOut.write(valueMem.getByte(i));
		}
	}


}
