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
import com.hcl.domino.richtext.structures.WSIG;

/**
 * VIEWMAP_TEXT_RECORD
 * 
 * @author artcnot
 * @since 1.0.38
 */
@StructureDefinition(
  name = "VIEWMAP_TEXT_RECORD", 
  members = { 
    @StructureMember(name = "DRobj", type = VMODSbigobj.class),
    @StructureMember(name = "LineColor", type = short.class, unsigned = true), /* Color of the boundary line.   Use NOTES_COLOR_xxx value. */
    @StructureMember(name = "FillFGColor", type = short.class, unsigned = true),
    @StructureMember(name = "FillBGColor", type = short.class, unsigned = true),
    @StructureMember(name = "LineStyle", type = short.class, unsigned = true),
    @StructureMember(name = "LineWidth", type = short.class, unsigned = true),
    @StructureMember(name = "FillStyle", type = short.class, unsigned = true),
    @StructureMember(name = "Spare", type = int[].class, length = 4)

})
public interface ViewmapTextRecord extends RichTextRecord<WSIG> {
  @StructureGetter("DRobj")
  VMODSbigobj getDRObj();

  @StructureGetter("LineColor")
  int getLineColor();

  @StructureGetter("FillFGColor")
  int getFillFGColor();

  @StructureGetter("FillBGColor")
  int getFillBGColor();

  @StructureGetter("LineStyle")
  int getLineStyle();

  @StructureGetter("LineWidth")
  int getLineWidth();

  @StructureGetter("FillStyle")
  int getFillStyle();

  @StructureSetter("LineColor")
  ViewmapTextRecord setLineColor(int color);

  @StructureSetter("FillFGColor")
   ViewmapTextRecord setFillFGColor(int color);

  @StructureSetter("FillBGColor")
   ViewmapTextRecord setFillBGColor(int color);

  @StructureSetter("LineStyle")
   ViewmapTextRecord setLineStyle(int style);

  @StructureSetter("LineWidth")
   ViewmapTextRecord setLineWidth(int width);

  @StructureSetter("FillStyle")
   ViewmapTextRecord setFillStyle(int style);
}
