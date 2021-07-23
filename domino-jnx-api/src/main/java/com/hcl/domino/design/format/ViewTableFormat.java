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
package com.hcl.domino.design.format;

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.ViewFormatConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.MemoryStructure;

@StructureDefinition(
	name="VIEW_TABLE_FORMAT",
	members={
		@StructureMember(name="Header", type=ViewFormatHeader.class),
		@StructureMember(name="Columns", type=short.class, unsigned=true),
		@StructureMember(name="ItemSequenceNumber", type=short.class, unsigned=true),
		@StructureMember(name="Flags", type=ViewTableFormat.Flag.class, bitfield=true),
		@StructureMember(name="Flags2", type=ViewTableFormat.Flag2.class, bitfield=true),
	}
)
public interface ViewTableFormat extends MemoryStructure {
	enum Flag implements INumberEnum<Short> {
		COLLAPSED(ViewFormatConstants.VIEW_TABLE_FLAG_COLLAPSED),
		FLATINDEX(ViewFormatConstants.VIEW_TABLE_FLAG_FLATINDEX),
		DISP_ALLUNREAD(ViewFormatConstants.VIEW_TABLE_FLAG_DISP_ALLUNREAD),
		CONFLICT(ViewFormatConstants.VIEW_TABLE_FLAG_CONFLICT),
		DISP_UNREADDOCS(ViewFormatConstants.VIEW_TABLE_FLAG_DISP_UNREADDOCS),
		GOTO_TOP_ON_OPEN(ViewFormatConstants.VIEW_TABLE_GOTO_TOP_ON_OPEN),
		GOTO_BOTTOM_ON_OPEN(ViewFormatConstants.VIEW_TABLE_GOTO_BOTTOM_ON_OPEN),
		ALTERNATE_ROW_COLORING(ViewFormatConstants.VIEW_TABLE_ALTERNATE_ROW_COLORING),
		HIDE_HEADINGS(ViewFormatConstants.VIEW_TABLE_HIDE_HEADINGS),
		HIDE_LEFT_MARGIN(ViewFormatConstants.VIEW_TABLE_HIDE_LEFT_MARGIN),
		SIMPLE_HEADINGS(ViewFormatConstants.VIEW_TABLE_SIMPLE_HEADINGS),
		VARIABLE_LINE_COUNT(ViewFormatConstants.VIEW_TABLE_VARIABLE_LINE_COUNT),
		GOTO_TOP_ON_REFRESH(ViewFormatConstants.VIEW_TABLE_GOTO_TOP_ON_REFRESH),
		GOTO_BOTTOM_ON_REFRESH(ViewFormatConstants.VIEW_TABLE_GOTO_BOTTOM_ON_REFRESH),
		EXTEND_LAST_COLUMN(ViewFormatConstants.VIEW_TABLE_EXTEND_LAST_COLUMN),
		RTLVIEW(ViewFormatConstants.VIEW_TABLE_RTLVIEW);
		private final short value;
		private Flag(short value) { this.value = value; }
		@Override
		public Short getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum Flag2 implements INumberEnum<Short> {
		FLAT_HEADINGS(ViewFormatConstants.VIEW_TABLE_FLAT_HEADINGS),
		COLORIZE_ICONS(ViewFormatConstants.VIEW_TABLE_COLORIZE_ICONS),
		HIDE_SB(ViewFormatConstants.VIEW_TABLE_HIDE_SB),
		HIDE_CAL_HEADER(ViewFormatConstants.VIEW_TABLE_HIDE_CAL_HEADER),
		NOT_CUSTOMIZED(ViewFormatConstants.VIEW_TABLE_NOT_CUSTOMIZED),
		SHOW_PARTIAL_THREADS(ViewFormatConstants.VIEW_TABLE_SHOW_PARITAL_THREADS),
		PARTIAL_FLATINDEX(ViewFormatConstants.VIEW_TABLE_FLAG_PARTIAL_FLATINDEX);
		private final short value;
		private Flag2(short value) { this.value = value; }
		@Override
		public Short getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	
	@StructureGetter("Header")
	ViewFormatHeader getHeader();
	
	@StructureGetter("Columns")
	int getColumnCount();
	@StructureSetter("Columns")
	ViewTableFormat setColumnCount(int count);
	
	@StructureGetter("ItemSequenceNumber")
	int getItemSequenceNumber();
	@StructureSetter("ItemSequenceNumber")
	ViewTableFormat setItemSequenceNumber(int seqNum);
	
	@StructureGetter("Flags")
	Set<Flag> getFlags();
	@StructureSetter("Flags")
	ViewTableFormat setFlags(Collection<Flag> flags);
	
	@StructureGetter("Flags2")
	Set<Flag2> getFlags2();
	@StructureSetter("Flags2")
	ViewTableFormat setFlags2(Collection<Flag2> flags);
}
