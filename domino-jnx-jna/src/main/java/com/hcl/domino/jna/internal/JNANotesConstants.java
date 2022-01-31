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

import com.hcl.domino.jna.internal.structs.HtmlApi_UrlArgStruct;
import com.hcl.domino.jna.internal.structs.HtmlApi_UrlTargetComponentStruct;
import com.hcl.domino.jna.internal.structs.IntlFormatStruct;
import com.hcl.domino.jna.internal.structs.NoteIdStruct;
import com.hcl.domino.jna.internal.structs.NotesAdminpRequestProfileStruct;
import com.hcl.domino.jna.internal.structs.NotesCDResourceStruct;
import com.hcl.domino.jna.internal.structs.NotesCollateDescriptorStruct;
import com.hcl.domino.jna.internal.structs.NotesCollationStruct;
import com.hcl.domino.jna.internal.structs.NotesFileObjectStruct;
import com.hcl.domino.jna.internal.structs.NotesItemValueTableLargeStruct;
import com.hcl.domino.jna.internal.structs.NotesItemValueTableStruct;
import com.hcl.domino.jna.internal.structs.NotesMIMEPartStruct;
import com.hcl.domino.jna.internal.structs.NotesNumberPairStruct;
import com.hcl.domino.jna.internal.structs.NotesOriginatorIdStruct;
import com.hcl.domino.jna.internal.structs.NotesRangeStruct;
import com.hcl.domino.jna.internal.structs.NotesReplicationHistorySummaryStruct;
import com.hcl.domino.jna.internal.structs.NotesSchedEntryExtStruct;
import com.hcl.domino.jna.internal.structs.NotesSchedEntryStruct;
import com.hcl.domino.jna.internal.structs.NotesScheduleListStruct;
import com.hcl.domino.jna.internal.structs.NotesScheduleStruct;
import com.hcl.domino.jna.internal.structs.NotesTableItemLargeStruct;
import com.hcl.domino.jna.internal.structs.NotesTableItemStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDatePairStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeStruct;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.hcl.domino.jna.internal.structs.NotesViewColumnFormat2Struct;
import com.hcl.domino.jna.internal.structs.NotesViewColumnFormat3Struct;
import com.hcl.domino.jna.internal.structs.NotesViewColumnFormat4Struct;
import com.hcl.domino.jna.internal.structs.NotesViewColumnFormat5Struct;
import com.hcl.domino.jna.internal.structs.NotesViewColumnFormatStruct;
import com.hcl.domino.jna.internal.structs.NotesViewTableFormat2Struct;
import com.hcl.domino.jna.internal.structs.NotesViewTableFormat4Struct;
import com.hcl.domino.jna.internal.structs.NotesViewTableFormat5Struct;
import com.hcl.domino.jna.internal.structs.NotesViewTableFormatStruct;
import com.hcl.domino.jna.internal.structs.StringListStruct;
import com.hcl.domino.jna.internal.structs.ValueUnion;
import com.sun.jna.Pointer;

public interface JNANotesConstants {
	int timeDateSize = NotesTimeDateStruct.newInstance().size();
	int rangeSize = NotesRangeStruct.newInstance().size();
	int timeSize = NotesTimeStruct.newInstance().size();
	int winNamesListHeaderSize64 = WinNotesNamesListHeader64Struct.newInstance().size();
	int winNamesListHeaderSize32 = WinNotesNamesListHeader32Struct.newInstance().size();
	int namesListHeaderSize32 = NotesNamesListHeader32Struct.newInstance().size();
	int linuxNamesListHeaderSize64 = LinuxNotesNamesListHeader64Struct.newInstance().size();
	int macNamesListHeaderSize64 = MacNotesNamesListHeader64Struct.newInstance().size();
	int intlFormatSize = IntlFormatStruct.newInstance().size();
	int oidSize = NotesOriginatorIdStruct.newInstance().size();
	int notesViewTableFormatSize = NotesViewTableFormatStruct.newInstance().size();
	int notesViewTableFormat2Size = NotesViewTableFormat2Struct.newInstance().size();
	int notesViewTableFormat4Size = NotesViewTableFormat4Struct.newInstance().size();
	int notesViewTableFormat5Size = NotesViewTableFormat5Struct.newInstance().size();
	int notesViewColumnFormatSize = NotesViewColumnFormatStruct.newInstance().size();
	int notesViewColumnFormat2Size = NotesViewColumnFormat2Struct.newInstance().size();
	int notesViewColumnFormat3Size = NotesViewColumnFormat3Struct.newInstance().size();
	int notesViewColumnFormat4Size = NotesViewColumnFormat4Struct.newInstance().size();
	int notesViewColumnFormat5Size = NotesViewColumnFormat5Struct.newInstance().size();
	int numberPairSize = NotesNumberPairStruct.newInstance().size();
	int timeDatePairSize = NotesTimeDatePairStruct.newInstance().size();
	int fileObjectSize = NotesFileObjectStruct.newInstance().size();
	int notesCollationSize = NotesCollationStruct.newInstance().size();
	int notesCollateDescriptorSize = NotesCollateDescriptorStruct.newInstance().size();
	int notesUniversalNoteIdSize = NotesUniversalNoteIdStruct.newInstance().size();
	int mimePartSize = NotesMIMEPartStruct.newInstance().size();
	int tableItemSize = NotesTableItemStruct.newInstance().size();
	int tableItemLargeSize = NotesTableItemLargeStruct.newInstance().size();
	int itemValueTableSize = NotesItemValueTableStruct.newInstance().size();
	int itemValueTableLargeSize = NotesItemValueTableLargeStruct.newInstance().size();
	int scheduleSize = NotesScheduleStruct.newInstance().size();
	int schedListSize = NotesScheduleListStruct.newInstance().size();
	int schedEntrySize = NotesSchedEntryStruct.newInstance().size();
	int schedEntryExtSize = NotesSchedEntryExtStruct.newInstance().size();
	int valueUnionSize = Math.max(
			ValueUnion.newInstance(0).size(),
			Math.max(ValueUnion.newInstance(NotesUniversalNoteIdStruct.newInstance()).size(),
					Math.max(ValueUnion.newInstance(NoteIdStruct.newInstance()).size(), ValueUnion.newInstance(StringListStruct.newInstance()).size())));
	
	int htmlApiUrlTargetComponentSize = Math.max(
			HtmlApi_UrlTargetComponentStruct.newInstance(0, 0, ValueUnion.newInstance(new Pointer(0))).size(),
			Math.max(
					HtmlApi_UrlTargetComponentStruct.newInstance(0, 0, ValueUnion.newInstance(NotesUniversalNoteIdStruct.newInstance())).size(),
					HtmlApi_UrlTargetComponentStruct.newInstance(0, 0, ValueUnion.newInstance(StringListStruct.newInstance())).size()
					)
			);
	int htmlApiUrlArgSize = Math.max(
			HtmlApi_UrlArgStruct.newInstance(0, 0, ValueUnion.newInstance(new Pointer(0))).size(),
			Math.max(
					HtmlApi_UrlArgStruct.newInstance(0, 0, ValueUnion.newInstance(NotesUniversalNoteIdStruct.newInstance())).size(),
					HtmlApi_UrlArgStruct.newInstance(0, 0, ValueUnion.newInstance(StringListStruct.newInstance())).size()
					)
			);
	int htmlApiUrlComponentSize = Math.max(htmlApiUrlTargetComponentSize, htmlApiUrlArgSize);
	int notesCDResourceStructSize = NotesCDResourceStruct.newInstance().size();
	int notesReplicationHistorySummaryStructSize = NotesReplicationHistorySummaryStruct.newInstance().size();
	int queueEntryHeaderSize = 8; //DHANDLE/DHANDLE with 32 bit
	int resultsStreamBufferHeaderSize = 12;
	int adminpRequestProfileStructSize = NotesAdminpRequestProfileStruct.newInstance().size();
}
