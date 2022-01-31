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
@StructureDefinition(name = "CDACTIONNEWSLETTER", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "dwFlags", type = CDActionNewsletter.Flag.class, bitfield = true),
    @StructureMember(name = "dwGather", type = int.class, unsigned = true),
    @StructureMember(name = "wViewNameLen", type = short.class, unsigned = true),
    @StructureMember(name = "wSpare", type = short.class),
    @StructureMember(name = "wFieldLen", type = short[].class, length = RichTextConstants.ACTIONSENDMAIL_FIELDCOUNT)
})
public interface CDActionNewsletter extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /** Summary of docs (with DocLinks) */
    SUMMARY(RichTextConstants.ACTIONNEWSLETTER_FLAG_SUMMARY),
    /** Gather at least n before mailing */
    GATHER(RichTextConstants.ACTIONNEWSLETTER_FLAG_GATHER),
    /** Include all notes when mailing out multiple notes */
    INCLUDEALL(RichTextConstants.ACTIONNEWSLETTER_FLAG_INCLUDEALL);

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
    return StructureSupport.extractStringValueWordPadded(
        this,
        this.getViewNameLength() + this.getToLength() + this.getCcLength(),
        this.getBccLength());
  }

  default int getBccLength() {
    return Short.toUnsignedInt(this.getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_BCCFIELD]);
  }

  default String getBody() {
    return StructureSupport.extractStringValueUnpacked(
        this,
        this.getViewNameLength() + this.getToLength() + this.getCcLength() + this.getBccLength() + this.getSubjectLength(),
        this.getBodyLength());
  }

  default int getBodyLength() {
    return Short.toUnsignedInt(this.getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_BODYFIELD]);
  }

  default String getCc() {
    return StructureSupport.extractStringValueWordPadded(
        this,
        this.getViewNameLength() + this.getToLength(),
        this.getCcLength());
  }

  default int getCcLength() {
    return Short.toUnsignedInt(this.getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_CCFIELD]);
  }

  @StructureGetter("wFieldLen")
  short[] getFieldLengthsRaw();

  @StructureGetter("dwFlags")
  Set<Flag> getFlags();

  @StructureGetter("dwGather")
  long getGatherCount();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  default String getSubject() {
    return StructureSupport.extractStringValueWordPadded(
        this,
        this.getViewNameLength() + this.getToLength() + this.getCcLength() + this.getBccLength(),
        this.getSubjectLength());
  }

  default int getSubjectLength() {
    return Short.toUnsignedInt(this.getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_SUBJECTFIELD]);
  }

  default String getTo() {
    return StructureSupport.extractStringValueWordPadded(
        this,
        this.getViewNameLength(),
        this.getToLength());
  }

  default int getToLength() {
    return Short.toUnsignedInt(this.getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_TOFIELD]);
  }

  default String getViewName() {
    return StructureSupport.extractStringValue(this, 0, this.getViewNameLength());
  }

  @StructureGetter("wViewNameLen")
  int getViewNameLength();

  default CDActionNewsletter setBcc(final String value) {
    return StructureSupport.writeStringValueWordPadded(
        this,
        this.getViewNameLength() + this.getToLength() + this.getCcLength(),
        this.getBccLength(),
        value,
        this::setBccLength);
  }

  default CDActionNewsletter setBccLength(final int len) {
    final short[] lengths = this.getFieldLengthsRaw();
    lengths[RichTextConstants.ACTIONSENDMAIL_BCCFIELD] = (short) len;
    this.setFieldLengthsRaw(lengths);
    return this;
  }

  default CDActionNewsletter setBody(final String value) {
    return StructureSupport.writeStringValueUnpacked(
        this,
        this.getViewNameLength() + this.getToLength() + this.getCcLength() + this.getBccLength() + this.getSubjectLength(),
        this.getBodyLength(),
        value,
        this::setBodyLength);
  }

  default CDActionNewsletter setBodyLength(final int len) {
    final short[] lengths = this.getFieldLengthsRaw();
    lengths[RichTextConstants.ACTIONSENDMAIL_BODYFIELD] = (short) len;
    this.setFieldLengthsRaw(lengths);
    return this;
  }

  default CDActionNewsletter setCc(final String value) {
    return StructureSupport.writeStringValueWordPadded(
        this,
        this.getViewNameLength() + this.getToLength(),
        this.getCcLength(),
        value,
        this::setCcLength);
  }

  default CDActionNewsletter setCcLength(final int len) {
    final short[] lengths = this.getFieldLengthsRaw();
    lengths[RichTextConstants.ACTIONSENDMAIL_CCFIELD] = (short) len;
    this.setFieldLengthsRaw(lengths);
    return this;
  }

  @StructureSetter("wFieldLen")
  CDActionNewsletter setFieldLengthsRaw(short[] lengths);

  @StructureSetter("dwFlags")
  CDActionNewsletter setFlags(Collection<Flag> flags);

  @StructureSetter("dwGather")
  CDActionNewsletter setGatherCount(long count);

  default CDActionNewsletter setSubject(final String value) {
    return StructureSupport.writeStringValueWordPadded(
        this,
        this.getViewNameLength() + this.getToLength() + this.getCcLength() + this.getBccLength(),
        this.getSubjectLength(),
        value,
        this::setSubjectLength);
  }

  default CDActionNewsletter setSubjectLength(final int len) {
    final short[] lengths = this.getFieldLengthsRaw();
    lengths[RichTextConstants.ACTIONSENDMAIL_SUBJECTFIELD] = (short) len;
    this.setFieldLengthsRaw(lengths);
    return this;
  }

  default CDActionNewsletter setTo(final String value) {
    return StructureSupport.writeStringValueWordPadded(
        this,
        this.getViewNameLength(),
        this.getToLength(),
        value,
        this::setToLength);
  }

  default CDActionNewsletter setToLength(final int len) {
    final short[] lengths = this.getFieldLengthsRaw();
    lengths[RichTextConstants.ACTIONSENDMAIL_TOFIELD] = (short) len;
    this.setFieldLengthsRaw(lengths);
    return this;
  }

  default CDActionNewsletter setViewName(final String viewName) {
    StructureSupport.writeStringValue(this, 0, this.getViewNameLength(), viewName, this::setViewNameLength);
    return this;
  }

  @StructureSetter("wViewNameLen")
  CDActionNewsletter setViewNameLength(int len);
}
