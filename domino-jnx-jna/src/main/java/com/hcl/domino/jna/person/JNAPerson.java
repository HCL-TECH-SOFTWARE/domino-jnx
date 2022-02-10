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
package com.hcl.domino.jna.person;

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;

import com.hcl.domino.DominoException;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAPersonAllocations;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.person.OutOfOffice;
import com.hcl.domino.person.Person;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class JNAPerson extends BaseJNAAPIObject<JNAPersonAllocations> implements Person {
	private String m_usernameCanonical;
	
	public JNAPerson(IGCDominoClient<?> parent, String username) {
		super(parent);
		
		m_usernameCanonical = NotesNamingUtils.toCanonicalName(username);
		
		setInitialized();
	}

	@Override
	public UserNamesList getUserNamesList(String server) {
		return NotesNamingUtils.buildNamesList(getParentDominoClient(), server, m_usernameCanonical);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNAPersonAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {

		return new JNAPersonAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public String toStringLocal() {
		return MessageFormat.format("JNAPerson [username={0}]", m_usernameCanonical); //$NON-NLS-1$
	}

	@Override
	public String getUsername() {
		return m_usernameCanonical;
	}

	@Override
	public OutOfOffice openOutOfOffice(String homeMailServer, boolean isHomeMailServer, Database dbMail) {
		if (dbMail!=null && !(dbMail instanceof JNADatabase)) {
			throw new DominoException(MessageFormat.format("Database must be a JNADatabase: {0}", dbMail.getClass().getName()));
		}

		if (isHomeMailServer && StringUtil.isEmpty(homeMailServer)) {
			throw new IllegalArgumentException("Home mail server cannot be empty if isHomeMailServer is set to true");
		}
		
		Memory mailOwnerNameMem = NotesStringUtils.toLMBCS(m_usernameCanonical, true);
		Memory homeMailServerMem = NotesStringUtils.toLMBCS(NotesNamingUtils.toCanonicalName(homeMailServer), true);

		short result = NotesCAPI.get().OOOInit();
		NotesErrorUtils.checkResult(result);

		try {
			IntByReference hOOOContext = new IntByReference();
			PointerByReference pOOOOContext = new PointerByReference();

			JNADatabaseAllocations dbMailAllocations = (JNADatabaseAllocations) (dbMail==null ? null : dbMail.getAdapter(APIObjectAllocations.class));
			HANDLE dbHandle = dbMailAllocations==null ? null : dbMailAllocations.getDBHandle();

			result = LockUtil.lockHandle(dbHandle, (hDbByVal) -> {
				return NotesCAPI.get().OOOStartOperation(mailOwnerNameMem,
						homeMailServerMem, isHomeMailServer ? 1 : 0, hDbByVal, hOOOContext,
								pOOOOContext);
			});
			NotesErrorUtils.checkResult(result);

			JNAOutOfOffice ctx = new JNAOutOfOffice(this, new IAdaptable() {
				@SuppressWarnings("unchecked")
				@Override
				public <T> T getAdapter(Class<T> clazz) {
					if (Integer.class == clazz) {
						return (T) Integer.valueOf(hOOOContext.getValue());
					}

					if (Pointer.class == clazz) {
						return (T) pOOOOContext.getValue();
					}

					return null;
				}
			});

			return ctx;
		}
		catch (Exception e) {
			short termResult = NotesCAPI.get().OOOTerm();
			NotesErrorUtils.checkResult(termResult);

			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			else {
				throw new DominoException(MessageFormat.format("Error opening Out-of-Office for user {0}", m_usernameCanonical), e);
			}
		}
	}


}
