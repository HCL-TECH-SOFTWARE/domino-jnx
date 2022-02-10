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

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "CDACTIONSENDMAIL", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "dwFlags", type = CDActionSendMail.Flag.class, bitfield = true),
    @StructureMember(name = "wFieldLen", type = short[].class, length = RichTextConstants.ACTIONSENDMAIL_FIELDCOUNT)
})
public interface CDActionSendMail extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /** Include matching document */
    INCLUDEDOC(RichTextConstants.ACTIONSENDMAIL_FLAG_INCLUDEDOC),
    /** Include doclink to document */
    INCLUDELINK(RichTextConstants.ACTIONSENDMAIL_FLAG_INCLUDELINK),
    /** save copy */
    SAVEMAIL(RichTextConstants.ACTIONSENDMAIL_FLAG_SAVEMAIL),
    /** To field is a formula */
    TOFORMULA(RichTextConstants.ACTIONSENDMAIL_FLAG_TOFORMULA),
    /** cc field is a formula */
    CCFORMULA(RichTextConstants.ACTIONSENDMAIL_FLAG_CCFORMULA),
    /** bcc field is a formula */
    BCCFORMULA(RichTextConstants.ACTIONSENDMAIL_FLAG_BCCFORMULA),
    /** Subject field is a formula */
    SUBJECTFORMULA(RichTextConstants.ACTIONSENDMAIL_FLAG_SUBJECTFORMULA),
    ;

    private final int value;

    Flag(final int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Integer getValue() {
      return this.value;
    }
  }

  default String getBcc() {
    if (this.getFlags().contains(Flag.BCCFORMULA)) {
      return StructureSupport.extractCompiledFormula(
          this,
          this.getToLength() + this.getCcLength(),
          this.getBccLength());
    } else {
      return StructureSupport.extractStringValueUnpacked(
          this,
          this.getToLength() + this.getCcLength(),
          this.getBccLength());
    }
  }

  default int getBccLength() {
    return Short.toUnsignedInt(this.getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_BCCFIELD]);
  }

  default String getBody() {
    return StructureSupport.extractStringValueUnpacked(
        this,
        this.getToLength() + this.getCcLength() + this.getBccLength() + this.getSubjectLength(),
        this.getBodyLength());
  }

  default int getBodyLength() {
    return Short.toUnsignedInt(this.getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_BODYFIELD]);
  }

  default String getCc() {
    if (this.getFlags().contains(Flag.CCFORMULA)) {
      return StructureSupport.extractCompiledFormula(
          this,
          this.getToLength(),
          this.getCcLength());
    } else {
      return StructureSupport.extractStringValueUnpacked(
          this,
          this.getToLength(),
          this.getCcLength());
    }
  }

  default int getCcLength() {
    return Short.toUnsignedInt(this.getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_CCFIELD]);
  }

  @StructureGetter("wFieldLen")
  short[] getFieldLengthsRaw();

  @StructureGetter("dwFlags")
  Set<Flag> getFlags();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  default String getSubject() {
    if (this.getFlags().contains(Flag.SUBJECTFORMULA)) {
      return StructureSupport.extractCompiledFormula(
          this,
          this.getToLength() + this.getCcLength() + this.getBccLength(),
          this.getSubjectLength());
    } else {
      return StructureSupport.extractStringValueUnpacked(
          this,
          this.getToLength() + this.getCcLength() + this.getBccLength(),
          this.getSubjectLength());
    }
  }

  default int getSubjectLength() {
    return Short.toUnsignedInt(this.getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_SUBJECTFIELD]);
  }

  default String getTo() {
    if (this.getFlags().contains(Flag.TOFORMULA)) {
      return StructureSupport.extractCompiledFormula(
          this,
          0,
          this.getToLength());
    } else {
      return StructureSupport.extractStringValueUnpacked(
          this,
          0,
          this.getToLength());
    }
  }

  default int getToLength() {
    return Short.toUnsignedInt(this.getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_TOFIELD]);
  }

  default CDActionSendMail setBcc(final String value) {
    final Set<Flag> flags = this.getFlags();
    flags.remove(Flag.BCCFORMULA);
    this.setFlags(flags);
    return StructureSupport.writeStringValueUnpacked(
        this,
        this.getToLength() + this.getCcLength(),
        this.getBccLength(),
        value,
        this::setBccLength);
  }

  default CDActionSendMail setBccFormula(final String value) {
    final Set<Flag> flags = this.getFlags();
    flags.remove(Flag.BCCFORMULA);
    this.setFlags(flags);
    return StructureSupport.writeCompiledFormula(
        this,
        this.getToLength() + this.getCcLength(),
        this.getBccLength(),
        value,
        this::setBccLength);
  }

  default CDActionSendMail setBccLength(final int len) {
    final short[] lengths = this.getFieldLengthsRaw();
    lengths[RichTextConstants.ACTIONSENDMAIL_BCCFIELD] = (short) len;
    this.setFieldLengthsRaw(lengths);
    return this;
  }

  default CDActionSendMail setBody(final String value) {
    return StructureSupport.writeStringValueUnpacked(
        this,
        this.getToLength() + this.getCcLength() + this.getBccLength() + this.getSubjectLength(),
        this.getBodyLength(),
        value,
        this::setBodyLength);
  }

  default CDActionSendMail setBodyLength(final int len) {
    final short[] lengths = this.getFieldLengthsRaw();
    lengths[RichTextConstants.ACTIONSENDMAIL_BODYFIELD] = (short) len;
    this.setFieldLengthsRaw(lengths);
    return this;
  }

  default CDActionSendMail setCc(final String value) {
    final Set<Flag> flags = this.getFlags();
    flags.remove(Flag.CCFORMULA);
    this.setFlags(flags);
    return StructureSupport.writeStringValueUnpacked(
        this,
        this.getToLength(),
        this.getCcLength(),
        value,
        this::setCcLength);
  }

  default CDActionSendMail setCcFormula(final String value) {
    final Set<Flag> flags = this.getFlags();
    flags.add(Flag.CCFORMULA);
    this.setFlags(flags);
    return StructureSupport.writeCompiledFormula(
        this,
        this.getToLength(),
        this.getCcLength(),
        value,
        this::setCcLength);
  }

  default CDActionSendMail setCcLength(final int len) {
    final short[] lengths = this.getFieldLengthsRaw();
    lengths[RichTextConstants.ACTIONSENDMAIL_CCFIELD] = (short) len;
    this.setFieldLengthsRaw(lengths);
    return this;
  }

  @StructureSetter("wFieldLen")
  CDActionSendMail setFieldLengthsRaw(short[] lengths);

  @StructureSetter("dwFlags")
  CDActionSendMail setFlags(Collection<Flag> flags);

  default CDActionSendMail setSubject(final String value) {
    final Set<Flag> flags = this.getFlags();
    flags.remove(Flag.SUBJECTFORMULA);
    this.setFlags(flags);
    return StructureSupport.writeStringValueUnpacked(
        this,
        this.getToLength() + this.getCcLength() + this.getBccLength(),
        this.getSubjectLength(),
        value,
        this::setSubjectLength);
  }

  default CDActionSendMail setSubjectFormula(final String value) {
    final Set<Flag> flags = this.getFlags();
    flags.add(Flag.SUBJECTFORMULA);
    this.setFlags(flags);
    return StructureSupport.writeCompiledFormula(
        this,
        this.getToLength() + this.getCcLength() + this.getBccLength(),
        this.getSubjectLength(),
        value,
        this::setSubjectLength);
  }

  default CDActionSendMail setSubjectLength(final int len) {
    final short[] lengths = this.getFieldLengthsRaw();
    lengths[RichTextConstants.ACTIONSENDMAIL_SUBJECTFIELD] = (short) len;
    this.setFieldLengthsRaw(lengths);
    return this;
  }

  default CDActionSendMail setTo(final String value) {
    final Set<Flag> flags = this.getFlags();
    flags.remove(Flag.TOFORMULA);
    this.setFlags(flags);
    return StructureSupport.writeStringValueUnpacked(
        this,
        0,
        this.getToLength(),
        value,
        this::setToLength);
  }

  default CDActionSendMail setToFormula(final String value) {
    final Set<Flag> flags = this.getFlags();
    flags.add(Flag.TOFORMULA);
    this.setFlags(flags);
    return StructureSupport.writeCompiledFormula(
        this,
        0,
        this.getToLength(),
        value,
        this::setToLength);
  }

  default CDActionSendMail setToLength(final int len) {
    final short[] lengths = this.getFieldLengthsRaw();
    lengths[RichTextConstants.ACTIONSENDMAIL_TOFIELD] = (short) len;
    this.setFieldLengthsRaw(lengths);
    return this;
  }
}
