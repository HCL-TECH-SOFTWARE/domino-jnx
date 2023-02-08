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
package com.hcl.domino.jna;

import com.hcl.domino.commons.OSLoadStringProvider;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;

/**
 * @author Jesse Gallagher
 * @since 1.0.19
 */
public class JNAOSLoadStringProvider implements OSLoadStringProvider {

	@Override
	public String loadString(int module, short status) {
		
		try(DisposableMemory retBuffer = new DisposableMemory(256)) {
	    retBuffer.clear();
			short outStrLength = NotesCAPI.get().OSLoadString(0, status, retBuffer, (short) 255);
			if (outStrLength==0) {
				return ""; //$NON-NLS-1$
			}

			return NotesStringUtils.fromLMBCS(retBuffer, outStrLength);
		}
	}

}
