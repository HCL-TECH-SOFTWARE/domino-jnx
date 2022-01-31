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

import java.nio.ByteBuffer;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.LSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDBITMAPSEGMENT",
  members = {
    @StructureMember(name = "Header", type = LSIG.class),
    @StructureMember(name = "Reserved", type = int[].class, length = 2),
    @StructureMember(name = "ScanlineCount", type = short.class, unsigned = true),
    @StructureMember(name = "DataSize", type = short.class, unsigned = true)
    /* Comressed raster data for the segment follows right here */
  }
)
public interface CDBitmapSegment extends RichTextRecord<LSIG> {
  @StructureGetter("Header")
  @Override
  LSIG getHeader();
  
  @StructureGetter("ScanlineCount")
  int getScanlineCount();
  
  @StructureGetter("DataSize")
  int getDataSize();

  /* BitmapData follows after the BitmapName and the DisplayLabel (i.e at the end) */
  default byte[] getBitmapData() {
    final ByteBuffer buf = this.getVariableData();
    final int len = this.getDataSize();
    final byte[] data = new byte[len];
    buf.get(data);
    return data;
  }
  
  @StructureSetter("ScanlineCount")
  CDBitmapSegment setScanlineCount(int count);

  @StructureSetter("DataSize")
  CDBitmapSegment setDataSize(int size);

  default CDBitmapSegment setFileSegmentData(final byte[] data) {
    final ByteBuffer buf = this.getVariableData();
    buf.put(data);
    final int remaining = buf.remaining();
    for (int i = 0; i < remaining; i++) {
      buf.put((byte) 0);
    }
    return this;
  }
}
