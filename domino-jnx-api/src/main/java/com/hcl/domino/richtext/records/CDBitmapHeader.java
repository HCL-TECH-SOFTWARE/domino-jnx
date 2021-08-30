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

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.LSIG;
import com.hcl.domino.richtext.structures.RectSize;

/**
 * @author Jesse Gallagher
 * @since 1.0.33
 */
@StructureDefinition(
  name = "CDBITMAPHEADER",
  members = {
    @StructureMember(name = "Header", type = LSIG.class),
    @StructureMember(name = "Dest", type = RectSize.class),
    @StructureMember(name = "Crop", type = RectSize.class),
    @StructureMember(name = "Flags", type = CDBitmapHeader.Flag.class, bitfield = true),
    @StructureMember(name = "wReserved", type = short.class),
    @StructureMember(name = "lReserved", type = int.class),
    @StructureMember(name = "Width", type = short.class, unsigned = true),
    @StructureMember(name = "Height", type = short.class, unsigned = true),
    @StructureMember(name = "BitsPerPixel", type = short.class, unsigned = true),
    @StructureMember(name = "SamplesPerPixel", type = short.class, unsigned = true),
    @StructureMember(name = "BitsPerSample", type = short.class, unsigned = true),
    @StructureMember(name = "SegmentCount", type = short.class, unsigned = true),
    @StructureMember(name = "ColorCount", type = short.class, unsigned = true),
    @StructureMember(name = "PatternCount", type = short.class, unsigned = true),
  }
)
public interface CDBitmapHeader extends RichTextRecord<LSIG> {
  enum Flag implements INumberEnum<Short> {
    REQUIRES_PALETTE(RichTextConstants.CDBITMAP_FLAG_REQUIRES_PALETTE),
    COMPUTE_PALETTE(RichTextConstants.CDBITMAP_FLAG_COMPUTE_PALETTE);
    private final short value;
    private Flag(short value) {
      this.value = value;
    }
    
    @Override
    public long getLongValue() {
      return value;
    }
    
    @Override
    public Short getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  LSIG getHeader();
  
  @StructureGetter("Dest")
  RectSize getDest();
  
  @StructureGetter("Crop")
  RectSize getCrop();
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDBitmapHeader setFlags(Collection<Flag> flags);
  
  @StructureGetter("Width")
  int getWidth();
  
  @StructureSetter("Width")
  CDBitmapHeader setWidth(int width);
  
  @StructureGetter("Height")
  int getHeight();
  
  @StructureSetter("Height")
  CDBitmapHeader setHeight(int height);
  
  @StructureGetter("BitsPerPixel")
  int getBitsPerPixel();
  
  @StructureSetter("BitsPerPixel")
  CDBitmapHeader setBitsPerPixel(int bpp);
  
  @StructureGetter("SamplesPerPixel")
  int getSamplesPerPixel();
  
  @StructureSetter("SamplesPerPixel")
  CDBitmapHeader setSamplesPerPixel(int spp);
  
  @StructureGetter("BitsPerSample")
  int getBitsPerSample();
  
  @StructureSetter("BitsPerSample")
  CDBitmapHeader setBitsPerSample(int bps);
  
  @StructureGetter("SegmentCount")
  int getSegmentCount();
  
  @StructureSetter("SegmentCount")
  CDBitmapHeader setSegmentCount(int count);
  
  @StructureGetter("ColorCount")
  int getColorCount();
  
  @StructureSetter("ColorCount")
  CDBitmapHeader setColorCount(int count);
  
  @StructureGetter("PatternCount")
  int getPatternCount();
  
  @StructureSetter("PatternCount")
  CDBitmapHeader setPatternCount(int count);
}
