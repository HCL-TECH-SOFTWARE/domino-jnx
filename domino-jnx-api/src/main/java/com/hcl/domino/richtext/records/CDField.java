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
public interface CDField extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Short> {
    /** Field contains read/writers */
    READWRITERS(RichTextConstants.FREADWRITERS),
    /** Field is editable, not read only */
    EDITABLE(RichTextConstants.FEDITABLE),
    /** Field contains distinguished names */
    NAMES(RichTextConstants.FNAMES),
    /** Store DV, even if not spec'ed by user */
    STOREDV(RichTextConstants.FSTOREDV),
    /** Field contains document readers */
    READERS(RichTextConstants.FREADERS),
    /** Field contains a section */
    SECTION(RichTextConstants.FSECTION),
    /** can be assumed to be clear in memory, V3 &amp; later */
    SPARE3(RichTextConstants.FSPARE3),
    /** IF CLEAR, CLEAR AS ABOVE */
    V3FAB(RichTextConstants.FV3FAB),
    /** Field is a computed field */
    COMPUTED(RichTextConstants.FCOMPUTED),
    /** Field is a keywords field */
    KEYWORDS(RichTextConstants.FKEYWORDS),
    /** Field is protected */
    PROTECTED(RichTextConstants.FPROTECTED),
    /** Field name is simply a reference to a shared field note */
    REFERENCE(RichTextConstants.FREFERENCE),
    /** sign field */
    SIGN(RichTextConstants.FSIGN),
    /** seal field */
    SEAL(RichTextConstants.FSEAL),
    /** standard UI */
    KEYWORDS_UI_STANDARD(RichTextConstants.FKEYWORDS_UI_STANDARD),
    /** checkbox UI */
    KEYWORDS_UI_CHECKBOX(RichTextConstants.FKEYWORDS_UI_CHECKBOX),
    /** radiobutton UI */
    KEYWORDS_UI_RADIOBUTTON(RichTextConstants.FKEYWORDS_UI_RADIOBUTTON),
    /** allow doc editor to add new values */
    KEYWORDS_UI_ALLOW_NEW(RichTextConstants.FKEYWORDS_UI_ALLOW_NEW);

    private final short value;

    Flag(final short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  default String getDefaultValueFormula() {
    return StructureSupport.extractCompiledFormula(
        this,
        0,
        this.getDefaultValueLength());
  }

  @StructureGetter("DVLength")
  int getDefaultValueLength();

  default String getDescription() {
    return StructureSupport.extractStringValue(
        this,
        this.getDefaultValueLength() + this.getInputTranslationLength() + this.getInputValidationLength() + this.getNameLength(),
        this.getDescriptionLength());
  }

  @StructureGetter("DescLength")
  int getDescriptionLength();

  @StructureGetter("DataType")
  ItemDataType getFieldType();

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureGetter("FontID")
  FontStyle getFontStyle();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  default String getInputTranslationFormula() {
    return StructureSupport.extractCompiledFormula(
        this,
        this.getDefaultValueLength(),
        this.getInputTranslationLength());
  }

  @StructureGetter("ITLength")
  int getInputTranslationLength();

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
  Set<FieldListDelimiter> getListDelimiters();

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

  /**
   * @return an {@link Optional} describing the value formula for field choices,
   *         or an empty one if the values are defined by a an explicit text list
   */
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

  /**
   * @return an {@link Optional} describing the explicit text value options for
   *         this field,
   *         or an empty one if the values are defined by a formula
   */
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
