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

import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * Rich text record of type CDEMBEDEXTRAINFO
 */
@StructureDefinition(name = "CDEMBEDEXTRAINFO", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "NameLength", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = int.class),
    @StructureMember(name = "Reserved", type = int[].class, length = 5)
})
public interface CDEmbeddedExtraInfo extends RichTextRecord<WSIG> {

  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("NameLength")
  int getNameLength();
  
  @StructureSetter("NameLength")
  CDEmbeddedExtraInfo setNameLength(int nameLength);
  
  default String getName() {
    return StructureSupport.extractStringValue(
        this,
        0,
        this.getNameLength()
        );
  }

  default CDEmbeddedExtraInfo setName(final String name) {
    return StructureSupport.writeStringValue(
        this,
        0,
        this.getNameLength(),
        name,
        this::setNameLength
        );
  }
}
