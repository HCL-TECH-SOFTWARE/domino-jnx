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

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;

/**
 * Rich text record of type CDVERTICALALIGN
 */
@StructureDefinition(name = "CDVERTICALALIGN", members = {
    @StructureMember(name = "Header", type = BSIG.class),
    @StructureMember(name = "Alignment", type = CDVerticalAlign.Alignment.class)
})
public interface CDVerticalAlign extends RichTextRecord<BSIG> {
  enum Alignment implements INumberEnum<Short> {
    BASELINE((short)RichTextConstants.VERTICAL_ALIGNMENT_BASELINE),
    CENTER((short)RichTextConstants.VERTICAL_ALIGNMENT_CENTER),
    TOP((short)RichTextConstants.VERTICAL_ALIGNMENT_TOP),
    BOTTOM((short)RichTextConstants.VERTICAL_ALIGNMENT_BOTTOM);
    private final short value;
    private Alignment(short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Short getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  BSIG getHeader();

  /**
   * Returns the vertical alignment
   *
   * @return Alignment
   */
  @StructureGetter("Alignment")
  Alignment getAlignment();
  
  @StructureSetter("Alignment")
  CDVerticalAlign setAlignment(Alignment alignment);
  
  @StructureGetter("Alignment")
  short getAlignmentRaw();
  
  @StructureSetter("Alignment")
  CDVerticalAlign setAlignmentRaw(short alignment);

}
