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

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.LSIG;

/**
 * Rich text record of type CDMACMETASEG (editods.h
 */
@StructureDefinition(
  name = "CDMACMETASEG",
  members = {
    @StructureMember(name = "Header", type = LSIG.class),
    @StructureMember(name = "DataSize", type = short.class, unsigned = true),
    @StructureMember(name = "SegSize", type = short.class, unsigned = true)
  }
)
public interface CDMacMetaSegment extends RichTextRecord<LSIG> {
  @StructureGetter("Header")
  @Override
  LSIG getHeader();
  
  @StructureGetter("DataSize")
  int getDataSize();
  
  @StructureSetter("DataSize")
  CDMacMetaSegment setDataSize(int length);
  
  @StructureGetter("SegSize")
  int getSegSize();
  
  @StructureSetter("SegSize")
  CDMacMetaSegment setSegSize(int xExt);
}
