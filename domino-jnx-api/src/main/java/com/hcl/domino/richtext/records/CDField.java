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
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "CDFIELD", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDField.Flag.class, bitfield = true),
    @StructureMember(name = "DataType", type = ItemDataType.class),
    @StructureMember(name = "ListDelim", type = FieldListDelimiter.class, bitfield = true),
    @StructureMember(name = "NumberFormat", type = NFMT.class),
    @StructureMember(name = "TimeFormat", type = TFMT.class),
    @StructureMember(name = "FontID", type = FontStyle.class),
    @StructureMember(name = "DVLength", type = short.class, unsigned = true),
    @StructureMember(name = "ITLength", type = short.class, unsigned = true),
    @StructureMember(name = "TabOrder", type = short.class, unsigned = true),
    @StructureMember(name = "IVLength", type = short.class, unsigned = true),
    @StructureMember(name = "NameLength", type = short.class, unsigned = true),
    @StructureMember(name = "DescLength", type = short.class, unsigned = true),
    @StructureMember(name = "TextValueLength", type = short.class, unsigned = true),
})
public interface CDField extends RichTextRecord<WSIG>, ICDField {
  @Override
  default String getDefaultValueFormula() {
    return StructureSupport.extractCompiledFormula(
        this,
        0,
        this.getDefaultValueLength());
  }

  @StructureGetter("DVLength")
  int getDefaultValueLength();

  @Override
  default String getDescription() {
    return StructureSupport.extractStringValue(
        this,
        this.getDefaultValueLength() + this.getInputTranslationLength() + this.getInputValidationLength() + this.getNameLength(),
        this.getDescriptionLength());
  }

  @StructureGetter("DescLength")
  int getDescriptionLength();

  @StructureGetter("DataType")
  @Override
  Optional<ItemDataType> getFieldType();

  /**
   * Retrieves the field type as a raw {@code short}.
   * 
   * @return the field type as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("DataType")
  short getFieldTypeRaw();

  @StructureGetter("Flags")
  @Override
  Set<Flag> getFlags();

  @StructureGetter("FontID")
  FontStyle getFontStyle();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  @Override
  default String getInputTranslationFormula() {
    return StructureSupport.extractCompiledFormula(
        this,
        this.getDefaultValueLength(),
        this.getInputTranslationLength());
  }

  @StructureGetter("ITLength")
  int getInputTranslationLength();

  @Override
  default String getInputValidationFormula() {
    return StructureSupport.extractCompiledFormula(
        this,
        this.getDefaultValueLength() + this.getInputTranslationLength(),
        this.getInputValidationLength());
  }

  @StructureGetter("IVLength")
  int getInputValidationLength();

  @StructureGetter("ListDelim")
  short getListDelimiterRaw();

  @StructureGetter("ListDelim")
  @Override
  Set<FieldListDelimiter> getListDelimiters();

  @Override
  default FieldListDisplayDelimiter getListDisplayDelimiter() {
    // The default mechanism doesn't pick up this single value, since the storage is
    // masked with
    // non-display delimiters
    final short lddVal = (short) (this.getListDelimiterRaw() & RichTextConstants.LDD_MASK);
    for (final FieldListDisplayDelimiter delim : FieldListDisplayDelimiter.values()) {
      if (delim.getValue() == lddVal) {
        return delim;
      }
    }
    return null;
  }

  @Override
  default String getName() {
    return StructureSupport.extractStringValue(
        this,
        this.getDefaultValueLength() + this.getInputTranslationLength() + this.getInputValidationLength(),
        this.getNameLength());
  }

  @StructureGetter("NameLength")
  int getNameLength();

  @StructureGetter("NumberFormat")
  NFMT getNumberFormat();

  @StructureGetter("TabOrder")
  int getTabOrder();

  @Override
  default Optional<String> getTextValueFormula() {
    final int len = this.getTextValueLength();
    if (len == 0) {
      return Optional.of(""); //$NON-NLS-1$
    }

    final int preLen = this.getDefaultValueLength() + this.getInputTranslationLength() + this.getInputValidationLength()
        + this.getNameLength() + this.getDescriptionLength();

    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    if (buf.getShort() != 0 || !buf.hasRemaining()) {
      // Then it's actually a zero-element list
      return Optional.empty();
    }

    final byte[] compiled = new byte[len - 2];
    buf.get(compiled);
    return Optional.of(FormulaCompiler.get().decompile(compiled));
  }

  @StructureGetter("TextValueLength")
  int getTextValueLength();

  @Override
  default Optional<List<String>> getTextValues() {
    final int len = this.getTextValueLength();
    if (len == 0) {
      return Optional.of(Collections.emptyList());
    }

    final int preLen = this.getDefaultValueLength() + this.getInputTranslationLength() + this.getInputValidationLength()
        + this.getNameLength() + this.getDescriptionLength();

    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    if (buf.getShort(buf.position()) == 0) {
      // This might denote a formula
      if (buf.remaining() > 2) {
        return Optional.empty();
      } else {
        // Then it's actually a zero-element list
        return Optional.of(Collections.emptyList());
      }
    }

    final byte[] nativeBytes = new byte[buf.remaining()];
    buf.get(nativeBytes);
    return Optional.of(NativeItemCoder.get().decodeStringList(nativeBytes));
  }

  @StructureGetter("TimeFormat")
  TFMT getTimeFormat();

  default CDField setDefaultValueFormula(final String formula) {
    StructureSupport.writeCompiledFormula(
        this,
        0,
        this.getDefaultValueLength(),
        formula,
        this::setDefaultValueLength);

    return this;
  }

  @StructureSetter("DVLength")
  CDField setDefaultValueLength(int len);

  default CDField setDescription(final String description) {
    StructureSupport.writeStringValue(
        this,
        this.getDefaultValueLength() + this.getInputTranslationLength() + this.getInputValidationLength() + this.getNameLength(),
        this.getDescriptionLength(),
        description,
        this::setDescriptionLength);

    return this;
  }

  @StructureSetter("DescLength")
  CDField setDescriptionLength(int len);

  @StructureSetter("DataType")
  CDField setFieldType(ItemDataType fieldType);

  /**
   * Sets the field type as a raw {@code short}.
   * 
   * @param fieldType the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("DataType")
  CDField setFieldTypeRaw(short fieldType);

  @StructureSetter("Flags")
  CDField setFlags(Collection<Flag> flags);

  default CDField setInputTranslationFormula(final String formula) {
    StructureSupport.writeCompiledFormula(
        this,
        this.getDefaultValueLength(),
        this.getInputTranslationLength(),
        formula,
        this::setInputTranslationLength);

    return this;
  }

  @StructureSetter("ITLength")
  CDField setInputTranslationLength(int len);

  default CDField setInputValidationFormula(final String formula) {
    StructureSupport.writeCompiledFormula(
        this,
        this.getDefaultValueLength() + this.getInputTranslationLength(),
        this.getInputValidationLength(),
        formula,
        this::setInputValidationLength);

    return this;
  }

  @StructureSetter("IVLength")
  CDField setInputValidationLength(int len);

  @StructureSetter("ListDelim")
  CDField setListDelimiterRaw(short listDelim);

  default CDField setListDelimiters(final Collection<FieldListDelimiter> delimiters) {
    final short ldVal = delimiters == null ? 0
        : (short) delimiters.stream()
            .mapToLong(FieldListDelimiter::getLongValue)
            .reduce((left, right) -> left | right)
            .getAsLong();
    final short rawVal = this.getListDelimiterRaw();
    final short newVal = (short) (rawVal & RichTextConstants.LDD_MASK | ldVal);
    this.setListDelimiterRaw(newVal);
    return this;
  }

  default CDField setListDisplayDelimiter(final FieldListDisplayDelimiter delimiter) {
    final short lddVal = delimiter == null ? 0 : delimiter.getValue();
    final short rawVal = this.getListDelimiterRaw();
    final short newVal = (short) (rawVal & RichTextConstants.LD_MASK | lddVal);
    this.setListDelimiterRaw(newVal);
    return this;
  }

  default CDField setName(final String name) {
    StructureSupport.writeStringValue(
        this,
        this.getDefaultValueLength() + this.getInputTranslationLength() + this.getInputValidationLength(),
        this.getNameLength(),
        name,
        this::setNameLength);

    return this;
  }

  @StructureSetter("NameLength")
  CDField setNameLength(int len);

  @StructureSetter("TabOrder")
  CDField setTabOrder(int tabOrder);

  default CDField setTextValueFormula(final String formula) {
    final int preLen = this.getDefaultValueLength() + this.getInputTranslationLength() + this.getInputValidationLength()
        + this.getNameLength() + this.getDescriptionLength();

    final byte[] compiled = FormulaCompiler.get().compile(formula);
    this.setTextValueLength(compiled.length + 2);
    final int newLen = preLen + 2 + compiled.length;
    this.resizeVariableData(newLen);
    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    buf.putShort((short) 0);
    buf.put(compiled);

    return this;
  }

  @StructureSetter("TextValueLength")
  CDField setTextValueLength(int len);

  default CDField setTextValues(final Collection<String> values) {
    final int preLen = this.getDefaultValueLength() + this.getInputTranslationLength() + this.getInputValidationLength()
        + this.getNameLength() + this.getDescriptionLength();

    final List<String> newVals = new ArrayList<>();
    if (values != null) {
      newVals.addAll(values);
    }
    final byte[] nativeValues = NativeItemCoder.get().encodeStringList(newVals);
    this.setTextValueLength(nativeValues.length);
    final int newLen = preLen + nativeValues.length;
    this.resizeVariableData(newLen);
    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    buf.put(nativeValues);

    return this;
  }
}
