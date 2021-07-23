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
package com.hcl.domino.commons.design;

import java.util.stream.Collectors;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.LotusScriptLibrary;
import com.hcl.domino.misc.NotesConstants;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class LotusScriptLibraryImpl extends AbstractScriptLibrary<LotusScriptLibrary> implements LotusScriptLibrary {

	public LotusScriptLibraryImpl(Document doc) {
		super(doc);
	}

	@Override
	public String getScript() {
		// This must be stored in $ScriptLib items
		return getDocument().allItems()
			.filter(item -> NotesConstants.SCRIPTLIB_ITEM_NAME.equalsIgnoreCase(item.getName()))
			.map(item -> item.getValue().get(0))
			.map(String::valueOf)
			.collect(Collectors.joining());
	}
}
