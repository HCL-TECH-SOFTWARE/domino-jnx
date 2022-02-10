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
package com.hcl.domino.jna.internal.views;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.exception.FormulaCompilationException;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.sun.jna.Memory;
import com.sun.jna.ptr.ShortByReference;

/**
 * Utility class to generate the $FORMULA item of view design notes
 * 
 * @author Karsten Lehmann
 */
public class ViewFormulaCompiler {
	
	/**
	 * Method to generate the data for the $FORMULA item of a view definition by combining
	 * the view's selection formula with the programmatic names and formulas of the columns
	 * 
	 * @param selectionFormula selection formula
	 * @param columnItemNamesAndFormulas map with programmatic column names as keys and their formula as values, will be processed in key order; if null, we simply compile the selection formula
	 * @return handle to combined formula
	 */
	public static DHANDLE.ByReference compile(String selectionFormula, LinkedHashMap<String,String> columnItemNamesAndFormulas) {
		Memory formulaName = null;
		short formulaNameLength = 0;
		Memory selectionFormulaMem = NotesStringUtils.toLMBCS(selectionFormula, false);
		short selectionFormulaLength = (short) (selectionFormulaMem.size() & 0xffff);

		ShortByReference retFormulaLength = new ShortByReference();
		retFormulaLength.setValue((short) 0);
		ShortByReference retCompileError = new ShortByReference();
		retCompileError.setValue((short) 0);
		ShortByReference retCompileErrorLine = new ShortByReference();
		retCompileErrorLine.setValue((short) 0);
		ShortByReference retCompileErrorColumn = new ShortByReference();
		retCompileErrorColumn.setValue((short) 0);
		ShortByReference retCompileErrorOffset = new ShortByReference();
		retCompileErrorOffset.setValue((short) 0);
		ShortByReference retCompileErrorLength = new ShortByReference();
		retCompileErrorLength.setValue((short) 0);

		DHANDLE.ByReference rethViewFormula = DHANDLE.newInstanceByReference();
		rethViewFormula.clear();
		
		short result = NotesCAPI.get().NSFFormulaCompile(formulaName, formulaNameLength, selectionFormulaMem, selectionFormulaLength,
				rethViewFormula,
				retFormulaLength, retCompileError, retCompileErrorLine, retCompileErrorColumn, retCompileErrorOffset,
				retCompileErrorLength);
		
		if (result == INotesErrorConstants.ERR_FORMULA_COMPILATION) {
			String errMsg = NotesErrorUtils.errToString(result); // "Formula Error"
			String compileErrorReason = NotesErrorUtils.errToString(retCompileError.getValue());

			throw new FormulaCompilationException(result, errMsg, selectionFormula,
					compileErrorReason,
					retCompileError.getValue(),
					retCompileErrorLine.getValue(),
					retCompileErrorColumn.getValue(),
					retCompileErrorOffset.getValue(),
					retCompileErrorLength.getValue());
		}
		NotesErrorUtils.checkResult(result);
		
		if (columnItemNamesAndFormulas!=null) {
			boolean errorCompilingColumns = true;
			
			//keep track of what to dispose when compiling errors occur
			List<DHANDLE> columnFormulaHandlesToDisposeOnError = new ArrayList<>();
			
			try {
				//compile each column and merge them with the view formula
				for (Entry<String,String> currEntry : columnItemNamesAndFormulas.entrySet()) {
					String columnItemName = currEntry.getKey();

					Memory columnItemNameMem = NotesStringUtils.toLMBCS(columnItemName, false);
					short columnItemNameLength = (short) (columnItemNameMem.size() & 0xffff);

					//add summary item definition for column
					result = LockUtil.lockHandle(rethViewFormula, (viewFormulaHandleByValue) -> {
						return NotesCAPI.get().NSFFormulaSummaryItem(viewFormulaHandleByValue, columnItemNameMem, columnItemNameLength);
					});
					NotesErrorUtils.checkResult(result);

					String columnFormula = currEntry.getValue().trim();
					
					if (!StringUtil.isEmpty(columnFormula)) {
						//if we have a column formula, compile it and add it to the view formula
						Memory columnFormulaMem = NotesStringUtils.toLMBCS(columnFormula, false);
						short columnFormulaLength = (short) (columnFormulaMem.size() & 0xffff);
						
						ShortByReference retColumnFormulaLength = new ShortByReference();
						retColumnFormulaLength.setValue((short) 0);
						ShortByReference retColumnCompileError = new ShortByReference();
						retColumnCompileError.setValue((short) 0);
						ShortByReference retColumnCompileErrorLine = new ShortByReference();
						retColumnCompileErrorLine.setValue((short) 0);
						ShortByReference retColumnCompileErrorColumn = new ShortByReference();
						retColumnCompileErrorColumn.setValue((short) 0);
						ShortByReference retColumnCompileErrorOffset = new ShortByReference();
						retColumnCompileErrorOffset.setValue((short) 0);
						ShortByReference retColumnCompileErrorLength = new ShortByReference();
						retColumnCompileErrorLength.setValue((short) 0);
						
						DHANDLE.ByReference rethColumnFormula = DHANDLE.newInstanceByReference();
						
						result = NotesCAPI.get().NSFFormulaCompile(columnItemNameMem, columnItemNameLength, columnFormulaMem,
								columnFormulaLength, rethColumnFormula, retColumnFormulaLength, retColumnCompileError, retColumnCompileErrorLine,
								retColumnCompileErrorColumn, retColumnCompileErrorOffset, retColumnCompileErrorOffset);
						
						if (result == INotesErrorConstants.ERR_FORMULA_COMPILATION) {
							String errMsg = NotesErrorUtils.errToString(result); // "Formula Error"
							String compileErrorReason = NotesErrorUtils.errToString(retCompileError.getValue());

							throw new FormulaCompilationException(result, errMsg, columnFormula,
									compileErrorReason,
									retColumnCompileError.getValue(),
									retColumnCompileErrorLine.getValue(),
									retColumnCompileErrorColumn.getValue(),
									retColumnCompileErrorOffset.getValue(),
									retColumnCompileErrorLength.getValue());
						}
						NotesErrorUtils.checkResult(result);

						if (rethColumnFormula.isNull()) {
							throw new IllegalStateException(MessageFormat.format("Column formula handle is 0 for formula: {0}", columnFormula));
						}
						
						columnFormulaHandlesToDisposeOnError.add(rethColumnFormula);
						
						//merge formulas
						result = LockUtil.lockHandles(
								rethColumnFormula,
								rethViewFormula,
								
								(hColumnFormulaByVal, hViewFormulaByVal) -> {
									return NotesCAPI.get().NSFFormulaMerge(hColumnFormulaByVal, hViewFormulaByVal);
									
								}
								);
						NotesErrorUtils.checkResult(result);
					}
				}
				//all ok!
				errorCompilingColumns = false;
			}
			finally {
				//in any case free the compiled column memory
				for (DHANDLE currColumnFormulaHandle : columnFormulaHandlesToDisposeOnError) {
					LockUtil.lockHandle(currColumnFormulaHandle, (currColumnFormulaHandleByVal) -> {
						short localResult = Mem.OSMemFree(currColumnFormulaHandleByVal);
						NotesErrorUtils.checkResult(localResult);
						return 0;
					});
				}
				
				//and if errors occurred compiling the columns, free the view formula memory as well
				if (errorCompilingColumns) {
					LockUtil.lockHandle(rethViewFormula, (hViewFormulaByVal) -> {
						short localResult = Mem.OSMemFree(hViewFormulaByVal);
						NotesErrorUtils.checkResult(localResult);
						return 0;
					});
				}
			}
			
			if (errorCompilingColumns) {
				//should not happen; just avoiding to return a null handle in case of programming error
				throw new IllegalStateException("Unexpected state. There were unreported errors compiling the column formulas");
			}
		}
		
		if (rethViewFormula.isNull()) {
			throw new IllegalStateException("Unexpected state. Formula handle to be returned is null");
		}
		
		return rethViewFormula;
	}

}
