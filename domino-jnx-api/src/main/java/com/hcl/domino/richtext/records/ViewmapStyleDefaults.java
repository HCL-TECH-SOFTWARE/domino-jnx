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
 * View Map (Navigator) Style Defaults
 * @author artcnot
 * @since 1.0.15
 */
@StructureDefinition(
  name = "VIEWMAP_STYLE_DEFAULTS",
  members = {
    @StructureMember(name = "Shapes", type = ViewmapShapeDefaults.class),
    @StructureMember(name = "Lines", type = ViewmapLineDefaults.class),
    @StructureMember(name = "Regions", type = ViewmapRegionDefaults.class),
    @StructureMember(name = "Buttons", type = ViewmapButtonDefaults.class),
    @StructureMember(name = "Bitmaps", type = ViewmapBitmapDefaults.class),
    @StructureMember(name = "TextBoxes", type = ViewmapTextboxDefaults.class)
  }
)
public interface ViewmapStyleDefaults extends MemoryStructure {
  @StructureGetter("Shapes")
  ViewmapShapeDefaults getShapes();

  @StructureGetter("Lines")
  ViewmapLineDefaults getLines();

  @StructureGetter("Regions")
  ViewmapRegionDefaults getRegions();

  @StructureGetter("Buttons")
  ViewmapButtonDefaults getButtons();

  @StructureGetter("Bitmaps")
  ViewmapBitmapDefaults getBitmaps();

  @StructureGetter("TextBoxes")
  ViewmapTextboxDefaults getTextBoxes();

  @StructureSetter("Shapes")
  ViewmapStyleDefaults setShapes(ViewmapShapeDefaults type);

  @StructureSetter("Lines")
  ViewmapStyleDefaults setLines(ViewmapLineDefaults type);

  @StructureSetter("Regions")
  ViewmapStyleDefaults setRegions(ViewmapRegionDefaults type);

  @StructureSetter("Buttons")
  ViewmapStyleDefaults setButtons(ViewmapButtonDefaults type);

  @StructureSetter("Bitmaps")
  ViewmapStyleDefaults setBitmaps(ViewmapBitmapDefaults type);

  @StructureSetter("TextBoxes")
  ViewmapStyleDefaults setTextBoxes(ViewmapTextboxDefaults type);

}
