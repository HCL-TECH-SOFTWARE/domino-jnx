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
package com.hcl.domino.richtext.structures;

import java.util.Optional;

import com.hcl.domino.data.StandardColors;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "ELEMENTHEADER",
  members = {
    @StructureMember(name = "wLeft", type = short.class, unsigned = true),
    @StructureMember(name = "wTop", type = short.class, unsigned = true),
    @StructureMember(name = "wWidth", type = short.class, unsigned = true),
    @StructureMember(name = "wHeight", type = short.class, unsigned = true),
    @StructureMember(name = "FontID", type = FontStyle.class),
    @StructureMember(name = "byBackColor", type = byte.class),
    @StructureMember(name = "bSpare", type = byte.class),
    @StructureMember(name = "BackgroundColor", type = ColorValue.class),
  }
)
public interface ElementHeader extends MemoryStructure {
  @StructureGetter("wLeft")
  int getLeft();
  
  @StructureSetter("wLeft")
  ElementHeader setLeft(int left);
  
  @StructureGetter("wTop")
  int getTop();
  
  @StructureSetter("wTop")
  ElementHeader setTop(int top);
  
  @StructureGetter("wWidth")
  int getWidth();
  
  @StructureSetter("wWidth")
  ElementHeader setWidth(int width);
  
  @StructureGetter("wHeight")
  int getHeight();
  
  @StructureSetter("wHeight")
  ElementHeader setHeight(int height);
  
  @StructureGetter("FontID")
  FontStyle getFontID();
  
  @StructureGetter("byBackColor")
  Optional<StandardColors> getPreV5BackgroundColor();
  
  @StructureSetter("byBackColor")
  ElementHeader setPreV5BackgroundColor(StandardColors color);
  
  @StructureGetter("BackgroundColor")
  ColorValue getBackgroundColor();
}
