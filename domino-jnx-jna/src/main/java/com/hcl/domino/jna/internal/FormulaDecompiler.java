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
package com.hcl.domino.jna.internal;

import java.util.ArrayList;
import java.util.List;

import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ShortByReference;

public class FormulaDecompiler {

  /**
   * Decompiles a compiled formula
   * 
   * @param compiledFormula compiled formula as byte array
   * @return formula
   */
  public static String decompileFormula(byte[] compiledFormula) {
    return decompileFormula(compiledFormula, false);
  }
  
	/**
	 * Decompiles a compiled formula
	 * 
	 * @param compiledFormula compiled formula as byte array
   * @param isSelectionFormula true if decoding a selection formula
	 * @return formula
	 */
	public static String decompileFormula(byte[] compiledFormula, boolean isSelectionFormula) {
		DisposableMemory mem = new DisposableMemory(compiledFormula.length);
		try {
			mem.write(0, compiledFormula, 0, compiledFormula.length);
			return decompileFormula(mem, isSelectionFormula);
		}
		finally {
			mem.dispose();
		}
	}
	
	 /**
   * Decompiles a compiled formula
   * 
   * @param ptr pointer to compiled formula
   * @return formula
   */
  public static String decompileFormula(Pointer ptr) {
    return decompileFormula(ptr, false);
  }
  
	/**
	 * Decompiles a compiled formula
	 * 
	 * @param ptr pointer to compiled formula
	 * @param isSelectionFormula true if decoding a selection formula
	 * @return formula
	 */
	public static String decompileFormula(Pointer ptr, boolean isSelectionFormula) {
		DHANDLE.ByReference rethFormulaText = DHANDLE.newInstanceByReference();
		ShortByReference retFormulaTextLength = new ShortByReference();
		short result = NotesCAPI.get().NSFFormulaDecompile(ptr, isSelectionFormula, rethFormulaText, retFormulaTextLength);
		NotesErrorUtils.checkResult(result);

		return LockUtil.lockHandle(rethFormulaText, (rethFormulaTextByVal) -> {
			Pointer formulaPtr = Mem.OSLockObject(rethFormulaTextByVal);
			try {
				int textLen = retFormulaTextLength.getValue() & 0xffff;
				String formula = NotesStringUtils.fromLMBCS(formulaPtr, textLen);
				return formula;
			}
			finally {
				Mem.OSUnlockObject(rethFormulaTextByVal);
				short freeResult = Mem.OSMemFree(rethFormulaTextByVal);
				NotesErrorUtils.checkResult(freeResult);
			}
		});
	}

	/**
	 * Returns the size of a compiled formula in bytes
	 * 
	 * @param ptr pointer to compiled formula
	 * @return size
	 */
  public static int getSize(Pointer ptr) {
    ShortByReference retFormulaLength = new ShortByReference();
    short result = NotesCAPI.get().NSFFormulaGetSizeP(ptr, retFormulaLength);
    NotesErrorUtils.checkResult(result);
    
    return (int) (retFormulaLength.getValue() & 0xffff);
  }
	
	/**
	 * Decompiles multiple formulas stored next to each other
	 * 
	 * @param ptr pointer to memory with compiled formulas
	 * @param totalLen total size of memory
	 * @return list of formulas
	 */
	public static List<String> decompileFormulas(Pointer ptr, int totalLen) {
	  List<String> formulas = new ArrayList<>();
	  
	  int offset = 0;
	  while (offset < totalLen) {
	    Pointer currPtr = ptr.share(offset);
	    int size = getSize(currPtr);
	    String formula = decompileFormula(currPtr);
	    formulas.add(formula);
	    offset += size;
	  }
	  
	  return formulas;
	}
}
