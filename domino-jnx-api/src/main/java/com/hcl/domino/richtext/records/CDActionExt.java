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

import com.hcl.domino.design.format.ActionBarControlType;
import com.hcl.domino.misc.StructureSupport;
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
    @StructureMember(name = "dwFlags", type = int.class),
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
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("wControlType")
  ActionBarControlType getControlType();
  
  @StructureSetter("wControlType")
  CDActionExt setControlType(ActionBarControlType type);
  
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
