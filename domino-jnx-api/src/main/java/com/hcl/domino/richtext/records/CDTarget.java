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
 * @author Jesse Gallagher
 * @since 1.0.32
 */
@StructureDefinition(
  name = "CDTARGET",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "TargetLength", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = CDTarget.Flag.class, bitfield = true),
    @StructureMember(name = "Reserved", type = int.class)
  }
)
public interface CDTarget extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Short> {
    IS_FORMULA(RichTextConstants.FLAG_TARGET_IS_FORMULA);
    
    private final short value;
    private Flag(short value) {
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
  WSIG getHeader();

  @StructureGetter("TargetLength")
  int getTargetLength();
  
  @StructureSetter("TargetLength")
  CDTarget setTargetLength(int length);
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDTarget setFlags(Collection<Flag> flags);
  
  default String getTargetString() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getTargetLength()
    );
  }
  
  default CDTarget setTargetString(String target) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getTargetLength(),
      target,
      this::setTargetLength
    );
  }
  
  default String getTargetFormula() {
    return StructureSupport.extractCompiledFormula(
        this,
        0,
        getTargetLength()
      );
  }
  
  default CDTarget setTargetFormula(String target) {
    return StructureSupport.writeCompiledFormula(
      this,
      0,
      getTargetLength(),
      target,
      this::setTargetLength
    );
  }
}
