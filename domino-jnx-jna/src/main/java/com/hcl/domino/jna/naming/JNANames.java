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

import com.hcl.domino.DominoClient;
import com.hcl.domino.Name;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.commons.DefaultJNXName;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.naming.Names;

public class JNANames implements Names {

	@Override
	public Name _createName(String name) {
		return new DefaultJNXName(name);
	}

	@Override
	public String _toAbbreviated(String name) {
		return NotesNamingUtils.toAbbreviatedName(name);
	}

	@Override
	public String _toCommon(String name) {
		return NotesNamingUtils.toCommonName(name);
	}

	@Override
	public String _toCanonical(String name) {
		return NotesNamingUtils.toCanonicalName(name);
	}

	@Override
	public boolean _equalNames(String name1, String name2) {
		return NotesNamingUtils.equalNames(name1, name2);
	}

	@Override
	public UserNamesList _buildNamesList(DominoClient client, String name) {
		return NotesNamingUtils.buildNamesList((IAPIObject<?>)client, name);
	}
}
