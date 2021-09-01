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

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * Rich text record of type CDTEXT
 */
@StructureDefinition(name = "CDTEXT", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "FontID", type = FontStyle.class)
})
public interface CDText extends RichTextRecord<WSIG> {

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  /**
   * Returns the font style of the text
   *
   * @return style
   */
  @StructureGetter("FontID")
  FontStyle getStyle();

  /**
   * Returns the text stored in the text record
   *
   * @return text
   */
  default String getText() {
    final ByteBuffer buf = this.getVariableData();
    final int len = buf.remaining();
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, Charset.forName("LMBCS-native")); //$NON-NLS-1$
  }

  /**
   * Sets the text for this record.
   *
   * @param text the new text to set
   * @return this record
   */
  default CDText setText(final String text) {
    final byte[] lmbcs = text.getBytes(Charset.forName("LMBCS-native")); //$NON-NLS-1$
    this.resizeVariableData(lmbcs.length);
    final ByteBuffer buf = this.getVariableData();
    buf.put(lmbcs);
    return this;
  }

}
