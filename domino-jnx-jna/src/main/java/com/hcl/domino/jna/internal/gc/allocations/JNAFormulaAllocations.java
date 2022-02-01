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
package com.hcl.domino.jna.internal.gc.allocations;

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;

import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.exception.FormulaCompilationException;
import com.hcl.domino.jna.data.JNAFormula;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ShortByReference;

public class JNAFormulaAllocations extends APIObjectAllocations<JNAFormula> {
	private DHANDLE m_hFormula;
	private DHANDLE m_hCompute;
	private Pointer m_ptrCompiledFormula;
	private int m_compiledFormulaLength;
	private boolean m_disposed;
	
	@SuppressWarnings("rawtypes")
	public JNAFormulaAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNAFormula referent, ReferenceQueue<? super IAPIObject> q) {
		
		super(parentDominoClient, parentAllocations, referent, q);
	}

	@Override
	public boolean isDisposed() {
		return m_disposed;
	}

	public DHANDLE getFormulaHandle() {
		return m_hFormula;
	}
	
	public DHANDLE getComputeHandle() {
		return m_hCompute;
	}
	
	public byte[] getCompiledFormula() {
		checkDisposed();
		
		//return compiled formula as byte array
		byte[] compiledFormula = m_ptrCompiledFormula.getByteArray(0, m_compiledFormulaLength);
		return compiledFormula;
	}
	
	public void initWithFormula(String formula) {
		if (m_hFormula!=null) {
			dispose();
		}
		
		Memory formulaName = null;
		short formulaNameLength = 0;
		Memory formulaText = NotesStringUtils.toLMBCS(formula, false, false);
		short formulaTextLength = (short) formulaText.size();

		short computeFlags = 0;

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
		m_hFormula = rethFormula;
		m_compiledFormulaLength = retFormulaLength.getValue() & 0xffff;
		
		DHANDLE.ByReference rethCompute = DHANDLE.newInstanceByReference();
		
		m_ptrCompiledFormula = LockUtil.lockHandle(m_hFormula, (handleByVal) -> {
			return Mem.OSLockObject(handleByVal);
		});
		
		result = NotesCAPI.get().NSFComputeStart(computeFlags, m_ptrCompiledFormula, rethCompute);
		NotesErrorUtils.checkResult(result);
		
		m_hCompute = rethCompute;
	}
	
	@SuppressWarnings("unused")
	private static String toDetailedErrorMessage(String msg, String formula,
			short compileError,
			short compileErrorLine,
			short compileErrorColumn,
			short compileErrorOffset,
			short compileErrorLength) {
		
		return MessageFormat.format(
			"{0}. {1}, line={2}, column={3}, offset={4}, length={5}, formula={6}", //$NON-NLS-1$
			msg, NotesErrorUtils.errToString(compileError),
			compileErrorLine, compileErrorColumn, compileErrorOffset, compileErrorLength, formula
		);
	}
	
	@Override
	public void dispose() {
		if (isDisposed()) {
			return;
		}

		if (m_hCompute!=null) {
			LockUtil.lockHandle(m_hCompute, (handleByVal) -> {
				if (isDisposed()) {
					return 0;
				}
				
				short result = NotesCAPI.get().NSFComputeStop(handleByVal);
				NotesErrorUtils.checkResult(result);
				m_hCompute.setDisposed();
				m_hCompute = null;
				
				return 0;
			});
		}
		
		if (m_hFormula!=null) {
			LockUtil.lockHandle(m_hFormula, (handleByVal) -> {
				if (isDisposed()) {
					return 0;
				}
				
				Mem.OSUnlockObject(handleByVal);
				
				short result = Mem.OSMemFree(handleByVal);
				NotesErrorUtils.checkResult(result);
				
				m_hFormula.setDisposed();
				m_hFormula = null;
				m_ptrCompiledFormula = null;
				
				return 0;
			});
		}
	}

}
