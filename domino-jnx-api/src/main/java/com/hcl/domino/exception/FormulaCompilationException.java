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
package com.hcl.domino.exception;

import java.text.MessageFormat;

import com.hcl.domino.DominoException;

/**
 * Subclass of {@link DominoException} that is thrown when formula compilation
 * fails
 * and that provides details about the error
 *
 * @author Karsten Lehmann
 */
public class FormulaCompilationException extends DominoException {
  private static final long serialVersionUID = -3252229485728491910L;

  /**
   * Concatenates all parts of the error into one string
   *
   * @param msg                generic error message, e.g. "Formula error"
   * @param formula            formula that could not be compiled
   * @param compileErrorText   detail error describing what is wrong in the
   *                           formula
   * @param compileErrorStatus compile error reason status code
   * @param compileErrorLine   line number
   * @param compileErrorColumn column number
   * @param compileErrorOffset offset
   * @param compileErrorLength length of error
   * @return concatenated text with full error details
   */
  private static String toDetailedErrorMessage(final String msg, final String formula,
      final String compileErrorText,
      final short compileErrorStatus,
      final short compileErrorLine,
      final short compileErrorColumn,
      final short compileErrorOffset,
      final short compileErrorLength) {

    return MessageFormat.format(
        "{0}. errorcode={1}, errortext={2}, line={3}, column={4}, offset={5}, length={6}, formula={7}", //$NON-NLS-1$
        msg, compileErrorStatus, compileErrorText, compileErrorLine, compileErrorColumn, compileErrorOffset, compileErrorLength,
        formula);
  }

  private final short m_compileErrorReasonCode;
  private final String m_compileErrorReason;
  private final String m_formula;
  private final short m_compileErrorLine;
  private final short m_compileErrorColumn;
  private final short m_compileErrorOffset;

  private final short m_compileErrorLength;

  /**
   * Creates a new instance
   *
   * @param id                 error code for formula compilation, e.g.
   *                           ERR_FORMULA_COMPILATION (1281)
   * @param msg                error message for formula compilation, e.g.
   *                           "Formula Error"
   * @param formula            formula for which compilation failed
   * @param compileErrorReason detail error describing what is wrong in the
   *                           formula
   * @param compileErrorStatus compile error reason status code
   * @param compileErrorLine   line number
   * @param compileErrorColumn column number
   * @param compileErrorOffset offset
   * @param compileErrorLength length of error
   */
  public FormulaCompilationException(final int id, final String msg,
      final String formula,
      final String compileErrorReason,
      final short compileErrorStatus,
      final short compileErrorLine,
      final short compileErrorColumn,
      final short compileErrorOffset,
      final short compileErrorLength) {

    super(id, FormulaCompilationException.toDetailedErrorMessage(msg, formula,
        compileErrorReason,
        compileErrorStatus,
        compileErrorLine,
        compileErrorColumn,
        compileErrorOffset,
        compileErrorLength));

    this.m_formula = formula;
    this.m_compileErrorReason = compileErrorReason;
    this.m_compileErrorReasonCode = compileErrorStatus;
    this.m_compileErrorLine = compileErrorLine;
    this.m_compileErrorColumn = compileErrorColumn;
    this.m_compileErrorOffset = compileErrorOffset;
    this.m_compileErrorLength = compileErrorLength;
  }

  /**
   * Returns the column number where the error occurred
   *
   * @return column
   */
  public int getCompileErrorColumn() {
    return this.m_compileErrorColumn;
  }

  /**
   * Returns the lengths of the error (probably the length of the error producing
   * code)
   *
   * @return length
   */
  public int getCompileErrorLength() {
    return this.m_compileErrorLength;
  }

  /**
   * Returns the line where the error occurred
   *
   * @return line
   */
  public int getCompileErrorLine() {
    return this.m_compileErrorLine;
  }

  /**
   * Returns the offset where the error occurred
   *
   * @return offset
   */
  public int getCompileErrorOffset() {
    return this.m_compileErrorOffset;
  }

  /**
   * Returns the detail info what is wrong in the formula
   *
   * @return error reason
   */
  public String getCompileErrorReason() {
    return this.m_compileErrorReason;
  }

  /**
   * Returns a numeric error code for the compile error
   *
   * @return error reason code
   */
  public int getCompileErrorReasonCode() {
    return this.m_compileErrorReasonCode;
  }

  /**
   * Returns the formula that raised the error
   *
   * @return formula
   */
  public String getFormula() {
    return this.m_formula;
  }
}
