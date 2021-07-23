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
package com.hcl.domino.jna.naming;

import static com.hcl.domino.commons.util.NotesErrorUtils.checkResult;

import java.util.LinkedHashSet;
import java.util.Set;

import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.naming.UserDirectoryQuery;
import com.hcl.domino.naming.UserDirectory;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ShortByReference;

/**
 * @author Jesse Gallagher
 * @since 1.0.2
 */
public class JNAUserDirectory implements UserDirectory {
	private final JNADominoClient client;
	private final String serverName;
	
	public JNAUserDirectory(JNADominoClient client, String serverName) {
		this.client = client;
		this.serverName = serverName;
	}

	@Override
	public Set<String> getDirectoryPaths() {
		Memory server = NotesStringUtils.toLMBCS(serverName, true);
		ShortByReference returnCount = new ShortByReference();
		ShortByReference returnLength = new ShortByReference();
		
		DHANDLE.ByReference hReturn = DHANDLE.newInstanceByReference();
		checkResult(NotesCAPI.get().NAMEGetAddressBooks(
			server,
			(short)0,
			returnCount,
			returnLength,
			hReturn
		));
		
		return LockUtil.lockHandle(hReturn, hBuffer -> {
			return Mem.OSLockObject(hBuffer, ptr -> {
				int count = returnCount.getValue();
				Set<String> result = new LinkedHashSet<>(count);
				
				Pointer strPtr = ptr.share(0);
				for(int i = 0; i < count; i++) {
					int strlen = NotesStringUtils.getNullTerminatedLength(strPtr);
					String path = NotesStringUtils.fromLMBCS(strPtr, strlen);
					if(StringUtil.isNotEmpty(path)) {
						result.add(path);
					}
					
					strPtr = strPtr.share(strlen);
				}
				
				return result;
			});
		});
	}

	@Override
	public UserDirectoryQuery query() {
		return new JNAUserDirectoryQuery(client, serverName);
	}

}
