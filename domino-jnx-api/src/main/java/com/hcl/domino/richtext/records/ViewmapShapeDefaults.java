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

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructure;

/**
 * View Map (Navigator) Shape Defaults
 * @author artcnot
 * @since 1.0.37
 */
@StructureDefinition(
  name = "VIEWMAP_SHAPE_DEFAULTS",
  members = {
    @StructureMember(name = "Highlight", type = ViewmapHighlightDefaults.class),
    @StructureMember(name = "LineColor", type = short.class, unsigned = true),
    @StructureMember(name = "FillFGColor", type = short.class, unsigned = true),
    @StructureMember(name = "FillBGColor", type = short.class, unsigned = true),
    @StructureMember(name = "LineStyle", type = short.class, unsigned = true),
    @StructureMember(name = "LineWidth", type = short.class, unsigned = true),
    @StructureMember(name = "FillStyle", type = short.class, unsigned = true),
    @StructureMember(name = "FontID", type = FontStyle.class)
  }
)
public interface ViewmapShapeDefaults extends MemoryStructure {
  @StructureGetter("Highlight")
  ViewmapHighlightDefaults getHighlight();

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

  @StructureGetter("FontID")
  FontStyle getFontID();

  @StructureSetter("LineColor")
  ViewmapShapeDefaults setLineColor(int lineColor);

  @StructureSetter("FillFGColor")
  ViewmapShapeDefaults setFillFGColor(int fillFGColor);

  @StructureSetter("FillBGColor")
  ViewmapShapeDefaults setFillBGColor(int fillBGColor);

  @StructureSetter("LineStyle")
  ViewmapShapeDefaults setLineStyle(int lineStyle);

  @StructureSetter("LineWidth")
  ViewmapShapeDefaults setLineWidth(int lineWidth);

  @StructureSetter("FillStyle")
  ViewmapShapeDefaults setFillStyle(int fillStyle);
}
