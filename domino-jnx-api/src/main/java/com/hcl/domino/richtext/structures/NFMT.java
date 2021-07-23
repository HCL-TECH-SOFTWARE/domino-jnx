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
package com.hcl.domino.richtext.structures;

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "NFMT", members = {
    @StructureMember(name = "Digits", type = byte.class, unsigned = true),
    @StructureMember(name = "Format", type = NFMT.Format.class),
    @StructureMember(name = "Attributes", type = NFMT.Attribute.class, bitfield = true),
    @StructureMember(name = "Unused", type = byte.class)
})
public interface NFMT extends MemoryStructure {
  enum Attribute implements INumberEnum<Byte> {
    /** The number will be punctuated by the appropriate separator character. */
    PUNCTUATED(RichTextConstants.NATTR_PUNCTUATED),
    /**
     * Negative numbers will be displayed in parentheses, rather than being preceded
     * by a minus sign.
     */
    PARENS(RichTextConstants.NATTR_PARENS),
    /**
     * The number entered will be interpreted as a percentage, and will be converted
     * to its
     * decimal equivalent by moving the decimal point two places to the left.
     */
    PERCENT(RichTextConstants.NATTR_PERCENT),
    /**
     * Numbers can have a varying number of decimal places (applies to Decimal &
     * Percent only).
     */
    VARYING(RichTextConstants.NATTR_VARYING),
    BYTES(RichTextConstants.NATTR_BYTES);

    private final byte value;

    Attribute(final byte value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Byte getValue() {
      return this.value;
    }
  }

  enum Format implements INumberEnum<Byte> {
    /**
     * Only the significant digits in a number will be displayed; leading zeroes and
     * trailing zeroes will be stripped.
     */
    GENERAL(RichTextConstants.NFMT_GENERAL),
    /**
     * The number displayed will contain to the right of the decimal point the
     * number of digits specified in the NFMT.Digits
     * structure member. The number will be either truncated or padded with zeroes,
     * as appropriate.
     */
    FIXED(RichTextConstants.NFMT_FIXED),
    /** The number will be displayed in scientific notation. */
    SCIENTIFIC(RichTextConstants.NFMT_SCIENTIFIC),
    /** The appropriate currency symbol will be displayed with the number. */
    CURRENCY(RichTextConstants.NFMT_CURRENCY),
    /** Numbers are displayed as byte-based size units (B, KB, MB, etc.) */
    BYTES(RichTextConstants.NFMT_BYTES);

    private final byte value;

    Format(final byte value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Byte getValue() {
      return this.value;
    }
  }

  @StructureGetter("Attributes")
  Set<Attribute> getAttributes();

  @StructureGetter("Digits")
  short getDigits();

  @StructureGetter("Format")
  Format getFormat();

  @StructureSetter("Attributes")
  NFMT setAttributes(Collection<Attribute> attriutes);

  @StructureSetter("Digits")
  NFMT setDigits(short digits);

  @StructureSetter("Format")
  NFMT setFormat(Format format);
}
