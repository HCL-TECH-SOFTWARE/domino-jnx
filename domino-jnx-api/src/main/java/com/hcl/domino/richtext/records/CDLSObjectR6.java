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

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * CDLSOBJECT_R6
 * 
 * @author pbugga
 * @since 1.0.46
 */

@StructureDefinition(
  name = "CDLSOBJECT_R6", 
  members = { 
    @StructureMember(name = "Header", type = WSIG.class),                        
    @StructureMember(name = "Flags", type = byte.class),                         
    @StructureMember(name = "Reserved", type = byte[].class, length = 7),        
})
public interface CDLSObjectR6 extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Byte> {
    LSOBJECT_R6_TYPE((byte)RichTextConstants.CDLSOBJECT_R6_TYPE); /* signals multiple code segments for R6 >64k */
    
    private final byte value;

    Flag(final byte value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Byte getValue() {
      return this.value;
    }
  }

  @Override
  @StructureGetter("Header")
  WSIG getHeader();

  @StructureGetter("Flags")
  byte getFlagsRaw();
  
  @StructureGetter("Flags")
  Flag getFlags();

  @StructureSetter("Flags")
  CDLSObjectR6 setFlagsRaw(byte flags);
  
  @StructureSetter("Flags")
  CDLSObjectR6 setFlags(Flag flags);

}
