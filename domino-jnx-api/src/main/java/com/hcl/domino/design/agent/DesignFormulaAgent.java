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
package com.hcl.domino.design.agent;

import com.hcl.domino.design.DesignAgent;
import com.hcl.domino.design.simpleaction.RunFormulaAction;

/**
 * Subtype of {@link DesignAgent} that executes a formula
 * 
 * @since 1.0.47
 */
public interface DesignFormulaAgent extends DesignAgent, RunFormulaAction {
  
  /**
   * Sets the document action performed by this agent
   * 
   * @param action action
   */
  void setDocumentAction(DocumentAction action);
  
  /**
   * Changes the formula code
   * 
   * @param formula the agent formula script
   */
  void setFormula(String formula);

}
