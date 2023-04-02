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
package com.hcl.domino.formula;

import java.util.LinkedHashMap;
import java.util.List;

import com.hcl.domino.data.FormulaAnalyzeResult;
import com.hcl.domino.exception.FormulaCompilationException;
import com.hcl.domino.misc.JNXServiceFinder;

/**
 * This interface represents a service capable of compiling and decompiling
 * formula
 * language code.
 *
 * @author Jesse Gallagher
 * @since 1.0.15
 */
public interface FormulaCompiler {
  static FormulaCompiler get() {
    return JNXServiceFinder.findRequiredService(FormulaCompiler.class, FormulaCompiler.class.getClassLoader());
  }

  byte[] compile(String formula) throws FormulaCompilationException;

  /**
   * Method to generate the data for the $FORMULA item of a view definition by combining
   * the view's selection formula with the programmatic names and formulas of the columns
   * 
   * @param selectionFormula selection formula
   * @param columnItemNamesAndFormulas map with programmatic column names as keys and their formula as values, will be processed in key order; if null, we simply compile the selection formula
   * @param addConflict true to add special column for $Conflict at the end of the compiled formula (required for $Formula item of views)
   * @param addRef true to add the special column $REF at the end of the compiled formula (required for $Formula item of views)
   * @return data of combined formula
   */
  byte[] compile(String selectionFormula, LinkedHashMap<String,String> columnItemNamesAndFormulas, boolean addConflict, boolean addRef);

  default String decompile(byte[] compiledFormula) {
    return decompile(compiledFormula, false);
  }
  String decompile(byte[] compiledFormula, boolean isSelectionFormula);
  
  int getSize(byte[] formula);
  
  List<String> decompileMulti(byte[] compiledFormulas);

  public List<String> getAllFormulaFunctions();
  
  public List<String> getAllFormulaCommands();
  
  public List<String> getFunctionParameters(String atFunctionName);
  
  public FormulaAnalyzeResult analyzeFormula(String formula);
  
}
