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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.DesignElement.NamedDesignElement;
import com.hcl.domino.misc.NotesConstants;

public abstract class AbstractNamedDesignElement<T extends NamedDesignElement> extends AbstractDesignElement<T> implements NamedDesignElement {

	public AbstractNamedDesignElement(Document doc) {
		super(doc);
	}

	@Override
	public String getTitle() {
		String title = getDocument().getAsText(NotesConstants.FIELD_TITLE, '|');
		int barIndex = title.indexOf('|');
		if(barIndex < 0) {
			return title;
		} else {
			return title.substring(0, barIndex);
		}
	}

	@Override
	public List<String> getAliases() {
		String title = getDocument().getAsText(NotesConstants.FIELD_TITLE, '|');
		int barIndex = title.indexOf('|');
		if(barIndex < 0) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(title.substring(barIndex+1).split("\\|")); //$NON-NLS-1$
		}
	}

	@Override
	public void setTitle(String... title) {
		getDocument().replaceItemValue(NotesConstants.FIELD_TITLE, title);
	}
}
