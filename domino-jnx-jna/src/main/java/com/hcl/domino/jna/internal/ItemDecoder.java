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
package com.hcl.domino.jna.internal;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.hcl.domino.commons.data.DefaultDominoDateRange;
import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.structs.NotesTimeDatePairStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.richtext.records.RecordType;
import com.sun.jna.Pointer;

public class ItemDecoder {
  
  public static Object decodeTextValue(Pointer ptr, int valueLength, boolean convertStringsLazily, int offset) {
		if (valueLength<=0) {
			return ""; //$NON-NLS-1$
		}
		
		if (convertStringsLazily) {
			byte[] stringDataArr = new byte[valueLength];
			ptr.read(offset, stringDataArr, 0, valueLength);

			return new LMBCSString(stringDataArr);
		}
		else {
			return NotesStringUtils.fromLMBCS(ptr, valueLength, offset);
		}
	}
	
    public static List<Object> decodeTextListValue(Pointer ptr, boolean convertStringsLazily, int offset) {
      // USHORT ListItems
      // USHORT[ListItems] StringLengths
      // ...string data
      
      int listCountAsInt = Short.toUnsignedInt(ptr.getShort(offset));

      Object[] listValues = new Object[listCountAsInt];
      
      short[] strLens = ptr.getShortArray(offset+2, listCountAsInt);

      int ptrPos = offset + 2 + (listCountAsInt*2);
      for (int i = 0; i < listCountAsInt; i++) {
        int strLen = Short.toUnsignedInt(strLens[i]);

        if (strLen == 0) {
          listValues[i] = "";
        } else {
          if (convertStringsLazily) {
            byte[] stringDataArr = new byte[strLen];
            ptr.read(ptrPos, stringDataArr, 0, strLen);

            LMBCSString lmbcsString = new LMBCSString(stringDataArr);
            listValues[i] = lmbcsString;
          } else {
            String currListEntry =
                NotesStringUtils.fromLMBCS(ptr, strLen, ptrPos);
            listValues[i] = currListEntry;
          }
        }
        
        ptrPos += strLen;
      }

      return Arrays.asList(listValues);
    }
	
	public static DominoDateTime decodeTimeDateAsNotesTimeDate(final Pointer ptr, int valueLength, int offset) {
		int[] innards = ptr.getIntArray(offset, 2);
		
		return new JNADominoDateTime(innards);
	}

	public static List<Object> decodeNumberList(Pointer ptr, int valueLength, int offset) {
		
		//read number of list and range entries in range
	  // WORD ListEntries
	  // WORD RangeEntries
		int listEntriesAsInt = Short.toUnsignedInt(ptr.getShort(offset));
		int rangeEntriesAsInt = Short.toUnsignedInt(ptr.getShort(offset+2));
		
		//skip range header
		int offsetAfterRange = offset + JNANotesConstants.rangeSize;
		
		//we create an object list, because number ranges contain double[] array
		//(not sure whether number ranges exist in real life)
		Object[] numberValues = new Object[listEntriesAsInt + rangeEntriesAsInt];
		for (int t=0; t<listEntriesAsInt; t++) {
			double numVal = ptr.getDouble(offsetAfterRange + t * 8);
			numberValues[t] = numVal;
		}
		//skip list entries part of the buffer
		offsetAfterRange += 8 * listEntriesAsInt;
		
		for (int t=0; t<rangeEntriesAsInt; t++) {
			double[] numPair = ptr.getDoubleArray(offsetAfterRange + t * JNANotesConstants.numberPairSize, 2);
			numberValues[listEntriesAsInt+t] = numPair;
		}
		
		return Arrays.asList(numberValues);
	}
	
	public static List<Object> decodeTimeDateListAsNotesTimeDate(Pointer ptr, int offset) {
		//read number of list and range entries in range
		// WORD ListEntries
		// WORD RangeEntries
		int listEntriesAsInt = Short.toUnsignedInt(ptr.getShort(offset));
		int rangeEntriesAsInt = Short.toUnsignedInt(ptr.getShort(offset+2));
		
		//skip range header
		int offsetAfterRange = offset + JNANotesConstants.rangeSize;
		
		Object[] calendarValues = new Object[listEntriesAsInt + rangeEntriesAsInt];
		
		for (int t=0; t<listEntriesAsInt; t++) {
		  int entryOffset = offsetAfterRange + t * JNANotesConstants.timeDateSize;
			int[] innards = ptr.getIntArray(entryOffset, 2);
			calendarValues[t] = new JNADominoDateTime(innards);
		}
		
		//move position to the range data
		offsetAfterRange += listEntriesAsInt * JNANotesConstants.timeDateSize;
		
		// TODO switch to reading ints
		for (int t=0; t<rangeEntriesAsInt; t++) {
			Pointer ptrRangeEntry = ptr.share(offsetAfterRange + t * JNANotesConstants.timeDatePairSize);
			NotesTimeDatePairStruct timeDatePair = NotesTimeDatePairStruct.newInstance(ptrRangeEntry);
			timeDatePair.read();
			
			NotesTimeDateStruct lowerTimeDateStruct = timeDatePair.Lower;
			NotesTimeDateStruct upperTimeDateStruct = timeDatePair.Upper;
			
			int[] lowerTimeDateInnards = lowerTimeDateStruct.Innards;
			int[] upperTimeDateInnards = upperTimeDateStruct.Innards;
			
			DominoDateTime lowerTimeDate = new JNADominoDateTime(lowerTimeDateInnards);
			DominoDateTime upperTimeDate = new JNADominoDateTime(upperTimeDateInnards);
			
			calendarValues[listEntriesAsInt+t] = new DefaultDominoDateRange(lowerTimeDate, upperTimeDate);
		}
		
		return Arrays.asList(calendarValues);
	
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
			return decodeTextValue(ptr, length, false, 0);
		case TYPE_TEXT_LIST:
			return length==0 ? Collections.emptyList() : decodeTextListValue(ptr, false, 0);
		case TYPE_NUMBER:
        return ptr.getDouble(0);
		case TYPE_TIME:
			return decodeTimeDateAsNotesTimeDate(ptr, length, 0);
		case TYPE_NUMBER_RANGE:
			return decodeNumberList(ptr, length, 0);
		case TYPE_TIME_RANGE:
			return decodeTimeDateListAsNotesTimeDate(ptr, 0);
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
