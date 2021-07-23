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
package com.hcl.domino.jna.data;

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.errors.UnsupportedItemValueError;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.Formula;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.exception.FormulaCompilationException;
import com.hcl.domino.exception.IncompatibleImplementationException;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.ItemDecoder;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADocumentAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAFormulaAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.misc.DominoEnumUtil;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * Utility class to execute a Domino formula on one or more {@link Document} objects.
 * 
 * @author Karsten Lehmann
 */
public class JNAFormula extends BaseJNAAPIObject<JNAFormulaAllocations> implements IAdaptable, Formula {
	private String m_formula;

	/**
	 * Creates a new instance. The constructure compiles the formula and throws a {@link FormulaCompilationException},
	 * if there are any compilation errors
	 * 
	 * @param parent API parent object
	 * @param formula formula
	 * @throws FormulaCompilationException if formula has wrong syntax
	 */
	public JNAFormula(IAPIObject<?> parent, String formula) throws FormulaCompilationException {
		super(parent);
		
		m_formula = formula;
		
		getAllocations().initWithFormula(formula);
		
		setInitialized();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNAFormulaAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNAFormulaAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> T getAdapterLocal(Class<T> clazz) {
		if (clazz == byte[].class) {
			return (T) getAllocations().getCompiledFormula();
		}
		
		return null;
	}
	
	@Override
	public String getFormula() {
		return m_formula;
	}

	@Override
	public String toStringLocal() {
		if (isDisposed()) {
			return MessageFormat.format("Compiled formula [disposed, formula={0}]", m_formula); //$NON-NLS-1$
		}
		else {
			return MessageFormat.format("Compiled formula [formula={0}]", m_formula); //$NON-NLS-1$
		}
	}
	
	private List<Object> parseFormulaResult(Pointer valuePtr, int valueLength) {
		short dataType = valuePtr.getShort(0);
		int dataTypeAsInt = dataType & 0xffff;
		
		ItemDataType type = DominoEnumUtil.valueOf(ItemDataType.class, dataTypeAsInt).orElse(ItemDataType.TYPE_INVALID_OR_UNKNOWN);
		switch(type) {
		case TYPE_TEXT:
		case TYPE_TEXT_LIST:
		case TYPE_NUMBER:
		case TYPE_NUMBER_RANGE:
		case TYPE_TIME:
		case TYPE_TIME_RANGE:
		case TYPE_UNAVAILABLE:
		case TYPE_ERROR:
			break;
		default:
			throw new UnsupportedItemValueError(MessageFormat.format("Data type is currently unsupported: {0}", dataTypeAsInt));
		}

		int checkDataType = valuePtr.getShort(0) & 0xffff;
		Pointer valueDataPtr = valuePtr.share(2);
		int valueDataLength = valueLength - 2;
		
		if (checkDataType!=dataTypeAsInt) {
			throw new IllegalStateException(MessageFormat.format("Value data type does not meet expected date type: found {0}, expected {1}", checkDataType,
					dataTypeAsInt));
		}
		switch(type) {
		case TYPE_TEXT: {
			String txtVal = (String) ItemDecoder.decodeTextValue(valueDataPtr, valueDataLength, false);
			return txtVal==null ? Collections.emptyList() : Arrays.asList((Object) txtVal);
		}
		case TYPE_TEXT_LIST: {
			List<Object> textList = valueDataLength==0 ? Collections.emptyList() : ItemDecoder.decodeTextListValue(valueDataPtr, false);
			return textList==null ? Collections.emptyList() : textList;
		}
		case TYPE_NUMBER: {
			double numVal = ItemDecoder.decodeNumber(valueDataPtr, valueDataLength);
			return Arrays.asList((Object) Double.valueOf(numVal));
		}
		case TYPE_NUMBER_RANGE: {
			List<Object> numberList = ItemDecoder.decodeNumberList(valueDataPtr, valueDataLength);
			return numberList==null ? Collections.emptyList() : numberList;
		}
		case TYPE_TIME: {
			DominoDateTime td = ItemDecoder.decodeTimeDateAsNotesTimeDate(valueDataPtr, valueDataLength);
			return td==null ? Collections.emptyList() : Arrays.asList((Object) td);
		}
		case TYPE_TIME_RANGE: {
			List<Object> tdValues = ItemDecoder.decodeTimeDateListAsNotesTimeDate(valueDataPtr);
			return tdValues==null ? Collections.emptyList() : tdValues;
		}
		case TYPE_UNAVAILABLE: {
			//e.g. returned by formula "@DeleteDocument"
			return Collections.emptyList();
		}
		case TYPE_ERROR: {
			if (valueLength>=2) {
				short formulaErrorCode = valueDataPtr.getShort(0);
				String errMsg = NotesErrorUtils.errToString(formulaErrorCode);
				throw new DominoException(formulaErrorCode, MessageFormat.format("Could not evaluate formula: {0}\nError: {1}", m_formula, errMsg));
			}
			else {
				throw new DominoException(0, MessageFormat.format("Could not evaluate formula: {0}", m_formula));
			}
		}
		default:
			throw new UnsupportedItemValueError(MessageFormat.format("Data is currently unsupported: {0}", dataTypeAsInt));
		}
	}

	/**
	 * Evaluates the formula on a note
	 * 
	 * @param doc document
	 * @return formula computation result
	 */
	@Override
	public List<Object> evaluate(Document doc) {
		return evaluateExt(doc).getValue();
	}

	/**
	 * Evaluates the formula on a document. Provides extended information.
	 * 
	 * @param doc document to be used as additional variables available to the formula or null
	 * @return formula computation result with flags
	 */
	@Override
	public FormulaExecutionResult evaluateExt(Document doc) {
		checkDisposed();

		JNADocument ctxDoc = null;

		if (doc!=null) {
			if (!(doc instanceof JNADocument)) {
				throw new IncompatibleImplementationException(doc, JNADocument.class);
			}
			ctxDoc = (JNADocument) doc;

			if (ctxDoc.isDisposed()) {
				throw new ObjectDisposedException(this);
			}
		}

		ShortByReference retResultLength = new ShortByReference();
		IntByReference retNoteMatchesFormula = new IntByReference();
		IntByReference retNoteShouldBeDeleted = new IntByReference();
		IntByReference retNoteModified = new IntByReference();

		DHANDLE ctxDocHandle = ctxDoc!=null ? ((JNADocumentAllocations)ctxDoc.getAdapter(APIObjectAllocations.class)).getNoteHandle() : null;

		return LockUtil.lockHandles(
				ctxDocHandle,
				getAllocations().getComputeHandle(),
				(optDocHandle, computeHdlByVal) -> {

					DHANDLE.ByReference rethResult = DHANDLE.newInstanceByReference();

					short result = NotesCAPI.get().NSFComputeEvaluate(computeHdlByVal, optDocHandle, rethResult,
							retResultLength, retNoteMatchesFormula, retNoteShouldBeDeleted, retNoteModified);
					NotesErrorUtils.checkResult(result);

					return LockUtil.lockHandle(rethResult, (resultHdlByVal) -> {
						if (resultHdlByVal==null || resultHdlByVal.isNull()) {
							throw new IllegalStateException("got a null handle as computation result");
						}
						int valueLength = retResultLength.getValue() & 0xffff;

						Pointer valuePtr = Mem.OSLockObject(resultHdlByVal);
						try {
							List<Object> formulaResult = parseFormulaResult(valuePtr, valueLength);

							return new FormulaExecutionResultImpl(formulaResult, retNoteMatchesFormula.getValue()==1,
									retNoteShouldBeDeleted.getValue()==1, retNoteModified.getValue()==1);
						}
						finally {
							Mem.OSUnlockObject(resultHdlByVal);
							short localResult = Mem.OSMemFree(resultHdlByVal);
							NotesErrorUtils.checkResult(localResult);
						}
					});

				}
				);
	}
	
	private static class FormulaExecutionResultImpl implements FormulaExecutionResult {
		private List<Object> m_result;
		private boolean m_matchesFormula;
		private boolean m_shouldBeDeleted;
		private boolean m_docModified;
		
		public FormulaExecutionResultImpl(List<Object> result, boolean matchesFormula, boolean shouldBeDeleted, boolean noteModified) {
			m_result = result;
			m_matchesFormula = matchesFormula;
			m_shouldBeDeleted = shouldBeDeleted;
			m_docModified = noteModified;
		}
		
		@Override
		public List<Object> getValue() {
			return m_result;
		}
		
		@Override
		public boolean matchesFormula() {
			return m_matchesFormula;
		}
		
		@Override
		public boolean shouldBeDeleted() {
			return m_shouldBeDeleted;
		}
		
		@Override
		public boolean isDocModified() {
			return m_docModified;
		}

		@Override
		public String toString() {
			return MessageFormat.format(
				"FormulaExecutionResult [result={0}, matchesFormula={1}, shouldBeDeleted={2}, docModified={3}]", //$NON-NLS-1$
				m_result, m_matchesFormula, m_shouldBeDeleted, m_docModified
			);
		}
	}

	@Override
	public List<Object> evaluate() {
		return evaluateExt(null).getValue();
	}

	@Override
	public String evaluateAsString() {
		List<Object> result = evaluate();
		if (result.isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		else {
			return result.get(0).toString();
		}
	}

	@Override
	public Double evaluateAsNumber(Double defaultValue) {
		return evaluateAsNumber(null, defaultValue);
	}
	
	@Override
	public String evaluateAsString(Document doc) {
		List<Object> result = evaluate(doc);
		if (result.isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		else {
			return StringUtil.toString(result.get(0));
		}
	}

	@Override
	public Double evaluateAsNumber(Document doc, Double defaultValue) {
		List<Object> result = evaluate(doc);
		if (!result.isEmpty()) {
			if (result.get(0) instanceof Number) {
				return ((Number)result.get(0)).doubleValue();
			}
		}
		
		return defaultValue;
	}


}
