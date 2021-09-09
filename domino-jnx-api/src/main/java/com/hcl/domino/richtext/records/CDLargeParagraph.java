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
 * Rich text record of type CDLARGEPARAGRAPH
 */
@StructureDefinition(name = "CDLARGEPARAGRAPH", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Version", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = CDLargeParagraph.Flag.class),
    @StructureMember(name = "Spare", type = int[].class, length = 2)
})
public interface CDLargeParagraph extends RichTextRecord<WSIG> {
  
  enum Flag implements INumberEnum<Short> {
    CDLARGEPARAGRAPH_BEGIN((short)RichTextConstants.CDLARGEPARAGRAPH_BEGIN),
    CDLARGEPARAGRAPH_END((short)RichTextConstants.CDLARGEPARAGRAPH_END);
    
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
  
  @StructureGetter("Version")
  int getVersion();
  
  @StructureSetter("Version")
  CDLargeParagraph setVersion(int version);
  
  @StructureGetter("Flags")
  Flag getFlags();
  
  @StructureSetter("Flags")
  CDLargeParagraph setFlags(Flag flags);
  
  @StructureGetter("Flags")
  short getFlagsRaw();
  
  @StructureSetter("Flags")
  CDLargeParagraph setFlagsRaw(short flags);
  
  @StructureGetter("Spare")
  int[] getSpare();
  
  @StructureSetter("Spare")
  CDLargeParagraph setSpare(int[] spare);
}
