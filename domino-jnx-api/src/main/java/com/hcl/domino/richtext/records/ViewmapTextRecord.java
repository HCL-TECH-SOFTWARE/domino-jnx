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
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * VIEWMAP_TEXT_RECORD
 * 
 * @author artcnot
 * @author Jesse Gallagher
 * @since 1.0.38
 */
@StructureDefinition(
  name = "VIEWMAP_TEXT_RECORD", 
  members = { 
    @StructureMember(name = "DRobj", type = ViewmapBigDrawingObject.class),
    @StructureMember(name = "LineColor", type = short.class, unsigned = true), /* Color of the boundary line.   Use NOTES_COLOR_xxx value. */
    @StructureMember(name = "FillFGColor", type = short.class, unsigned = true),
    @StructureMember(name = "FillBGColor", type = short.class, unsigned = true),
    @StructureMember(name = "LineStyle", type = NavigatorLineStyle.class),
    @StructureMember(name = "LineWidth", type = short.class, unsigned = true),
    @StructureMember(name = "FillStyle", type = NavigatorFillStyle.class),
    @StructureMember(name = "Spare", type = int[].class, length = 4)

})
public interface ViewmapTextRecord extends RichTextRecord<WSIG> {

  @StructureGetter("DRobj")
  ViewmapBigDrawingObject getDrawingObject();

  @Override
  default WSIG getHeader() {
    return getDrawingObject().getHeader();
  }

  @StructureGetter("LineColor")
  short getLineColorRaw();
  
  @StructureGetter("LineColor")
  Optional<StandardColors> getLineColor();
  
  @StructureSetter("LineColor")
  ViewmapTextRecord setLineColor(StandardColors color);

  @StructureGetter("FillFGColor")
  short getFillForegroundColorRaw();
  
  @StructureGetter("FillFGColor")
  Optional<StandardColors> getFillForegroundColor();
  
  @StructureSetter("FillFGColor")
  ViewmapTextRecord setFillForegroundColor(StandardColors color);

  @StructureGetter("FillBGColor")
  short getFillBackgroundColorRaw();
  
  @StructureGetter("FillBGColor")
  Optional<StandardColors> getFillBackgroundColor();
  
  @StructureSetter("FillBGColor")
  ViewmapTextRecord setFillBackgroundColor(StandardColors color);
  
  @StructureGetter("LineStyle")
  NavigatorLineStyle getLineStyle();
  
  @StructureSetter("LineStyle")
  ViewmapTextRecord setLineStyle(NavigatorLineStyle style);

  @StructureGetter("LineWidth")
  int getLineWidth();

  @StructureSetter("LineWidth")
  ViewmapTextRecord setLineWidth(int width);
  
  @StructureGetter("FillStyle")
  NavigatorFillStyle getFillStyle();
  
  @StructureSetter("FillStyle")
  ViewmapTextRecord setFillStyle(NavigatorFillStyle style);
  
  default String getName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getDrawingObject().getNameLen()
    );
  }
  
  default ViewmapTextRecord setName(String name) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getDrawingObject().getNameLen(),
      name,
      getDrawingObject()::setNameLen
    );
  }

  default String getLabel() {
    return StructureSupport.extractStringValue(
      this,
      getDrawingObject().getNameLen(),
      getDrawingObject().getLabelLen()
    );
  }
  
  default ViewmapTextRecord setLabel(String label) {
    return StructureSupport.writeStringValue(
      this,
      getDrawingObject().getNameLen(),
      getDrawingObject().getLabelLen(),
      label,
      getDrawingObject()::setLabelLen
    );
  }
}
