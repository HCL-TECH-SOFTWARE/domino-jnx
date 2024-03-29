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

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

@StructureDefinition(name = "BSIG", members = {
    @StructureMember(name = "Signature", type = byte.class),
    @StructureMember(name = "Length", type = byte.class, unsigned = true)
})
public interface BSIG extends CDSignature<Byte, Short, BSIG> {
  @StructureGetter("Length")
  @Override
  Short getLength();

  @StructureGetter("Signature")
  @Override
  Byte getSignature();

  @StructureSetter("Length")
  @Override
  BSIG setLength(Short length);

  @StructureSetter("Signature")
  @Override
  BSIG setSignature(Byte signature);
}
