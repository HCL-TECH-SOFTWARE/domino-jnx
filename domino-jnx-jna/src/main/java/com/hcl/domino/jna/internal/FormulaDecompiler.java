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
package com.hcl.domino.jna.internal;

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
		DisposableMemory mem = new DisposableMemory(compiledFormula.length);
		try {
			mem.write(0, compiledFormula, 0, compiledFormula.length);
			return decompileFormula(mem);
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
		DHANDLE.ByReference rethFormulaText = DHANDLE.newInstanceByReference();
		ShortByReference retFormulaTextLength = new ShortByReference();
		short result = NotesCAPI.get().NSFFormulaDecompile(ptr, false, rethFormulaText, retFormulaTextLength);
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
}
