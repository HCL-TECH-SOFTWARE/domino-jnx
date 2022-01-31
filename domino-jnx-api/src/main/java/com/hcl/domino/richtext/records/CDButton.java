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

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDBUTTON",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDButton.Flag.class, bitfield = true),
    @StructureMember(name = "Width", type = short.class, unsigned = true),
    @StructureMember(name = "Height", type = short.class, unsigned = true),
    @StructureMember(name = "Lines", type = short.class, unsigned = true),
    @StructureMember(name = "FontID", type = FontStyle.class)
  }
)
public interface CDButton extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Short> {
    UNUSED(RichTextConstants.BUTTON_UNUSED),
    RUNFLAG_SCRIPT(RichTextConstants.BUTTON_RUNFLAG_SCRIPT),
    RUNFLAG_NOWRAP(RichTextConstants.BUTTON_RUNFLAG_NOWRAP),
    RUNFLAG_RTL(RichTextConstants.BUTTON_RUNFLAG_RTL),
    RUNFLAG_FIXED(RichTextConstants.BUTTON_RUNFLAG_FIXED),
    RUNFLAG_MINIMUM(RichTextConstants.BUTTON_RUNFLAG_MINIMUM),
    RUNFLAG_CONTENT(RichTextConstants.BUTTON_RUNFLAG_CONTENT),
    RUNFLAG_PROPORTIONAL(RichTextConstants.BUTTON_RUNFLAG_PROPORTIONAL),
    /**  button has focus  */
    FOCUS_ON(RichTextConstants.BUTTON_FOCUS_ON),
    EDGE_ROUNDED(RichTextConstants.BUTTON_EDGE_ROUNDED),
    EDGE_SQUARE(RichTextConstants.BUTTON_EDGE_SQUARE);
    private final short value;
    private Flag(short value) {
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
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDButton setFlags(Collection<Flag> flags);
  
  @StructureGetter("Width")
  int getWidth();
  
  @StructureSetter("Width")
  CDButton setWidth(int width);
  
  @StructureGetter("Height")
  int getHeight();
  
  @StructureSetter("Height")
  CDButton setHeight(int height);
  
  @StructureGetter("Lines")
  int getLines();
  
  @StructureSetter("Lines")
  CDButton setLines(int lines);
  
  @StructureGetter("FontID")
  FontStyle getFontID();
  
  default String getText() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getVariableData().remaining()
    );
  }
  
  default CDButton setText(String text) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getVariableData().remaining(),
      text,
      (int len) -> {}
    );
  }
}
