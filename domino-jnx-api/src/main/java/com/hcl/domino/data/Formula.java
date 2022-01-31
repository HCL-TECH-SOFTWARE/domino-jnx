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
package com.hcl.domino.data;

import java.util.List;

/**
 * A compiled Domino formula that can be evaluated standalone or
 * on one or multiple documents.
 */
public interface Formula {

  /**
   * Formula computation result
   */
  public interface FormulaExecutionResult {

    /**
     * Returns the result
     *
     * @return result, e.g. List with String or {@link DominoDateTime}
     */
    List<Object> getValue();

    /**
     * Returns true if the formula has modified the document
     *
     * @return true if modified
     */
    boolean isDocModified();

    /**
     * Returns true if the formula matches the current document
     *
     * @return true if match
     */
    boolean matchesFormula();

    /**
     * Returns true if the formula triggered a @DeleteDocument function
     *
     * @return true if deletion is requested
     */
    boolean shouldBeDeleted();

  }

  /**
   * Evaluates the formula in the current environment (client/server)
   *
   * @return the return values of the formula as a {@link List}
   */
  List<Object> evaluate();

  /**
   * Runs a formula on a document
   *
   * @param doc the document context
   * @return result, e.g. List with String or {@link DominoDateTime}
   */
  List<Object> evaluate(Document doc);

  /**
   * Runs a formula on a document and converts the result to a number
   *
   * @param doc          document
   * @param defaultValue default value to be returned of result is not a number
   * @return result
   */
  Double evaluateAsNumber(Document doc, Double defaultValue);

  /**
   * Evaluates the formula and converts the result to a number
   *
   * @param defaultValue default value to be returned of result is not a number
   * @return result
   */
  Double evaluateAsNumber(Double defaultValue);

  /**
   * Evaluates the formula and converts the result to a string
   *
   * @return result
   */
  String evaluateAsString();

  /**
   * Runs a formula on a document and converts the result to a string
   *
   * @param doc document
   * @return result
   */
  String evaluateAsString(Document doc);

  /**
   * Formula execution that returns more information about the computation
   * result
   *
   * @param doc document
   * @return result
   */
  FormulaExecutionResult evaluateExt(Document doc);

  /**
   * Returns the formula string that was used to compile this {@link Formula}
   * object
   *
   * @return formula string
   */
  String getFormula();
}
