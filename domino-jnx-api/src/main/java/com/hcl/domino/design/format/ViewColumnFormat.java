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

import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.misc.ViewFormatConstants;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.NFMT;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;
import com.hcl.domino.richtext.structures.TFMT;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(
	name="VIEW_COLUMN_FORMAT",
	members={
		@StructureMember(name="Signature", type=short.class),
		@StructureMember(name="Flags1", type=ViewColumnFormat.Flag.class, bitfield=true),
		@StructureMember(name="ItemNameSize", type=short.class, unsigned=true),
		@StructureMember(name="TitleSize", type=short.class, unsigned=true),
		@StructureMember(name="FormulaSize", type=short.class, unsigned=true),
		@StructureMember(name="ConstantValueSize", type=short.class, unsigned=true),
		@StructureMember(name="DisplayWidth", type=short.class, unsigned=true),
		@StructureMember(name="FontID", type=FontStyle.class),
		@StructureMember(name="Flags2", type=short.class),
		@StructureMember(name="NumberFormat", type=NFMT.class),
		@StructureMember(name="TimeFormat", type=TFMT.class),
		@StructureMember(name="FormatDataType", type=ViewColumnFormat.DataType.class),
		@StructureMember(name="ListSep", type=ViewColumnFormat.ListDelimiter.class)
	}
)
public interface ViewColumnFormat extends ResizableMemoryStructure {
	enum Flag implements INumberEnum<Short> {
		Sort(NotesConstants.VCF1_M_Sort), SortCategorize(NotesConstants.VCF1_M_SortCategorize),
		SortDescending(NotesConstants.VCF1_M_SortDescending), Hidden(NotesConstants.VCF1_M_Hidden),
		Response(NotesConstants.VCF1_M_Response), HideDetail(NotesConstants.VCF1_M_HideDetail),
		Icon(NotesConstants.VCF1_M_Icon), NoResize(NotesConstants.VCF1_M_NoResize),
		ResortAscending(NotesConstants.VCF1_M_ResortAscending), ResortDescending(NotesConstants.VCF1_M_ResortDescending),
		Twistie(NotesConstants.VCF1_M_Twistie), ResortToView(NotesConstants.VCF1_M_ResortToView),
		SecondResort(NotesConstants.VCF1_M_SecondResort), SecondResortDescending(NotesConstants.VCF1_M_SecondResortDescending),
		/** @deprecated replaced by {@link ViewColumnFormat2.Flag3#CaseSensitiveSortInV5} */
		CaseInsensitiveSort(NotesConstants.VCF1_M_CaseInsensitiveSort),
		/** @deprecated replaced by {@link ViewColumnFormat2.Flag3#AccentSensitiveSortInV5} */
		AccentInsensitiveSort(NotesConstants.VCF1_M_AccentInsensitiveSort);
		private final short value;
		Flag(int value) { this.value = (short)value; }
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
		HeaderAlignment(NotesConstants.VCF2_M_HeaderAlignment),
		SortPermute(NotesConstants.VCF2_M_SortPermute),
		SecondResortUniqueSort(NotesConstants.VCF2_M_SecondResortUniqueSort),
		SecondResortCategorized(NotesConstants.VCF2_M_SecondResortCategorized),
		SecondResortPermute(NotesConstants.VCF2_M_SecondResortPermute),
		SecondResortPermutePair(NotesConstants.VCF2_M_SecondResortPermutePair),
		ShowValuesAsLinks(NotesConstants.VCF2_M_ShowValuesAsLinks),
		DisplayReadingOrder(NotesConstants.VCF2_M_DisplayReadingOrder),
		HeaderReadingOrder(NotesConstants.VCF2_M_HeaderReadingOrder);
		private final short value;
		Flag2(int value) { this.value = (short)value; }
		@Override
		public Short getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum DataType implements INumberEnum<Short> {
		NUMBER(NotesConstants.VIEW_COL_NUMBER),
		TIMEDATE(NotesConstants.VIEW_COL_TIMEDATE),
		TEXT(NotesConstants.VIEW_COL_TEXT);
		private final short value;
		DataType(int value) { this.value = (short)value; }
		@Override
		public Short getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum StatType implements INumberEnum<Byte> {
		NONE(NotesConstants.NIF_STAT_NONE),
		TOTAL(NotesConstants.NIF_STAT_TOTAL),
		AVG_PER_CHILD(NotesConstants.NIF_STAT_AVG_PER_CHILD),
		PCT_OVERALL(NotesConstants.NIF_STAT_PCT_OVERALL),
		PCT_PARENT(NotesConstants.NIF_STAT_PCT_PARENT),
		AVG_PER_ENTRY(NotesConstants.NIF_STAT_AVG_PER_ENTRY);
		private final byte value;
		StatType(int value) { this.value = (byte)value; }
		@Override
		public Byte getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum Alignment implements INumberEnum<Byte> {
		LEFT(ViewFormatConstants.VIEW_COL_ALIGN_LEFT),
		RIGHT(ViewFormatConstants.VIEW_COL_ALIGN_RIGHT),
		CENTER(ViewFormatConstants.VIEW_COL_ALIGN_CENTER);
		private final byte value;
		Alignment(int value) { this.value = (byte)value; }
		@Override
		public Byte getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum ReadingOrder implements INumberEnum<Byte> {
		LTR(ViewFormatConstants.VIEW_COL_LTR),
		RTL(ViewFormatConstants.VIEW_COL_RTL);
		private final byte value;
		ReadingOrder(int value) { this.value = (byte)value; }
		@Override
		public Byte getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum ListDelimiter implements INumberEnum<Short> {
		NONE(0),
		SPACE(RichTextConstants.LDDELIM_SPACE),
		COMMA(RichTextConstants.LDDELIM_COMMA),
		SEMICOLON(RichTextConstants.LDDELIM_SEMICOLON),
		NEWLINE(RichTextConstants.LDDELIM_NEWLINE)
		;
		private final short value;
		ListDelimiter(int value) { this.value = (short)value; }
		@Override
		public Short getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}

	
	@StructureGetter("Signature")
	short getSignature();
	@StructureSetter("Signature")
	ViewColumnFormat setSignature(short signature);
	
	@StructureGetter("Flags1")
	Set<Flag> getFlags();
	@StructureSetter("Flags1")
	ViewColumnFormat setFlags(Collection<Flag> flags);
	
	@StructureGetter("ItemNameSize")
	int getItemNameLength();
	@StructureSetter("ItemNameSize")
	ViewColumnFormat setItemNameLength(int len);
	
	@StructureGetter("TitleSize")
	int getTitleLength();
	@StructureSetter("TitleSize")
	ViewColumnFormat setTitleLength(int len);
	
	@StructureGetter("FormulaSize")
	int getFormulaLength();
	@StructureSetter("FormulaSize")
	ViewColumnFormat setFormulaLength(int len);
	
	@StructureGetter("ConstantValueSize")
	int getConstantValueLength();
	@StructureSetter("ConstantValueSize")
	ViewColumnFormat setConstantValueLength(int len);
	
	@StructureGetter("DisplayWidth")
	int getDisplayWidth();
	@StructureSetter("DisplayWidth")
	ViewColumnFormat setDisplayWidth(int len);
	
	@StructureGetter("FontID")
	FontStyle getFontStyle();
	
	@StructureGetter("Flags2")
	short getFlags2Raw();
	@StructureSetter("Flags2")
	ViewColumnFormat setFlags2Raw(short flags);
	
	@StructureGetter("NumberFormat")
	NFMT getNumberFormat();
	
	@StructureGetter("TimeFormat")
	TFMT getTimeForat();
	
	@StructureGetter("FormatDataType")
	DataType getDataType();
	@StructureSetter("FormatDataType")
	ViewColumnFormat setDataType(DataType type);
	
	@StructureGetter("ListSep")
	ListDelimiter getListDelimiter();
	@StructureSetter("ListSep")
	ViewColumnFormat setListDelimiter(ListDelimiter delim);
	
	default Set<Flag2> getFlags2() {
		short rawFlags = getFlags2Raw();
		return DominoEnumUtil.valuesOf(Flag2.class, rawFlags & NotesConstants.VCF2_MASK_FLAGS);
	}
	default ViewColumnFormat setFlags2(Collection<Flag2> flags) {
		short val = DominoEnumUtil.toBitField(Flag2.class, flags);
		short rawFlags = getFlags2Raw();
		setFlags2Raw((short)(val | (rawFlags & ~NotesConstants.VCF2_MASK_FLAGS)));
		return this;
	}
	
	default Alignment getAlignment() {
		short rawFlags = getFlags2Raw();
		return DominoEnumUtil.valueOf(Alignment.class, rawFlags & NotesConstants.VCF2_M_DisplayAlignment)
			.orElse(Alignment.LEFT);
	}
	default ViewColumnFormat setAlignment(Alignment alignment) {
		byte val = alignment == null ? 0 : alignment.getValue();
		short rawFlags = getFlags2Raw();
		setFlags2Raw((short)((rawFlags & ~NotesConstants.VCF2_M_DisplayAlignment) | val));
		return this;
	}
	
	default Alignment getHeaderAlignment() {
		short rawFlags = getFlags2Raw();
		return DominoEnumUtil.valueOf(Alignment.class, (rawFlags & NotesConstants.VCF2_M_HeaderAlignment) >> NotesConstants.VCF2_S_HeaderAlignment)
			.orElse(Alignment.LEFT);
	}
	default ViewColumnFormat setHeaderAlignment(Alignment alignment) {
		byte val = alignment == null ? 0 : alignment.getValue();
		short rawFlags = getFlags2Raw();
		setFlags2Raw((short)((rawFlags & ~NotesConstants.VCF2_M_HeaderAlignment) | (val << NotesConstants.VCF2_S_HeaderAlignment)));
		return this;
	}
	
	default StatType getTotalType() {
		short rawFlags = getFlags2Raw();
		return DominoEnumUtil.valueOf(StatType.class, (rawFlags & NotesConstants.VCF2_M_SubtotalCode) >> NotesConstants.VCF2_S_SubtotalCode)
			.orElse(StatType.NONE);
	}
	default ViewColumnFormat setTotalType(StatType type) {
		byte val = type == null ? 0 : type.getValue();
		short rawFlags = getFlags2Raw();
		setFlags2Raw((short)((rawFlags & ~NotesConstants.VCF2_M_SubtotalCode) | (val << NotesConstants.VCF2_S_SubtotalCode)));
		return this;
	}
	
	default ReadingOrder getHeaderReadingOrder() {
		short rawFlags = getFlags2Raw();
		return DominoEnumUtil.valueOf(ReadingOrder.class, (rawFlags & NotesConstants.VCF2_M_HeaderReadingOrder) >> NotesConstants.VCF2_S_HeaderReadingOrder)
			.orElse(ReadingOrder.LTR);
	}
	default ViewColumnFormat setHeaderReadingOrder(ReadingOrder type) {
		byte val = type == null ? 0 : type.getValue();
		short rawFlags = getFlags2Raw();
		setFlags2Raw((short)((rawFlags & ~NotesConstants.VCF2_M_HeaderReadingOrder) | (val << NotesConstants.VCF2_S_HeaderReadingOrder)));
		return this;
	}
	
	default ReadingOrder getReadingOrder() {
		short rawFlags = getFlags2Raw();
		return DominoEnumUtil.valueOf(ReadingOrder.class, (rawFlags & NotesConstants.VCF2_M_DisplayReadingOrder) >> NotesConstants.VCF2_S_DisplayReadingOrder)
			.orElse(ReadingOrder.LTR);
	}
	default ViewColumnFormat setReadingOrder(ReadingOrder type) {
		byte val = type == null ? 0 : type.getValue();
		short rawFlags = getFlags2Raw();
		setFlags2Raw((short)((rawFlags & ~NotesConstants.VCF2_M_DisplayReadingOrder) | (val << NotesConstants.VCF2_S_DisplayReadingOrder)));
		return this;
	}
	
	default String getItemName() {
		return StructureSupport.extractStringValue(this,
			0,
			getItemNameLength()
		);
	}
	default ViewColumnFormat setItemName(String itemName) {
		return StructureSupport.writeStringValue(
			this,
			0,
			getItemNameLength(),
			itemName,
			this::setItemNameLength
		);
	}
	
	default String getTitle() {
		return StructureSupport.extractStringValue(this,
			getItemNameLength(),
			getTitleLength()
		);
	}
	default ViewColumnFormat setTitle(String title) {
		return StructureSupport.writeStringValue(
			this,
			getItemNameLength(),
			getTitleLength(),
			title,
			this::setTitleLength
		);
	}
	
	default String getFormula() {
		return StructureSupport.extractCompiledFormula(this,
			getItemNameLength() + getTitleLength(),
			getFormulaLength()
		);
	}
	default ViewColumnFormat setFormula(String formula) {
		return StructureSupport.writeStringValue(
			this,
			getItemNameLength() + getTitleLength(),
			getFormulaLength(),
			formula,
			this::setFormulaLength
		);
	}
}
