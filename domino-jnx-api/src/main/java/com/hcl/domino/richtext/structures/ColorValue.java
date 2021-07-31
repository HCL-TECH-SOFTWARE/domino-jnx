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

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(name = "COLOR_VALUE", members = {
    @StructureMember(name = "Flags", type = ColorValue.Flag.class, bitfield = true),
    @StructureMember(name = "Component1", type = byte.class, unsigned = true),
    @StructureMember(name = "Component2", type = byte.class, unsigned = true),
    @StructureMember(name = "Component3", type = byte.class, unsigned = true),
    @StructureMember(name = "Component4", type = byte.class, unsigned = true)
})
public interface ColorValue extends MemoryStructure {
  enum Flag implements INumberEnum<Short> {
    ISRGB(RichTextConstants.COLOR_VALUE_FLAGS_ISRGB), /* Color space is RGB */
    NOCOLOR(RichTextConstants.COLOR_VALUE_FLAGS_NOCOLOR), /* This object has no color */
    SYSTEMCOLOR(RichTextConstants.COLOR_VALUE_FLAGS_SYSTEMCOLOR), /* Use system default color, ignore color here */
    HASGRADIENT(RichTextConstants.COLOR_VALUE_FLAGS_HASGRADIENT), /* This color has a gradient color that follows */
    RESERVED1(RichTextConstants.COLOR_VALUE_FLAGS_RESERVED1), /* reserved for user */
    RESERVED2(RichTextConstants.COLOR_VALUE_FLAGS_RESERVED2), /* reserved for user */
    RESERVED3(RichTextConstants.COLOR_VALUE_FLAGS_RESERVED3), /* reserved for user */
    RESERVED4(RichTextConstants.COLOR_VALUE_FLAGS_RESERVED4), /* reserved for user */
    ;

    private final short value;

    Flag(final short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  @StructureGetter("Component3")
  short getBlue();

  @StructureGetter("Component4")
  short getComponent4();

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureGetter("Component2")
  short getGreen();

  @StructureGetter("Component1")
  short getRed();

  @StructureSetter("Component3")
  ColorValue setBlue(short blue);

  @StructureSetter("Component4")
  ColorValue setComponent4(short component4);

  @StructureSetter("Flags")
  ColorValue setFlags(Collection<Flag> flags);

  @StructureSetter("Component2")
  ColorValue setGreen(short green);

  @StructureSetter("Component1")
  ColorValue setRed(short red);

  default String toHexString() {
    return String.format("%02X%02X%02X", this.getRed(), this.getGreen(), this.getBlue()); //$NON-NLS-1$
  }
}
