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
package com.hcl.domino.design.format;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;

/**
 * @author Jesse Gallagher
 * @since 1.0.32
 */
@StructureDefinition(name = "COLLATION",
  members = {
    @StructureMember(name = "BufferSize", type = short.class, unsigned = true),
    @StructureMember(name = "Items", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = Collation.Flag.class, bitfield = true),
    @StructureMember(name = "signature", type = byte.class)
  }
)
public interface Collation extends ResizableMemoryStructure {
  
  static Collation newInstance() {
    Collation c = MemoryStructureWrapperService.get().newStructure(Collation.class, 0);
    c.setSignature(NotesConstants.COLLATION_SIGNATURE);
    return c;
  }
  
  enum Flag implements INumberEnum<Byte> {
    /** Flag to indicate unique keys. */
    UNIQUE(NotesConstants.COLLATION_FLAG_UNIQUE),
    /** Flag to indicate only build demand. */
    BUILD_ON_DEMAND(NotesConstants.COLLATION_FLAG_BUILD_ON_DEMAND);

    private final byte value;

    Flag(final byte value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Byte getValue() {
      return this.value;
    }
  }
  
  @StructureGetter("BufferSize")
  int getBufferSize();
  
  @StructureSetter("BufferSize")
  Collation setBufferSize(int bufferSize);
  
  @StructureGetter("Items")
  int getItems();
  
  @StructureSetter("Items")
  Collation setItems(int items);

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureSetter("Flags")
  Collation setFlags(Collection<Flag> flags);

  default Collation setFlag(Flag flag, boolean b) {
    Set<Flag> oldFlags = getFlags();
    if (b) {
      if (!oldFlags.contains(flag)) {
        Set<Flag> newFlags = new HashSet<>(oldFlags);
        newFlags.add(flag);
        setFlags(newFlags);
      }
    }
    else {
      if (oldFlags.contains(flag)) {
        Set<Flag> newFlags = oldFlags
            .stream()
            .filter(currFlag -> !flag.equals(currFlag))
            .collect(Collectors.toSet());
        setFlags(newFlags);
      }
    }
    return this;
  }

  @StructureGetter("signature")
  byte getSignature();

  @StructureSetter("signature")
  Collation setSignature(byte signature);
}
