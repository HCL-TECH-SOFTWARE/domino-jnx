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
 * CDINLINE
 * 
 * @author 
 * @since 1.0.46
 */

@StructureDefinition(
  name = "CDINLINE", 
  members = { 
    @StructureMember(name = "Header", type = WSIG.class),                        
    @StructureMember(name = "wDatalength", type = short.class, unsigned = true), 
    @StructureMember(name = "dwFlags", type = int.class),                        
    @StructureMember(name = "dwReserved", type = int[].class, length = 4),       
})
public interface CDInline extends RichTextRecord<WSIG> {
  static int INLINE_FLAG_UNKNOWN = 0x00000000; 
  static int INLINE_FLAG_SCRIPT_LIB = 0x00000001; 
  static int INLINE_FLAG_STYLE_SHEET = 0x00000002; 
  static int INLINE_FLAG_HTML = 0x00000004; 
  static int INLINE_FLAG_HTMLFILERES = 0x00000008; 
  static int INLINE_FLAG_TYPES_MASK = 0x0000000F; 
  static int INLINE_VERSION1 = 1; 

  @StructureGetter("Header")
  WSIG getHeader();

  @StructureGetter("wDatalength")
  int getwDatalength();

  @StructureGetter("dwFlags")
  int getdwFlags();

  @StructureSetter("wDatalength")
  CDInline setwDatalength(int wDatalength);

  @StructureSetter("dwFlags")
  CDInline setdwFlags(int dwFlags);

}
