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
import com.hcl.domino.richtext.structures.BSIG;

/**
 * View Map (Navigator) Header
 * @author artcnot
 * @since 1.0.37
 */
@StructureDefinition(
  name = "VIEWMAP_HEADER_RECORD",
  members = {
    @StructureMember(name = "Header", type = BSIG.class),
    @StructureMember(name = "Version", type = short.class, unsigned = true), /* see VIEWMAP_VERSION */
    @StructureMember(name = "NameLen", type = short.class, unsigned = true) /* always 0 (reserved for future use) */
  }
)
public interface ViewmapHeaderRecord extends RichTextRecord<BSIG> {
  static int VIEWMAP_VERSION = 8;


  @StructureGetter("Header")
  @Override
  BSIG getHeader();

  @StructureGetter("Version")
  int getVersion();

  @StructureGetter("NameLen")
  int getNameLen();

  @StructureSetter("Version")
  ViewmapHeaderRecord setVersion(int version);

  @StructureSetter("NameLen")
  ViewmapHeaderRecord setNameLen(int length);

}
