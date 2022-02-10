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
package com.hcl.domino.richtext.structures;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.util.JNXStringUtil;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "TIMEDATE", members = {
    @StructureMember(name = "Innards", type = int[].class, length = 2)
})
public interface OpaqueTimeDate extends MemoryStructure {
  @StructureGetter("Innards")
  int[] getInnards();

  @StructureSetter("Innards")
  OpaqueTimeDate setInnards(int[] innards);
  
  default String toReplicaId() {
	  int[] innards = getInnards();

	  return JNXStringUtil.pad(Integer.toHexString(innards[1]).toUpperCase(), 8, '0', false) +
			  JNXStringUtil.pad(Integer.toHexString(innards[0]).toUpperCase(), 8, '0', false);
  }

  default OpaqueTimeDate setFromReplicaId(String replicaId) {
	  int[] innards = new int[2];
	  
	  if (replicaId==null || replicaId.length()==0) {
		  innards[0] = 0;
		  innards[1] = 0;
	  }
	  else {
		  if (replicaId.contains(":")) { //$NON-NLS-1$
			  replicaId = replicaId.replace(":", ""); //$NON-NLS-1$ //$NON-NLS-2$
		  }

		  if (replicaId.length() != 16) {
			  throw new IllegalArgumentException("Replica ID is expected to have 16 hex characters or 8:8 format");
		  }

		  innards[1] = (int) (Long.parseLong(replicaId.substring(0,8), 16) & 0xffffffff);
		  innards[0] = (int) (Long.parseLong(replicaId.substring(8), 16) & 0xffffffff);
	  }

	  setInnards(innards);
	  return this;
  }
}
