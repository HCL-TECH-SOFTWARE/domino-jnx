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
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * C API methods introduced in R12
 * 
 * @author Karsten Lehmann
 */
public interface INotesCAPI12 extends Library {

  @UndocumentedAPI
	public short NSFProcessResults(HANDLE.ByValue hDb,
			Memory viewname,
			int dwFlags,
			int hInResults,
			int hOutFields,
			int hFieldRules,
			int hCombineRules,
			IntByReference hErrorText,
			DHANDLE.ByReference phStreamedhQueue);  

  @UndocumentedAPI
	short NSFQueryAddToResultsList(int /* QUEP_LISTTYPE */ type,
			Pointer pInEntry, Pointer phEntryList,
			IntByReference phErrorText);

  /**
   * 
   * @param ptdTimeDate A pointer to a Notes time/date value
   * @param wTextBufSize Big enough to include the trailing null char
   * @param pachText A pointer to string buffer for ASCII string
   * @return On invalid syntax, ERR_TDI_CONV error is returned, on
   *         successful conversion returns NOERROR
   * @since JNX 1.48.0
   * @since Domino 12.0.2
   */
  short ConvertTIMEDATEtoRFC3339Date(Pointer ptdTimeDate,
      Pointer pachText,
      short wTextBufSize);
}
