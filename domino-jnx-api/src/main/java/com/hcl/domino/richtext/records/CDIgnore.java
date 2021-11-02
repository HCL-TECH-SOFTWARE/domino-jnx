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
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;

/**
 * CDIGNORE
 * 
 * @author 
 * @since 1.0.46
 */

@StructureDefinition(
  name = "CDIGNORE", 
  members = { 
    @StructureMember(name = "Header", type = BSIG.class),                        
    @StructureMember(name = "wNotesVersion", type = short.class, unsigned = true), 				/* Version of Notes */
    @StructureMember(name = "dwFlags", type = CDIgnore.Flag.class,  bitfield = true),                        					/* See FLAG_CDIGNORE_  */
    @StructureMember(name = "dwUnused", type = int[].class, length = 6),         				/* Reserved for future use. Should be zeroed out. */
})
public interface CDIgnore extends RichTextRecord<BSIG> {
  public enum Flag implements INumberEnum<Integer> {
    CDIGNORE_BEGIN(RichTextConstants.FLAG_CDIGNORE_BEGIN), 
    CDIGNORE_END(RichTextConstants.FLAG_CDIGNORE_END);
    
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
  
  static int CDIGNORE_NOTES_VERSION_6_0_0 = 1; /* 6.0.0 */
  static int CDIGNORE_NOTES_VERSION_CURRENT = 1; /* 6.0.0 */

  @StructureGetter("Header")
  BSIG getHeader();

  @StructureGetter("wNotesVersion")
  int getNotesVersion();

  @StructureGetter("dwFlags")
  int getFlagsRaw();
  
  @StructureGetter("dwFlags")
  Set<Flag> getFlags();

  @StructureSetter("wNotesVersion")
  CDIgnore setNotesVersion(int wNotesVersion);

  @StructureSetter("dwFlags")
  CDIgnore setFlagsRaw(int dwFlags);
  
  @StructureSetter("dwFlags")
  CDIgnore setFlags(Collection<Flag> dwFlags);

}
