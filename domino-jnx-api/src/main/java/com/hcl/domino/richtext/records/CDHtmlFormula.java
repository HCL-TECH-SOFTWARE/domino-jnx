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
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.44
 */
@StructureDefinition(
  name = "CDHTMLFORMULA",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "dwFlags", type = CDHtmlFormula.Flag.class, bitfield = true),
    @StructureMember(name = "cbLevel", type = byte.class),
    @StructureMember(name = "cdReserved", type = byte.class),
    @StructureMember(name = "Reserved", type = short.class),
  }
)
public interface CDHtmlFormula extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /** formula is HTML attributes */
    ATTR(NotesConstants.CDHTMLFORMULA_FLAG_ATTR),
    /** formula is alternate HTML */
    ALT(NotesConstants.CDHTMLFORMULA_FLAG_ALT),
    /** formula is an action label formula. */
    ACTION_LABEL(NotesConstants.CDHTMLFORMULA_FLAG_ACTION_LABEL);
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
  WSIG getHeader();

  @StructureGetter("dwFlags")
  Set<Flag> getFlags();
  
  @StructureSetter("dwFlags")
  CDHtmlFormula setFlags(Collection<Flag> flags);
  
  default String getFormula() {
    return StructureSupport.extractCompiledFormula(
      this,
      0,
      getVariableData().remaining()
    );
  }
  
  default CDHtmlFormula setFormula(String formula) {
    return StructureSupport.writeCompiledFormula(
      this,
      0,
      getVariableData().remaining(),
      formula,
      (int len) -> {}
    );
  }
}
