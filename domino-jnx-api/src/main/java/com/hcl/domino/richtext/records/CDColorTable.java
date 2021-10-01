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

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.structures.LSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 * 
 * Bitmap Color Table. If the bitmap is 8 bit color or grey scale, you
 * must have a color table.  However, you only need as many entries as
 * you have colors, i.e. if a 16 color bitmap was converted to 8 bit
 * form for Notes, the color table would only require 16 entries even
 * though 8 bit color implies 256 entries.  The number of entries must
 * match that specified in the CDBITMAPHEADER ColorCount. 
 */
@StructureDefinition(
  name = "CDCOLORTABLE",
  members = {
    @StructureMember(name = "Header", type = LSIG.class)
    /* One or more color table entries go here */
  }
)
public interface CDColorTable extends RichTextRecord<LSIG> {
  @StructureGetter("Header")
  @Override
  LSIG getHeader();

  default byte[] getColorTableEntries() {
    final ByteBuffer buf = this.getVariableData();
    /* Is there a way to get ColorCount from CDBITMAPHEADER istead of ... */
    final int len = (int) (this.getHeader().getLength() - 6); /* note length  -  header length  */
    final byte[] data = new byte[len];
    buf.get(data);
    return data;
  }

  default CDColorTable setColorTableEntries(final byte[] data) {
    final ByteBuffer buf = this.getVariableData();
    buf.put(data);
    final int remaining = buf.remaining();
    for (int i = 0; i < remaining; i++) {
      buf.put((byte) 0);
    }
    return this;
  }

}
