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

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDCELLBACKGROUNDDATA",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Repeat", type = CDCellBackgroundData.Repeat.class),
    @StructureMember(name = "Spare", type = byte.class),
    @StructureMember(name = "SpareDWORD", type = int.class)
  }
)
public interface CDCellBackgroundData extends RichTextRecord<WSIG> {
  enum Repeat implements INumberEnum<Byte> {
    UNKNOWN(RichTextConstants.REPEAT_UNKNOWN),
    ONCE(RichTextConstants.REPEAT_ONCE),
    VERT(RichTextConstants.REPEAT_VERT),
    HORIZ(RichTextConstants.REPEAT_HORIZ),
    BOTH(RichTextConstants.REPEAT_BOTH),
    SIZE(RichTextConstants.REPEAT_SIZE),
    CENTER(RichTextConstants.REPEAT_CENTER);
    private final byte value;
    private Repeat(byte value) {
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
  WSIG getHeader();
  
  @StructureGetter("Repeat")
  Repeat getRepeat();
  
  @StructureSetter("Repeat")
  CDCellBackgroundData setRepeat(Repeat repeat);
}
