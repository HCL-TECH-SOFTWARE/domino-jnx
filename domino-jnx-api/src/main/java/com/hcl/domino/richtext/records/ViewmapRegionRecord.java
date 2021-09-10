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

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;

/**
 * VIEWMAP_REGION_RECORD
 * 
 * @author artcnot
 * @since 1.0.39
 */
@StructureDefinition(
  name = "VIEWMAP_REGION_RECORD", 
  members = { 
    @StructureMember(name = "DRobj", type = VMODSdrobj.class),
    @StructureMember(name = "LineColor", type = short.class, unsigned = true),
    @StructureMember(name = "LineStyle", type = short.class, unsigned = true),
    @StructureMember(name = "LineWidth", type = short.class, unsigned = true),
    @StructureMember(name = "FillStyle", type = short.class, unsigned = true),
    @StructureMember(name = "Spare", type = int[].class, length = 4)

})
public interface ViewmapRegionRecord extends RichTextRecord<BSIG> {
  @StructureGetter("DRobj")
  VMODSdrobj getDRObj();

  @StructureGetter("LineColor")
  int getLineColor();

  @StructureGetter("LineStyle")
  int getLineStyle();

  @StructureGetter("LineWidth")
  int getLineWidth();

  @StructureGetter("FillStyle")
  int getFillStyle();

  @StructureSetter("LineColor")
  ViewmapRegionRecord setLineColor(int color);

  @StructureSetter("LineStyle")
  ViewmapRegionRecord setLineStyle(int style);

  @StructureSetter("LineWidth")
  ViewmapRegionRecord setLineWidth(int width);

  @StructureSetter("FillStyle")
  ViewmapRegionRecord setFillStyle(int style);

}
