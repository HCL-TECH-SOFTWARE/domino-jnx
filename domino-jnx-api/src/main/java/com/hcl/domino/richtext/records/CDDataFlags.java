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

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "CDDATAFLAGS", members = {
    @StructureMember(name = "Header", type = BSIG.class),
    @StructureMember(name = "nFlags", type = short.class, unsigned = true),
    @StructureMember(name = "elemType", type = CDDataFlags.ElementType.class),
    @StructureMember(name = "dwReserved", type = int.class)
})
public interface CDDataFlags extends RichTextRecord<BSIG> {
  enum ElementType implements INumberEnum<Short> {
    SECTION(RichTextConstants.CD_SECTION_ELEMENT),
    FIELDLIMIT(RichTextConstants.CD_FIELDLIMIT_ELEMENT),
    BUTTONEX(RichTextConstants.CD_BUTTONEX_ELEMENT),
    TABLECELL(RichTextConstants.CD_TABLECELL_ELEMENT);

    private final short value;

    ElementType(final short value) {
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

  @StructureGetter("elemType")
  ElementType getElementType();

  @StructureGetter("nFlags")
  int getFlagCount();

  default int[] getFlags() {
    final ByteBuffer buf = this.getVariableData();
    final int[] result = new int[this.getFlagCount()];
    for (int i = 0; i < result.length; i++) {
      result[i] = buf.getInt();
    }
    return result;
  }

  @StructureGetter("Header")
  @Override
  BSIG getHeader();

  @StructureSetter("elemType")
  CDDataFlags setElementType(ElementType type);

  @StructureSetter("nFlags")
  CDDataFlags setFlagCount(int count);

  default CDDataFlags setFlags(final int[] flags) {
    final int[] storage = flags == null ? new int[0] : flags;
    this.setFlagCount(storage.length);

    this.resizeVariableData(storage.length * 4);
    final ByteBuffer buf = this.getVariableData();
    for (final int elem : storage) {
      buf.putInt(elem);
    }

    return this;
  }
}
