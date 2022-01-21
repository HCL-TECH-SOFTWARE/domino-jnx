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
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;

/**
 * @author Karsten Lehmann
 * @since 1.1.2
 */
@StructureDefinition(name = "COLLATE_DESCRIPTOR", members = {
    @StructureMember(name = "Flags", type = CollateDescriptor.Flag.class, bitfield = true),
    @StructureMember(name = "Signature", type = byte.class),
    @StructureMember(name = "keytype", type = byte.class),
    @StructureMember(name = "NameOffset", type = short.class, unsigned = true),
    @StructureMember(name = "NameLength", type = short.class, unsigned = true)
    //we store the name in the var data of this structure although in the $Collation item value
    //all names are stored as packed strings after the COLLATE_DESCRIPTOR array
})
public interface CollateDescriptor extends ResizableMemoryStructure {
  
  static CollateDescriptor newInstance() {
    CollateDescriptor c = MemoryStructureWrapperService.get().newStructure(CollateDescriptor.class, 0);
    c.setSignature(NotesConstants.COLLATE_DESCRIPTOR_SIGNATURE);
    return c;
  }
  
//  typedef struct {
//    BYTE Flags;
//    BYTE signature;  /* Must be COLLATE_DESCRIPTOR_SIGNATURE */
//    BYTE keytype;    /* Type of key (COLLATE_TYPE_xxx) */
//    WORD NameOffset; /* Offset to the name string */
//                     /* (relative to text area of buffer) */
//    WORD NameLength; /* Length of the name string */
// } COLLATE_DESCRIPTOR;

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureSetter("Flags")
  CollateDescriptor setFlags(Collection<Flag> flags);

  default CollateDescriptor setFlag(Flag flag, boolean b) {
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

  @StructureGetter("Signature")
  byte getSignature();

  @StructureSetter("Signature")
  CollateDescriptor setSignature(byte sig);

  @StructureGetter("keytype")
  CollateType getKeyType();
  
  @StructureSetter("keytype")
  CollateDescriptor setKeyType(CollateType type);
  
  @StructureGetter("NameOffset")
  int getNameOffset();

  @StructureSetter("NameOffset")
  CollateDescriptor setNameOffset(int offset);
  
  @StructureGetter("NameLength")
  int getNameLength();
  
  @StructureSetter("NameLength")
  CollateDescriptor setNameLength(int len);
  
  default String getName() {
    return StructureSupport.extractStringValue(this,
        0,
        this.getNameLength());
  }

  default CollateDescriptor setName(final String name) {
    return StructureSupport.writeStringValue(
        this,
        0,
        this.getNameLength(),
        name,
        this::setNameLength);
  }

  enum Flag implements INumberEnum<Byte> {
    
    /** False if ascending order (default) */
    Descending(NotesConstants.CDF_M_descending),

    /** Obsolete - see new constant below */
    CaseSensitive(NotesConstants.CDF_M_caseinsensitive),
    
    /** If prefix list, then ignore for sorting */
    IgnorePrefixes(NotesConstants.CDF_M_ignoreprefixes),

    /** Obsolete - see new constant below */
    AccentInsensitive(NotesConstants.CDF_M_accentinsensitive),

    /** If set, lists are permuted */
    Permuted(NotesConstants.CDF_M_permuted),
    
    /**
     * Qualifier if lists are permuted; if set, lists are pairwise permuted,
     * otherwise lists are multiply permuted.
     */
    PermutedPairwise(NotesConstants.CDF_M_permuted_pairwise),
    
    /** If set, treat as permuted */
    FlatInV5(NotesConstants.CDF_M_flat_in_v5),

    /** If set, text compares are case-sensitive */
    CaseSensitiveInV5(NotesConstants.CDF_M_casesensitive_in_v5),
    
    /** If set, text compares are accent-sensitive */
    AccentSensitiveInV5(NotesConstants.CDF_M_accentsensitive_in_v5);

    private final byte value;

    Flag(final int value) {
      this.value = (byte) value;
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

  /**
   * These are the possible values for the keytype member of the
   * COLLATE_DESCRIPTOR data structure.<br>
   * The keytype structure member specifies the type of sorting that is done in
   * the specified column in a view.
   */
  enum CollateType implements INumberEnum<Byte> {

    /** Collate by key in summary buffer (requires key name string) */
    KEY(NotesConstants.COLLATE_TYPE_KEY),
    /** Collate by note ID */
    NOTEID(NotesConstants.COLLATE_TYPE_NOTEID),
    /** Collate by "tumbler" summary key (requires key name string) */
    TUMBLER(NotesConstants.COLLATE_TYPE_TUMBLER),
    /** Collate by "category" summary key (requires key name string) */
    CATEGORY(NotesConstants.COLLATE_TYPE_CATEGORY);
    
    private final byte value;

    CollateType(final int value) {
      this.value = (byte) value;
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
  
}
