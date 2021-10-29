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

/**
 * CDPMMETASEG
 * 
 * @author 
 * @since 1.0.46
 */

@StructureDefinition(
  name = "CDPMMETASEG", 
  members = { 
    @StructureMember(name = "Header", type = LSIG.class),                        /* Signature and Length */
    @StructureMember(name = "DataSize", type = short.class, unsigned = true),    /* Actual Size of metafile bits in bytes, ignoring any filler */
    @StructureMember(name = "SegSize", type = short.class, unsigned = true),     /* Size of segment, is equal to or larger than DataSize if filler byte added to maintain word boundary */
FIX ME >>> /* PM Metafile Bits for this segment. Must be <= 64K bytes. */
})
public interface CDPmMetaSeg extends RichTextRecord<LSIG> {

  @StructureGetter("Header")
  LSIG getHeader();

  @StructureGetter("DataSize")
  int getDataSize();

  @StructureGetter("SegSize")
  int getSegSize();

  @StructureSetter("DataSize")
  CDPmMetaSeg setDataSize(int dataSize);

  @StructureSetter("SegSize")
  CDPmMetaSeg setSegSize(int segSize);

}
