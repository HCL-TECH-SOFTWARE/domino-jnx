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
package com.hcl.domino.design.format;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

@StructureDefinition(name = "VIEW_TABLE_FORMAT5", members = {
    @StructureMember(name = "Length", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = int.class),
    @StructureMember(name = "RepeatType", type = short.class)
})
public interface ViewTableFormat5 extends MemoryStructure {
  public static ViewTableFormat5 newInstanceWithDefaults() {
    ViewTableFormat5 format5 = MemoryStructureWrapperService.get().newStructure(ViewTableFormat5.class, 0);
    
    //TODO set defaults
    
    return format5;
    
  }
  
  @StructureGetter("Length")
  int getLength();

  @StructureSetter("Length")
  ViewTableFormat5 setLength(int len);

  @StructureSetter("Flags")
  ViewTableFormat5 setFlagsRaw(int flags);

  @StructureGetter("Flags")
  int getFlagsRaw();
}
