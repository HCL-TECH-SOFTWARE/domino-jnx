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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.RichTextBuilder;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderContext;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderOperation;
import com.hcl.domino.jna.richtext.op.RenameFieldsOperation;
import com.hcl.domino.jna.richtext.op.RepeatOperation;
import com.hcl.domino.jna.richtext.op.RepeatTableRowOperation;
import com.hcl.domino.jna.richtext.op.ReplaceImageResourceFormulaOperation;
import com.hcl.domino.jna.richtext.op.ReplaceOperation;
import com.hcl.domino.richtext.conversion.IRichTextConversion;

/**
 * Abstract base implementation of {@link RichTextBuilderContext}
 * 
 * @author Karsten Lehmann
 *
 * @param <O> result type
 */
public abstract class JNARichTextBuilderContext<O> implements RichTextBuilderContext<O> {
	private JNARichTextBuilder rtBuilder;
	private Database targetDb;
	private Document templateDoc;
	private String itemName;
	protected List<RichTextBuilderOperation> operations;
	private Class<O> resultType;
	
	public JNARichTextBuilderContext(JNARichTextBuilder rtBuilder, Database targetDb, Document template, String itemName, Class<O> resultType) {
		this.rtBuilder = rtBuilder;
		this.targetDb = targetDb;
		this.templateDoc = template;
		this.itemName = itemName;
		this.resultType = resultType;
		this.operations = new ArrayList<>();
	}
	
	@Override
	public RichTextBuilder getRichTextBuilder() {
		return rtBuilder;
	}
	
	@Override
	public Class<O> getResultType() {
		return resultType;
	}
	
	@Override
	public String getItemName() {
		return itemName;
	}
	
	protected Document getTemplate() {
		return templateDoc;
	}

	@Override
	public RichTextBuilderContext<O> replaceExt(Map<Pattern, Function<Matcher, Object>> replacements) {
		operations.add(new ReplaceOperation(replacements));
		return this;
	}
	
	@Override
	public RichTextBuilderContext<O> repeat(int repetitions,
			Map<Pattern, BiFunction<Integer,Matcher,Object>> replacements) {
		operations.add(new RepeatOperation(repetitions, replacements));
		return this;
	}
	
	protected Optional<Document> unwrapDocument(Object obj) {
		if (obj instanceof Document) {
			return Optional.of((Document) obj);
		}
		else if (obj instanceof DesignElement) {
			return Optional.of(((DesignElement)obj).getDocument());
		}
		else {
			return Optional.empty();
		}
	}
	
	@Override
	public RichTextBuilderContext<O> repeatTableRowExt(int rowIdx, int nrOfRows,
			Map<Pattern, BiFunction<Integer,Matcher,Object>> replacements) {
		operations.add(new RepeatTableRowOperation(getItemName(), rowIdx, nrOfRows, replacements));
		return this;
	}

	@Override
	public RichTextBuilderContext<O> renameField(String newFieldName) {
		Map<Pattern,Function<Matcher,String>> replacements = new HashMap<>();
		replacements.put(Pattern.compile(Pattern.quote("@firstfield"), Pattern.CASE_INSENSITIVE),
				(matcher) -> { return newFieldName; });
		return renameFields(replacements);
	}

	@Override
	public RichTextBuilderContext<O> renameFields(Map<Pattern,Function<Matcher,String>> replacements) {
		operations.add(new RenameFieldsOperation(replacements));
		return this;
	}

	@Override
	public RichTextBuilderContext<O> replaceInImageResourceFormula(Map<Pattern,Function<Matcher,String>> replacements) {
		operations.add(new ReplaceImageResourceFormulaOperation(replacements));
		return this;
	}

	@Override
	public RichTextBuilderContext<O> apply(RichTextBuilderOperation op) {
		operations.add(op);
		return this;
	}

	@Override
	public RichTextBuilderContext<O> apply(IRichTextConversion rtConv) {
		operations.add(new RichTextBuilderOperation() {

			@Override
			public Document apply(RichTextBuilderContext<?> ctx, Document doc) {
				doc.convertRichTextItem(getItemName(), rtConv);
				return doc;
			}
			
		});
		return this;
	}
	
	protected Document buildResultDocument() {
		Document sourceDoc = getTemplate();
		Document workDoc = sourceDoc.copyToDatabase(targetDb);
		
		for (RichTextBuilderOperation currOp : operations) {
			workDoc = currOp.apply(this, workDoc);
		}
		
		Set<DocumentClass> docClass = workDoc.getDocumentClass();
		if (!docClass.contains(DocumentClass.DATA)) {
			workDoc.compileLotusScript();
			workDoc.sign();
		}
		
		return workDoc;
	}
	
}
