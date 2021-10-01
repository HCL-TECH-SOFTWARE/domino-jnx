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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.hcl.domino.commons.design.NativeDesignSupport;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.data.JNAItem;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADocumentAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.Pair;
import com.hcl.domino.richtext.structures.ObjectDescriptor;
import com.hcl.domino.richtext.structures.ObjectDescriptor.ObjectType;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

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

  private final byte[] initialRunInfoData = new byte[] {
      0x00, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00,
      0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x10, 0x20, 0x43, 0x00,
      0x61, (byte) 0x87, 0x25, (byte) 0xc1, (byte) 0x8b, 0x44, 0x34, 0x00,
      0x2d, (byte) 0x87, 0x25, (byte) 0xc1, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
  };

  @Override
  public void initAgentRunInfo(Document doc) {
    JNADatabase db = (JNADatabase) doc.getParentDatabase();
    JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) db.getAdapter(APIObjectAllocations.class);
    dbAllocations.checkDisposed();
    
    JNADocumentAllocations docAllocations = (JNADocumentAllocations) ((JNADocument)doc).getAdapter(APIObjectAllocations.class);
    docAllocations.checkDisposed();

    //TODO replace this hardcoded run info data with code that writes AssistRunObjectHeader, AssistRunObjectEntry and AssistRunInfo
    byte[] runInfoDataWithSpace = new byte[1032];
    Arrays.fill(runInfoDataWithSpace, (byte) 0xaa);
    System.arraycopy(initialRunInfoData, 0, runInfoDataWithSpace, 0, initialRunInfoData.length);
    
    short noteClass = DocumentClass.DOCUMENT.getValue();
    short privs = 0;
    ObjectType objectType = ObjectType.ASSIST_RUNDATA;
    
    final DHANDLE.ByReference retCopyBufferHandle = DHANDLE.newInstanceByReference();
    short result = Mem.OSMemAlloc((short) 0, runInfoDataWithSpace.length, retCopyBufferHandle);
    NotesErrorUtils.checkResult(result);

    int rrv = LockUtil.lockHandles(dbAllocations.getDBHandle(), docAllocations.getNoteHandle(), retCopyBufferHandle,
        (hDbByVal, hNoteByVal, hCopyBufferByVal) -> {

          IntByReference rtnRRV = new IntByReference();

          short allocObjResult = NotesCAPI.get().NSFDbAllocObjectExtended2(hDbByVal,
              runInfoDataWithSpace.length,
              noteClass, privs, objectType.getValue(), rtnRRV);
          NotesErrorUtils.checkResult(allocObjResult);

          //copy buffer array data into memory buffer
          Pointer ptrBuffer = Mem.OSLockObject(hCopyBufferByVal);
          try {
            ptrBuffer.write(0, runInfoDataWithSpace, 0, runInfoDataWithSpace.length);
          }
          finally {
            Mem.OSUnlockObject(hCopyBufferByVal);
          }

          short writeObjResult = NotesCAPI.get().NSFDbWriteObject(
              hDbByVal,
              rtnRRV.getValue(),
              hCopyBufferByVal,
              0,
              runInfoDataWithSpace.length);
          NotesErrorUtils.checkResult(writeObjResult);

          Mem.OSMemFree(hCopyBufferByVal);

          return rtnRRV.getValue();
        });

    ObjectDescriptor objDescriptor = MemoryStructureUtil.newStructure(ObjectDescriptor.class, 0);
    objDescriptor.setRRV(rrv);
    objDescriptor.setObjectType(objectType);
    
    ByteBuffer objDescriptorData = objDescriptor.getData();
    ByteBuffer objDescriptorDataWithType = ByteBuffer.allocate(2 + objDescriptorData.limit());
    objDescriptorDataWithType.putShort(ItemDataType.TYPE_OBJECT.getValue());
    objDescriptorDataWithType.put(objDescriptorData);
    objDescriptorDataWithType.position(0);
    
    doc.replaceItemValue(NotesConstants.ASSIST_RUNINFO_ITEM, EnumSet.of(ItemFlag.SUMMARY), objDescriptorDataWithType);
  }
}
