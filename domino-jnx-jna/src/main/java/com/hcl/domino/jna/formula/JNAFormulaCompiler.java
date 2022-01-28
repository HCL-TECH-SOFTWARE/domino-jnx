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
package com.hcl.domino.jna.formula;

import java.util.LinkedHashMap;
import java.util.List;

import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.exception.FormulaCompilationException;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.FormulaDecompiler;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.views.ViewFormulaCompiler;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

public class JNAFormulaCompiler implements FormulaCompiler {

	@Override
	public byte[] compile(String formula)  throws FormulaCompilationException {
		Memory formulaName = null;
		short formulaNameLength = 0;
		Memory formulaText = NotesStringUtils.toLMBCS(formula, false, false);
		short formulaTextLength = (short) formulaText.size();

		DHANDLE.ByReference rethFormula = DHANDLE.newInstanceByReference();
		ShortByReference retFormulaLength = new ShortByReference();
		ShortByReference retCompileError = new ShortByReference();
		ShortByReference retCompileErrorLine = new ShortByReference();
		ShortByReference retCompileErrorColumn = new ShortByReference();
		ShortByReference retCompileErrorOffset = new ShortByReference();
		ShortByReference retCompileErrorLength = new ShortByReference();
		
		short result = NotesCAPI.get().NSFFormulaCompile(formulaName, formulaNameLength,
				formulaText, formulaTextLength, rethFormula, retFormulaLength,
				retCompileError, retCompileErrorLine, retCompileErrorColumn,
				retCompileErrorOffset, retCompileErrorLength);
		
		if (result == INotesErrorConstants.ERR_FORMULA_COMPILATION) {
			String errMsg = NotesErrorUtils.errToString(result); // "Formula Error"
			String compileErrorReason = NotesErrorUtils.errToString(retCompileError.getValue());

			throw new FormulaCompilationException(result, errMsg, formula,
					compileErrorReason,
					retCompileError.getValue(),
					retCompileErrorLine.getValue(),
					retCompileErrorColumn.getValue(),
					retCompileErrorOffset.getValue(),
					retCompileErrorLength.getValue());
		}
		NotesErrorUtils.checkResult(result);
		
		int compiledFormulaLength = retFormulaLength.getValue() & 0xffff;
		
		return LockUtil.lockHandle(rethFormula, (handleByVal) -> {
			Pointer ptrCompiledFormula = Mem.OSLockObject(handleByVal);
			byte[] resultBytes = new byte[compiledFormulaLength];
			ptrCompiledFormula.getByteBuffer(0, compiledFormulaLength).get(resultBytes);
			Mem.OSUnlockObject(handleByVal);
			Mem.OSMemFree(handleByVal);
			return resultBytes;
		});
	}

	@Override
	public String decompile(byte[] compiledFormula, boolean isSelectionFormula) {
		return FormulaDecompiler.decompileFormula(compiledFormula, isSelectionFormula);
	}
	
	@Override
	public int getSize(byte[] formula) {
	  try (DisposableMemory mem = new DisposableMemory(formula.length)) {
	    mem.write(0, formula, 0, formula.length);
	    
	    ShortByReference retFormulaLength = new ShortByReference();
	    short result = NotesCAPI.get().NSFFormulaGetSizeP(mem, retFormulaLength);
	    NotesErrorUtils.checkResult(result);
	    
	    return (int) (retFormulaLength.getValue() & 0xffff);
	  }
	}
	
	@Override
	public List<String> decompileMulti(byte[] compiledFormulas) {
	  try (DisposableMemory mem = new DisposableMemory(compiledFormulas.length)) {
      mem.write(0, compiledFormulas, 0, compiledFormulas.length);

      return FormulaDecompiler.decompileFormulas(mem, compiledFormulas.length);
    }
	}

	@Override
	public byte[] compile(String selectionFormula, LinkedHashMap<String,String> columnItemNamesAndFormulas) {
	  DHANDLE.ByReference hFormula = null;
	  try {
	    hFormula = ViewFormulaCompiler.compile(selectionFormula, columnItemNamesAndFormulas);
	    
	    return LockUtil.lockHandle(hFormula, (hFormulaByVal) -> {
	       IntByReference retSize = new IntByReference();
	       short resultGetSize = Mem.OSMemGetSize(hFormulaByVal, retSize);
	        NotesErrorUtils.checkResult(resultGetSize);

	        Pointer ptrCompiledFormula = Mem.OSLockObject(hFormulaByVal);
	        try {
	          byte[] resultBytes = new byte[retSize.getValue()];
	          ptrCompiledFormula.getByteBuffer(0, resultBytes.length).get(resultBytes);
	          return resultBytes;
	        }
	        finally {
	          Mem.OSUnlockObject(hFormulaByVal);
	        }
	    });
	  }
	  finally {
	    if (hFormula!=null && !hFormula.isNull()) {
	      short result = LockUtil.lockHandle(hFormula, (hFormulaByVal) -> {
	        return Mem.OSMemFree(hFormulaByVal);
	      });
	      NotesErrorUtils.checkResult(result);
	    }
	  }
	}

}
