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

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hcl.domino.commons.data.DefaultDominoDateRange;
import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.structs.NotesNumberPairStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDatePairStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.richtext.records.RecordType;
import com.sun.jna.Pointer;

public class ItemDecoder {
  
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
      // USHORT ListItems
      // USHORT[ListItems] StringLengths
      // ...string data
      
      int listCountAsInt = Short.toUnsignedInt(ptr.getShort(0));

      List<Object> listValues = new ArrayList<>(listCountAsInt);
      
      short[] strLens = ptr.getShortArray(2, listCountAsInt);

      int ptrPos = 2 + (listCountAsInt*2);
      for (int i = 0; i < listCountAsInt; i++) {
        int strLen = Short.toUnsignedInt(strLens[i]);
        Pointer pointerToTextInMem = ptr.share(ptrPos);

        if (strLen == 0) {
          listValues.add(""); //$NON-NLS-1$
        } else {
          if (convertStringsLazily) {
            byte[] stringDataArr = new byte[strLen];
            pointerToTextInMem.read(0, stringDataArr, 0, strLen);

            LMBCSString lmbcsString = new LMBCSString(stringDataArr);
            listValues.add(lmbcsString);
          } else {
            String currListEntry =
                NotesStringUtils.fromLMBCS(pointerToTextInMem, strLen);
            listValues.add(currListEntry);
          }
        }
        
        ptrPos += strLen;
      }

      return listValues;
    }
	
	public static DominoDateTime decodeTimeDateAsNotesTimeDate(final Pointer ptr, int valueLength) {
		int[] innards = ptr.getIntArray(0, 2);
		
		return new JNADominoDateTime(innards);
	}

	public static List<Object> decodeNumberList(Pointer ptr, int valueLength) {
		
		//read number of list and range entries in range
	  // WORD ListEntries
	  // WORD RangeEntries
		int listEntriesAsInt = Short.toUnsignedInt(ptr.getShort(0));
		int rangeEntriesAsInt = Short.toUnsignedInt(ptr.getShort(2));
		
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

    public static List<Object> decodeNumberList(ByteBuffer ptr, int valueLength) {
        //read number of list and range entries in range
        int listEntriesAsInt = Short.toUnsignedInt(ptr.getShort(0));
        int rangeEntriesAsInt = Short.toUnsignedInt(ptr.getShort(2));
        
        int bufferPos = JNANotesConstants.rangeSize;
        
        //we create an object list, because number ranges contain double[] array
        //(not sure whether number ranges exist in real life)
        List<Object> numberValues = new ArrayList<>(listEntriesAsInt + rangeEntriesAsInt);
        for (int t=0; t<listEntriesAsInt; t++) {
            double numVal = ptr.getDouble(bufferPos + t * 8);
            numberValues.add(numVal);
        }
        //skip list entries part of the buffer
        bufferPos += 8 * listEntriesAsInt;
        
        for (int t=0; t<rangeEntriesAsInt; t++) {
            double lower = ptr.getDouble(bufferPos + t * JNANotesConstants.numberPairSize);
            double upper = ptr.getDouble(bufferPos + t * JNANotesConstants.numberPairSize + 8);
            
            numberValues.add(new double[] {lower, upper});
        }
        
        return numberValues;
    }
	public static List<Object> decodeTimeDateListAsNotesTimeDate(Pointer ptr) {
		//read number of list and range entries in range
		// WORD ListEntries
		// WORD RangeEntries
		int listEntriesAsInt = Short.toUnsignedInt(ptr.getShort(0));
		int rangeEntriesAsInt = Short.toUnsignedInt(ptr.getShort(2));
		
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

    public static List<Object> decodeTimeDateListAsNotesTimeDate(ByteBuffer ptr) {
        //read number of list and range entries in range
        int listEntriesAsInt = Short.toUnsignedInt(ptr.getShort(0));
        int rangeEntriesAsInt = Short.toUnsignedInt(ptr.getShort(2));
        
        //skip range header
        int bufferPos = JNANotesConstants.rangeSize;
        
        List<Object> calendarValues = new ArrayList<>(listEntriesAsInt + rangeEntriesAsInt);
        
        for (int t=0; t<listEntriesAsInt; t++) {
            int[] innards = new int[2];
            innards[0] = ptr.getInt(bufferPos + t * JNANotesConstants.timeDateSize);
            innards[1] = ptr.getInt(bufferPos + t * JNANotesConstants.timeDateSize + 4);
            calendarValues.add(new JNADominoDateTime(innards));
        }
        
        //move position to the range data
        bufferPos += listEntriesAsInt * JNANotesConstants.timeDateSize;
        
        for (int t=0; t<rangeEntriesAsInt; t++) {
          int[] lowerTimeDateInnards = new int[2];
          lowerTimeDateInnards[0] = ptr.getInt(bufferPos + t * JNANotesConstants.timeDateSize);
          lowerTimeDateInnards[1] = ptr.getInt(bufferPos + t * JNANotesConstants.timeDateSize + 4);
          int[] upperTimeDateInnards = new int[2];
          upperTimeDateInnards[0] = ptr.getInt(bufferPos + t * JNANotesConstants.timeDateSize + 8);
          upperTimeDateInnards[1] = ptr.getInt(bufferPos + t * JNANotesConstants.timeDateSize + 12);
            
            DominoDateTime lowerTimeDate = new JNADominoDateTime(lowerTimeDateInnards);
            DominoDateTime upperTimeDate = new JNADominoDateTime(upperTimeDateInnards);
            
            calendarValues.add(new DefaultDominoDateRange(lowerTimeDate, upperTimeDate));
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
        return ptr.getDouble(0);
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
