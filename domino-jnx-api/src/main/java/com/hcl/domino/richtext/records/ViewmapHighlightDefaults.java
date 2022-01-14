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
import com.hcl.domino.richtext.structures.MemoryStructure;

/**
 * View Map (Navigator) Highlight Defaults
 * @author artcnot
 * @since 1.0.37
 */
@StructureDefinition(
  name = "VIEWMAP_HIGHLIGHT_DEFAULTS",
  members = {
    @StructureMember(name = "bHighlightTouch", type = short.class, unsigned = true),
    @StructureMember(name = "bHighlightCurrent", type = short.class, unsigned = true),
    @StructureMember(name = "HLOutlineColor", type = short.class, unsigned = true),
    @StructureMember(name = "HLOutlineWidth", type = short.class, unsigned = true),
    @StructureMember(name = "HLOutlineStyle", type = short.class, unsigned = true),
    @StructureMember(name = "HLFillColor", type = short.class, unsigned = true)
  }
)
public interface ViewmapHighlightDefaults extends MemoryStructure {
  @StructureGetter("bHighlightTouch")
  int getbHighlightTouch();

  @StructureGetter("bHighlightCurrent")
  int getbHighlightCurrent();

  @StructureGetter("HLOutlineColor")
  int getHLOutlineColor();

  @StructureGetter("HLOutlineWidth")
  int getHLOutlineWidth();

  @StructureGetter("HLOutlineStyle")
  int getHLOutlineStyle();

  @StructureGetter("HLFillColor")
  int getHLFillColor();

  @StructureSetter("bHighlightTouch")
  ViewmapHighlightDefaults setbHighlightTouch(int touch);

  @StructureSetter("bHighlightCurrent")
  ViewmapHighlightDefaults setbHighlightCurrent(int current);

  @StructureSetter("HLOutlineColor")
  ViewmapHighlightDefaults setHLOutlineColor(int outlineColor);

  @StructureSetter("HLOutlineWidth")
  ViewmapHighlightDefaults setHLOutlineWidth(int outlineWidth);

  @StructureSetter("HLOutlineStyle")
  ViewmapHighlightDefaults setHLOutlineStyle(int outlineStyle);

  @StructureSetter("HLFillColor")
  ViewmapHighlightDefaults setHLFillColor(int fillColor);
}
