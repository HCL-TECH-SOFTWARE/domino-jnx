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
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;
import com.hcl.domino.richtext.structures.LengthValue;

@StructureDefinition(
    name = "CDPOSITIONING",
    members = {
      @StructureMember(name = "Header", type = BSIG.class),
      @StructureMember(name = "Scheme", type = CDPositioning.Scheme.class),
      @StructureMember(name = "bReserved", type = byte.class),
      @StructureMember(name = "ZIndex", type = int.class),
      @StructureMember(name = "Top", type = LengthValue.class),
      @StructureMember(name = "Left", type = LengthValue.class),
      @StructureMember(name = "Bottom", type = LengthValue.class),
      @StructureMember(name = "Right", type = LengthValue.class),
      @StructureMember(name = "BrowserLeftOffset", type = double.class),
      @StructureMember(name = "BrowserRightOffset", type = double.class)
    }
  )
public interface CDPositioning extends RichTextRecord<BSIG> {
  
  enum Scheme implements INumberEnum<Byte> {
    STATIC((byte)RichTextConstants.CDPOSITIONING_SCHEME_STATIC),
    ABSOLUTE((byte)RichTextConstants.CDPOSITIONING_SCHEME_ABSOLUTE),
    RELATIVE((byte)RichTextConstants.CDPOSITIONING_SCHEME_RELATIVE),
    FIXED((byte)RichTextConstants.CDPOSITIONING_SCHEME_FIXED);
    private final byte value;
    private Scheme(byte value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Byte getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  BSIG getHeader();
  
  @StructureGetter("Scheme")
  Optional<Scheme> getScheme();
  
  @StructureSetter("Scheme")
  CDPositioning setScheme(Scheme scheme);
  
  @StructureGetter("Scheme")
  byte getSchemeRaw();
  
  @StructureSetter("Scheme")
  CDPositioning setSchemeRaw(byte scheme);
  
  @StructureGetter("ZIndex")
  int getZIndex();
  
  @StructureSetter("ZIndex")
  CDPositioning setZIndex(int zindex);
  
  @StructureGetter("Top")
  LengthValue getTop();
  
  @StructureGetter("Left")
  LengthValue getLeft();
  
  @StructureGetter("Bottom")
  LengthValue getBottom();
  
  @StructureGetter("Right")
  LengthValue getRight();
  
  @StructureGetter("BrowserLeftOffset")
  double getBrowserLeftOffset();
  
  @StructureSetter("BrowserLeftOffset")
  CDPositioning setBrowserLeftOffset(double browserLeftOffset);
  
  @StructureGetter("BrowserRightOffset")
  double getBrowserRightOffset();
  
  @StructureSetter("BrowserRightOffset")
  CDPositioning setBrowserRightOffset(double browserRightOffset);

}
