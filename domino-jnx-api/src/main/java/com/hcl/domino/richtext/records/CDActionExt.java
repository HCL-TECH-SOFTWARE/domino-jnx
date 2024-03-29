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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.design.format.ActionBarControlType;
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
  name = "CDACTIONEXT",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "dwFlags", type = CDActionExt.Flag.class, bitfield = true),
    @StructureMember(name = "wControlType", type = ActionBarControlType.class),
    @StructureMember(name = "wControlFormulaLen", type = short.class, unsigned = true),
    @StructureMember(name = "wLabelFormulaLen", type = short.class, unsigned = true),
    @StructureMember(name = "wParentLabelFormulaLen", type = short.class, unsigned = true),
    @StructureMember(name = "wCompActionIDLen", type = short.class, unsigned = true),
    @StructureMember(name = "wProgrammaticUseTxtLen", type = short.class, unsigned = true),
    @StructureMember(name = "dwExtra", type = int[].class, length = 2),
  }
)
public interface CDActionExt extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    INCLUDE_IN_SWIPE_LEFT(RichTextConstants.ACTIONEXT_INCLUDE_IN_SWIPE_LEFT),
    INCLUDE_IN_SWIPE_RIGHT(RichTextConstants.ACTIONEXT_INCLUDE_IN_SWIPE_RIGHT);
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
  
  @StructureGetter("dwFlags")
  Set<Flag> getFlags();
  
  @StructureSetter("dwFlags")
  CDActionExt setFlags(Collection<Flag> flags);
  
  @StructureGetter("wControlType")
  Optional<ActionBarControlType> getControlType();

  /**
   * Retrieves the control type as a raw {@code short}.
   * 
   * @return the control type as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("wControlType")
  short getControlTypeRaw();
  
  @StructureSetter("wControlType")
  CDActionExt setControlType(ActionBarControlType type);
  
  /**
   * Sets the control type as a raw {@code short}.
   * 
   * @param type the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("wControlType")
  CDActionExt setControlTypeRaw(short type);
  
  @StructureGetter("wControlFormulaLen")
  int getControlFormulaLen();
  
  @StructureSetter("wControlFormulaLen")
  CDActionExt setControlFormulaLen(int len);
  
  @StructureGetter("wLabelFormulaLen")
  int getLabelFormulaLen();
  
  @StructureSetter("wLabelFormulaLen")
  CDActionExt setLabelFormulaLen(int len);
  
  @StructureGetter("wParentLabelFormulaLen")
  int getParentLabelFormulaLen();
  
  @StructureSetter("wParentLabelFormulaLen")
  CDActionExt setParentLabelFormulaLen(int len);
  
  @StructureGetter("wCompActionIDLen")
  int getCompActionIdLen();
  
  @StructureSetter("wCompActionIDLen")
  CDActionExt setCompActionIdLen(int len);
  
  @StructureGetter("wProgrammaticUseTxtLen")
  int getProgrammaticUseTextLen();
  
  @StructureSetter("wProgrammaticUseTxtLen")
  CDActionExt setProgrammaticUseTextLen(int len);
  
  default String getControlFormula() {
    return StructureSupport.extractCompiledFormula(
      this,
      0,
      getControlFormulaLen()
    );
  }
  
  default CDActionExt setControlFormula(String formula) {
    return StructureSupport.writeCompiledFormula(
      this,
      0,
      getControlFormulaLen(),
      formula,
      this::setControlFormulaLen
    );
  }
  
  default String getLabelFormula() {
    return StructureSupport.extractCompiledFormula(
      this,
      getControlFormulaLen(),
      getLabelFormulaLen()
    );
  }
  
  default CDActionExt setLabelFormula(String formula) {
    return StructureSupport.writeCompiledFormula(
      this,
      getControlFormulaLen(),
      getLabelFormulaLen(),
      formula,
      this::setLabelFormulaLen
    );
  }
  
  default String getParentLabelFormula() {
    return StructureSupport.extractCompiledFormula(
      this,
      getControlFormulaLen() + getLabelFormulaLen(),
      getParentLabelFormulaLen()
    );
  }
  
  default CDActionExt setParentLabelFormula(String formula) {
    return StructureSupport.writeCompiledFormula(
      this,
      getControlFormulaLen() + getLabelFormulaLen(),
      getParentLabelFormulaLen(),
      formula,
      this::setParentLabelFormulaLen
    );
  }
  
  default String getCompActionId() {
    return StructureSupport.extractStringValue(
      this,
      getControlFormulaLen() + getLabelFormulaLen() + getParentLabelFormulaLen(),
      getCompActionIdLen()
    );
  }
  
  default CDActionExt setCompActionId(String id) {
    return StructureSupport.writeStringValue(
      this,
      getControlFormulaLen() + getLabelFormulaLen() + getParentLabelFormulaLen(),
      getCompActionIdLen(),
      id,
      this::setCompActionIdLen
    );
  }
  
  default String getProgrammaticUseText() {
    return StructureSupport.extractStringValue(
      this,
      getControlFormulaLen() + getLabelFormulaLen() + getParentLabelFormulaLen() + getCompActionIdLen(),
      getProgrammaticUseTextLen()
    );
  }
  
  default CDActionExt setProgrammaticUseText(String text) {
    return StructureSupport.writeStringValue(
      this,
      getControlFormulaLen() + getLabelFormulaLen() + getParentLabelFormulaLen() + getCompActionIdLen(),
      getProgrammaticUseTextLen(),
      text,
      this::setProgrammaticUseTextLen
    );
  }
}
