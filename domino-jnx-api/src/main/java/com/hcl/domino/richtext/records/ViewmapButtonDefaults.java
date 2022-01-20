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

import java.util.Optional;

import com.hcl.domino.data.StandardColors;
import com.hcl.domino.design.navigator.NavigatorFillStyle;
import com.hcl.domino.design.navigator.NavigatorLineStyle;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructure;

/**
 * View Map (Navigator) Button Defaults
 * @author artcnot
 * @author Jesse Gallagher
 * @since 1.0.37
 */
@StructureDefinition(
  name = "VIEWMAP_BUTTON_DEFAULTS",
  members = {
    @StructureMember(name = "Highlight", type = ViewmapHighlightDefaults.class),
    @StructureMember(name = "LineColor", type = short.class, unsigned = true),
    @StructureMember(name = "FillFGColor", type = short.class, unsigned = true),
    @StructureMember(name = "FillBGColor", type = short.class, unsigned = true),
    @StructureMember(name = "LineStyle", type = NavigatorLineStyle.class),
    @StructureMember(name = "LineWidth", type = short.class, unsigned = true),
    @StructureMember(name = "FillStyle", type = NavigatorFillStyle.class),
    @StructureMember(name = "FontID", type = FontStyle.class)
  }
)
public interface ViewmapButtonDefaults extends MemoryStructure {
  @StructureGetter("Highlight")
  ViewmapHighlightDefaults getHighlight();

  @StructureGetter("LineColor")
  int getLineColorRaw();
  
  @StructureGetter("LineColor")
  Optional<StandardColors> getLineColor();

  @StructureSetter("LineColor")
  ViewmapButtonDefaults setLineColor(StandardColors lineColor);

  @StructureGetter("FillFGColor")
  int getFillForegroundColorRaw();
  
  @StructureGetter("FillFGColor")
  Optional<StandardColors> getFillForegroundColor();

  @StructureSetter("FillFGColor")
  ViewmapButtonDefaults setFillForegroundColor(StandardColors fillFGColor);

  @StructureGetter("FillBGColor")
  int getFillBackgroundColorRaw();

  @StructureGetter("FillBGColor")
  Optional<StandardColors> getFillBackgroundColor();

  @StructureSetter("FillBGColor")
  ViewmapButtonDefaults setFillBackgroundColor(StandardColors fillBGColor);

  @StructureGetter("LineStyle")
  NavigatorLineStyle getLineStyle();

  @StructureSetter("LineStyle")
  ViewmapButtonDefaults setLineStyle(NavigatorLineStyle lineStyle);

  @StructureGetter("LineWidth")
  int getLineWidth();

  @StructureSetter("LineWidth")
  ViewmapButtonDefaults setLineWidth(int lineWidth);

  @StructureGetter("FillStyle")
  NavigatorFillStyle getFillStyle();

  @StructureSetter("FillStyle")
  ViewmapButtonDefaults setFillStyle(NavigatorFillStyle fillStyle);

  @StructureGetter("FontID")
  FontStyle getFontID();
}
