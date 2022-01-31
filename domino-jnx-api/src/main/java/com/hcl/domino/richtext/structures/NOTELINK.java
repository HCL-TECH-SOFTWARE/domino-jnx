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

/**
 * @author Karsten Lehmann
 * @since 1.0.38
 */
@StructureDefinition(name = "NOTELINK", members = {
		@StructureMember(name = "File", type = OpaqueTimeDate.class),
		@StructureMember(name = "View", type = UNID.class),
		@StructureMember(name = "Note", type = UNID.class)
})
public interface NOTELINK extends MemoryStructure {

	@StructureGetter("File")
	OpaqueTimeDate getFile();

	@StructureGetter("View")
	UNID getView();

	@StructureGetter("Note")
	UNID getNote();

	default String getReplicaId() {
		return getFile().toReplicaId();
	}

	default NOTELINK setReplicaId(String replicaId) {
		getFile().setFromReplicaId(replicaId);
		return this;
	}

	default NOTELINK setViewUnid(String unid) {
		getView().setUnid(unid);
		return this;
	}

	default String getViewUnid() {
		UNID unid = getView();
		return unid.toUnidString();
	}

	default NOTELINK setDocUnid(String unid) {
		getNote().setUnid(unid);
		return this;
	}

	default String getDocUnid() {
		return getNote().toUnidString();
	}
	
	/**
	 * Copies all data from another link
	 * 
	 * @param otherLink link
	 */
	default void copyFrom(NOTELINK otherLink) {
	  setReplicaId(otherLink.getReplicaId());
	  setViewUnid(otherLink.getViewUnid());
	  setDocUnid(otherLink.getDocUnid());
	}
}
