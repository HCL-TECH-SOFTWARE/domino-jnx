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

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;
import com.hcl.domino.richtext.structures.ElementHeader;

/**
 * @author Jesse Gallagher
 * @since 1.0.345
 */
@StructureDefinition(
  name = "CDLAYOUTGRAPHIC",
  members = {
    @StructureMember(name = "Header", type = BSIG.class),
    @StructureMember(name = "ElementHeader", type = ElementHeader.class),
    @StructureMember(name = "Flags", type = CDLayoutGraphic.Flag.class, bitfield = true),
    @StructureMember(name = "Reserved", type = byte[].class, length = 16)
  }
)
public interface CDLayoutGraphic extends RichTextRecord<BSIG> {
  enum Flag implements INumberEnum<Integer> {
    BUTTON(NotesConstants.LAYOUT_GRAPHIC_FLAG_BUTTON);
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
  BSIG getHeader();
  
  @StructureGetter("ElementHeader")
  ElementHeader getElementHeader();
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDLayoutGraphic setFlags(Collection<Flag> flags);
}
