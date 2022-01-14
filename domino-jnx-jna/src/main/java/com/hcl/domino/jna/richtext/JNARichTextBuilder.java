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
package com.hcl.domino.jna.richtext;

import com.hcl.domino.commons.design.DesignUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.GenericFormOrSubform;
import com.hcl.domino.design.RichTextBuilder;
import com.hcl.domino.misc.NotesConstants;

/**
 * Implementation of {@link RichTextBuilder} for JNA
 * 
 * @author Karsten Lehmann
 */
public class JNARichTextBuilder implements RichTextBuilder {
	private Database db;
	
	public JNARichTextBuilder(Database db) {
		this.db = db;
	}
	
	@SuppressWarnings("unchecked")
  @Override
	public <F extends GenericFormOrSubform<F>> RichTextBuilderContext<F> from(F templateForm) {
		return new JNARichTextBuilderContext<F>(this, db, templateForm.getDocument(), NotesConstants.ITEM_NAME_TEMPLATE, (Class<F>) templateForm.getClass()) {
			@Override
			public F build() {
				Document doc = buildResultDocument();
				return (F) DesignUtil.createDesignElement(doc);
			}
		};
	}

	@Override
	public RichTextBuilderContext<Document> from(Document doc, String rtItemName) {
		return new JNARichTextBuilderContext<Document>(this, db, doc, rtItemName, Document.class) {

			@Override
			public Document build() {
				return buildResultDocument();
			}
			
		};
	}
	
}
