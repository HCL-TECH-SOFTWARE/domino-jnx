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
package com.hcl.domino.jna.person;

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.time.temporal.TemporalAccessor;

import com.hcl.domino.commons.data.DefaultDominoDateRange;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNAOOOContextAllocations;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.Ref;
import com.hcl.domino.person.OutOfOffice;
import com.hcl.domino.person.Person;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

public class JNAOutOfOffice extends BaseJNAAPIObject<JNAOOOContextAllocations> implements OutOfOffice {
	
	public JNAOutOfOffice(JNAPerson person, IAdaptable adaptable) {
		super(person);
		
		Integer hOOOContext = adaptable.getAdapter(Integer.class);
		if (hOOOContext==null) {
			throw new IllegalArgumentException("Missing C handle for OOO context");
		}
		Pointer pOOOContext = adaptable.getAdapter(Pointer.class);
		if (pOOOContext==null) {
			throw new IllegalArgumentException("Missing C pointer for OOO context");
		}
		
		getAllocations().init(hOOOContext, pOOOContext);
		
		setInitialized();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNAOOOContextAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNAOOOContextAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	@Override
	public Person getParentPerson() {
		return (Person) getParent();
	}
	
	@Override
	public DominoDateRange getAwayPeriod() {
		checkDisposed();
		
		NotesTimeDateStruct tdStartAwayStruct = NotesTimeDateStruct.newInstance();
		NotesTimeDateStruct tdEndAwayStruct = NotesTimeDateStruct.newInstance();
		
		JNAOOOContextAllocations allocations = getAllocations();
		synchronized (allocations) {
			short result = NotesCAPI.get().OOOGetAwayPeriod(allocations.getOOOPointer(), tdStartAwayStruct, tdEndAwayStruct);
			NotesErrorUtils.checkResult(result);
			
		}
		
		DominoDateTime tdStartAway = new JNADominoDateTime(tdStartAwayStruct);
		DominoDateTime tdEndAway = new JNADominoDateTime(tdEndAwayStruct);
		
		return new DefaultDominoDateRange(tdStartAway, tdEndAway);
	}

	@Override
	public boolean isExcludeInternet() {
		checkDisposed();
		
		IntByReference bExcludeInternet = new IntByReference();
		
		JNAOOOContextAllocations allocations = getAllocations();
		synchronized (allocations) {
			short result = NotesCAPI.get().OOOGetExcludeInternet(allocations.getOOOPointer(), bExcludeInternet);
			NotesErrorUtils.checkResult(result);
		}
		
		return bExcludeInternet.getValue()==1;
	}

	@Override
	public void setExcludeInternet(boolean exclude) {
		checkDisposed();
		
		JNAOOOContextAllocations allocations = getAllocations();
		synchronized (allocations) {
			short result = NotesCAPI.get().OOOSetExcludeInternet(allocations.getOOOPointer(), exclude ? 1 : 0);
			NotesErrorUtils.checkResult(result);
		}
	}

	@Override
	public boolean isEnabled() {
		Ref<Boolean> retIsEnabled = new Ref<>();
		getState(null, retIsEnabled);
		return Boolean.TRUE.equals(retIsEnabled.get());
	}

	@Override
	public OOOType getType() {
		Ref<OOOType> retType = new Ref<>();
		getState(retType, null);
		return retType.get();
	}

	@Override
	public void getState(Ref<OOOType> retType, Ref<Boolean> retIsEnabled) {
		checkDisposed();

		ShortByReference retVersion = new ShortByReference();
		ShortByReference retState = new ShortByReference();

		JNAOOOContextAllocations allocations = getAllocations();
		synchronized (allocations) {
			short result = NotesCAPI.get().OOOGetState(allocations.getOOOPointer(), retVersion, retState);
			NotesErrorUtils.checkResult(result);
		}

		if (retType!=null) {
			if (retVersion.getValue() == 1) {
				retType.set(OOOType.AGENT);
			}
			else if (retVersion.getValue() == 2) {
				retType.set(OOOType.SERVICE);
			}
		}
		
		if (retIsEnabled!=null) {
			if (retState.getValue()==1) {
				retIsEnabled.set(Boolean.TRUE);
			}
			else {
				retIsEnabled.set(Boolean.FALSE);
			}
		}
	}

	@Override
	public String getGeneralSubject() {
		checkDisposed();
		
		DisposableMemory retSubject = new DisposableMemory(NotesConstants.OOOPROF_MAX_BODY_SIZE);
		try {
			JNAOOOContextAllocations allocations = getAllocations();
			synchronized (allocations) {
				short result = NotesCAPI.get().OOOGetGeneralSubject(allocations.getOOOPointer(), retSubject);
				NotesErrorUtils.checkResult(result);
			}
			
			String subject = NotesStringUtils.fromLMBCS(retSubject, -1);
			return subject;
		}
		finally {
			retSubject.dispose();
		}
	}

	@Override
	public String getGeneralMessage() {
		checkDisposed();
		
		JNAOOOContextAllocations allocations = getAllocations();
		synchronized (allocations) {
			//first get the length
			ShortByReference retGeneralMessageLen = new ShortByReference();
			
			short result = NotesCAPI.get().OOOGetGeneralMessage(allocations.getOOOPointer(), null, retGeneralMessageLen);
			NotesErrorUtils.checkResult(result);
			
			int iGeneralMessageLen = retGeneralMessageLen.getValue() & 0xffff;
			if (iGeneralMessageLen==0) {
				return ""; //$NON-NLS-1$
			}
			
			DisposableMemory retMessage = new DisposableMemory(iGeneralMessageLen + 1);
			try {
				result = NotesCAPI.get().OOOGetGeneralMessage(allocations.getOOOPointer(), retMessage, retGeneralMessageLen);
				NotesErrorUtils.checkResult(result);
				String msg = NotesStringUtils.fromLMBCS(retMessage, retGeneralMessageLen.getValue());
				return msg;
			}
			finally {
				retMessage.dispose();
			}
		}
	}

	@Override
	public void setAwayPeriod(TemporalAccessor tdStartAway, TemporalAccessor tdEndAway) {
		checkDisposed();
		
		JNAOOOContextAllocations allocations = getAllocations();
		synchronized (allocations) {
			NotesTimeDateStruct.ByValue tdStartWayByVal = NotesTimeDateStruct.ByValue.newInstance(JNADominoDateTime.from(tdStartAway).getInnards());
			NotesTimeDateStruct.ByValue tdEndWayByVal = NotesTimeDateStruct.ByValue.newInstance(JNADominoDateTime.from(tdEndAway).getInnards());
			
			short result = NotesCAPI.get().OOOSetAwayPeriod(allocations.getOOOPointer(), tdStartWayByVal, tdEndWayByVal);
			NotesErrorUtils.checkResult(result);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		checkDisposed();
		
		JNAOOOContextAllocations allocations = getAllocations();
		synchronized (allocations) {
			short result = NotesCAPI.get().OOOEnable(allocations.getOOOPointer(), enabled ? 1 : 0);
			NotesErrorUtils.checkResult(result);
		}
	}

	@Override
	public void setGeneralSubject(String subject, boolean displayReturnDate) {
		checkDisposed();
		
		Memory subjectMem = NotesStringUtils.toLMBCS(subject, true);
		
		JNAOOOContextAllocations allocations = getAllocations();
		synchronized (allocations) {
			short result = NotesCAPI.get().OOOSetGeneralSubject(allocations.getOOOPointer(), subjectMem, displayReturnDate ? 1 : 0);
			NotesErrorUtils.checkResult(result);
		}
	}

	@Override
	public void setGeneralMessage(String msg) {
		checkDisposed();
		
		Memory msgMem = NotesStringUtils.toLMBCS(msg, false);
		if (msgMem.size() > 65535) {
			throw new IllegalArgumentException(MessageFormat.format("Message exceeds max length, {0}> 65535 bytes", msgMem.size()));
		}
		
		JNAOOOContextAllocations allocations = getAllocations();
		synchronized (allocations) {
			short result = NotesCAPI.get().OOOSetGeneralMessage(allocations.getOOOPointer(), msgMem, (short) (msgMem.size() & 0xffff));
			NotesErrorUtils.checkResult(result);
		}
	}

	@Override
	public String toStringLocal() {
		return MessageFormat.format("JNAOOOContext [user={0}]", getParentPerson()); //$NON-NLS-1$
	}

	
}