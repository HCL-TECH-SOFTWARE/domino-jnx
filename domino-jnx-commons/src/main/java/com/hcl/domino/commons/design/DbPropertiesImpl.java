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

import com.hcl.domino.data.Document;
import com.hcl.domino.design.DbProperties;

public class DbPropertiesImpl extends AbstractDesignElement<DbProperties> implements DbProperties {

	public DbPropertiesImpl(Document doc) {
		super(doc);
	}

	@Override
	public void initializeNewDesignNote() {
		
	}

	@Override
	public boolean isGenerateEnhancedHtml() {
		return "1".equals(getDocument().getAsText(DesignConstants.DB_NEW_HTML, ' ')); //$NON-NLS-1$
	}
	@Override
	public void setGenerateEnhancedHtml(boolean generateEnhancedHtml) {
		if(generateEnhancedHtml) {
			getDocument().replaceItemValue(DesignConstants.DB_NEW_HTML, '1');
		} else {
			getDocument().removeItem(DesignConstants.DB_NEW_HTML);
		}
	}
}
