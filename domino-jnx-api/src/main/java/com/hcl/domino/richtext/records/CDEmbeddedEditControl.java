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

import java.util.Collection;
import java.util.Set;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * Rich text record of type CDEMBEDDEDEDITCTL
 */
@StructureDefinition(name = "CDEMBEDDEDEDITCTL", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDEmbeddedEditControl.Flag.class, bitfield = true),
    @StructureMember(name = "NameLength", type = short.class, unsigned = true),
    @StructureMember(name = "SpareWORD", type = short[].class, length = 5),
    @StructureMember(name = "SpareDWORD", type = int[].class, length = 11)
})
public interface CDEmbeddedEditControl extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    HASNAME(RichTextConstants.EMBEDDEDEDITCTL_FLAG_HASNAME),
    HIDE_ACTIONBAR(RichTextConstants.EMBEDDEDEDITCTL_FLAG_HIDE_ACTIONBAR);
    private final int value;
    private Flag(int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Integer getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDEmbeddedEditControl setFlags(Collection<Flag> flags);
  
  @StructureGetter("NameLength")
  int getNameLength();
  
  @StructureSetter("NameLength")
  CDEmbeddedEditControl setNameLength(int length);
  
  /**
   * Gets the text for Name in the variable data portion of this record.
   *
   * @return this record
   */
  default String getName() {
    return StructureSupport.extractStringValue(
        this,
        0,
        this.getNameLength()
        );
  }
  
  /**
   * Stores the text for Name in the variable data portion of this record.
   * <p>
   * The buffer will be resized, if necessary, to hold the text value.
   * </p>
   * <p>
   * This method also sets the {@code NameLength} property to the appropriate value.
   * </p>
   *
   * @param Name name for embedded edit control
   * @return this record
   */
  default CDEmbeddedEditControl setName(final String name) {
    return StructureSupport.writeStringValue(
        this,
        0,
        this.getNameLength(),
        name,
        this::setNameLength
      );
  }
}
