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
package com.hcl.domino.jna.internal;

import static com.hcl.domino.commons.util.NotesErrorUtils.checkResult;

import java.util.Arrays;
import java.util.Optional;

import com.hcl.domino.commons.design.agent.DefaultAgentLastRunInfo;
import com.hcl.domino.commons.misc.ODSTypes;
import com.hcl.domino.data.Database;
import com.hcl.domino.design.DesignAgent.LastRunInfo;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.richtext.structures.AssistRunInfo;
import com.hcl.domino.richtext.structures.AssistRunObjectEntry;
import com.hcl.domino.richtext.structures.AssistRunObjectHeader;
import com.hcl.domino.richtext.structures.ObjectDescriptor;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public enum AgentRunInfoDecoder {
  ;
  
  /**
   * Decodes the information found in an $AssistRunInfo item.
   * 
   * <p>The provided pointer is expected to start _after_ the data-type WORD if it
   * comes from an item.</p>
   * 
   * @param database parent database
   * @param valuePtr a {@link Pointer} to the start of the run info data
   * @param valueLen the length of the run info in memory
   * @return an {@link Optional} describing the {@link LastRunInfo} instance for the data,
   *         or an empty one if the object is empty
   */
  public static Optional<LastRunInfo> decodeAgentRunInfo(Database database, Pointer valuePtr, int valueLen) {
    ObjectDescriptor objDescriptor = JNAMemoryUtils.readStructure(ObjectDescriptor.class, valuePtr);
    Optional<ObjectDescriptor.ObjectType> objectType = objDescriptor.getObjectType();
    if(!objectType.isPresent()) {
      return Optional.empty();
    }
    
    HANDLE.ByReference hDb = database.getAdapter(HANDLE.ByReference.class);
    IntByReference objectSize = new IntByReference();
    checkResult(LockUtil.lockHandle(hDb, dbHandleByVal ->
      NotesCAPI.get().NSFDbGetObjectSize(
          dbHandleByVal,
        objDescriptor.getRRV(),
        objectType.get().getValue().shortValue(),
        objectSize,
        null,
        null
      )
    ));
    if(objectSize.getValue() == 0) {
      return Optional.empty();
    }
    
    // NB: declared as array to avoid final-in-closure restrictions
    int[] offset = new int[] { 0 };
    
    DHANDLE.ByReference rethBuffer = DHANDLE.newInstanceByReference();

    // Get a handle to the header object and read it into a structure
    int headerLen = NotesCAPI.get().ODSLength(ODSTypes._ODS_ASSISTRUNOBJECTHEADER);
    checkResult(LockUtil.lockHandle(hDb, (dbHandleByVal) ->
      NotesCAPI.get().NSFDbReadObject(
        dbHandleByVal,
        objDescriptor.getRRV(),
        offset[0], 
        headerLen,
        rethBuffer
      )
    ));
    AssistRunObjectHeader header = LockUtil.lockHandle(rethBuffer, hBuffer -> {
      try {
        return Mem.OSLockObject(hBuffer, pObject -> {
          return JNAMemoryUtils.odsReadMemory(pObject, ODSTypes._ODS_ASSISTRUNOBJECTHEADER, AssistRunObjectHeader.class);
        });
      } finally {
        Mem.OSMemFree(hBuffer);
      }
    });
    offset[0] += headerLen;
    
    // Next, read the entries if needed
    int entryCount = header.getEntries();
    if(entryCount == 0) {
      return Optional.empty();
    }
    int entryLen = NotesCAPI.get().ODSLength(ODSTypes._ODS_ASSISTRUNOBJECTENTRY);
    
    // Read all entry objects into our handle
    checkResult(LockUtil.lockHandle(hDb, (dbHandleByVal) ->
      NotesCAPI.get().NSFDbReadObject(
        dbHandleByVal,
        objDescriptor.getRRV(),
        offset[0], 
        entryLen * header.getEntries(),
        rethBuffer
      )
    ));
    
    AssistRunObjectEntry[] entries = new AssistRunObjectEntry[entryCount];
    boolean shouldContinue = LockUtil.lockHandle(rethBuffer, hBuffer ->
      Mem.OSLockObject(hBuffer, pObject -> {
        PointerByReference ppObject = new PointerByReference(pObject);
        
        // Read one to make sure it exists
        entries[0] = JNAMemoryUtils.odsReadMemory(ppObject, ODSTypes._ODS_ASSISTRUNOBJECTENTRY, AssistRunObjectEntry.class);
        if(entries[0].getLength() == 0) {
          return false;
        }
        
        // If we're here, we know at least two more exist
        entries[1] = JNAMemoryUtils.odsReadMemory(ppObject, ODSTypes._ODS_ASSISTRUNOBJECTENTRY, AssistRunObjectEntry.class);
        entries[2] = JNAMemoryUtils.odsReadMemory(ppObject, ODSTypes._ODS_ASSISTRUNOBJECTENTRY, AssistRunObjectEntry.class);
        
        // If the third entry has zero length, then that means the agent hasn't actually run
        if(entries[2].getLength() == 0) {
          return false;
        }
        
        // Read in the remaining objects
        for(int i = 3; i < entryCount; i++) {
          entries[i] = JNAMemoryUtils.odsReadMemory(ppObject, ODSTypes._ODS_ASSISTRUNOBJECTENTRY, AssistRunObjectEntry.class);
        }
        
        return true;
      })
    );
    if(!shouldContinue) {
      return Optional.empty();
    }
    offset[0] += entryLen * header.getEntries();
    
    
    // Next up is the run info struct
    int runInfoLen = NotesCAPI.get().ODSLength(ODSTypes._ODS_ASSISTRUNINFO);
    
    checkResult(LockUtil.lockHandle(hDb, (dbHandleByVal) ->
      NotesCAPI.get().NSFDbReadObject(
        dbHandleByVal,
        objDescriptor.getRRV(),
        offset[0], 
        runInfoLen,
        rethBuffer
      )
    ));
    
    AssistRunInfo info = LockUtil.lockHandle(rethBuffer, hBuffer -> {
      try {
        return Mem.OSLockObject(hBuffer, pObject -> {
          return JNAMemoryUtils.odsReadMemory(pObject, ODSTypes._ODS_ASSISTRUNINFO, AssistRunInfo.class);
        });
      } finally {
        Mem.OSMemFree(hBuffer);
      }
    });
    offset[0] += runInfoLen;
    
    
    // Finally, the variable data referenced by the entry lengths
    byte[][] varData = new byte[entryCount][];
    for(int i = 0; i < entryCount; i++) {
      long thisEntryLen = entries[i].getLength();
      if(thisEntryLen == 0) {
        varData[i] = new byte[0];
      } else {
        checkResult(LockUtil.lockHandle(hDb, (dbHandleByVal) ->
          NotesCAPI.get().NSFDbReadObject(
            dbHandleByVal,
            objDescriptor.getRRV(),
            offset[0], 
            (int)thisEntryLen,
            rethBuffer
          )
        ));
        
        varData[i] = LockUtil.lockHandle(rethBuffer, hBuffer -> {
          try {
            return Mem.OSLockObject(hBuffer, pObject -> {
              return pObject.getByteArray(0, (int)thisEntryLen);
            });
          } finally {
            Mem.OSMemFree(hBuffer);
          }
        });
      }
      offset[0] += thisEntryLen;
    }
    
    return Optional.of(new DefaultAgentLastRunInfo(header, Arrays.asList(entries), info, Arrays.asList(varData)));
  }

}
