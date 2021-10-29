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
 * CDBACKGROUNDPROPERTIES
 * 
 * @author 
 * @since 1.0.46
 */

@StructureDefinition(
  name = "CDBACKGROUNDPROPERTIES", 
  members = { 
    @StructureMember(name = "Header", type = BSIG.class),                        
    @StructureMember(name = "Repeat", type = byte.class),                        
    @StructureMember(name = "bReserved", type = byte.class),                     
    @StructureMember(name = "lvReservedX", type = LENGTH_VALUE.class),           
    @StructureMember(name = "lvReservedY", type = LENGTH_VALUE.class),           
    @StructureMember(name = "dwReserved", type = int[].class, length = 4),       
})
public interface CDBackgroundProperties extends RichTextRecord<BSIG> {
  static int REPEAT_UNKNOWN = 0; 
  static int REPEAT_ONCE = 1; 
  static int REPEAT_VERT = 2; 
  static int REPEAT_HORIZ = 3; 
  static int REPEAT_BOTH = 4; 
  static int REPEAT_SIZE = 5; 
  static int REPEAT_CENTER = 6; 

  @StructureGetter("Header")
  BSIG getHeader();

  @StructureGetter("Repeat")
  byte getRepeat();

  @StructureGetter("bReserved")
  byte getbReserved();

  @StructureGetter("lvReservedX")
  LENGTH_VALUE getlvReservedX();

  @StructureGetter("lvReservedY")
  LENGTH_VALUE getlvReservedY();

  @StructureSetter("Repeat")
  CDBackgroundProperties setRepeat(byte repeat);

  @StructureSetter("bReserved")
  CDBackgroundProperties setbReserved(byte bReserved);

}
