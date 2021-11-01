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

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * CDLSOBJECT_R6
 * 
 * @author 
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
  static int CDLSOBJECT_R6_TYPE = 01; /* signals multiple code segments for R6 >64k */

  @StructureGetter("Header")
  WSIG getHeader();

  @StructureGetter("Flags")
  byte getFlags();

  @StructureSetter("Flags")
  CDLSObjectR6 setFlags(byte flags);

}
