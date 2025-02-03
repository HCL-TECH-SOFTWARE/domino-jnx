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
package com.hcl.domino.jna.naming;

import static com.hcl.domino.commons.util.NotesErrorUtils.checkResult;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.INotesCAPI;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.naming.UserDirectory;
import com.hcl.domino.naming.UserDirectoryQuery;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ShortByReference;

/**
 * @author Jesse Gallagher
 * @since 1.0.2
 */
public class JNAUserDirectory implements UserDirectory {
  private static String localEnvPrimaryDirectoryPath;
  
	private final JNADominoClient client;
	private final String serverName;
	
	public JNAUserDirectory(JNADominoClient client, String serverName) {
		this.client = client;
		this.serverName = serverName;
	}

	@Override
	public Optional<String> getPrimaryDirectoryPath() {
	  if (StringUtil.isEmpty(serverName)) {
	    synchronized (JNAUserDirectory.class) {
	      if (localEnvPrimaryDirectoryPath!=null) {
	        //return cached path for local environment if possible
	        return Optional.of(localEnvPrimaryDirectoryPath);
	      }
	    }
	  }
	  
    final INotesCAPI capi = NotesCAPI.get();
    final ShortByReference wEntryLen = new ShortByReference();
    final ShortByReference wCount = new ShortByReference();
    final DHANDLE.ByReference hReturn = DHANDLE.newInstanceByReference();
    
    try {
      try(DisposableMemory chPrimaryNamePtr = new DisposableMemory(NotesConstants.MAXPATH + 1)) {
        final Memory serverNameMem = NotesStringUtils.toLMBCS(serverName, true);
        final short status = capi.NAMEGetAddressBooks(serverNameMem, NotesConstants.NAME_GET_AB_FIRSTONLY, wCount, wEntryLen, hReturn);
        NotesErrorUtils.checkResult(status);

        if (wCount.getValue() != 0) {
          LockUtil.lockHandle(hReturn, (hReturnByVal) -> {
            return Mem.OSLockObject(hReturnByVal, (pszReturn) -> {
              short status2 = capi.OSPathNetParse(pszReturn, null,
                  null, chPrimaryNamePtr);
              NotesErrorUtils.checkResult(status2);

              final short wNameLen = capi.Cstrlen(chPrimaryNamePtr);
              final ShortByReference wTypePos = new ShortByReference();
              wTypePos.setValue(wNameLen);

              status2 =
                  capi.OSPathFileType(chPrimaryNamePtr, wTypePos);
              NotesErrorUtils.checkResult(status2);

              if (wNameLen == wTypePos.getValue()) {
                /* no file type specified */
                Memory dbTypeMem = NotesStringUtils.toLMBCS(NotesConstants.DBTYPE, true);
                capi.Cstrncat(chPrimaryNamePtr, dbTypeMem,
                    (NotesConstants.MAXPATH - 1));
              }
              capi.OSLocalizePath(chPrimaryNamePtr);

              return INotesErrorConstants.NOERROR;
            });
          });
          
          final int strlen = NotesStringUtils.getNullTerminatedLength(chPrimaryNamePtr);
          String path = NotesStringUtils.fromLMBCS(chPrimaryNamePtr, strlen);
          
          if (!StringUtil.isEmpty(path)) {
            if (StringUtil.isEmpty(serverName)) {
              //cache path for local environment
              synchronized (JNAUserDirectory.class) {
                localEnvPrimaryDirectoryPath = path;
              }
            }
            return Optional.of(path);
          }
        }

        return Optional.empty();
      }
    } finally {
      if (!hReturn.isNull()) {
        final short result = LockUtil.lockHandle(hReturn, (hReturnByVal) -> {
          return Mem.OSMemFree(hReturnByVal);
        });
        NotesErrorUtils.checkResult(result);
      }
    }
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
		
		return LockUtil.lockHandle(hReturn, hReturnByVal -> {
      int count = returnCount.getValue();
		  Set<String> result = new LinkedHashSet<>(count);
		  
			Mem.OSLockObject(hReturnByVal, ptr -> {
				Pointer strPtr = ptr.share(0);
				for(int i = 0; i < count; i++) {
					int strlen = NotesStringUtils.getNullTerminatedLength(strPtr);
					String path = NotesStringUtils.fromLMBCS(strPtr, strlen);
					if(StringUtil.isNotEmpty(path)) {
						result.add(path);
					}
					
					strPtr = strPtr.share(strlen+1);
				}
				
				return null;
			});
			
			Mem.OSMemFree(hReturnByVal);
			
			return result;
		});
	}

	@Override
	public UserDirectoryQuery query() {
		return new JNAUserDirectoryQuery(client, serverName);
	}

}
