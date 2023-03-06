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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.design.format.LengthUnit;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(name = "LENGTH_VALUE", members = {
    @StructureMember(name = "Flags", type = LengthValue.Flag.class, bitfield = true),
    @StructureMember(name = "Length", type = double.class),
    @StructureMember(name = "Units", type = LengthUnit.class),
    @StructureMember(name = "Reserved", type = byte.class)
})
public interface LengthValue extends MemoryStructure {
  enum Flag implements INumberEnum<Short> {
    AUTO(0x0001),
    INHERIT(0x0002);

    private final short value;

    Flag(final int value) {
      this.value = (short) value;
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

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureGetter("Length")
  double getLength();

  @StructureGetter("Units")
  Optional<LengthUnit> getUnit();
  
  /**
   * Retrieves the unit value as a raw {@code byte}.
   * 
   * @return the unit as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("Units")
  byte getUnitRaw();

  @StructureSetter("Flags")
  LengthValue setFlags(Collection<Flag> flags);

  @StructureSetter("Length")
  LengthValue setLength(double length);

  @StructureSetter("Units")
  LengthValue setUnit(LengthUnit unit);
  
  /**
   * Sets the type unit as a raw {@code byte}.
   * 
   * @param unit the unit to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("Units")
  LengthValue setUnitRaw(byte unit);
}
