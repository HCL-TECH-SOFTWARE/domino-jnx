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
package com.hcl.domino.jna.internal.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.data.AbstractTypedAccess;
import com.hcl.domino.commons.data.DefaultDominoDateRange;
import com.hcl.domino.commons.util.NotesDateTimeUtils;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.views.IItemTableData;
import com.hcl.domino.commons.views.IItemValueTableData;
import com.hcl.domino.commons.views.NotesCollectionStats;
import com.hcl.domino.commons.views.ReadMask;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.OpenDocumentMode;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.TypedAccess;
import com.hcl.domino.jna.data.JNACollectionEntry;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.data.JNADominoCollection;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.ItemDecoder;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.LMBCSString;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.search.ItemTableDataDocAdapter;
import com.hcl.domino.jna.internal.structs.NotesCollectionStatsStruct;
import com.hcl.domino.jna.internal.structs.NotesItemTableLargeStruct;
import com.hcl.domino.jna.internal.structs.NotesItemTableStruct;
import com.sun.jna.Pointer;

/**
 * Utility class to decode the buffer returned by data lookups, e.g. in {@link DominoCollection}s
 * and for database searches.
 * 
 * @author Karsten Lehmann
 */
public class NotesLookupResultBufferDecoder {
	
	/**
	 * Decodes the buffer
	 * 
	 * @param parentCollection parent collection
	 * @param bufferHandle buffer handle
	 * @param numEntriesSkipped entries skipped during collection scan
	 * @param numEntriesReturned entries read during collection scan
	 * @param returnMask bitmask used to fill the buffer with data
	 * @param signalFlags signal flags returned by NIFReadEntries, e.g. whether we have more data to read
	 * @param pos position to add to NotesViewLookupResultData object in case view data is read via {@link JNADominoCollection#findByKeyExtended2(Set, Set, Object...)}
	 * @param indexModifiedSequenceNo index modified sequence no
	 * @param retDiffTime only set in {@link JNADominoCollection#readEntriesExt(com.hcl.domino.jna.data.JNADominoCollectionPosition, com.hcl.domino.data.Navigate, boolean, int, com.hcl.domino.data.Navigate, int, Set, DominoDateTime, com.hcl.domino.jna.data.JNAIDTable, Integer)}
	 * @param convertStringsLazily true to delay string conversion until the first use
	 * @param convertDominoDateTimeToCalendar true to convert {@link JNADominoDateTime} values to {@link Calendar}
	 * @param singleColumnLookupName for single column lookups, programmatic name of lookup column
	 * @return collection data
	 */
	public static NotesViewLookupResultData decodeCollectionLookupResultBuffer(JNADominoCollection parentCollection,
			DHANDLE bufferHandle, int numEntriesSkipped, int numEntriesReturned,
			Set<ReadMask> returnMask, short signalFlags, String pos, int indexModifiedSequenceNo, DominoDateTime retDiffTime,
			boolean convertStringsLazily, boolean convertDominoDateTimeToCalendar, String singleColumnLookupName) {
		
		return LockUtil.lockHandle(bufferHandle, (handleByVal) -> {
			Pointer bufferPtr = Mem.OSLockObject(handleByVal);
			try {
				return decodeCollectionLookupResultBuffer(parentCollection, bufferPtr, numEntriesSkipped,
						numEntriesReturned, returnMask, signalFlags, pos, indexModifiedSequenceNo, retDiffTime,
						convertStringsLazily, convertDominoDateTimeToCalendar, singleColumnLookupName);
			}
			finally {
				Mem.OSUnlockObject(handleByVal);
				short result = Mem.OSMemFree(handleByVal);
				NotesErrorUtils.checkResult(result);
			}
		});
	}
	
	/**
	 * Decodes the buffer
	 * 
	 * @param parentCollection parent collection
	 * @param bufferPtr buffer pointer
	 * @param numEntriesSkipped entries skipped during collection scan
	 * @param numEntriesReturned entries read during collection scan
	 * @param returnMask bitmask used to fill the buffer with data
	 * @param signalFlags signal flags returned by NIFReadEntries, e.g. whether we have more data to read
	 * @param pos position to add to NotesViewLookupResultData object in case view data is read via {@link JNADominoCollection#findByKeyExtended2(Set, Set, Object...)}
	 * @param indexModifiedSequenceNo index modified sequence no
	 * @param retDiffTime only set in {@link JNADominoCollection#readEntriesExt(com.hcl.domino.jna.data.JNADominoCollectionPosition, com.hcl.domino.data.Navigate, boolean, int, com.hcl.domino.data.Navigate, int, Set, DominoDateTime, com.hcl.domino.jna.data.JNAIDTable, Integer)}
	 * @param convertStringsLazily true to delay string conversion until the first use
	 * @param convertDominoDateTimeToCalendar true to convert {@link JNADominoDateTime} values to {@link Calendar}
	 * @param singleColumnLookupName for single column lookups, programmatic name of lookup column
	 * @return collection data
	 */
	public static NotesViewLookupResultData decodeCollectionLookupResultBuffer(JNADominoCollection parentCollection,
			Pointer bufferPtr, int numEntriesSkipped, int numEntriesReturned,
			Set<ReadMask> returnMask, short signalFlags, String pos, int indexModifiedSequenceNo, DominoDateTime retDiffTime,
			boolean convertStringsLazily, boolean convertDominoDateTimeToCalendar, String singleColumnLookupName) {

		int bufferPos = 0;
		
		NotesCollectionStats collectionStats = null;

		if (returnMask.contains(ReadMask.COLLECTIONSTATS)) {
			NotesCollectionStatsStruct tmpStats = NotesCollectionStatsStruct.newInstance(bufferPtr);
			tmpStats.read();
			
			collectionStats = new NotesCollectionStats(tmpStats.TopLevelEntries, tmpStats.LastModifiedTime);
					
			bufferPos += tmpStats.size();
		}

		List<JNACollectionEntry> viewEntries = new ArrayList<>();
		
		final boolean decodeAllValues = true;

		if (returnMask.size()==1 && returnMask.contains(ReadMask.NOTEID)) {
			//special optimized case for reading only note ids
			int[] noteIds = new int[numEntriesReturned];
			bufferPtr.read(0, noteIds, 0, numEntriesReturned);
			
			for (int i=0; i<noteIds.length; i++) {
				JNACollectionEntry newData = new JNACollectionEntry(parentCollection);
				viewEntries.add(newData);
				newData.setNoteID(noteIds[i]);
			}
			
		}
		else {
			for (int i=0; i<numEntriesReturned; i++) {
				JNACollectionEntry newData = new JNACollectionEntry(parentCollection);
				viewEntries.add(newData);

				if (returnMask.contains(ReadMask.NOTEID)) {
					int entryNoteId = bufferPtr.getInt(bufferPos);
					newData.setNoteID(entryNoteId);

					bufferPos+=4;
				}

				if (returnMask.contains(ReadMask.NOTEUNID)) {
					long[] unidLongs = bufferPtr.getLongArray(bufferPos, 2);
					newData.setUNID(unidLongs);

					bufferPos+=16;
				}
				if (returnMask.contains(ReadMask.NOTECLASS)) {
					short noteClass = bufferPtr.getShort(bufferPos);
					newData.setNoteClass(noteClass);

					bufferPos+=2;
				}
				if (returnMask.contains(ReadMask.INDEXSIBLINGS)) {
					int siblingCount = bufferPtr.getInt(bufferPos);
					newData.setSiblingCount(siblingCount);

					bufferPos+=4;
				}
				if (returnMask.contains(ReadMask.INDEXCHILDREN)) {
					int childCount = bufferPtr.getInt(bufferPos);
					newData.setChildCount(childCount);

					bufferPos+=4;
				}
				if (returnMask.contains(ReadMask.INDEXDESCENDANTS)) {
					int descendantCount = bufferPtr.getInt(bufferPos);
					newData.setDescendantCount(descendantCount);

					bufferPos+=4;
				}
				if (returnMask.contains(ReadMask.INDEXANYUNREAD)) {
					boolean isAnyUnread = bufferPtr.getShort(bufferPos) == 1;
					newData.setAnyUnread(isAnyUnread);

					bufferPos+=2;
				}
				if (returnMask.contains(ReadMask.INDENTLEVELS)) {
					short indentLevels = bufferPtr.getShort(bufferPos);
					newData.setIndentLevels(indentLevels);

					bufferPos += 2;
				}
				if (returnMask.contains(ReadMask.SCORE)) {
					short score = bufferPtr.getShort(bufferPos);
					newData.setFTScore(score);

					bufferPos += 2;
				}
				if (returnMask.contains(ReadMask.INDEXUNREAD)) {
					boolean isUnread = bufferPtr.getShort(bufferPos) == 1;
					newData.setUnread(isUnread);

					bufferPos+=2;
				}
				if (returnMask.contains(ReadMask.INDEXPOSITION)) {
					short level = bufferPtr.getShort(bufferPos);
					int[] posArr = new int[level+1];
					bufferPtr.read(bufferPos + 2 /* level */  + 2 /* MinLevel+MaxLevel */, posArr, 0, level+1);

					newData.setPosition(posArr);

					bufferPos += 4 * (level + 2);
				}
				if (returnMask.contains(ReadMask.SUMMARYVALUES)) {
					//				The information in a view summary of values is as follows:
					//
					//					ITEM_VALUE_TABLE containing header information (total length of summary, number of items in summary)
					//					WORD containing the length of item #1 (including data type)
					//					WORD containing the length of item #2 (including data type)
					//					WORD containing the length of item #3 (including data type)
					//					...
					//					USHORT containing the data type of item #1
					//					value of item #1
					//					USHORT containing the data type of item #2
					//					value of item #2
					//					USHORT containing the data type of item #3
					//					value of item #3
					//					....

					int startBufferPosOfSummaryValues = bufferPos;

					Pointer itemValueTablePtr = bufferPtr.share(bufferPos);
					ItemValueTableDataImpl itemTableData = (ItemValueTableDataImpl) decodeItemValueTable(itemValueTablePtr,
							convertStringsLazily, convertDominoDateTimeToCalendar, decodeAllValues);

					//move to the end of the buffer
					bufferPos = startBufferPosOfSummaryValues + itemTableData.getTotalBufferLength();

					Object[] decodedItemValues = new Object[itemTableData.getItemsCount()];
					for (int c=0; c<itemTableData.getItemsCount(); c++) {
						decodedItemValues[c] = itemTableData.getItemValue(c);
					}
					newData.setColumnValues(decodedItemValues);
					//add some statistical information to the data object to be able to see which columns "pollute" the summary buffer
					newData.setColumnValueSizesInBytes(itemTableData.getItemValueLengthsInBytes());
				}
				if (returnMask.contains(ReadMask.SUMMARY)) {
					int startBufferPosOfSummaryValues = bufferPos;

					Pointer itemTablePtr = bufferPtr.share(bufferPos);
					ItemTableDataImpl itemTableData = (ItemTableDataImpl) decodeItemTable(itemTablePtr, convertStringsLazily,
							convertDominoDateTimeToCalendar, decodeAllValues);

					//move to the end of the buffer
					bufferPos = startBufferPosOfSummaryValues + itemTableData.getTotalBufferLength();

					Map<String,Object> itemValues = itemTableData.asMap(false);
					newData.setSummaryData(itemValues);
				}
				if (singleColumnLookupName!=null) {
					newData.setSingleColumnLookupName(singleColumnLookupName);
				}
			}
		}
		
		return new NotesViewLookupResultData(collectionStats, viewEntries, numEntriesSkipped, numEntriesReturned, signalFlags, pos, indexModifiedSequenceNo, retDiffTime);
	}

	/**
	 * Produces an item table by decoding an ITEM_VALUE_TABLE structure, which contains an ordered list of item values,
	 * and adding an array of column names
	 * 
	 * @param db parent database
	 * @param noteId note ID of document
	 * @param columnFormulasFixedOrder column item names/formulas in a fixed order (matching the summary buffer content)
	 * @param bufferPtr pointer to a buffer
	 * @param convertStringsLazily true to delay string conversion until the first use
	 * @param convertJNADominoDateTimeToCalendar true to convert {@link JNADominoDateTime} values to {@link Calendar}
	 * @param decodeAllValues true to decode all values in the buffer
	 * @return item value table data
	 */
	public static IItemTableData decodeItemValueTableWithColumnNames(
			Database db,
			int noteId,
			LinkedHashMap<String,String> columnFormulasFixedOrder,
			Pointer bufferPtr, boolean convertStringsLazily, boolean convertJNADominoDateTimeToCalendar, boolean decodeAllValues) {
		
		ItemValueTableDataImpl valueTable = (ItemValueTableDataImpl) decodeItemValueTable(bufferPtr, convertStringsLazily, convertJNADominoDateTimeToCalendar, decodeAllValues);
		IItemTableData itemTableData = new ItemTableDataImpl(db, noteId, columnFormulasFixedOrder, valueTable);
		return itemTableData;
	}
	
	/**
	 * Produces an item table by decoding an ITEM_VALUE_TABLE structure, which contains an ordered list of item values,
	 * and adding an array of column names
	 * 
	 * @param db parent database
	 * @param noteId note ID of document
	 * @param columnFormulasFixedOrder column item names/formulas in a fixed order (matching the summary buffer content)
	 * @param bufferPtr pointer to a buffer
	 * @param convertStringsLazily true to delay string conversion until the first use
	 * @param convertJNADominoDateTimeToCalendar true to convert {@link JNADominoDateTime} values to {@link Calendar}
	 * @param decodeAllValues true to decode all values in the buffer
	 * @return item value table data
	 */
	public static IItemTableData decodeItemValueTableLargeWithColumnNames(
			Database db,
			int noteId,
			LinkedHashMap<String,String> columnFormulasFixedOrder,
			Pointer bufferPtr, boolean convertStringsLazily, boolean convertJNADominoDateTimeToCalendar, boolean decodeAllValues) {

		ItemValueTableDataImpl valueTable = (ItemValueTableDataImpl) decodeItemValueTableLarge(bufferPtr, convertStringsLazily, convertJNADominoDateTimeToCalendar, decodeAllValues);
		IItemTableData itemTableData = new ItemTableDataImpl(db, noteId, columnFormulasFixedOrder, valueTable);
		return itemTableData;
	}
	
	/**
	 * Decodes a large item value table structure, which contains an ordered list of item values
	 * 
	 * @param bufferPtr pointer to a buffer
	 * @param convertStringsLazily true to delay string conversion until the first use
	 * @param convertJNADominoDateTimeToCalendar true to convert {@link JNADominoDateTime} values to {@link Calendar}
	 * @param decodeAllValues true to decode all values in the buffer
	 * @return item value table data
	 */
	public static IItemValueTableData decodeItemValueTableLarge(Pointer bufferPtr,
			boolean convertStringsLazily, boolean convertJNADominoDateTimeToCalendar, boolean decodeAllValues) {
		int bufferPos = 0;
		
		//skip item value table header
		bufferPos += JNANotesConstants.itemValueTableLargeSize;
		
//		The information in a view summary of values is as follows:
//
//			ITEM_VALUE_TABLE containing header information (total length of summary, number of items in summary)
//			WORD containing the length of item #1 (including data type)
//			WORD containing the length of item #2 (including data type)
//			WORD containing the length of item #3 (including data type)
//			...
//			USHORT containing the data type of item #1
//			value of item #1
//			USHORT containing the data type of item #2
//			value of item #2
//			USHORT containing the data type of item #3
//			value of item #3
//			....
		
		int totalBufferLength = bufferPtr.getInt(0);// & 0xffff;
		int itemsCount = bufferPtr.getShort(4) & 0xffff;
		
		int[] itemValueLengths = new int[itemsCount];
		//we don't have any item names:
		int[] itemNameLengths = null;
		bufferPos+=2;
		//read all item lengths
		for (int j=0; j<itemsCount; j++) {
			//convert USHORT to int without sign
			itemValueLengths[j] = (int) bufferPtr.getInt(bufferPos); // & 0xffff);
			bufferPos += 4;
		}

		ItemValueTableDataImpl data = new ItemValueTableDataImpl(convertStringsLazily);
		data.setPreferNotesTimeDates(!convertJNADominoDateTimeToCalendar);
		data.m_totalBufferLength = totalBufferLength;
		data.m_itemsCount = itemsCount;
		
		Pointer itemValuePtr = bufferPtr.share(bufferPos);
		populateItemValueTableLargeData(itemValuePtr, itemsCount, itemNameLengths, itemValueLengths, data,
				convertStringsLazily, convertJNADominoDateTimeToCalendar, decodeAllValues);

		return data;
	}
	
	/**
	 * Decodes an ITEM_VALUE_TABLE structure, which contains an ordered list of item values
	 * 
	 * @param bufferPtr pointer to a buffer
	 * @param convertStringsLazily true to delay string conversion until the first use
	 * @param convertJNADominoDateTimeToCalendar true to convert {@link JNADominoDateTime} values to {@link Calendar}
	 * @param decodeAllValues true to decode all values in the buffer
	 * @return item value table data
	 */
	public static IItemValueTableData decodeItemValueTable(Pointer bufferPtr,
			boolean convertStringsLazily, boolean convertJNADominoDateTimeToCalendar, boolean decodeAllValues) {
		int bufferPos = 0;
		
		//skip item value table header
		bufferPos += JNANotesConstants.itemValueTableSize;
		
//		The information in a view summary of values is as follows:
//
//			ITEM_VALUE_TABLE containing header information (total length of summary, number of items in summary)
//			WORD containing the length of item #1 (including data type)
//			WORD containing the length of item #2 (including data type)
//			WORD containing the length of item #3 (including data type)
//			...
//			USHORT containing the data type of item #1
//			value of item #1
//			USHORT containing the data type of item #2
//			value of item #2
//			USHORT containing the data type of item #3
//			value of item #3
//			....
		
		int totalBufferLength = bufferPtr.getShort(0) & 0xffff;
		int itemsCount = bufferPtr.getShort(2) & 0xffff;
		
		int[] itemValueLengths = new int[itemsCount];
		//we don't have any item names:
		int[] itemNameLengths = null;
		
		//read all item lengths
		for (int j=0; j<itemsCount; j++) {
			//convert USHORT to int without sign
			itemValueLengths[j] = (int) (bufferPtr.getShort(bufferPos) & 0xffff);
			bufferPos += 2;
		}

		ItemValueTableDataImpl data = new ItemValueTableDataImpl(convertStringsLazily);
		data.setPreferNotesTimeDates(!convertJNADominoDateTimeToCalendar);
		data.m_totalBufferLength = totalBufferLength;
		data.m_itemsCount = itemsCount;

		Pointer itemValuePtr = bufferPtr.share(bufferPos);
		populateItemValueTableData(itemValuePtr, itemsCount, itemNameLengths, itemValueLengths, data,
				convertStringsLazily, convertJNADominoDateTimeToCalendar, decodeAllValues);

		return data;
	}

	/**
	 * This utility method extracts the item values from the buffer
	 * 
	 * @param bufferPtr buffer pointer
	 * @param itemsCount number of items in the buffer
	 * @param itemValueLengths lengths of the item values
	 * @param retData data object to populate
	 * @param convertStringsLazily true to delay string conversion until the first use
	 * @param convertJNADominoDateTimeToCalendar true to convert {@link JNADominoDateTime} values to {@link Calendar}
	 * @param decodeAllValues true to decode all values in the buffer
	 */
	@SuppressWarnings("deprecation")
	private static void populateItemValueTableData(Pointer bufferPtr, int itemsCount,
			int[] itemNameLengths, int[] itemValueLengths, ItemValueTableDataImpl retData, boolean convertStringsLazily,
			boolean convertJNADominoDateTimeToCalendar, boolean decodeAllValues) {
		int bufferPos = 0;
		String[] itemNames = new String[itemsCount];
		int[] itemDataTypes = new int[itemsCount];
		Pointer[] itemValueBufferPointers = new Pointer[itemsCount];
		int[] itemValueBufferSizes = new int[itemsCount];
		Object[] decodedItemValues = new Object[itemsCount];
		
		for (int j=0; j<itemsCount; j++) {
			if (itemNameLengths!=null && itemNameLengths[j]>0) {
				itemNames[j] = NotesStringUtils.fromLMBCS(bufferPtr.share(bufferPos), itemNameLengths[j]);
				bufferPos += itemNameLengths[j];
			}
			
			//read data type
			if (itemValueLengths[j] == 0) {
				/* If an item has zero length it indicates an "empty" item in the
				summary. This might occur in a lower-level category and stand for a
				higher-level category that has already appeared. Or an empty item might
				be a field that is missing in a response doc. Just print * as a place
				holder and go on to the next item in the pSummary. */
				continue;
			}
			else {
				itemDataTypes[j] = bufferPtr.getShort(bufferPos) & 0xffff;
				
				//add data type size to position
				bufferPos += 2;
				
				//read item values
				itemValueBufferPointers[j] = bufferPtr.share(bufferPos);
				itemValueBufferSizes[j] = itemValueLengths[j] - 2;
				
				//skip item value
				bufferPos += (itemValueLengths[j] - 2);

				if (decodeAllValues) {
					int itemValueBufferSizeAsInt = (int) (itemValueBufferSizes[j] & 0xffffffff);

					if (itemDataTypes[j] == ItemDataType.TYPE_TEXT.getValue()) {
						Object strVal = ItemDecoder.decodeTextValue(itemValueBufferPointers[j], itemValueBufferSizeAsInt, convertStringsLazily);
						decodedItemValues[j] = strVal;
					}
					else if (itemDataTypes[j] == ItemDataType.TYPE_TEXT_LIST.getValue()) {
						//read a text list item value
						List<Object> listValues = itemValueBufferSizeAsInt==0 ? Collections.emptyList() : ItemDecoder.decodeTextListValue(itemValueBufferPointers[j], convertStringsLazily);
						decodedItemValues[j]  = listValues;
					}
					else if (itemDataTypes[j] == ItemDataType.TYPE_NUMBER.getValue()) {
						double numVal = ItemDecoder.decodeNumber(itemValueBufferPointers[j], itemValueBufferSizeAsInt);
						decodedItemValues[j] = numVal;
					}
					else if (itemDataTypes[j] == ItemDataType.TYPE_TIME.getValue()) {
						if (convertJNADominoDateTimeToCalendar) {
							Calendar cal = ItemDecoder.decodeTimeDate(itemValueBufferPointers[j], itemValueBufferSizeAsInt);
							decodedItemValues[j]  = cal;
						}
						else {
							DominoDateTime td = ItemDecoder.decodeTimeDateAsNotesTimeDate(itemValueBufferPointers[j], itemValueBufferSizeAsInt);
							decodedItemValues[j]  = td;
						}
					}
					else if (itemDataTypes[j] == ItemDataType.TYPE_NUMBER_RANGE.getValue()) {
						List<Object> numberList = ItemDecoder.decodeNumberList(itemValueBufferPointers[j], itemValueBufferSizeAsInt);
						decodedItemValues[j]  = numberList;
					}
					else if (itemDataTypes[j] == ItemDataType.TYPE_TIME_RANGE.getValue()) {
						List<Object> calendarValues;
						if (convertJNADominoDateTimeToCalendar) {
							calendarValues = ItemDecoder.decodeTimeDateList(itemValueBufferPointers[j]);
						}
						else {
							calendarValues = ItemDecoder.decodeTimeDateListAsNotesTimeDate(itemValueBufferPointers[j]);
						}
						decodedItemValues[j] = calendarValues;
					}
				}
			}
		}
		
		retData.m_itemValueBufferPointers = itemValueBufferPointers;
		retData.m_itemValueBufferSizes = itemValueBufferSizes;
		retData.m_itemValues = decodedItemValues;
		retData.m_itemDataTypes = itemDataTypes;
		retData.m_itemValueLengthsInBytes = itemValueLengths;
		
		if (retData instanceof ItemTableDataImpl) {
			((ItemTableDataImpl)retData).m_itemNames = itemNames;
		}
	}

	/**
	 * This utility method extracts the item values from the buffer
	 * 
	 * @param bufferPtr buffer pointer
	 * @param itemsCount number of items in the buffer
	 * @param itemValueLengths lengths of the item values
	 * @param retData data object to populate
	 * @param convertStringsLazily true to delay string conversion until the first use
	 * @param convertJNADominoDateTimeToCalendar true to convert {@link JNADominoDateTime} values to {@link Calendar}
	 * @param decodeAllValues true to decode all values in the buffer
	 */
	@SuppressWarnings("deprecation")
	private static void populateItemValueTableLargeData(Pointer bufferPtr, int itemsCount,
			int[] itemNameLengths, int[] itemValueLengths, ItemValueTableDataImpl retData, boolean convertStringsLazily,
			boolean convertJNADominoDateTimeToCalendar, boolean decodeAllValues) {
		int bufferPos = 0;
		String[] itemNames = new String[itemsCount];
		int[] itemDataTypes = new int[itemsCount];
		Pointer[] itemValueBufferPointers = new Pointer[itemsCount];
		int[] itemValueBufferSizes = new int[itemsCount];
		Object[] decodedItemValues = new Object[itemsCount];
		
		for (int j=0; j<itemsCount; j++) {
			if (itemNameLengths!=null && itemNameLengths[j]>0) {
				itemNames[j] = NotesStringUtils.fromLMBCS(bufferPtr.share(bufferPos), itemNameLengths[j]);
				bufferPos += itemNameLengths[j];
			}
			
			//read data type
			if (itemValueLengths[j] == 0) {
				/* If an item has zero length it indicates an "empty" item in the
				summary. This might occur in a lower-level category and stand for a
				higher-level category that has already appeared. Or an empty item might
				be a field that is missing in a response doc. Just print * as a place
				holder and go on to the next item in the pSummary. */
				continue;
			}
			else {
				itemDataTypes[j] = bufferPtr.getShort(bufferPos) & 0xffff;
				
				//add data type size to position
				bufferPos += 2;
				
				//read item values
				itemValueBufferPointers[j] = bufferPtr.share(bufferPos);
				itemValueBufferSizes[j] = itemValueLengths[j] - 2;
				
				//skip item value
				bufferPos += (itemValueLengths[j] - 2);

				if (decodeAllValues) {
					if (itemValueBufferSizes[j] > Integer.MAX_VALUE) {
						throw new IllegalArgumentException("Item value lengths exceeds MAXDWORD: "+itemValueBufferSizes[j]);
					}
					int itemValueBufferSizeAsInt = (int) (itemValueBufferSizes[j] & 0xffffffff);
					
					if (itemDataTypes[j] == ItemDataType.TYPE_TEXT.getValue()) {
						Object strVal = ItemDecoder.decodeTextValue(itemValueBufferPointers[j], itemValueBufferSizeAsInt, convertStringsLazily);
						decodedItemValues[j] = strVal;
					}
					else if (itemDataTypes[j] == ItemDataType.TYPE_TEXT_LIST.getValue()) {
						//read a text list item value
						List<Object> listValues = itemValueBufferSizeAsInt==0 ? Collections.emptyList() : ItemDecoder.decodeTextListValue(itemValueBufferPointers[j], convertStringsLazily);
						decodedItemValues[j]  = listValues;
					}
					else if (itemDataTypes[j] == ItemDataType.TYPE_NUMBER.getValue()) {
						double numVal = ItemDecoder.decodeNumber(itemValueBufferPointers[j], itemValueBufferSizeAsInt);
						decodedItemValues[j] = numVal;
					}
					else if (itemDataTypes[j] == ItemDataType.TYPE_TIME.getValue()) {
						if (convertJNADominoDateTimeToCalendar) {
							Calendar cal = ItemDecoder.decodeTimeDate(itemValueBufferPointers[j], itemValueBufferSizeAsInt);
							decodedItemValues[j]  = cal;
						}
						else {
							DominoDateTime td = ItemDecoder.decodeTimeDateAsNotesTimeDate(itemValueBufferPointers[j], itemValueBufferSizeAsInt);
							decodedItemValues[j]  = td;
						}
					}
					else if (itemDataTypes[j] == ItemDataType.TYPE_NUMBER_RANGE.getValue()) {
						List<Object> numberList = ItemDecoder.decodeNumberList(itemValueBufferPointers[j], itemValueBufferSizeAsInt);
						decodedItemValues[j]  = numberList;
					}
					else if (itemDataTypes[j] == ItemDataType.TYPE_TIME_RANGE.getValue()) {
						List<Object> calendarValues;
						if (convertJNADominoDateTimeToCalendar) {
							calendarValues = ItemDecoder.decodeTimeDateList(itemValueBufferPointers[j]);
						}
						else {
							calendarValues = ItemDecoder.decodeTimeDateListAsNotesTimeDate(itemValueBufferPointers[j]);
						}
						decodedItemValues[j] = calendarValues;
					}
				}
			}
		}
		
		retData.m_itemValueBufferPointers = itemValueBufferPointers;
		retData.m_itemValueBufferSizes = itemValueBufferSizes;
		retData.m_itemValues = decodedItemValues;
		retData.m_itemDataTypes = itemDataTypes;
		retData.m_itemValueLengthsInBytes = itemValueLengths;
		
		if (retData instanceof ItemTableDataImpl) {
			((ItemTableDataImpl)retData).m_itemNames = itemNames;
		}
	}
	
	/**
	 * Decodes an ITEM_TABLE_LARGE structure with item names and item values
	 * 
	 * @param bufferPtr pointer to a buffer
	 * @param convertStringsLazily true to delay string conversion until the first use
	 * @param convertJNADominoDateTimeToCalendar true to convert {@link JNADominoDateTime} values to {@link Calendar}
	 * @param decodeAllValues true to decode all values in the buffer
	 * @return data
	 */
	public static IItemTableData decodeItemTableLarge(Pointer bufferPtr,
			boolean convertStringsLazily, boolean convertJNADominoDateTimeToCalendar, boolean decodeAllValues) {
		int bufferPos = 0;
		NotesItemTableLargeStruct itemTable = NotesItemTableLargeStruct.newInstance(bufferPtr);
		itemTable.read();

		//skip item table header
		bufferPos += itemTable.size();

		int itemsCount = itemTable.getItemsAsInt();
		int[] itemValueLengths = new int[itemsCount];
		int[] itemNameLengths = new int[itemsCount];

		//read  ITEM_LARGE structures for each item
		for (int j=0; j<itemsCount; j++) {
			Pointer itemPtr = bufferPtr.share(bufferPos);
			itemNameLengths[j] = itemPtr.getShort(0) & 0xffff;
			//4 -> skip filler WORD
			itemValueLengths[j] = itemPtr.share(4).getInt(0);
			
			bufferPos += JNANotesConstants.tableItemLargeSize;
		}

		ItemTableDataImpl data = new ItemTableDataImpl(convertStringsLazily);
		data.setPreferNotesTimeDates(!convertJNADominoDateTimeToCalendar);
		data.m_totalBufferLength = itemTable.getLengthAsInt();
		data.m_itemsCount = itemsCount;
		
		Pointer itemValuePtr = bufferPtr.share(bufferPos);
		populateItemValueTableLargeData(itemValuePtr, itemsCount, itemNameLengths, itemValueLengths,
				data, convertStringsLazily, convertJNADominoDateTimeToCalendar, decodeAllValues);
		
		return data;
	}
	
	/**
	 * Decodes an ITEM_TABLE structure with item names and item values
	 * 
	 * @param bufferPtr pointer to a buffer
	 * @param convertStringsLazily true to delay string conversion until the first use
	 * @param convertJNADominoDateTimeToCalendar true to convert {@link JNADominoDateTime} values to {@link Calendar}
	 * @param decodeAllValues true to decode all values in the buffer
	 * @return data
	 */
	public static IItemTableData decodeItemTable(Pointer bufferPtr,
			boolean convertStringsLazily, boolean convertJNADominoDateTimeToCalendar, boolean decodeAllValues) {
		int bufferPos = 0;
		NotesItemTableStruct itemTable = NotesItemTableStruct.newInstance(bufferPtr);
		itemTable.read();
		
		//skip item table header
		bufferPos += itemTable.size();

//		typedef struct {
//			   USHORT Length; /*  total length of this buffer */
//			   USHORT Items;  /* number of items in the table */
//			/* now come an array of ITEMs */
//			/* now comes the packed text containing the item names. */
//			} ITEM_TABLE;					
		
		int itemsCount = itemTable.getItemsAsInt();
		int[] itemValueLengths = new int[itemsCount];
		int[] itemNameLengths = new int[itemsCount];
		
		//read ITEM structures for each item
		for (int j=0; j<itemsCount; j++) {
			Pointer itemPtr = bufferPtr.share(bufferPos);
			itemNameLengths[j] = itemPtr.getShort(0) & 0xffff;
			itemValueLengths[j] = itemPtr.share(2).getShort(0) & 0xffff;
			
			bufferPos += JNANotesConstants.tableItemSize;
		}
		
		ItemTableDataImpl data = new ItemTableDataImpl(convertStringsLazily);
		data.setPreferNotesTimeDates(!convertJNADominoDateTimeToCalendar);
		data.m_totalBufferLength = itemTable.getLengthAsInt();
		data.m_itemsCount = itemsCount;
		
		Pointer itemValuePtr = bufferPtr.share(bufferPos);
		populateItemValueTableData(itemValuePtr, itemsCount, itemNameLengths, itemValueLengths,
				data, convertStringsLazily, convertJNADominoDateTimeToCalendar, decodeAllValues);
		
		return data;
	}
	
	/**
	 * Container class for the data parsed from an ITEM_VALUE_TABLE structure
	 * 
	 * @author Karsten Lehmann
	 */
	private static class ItemValueTableDataImpl implements IItemValueTableData {
		protected Pointer[] m_itemValueBufferPointers;
		protected int[] m_itemValueBufferSizes;
		protected Object[] m_itemValues;
		protected int[] m_itemDataTypes;
		protected int m_totalBufferLength;
		protected int m_itemsCount;
		protected int[] m_itemValueLengthsInBytes;
		protected boolean m_convertStringsLazily;
		protected boolean m_freed;
		private boolean m_preferJNADominoDateTimes;
		
		public ItemValueTableDataImpl(boolean convertStringsLazily) {
			m_convertStringsLazily = convertStringsLazily;
		}
		
		public void free() {
			m_freed = true;
		}
		
		public boolean isFreed() {
			return m_freed;
		}
		
		@Override
		public void setPreferNotesTimeDates(boolean b) {
			m_preferJNADominoDateTimes = b;
		}
		
		@Override
		public boolean isPreferNotesTimeDates() {
			return m_preferJNADominoDateTimes;
		}

		@Override
		public Object getItemValue(int index) {
			int type = getItemDataType(index);
			
			if (m_itemValues[index] == null) {
				if (isFreed()) {
					throw new DominoException("Buffer already freed");
				}
				
				int itemValueBufferSizeAsInt = (int) (m_itemValueBufferSizes[index] & 0xffffffff);

				if (type == ItemDataType.TYPE_TEXT.getValue()) {
					m_itemValues[index] = ItemDecoder.decodeTextValue(m_itemValueBufferPointers[index], itemValueBufferSizeAsInt, m_convertStringsLazily);
				}
				else if (type == ItemDataType.TYPE_TEXT_LIST.getValue()) {
					//read a text list item value
					m_itemValues[index] = itemValueBufferSizeAsInt==0 ? Collections.emptyList() : ItemDecoder.decodeTextListValue(m_itemValueBufferPointers[index], m_convertStringsLazily);
				}
				else if (type == ItemDataType.TYPE_NUMBER.getValue()) {
					m_itemValues[index] = ItemDecoder.decodeNumber(m_itemValueBufferPointers[index], itemValueBufferSizeAsInt);
				}
				else if (type == ItemDataType.TYPE_TIME.getValue()) {
					//we always store JNADominoDateTime and convert to Calendar if requested by caller
					m_itemValues[index] = ItemDecoder.decodeTimeDateAsNotesTimeDate(m_itemValueBufferPointers[index], itemValueBufferSizeAsInt);
				}
				else if (type == ItemDataType.TYPE_NUMBER_RANGE.getValue()) {
					m_itemValues[index] = ItemDecoder.decodeNumberList(m_itemValueBufferPointers[index], itemValueBufferSizeAsInt);
				}
				else if (type == ItemDataType.TYPE_TIME_RANGE.getValue()) {
					//we always store a List of JNADominoDateTime and convert to Calendar if requested by caller
					m_itemValues[index] = ItemDecoder.decodeTimeDateListAsNotesTimeDate(m_itemValueBufferPointers[index]);
				}
			}
			
			if (type == ItemDataType.TYPE_TIME.getValue() && !isPreferNotesTimeDates()) {
				if (m_itemValues[index] instanceof JNADominoDateTime) {
					return NotesDateTimeUtils.timeDateToCalendar((JNADominoDateTime)m_itemValues[index]);
				}
				else if (m_itemValues[index] instanceof JNADominoDateTime[]) {
					JNADominoDateTime[] range = (JNADominoDateTime[]) m_itemValues[index];
					Calendar[] convertedRange = new Calendar[] {NotesDateTimeUtils.timeDateToCalendar(range[0]), NotesDateTimeUtils.timeDateToCalendar(range[1])};
					return convertedRange;
				}
				else {
					//should not happen
					return m_itemValues[index];
				}
			}
			else if (type == ItemDataType.TYPE_TIME_RANGE.getValue() && m_itemValues[index] instanceof List && !isPreferNotesTimeDates()) {
				@SuppressWarnings("unchecked")
				List<Object> tdList = (List<Object>) m_itemValues[index];
				
				List<Object> calList = new ArrayList<>(tdList.size());
				
				for (int i=0; i<tdList.size(); i++) {
					if (tdList.get(i) instanceof JNADominoDateTime) {
						calList.add(NotesDateTimeUtils.timeDateToCalendar((JNADominoDateTime) tdList.get(i)));
					}
					else if (tdList.get(i) instanceof DefaultDominoDateRange) {
						DefaultDominoDateRange range = (DefaultDominoDateRange) tdList.get(i);
						Calendar[] convertedRange = new Calendar[] {NotesDateTimeUtils.timeDateToCalendar((JNADominoDateTime)range.getStartDateTime()), NotesDateTimeUtils.timeDateToCalendar((JNADominoDateTime)range.getEndDateTime())};
						calList.add(convertedRange);
					}
					else if (tdList.get(i) instanceof JNADominoDateTime[]) {
						JNADominoDateTime[] range = (JNADominoDateTime[]) tdList.get(i);
						Calendar[] convertedRange = new Calendar[] {NotesDateTimeUtils.timeDateToCalendar(range[0]), NotesDateTimeUtils.timeDateToCalendar(range[1])};
						calList.add(convertedRange);
					}
					else {
						//should not happen
						calList.add(tdList.get(i));
					}
				}
				
				return calList;
			} else {
				return m_itemValues[index];
			}
		}
		
		@Override
		public int getItemDataType(int index) {
			return m_itemDataTypes[index];
		}
		
		/**
		 * Returns the total length of the summary buffer
		 * 
		 * @return length
		 */
		public int getTotalBufferLength() {
			return m_totalBufferLength;
		}
		
		@Override
		public int getItemsCount() {
			return m_itemsCount;
		}
		
		/**
		 * Returns the lengths of the encoded item values in bytes, e.g. for of each column
		 * in a collection (for {@link ReadMask#SUMMARYVALUES}) or for the summary buffer items
		 * returned for {@link ReadMask#SUMMARY}.
		 * 
		 * @return lengths
		 */
		public int[] getItemValueLengthsInBytes() {
			return m_itemValueLengthsInBytes;
		}
	}
	
	/**
	 * Container class for the data parsed from an ITEM_VALUE structure
	 * 
	 * @author Karsten Lehmann
	 */
	private static class ItemTableDataImpl extends ItemValueTableDataImpl implements IItemTableData {
		protected String[] m_itemNames;
		private ItemValueTableDataImpl m_wrappedValueTable;
		private Map<String,Boolean> m_itemExistence;
		private TypedAccess m_typedItems;
		private Database m_db;
		private int m_noteId;
		
		private LinkedHashMap<String,String> m_columnFormulasFixedOrder;
		private ItemTableDataDocAdapter m_itemTableDocAdapter;
		private boolean m_itemTableDocAdapterLoadFailed;
		
		public ItemTableDataImpl(Database db, int noteId, LinkedHashMap<String,String> columnFormulasFixedOrder,
				ItemValueTableDataImpl valueTable) {
			super(valueTable.m_convertStringsLazily);
			
			m_db = db;
			m_noteId = noteId;
			m_columnFormulasFixedOrder = columnFormulasFixedOrder;
			m_itemNames = columnFormulasFixedOrder==null ? new String[0] : columnFormulasFixedOrder.keySet().toArray(new String[0]);
			
			m_wrappedValueTable = valueTable;
			m_itemValueBufferPointers = valueTable.m_itemValueBufferPointers;
			m_itemValueBufferSizes = valueTable.m_itemValueBufferSizes;
			m_itemValues = valueTable.m_itemValues;
			m_itemDataTypes = valueTable.m_itemDataTypes;
			m_totalBufferLength = valueTable.m_totalBufferLength;
			m_itemsCount = valueTable.m_itemsCount;
			m_itemValueLengthsInBytes = valueTable.m_itemValueLengthsInBytes;
			
			m_typedItems = new AbstractTypedAccess() {
				
				@Override
				public List<String> getItemNames() {
					return ItemTableDataImpl.this.getItemNames();
				}

				@Override
				public boolean hasItem(String itemName) {
					return ItemTableDataImpl.this.hasItem(itemName);
				}
				
				@Override
				protected List<?> getItemValue(String itemName) {
					Object val = ItemTableDataImpl.this.get(itemName);
					if (val==null) {
						return null;
					}
					else if (val instanceof List) {
						return (List<?>) val;
					}
					else {
						return Arrays.asList(val);
					}
				}
			};
			setPreferNotesTimeDates(true);
		}
		
		public ItemTableDataImpl(boolean convertStringsLazily) {
			super(convertStringsLazily);
			
			m_typedItems = new AbstractTypedAccess() {
				
				@Override
				public List<String> getItemNames() {
					return ItemTableDataImpl.this.getItemNames();
				}

				@Override
				public boolean hasItem(String itemName) {
					return ItemTableDataImpl.this.hasItem(itemName);
				}

				@Override
				protected List<?> getItemValue(String itemName) {
					Object val = ItemTableDataImpl.this.get(itemName);
					if (val==null) {
						return null;
					}
					else if (val instanceof List) {
						return (List<?>) val;
					}
					else {
						return Arrays.asList(val);
					}
				}
			};
		}
		
		@Override
		public void free() {
			super.free();
			
			if (m_itemTableDocAdapter!=null && !m_itemTableDocAdapter.isFreed()) {
				m_itemTableDocAdapter.free();
			}
		}
		
		@Override
		public <T> T get(String itemName, Class<T> valueType, T defaultValue) {
			return m_typedItems.get(itemName, valueType, defaultValue);
		}
		
		@Override
		public <T> List<T> getAsList(String itemName, Class<T> valueType, List<T> defaultValue) {
			return m_typedItems.getAsList(itemName, valueType, defaultValue);
		}
		
		@Override
		public boolean hasItem(String itemName) {
			Boolean exists = null;
			if (m_itemExistence!=null) {
				exists = m_itemExistence.get(itemName);
			}
			if (exists==null) {
				//hash the result in case we have some really frequent calls for the same item
				for (String currItem : m_itemNames) {
					if (currItem.equalsIgnoreCase(itemName)) {
						exists = Boolean.TRUE;
						break;
					}
				}
				if (exists==null) {
					exists = Boolean.FALSE;
				}
				
				if (m_itemExistence==null) {
					m_itemExistence = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
				}
				m_itemExistence.put(itemName, exists);
			}
			return exists;
		}
		
		@Override
		public List<String> getItemNames() {
			return Arrays.asList(m_itemNames);
		}
		
		@Override
		public <T> Optional<T> getOptional(String itemName, Class<T> valueType) {
		  return m_typedItems.getOptional(itemName, valueType);
		}
		
		@Override
		public <T> Optional<List<T>> getAsListOptional(String itemName, Class<T> valueType) {
		  return m_typedItems.getAsListOptional(itemName, valueType);
		}
		
		private ItemTableDataDocAdapter getTableDocAdapter() {
			if (m_itemTableDocAdapter==null || m_itemTableDocAdapter.isFreed()) {
				if (!m_itemTableDocAdapterLoadFailed) {
					Optional<Document> doc = m_db.getDocumentById(m_noteId, EnumSet.of(OpenDocumentMode.SUMMARY_ONLY, OpenDocumentMode.NOOBJECTS));
					if (doc.isPresent()) {
						m_itemTableDocAdapter = new ItemTableDataDocAdapter((JNADocument) doc.get(), m_columnFormulasFixedOrder);
					}
					else {
						m_itemTableDocAdapterLoadFailed = true;
					}
				}
				
			}
			return m_itemTableDocAdapter;
		}
		
		public Object get(String itemName) {
			if (m_wrappedValueTable!=null && m_wrappedValueTable.isFreed()) {
				throw new DominoException("Buffer already freed");
			}
			
			for (int i=0; i<m_itemNames.length; i++) {
				if (m_itemNames[i].equalsIgnoreCase(itemName)) {
					Object val = null;
					
					if (getItemDataType(i) == ItemDataType.TYPE_ERROR.getValue()) {
						//TODO workaround for missing large item values in R12; fallback to document
						ItemTableDataDocAdapter docAdapter = getTableDocAdapter();
						if (docAdapter!=null) {
							val = docAdapter.get(itemName, Object.class, null);
						}
					}
					
					if (val==null) {
						val = getItemValue(i);
					}
					
					if (val instanceof LMBCSString) {
						return ((LMBCSString)val).getValue();
					}
					else if (val instanceof List) {
						@SuppressWarnings("unchecked")
						List<Object> valAsList = (List<Object>) val;
						for (int j=0; j<valAsList.size(); j++) {
							Object currListValue = valAsList.get(j);
							
							if (currListValue instanceof LMBCSString) {
								valAsList.set(j, ((LMBCSString)currListValue).getValue());
							}
						}
						return valAsList;
					}
					else {
						return val;
					}
				}
			}
			return null;
		}

		@Override
		public Map<String,Object> asMap() {
			return asMap(true);
		}
		
		@Override
		public Map<String,Object> asMap(boolean decodeLMBCS) {
			Map<String,Object> data = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			int itemCount = getItemsCount();
			for (int i=0; i<itemCount; i++) {
				Object val = getItemValue(i);
				
				if (val instanceof LMBCSString) {
					if (decodeLMBCS) {
						data.put(m_itemNames[i], ((LMBCSString)val).getValue());
					}
					else {
						data.put(m_itemNames[i], val);
					}
				}
				else if(!isPreferNotesTimeDates() && val instanceof JNADominoDateTime) {
					data.put(m_itemNames[i], NotesDateTimeUtils.timeDateToCalendar((JNADominoDateTime)val));
				}
				else if (val instanceof List) {
					if (decodeLMBCS) {
						//check for LMBCS strings and JNADominoDateTime
						List<?> valAsList = (List<?>) val;
						boolean hasLMBCS = false;
						boolean hasTimeDate = false;
						
						for (int j=0; j<valAsList.size(); j++) {
							if (valAsList.get(j) instanceof LMBCSString) {
								hasLMBCS = true;
								break;
							}
							else if (!isPreferNotesTimeDates() && valAsList.get(j) instanceof JNADominoDateTime) {
								hasTimeDate = true;
								break;
							}
						}
						
						if (hasLMBCS || hasTimeDate) {
							List<Object> convList = new ArrayList<>(valAsList.size());
							for (int j=0; j<valAsList.size(); j++) {
								Object currObj = valAsList.get(j);
								if (currObj instanceof LMBCSString) {
									convList.add(((LMBCSString)currObj).getValue());
								}
								else if (!isPreferNotesTimeDates() && currObj instanceof JNADominoDateTime) {
									convList.add(NotesDateTimeUtils.timeDateToCalendar((JNADominoDateTime)currObj));
								}
								else {
									convList.add(currObj);
								}
							}
							data.put(m_itemNames[i], convList);
						}
						else {
							data.put(m_itemNames[i], val);
						}
					}
					else {
						data.put(m_itemNames[i], val);
					}
				}
				else {
					data.put(m_itemNames[i], val);
				}
			}
			return data;
		}
	}
	
}
