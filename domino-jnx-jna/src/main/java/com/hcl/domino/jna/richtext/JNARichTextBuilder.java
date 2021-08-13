package com.hcl.domino.jna.richtext;

import com.hcl.domino.commons.design.DesignUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.GenericFormOrSubform;
import com.hcl.domino.design.RichTextBuilder;

public class JNARichTextBuilder implements RichTextBuilder {
	private Database db;
	
	public JNARichTextBuilder(Database db) {
		this.db = db;
	}
	
	@Override
	public <F extends GenericFormOrSubform<F>> RichTextBuilderContext<F> from(F templateForm) {
		return new JNARichTextBuilderContext<F>(db, templateForm.getDocument(), "$body", (Class<F>) templateForm.getClass()) {
			@Override
			public F build() {
				Document doc = buildResultDocument();
				return (F) DesignUtil.createDesignElement(doc);
			}
		};
	}

	@Override
	public RichTextBuilderContext<Document> from(Document doc, String rtItemName) {
		return new JNARichTextBuilderContext<Document>(db, doc, rtItemName, Document.class) {

			@Override
			public Document build() {
				return buildResultDocument();
			}
			
		};
	}
	
}
