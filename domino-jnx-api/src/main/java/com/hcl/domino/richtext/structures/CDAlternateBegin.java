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
 * CDALTERNATEBEGIN
 * 
 * @author 
 * @since 1.0.46
 */

@StructureDefinition(
  name = "CDALTERNATEBEGIN", 
  members = { 
    @StructureMember(name = "Header", type = WSIG.class),                        /* Signature and length of this record */
    @StructureMember(name = "Type", type = short.class, unsigned = true),        /* Unused at this time */
    @StructureMember(name = "SequenceNumber", type = int.class),                 /* ID/Sequence number should match what's in some ACTIVEOBJECT in the doc */
    @StructureMember(name = "Flags", type = int.class),                          /* Unused at this time */
    @StructureMember(name = "DataLength", type = short.class, unsigned = true),  /* Unused at this time */
FIX ME >>> /*	Data Follows. */
})
public interface CDAlternateBegin extends RichTextRecord<WSIG> {

  @StructureGetter("Header")
  WSIG getHeader();

  @StructureGetter("Type")
  int getType();

  @StructureGetter("SequenceNumber")
  int getSequenceNumber();

  @StructureGetter("Flags")
  int getFlags();

  @StructureGetter("DataLength")
  int getDataLength();

  @StructureSetter("Type")
  CDAlternateBegin setType(int type);

  @StructureSetter("SequenceNumber")
  CDAlternateBegin setSequenceNumber(int sequenceNumber);

  @StructureSetter("Flags")
  CDAlternateBegin setFlags(int flags);

  @StructureSetter("DataLength")
  CDAlternateBegin setDataLength(int dataLength);

}
