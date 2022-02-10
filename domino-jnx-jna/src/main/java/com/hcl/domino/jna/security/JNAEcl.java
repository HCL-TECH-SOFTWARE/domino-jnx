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
package com.hcl.domino.jna.security;

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.hcl.domino.DominoClient.ECLType;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.data.Document;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.data.JNAUserNamesList;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADocumentAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAEclAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAUserNamesListAllocations;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.security.ECLCapability;
import com.hcl.domino.security.Ecl;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * JNA implementation of {@link Ecl} to read and modify the execution control list.
 * 
 * @author Karsten Lehmann
 */
public class JNAEcl extends BaseJNAAPIObject<JNAEclAllocations> implements Ecl {
	private ECLType m_eclType;
	private JNAUserNamesList m_namesList;
	private Set<ECLCapability> m_capabilities;
	private boolean m_canModifyEcl;
	
	public JNAEcl(IAPIObject<?> parent, ECLType eclType, String userName) {
		super(parent);
		m_eclType = eclType;
		m_namesList = NotesNamingUtils.buildNamesList(this, NotesNamingUtils.toCanonicalName(userName));
		
		readCapabilities();
		
		setInitialized();
	}

	public JNAEcl(IAPIObject<?> parent, ECLType eclType, List<String> namesList) {
		super(parent);
		m_eclType = eclType;
		m_namesList = NotesNamingUtils.writeNewNamesList(this, NotesNamingUtils.toCanonicalNames(namesList));
		
		readCapabilities();

		setInitialized();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNAEclAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {

		return new JNAEclAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public UserNamesList getNamesList() {
		return m_namesList;
	}
	
	@Override
	public Set<ECLCapability> getCapabilities() {
		return m_capabilities;
	}
	
	@Override
	public boolean isEditable() {
		return m_canModifyEcl;
	}
	
	public void readCapabilities() {
		ShortByReference retwCapabilities = new ShortByReference();
		ShortByReference retwCapabilities2 = new ShortByReference();
		IntByReference retfUserCanModifyECL = new IntByReference();
		
		if (m_namesList.isDisposed()) {
			throw new ObjectDisposedException(m_namesList);
		}
		JNAUserNamesListAllocations namesListAllocations = (JNAUserNamesListAllocations) m_namesList.getAdapter(APIObjectAllocations.class);
		
		short result = LockUtil.lockHandle(namesListAllocations.getHandle(), (hNamesListByVal) -> {
			Pointer pNamesList = Mem.OSLockObject(hNamesListByVal);
			try {
				return NotesCAPI.get().ECLGetListCapabilities(pNamesList,
						m_eclType.getTypeAsShort(), retwCapabilities2, retwCapabilities2, retfUserCanModifyECL);
			}
			finally {
				Mem.OSUnlockObject(hNamesListByVal);
			}
		});
		NotesErrorUtils.checkResult(result);
		
		int retwCapabilitiesAsInt = retwCapabilities.getValue() & 0xffff;
		int retwCapabilities2AsInt = retwCapabilities2.getValue() & 0xffff;
		
		m_capabilities = toCapabilitySet(retwCapabilitiesAsInt, retwCapabilities2AsInt);
		m_canModifyEcl = retfUserCanModifyECL.getValue()==1;
	}

	private static EnumSet<ECLCapability> toCapabilitySet(int wCapabilitiesAsInt, int wCapabilities2AsInt) {
		EnumSet<ECLCapability> set = EnumSet.noneOf(ECLCapability.class);
		
		for (ECLCapability currCapability : ECLCapability.values()) {
			int currCapabilityInt = currCapability.getValue();
			
			if (currCapability.isWorkstationECL()) {
				if ((wCapabilities2AsInt & currCapabilityInt) == currCapabilityInt) {
					set.add(currCapability);
				}
			}
			else {
				if ((wCapabilitiesAsInt & currCapabilityInt) == currCapabilityInt) {
					set.add(currCapability);
				}
			}
		}

		return set;
	}
	
	@Override
	public Set<ECLCapability> trustNoSignatureUser(ECLType type, Collection<ECLCapability> capabilities, boolean sessionOnly) {
		return internalTrustSigner(null, type, capabilities, sessionOnly);
	}

	@Override
	public Set<ECLCapability> trustSignerOfDocument(Document doc, ECLType type, Collection<ECLCapability> capabilities,
			boolean sessionOnly) {
		Objects.requireNonNull(doc, "Document cannot be null");
		
		return internalTrustSigner(doc, type, capabilities, sessionOnly);
	}

	public static short toBitMaskNotExtendedFlags(Collection<ECLCapability> capabilitySet) {
		int result = 0;
		if (capabilitySet!=null) {
			for (ECLCapability currCapability : ECLCapability.values()) {
				if (!currCapability.isWorkstationECL()) {
					if (capabilitySet.contains(currCapability)) {
						result = result | currCapability.getValue();
					}
				}
			}
		}
		return (short) (result & 0xffff);
	}

	public static short toBitMaskExtendedFlags(Collection<ECLCapability> capabilitySet) {
		int result = 0;
		if (capabilitySet!=null) {
			for (ECLCapability currCapability : ECLCapability.values()) {
				if (currCapability.isWorkstationECL()) {
					if (capabilitySet.contains(currCapability)) {
						result = result | currCapability.getValue();
					}
				}
			}
		}
		return (short) (result & 0xffff);
	}
	/**
	 * Internal method with shared code
	 * 
	 * @param doc signed document (we read $Signature internally) or null to use "-No signature-" entry
	 * @param type ECL type
	 * @param capabilities capabilities to trust
	 * @param sessionOnly true to not permanently change the ECL
	 * @return new capabilities
	 */
	private static EnumSet<ECLCapability> internalTrustSigner(Document doc, ECLType type, Collection<ECLCapability> capabilities, boolean sessionOnly) {
		if (doc instanceof JNADocument && ((JNADocument)doc).isDisposed()) {
			throw new ObjectDisposedException(doc);
		}
		
		ShortByReference retwCapabilities = new ShortByReference();
		ShortByReference retwCapabilities2 = new ShortByReference();

		short wCapabilities = toBitMaskNotExtendedFlags(capabilities);
		short wCapabilities2 = toBitMaskExtendedFlags(capabilities);
		
		short result;
		
		boolean freeCtx;
		IntByReference rethCESCTX = new IntByReference();
		if (doc!=null) {
			JNADocumentAllocations docAllocations = (JNADocumentAllocations) doc.getAdapter(APIObjectAllocations.class);
			
			result = LockUtil.lockHandle(docAllocations.getNoteHandle(), (hNoteByVal) -> {
				return NotesCAPI.get().CESCreateCTXFromNote(hNoteByVal, rethCESCTX);
			});
			freeCtx = true;
		}
		else {
			result = NotesCAPI.get().CESGetNoSigCTX(rethCESCTX);
			freeCtx = false;
		}
		NotesErrorUtils.checkResult(result);				

		try {
			result = NotesCAPI.get().ECLUserTrustSigner(rethCESCTX.getValue(), type.getTypeAsShort(),
					(short) (sessionOnly ? 1 : 0), wCapabilities, wCapabilities2, retwCapabilities, retwCapabilities2);
			NotesErrorUtils.checkResult(result);

			
			int retwCapabilitiesAsInt = retwCapabilities.getValue() & 0xffff;
			int retwCapabilities2AsInt = retwCapabilities2.getValue() & 0xffff;
			
			EnumSet<ECLCapability> set = toCapabilitySet(retwCapabilitiesAsInt, retwCapabilities2AsInt);

			return set;
		}
		finally {
			if (freeCtx) {
				NotesCAPI.get().CESFreeCTX(rethCESCTX.getValue());
			}
		}
	}

	@Override
	public String toStringLocal() {
		return MessageFormat.format(
			"JNAEcl [ecltype={0}, nameslist={1}, iseditable={2}, capabilities={3}]", //$NON-NLS-1$
			m_eclType, m_namesList, m_canModifyEcl, m_capabilities
		);
	}
	
}
