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
import com.hcl.domino.richtext.structures.BSIG;

/**
 * View Map Rectangle record
 * @author Jesse Gallagher
 * @since 1.1.2
 */
@StructureDefinition(
  name = "VIEWMAP_RECT_RECORD",
  members = {
    @StructureMember(name = "DRobj", type = ViewmapDrawingObject.class),
    @StructureMember(name = "LineColor", type = short.class),
    @StructureMember(name = "FillFGColor", type = short.class),
    @StructureMember(name = "FillBGColor", type = short.class),
    @StructureMember(name = "LineStyle", type = NavigatorLineStyle.class),
    @StructureMember(name = "LineWidth", type = short.class, unsigned = true),
    @StructureMember(name = "FillStyle", type = NavigatorFillStyle.class),
    @StructureMember(name = "spare", type=int[].class, length = 4)
  }
)
public interface ViewmapRectRecord extends RichTextRecord<BSIG> {
  static int VIEWMAP_VERSION = 8;

  @StructureGetter("DRobj")
  ViewmapDrawingObject getDrawingObject();

  @Override
  default BSIG getHeader() {
    return getDrawingObject().getHeader();
  }

  @StructureGetter("LineColor")
  short getLineColorRaw();
  
  @StructureGetter("LineColor")
  Optional<StandardColors> getLineColor();
  
  @StructureSetter("LineColor")
  ViewmapRectRecord setLineColor(StandardColors color);

  @StructureGetter("FillFGColor")
  short getFillForegroundColorRaw();
  
  @StructureGetter("FillFGColor")
  Optional<StandardColors> getFillForegroundColor();
  
  @StructureSetter("FillFGColor")
  ViewmapRectRecord setFillForegroundColor(StandardColors color);

  @StructureGetter("FillBGColor")
  short getFillBackgroundColorRaw();
  
  @StructureGetter("FillBGColor")
  Optional<StandardColors> getFillBackgroundColor();
  
  @StructureSetter("FillBGColor")
  ViewmapRectRecord setFillBackgroundColor(StandardColors color);
  
  @StructureGetter("LineStyle")
  Optional<NavigatorLineStyle> getLineStyle();
  
  /**
   * Retrieves the line style as a raw {@code short}.
   * 
   * @return the line style as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("LineStyle")
  short getLineStyleRaw();
  
  @StructureSetter("LineStyle")
  ViewmapRectRecord setLineStyle(NavigatorLineStyle style);
  
  /**
   * Sets the line style as a raw {@code short}.
   * 
   * @param style the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("LineStyle")
  ViewmapRectRecord setLineStyleRaw(short style);
  
  @StructureGetter("LineWidth")
  int getLineWidth();
  
  @StructureSetter("LineWidth")
  ViewmapRectRecord setLineWidth(int width);
  
  @StructureGetter("FillStyle")
  Optional<NavigatorFillStyle> getFillStyle();
  
  /**
   * Retrieves the fill style as a raw {@code short}.
   * 
   * @return the fill style as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("FillStyle")
  short getFillStyleRaw();
  
  @StructureSetter("FillStyle")
  ViewmapRectRecord setFillStyle(NavigatorFillStyle style);
  
  /**
   * Sets the fill style as a raw {@code short}.
   * 
   * @param style the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("FillStyle")
  ViewmapRectRecord setFillStyleRaw(short style);
  
  default String getName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getDrawingObject().getNameLen()
    );
  }
  
  default ViewmapRectRecord setName(String name) {
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
  
  default ViewmapRectRecord setLabel(String label) {
    return StructureSupport.writeStringValue(
      this,
      getDrawingObject().getNameLen(),
      getDrawingObject().getLabelLen(),
      label,
      getDrawingObject()::setLabelLen
    );
  }

}
