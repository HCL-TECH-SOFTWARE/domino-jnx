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
package com.hcl.domino.exception;

import java.text.MessageFormat;

import com.hcl.domino.DominoException;

/**
 * Subclass of {@link DominoException} that is thrown when formula compilation fails
 * and that provides details about the error
 * 
 * @author Karsten Lehmann
 */
public class FormulaCompilationException extends DominoException {
	private static final long serialVersionUID = -3252229485728491910L;
	private short m_compileErrorReasonCode;
	private String m_compileErrorReason;
	private String m_formula;
	private short m_compileErrorLine;
	private short m_compileErrorColumn;
	private short m_compileErrorOffset;
	private short m_compileErrorLength;
	
	/**
	 * Creates a new instance
	 * 
	 * @param id error code for formula compilation, e.g. ERR_FORMULA_COMPILATION (1281)
	 * @param msg error message for formula compilation, e.g. "Formula Error"
	 * @param formula formula for which compilation failed
	 * @param compileErrorReason detail error describing what is wrong in the formula
	 * @param compileErrorStatus compile error reason status code
	 * @param compileErrorLine line number
	 * @param compileErrorColumn column number
	 * @param compileErrorOffset offset
	 * @param compileErrorLength length of error
	 */
	public FormulaCompilationException(int id, String msg,
			String formula,
			String compileErrorReason,
			short compileErrorStatus,
			short compileErrorLine,
			short compileErrorColumn,
			short compileErrorOffset,
			short compileErrorLength) {
		
		super(id, toDetailedErrorMessage(msg, formula,
				compileErrorReason,
				compileErrorStatus,
				compileErrorLine,
				compileErrorColumn,
				compileErrorOffset,
				compileErrorLength));
		
		m_formula = formula;
		m_compileErrorReason = compileErrorReason;
		m_compileErrorReasonCode = compileErrorStatus;
		m_compileErrorLine = compileErrorLine;
		m_compileErrorColumn = compileErrorColumn;
		m_compileErrorOffset = compileErrorOffset;
		m_compileErrorLength = compileErrorLength;
	}
	
	/**
	 * Concatenates all parts of the error into one string
	 * 
	 * @param msg generic error message, e.g. "Formula error"
	 * @param formula formula that could not be compiled
	 * @param compileErrorText detail error describing what is wrong in the formula
	 * @param compileErrorStatus compile error reason status code
	 * @param compileErrorLine line number
	 * @param compileErrorColumn column number
	 * @param compileErrorOffset offset
	 * @param compileErrorLength length of error
	 * @return concatenated text with full error details
	 */
	private static String toDetailedErrorMessage(String msg, String formula,
			String compileErrorText,
			short compileErrorStatus,
			short compileErrorLine,
			short compileErrorColumn,
			short compileErrorOffset,
			short compileErrorLength) {
		
		return MessageFormat.format(
			"{0}. errorcode={1}, errortext={2}, line={3}, column={4}, offset={5}, length={6}, formula={7}", //$NON-NLS-1$
			msg, compileErrorStatus, compileErrorText, compileErrorLine, compileErrorColumn, compileErrorOffset, compileErrorLength, formula
		);
	}
	
	/**
	 * Returns the formula that raised the error
	 * 
	 * @return formula
	 */
	public String getFormula() {
		return m_formula;
	}
	
	/**
	 * Returns the detail info what is wrong in the formula
	 * 
	 * @return error reason
	 */
	public String getCompileErrorReason() {
		return m_compileErrorReason;
	}
	
	/**
	 * Returns a numeric error code for the compile error
	 * 
	 * @return error reason code
	 */
	public int getCompileErrorReasonCode() {
		return m_compileErrorReasonCode;
	}
	
	/**
	 * Returns the line where the error occurred
	 * 
	 * @return line
	 */
	public int getCompileErrorLine() {
		return m_compileErrorLine;
	}
	
	/**
	 * Returns the column number where the error occurred
	 * 
	 * @return column
	 */
	public int getCompileErrorColumn() {
		return m_compileErrorColumn;
	}
	
	/**
	 * Returns the offset where the error occurred
	 * 
	 * @return offset
	 */
	public int getCompileErrorOffset() {
		return m_compileErrorOffset;
	}
	
	/**
	 * Returns the lengths of the error (probably the length of the error producing code)
	 * 
	 * @return length
	 */
	public int getCompileErrorLength() {
		return m_compileErrorLength;
	}
}
