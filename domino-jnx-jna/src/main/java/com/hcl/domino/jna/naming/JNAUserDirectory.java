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

import static com.hcl.domino.commons.data.types.JNXTypeUtil.word;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.data.types.WORD;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.data.types.WordByReference;
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
import com.hcl.domino.util.JNXStringUtil;
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
	  if (JNXStringUtil.isEmpty(serverName)) {
	    synchronized (JNAUserDirectory.class) {
	      if (localEnvPrimaryDirectoryPath!=null) {
	        //return cached path for local environment if possible
	        return Optional.of(localEnvPrimaryDirectoryPath);
	      }
	    }
	  }
	  
    final INotesCAPI capi = NotesCAPI.get();
    final WordByReference wEntryLen = new WordByReference();
    final WordByReference wCount = new WordByReference();
    final DHANDLE.ByReference hReturn = DHANDLE.newInstanceByReference();
    
    try {
      try(DisposableMemory chPrimaryNamePtr = new DisposableMemory(NotesConstants.MAXPATH + 1)) {
        final Memory serverNameMem = NotesStringUtils.toLMBCS(serverName, true);
        capi.NAMEGetAddressBooks(serverNameMem, word(NotesConstants.NAME_GET_AB_FIRSTONLY), wCount, wEntryLen, hReturn)
          .orElseThrow();

        if (wCount.getValue() != 0) {
          LockUtil.lockHandle(hReturn, (hReturnByVal) -> {
            return Mem.OSLockObject(hReturnByVal, (pszReturn) -> {
              capi.OSPathNetParse(pszReturn, null,
                  null, chPrimaryNamePtr).orElseThrow();

              final WORD wNameLen = capi.Cstrlen(chPrimaryNamePtr);
              final WordByReference wTypePos = new WordByReference();
              wTypePos.setValue(wNameLen.getValue());

              capi.OSPathFileType(chPrimaryNamePtr, wTypePos).orElseThrow();

              if ((short)wNameLen.getValue() == wTypePos.getValue()) {
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
          
          if (!JNXStringUtil.isEmpty(path)) {
            if (JNXStringUtil.isEmpty(serverName)) {
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
        LockUtil.lockHandle(hReturn, (hReturnByVal) -> {
          return Mem.OSMemFree(hReturnByVal).orElseThrow();
        });
      }
    }
	}
	
	@Override
	public Set<String> getDirectoryPaths() {
		Memory server = NotesStringUtils.toLMBCS(serverName, true);
		WordByReference returnCount = new WordByReference();
		WordByReference returnLength = new WordByReference();
		
		DHANDLE.ByReference hReturn = DHANDLE.newInstanceByReference();
		NotesCAPI.get().NAMEGetAddressBooks(
			server,
			word(0),
			returnCount,
			returnLength,
			hReturn
		);
		
		return LockUtil.lockHandle(hReturn, hReturnByVal -> {
      int count = returnCount.getValue();
		  Set<String> result = new LinkedHashSet<>(count);
		  
			Mem.OSLockObject(hReturnByVal, ptr -> {
				Pointer strPtr = ptr.share(0);
				for(int i = 0; i < count; i++) {
					int strlen = NotesStringUtils.getNullTerminatedLength(strPtr);
					String path = NotesStringUtils.fromLMBCS(strPtr, strlen);
					if(JNXStringUtil.isNotEmpty(path)) {
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
