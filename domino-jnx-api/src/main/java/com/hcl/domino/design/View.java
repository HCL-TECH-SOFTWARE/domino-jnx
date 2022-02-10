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
package com.hcl.domino.design;

import java.util.List;

import com.hcl.domino.design.simplesearch.SimpleSearchTerm;

/**
 * Access to a database design. Search for design, database as constructor
 * parameter
 *
 * @author t.b.d
 */
public interface View extends CollectionDesignElement<View> {

  String getSelectionFormula();

  View setSelectionFormula(final String selectionFormula);
  
  /**
   * Retrieves the simple search terms used to select documents for document selection,
   * if provided.
   * 
   * @return a {@link List} containing {@link SimpleSearchTerm} subclass instances, or an
   *         empty list if no search criteria are provided
   * @since 1.0.38
   */
  List<? extends SimpleSearchTerm> getDocumentSelection();

  /**
   * Retrieves the "formula class" value, a special indicator used by the indexer
   * to select note types.
   * 
   * @return the formula-class value for this view
   * @since 1.0.32
   */
  String getFormulaClass();
  
  /**
   * Sets the "formula class" value, a special indicator used by the indexer
   * to select note types.
   * 
   * @param formulaClass the formula-class value to set for this view
   * @return this view
   * @since 1.0.32
   */
  View setFormulaClass(String formulaClass);
}