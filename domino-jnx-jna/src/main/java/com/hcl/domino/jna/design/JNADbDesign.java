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
package com.hcl.domino.jna.design;

import com.hcl.domino.commons.design.AbstractDbDesign;
import com.hcl.domino.commons.errors.errorcodes.IMiscErr;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

public class JNADbDesign extends AbstractDbDesign {
	public JNADbDesign(JNADatabase database) {
		super(database);
	}
	
	@Override
	protected int findDesignNote(DocumentClass noteClass, String pattern, String name, boolean partialMatch) {
		Memory nameLMBCS = NotesStringUtils.toLMBCS(name, true);

		IntByReference retAgentNoteID = new IntByReference();
		retAgentNoteID.setValue(0);
		
		short result = LockUtil.lockHandle(getDatabase().getAdapter(HANDLE.class), (hDbByVal)-> {
			return NotesCAPI.get().NIFFindDesignNoteExt(hDbByVal, nameLMBCS,
					noteClass.getValue(),
					NotesStringUtils.toLMBCS(pattern, true),
					retAgentNoteID,
					NotesConstants.DGN_STRIPUNDERS);
		});
		
		if ((result & NotesConstants.ERR_MASK)==IMiscErr.ERR_NOT_FOUND) {
			return 0;
		}
		
		//throws an error if agent cannot be found:
		NotesErrorUtils.checkResult(result);
		
		return retAgentNoteID.getValue();
	}
}
