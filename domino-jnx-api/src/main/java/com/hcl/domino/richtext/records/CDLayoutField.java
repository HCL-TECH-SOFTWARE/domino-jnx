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
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;
import com.hcl.domino.richtext.structures.ElementHeader;

/**
 * @author Jesse Gallagher
 * @since 1.0.45
 */
@StructureDefinition(
  name = "CDLAYOUTFIELD",
  members = {
    @StructureMember(name = "Header", type = BSIG.class),
    @StructureMember(name = "ElementHeader", type = ElementHeader.class),
    @StructureMember(name = "Flags", type = CDLayoutField.Flag.class, bitfield = true),
    @StructureMember(name = "bFieldType", type = CDLayoutField.Type.class),
    @StructureMember(name = "Reserved", type = byte[].class, length = 15)
  }
)
public interface CDLayoutField extends RichTextRecord<BSIG> {
  enum Flag implements INumberEnum<Integer> {
    SINGLELINE(NotesConstants.LAYOUT_FIELD_FLAG_SINGLELINE),
    VSCROLL(NotesConstants.LAYOUT_FIELD_FLAG_VSCROLL),
    MULTISEL(NotesConstants.LAYOUT_FIELD_FLAG_MULTISEL),
    STATIC(NotesConstants.LAYOUT_FIELD_FLAG_STATIC),
    NOBORDER(NotesConstants.LAYOUT_FIELD_FLAG_NOBORDER),
    IMAGE(NotesConstants.LAYOUT_FIELD_FLAG_IMAGE),
    LTR(NotesConstants.LAYOUT_FIELD_FLAG_LTR),
    RTL(NotesConstants.LAYOUT_FIELD_FLAG_RTL),
    TRANS(NotesConstants.LAYOUT_FIELD_FLAG_TRANS),
    LEFT(NotesConstants.LAYOUT_FIELD_FLAG_LEFT),
    CENTER(NotesConstants.LAYOUT_FIELD_FLAG_CENTER),
    RIGHT(NotesConstants.LAYOUT_FIELD_FLAG_RIGHT),
    VCENTER(NotesConstants.LAYOUT_FIELD_FLAG_VCENTER);
    
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
  enum Type implements INumberEnum<Byte> {
    TEXT(NotesConstants.LAYOUT_FIELD_TYPE_TEXT),
    CHECK(NotesConstants.LAYOUT_FIELD_TYPE_CHECK),
    RADIO(NotesConstants.LAYOUT_FIELD_TYPE_RADIO),
    LIST(NotesConstants.LAYOUT_FIELD_TYPE_LIST),
    COMBO(NotesConstants.LAYOUT_FIELD_TYPE_COMBO);
    
    private final byte value;
    private Type(byte value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Byte getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  BSIG getHeader();
  
  @StructureGetter("ElementHeader")
  ElementHeader getElementHeader();
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDLayoutField setFlags(Collection<Flag> flags);
  
  @StructureGetter("bFieldType")
  Optional<Type> getFieldType();
  
  @StructureSetter("bFieldType")
  CDLayoutField setFieldType(Type type);
}
