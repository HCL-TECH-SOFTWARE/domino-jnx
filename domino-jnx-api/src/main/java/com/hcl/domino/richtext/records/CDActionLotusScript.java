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
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "CDACTIONLOTUSSCRIPT", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "dwFlags", type = int.class),
    @StructureMember(name = "dwScriptLen", type = int.class, unsigned = true)
})
public interface CDActionLotusScript extends RichTextRecord<WSIG> {
  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  default String getScript() {
    return StructureSupport.extractStringValue(this, 0, this.getScriptLength());
  }

  @StructureGetter("dwScriptLen")
  long getScriptLength();

  default CDActionLotusScript setScript(final String script) {
    StructureSupport.writeStringValue(this, 0, this.getScriptLength(), script, (final long newVal) -> this.setScriptLength(newVal));
    return this;
  }

  @StructureSetter("dwScriptLen")
  CDActionLotusScript setScriptLength(long len);
}
