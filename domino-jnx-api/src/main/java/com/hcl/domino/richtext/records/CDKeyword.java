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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "CDKEYWORD", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "FontID", type = FontStyle.class),
    @StructureMember(name = "Keywords", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = CDKeyword.Flag.class, bitfield = true)
})
public interface CDKeyword extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Short> {
    RADIO(RichTextConstants.CDKEYWORD_RADIO),
    FRAME_3D(RichTextConstants.CDKEYWORD_FRAME_3D),
    FRAME_STANDARD(RichTextConstants.CDKEYWORD_FRAME_STANDARD),
    FRAME_NONE(RichTextConstants.CDKEYWORD_FRAME_NONE),
    KEYWORD_RTL(RichTextConstants.CDKEYWORD_KEYWORD_RTL),
    RO_ACTIVE(RichTextConstants.CDKEYWORD_RO_ACTIVE),
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

  default List<Boolean> getEnabledStates() {
    final int count = this.getKeywordCount();
    if (count == 0) {
      return Collections.emptyList();
    }

    final ByteBuffer buf = this.getVariableData();
    final Boolean[] result = new Boolean[count];
    for (int i = 0; i < count; i++) {
      result[i] = buf.get() != 0;
    }

    return Arrays.asList(result);
  }

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureGetter("FontID")
  FontStyle getFontStyle();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  @StructureGetter("Keywords")
  int getKeywordCount();

  default List<String> getKeywords() {
    final int count = this.getKeywordCount();
    if (count == 0) {
      return Collections.emptyList();
    }

    final ByteBuffer buf = this.getVariableData();
    buf.position(count); // Skip enabled/disabled bytes
    final byte[] textList = new byte[buf.remaining()];
    buf.get(textList);
    return NativeItemCoder.get().decodeStringList(textList);
  }

  @StructureSetter("Flags")
  CDKeyword setFlags(Collection<Flag> flags);

  @StructureSetter("Keywords")
  CDKeyword setKeywordCount(int count);

  default CDKeyword setKeywords(final List<String> values, final List<Boolean> enabledStates) {
    final List<String> stringVals = values == null ? Collections.emptyList() : values;
    final List<Boolean> boolVals = enabledStates == null ? Collections.emptyList() : enabledStates;
    if (stringVals.size() != boolVals.size()) {
      throw new IllegalArgumentException("values and enabledStates must be the same size");
    }

    this.setKeywordCount(stringVals.size());
    final byte[] stringBytes = NativeItemCoder.get().encodeStringList(stringVals);
    this.resizeVariableData(stringVals.size() + stringBytes.length);
    final ByteBuffer buf = this.getVariableData();
    for (final boolean enabled : boolVals) {
      buf.put(enabled ? (byte) 1 : (byte) 0);
    }
    buf.put(stringBytes);

    return this;
  }
}
