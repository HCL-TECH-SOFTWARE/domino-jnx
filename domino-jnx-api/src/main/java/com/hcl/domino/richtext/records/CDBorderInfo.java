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

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.design.format.BorderStyle;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.32
 */
@StructureDefinition(
  name = "CDBORDERINFO",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = int.class),
    @StructureMember(name = "BorderStyle", type = BorderStyle.class),
    @StructureMember(name = "BorderWidthTop", type = short.class, unsigned = true),
    @StructureMember(name = "BorderWidthLeft", type = short.class, unsigned = true),
    @StructureMember(name = "BorderWidthBottom", type = short.class, unsigned = true),
    @StructureMember(name = "BorderWidthRight", type = short.class, unsigned = true),
    @StructureMember(name = "dwSpare", type = int.class),
    @StructureMember(name = "BorderFlags", type = CDBorderInfo.BorderFlag.class, bitfield = true),
    @StructureMember(name = "DropShadowWidth", type = short.class, unsigned = true),
    @StructureMember(name = "InnerWidthTop", type = short.class, unsigned = true),
    @StructureMember(name = "InnerWidthLeft", type = short.class, unsigned = true),
    @StructureMember(name = "InnerWidthBottom", type = short.class, unsigned = true),
    @StructureMember(name = "InnerWidthRight", type = short.class, unsigned = true),
    @StructureMember(name = "OuterWidthTop", type = short.class, unsigned = true),
    @StructureMember(name = "OuterWidthLeft", type = short.class, unsigned = true),
    @StructureMember(name = "OuterWidthBottom", type = short.class, unsigned = true),
    @StructureMember(name = "OuterWidthRight", type = short.class, unsigned = true),
    @StructureMember(name = "Color", type = ColorValue.class),
    @StructureMember(name = "wSpares", type = short[].class, length = 5)
  }
)
public interface CDBorderInfo extends RichTextRecord<WSIG> {
  enum BorderFlag implements INumberEnum<Short> {
    DROP_SHADOW(RichTextConstants.CDBORDER_FLAGS_DROP_SHADOW);
    private final short value;
    private BorderFlag(short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Short getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("BorderStyle")
  BorderStyle getBorderStyle();
  
  @StructureSetter("BorderStyle")
  CDBorderInfo setBorderStyle(BorderStyle style);
  
  @StructureGetter("BorderWidthTop")
  int getBorderWidthTop();
  
  @StructureSetter("BorderWidthTop")
  CDBorderInfo setBorderWidthTop(int width);
  
  @StructureGetter("BorderWidthLeft")
  int getBorderWidthLeft();
  
  @StructureSetter("BorderWidthLeft")
  CDBorderInfo setBorderWidthLeft(int width);
  
  @StructureGetter("BorderWidthBottom")
  int getBorderWidthBottom();
  
  @StructureSetter("BorderWidthBottom")
  CDBorderInfo setBorderWidthBottom(int width);
  
  @StructureGetter("BorderWidthRight")
  int getBorderWidthRight();
  
  @StructureSetter("BorderWidthRight")
  CDBorderInfo setBorderWidthRight(int width);
  
  @StructureGetter("BorderFlags")
  Set<BorderFlag> getBorderFlags();
  
  @StructureSetter("BorderFlags")
  CDBorderInfo setBorderFlags(Collection<BorderFlag> flags);
  
  @StructureGetter("DropShadowWidth")
  int getDropShadowWidth();
  
  @StructureSetter("DropShadowWidth")
  CDBorderInfo setDropShadowWidth(int width);
  
  @StructureGetter("InnerWidthTop")
  int getInnerWidthTop();
  
  @StructureSetter("InnerWidthTop")
  CDBorderInfo setInnerWidthTop(int width);
  
  @StructureGetter("InnerWidthLeft")
  int getInnerWidthLeft();
  
  @StructureSetter("InnerWidthLeft")
  CDBorderInfo setInnerWidthLeft(int width);
  
  @StructureGetter("InnerWidthBottom")
  int getInnerWidthBottom();
  
  @StructureSetter("InnerWidthBottom")
  CDBorderInfo setInnerWidthBottom(int width);
  
  @StructureGetter("InnerWidthRight")
  int getInnerWidthRight();
  
  @StructureSetter("InnerWidthRight")
  CDBorderInfo setInnerWidthRight(int width);
  
  @StructureGetter("OuterWidthTop")
  int getOuterWidthTop();
  
  @StructureSetter("OuterWidthTop")
  CDBorderInfo setOuterWidthTop(int width);
  
  @StructureGetter("OuterWidthLeft")
  int getOuterWidthLeft();
  
  @StructureSetter("OuterWidthLeft")
  CDBorderInfo setOuterWidthLeft(int width);
  
  @StructureGetter("OuterWidthBottom")
  int getOuterWidthBottom();
  
  @StructureSetter("OuterWidthBottom")
  CDBorderInfo setOuterWidthBottom(int width);
  
  @StructureGetter("OuterWidthRight")
  int getOuterWidthRight();
  
  @StructureSetter("OuterWidthRight")
  CDBorderInfo setOuterWidthRight(int width);
  
  @StructureGetter("Color")
  ColorValue getColor();
}
