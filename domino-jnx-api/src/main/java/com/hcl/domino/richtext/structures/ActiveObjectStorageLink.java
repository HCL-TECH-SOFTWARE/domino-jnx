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

import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * Represents the {@code ACTIVEOBJECTSTORAGELINK} structure.
 * 
 * @author Jesse Gallagher
 * @since 1.0.44
 */
@StructureDefinition(
  name = "ACTIVEOBJECTSTORAGELINK",
  members = {
    @StructureMember(name = "Length", type = short.class, unsigned = true),
    @StructureMember(name = "LinkType", type = short.class),
    @StructureMember(name = "Reserved", type = int.class)
  }
)
public interface ActiveObjectStorageLink extends ResizableMemoryStructure {
  @StructureGetter("Length")
  int getLength();
  
  @StructureSetter("Length")
  ActiveObjectStorageLink setLength(int len);
  
  default String getLink() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getLength()
    );
  }
  
  default ActiveObjectStorageLink setLink(String link) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getLength(),
      link,
      this::setLength
    );
  }
}
