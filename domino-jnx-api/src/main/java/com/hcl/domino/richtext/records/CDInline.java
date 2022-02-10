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
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * CDINLINE
 * 
 * @author pbugga
 * @since 1.0.46
 */

@StructureDefinition(
  name = "CDINLINE", 
  members = { 
    @StructureMember(name = "Header", type = WSIG.class),                        
    @StructureMember(name = "wDatalength", type = short.class, unsigned = true), 
    @StructureMember(name = "dwFlags", type = CDInline.Flag.class, bitfield = true),                        
    @StructureMember(name = "dwReserved", type = int[].class, length = 4),       
})
public interface CDInline extends RichTextRecord<WSIG> {
  public enum Flag implements INumberEnum<Integer> {
    SCRIPT_LIB(RichTextConstants.INLINE_FLAG_SCRIPT_LIB), 
    STYLE_SHEET(RichTextConstants.INLINE_FLAG_STYLE_SHEET), 
    HTML(RichTextConstants.INLINE_FLAG_HTML), 
    HTMLFILERES(RichTextConstants.INLINE_FLAG_HTMLFILERES), 
    TYPES_MASK(RichTextConstants.INLINE_FLAG_TYPES_MASK);
    
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
  static int INLINE_VERSION1 = 1; 

  @Override
  @StructureGetter("Header")
  WSIG getHeader();

  @StructureGetter("wDatalength")
  int getDatalength();

  @StructureGetter("dwFlags")
  int getFlagsRaw();
  
  @StructureGetter("dwFlags")
  Set<Flag> getFlags();

  @StructureSetter("wDatalength")
  CDInline setDatalength(int wDatalength);

  @StructureSetter("dwFlags")
  CDInline setFlagsRaw(int dwFlags);
  
  @StructureSetter("dwFlags")
  CDInline setFlags(Collection<Flag> dwFlags);

}
