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

import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;

/**
 * VIEWMAP_BITMAP_RECORD
 * 
 * @author artcnot
 * @since 1.0.39
 */
@StructureDefinition(
  name = "VIEWMAP_BITMAP_RECORD", 
  members = { 
    @StructureMember(name = "DRobj", type = VMODSdrobj.class),
    @StructureMember(name = "DataLen", type = short.class, unsigned = true),
    @StructureMember(name = "xBytes", type = short.class, unsigned = true),
    @StructureMember(name = "yBits", type = short.class, unsigned = true),
    @StructureMember(name = "zBits", type = short.class, unsigned = true),
    @StructureMember(name = "Spare", type = int[].class, length = 4)
    // This structure is followed by the bitmap name and the display label (if any) 
    // in packed format (no terminating NUL), then by the bitmap data.
})
public interface ViewmapBitmapRecord extends RichTextRecord<BSIG> {
  @StructureGetter("DRobj")
  VMODSdrobj getDRObj();

  @StructureGetter("DataLen")
  int getDataLen();

  @StructureGetter("xBytes")
  int getxBytes();

  @StructureGetter("yBits")
  int getyBits();

  @StructureGetter("zBits")
  int getzBits();

  /* BitmapName follows immediately after the above structure. */
  default String getBitmapName() {
    return StructureSupport.extractStringValue(
      this,
      0, // The total of all variable elements before this one
      this.getDRObj().getNameLen() // the length of this element
    );
  }

  /* DisplayLabel follows after the BitmapName */
  default String getDisplayLabel() {
    return StructureSupport.extractStringValue(
      this,
      this.getDRObj().getNameLen(), // The total of all variable elements before this one
      this.getDRObj().getLabelLen() // the length of this element
    );
  }

  /* BitmapData follows after the BitmapName and the DisplayLabel (i.e at the end) */
  default byte[] getBitmapData() {
    final ByteBuffer buf = this.getVariableData();
    final int len = this.getDataLen();
    final byte[] data = new byte[len];
    buf.get(data);
    return data;
  }

  @StructureSetter("DataLen")
   ViewmapBitmapRecord setDataLen(int length);

  @StructureSetter("xBytes")
   ViewmapBitmapRecord setxBytes(int bytes);

  @StructureSetter("yBits")
   ViewmapBitmapRecord setyBits(int bits);

  @StructureSetter("zBits")
   ViewmapBitmapRecord setzBits(int bits);

  default ViewmapBitmapRecord setBitmapName(final String name) {
    return StructureSupport.writeStringValue(
      this,
      0,
      this.getDRObj().getNameLen(),
      name,
      this.getDRObj()::setNameLen
    );
  }
  
  default ViewmapBitmapRecord setDisplayLabel(final String label) {
    return StructureSupport.writeStringValue(
      this,
      this.getDRObj().getNameLen(),
      this.getDRObj().getLabelLen(),
      label,
      this.getDRObj()::setLabelLen
    );
  }
  


  default ViewmapBitmapRecord setFileSegmentData(final byte[] data) {
    final ByteBuffer buf = this.getVariableData();
    buf.put(data);
    final int remaining = buf.remaining();
    for (int i = 0; i < remaining; i++) {
      buf.put((byte) 0);
    }
    return this;
  }

}
