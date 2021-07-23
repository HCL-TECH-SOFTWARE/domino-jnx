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
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.LSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.2
 */
@StructureDefinition(name = "CDIMAGESEGMENT", members = {
    @StructureMember(name = "Header", type = LSIG.class),
    @StructureMember(name = "DataSize", type = short.class, unsigned = true),
    @StructureMember(name = "SegSize", type = short.class, unsigned = true)
})
public interface CDImageSegment extends RichTextRecord<LSIG> {
  @StructureGetter("DataSize")
  int getDataSize();

  @StructureGetter("Header")
  @Override
  LSIG getHeader();

  /**
   * Returns the raw data of this image segment
   *
   * @return the image segment data as a byte array
   */
  default byte[] getImageSegmentData() {
    final ByteBuffer buf = this.getVariableData();
    final int len = this.getDataSize();
    final byte[] result = new byte[len];
    buf.get(result);
    return result;
  }

  @StructureGetter("SegSize")
  int getSegSize();

  @StructureSetter("DataSize")
  CDImageSegment setDataSize(int dataSize);

  default CDImageSegment setImageSegmentData(final byte[] data) {
    return this.setImageSegmentData(data, data.length);
  }

  default CDImageSegment setImageSegmentData(final byte[] data, final int len) {
    final ByteBuffer buf = this.getVariableData();
    buf.put(data, 0, len);
    final int remaining = buf.remaining();
    for (int i = 0; i < remaining; i++) {
      buf.put((byte) 0);
    }
    return this;
  }

  @StructureSetter("SegSize")
  CDImageSegment setSegSize(int segSize);
}
