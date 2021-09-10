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
import com.hcl.domino.richtext.structures.MemoryStructure;

/**
 * View Map (Navigator) Line Defaults
 * @author artcnot
 * @since 1.0.37
 */
@StructureDefinition(
  name = "VIEWMAP_LINE_DEFAULTS",
  members = {
    @StructureMember(name = "Highlight", type = ViewmapHighlightDefaults.class),
    @StructureMember(name = "LineColor", type = short.class, unsigned = true),
    @StructureMember(name = "FillFGColor", type = short.class, unsigned = true),
    @StructureMember(name = "FillBGColor", type = short.class, unsigned = true),
    @StructureMember(name = "LineStyle", type = short.class, unsigned = true),
    @StructureMember(name = "LineWidth", type = short.class, unsigned = true),
    @StructureMember(name = "FillStyle", type = short.class, unsigned = true)
  }
)
public interface ViewmapLineDefaults extends MemoryStructure {
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

  @StructureSetter("LineColor")
  ViewmapLineDefaults setbHighlightTouch(int lineColor);

  @StructureSetter("FillFGColor")
  ViewmapLineDefaults setbHighlightCurrent(int fillFGColor);

  @StructureSetter("FillBGColor")
  ViewmapLineDefaults setHLOutlineColor(int fillBGColor);

  @StructureSetter("LineStyle")
  ViewmapLineDefaults setHLOutlineWidth(int lineStyle);

  @StructureSetter("LineWidth")
  ViewmapLineDefaults setHLOutlineStyle(int lineWidth);

  @StructureSetter("FillStyle")
  ViewmapLineDefaults setHLFillColor(int fillStyle);
}
