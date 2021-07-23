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

import com.hcl.domino.misc.INumberEnum;
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
@StructureDefinition(name = "CDFIELDHINT", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "HintTextLength", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = CDFieldHint.Flag.class, bitfield = true),
    @StructureMember(name = "Spare", type = short.class),
    @StructureMember(name = "Spare2", type = short.class)
})
public interface CDFieldHint extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Short> {
    /**
     * CDFIELDHINT record contains hint information for limited object types in a
     * Rich Text Lite field.
     */
    LIMITED(RichTextConstants.FIELDHINT_LIMITED),
    ;

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

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  default String getHintText() {
    final ByteBuffer buf = this.getVariableData();
    final int len = this.getHintTextLength();
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
  }

  @StructureGetter("HintTextLength")
  int getHintTextLength();

  @StructureSetter("Flags")
  CDFieldHint setFlags(Collection<Flag> flags);

  default CDFieldHint setHintText(final String text) {
    final byte[] lmbcs = text == null ? new byte[0] : text.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
    this.setHintTextLength(lmbcs.length);
    this.resizeVariableData(lmbcs.length);
    final ByteBuffer buf = this.getVariableData();
    buf.put(lmbcs);
    return this;
  }

  @StructureSetter("HintTextLength")
  CDFieldHint setHintTextLength(int len);
}
