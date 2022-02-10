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
package com.hcl.domino.richtext.structures;

import java.util.Optional;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * @author Jesse Gallagher
 * @since 1.0.38
 */
@StructureDefinition(
  name = "OBJECT_DESCRIPTOR",
  members = {
    @StructureMember(name = "ObjectType", type = ObjectDescriptor.ObjectType.class),
    @StructureMember(name = "RRV", type = int.class)
  }
)
public interface ObjectDescriptor extends ResizableMemoryStructure {
  enum ObjectType implements INumberEnum<Short> {
    FILE(NotesConstants.OBJECT_FILE),
    FILTER_LEFTTODO(NotesConstants.OBJECT_FILTER_LEFTTODO),
    ASSIST_RUNDATA(NotesConstants.OBJECT_ASSIST_RUNDATA),
    UNKNOWN(NotesConstants.OBJECT_UNKNOWN);
    private final short value;
    
    private ObjectType(short value) {
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
  
  @StructureGetter("ObjectType")
  Optional<ObjectType> getObjectType();
  
  @StructureSetter("ObjectType")
  ObjectDescriptor setObjectType(ObjectType objectType);
  
  @StructureGetter("RRV")
  int getRRV();
  
  @StructureSetter("RRV")
  ObjectDescriptor setRRV(int rrv);
}
