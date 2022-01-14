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
package com.hcl.domino.jna;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.BaseAPIObject;
import com.hcl.domino.commons.gc.IAPIObject;

/**
 * Base class for all objects in the JNA API that have resources on the C API
 * side
 * 
 * @author Karsten Lehmann
 *
 * @param <AT> subclass of {@link APIObjectAllocations} that contain the C API
 *             handles and could to dispose them
 */
@SuppressWarnings("rawtypes")
public abstract class BaseJNAAPIObject<AT extends APIObjectAllocations> extends BaseAPIObject<AT> {
	private boolean m_initialized;

	public BaseJNAAPIObject(IAPIObject<?> parent) {
		super(parent);
	}

	@Override
  protected final void checkDisposed() {
		JNADominoProcess.checkThreadEnabledForDomino();

		AT allocations = getAllocations();
		if (allocations != null) {
			allocations.checkDisposed();
		}

		checkDisposedLocal();
	}

	/**
	 * Call this method in a subclass constructor to ensure that the API object has
	 * been initialized. We use this info to control whether {@link #toString()}
	 * returns the default Java return value or something meaningful (e.g. returning
	 * the handle value and data read from the handle).
	 */
	protected void setInitialized() {
		m_initialized = true;
	}

	protected boolean isInitialized() {
		return m_initialized;
	}

	/**
	 * Override this method and add your own disposed checks throwing a
	 * {@link DominoException} if resources are already disposed.<br>
	 * <br>
	 * Please note that we already check if allocations returned by
	 * {@link #getAllocations()} are disposed in the method {@link #checkDisposed()}
	 * that calls this method.<br>
	 * <br>
	 * The default implementation does nothing.
	 */
	protected void checkDisposedLocal() {
	}

	// @Override
	// public JNADominoClient getParentDominoClient() {
	// return (JNADominoClient)super.getParentDominoClient();
	// }

	/**
	 * Returns the allocations object created in
	 * <ul>
	 * <li>JNADominoClient#createAllocations</li>
	 * <li>APIObjectAllocations#createAllocations</li>
	 * <li>ReferenceQueue#createAllocations</li>
	 * </ul>
	 *
	 * @return allocations
	 */
	@Override
	protected final AT getAllocations() {
		JNADominoProcess.checkThreadEnabledForDomino();
		return super.getAllocations();
	}

	/**
	 * Overriding {@link #toString()} with a final method to ensure API objects are
	 * completely initialized until this method is called.<br>
	 * <br>
	 * It is expected that subclasses call {@link #setInitialized()} in their
	 * constructor as last method to ensure all is set up properly. Without calling
	 * this method, we just return the default Java {@link #toString()} result
	 * here.<br>
	 * <br>
	 * If {@link #setInitialized()} has been called, we return the result of
	 * {@link #toStringLocal()}.
	 * 
	 * @return string representation if the API object
	 */
	@Override
	public final String toString() {
		if (!m_initialized) {
			return super.toString();
		} else {
			try {
				return toStringLocal();
			} catch (Exception e) {
				StringWriter sWriter = new StringWriter();
				PrintWriter pWriter = new PrintWriter(sWriter);
				e.printStackTrace(pWriter);
				return "toStringLocal() caused error: " + sWriter.toString();
			}
		}
	}

	/**
	 * Returns a string representation of the object. Method is called by
	 * {@link BaseJNAAPIObject#toString()} if {@link #setInitialized()} has already
	 * been called, so it's safe to assume that all required handles have been
	 * read/stored.<br>
	 * If {@link #setInitialized()} has not been called,
	 * {@link BaseJNAAPIObject#toString()} falls back to the default method
	 * {@link Object#toString()}.
	 *
	 * @return a string representation of the object.
	 */
	protected String toStringLocal() {
		return super.toString();
	}
}
