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
package com.hcl.domino.jna.admin;

import java.lang.ref.ReferenceQueue;
import java.nio.file.Path;
import java.util.function.Function;

import com.hcl.domino.DominoException;
import com.hcl.domino.admin.idvault.UserId;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNAUserIDAllocations;
import com.hcl.domino.jna.utils.JNADominoUtils;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Container for an in-memory user ID fetched from the ID vault
 * 
 * @author Karsten Lehmann
 */
public class JNAUserId extends BaseJNAAPIObject<JNAUserIDAllocations> implements UserId {

	public JNAUserId(IAPIObject<?> parent, IAdaptable adaptable, boolean noDispose) {
		super(parent);
		PointerByReference phKFC = adaptable.getAdapter(PointerByReference.class);

		if (phKFC==null) {
			throw new DominoException("Unsupported adaptable parameter");
		}

		getAllocations().setIDHandle(phKFC);
		
		if (noDispose) {
			getAllocations().setNoDispose();
		}
		
		setInitialized();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNAUserIDAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		return new JNAUserIDAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public String getUsername() {
		checkDisposed();

		PointerByReference phKFC = getAllocations().getIDHandle();
		
		DisposableMemory retUsernameMem = new DisposableMemory(NotesConstants.MAXUSERNAME);

		short result;
		synchronized (phKFC) {
			result = NotesCAPI.get().SECKFMAccess((short) 32, phKFC.getValue(), retUsernameMem, null);
		}
		NotesErrorUtils.checkResult(result);

		String username = NotesStringUtils.fromLMBCS(retUsernameMem, -1);
		return username;
	}

	/**
	 * @param <T> the type of value to return
	 * @param consumer a {@link Function} processing the pointer from the ID
	 * @return the value produced by {@code consumer}
	 * @deprecated Use {@link JNADominoUtils#accessKFC(UserId, Function)}
	 */
	public <T> T accessKFC(Function<PointerByReference,T> consumer) {
		return JNADominoUtils.accessKFC(this, consumer);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <T> T getAdapterLocal(Class<T> clazz) {
		if(PointerByReference.class.equals(clazz)) {
			checkDisposed();
			return (T)getAllocations().getIDHandle();
		} else if(Long.class.equals(clazz)) {
			checkDisposed();
			return (T)Long.valueOf(Pointer.nativeValue(getAllocations().getIDHandle().getPointer()));
		}
		return null;
	}

	@Override
	public void makeSafeCopy(Path targetFilePath) {
		PointerByReference phKFC = getAllocations().getIDHandle();
		
		synchronized (phKFC) {
			checkDisposed();

			String targetFilePathStr = targetFilePath.toString();
			Memory targetFilePathMem = NotesStringUtils.toLMBCS(targetFilePathStr, true);
			
			NotesErrorUtils.checkResult(NotesCAPI.get().SECKFMMakeSafeCopy(phKFC.getValue(),
					NotesConstants.KFM_safecopy_Standard, (short) 0, targetFilePathMem));

		}
	}
	
	
}
