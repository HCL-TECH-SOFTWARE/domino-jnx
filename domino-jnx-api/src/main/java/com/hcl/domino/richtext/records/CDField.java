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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.design.format.FieldListDelimiter;
import com.hcl.domino.design.format.FieldListDisplayDelimiter;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.NFMT;
import com.hcl.domino.richtext.structures.TFMT;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(
	name="CDFIELD",
	members={
		@StructureMember(name="Header", type=WSIG.class),
		@StructureMember(name="Flags", type=CDField.Flag.class, bitfield=true),
		@StructureMember(name="DataType", type=ItemDataType.class),
		@StructureMember(name="ListDelim", type=short.class, bitfield=true),
		@StructureMember(name="NumberFormat", type=NFMT.class),
		@StructureMember(name="TimeFormat", type=TFMT.class),
		@StructureMember(name="FontID", type=FontStyle.class),
		@StructureMember(name="DVLength", type=short.class, unsigned=true),
		@StructureMember(name="ITLength", type=short.class, unsigned=true),
		@StructureMember(name="TabOrder", type=short.class, unsigned=true),
		@StructureMember(name="IVLength", type=short.class, unsigned=true),
		@StructureMember(name="NameLength", type=short.class, unsigned=true),
		@StructureMember(name="DescLength", type=short.class, unsigned=true),
		@StructureMember(name="TextValueLength", type=short.class, unsigned=true),
	}
)
public interface CDField extends RichTextRecord<WSIG> {
	enum Flag implements INumberEnum<Short> {
		/**  Field contains read/writers  */
		READWRITERS(RichTextConstants.FREADWRITERS),
		/**  Field is editable, not read only  */
		EDITABLE(RichTextConstants.FEDITABLE),
		/**  Field contains distinguished names  */
		NAMES(RichTextConstants.FNAMES),
		/**  Store DV, even if not spec'ed by user  */
		STOREDV(RichTextConstants.FSTOREDV),
		/**  Field contains document readers  */
		READERS(RichTextConstants.FREADERS),
		/**  Field contains a section  */
		SECTION(RichTextConstants.FSECTION),
		/**  can be assumed to be clear in memory, V3 & later  */
		SPARE3(RichTextConstants.FSPARE3),
		/**  IF CLEAR, CLEAR AS ABOVE  */
		V3FAB(RichTextConstants.FV3FAB),
		/**  Field is a computed field  */
		COMPUTED(RichTextConstants.FCOMPUTED),
		/**  Field is a keywords field  */
		KEYWORDS(RichTextConstants.FKEYWORDS),
		/**  Field is protected  */
		PROTECTED(RichTextConstants.FPROTECTED),
		/**  Field name is simply a reference to a shared field note  */
		REFERENCE(RichTextConstants.FREFERENCE),
		/**  sign field  */
		SIGN(RichTextConstants.FSIGN),
		/**  seal field  */
		SEAL(RichTextConstants.FSEAL),
		/**  standard UI  */
		KEYWORDS_UI_STANDARD(RichTextConstants.FKEYWORDS_UI_STANDARD),
		/**  checkbox UI  */
		KEYWORDS_UI_CHECKBOX(RichTextConstants.FKEYWORDS_UI_CHECKBOX),
		/**  radiobutton UI  */
		KEYWORDS_UI_RADIOBUTTON(RichTextConstants.FKEYWORDS_UI_RADIOBUTTON),
		/**  allow doc editor to add new values  */
		KEYWORDS_UI_ALLOW_NEW(RichTextConstants.FKEYWORDS_UI_ALLOW_NEW)
		;
		private final short value;
		Flag(short value) { this.value = value; }
		
		@Override
		public long getLongValue() {
			return value;
		}
		@Override
		public Short getValue() {
			return value;
		}
	}
	@StructureGetter("Header")
	@Override
	WSIG getHeader();
	
	@StructureGetter("Flags")
	Set<Flag> getFlags();
	@StructureSetter("Flags")
	CDField setFlags(Collection<Flag> flags);
	
	@StructureGetter("DataType")
	ItemDataType getFieldType();
	@StructureSetter("DataType")
	CDField setFieldType(ItemDataType fieldType);
	
	@StructureGetter("ListDelim")
	short getListDelimiterRaw();
	@StructureSetter("ListDelim")
	CDField setListDelimiterRaw(short listDelim);
	
	@StructureGetter("ListDelim")
	Set<FieldListDelimiter> getListDelimiters();
	default CDField setListDelimiters(Collection<FieldListDelimiter> delimiters) {
		short ldVal = delimiters == null ? 0 : (short)delimiters.stream()
			.mapToLong(FieldListDelimiter::getLongValue)
			.reduce((left, right) -> left | right)
			.getAsLong();
		short rawVal = getListDelimiterRaw();
		short newVal = (short)((rawVal & RichTextConstants.LDD_MASK) | ldVal);
		setListDelimiterRaw(newVal);
		return this;
	}
	
	default FieldListDisplayDelimiter getListDisplayDelimiter() {
		// The default mechanism doesn't pick up this single value, since the storage is masked with
		//   non-display delimiters
		short lddVal = (short)(getListDelimiterRaw() & RichTextConstants.LDD_MASK);
		for(FieldListDisplayDelimiter delim : FieldListDisplayDelimiter.values()) {
			if(delim.getValue() == lddVal) {
				return delim;
			}
		}
		return null;
	}
	default CDField setListDisplayDelimiter(FieldListDisplayDelimiter delimiter) {
		short lddVal = delimiter == null ? 0 : delimiter.getValue();
		short rawVal = getListDelimiterRaw();
		short newVal = (short)((rawVal & RichTextConstants.LD_MASK) | lddVal);
		setListDelimiterRaw(newVal);
		return this;
	}
	
	@StructureGetter("NumberFormat")
	NFMT getNumberFormat();
	
	@StructureGetter("TimeFormat")
	TFMT getTimeFormat();
	
	@StructureGetter("FontID")
	FontStyle getFontStyle();
	
	@StructureGetter("DVLength")
	int getDefaultValueLength();
	@StructureSetter("DVLength")
	CDField setDefaultValueLength(int len);
	
	@StructureGetter("ITLength")
	int getInputTranslationLength();
	@StructureSetter("ITLength")
	CDField setInputTranslationLength(int len);
	
	@StructureGetter("TabOrder")
	int getTabOrder();
	@StructureSetter("TabOrder")
	CDField setTabOrder(int tabOrder);
	
	@StructureGetter("IVLength")
	int getInputValidationLength();
	@StructureSetter("IVLength")
	CDField setInputValidationLength(int len);
	
	@StructureGetter("NameLength")
	int getNameLength();
	@StructureSetter("NameLength")
	CDField setNameLength(int len);
	
	@StructureGetter("DescLength")
	int getDescriptionLength();
	@StructureSetter("DescLength")
	CDField setDescriptionLength(int len);
	
	@StructureGetter("TextValueLength")
	int getTextValueLength();
	@StructureSetter("TextValueLength")
	CDField setTextValueLength(int len);
	
	default String getDefaultValueFormula() {
		return StructureSupport.extractCompiledFormula(
			this,
			0,
			getDefaultValueLength()
		);
	}
	default CDField setDefaultValueFormula(String formula) {
		StructureSupport.writeCompiledFormula(
			this,
			0,
			getDefaultValueLength(),
			formula,
			this::setDefaultValueLength
		);
		
		return this;
	}
	
	default String getInputTranslationFormula() {
		return StructureSupport.extractCompiledFormula(
			this,
			getDefaultValueLength(),
			getInputTranslationLength()
		);
	}
	default CDField setInputTranslationFormula(String formula) {
		StructureSupport.writeCompiledFormula(
			this,
			getDefaultValueLength(),
			getInputTranslationLength(),
			formula,
			this::setInputTranslationLength
		);
		
		return this;
	}
	
	default String getInputValidationFormula() {
		return StructureSupport.extractCompiledFormula(
			this,
			getDefaultValueLength() + getInputTranslationLength(),
			getInputValidationLength()
		);
	}
	default CDField setInputValidationFormula(String formula) {
		StructureSupport.writeCompiledFormula(
			this,
			getDefaultValueLength() + getInputTranslationLength(),
			getInputValidationLength(),
			formula,
			this::setInputValidationLength
		);
		
		return this;
	}
	
	default String getName() {
		return StructureSupport.extractStringValue(
			this,
			getDefaultValueLength() + getInputTranslationLength() + getInputValidationLength(),
			getNameLength()
		);
	}
	default CDField setName(String name) {
		StructureSupport.writeStringValue(
			this,
			getDefaultValueLength() + getInputTranslationLength() + getInputValidationLength(),
			getNameLength(),
			name,
			this::setNameLength
		);
		
		return this;
	}
	
	default String getDescription() {
		return StructureSupport.extractStringValue(
			this,
			getDefaultValueLength() + getInputTranslationLength() + getInputValidationLength() + getNameLength(),
			getDescriptionLength()
		);
	}
	default CDField setDescription(String description) {
		StructureSupport.writeStringValue(
			this,
			getDefaultValueLength() + getInputTranslationLength() + getInputValidationLength() + getNameLength(),
			getDescriptionLength(),
			description,
			this::setDescriptionLength
		);
		
		return this;
	}
	
	/**
	 * @return an {@link Optional} describing the explicit text value options for this field,
	 *      or an empty one if the values are defined by a formula
	 */
	default Optional<List<String>> getTextValues() {
		int len = getTextValueLength();
		if(len == 0) {
			return Optional.of(Collections.emptyList());
		}

		int preLen = getDefaultValueLength() + getInputTranslationLength() + getInputValidationLength() + getNameLength() + getDescriptionLength();
		
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		if(buf.getShort(buf.position()) == 0) {
			// This might denote a formula
			if(buf.remaining() > 2) {
				return Optional.empty();
			} else {
				// Then it's actually a zero-element list
				return Optional.of(Collections.emptyList());
			}
		}
		
		byte[] nativeBytes = new byte[buf.remaining()];
		buf.get(nativeBytes);
		return Optional.of(NativeItemCoder.get().decodeStringList(nativeBytes));
	}
	default CDField setTextValues(Collection<String> values) {
		int preLen = getDefaultValueLength() + getInputTranslationLength() + getInputValidationLength() + getNameLength() + getDescriptionLength();
		
		List<String> newVals = new ArrayList<>();
		if(values != null) {
			newVals.addAll(values);
		}
		byte[] nativeValues = NativeItemCoder.get().encodeStringList(newVals);
		setTextValueLength(nativeValues.length);
		int newLen = preLen + nativeValues.length;
		resizeVariableData(newLen);
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		buf.put(nativeValues);
		
		return this;
	}
	/**
	 * @return an {@link Optional} describing the value formula for field choices,
	 *      or an empty one if the values are defined by a an explicit text list
	 */
	default Optional<String> getTextValueFormula() {
		int len = getTextValueLength();
		if(len == 0) {
			return Optional.of(""); //$NON-NLS-1$
		}
		
		int preLen = getDefaultValueLength() + getInputTranslationLength() + getInputValidationLength() + getNameLength() + getDescriptionLength();
		
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		if(buf.getShort() != 0) {
			// This denotes an explicit list
			return Optional.empty();
		}
		if(!buf.hasRemaining()) {
			// Then it's actually a zero-element list
			return Optional.empty();
		}
		
		byte[] compiled = new byte[len-2];
		buf.get(compiled);
		return Optional.of(FormulaCompiler.get().decompile(compiled));
	}
	default CDField setTextValueFormula(String formula) {
		int preLen = getDefaultValueLength() + getInputTranslationLength() + getInputValidationLength() + getNameLength() + getDescriptionLength();
		
		byte[] compiled = FormulaCompiler.get().compile(formula);
		setTextValueLength(compiled.length+2);
		int newLen = preLen + 2 + compiled.length;
		resizeVariableData(newLen);
		ByteBuffer buf = getVariableData();
		buf.position(preLen);
		buf.putShort((short)0);
		buf.put(compiled);
		
		return this;
	}
}
