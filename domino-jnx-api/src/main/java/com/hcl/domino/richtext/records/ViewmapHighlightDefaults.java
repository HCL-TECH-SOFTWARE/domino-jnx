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
import com.hcl.domino.design.navigator.NavigatorLineStyle;
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
    @StructureMember(name = "HLOutlineStyle", type = NavigatorLineStyle.class),
    @StructureMember(name = "HLFillColor", type = short.class, unsigned = true)
  }
)
public interface ViewmapHighlightDefaults extends MemoryStructure {
  @StructureGetter("bHighlightTouch")
  boolean isTouch();

  @StructureSetter("bHighlightTouch")
  ViewmapHighlightDefaults setTouch(boolean touch);

  @StructureGetter("bHighlightCurrent")
  boolean isCurrent();

  @StructureSetter("bHighlightCurrent")
  ViewmapHighlightDefaults setCurrent(boolean current);
  
  @StructureGetter("HLOutlineColor")
  int getOutlineColorRaw();

  @StructureGetter("HLOutlineColor")
  Optional<StandardColors> getOutlineColor();

  @StructureSetter("HLOutlineColor")
  ViewmapHighlightDefaults setOutlineColor(StandardColors outlineColor);

  /**
   * Sets the outline color as a raw {@code int}.
   * 
   * @param outlineColor the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("HLOutlineColor")
  ViewmapHighlightDefaults setOutlineColorRaw(int outlineColor);

  @StructureGetter("HLOutlineWidth")
  int getOutlineWidth();

  @StructureSetter("HLOutlineWidth")
  ViewmapHighlightDefaults setOutlineWidth(int outlineWidth);

  @StructureGetter("HLOutlineStyle")
  Optional<NavigatorLineStyle> getOutlineStyle();

  /**
   * Retrieves the outline style as a raw {@code short}.
   * 
   * @return the outline style as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("HLOutlineStyle")
  short getOutlineStyleRaw();

  @StructureSetter("HLOutlineStyle")
  ViewmapHighlightDefaults setOutlineStyle(NavigatorLineStyle outlineStyle);

  /**
   * Sets the outline style as a raw {@code short}.
   * 
   * @param outlineStyle the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("HLOutlineStyle")
  ViewmapHighlightDefaults setOutlineStyleRaw(short outlineStyle);

  @StructureGetter("HLFillColor")
  int getFillColorRaw();
  
  @StructureGetter("HLFillColor")
  Optional<StandardColors> getFillColor();

  @StructureSetter("HLFillColor")
  ViewmapHighlightDefaults setFillColor(StandardColors fillColor);

  /**
   * Sets the fill color as a raw {@code int}.
   * 
   * @param fillColor the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("HLFillColor")
  ViewmapHighlightDefaults setFillColorRaw(int fillColor);
}
