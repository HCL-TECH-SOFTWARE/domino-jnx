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
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.StandardColors;
import com.hcl.domino.design.DesignType;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
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
 * @author Jesse Gallagher
 * @since 1.0.37
 */
@StructureDefinition(
  name = "VIEWMAP_DATASET_RECORD", 
  members = { 
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Version", type = short.class, unsigned = true),
    @StructureMember(name = "ViewNameLen", type = short.class, unsigned = true), /* length of initial view name; 0 if none */
    @StructureMember(name = "Gridsize", type = short.class, unsigned = true), /* (in pixels) */
    @StructureMember(name = "Flags", type = ViewmapDatasetRecord.Flag.class, bitfield = true),
    @StructureMember(name = "bAutoAdjust", type = short.class, unsigned = true),
    @StructureMember(name = "BGColor", type = short.class, unsigned = true),
    /* highest sequence number for each type of draw object supported (w/extra space for future) */
    @StructureMember(name = "SeqNums", type = short[].class, length = ViewmapDatasetRecord.VM_MAX_OBJTYPES, unsigned = true),
    @StructureMember(name = "StyleDefaults", type = ViewmapStyleDefaults.class),
    @StructureMember(name = "NumPaletteEntries", type = short.class, unsigned = true),
    @StructureMember(name = "ViewDesignType", type = DesignType.class), /* design type of initial view */
    @StructureMember(name = "BGColorValue", type = ColorValue.class), /* BG color stored in some color space */
    @StructureMember(name = "Spare", type = int[].class, length = 14)

})
public interface ViewmapDatasetRecord extends RichTextRecord<WSIG> {
  static int VM_MAX_OBJTYPES = 32;

  /* Navigator Dataset Flags VM_DSET_XXXXXXXX */
  enum Flag implements INumberEnum<Short> {
    /** show the grid in design mode, NIY */
    SHOW_GRID(NotesConstants.VM_DSET_SHOW_GRID),
    /** snap to grid */
    SNAPTO_GRID(NotesConstants.VM_DSET_SNAPTO_GRID),
    /** save web imagemap of navigator so it looks good on the web */
    SAVE_IMAGEMAP(NotesConstants.VM_DSET_SAVE_IMAGEMAP),
    /** reading order */
    READING_ORDER_RTL(NotesConstants.VM_DSET_READING_ORDER_RTL);


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
  WSIG getHeader();

  @StructureGetter("Version")
  int getVersion();

  @StructureSetter("Version")
  ViewmapDatasetRecord setVersion(int version);

  @StructureGetter("ViewNameLen")
  int getViewNameLen();

  @StructureSetter("ViewNameLen")
  ViewmapDatasetRecord setViewNameLen(int viewNameLen);

  @StructureGetter("Gridsize")
  int getGridSize();

  @StructureSetter("Gridsize")
  ViewmapDatasetRecord setGridSize(int gridsize);

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureSetter("Flags")
  ViewmapDatasetRecord setFlags(Collection<Flag> flags);

  @StructureGetter("bAutoAdjust")
  boolean isAutoAdjust();

  @StructureSetter("bAutoAdjust")
  ViewmapDatasetRecord setAutoAdjust(boolean autoAdjust);

  @StructureGetter("BGColor")
  int getBackgroundColorRaw();
  
  @StructureGetter("BGColor")
  Optional<StandardColors> getBackgroundColor();

  @StructureSetter("BGColor")
  ViewmapDatasetRecord setBackgroundColor(StandardColors color);

  @StructureGetter("SeqNums")
  int[] getSeqNums();
  
  @StructureSetter("SeqNums")
  ViewmapDatasetRecord setSeqNums(int[] nums);

  @StructureGetter("StyleDefaults")
  ViewmapStyleDefaults getStyleDefaults();

  @StructureGetter("NumPaletteEntries")
  int getNumPaletteEntries();

  @StructureSetter("NumPaletteEntries")
  ViewmapDatasetRecord setNumPaletteEntries(int numPaletteEntries);

  @StructureGetter("ViewDesignType")
  DesignType getViewDesignType();

  @StructureSetter("ViewDesignType")
  ViewmapDatasetRecord setViewDesignType(DesignType viewDesignType);

  @StructureGetter("BGColorValue")
  ColorValue getBackgroundColorValue();
  
  default String getViewName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getViewNameLen()
    );
  }
  default ViewmapDatasetRecord setViewName(String viewName) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getViewNameLen(),
      viewName,
      this::setViewNameLen
    );
  }
}
