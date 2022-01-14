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

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * View Map (Navigator) Data
 * 
 * @author artcnot
 * @since 1.0.37
 */
@StructureDefinition(
  name = "VIEWMAP_DATASET_RECORD", 
  members = { 
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Version", type = short.class, unsigned = true),
    @StructureMember(name = "ViewNameLen", type = short.class, unsigned = true), /* length of initial view name; 0 if none */
    @StructureMember(name = "Gridsize", type = short.class, unsigned = true), /* (in pixels) */
    @StructureMember(name = "Flags", type = ViewmapDatasetRecord.Flags.class, bitfield = true),
    @StructureMember(name = "bAutoAdjust", type = short.class, unsigned = true),
    @StructureMember(name = "BGColor", type = short.class, unsigned = true),
    /* highest sequence number for each type of draw object supported (w/extra space for future) */
    @StructureMember(name = "SeqNums", type = short[].class, length = ViewmapDatasetRecord.VM_MAX_OBJTYPES, unsigned = true),
    @StructureMember(name = "StyleDefaults", type = ViewmapStyleDefaults.class),
    @StructureMember(name = "NumPaletteEntries", type = short.class, unsigned = true),
    @StructureMember(name = "ViewDesignType", type = short.class, unsigned = true), /* design type of initial view */
    @StructureMember(name = "BGColorValue", type = ColorValue.class), /* BG color stored in some color space */
    @StructureMember(name = "Spare", type = int[].class, length = 14)

})
public interface ViewmapDatasetRecord extends RichTextRecord<WSIG> {
  static int VM_MAX_OBJTYPES = 32;

  /* Navigator Dataset Flags VM_DSET_XXXXXXXX */
  enum Flags implements INumberEnum<Short> {
    SHOW_GRID((short)0x0001), 
    SNAPTO_GRID((short)0x0002), 
    SAVE_IMAGEMAP((short)0x0004);

    private final short value;
    private Flags(short value) {
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
  WSIG getHeader();

  @StructureGetter("Version")
  int getVersion();

  @StructureGetter("ViewNameLen")
  int getViewNameLen();

  @StructureGetter("Gridsize")
  int getGridsize();

  @StructureGetter("Flags")
  Set<Flags> getFlags();

  @StructureGetter("bAutoAdjust")
  int getbAutoAdjust();

  @StructureGetter("BGColor")
  int getBGColor();

  @StructureGetter("SeqNums")
  int[] getSeqNums();

  @StructureGetter("StyleDefaults")
  ViewmapStyleDefaults getStyleDefaults();

  @StructureGetter("NumPaletteEntries")
  int getNumPaletteEntries();

  @StructureGetter("ViewDesignType")
  int getViewDesignType();

  @StructureGetter("BGColorValue")
  ColorValue getBGColorValue();

  @StructureGetter("Spare")
  int[] getSpare();

  @StructureSetter("Version")
  ViewmapDatasetRecord setVersion(int version);

  @StructureSetter("ViewNameLen")
  ViewmapDatasetRecord setViewNameLen(int viewNameLen);

  @StructureSetter("Gridsize")
  ViewmapDatasetRecord setGridsize(int gridsize);

  @StructureSetter("Flags")
  ViewmapDatasetRecord setFlags(Collection<Flags> flags);

  @StructureSetter("bAutoAdjust")
  ViewmapDatasetRecord setbAutoAdjust(int bAutoAdjust);

  @StructureSetter("BGColor")
  ViewmapDatasetRecord setBGColor(int bGColor);

  @StructureSetter("NumPaletteEntries")
  ViewmapDatasetRecord setNumPaletteEntries(int numPaletteEntries);

  @StructureSetter("ViewDesignType")
  ViewmapDatasetRecord setViewDesignType(int viewDesignType);

  // NO setter for BGColorValue because ... ?
}
