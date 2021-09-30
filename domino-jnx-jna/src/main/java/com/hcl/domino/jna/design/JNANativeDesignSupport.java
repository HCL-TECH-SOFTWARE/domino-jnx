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
 */package com.hcl.domino.jna.design;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;

import com.hcl.domino.commons.design.NativeDesignSupport;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jna.data.JNAItem;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.Pair;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * 
 * @author Karsten Lehmann
 * @since 1.0.43
 */
public class JNANativeDesignSupport implements NativeDesignSupport {

  @Override
  public Pair<String,String> formatLSForDesigner(String code, String nameOfContextClass) {
    Memory codeMem = NotesStringUtils.toLMBCS(code, true, false); //add null terminator, keep newlines
    byte[] codeLMBCS = new byte[(int) codeMem.size()];
    codeMem.read(0, codeLMBCS, 0, codeLMBCS.length);
    DHANDLE.ByReference hSrcByRef = DHANDLE.newInstanceByReference();
    short result = Mem.OSMemAlloc((short) 0, codeLMBCS.length, hSrcByRef);
    NotesErrorUtils.checkResult(result);

    byte[] nameOfContextClassLMBCS;
    DHANDLE.ByReference phDataByRef = DHANDLE.newInstanceByReference();
    if (!StringUtil.isEmpty(nameOfContextClass)) {
      nameOfContextClassLMBCS = nameOfContextClass.getBytes(Charset.forName("LMBCS-nullterm")); //$NON-NLS-1$
      if (nameOfContextClassLMBCS.length > (NotesConstants.MAXIMUM_ID_NAME_LENGTH+1)) {
        throw new IllegalArgumentException(MessageFormat.format("Name of context class exceeds max length of {0} bytes", NotesConstants.MAXIMUM_ID_NAME_LENGTH));
      }
      
      // allocate space for SCRIPTCONTEXTDESCR structure that describes the context of the LS code,
      // e.g. it adds binding code for UI document/view/button
      
      // typedef struct {
      //   DWORD Length;
      //   char  szNameOfContextClass[MAXIMUM_ID_NAME_LENGTH + 1];
      // }SCRIPTCONTEXTDESCR;

      result = Mem.OSMemAlloc((short) 0, 4 + NotesConstants.MAXIMUM_ID_NAME_LENGTH +1, phDataByRef);
      NotesErrorUtils.checkResult(result);
    }
    else {
      nameOfContextClassLMBCS = null;
    }
    
    return LockUtil.lockHandles(hSrcByRef, phDataByRef, (hSrcByVal, phDataByVal) -> {
      try {
        //write code to memory block
        Mem.OSLockObject(hSrcByVal, (ptr) -> {
          ptr.write(0, codeLMBCS, 0, codeLMBCS.length);
          
          return null;
        });
        
        if (!StringUtil.isEmpty(nameOfContextClass) && !phDataByVal.isNull()) {
          //write name of context class to memory block if set
          Mem.OSLockObject(phDataByVal, (ptr) -> {
            ptr.setInt(0, 4 + NotesConstants.MAXIMUM_ID_NAME_LENGTH +1); //sizeof(SCRIPTCONTEXTDESCR)
            ptr.write(4, nameOfContextClassLMBCS, 0, nameOfContextClassLMBCS.length);
            
            return null;
          });
        }
        
        //now format the LS:
        DHANDLE.ByReference hDest = DHANDLE.newInstanceByReference();
        DHANDLE.ByReference hErrs = DHANDLE.newInstanceByReference();
        int dwFlags = 0;
        
        short resultFormat = NotesCAPI.get().AgentLSTextFormat(hSrcByVal, hDest,
            hErrs, dwFlags, phDataByRef);
        //AgentLSTextFormat only returns NOERROR or ERR_MEMORY in case it runs out of memory
        NotesErrorUtils.checkResult(resultFormat);
        
        String formattedCode = lockAndReadText(hDest, true);
        String errorTxt = lockAndReadText(hErrs, true);
        
        return new Pair<>(formattedCode, errorTxt);
      }
      finally {
        Mem.OSMemFree(hSrcByVal);
        
        if (phDataByVal!=null && !phDataByVal.isNull()) {
          Mem.OSMemFree(phDataByVal);
        }
      }
    });
  }

  private String lockAndReadText(DHANDLE.ByReference hdl, boolean free) {
    if (hdl.isNull()) {
      return ""; //$NON-NLS-1$
    }
    
    return LockUtil.lockHandle(hdl, (hdlByVal) -> {
      Pointer ptrTxt = Mem.OSLockObject(hdlByVal);
      try {
        return NotesStringUtils.fromLMBCS(ptrTxt, -1);
      }
      finally {
        Mem.OSUnlockObject(hdlByVal);
        if (free) {
          Mem.OSMemFree(hdlByVal);
        }
      }
    });
  }
  
  @Override
  public List<ByteBuffer> splitAsLMBCS(String txt, boolean addNull, boolean replaceLinebreaks, int chunkSize) {
    return NotesStringUtils.splitAsLMBCS(txt, addNull, replaceLinebreaks, chunkSize);
  }

  @Override
  public void setCDRecordItemType(Document doc, Item item, ItemDataType newType) {
    if (!isInCDRecordFormat(item.getType())) {
      throw new IllegalArgumentException(MessageFormat.format("Item is not in CD record format: {0}", item.getType()));
    }
    if (!isInCDRecordFormat(newType)) {
      throw new IllegalArgumentException(MessageFormat.format("New item type is not in CD record format: {0}", newType));
    }
    
    ((JNAItem)item).setItemType(newType);
  }
  
  private boolean isInCDRecordFormat(ItemDataType type) {
    return type==ItemDataType.TYPE_COMPOSITE ||
        type==ItemDataType.TYPE_QUERY ||
        type==ItemDataType.TYPE_ACTION;
  }
}
