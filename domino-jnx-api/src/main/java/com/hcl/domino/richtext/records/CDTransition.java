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

import java.util.Optional;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
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
  name = "CDTRANSITION",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Type", type = CDTransition.Type.class),
    @StructureMember(name = "Reserved", type = short[].class, length = 4)
  }
)
public interface CDTransition extends RichTextRecord<WSIG> {
  public enum Type implements INumberEnum<Short> {
    LEFTTORIGHT_COLUMN(NotesConstants.TRANS_LEFTTORIGHT_COLUMN),
    ROLLING(NotesConstants.TRANS_ROLLING),
    TOPTOBOTTOM_ROW(NotesConstants.TRANS_TOPTOBOTTOM_ROW),
    WIPE(NotesConstants.TRANS_WIPE),
    BOXES_INCREMENT(NotesConstants.TRANS_BOXES_INCREMENT),
    EXPLODE(NotesConstants.TRANS_EXPLODE),
    DISSOLVE(NotesConstants.TRANS_DISSOLVE);
    
    private final short value;
    private Type(short value) {
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

  @StructureGetter("Type")
  Optional<Type> getTransitionType();

  @StructureSetter("Type")
  CDTransition setTransitionType(Type type);
}
