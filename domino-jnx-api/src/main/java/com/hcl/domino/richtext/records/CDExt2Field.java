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
package com.hcl.domino.richtext.records;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Set;

import com.hcl.domino.design.format.DateShowFormat;
import com.hcl.domino.design.format.DateShowSpecial;
import com.hcl.domino.design.format.DateTimeFlag;
import com.hcl.domino.design.format.DateTimeFlag2;
import com.hcl.domino.design.format.DayFormat;
import com.hcl.domino.design.format.MonthFormat;
import com.hcl.domino.design.format.NumberPref;
import com.hcl.domino.design.format.TimeShowFormat;
import com.hcl.domino.design.format.WeekFormat;
import com.hcl.domino.design.format.YearFormat;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.TFMT;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(
	name="CDEXT2FIELD",
	members={
		@StructureMember(name="Header", type=WSIG.class),
		@StructureMember(name="NumSymPref", type=NumberPref.class),
		@StructureMember(name="NumSymFlags", type=byte.class, bitfield=true),
		@StructureMember(name="DecimalSymLength", type=int.class, unsigned=true),
		@StructureMember(name="MilliSepSymLength", type=int.class, unsigned=true),
		@StructureMember(name="NegativeSymLength", type=int.class, unsigned=true),
		@StructureMember(name="MilliGroupSize", type=short.class, unsigned=true),
		@StructureMember(name="VerticalSpacing", type=short.class),
		@StructureMember(name="HorizontalSpacing", type=short.class),
		@StructureMember(name="Unused2", type=short.class),
		@StructureMember(name="FirstFieldLimitType", type=short.class, unsigned=true),
		@StructureMember(name="CurrencyPref", type=NumberPref.class),
		@StructureMember(name="CurrencyType", type=CDExt2Field.CurrencyType.class),
		@StructureMember(name="CurrencyFlags", type=CDExt2Field.CurrencyFlag.class, bitfield=true),
		@StructureMember(name="CurrencySymLength", type=int.class, unsigned=true),
		@StructureMember(name="ISOCountry", type=int.class, unsigned=true),
		@StructureMember(name="ThumbnailImageWidth", type=short.class, unsigned=true),
		@StructureMember(name="ThumbnailImageHeight", type=short.class, unsigned=true),
		@StructureMember(name="wThumbnailImageFileNameLength", type=short.class, unsigned=true),
		@StructureMember(name="wIMOnlineNameFormulaLen", type=short.class, unsigned=true),
		@StructureMember(name="DTPref", type=NumberPref.class),
		@StructureMember(name="DTFlags", type=DateTimeFlag.class, bitfield=true),
		@StructureMember(name="DTFlags2", type=DateTimeFlag2.class, bitfield=true),
		@StructureMember(name="DTDOWFmt", type=WeekFormat.class),
		@StructureMember(name="DTYearFmt", type=YearFormat.class),
		@StructureMember(name="DTMonthFmt", type=MonthFormat.class),
		@StructureMember(name="DTDayFmt", type=DayFormat.class),
		@StructureMember(name="DTDsep1Len", type=byte.class, unsigned=true),
		@StructureMember(name="DTDsep2Len", type=byte.class, unsigned=true),
		@StructureMember(name="DTDsep3Len", type=byte.class, unsigned=true),
		@StructureMember(name="DTTsepLen", type=byte.class, unsigned=true),
		@StructureMember(name="DTDShow", type=DateShowFormat.class),
		@StructureMember(name="DTDSpecial", type=DateShowSpecial.class),
		@StructureMember(name="DTTShow", type=TimeShowFormat.class),
		@StructureMember(name="DTTZone", type=TFMT.ZoneFormat.class),
		@StructureMember(name="Unused5", type=int.class),
		@StructureMember(name="ECFlags", type=CDExt2Field.FormatFlag.class, bitfield=true),
		@StructureMember(name="Unused612", type=byte.class),
		@StructureMember(name="wCharacters", type=short.class, unsigned=true),
		@StructureMember(name="wInputEnabledLen", type=short.class, unsigned=true),
		@StructureMember(name="wIMGroupFormulaLen", type=short.class, unsigned=true)
	}
)
public interface CDExt2Field extends RichTextRecord<WSIG> {
	enum CurrencyType implements INumberEnum<Byte> {
		COMMON(RichTextConstants.NCURFMT_COMMON),
		CUSTOM(RichTextConstants.NCURFMT_CUSTOM),
		;
		private final byte value;
		CurrencyType(byte value) { this.value = value; }
		
		@Override
		public long getLongValue() {
			return value;
		}
		@Override
		public Byte getValue() {
			return value;
		}
	}
	enum CurrencyFlag implements INumberEnum<Byte> {
		SYMFOLLOWS((byte)RichTextConstants.NCURFMT_SYMFOLLOWS),
		USESPACES((byte)RichTextConstants.NCURFMT_USESPACES),
		ISOSYMUSED((byte)RichTextConstants.NCURFMT_ISOSYMUSED),
		;
		private final byte value;
		CurrencyFlag(byte value) { this.value = value; }
		
		@Override
		public long getLongValue() {
			return value;
		}
		@Override
		public Byte getValue() {
			return value;
		}
	}
	enum FormatFlag implements INumberEnum<Byte> {
		PROPORTIONAL((byte)RichTextConstants.EC_FLAG_WIDTH_PROPORTIONAL)
		;
		private final byte value;
		FormatFlag(byte value) { this.value = value; }
		
		@Override
		public long getLongValue() {
			return value;
		}
		@Override
		public Byte getValue() {
			return value;
		}
	}
	
	@StructureGetter("Header")
	@Override
	WSIG getHeader();
	
	@StructureGetter("NumSymPref")
	NumberPref getNumberSymbolPreference();
	@StructureSetter("NumSymPref")
	CDExt2Field setNumberSymbolPreference(NumberPref pref);
	
	@StructureGetter("DecimalSymLength")
	long getDecimalSymbolLength();
	@StructureSetter("DecimalSymLength")
	CDExt2Field setDecimalSymbolLength(long len);
	
	@StructureGetter("MilliSepSymLength")
	long getMilliSeparatorLength();
	@StructureSetter("MilliSepSymLength")
	CDExt2Field setMilliSeparatorLength(long len);
	
	@StructureGetter("NegativeSymLength")
	long getNegativeSymbolLength();
	@StructureSetter("NegativeSymLength")
	CDExt2Field setNegativeSymbolLength(long len);
	
	@StructureGetter("MilliGroupSize")
	int getMilliGroupSize();
	@StructureSetter("MilliGroupSize")
	CDExt2Field setMilliGroupSize(int size);
	
	@StructureGetter("VerticalSpacing")
	short getVerticalSpacing();
	@StructureSetter("VerticalSpacing")
	CDExt2Field setVerticalSpacing(short spacing);
	
	@StructureGetter("HorizontalSpacing")
	short getHorizontalSpacing();
	@StructureSetter("HorizontalSpacing")
	CDExt2Field setHorizontalSpacing(short spacing);
	
	@StructureGetter("CurrencyPref")
	NumberPref getCurrencyPreference();
	@StructureSetter("CurrencyPref")
	CDExt2Field setCurrencyPreference(NumberPref pref);
	
	@StructureGetter("CurrencyType")
	CurrencyType getCurrencyType();
	@StructureSetter("CurrencyType")
	CDExt2Field setCurrencyType(CurrencyType type);
	
	@StructureGetter("CurrencyFlags")
	Set<CurrencyFlag> getCurrencyFlags();
	@StructureSetter("CurrencyFlags")
	CDExt2Field setCurrencyFlags(Collection<CurrencyFlag> flags);
	
	@StructureGetter("CurrencySymLength")
	long getCurrencySymbolLength();
	@StructureSetter("CurrencySymLength")
	CDExt2Field setCurrencySymbolLength(long len);
	
	@StructureGetter("ISOCountry")
	long getISOCountry();
	@StructureSetter("ISOCountry")
	CDExt2Field setISOCountry(long countryCode);
	
	@StructureGetter("ThumbnailImageWidth")
	int getThumbnailImageWidth();
	@StructureSetter("ThumbnailImageWidth")
	CDExt2Field setThumbnailImageWidth(int width);
	
	@StructureGetter("ThumbnailImageHeight")
	int getThumbnailImageHeight();
	@StructureSetter("ThumbnailImageHeight")
	CDExt2Field setThumbnailImageHeight(int height);
	
	@StructureGetter("wThumbnailImageFileNameLength")
	int getThumbnailImageFileNameLength();
	@StructureSetter("wThumbnailImageFileNameLength")
	CDExt2Field setThumbnailImageFileNameLength(int len);
	
	@StructureGetter("wIMOnlineNameFormulaLen")
	int getIMOnlineNameFormulaLength();
	@StructureSetter("wIMOnlineNameFormulaLen")
	CDExt2Field setIMOnlineNameFormulaLength(int len);
	
	@StructureGetter("DTPref")
	NumberPref getDateTimePreference();
	@StructureSetter("DTPref")
	CDExt2Field setDateTimePreference(NumberPref pref);
	
	@StructureGetter("DTFlags")
	Set<DateTimeFlag> getDateTimeFlags();
	@StructureSetter("DTFlags")
	CDExt2Field setDateTimeFlags(Collection<DateTimeFlag> flags);
	
	@StructureGetter("DTFlags2")
	Set<DateTimeFlag2> getDateTimeFlags2();
	@StructureSetter("DTFlags2")
	CDExt2Field setDateTimeFlags2(Collection<DateTimeFlag2> flags);
	
	@StructureGetter("DTDOWFmt")
	WeekFormat getDayOfWeekFormat();
	@StructureSetter("DTDOWFmt")
	CDExt2Field setDayOfWeekFormat(WeekFormat format);
	
	@StructureGetter("DTYearFmt")
	YearFormat getYearFormat();
	@StructureSetter("DTYearFmt")
	CDExt2Field setYearFormat(YearFormat format);
	
	@StructureGetter("DTMonthFmt")
	MonthFormat getMonthFormat();
	@StructureSetter("DTMonthFmt")
	CDExt2Field setMonthFormat(MonthFormat format);
	
	@StructureGetter("DTDayFmt")
	DayFormat getDayFormat();
	@StructureSetter("DTDayFmt")
	CDExt2Field setDayFormat(DayFormat format);
	
	@StructureGetter("DTDsep1Len")
	short getDateSeparator1Length();
	@StructureSetter("DTDsep1Len")
	CDExt2Field setDateSeparator1Length(short len);
	
	@StructureGetter("DTDsep2Len")
	short getDateSeparator2Length();
	@StructureSetter("DTDsep2Len")
	CDExt2Field setDateSeparator2Length(short len);
	
	@StructureGetter("DTDsep3Len")
	short getDateSeparator3Length();
	@StructureSetter("DTDsep3Len")
	CDExt2Field setDateSeparator3Length(short len);
	
	@StructureGetter("DTTsepLen")
	short getTimeSeparatorLength();
	@StructureSetter("DTTsepLen")
	CDExt2Field setTimeSeparatorLength(short len);
	
	@StructureGetter("DTDShow")
	DateShowFormat getDateShowFormat();
	@StructureSetter("DTDShow")
	CDExt2Field setDateShowFormat(DateShowFormat format);
	
	@StructureGetter("DTDSpecial")
	DateShowSpecial getDateShowSpecial();
	@StructureSetter("DTDSpecial")
	CDExt2Field setDateShowSpecial(DateShowSpecial format);
	
	@StructureGetter("DTTShow")
	TimeShowFormat getTimeShowFormat();
	@StructureSetter("DTTShow")
	CDExt2Field setTimeShowFormat(TimeShowFormat format);
	
	@StructureGetter("DTTZone")
	TFMT.ZoneFormat getTimeZoneFormat();
	@StructureSetter("DTTZone")
	CDExt2Field setTimeZoneFormat(TFMT.ZoneFormat format);
	
	@StructureGetter("ECFlags")
	Set<FormatFlag> getFormatFlags();
	@StructureSetter("ECFlags")
	CDExt2Field setFormatFlags(Collection<FormatFlag> flags);
	
	@StructureGetter("wCharacters")
	int getProportionalWidthCharacters();
	@StructureSetter("wCharacters")
	CDExt2Field setProportionalWidthCharacters(int characters);
	
	@StructureGetter("wInputEnabledLen")
	int getInputEnabledFormulaLength();
	@StructureSetter("wInputEnabledLen")
	CDExt2Field setInputEnabledFormulaLength(int len);
	
	@StructureGetter("wIMGroupFormulaLen")
	int getIMGroupFormulaLength();
	@StructureSetter("wIMGroupFormulaLen")
	CDExt2Field setIMGroupFormulaLength(int len);
	
	default String getDecimalSymbol() {
		int len = (int)getDecimalSymbolLength();
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}
		
		ByteBuffer buf = getVariableData();
		byte[] lmbcs = new byte[len];
		buf.get(lmbcs);
		return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
	}
	default CDExt2Field setDecimalSymbol(String symbol) {
		int currentLen = (int)getDecimalSymbolLength();
		ByteBuffer buf = getVariableData();
		int postLen = buf.remaining() - currentLen;

		buf.position(currentLen);
		byte[] postData = new byte[postLen];
		buf.get(postData);
		
		byte[] lmbcs = symbol == null ? new byte[0] : symbol.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
		setDecimalSymbolLength(lmbcs.length);
		int newLen = lmbcs.length + postLen;
		resizeVariableData(newLen);
		buf = getVariableData();
		buf.put(lmbcs);
		buf.put(postData);
		
		return this;
	}

	default String getMilliSeparator() {
		int len = (int)getMilliSeparatorLength();
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}
		
		int preLen = (int)getDecimalSymbolLength();
		
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		byte[] lmbcs = new byte[len];
		buf.get(lmbcs);
		return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
	}
	default CDExt2Field setMilliSeparator(String sep) {
		int preLen = (int)getDecimalSymbolLength();
		int currentLen = (int)getMilliSeparatorLength();
		ByteBuffer buf = getVariableData();
		int postLen = buf.remaining() - currentLen - preLen;

		buf.position(preLen + currentLen);
		byte[] postData = new byte[postLen];
		buf.get(postData);
		
		byte[] lmbcs = sep == null ? new byte[0] : sep.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
		setMilliSeparatorLength(lmbcs.length);
		int newLen = preLen + lmbcs.length + postLen;
		resizeVariableData(newLen);
		buf = getVariableData();
		buf.position(preLen);
		buf.put(lmbcs);
		buf.put(postData);
		
		return this;
	}

	default String getNegativeSymbol() {
		int len = (int)getNegativeSymbolLength();
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}
		
		int preLen = (int)(getDecimalSymbolLength() + getMilliSeparatorLength());
		
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		byte[] lmbcs = new byte[len];
		buf.get(lmbcs);
		return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
	}
	default CDExt2Field setNegativeSymbol(String symbol) {
		int preLen = (int)(getDecimalSymbolLength() + getMilliSeparatorLength());
		int currentLen = (int)getNegativeSymbolLength();
		ByteBuffer buf = getVariableData();
		int postLen = buf.remaining() - currentLen - preLen;

		buf.position(preLen + currentLen);
		byte[] postData = new byte[postLen];
		buf.get(postData);
		
		byte[] lmbcs = symbol == null ? new byte[0] : symbol.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
		setNegativeSymbolLength(lmbcs.length);
		int newLen = preLen + lmbcs.length + postLen;
		resizeVariableData(newLen);
		buf = getVariableData();
		buf.position(preLen);
		buf.put(lmbcs);
		buf.put(postData);
		
		return this;
	}

	default String getCurrencySymbol() {
		int len = (int)getCurrencySymbolLength();
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}
		
		int preLen = (int)(getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength());
		
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		byte[] lmbcs = new byte[len];
		buf.get(lmbcs);
		return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
	}
	default CDExt2Field setCurrencySymbol(String symbol) {
		int preLen = (int)(getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength());
		int currentLen = (int)getCurrencySymbolLength();
		ByteBuffer buf = getVariableData();
		int postLen = buf.remaining() - currentLen - preLen;

		buf.position(preLen + currentLen);
		byte[] postData = new byte[postLen];
		buf.get(postData);
		
		byte[] lmbcs = symbol == null ? new byte[0] : symbol.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
		setCurrencySymbolLength(lmbcs.length);
		int newLen = preLen + lmbcs.length + postLen;
		resizeVariableData(newLen);
		buf = getVariableData();
		buf.position(preLen);
		buf.put(lmbcs);
		buf.put(postData);
		
		return this;
	}

	default String getThumbnailImageFileName() {
		int len = (int)getThumbnailImageFileNameLength();
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}
		
		int preLen = (int)(getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength());
		
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		byte[] lmbcs = new byte[len];
		buf.get(lmbcs);
		return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
	}
	default CDExt2Field setThumbnailImageFileName(String fileName) {
		int preLen = (int)(getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength());
		int currentLen = (int)getThumbnailImageFileNameLength();
		ByteBuffer buf = getVariableData();
		int postLen = buf.remaining() - currentLen - preLen;

		buf.position(preLen + currentLen);
		byte[] postData = new byte[postLen];
		buf.get(postData);
		
		byte[] lmbcs = fileName == null ? new byte[0] : fileName.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
		setThumbnailImageFileNameLength(lmbcs.length);
		int newLen = preLen + lmbcs.length + postLen;
		resizeVariableData(newLen);
		buf = getVariableData();
		buf.position(preLen);
		buf.put(lmbcs);
		buf.put(postData);
		
		return this;
	}

	default String getIMOnlineNameFormula() {
		int len = (int)getIMOnlineNameFormulaLength();
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}
		
		int preLen = (int)(getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength() + getThumbnailImageFileNameLength());
		
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		byte[] compiled = new byte[len];
		buf.get(compiled);
		return FormulaCompiler.get().decompile(compiled);
	}
	default CDExt2Field setIMOnlineNameFormula(String formula) {
		int preLen = (int)(getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength() + getThumbnailImageFileNameLength());
		int currentLen = (int)getIMOnlineNameFormulaLength();
		ByteBuffer buf = getVariableData();
		int postLen = buf.remaining() - currentLen - preLen;

		buf.position(preLen + currentLen);
		byte[] postData = new byte[postLen];
		buf.get(postData);
		
		byte[] compiled = FormulaCompiler.get().compile(formula);
		setIMOnlineNameFormulaLength(compiled.length);
		int newLen = preLen + compiled.length + postLen;
		resizeVariableData(newLen);
		buf = getVariableData();
		buf.position(preLen);
		buf.put(compiled);
		buf.put(postData);
		
		return this;
	}

	default String getDateSeparator1() {
		int len = (int)getDateSeparator1Length();
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}
		
		int preLen = (int)(
			getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength() + getThumbnailImageFileNameLength()
			+ getIMOnlineNameFormulaLength()
		);
		
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		byte[] lmbcs = new byte[len];
		buf.get(lmbcs);
		return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
	}
	default CDExt2Field setDateSeparator1(String sep) {
		int preLen = (int)(
			getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength() + getThumbnailImageFileNameLength()
			+ getIMOnlineNameFormulaLength()
		);
		int currentLen = (int)getDateSeparator1Length();
		ByteBuffer buf = getVariableData();
		int postLen = buf.remaining() - currentLen - preLen;

		buf.position(preLen + currentLen);
		byte[] postData = new byte[postLen];
		buf.get(postData);
		
		byte[] compiled = sep == null ? new byte[0] : sep.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
		setDateSeparator1Length((short)compiled.length);
		int newLen = preLen + compiled.length + postLen;
		resizeVariableData(newLen);
		buf = getVariableData();
		buf.position(preLen);
		buf.put(compiled);
		buf.put(postData);
		
		return this;
	}

	default String getDateSeparator2() {
		int len = (int)getDateSeparator2Length();
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}
		
		int preLen = (int)(
			getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength() + getThumbnailImageFileNameLength()
			+ getIMOnlineNameFormulaLength() + getDateSeparator1Length()
		);
		
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		byte[] lmbcs = new byte[len];
		buf.get(lmbcs);
		return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
	}
	default CDExt2Field setDateSeparator2(String sep) {
		int preLen = (int)(
			getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength() + getThumbnailImageFileNameLength()
			+ getIMOnlineNameFormulaLength() + getDateSeparator1Length()
		);
		int currentLen = (int)getDateSeparator2Length();
		ByteBuffer buf = getVariableData();
		int postLen = buf.remaining() - currentLen - preLen;

		buf.position(preLen + currentLen);
		byte[] postData = new byte[postLen];
		buf.get(postData);
		
		byte[] compiled = sep == null ? new byte[0] : sep.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
		setDateSeparator2Length((short)compiled.length);
		int newLen = preLen + compiled.length + postLen;
		resizeVariableData(newLen);
		buf = getVariableData();
		buf.position(preLen);
		buf.put(compiled);
		buf.put(postData);
		
		return this;
	}

	default String getDateSeparator3() {
		int len = (int)getDateSeparator3Length();
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}
		
		int preLen = (int)(
			getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength() + getThumbnailImageFileNameLength()
			+ getIMOnlineNameFormulaLength() + getDateSeparator1Length() + getDateSeparator2Length()
		);
		
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		byte[] lmbcs = new byte[len];
		buf.get(lmbcs);
		return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
	}
	default CDExt2Field setDateSeparator3(String sep) {
		int preLen = (int)(
			getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength() + getThumbnailImageFileNameLength()
			+ getIMOnlineNameFormulaLength() + getDateSeparator1Length() + getDateSeparator2Length()
		);
		int currentLen = (int)getDateSeparator3Length();
		ByteBuffer buf = getVariableData();
		int postLen = buf.remaining() - currentLen - preLen;

		buf.position(preLen + currentLen);
		byte[] postData = new byte[postLen];
		buf.get(postData);
		
		byte[] compiled = sep == null ? new byte[0] : sep.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
		setDateSeparator3Length((short)compiled.length);
		int newLen = preLen + compiled.length + postLen;
		resizeVariableData(newLen);
		buf = getVariableData();
		buf.position(preLen);
		buf.put(compiled);
		buf.put(postData);
		
		return this;
	}

	default String getTimeSeparator() {
		int len = (int)getTimeSeparatorLength();
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}
		
		int preLen = (int)(
			getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength() + getThumbnailImageFileNameLength()
			+ getIMOnlineNameFormulaLength() + getDateSeparator1Length() + getDateSeparator2Length() + getDateSeparator3Length()
		);
		
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		byte[] lmbcs = new byte[len];
		buf.get(lmbcs);
		return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
	}
	default CDExt2Field setTimeSeparator(String sep) {
		int preLen = (int)(
			getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength() + getThumbnailImageFileNameLength()
			+ getIMOnlineNameFormulaLength() + getDateSeparator1Length() + getDateSeparator2Length() + getDateSeparator3Length()
		);
		int currentLen = (int)getTimeSeparatorLength();
		ByteBuffer buf = getVariableData();
		int postLen = buf.remaining() - currentLen - preLen;

		buf.position(preLen + currentLen);
		byte[] postData = new byte[postLen];
		buf.get(postData);
		
		byte[] compiled = sep == null ? new byte[0] : sep.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
		setTimeSeparatorLength((short)compiled.length);
		int newLen = preLen + compiled.length + postLen;
		resizeVariableData(newLen);
		buf = getVariableData();
		buf.position(preLen);
		buf.put(compiled);
		buf.put(postData);
		
		return this;
	}

	default String getInputEnabledFormula() {
		int len = (int)getInputEnabledFormulaLength();
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}

		int preLen = (int)(
			getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength()
			+ getThumbnailImageFileNameLength() + getDateSeparator1Length() + getDateSeparator2Length()
			+ getDateSeparator3Length() + getTimeSeparatorLength() + getIMOnlineNameFormulaLength()
		);
		
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		byte[] compiled = new byte[len];
		buf.get(compiled);
		return FormulaCompiler.get().decompile(compiled);
	}
	default CDExt2Field setInputEnabledFormula(String formula) {
		int preLen = (int)(
			getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength()
			+ getThumbnailImageFileNameLength() + getDateSeparator1Length() + getDateSeparator2Length()
			+ getDateSeparator3Length() + getTimeSeparatorLength() + getIMOnlineNameFormulaLength()
		);
		int currentLen = (int)getInputEnabledFormulaLength();
		ByteBuffer buf = getVariableData();
		int postLen = buf.remaining() - currentLen - preLen;

		buf.position(preLen + currentLen);
		byte[] postData = new byte[postLen];
		buf.get(postData);
		
		byte[] compiled = FormulaCompiler.get().compile(formula);
		setInputEnabledFormulaLength(compiled.length);
		int newLen = preLen + compiled.length + postLen;
		resizeVariableData(newLen);
		buf = getVariableData();
		buf.position(preLen);
		buf.put(compiled);
		buf.put(postData);
		
		return this;
	}

	default String getIMGroupFormula() {
		int len = (int)getIMGroupFormulaLength();
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}

		int preLen = (int)(
			getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength()
			+ getThumbnailImageFileNameLength() + getDateSeparator1Length() + getDateSeparator2Length()
			+ getDateSeparator3Length() + getTimeSeparatorLength() + getIMOnlineNameFormulaLength()
			+ getInputEnabledFormulaLength()
		);
		
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		byte[] compiled = new byte[len];
		buf.get(compiled);
		return FormulaCompiler.get().decompile(compiled);
	}
	default CDExt2Field setIMGroupFormula(String formula) {
		int preLen = (int)(
				getDecimalSymbolLength() + getMilliSeparatorLength() + getNegativeSymbolLength() + getCurrencySymbolLength()
				+ getThumbnailImageFileNameLength() + getDateSeparator1Length() + getDateSeparator2Length()
				+ getDateSeparator3Length() + getTimeSeparatorLength() + getIMOnlineNameFormulaLength()
				+ getInputEnabledFormulaLength()
			);
		int currentLen = (int)getIMGroupFormulaLength();
		ByteBuffer buf = getVariableData();
		int postLen = buf.remaining() - currentLen - preLen;

		buf.position(preLen + currentLen);
		byte[] postData = new byte[postLen];
		buf.get(postData);
		
		byte[] compiled = FormulaCompiler.get().compile(formula);
		setIMGroupFormulaLength(compiled.length);
		int newLen = preLen + compiled.length + postLen;
		resizeVariableData(newLen);
		buf = getVariableData();
		buf.position(preLen);
		buf.put(compiled);
		buf.put(postData);
		
		return this;
	}
}
