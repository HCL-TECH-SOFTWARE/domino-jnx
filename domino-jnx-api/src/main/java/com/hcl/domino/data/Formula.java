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

import java.util.Collection;
import java.util.List;

import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;

/**
 * A compiled Domino formula that can be evaluated standalone or
 * on one or multiple documents.<br>
 * <br>
 * The implementation supports more than the usual 64K of return value, e.g. to use
 * <code>@DBColumn</code> to read the column values in views with many entries.
 */
public interface Formula {
  public enum Disallow implements INumberEnum<Integer>{
    /** Setting of environment variables */
    SETENVIRONMENT(NotesConstants.COMPUTE_CAPABILITY_SETENVIRONMENT),
    UICOMMANDS(NotesConstants.COMPUTE_CAPABILITY_UICOMMANDS),
    /** <code>FIELD Foo :=</code> */
    ASSIGN(NotesConstants.COMPUTE_CAPABILITY_ASSIGN),
     /** <code>@SetDocField</code>, <code>@DocMark</code> */
    SIDEEFFECTS(NotesConstants.COMPUTE_CAPABILITY_SIDEEFFECTS),
    /** Any compute extension. */
    EXTENSION(NotesConstants.COMPUTE_CAPABILITY_EXTENSION),
     /** Any compute extension with side-effects */
    UNSAFE_EXTENSION(NotesConstants.COMPUTE_CAPABILITY_UNSAFE_EXTENSION),
    /** Built-in compute extensions */
    FALLBACK_EXT(NotesConstants.COMPUTE_CAPABILITY_FALLBACK_EXT),
    /** Unsafe is any @func that creates/modifies anything (i.e. not "read only") */
    UNSAFE(NotesConstants.COMPUTE_CAPABILITY_UNSAFE);
    
    private final int m_value;
    
    private Disallow(int value) {
      m_value = value;
    }
    
    @Override
    public Integer getValue() {
      return m_value;
    }
    
    @Override
    public long getLongValue() {
      return m_value;
    }
    
  }
  
  /**
   * Prevents the execution of unsafe formula operations.
   * 
   * @param actions disallowed actions
   * @return this instance
   * @since 1.10.11
   */
  Formula disallow(Collection<Disallow> actions);
  
  /**
   * Prevents the execution of unsafe formula operations.
   * 
   * @param action disallowed action
   * @return this instance
   * @since 1.10.11
   */
  Formula disallow(Disallow action);

  /**
   * Checks whether a formula operation is disallowed
   * 
   * @param action action
   * @return true if disallowed
   * @since 1.10.11
   */
  boolean isDisallowed(Disallow action);

  /**
   * Returns a list of all registered (public) formula functions. Use {@link #getFunctionParameters(String)}
   * to get a list of function parameters.
   * 
   * @return functions, e.g. "@Left("
   */
  public static List<String> getAllFormulaFunction() {
    return FormulaCompiler.get().getAllFormulaFunctions();
  }

  /**
   * Returns a list of all registered (public) formula commands. Use {@link #getFunctionParameters(String)}
   * to get a list of command parameters.
   * 
   * @return commands, e.g. "MailSend"
   */
  public static List<String> getAllFormulaCommands() {
    return FormulaCompiler.get().getAllFormulaCommands();
  }
  
  /**
   * Scan through the function table or keyword table to find parameters of an @ function or an @ command.
   * 
   * @param atFunctionName name returned by {@link #getAllFormulaFunction()} or {@link #getAllFormulaCommands()}, e.g. "@Left("
   * @return function parameters, e.g. ["stringToSearch; numberOfChars)", "stringToSearch; subString)"] for the function "@Left("
   */
  public static List<String> getFunctionParameters(String atFunctionName) {
    return FormulaCompiler.get().getFunctionParameters(atFunctionName);
  }
  
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
  
  /**
   * Analyzes the formula, e.g. to check if its just a field name, a constant value
   * or time based.
   * 
   * @return analyze result
   */
  FormulaAnalyzeResult analyze();
  
}
