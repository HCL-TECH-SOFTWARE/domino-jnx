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
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.WSIG;

@StructureDefinition(name = "CDEMBEDDEDCALCTL", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDEmbeddedCalendarControl.Flag.class, bitfield = true),
    @StructureMember(name = "HeaderBkgnd", type = ColorValue.class),
    @StructureMember(name = "SelectionColor", type = ColorValue.class),
    @StructureMember(name = "TargetFrameLength", type = short.class, unsigned = true),
    @StructureMember(name = "Spare", type = int[].class, length = 10)
})
public interface CDEmbeddedCalendarControl extends RichTextRecord<WSIG> {

  enum Flag implements INumberEnum<Integer> {
    NON_TRANSPARENT_BKGND(RichTextConstants.EMBEDDEDCAL_FLAG_NON_TRANSPARENT_BKGND),
    HASTARGETFRAME(RichTextConstants.EMBEDDEDCAL_FLAG_HASTARGETFRAME);
    private final int value;
    private Flag(int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Integer getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDEmbeddedCalendarControl setFlags(Collection<Flag> flags);
  
  @StructureGetter("HeaderBkgnd")
  ColorValue getHeaderBkgnd();

  @StructureGetter("SelectionColor")
  ColorValue getSelectionColor();

  @StructureGetter("TargetFrameLength")
  int getTargetFrameLength();
  
  @StructureSetter("TargetFrameLength")
  CDEmbeddedCalendarControl setTargetFrameLength(int targetFrameLength);
  
  default String getTargetFrameName() {
    return StructureSupport.extractStringValue(this, 
        0, 
        this.getTargetFrameLength());
  }
  
  default CDEmbeddedCalendarControl setTargetFrameName(String name) {
    return StructureSupport.writeStringValue(this, 
        0, 
        this.getTargetFrameLength(), 
        name, 
        this::setTargetFrameLength);
  }
}
