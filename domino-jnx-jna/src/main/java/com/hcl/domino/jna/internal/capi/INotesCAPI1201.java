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
package com.hcl.domino.jna.internal.capi;

import com.hcl.domino.jna.internal.capi.INotesCAPI.UndocumentedAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * C API methods introduced in R12.0.1
 * 
 * @author Karsten Lehmann
 */
public interface INotesCAPI1201 extends Library {

  @UndocumentedAPI
  short NABLookupBasicAuthentication(Memory userName, Memory password, int dwFlags,
      int nMaxFullNameLen,
      Memory fullUserName);

  @UndocumentedAPI
  short NSFProcessResultsExt(HANDLE.ByValue hDb,
      Memory resultsname,
      int dwFlags,
      int hInResults,
      int hOutFields,
      int hFieldRules,
      int hCombineRules,
      DHANDLE.ByValue hReaders,
      int dwHoursTillExpire,
      IntByReference phErrorText,
      DHANDLE.ByReference phStreamedhQueue,
      ShortByReference phViewOpened,
      IntByReference pViewNoteID,
      int dwQRPTimeLimit, 
      int dwQRPEntriesLimit,  
      int dwQRPTimeCheckInterval);  


  @UndocumentedAPI
  short ListAllocate2Ext (short ListEntries,
      int TextSize,
      boolean fPrefixDataType,
      IntByReference rethList,
      PointerByReference retpList,
      IntByReference retListSize,
      boolean bAllowLarge);

  @UndocumentedAPI
  short ListAddEntry2Ext(int mhList,
      boolean fPrefixDataType,
      IntByReference pListSize,
      char EntryNumber,
      Memory Text,
      char TextSize,
      boolean bAllowLarge);
  
  @UndocumentedAPI
  /* to issue  design command */
  short NSFDesignCommand(HANDLE.ByValue hDb, int cmd, int dwFlags, 
    Memory pObjectName, IntByReference phReturnVal, IntByReference phErrorText, int hDsgnCmd);

  @UndocumentedAPI
  /* to prep for design command - add fields for create index */
  short NSFDesignCommandAddComponent(Memory name, int attr,
    IntByReference phDsgnCmd);

}
