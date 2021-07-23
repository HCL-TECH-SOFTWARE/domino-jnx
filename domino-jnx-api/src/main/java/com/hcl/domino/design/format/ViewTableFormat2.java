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
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructure;

@StructureDefinition(
	name="VIEW_TABLE_FORMAT2",
	members={
		@StructureMember(name="Length", type=short.class, unsigned=true),
		@StructureMember(name="BackgroundColor", type=short.class),
		@StructureMember(name="V2BorderColor", type=short.class),
		@StructureMember(name="TitleFont", type=FontStyle.class),
		@StructureMember(name="UnreadFont", type=FontStyle.class),
		@StructureMember(name="TotalsFont", type=FontStyle.class),
		@StructureMember(name="AutoUpdateSeconds", type=short.class, unsigned=true),
		@StructureMember(name="AlternateBackgroundColor", type=short.class),
		@StructureMember(name="wSig", type=ViewTableFormat2.FormatSignature.class),
		@StructureMember(name="LineCount", type=byte.class, unsigned=true),
		@StructureMember(name="Spacing", type=ViewTableFormat2.Spacing.class),
		@StructureMember(name="BackgroundColorExt", type=short.class),
		@StructureMember(name="HeaderLineCount", type=byte.class, unsigned=true),
		@StructureMember(name="Flags1", type=ViewTableFormat2.Flag.class, bitfield=true),
		@StructureMember(name="Spare", type=short[].class, length=4),
	}
)
public interface ViewTableFormat2 extends MemoryStructure {
	enum FormatSignature implements INumberEnum<Short> {
		PRE_V4((short)0),
		VALID(ViewFormatConstants.VALID_VIEW_FORMAT_SIG);
		private final short value;
		private FormatSignature(short value) { this.value = value; }
		@Override
		public Short getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum Spacing implements INumberEnum<Byte> {
		SINGLE_SPACE(ViewFormatConstants.VIEW_TABLE_SINGLE_SPACE),
		ONE_POINT_25_SPACE(ViewFormatConstants.VIEW_TABLE_ONE_POINT_25_SPACE),
		ONE_POINT_50_SPACE(ViewFormatConstants.VIEW_TABLE_ONE_POINT_50_SPACE),
		ONE_POINT_75_SPACE(ViewFormatConstants.VIEW_TABLE_ONE_POINT_75_SPACE),
		DOUBLE_SPACE(ViewFormatConstants.VIEW_TABLE_DOUBLE_SPACE);
		private final byte value;
		private Spacing(byte value) { this.value = value; }
		@Override
		public Byte getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum Flag implements INumberEnum<Byte> {
		HAS_LINK_COLUMN(ViewFormatConstants.VIEW_TABLE_HAS_LINK_COLUMN),
		HTML_PASSTHRU(ViewFormatConstants.VIEW_TABLE_HTML_PASSTHRU);
		private final byte value;
		private Flag(byte value) { this.value = value; }
		@Override
		public Byte getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	
	@StructureGetter("Length")
	int getLength();
	@StructureSetter("Length")
	ViewTableFormat2 setLength(int len);
	
	@StructureGetter("BackgroundColor")
	short getBackgroundColor();
	@StructureSetter("BackgroundColor")
	ViewTableFormat2 setBackgroundColor(short color);
	
	@StructureGetter("V2BorderColor")
	short getV2BorderColor();
	@StructureSetter("V2BorderColor")
	ViewTableFormat2 setV2BorderColor(short color);
	
	@StructureGetter("TitleFont")
	FontStyle getTitleFont();
	
	@StructureGetter("UnreadFont")
	FontStyle getUnreadFont();
	
	@StructureGetter("TotalsFont")
	FontStyle getTotalsFont();
	
	@StructureGetter("AutoUpdateSeconds")
	int getAutoUpdateSeconds();
	@StructureSetter("AutoUpdateSeconds")
	ViewTableFormat2 setAutoUpdateSeconds(int seconds);
	
	@StructureGetter("AlternateBackgroundColor")
	short getAlternateBackgroundColor();
	@StructureSetter("AlternateBackgroundColor")
	ViewTableFormat2 setAlternateBackgroundColor(short color);
	
	@StructureGetter("wSig")
	FormatSignature getSignature();
	@StructureSetter("wSig")
	ViewTableFormat2 setSignature(FormatSignature sig);
	
	@StructureGetter("LineCount")
	short getLineCount();
	@StructureSetter("LineCount")
	ViewTableFormat2 setLineCount(short lineCount);
	
	@StructureGetter("Spacing")
	Spacing getSpacing();
	@StructureSetter("Spacing")
	ViewTableFormat2 setSpacing(Spacing spacing);
	
	@StructureGetter("BackgroundColorExt")
	short getBackgroundColorExt();
	@StructureSetter("BackgroundColorExt")
	ViewTableFormat2 setBackgroundColorExt(short color);
	
	@StructureGetter("HeaderLineCount")
	short getHeaderLineCount();
	@StructureSetter("HeaderLineCount")
	ViewTableFormat2 setHeaderLineCount(short lineCount);
	
	@StructureGetter("Flags1")
	Set<Flag> getFlags();
	@StructureSetter("Flags1")
	ViewTableFormat2 setFlags(Collection<Flag> flags);
}
