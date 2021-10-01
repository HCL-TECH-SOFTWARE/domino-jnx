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
 * A pattern table is a fixed-size color table used for patterns by
 * CDBITMAPHEADER (patterns are used to compress the bitmap).
 * A entry in the pattern table is 8 (PELS_PER_PATTERN) packed colors
 * (3 bytes per color as above). 
 */
@StructureDefinition(
  name = "CDPATTERNTABLE",
  members = {
    @StructureMember(name = "Header", type = LSIG.class)
    /* One or more pattern table entries */

  }
)
public interface CDPatternTable extends RichTextRecord<LSIG> {
  @StructureGetter("Header")
  @Override
  LSIG getHeader();
/*
  default String getPatternTableEntries() {
    return StructureSupport.extractStringValue(
      this,
      6, // header length (The total of all variable elements before this one)
      this.getHeader().getLength() - 6    // the length of this element
    );
  }

  default CDPatternTable setPatternTableEntries(final String entries) {
    return StructureSupport.writeStringValue(
      this,
      6,
      (int)(this.getHeader().getLength() - 6),
      entries,
      (int len) -> {}     // The length is not in this record, it's in BITMAPHEADER
    );
  }
*/

  default byte[] getPatternTableEntries() {
    final ByteBuffer buf = this.getVariableData();
    /* Is there a way to get PatternCount from CDBITMAPHEADER istead of ... */
    final int len = (int) (this.getHeader().getLength() - 6); /* note length  -  header length  */
    final byte[] data = new byte[len];
    buf.get(data);
    return data;
  }

  default CDPatternTable setPatternTableEntries(final byte[] data) {
    final ByteBuffer buf = this.getVariableData();
    buf.put(data);
    final int remaining = buf.remaining();
    for (int i = 0; i < remaining; i++) {
      buf.put((byte) 0);
    }
    return this;
  }
}


