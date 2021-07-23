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
package com.hcl.domino.richtext.structures;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;

@StructureDefinition(name = "RFC822ITEMDESC", members = {
    @StructureMember(name = "wVersion", type = short.class, unsigned = true),
    @StructureMember(name = "dwFlags", type = int.class),
    @StructureMember(name = "wNotesNativeLen", type = short.class, unsigned = true),
    @StructureMember(name = "w822NameLen", type = short.class, unsigned = true),
    @StructureMember(name = "w822DelimLen", type = short.class, unsigned = true),
    @StructureMember(name = "w822BodyLen", type = short.class, unsigned = true)
})
public interface RFC822ItemDesc extends MemoryStructure {
  @StructureGetter("w822BodyLen")
  int getBodyLength();

  @StructureGetter("w822DelimLen")
  int getDelimiterLength();

  @StructureGetter("dwFlags")
  int getFlags();

  @StructureGetter("w822NameLen")
  int getNameLength();

  @StructureGetter("wNotesNativeLen")
  int getNotesNativeLength();

  @StructureGetter("wVersion")
  int getVersion();
}
