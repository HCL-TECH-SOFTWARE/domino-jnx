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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.hcl.domino.commons.views.CollateType;
import com.hcl.domino.commons.views.NotesCollateDescriptor;
import com.hcl.domino.commons.views.NotesCollationInfo;
import com.hcl.domino.jna.internal.JNADumpUtil;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.structs.NotesCollateDescriptorStruct;
import com.hcl.domino.jna.internal.structs.NotesCollationStruct;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Pointer;

/**
 * Utility class to decode the COLLATION and COLLATE_DESCRIPTOR data structures from
 * view note items of type TYPE_COLLATION (e.g. $Collation, $Collation1, $Collation2 etc.).
 * 
 * @author Karsten Lehmann
 */
public class CollationDecoder {

	/**
	 * Decodes the item value. Extracted data is returned as {@link NotesCollationInfo} object
	 * 
	 * @param dataPtr item value pointer
	 * @return collation info
	 */
	public static NotesCollationInfo decodeCollation(Pointer dataPtr) {
		NotesCollationStruct collationStruct = NotesCollationStruct.newInstance(dataPtr);
		collationStruct.read();

		//sanity check that the signature byte is at the right position
		if (NotesConstants.COLLATION_SIGNATURE != collationStruct.signature) {
			throw new AssertionError(MessageFormat.format("Collation signature byte is not correct.\nMem dump:\n{0}", JNADumpUtil.dumpAsAscii(dataPtr, JNANotesConstants.notesCollationSize)));
		}
		
		List<NotesCollateDescriptor> collateDescriptors = new ArrayList<>();
		int items = collationStruct.Items & 0xffff;
		
		long baseOffsetDescriptors = JNANotesConstants.notesCollationSize;
		long baseOffsetTextBuffer = baseOffsetDescriptors + (items * JNANotesConstants.notesCollateDescriptorSize);
		
		NotesCollationInfo collationInfo = new NotesCollationInfo(collationStruct.Flags, collateDescriptors);

		for (int i=0; i<items; i++) {
			NotesCollateDescriptorStruct descStruct = NotesCollateDescriptorStruct.newInstance(dataPtr.share(baseOffsetDescriptors + i*JNANotesConstants.notesCollateDescriptorSize));
			descStruct.read();
			
			byte currDescFlags = descStruct.Flags;
			CollateType currDescType;
			try {
				currDescType = CollateType.toType(descStruct.keytype);
			}
			catch (IllegalArgumentException e) {
				throw new AssertionError(MessageFormat.format(
					"Collation structure invalid, collate type {0} unknown for column #{1}.\nMem dump:\n{2}",
					descStruct.keytype, i, JNADumpUtil.dumpAsAscii(dataPtr, JNANotesConstants.notesCollationSize + (items * JNANotesConstants.notesCollateDescriptorSize))
				));
			}
			
			//sanity check that the signature byte is at the right position
			if (NotesConstants.COLLATE_DESCRIPTOR_SIGNATURE != descStruct.signature) {
				throw new AssertionError(MessageFormat.format("Descriptor signature byte is not correct.\nMem dump:\n{0}", JNADumpUtil.dumpAsAscii(dataPtr, JNANotesConstants.notesCollationSize + (items * JNANotesConstants.notesCollateDescriptorSize))));
			}
			
			int currTextBufferOffset = descStruct.NameOffset & 0xffff;
			int currTextBufferLength = descStruct.NameLength & 0xffff;
			Pointer currTextPtr = dataPtr.share(baseOffsetTextBuffer + currTextBufferOffset);
			String currName = NotesStringUtils.fromLMBCS(currTextPtr, currTextBufferLength);
			
			NotesCollateDescriptor newDesc = new NotesCollateDescriptor(collationInfo, currName, currDescType, currDescFlags);
			collateDescriptors.add(newDesc);
		}
		
		return collationInfo;
	}
}
