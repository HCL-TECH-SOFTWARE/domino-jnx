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
package com.hcl.domino.jna.richtext.op;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderContext;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderOperation;
import com.hcl.domino.richtext.RichTextWriter;

/**
 * RichText builder operation to repeat content and apply text replacements for each repetition
 * 
 * @author Karsten Lehmann
 */
public class RepeatOperation implements RichTextBuilderOperation {
	private int repetitions;
	private Map<Pattern, BiFunction<Integer,Matcher,Object>> replacements;

	public RepeatOperation(int repetitions,
			Map<Pattern, BiFunction<Integer,Matcher,Object>> replacements) {
		this.repetitions = repetitions;
		this.replacements = replacements;
	}

	@Override
	public Document apply(RichTextBuilderContext<?> ctx, Document doc) {
		List<Document> rowDocs = new ArrayList<>();
		String rtItemName = ctx.getItemName();
		
		for (int i=0; i<repetitions; i++) {
			Document docCopy = doc.copyToDatabase(doc.getParentDatabase());
			
			Map<Pattern,Function<Matcher,Object>> replacementsForRow = new HashMap<>();
			int iFinal = i;
			for (Entry<Pattern, BiFunction<Integer,Matcher,Object>> currEntry : replacements.entrySet()) {
				replacementsForRow.put(currEntry.getKey(), (matcher) -> {
					return currEntry.getValue().apply(iFinal, matcher);
				});
			}
			Document docForRow = ctx.getRichTextBuilder()
			.from(docCopy, ctx.getItemName())
			.replaceExt(replacementsForRow)
			.build();
			rowDocs.add(docForRow);
		}
		
		while (doc.hasItem(rtItemName)) {
			doc.removeItem(rtItemName);
		}
		
		try (RichTextWriter rtWriter = doc.createRichTextItem(rtItemName)) {
			rowDocs
			.forEach((docForRow) -> {
				rtWriter.addRichText(docForRow, rtItemName);
			});
		}
		
		return doc;
	}

}
