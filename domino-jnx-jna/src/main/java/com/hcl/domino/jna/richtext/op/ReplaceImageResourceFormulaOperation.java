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

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderContext;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderOperation;

/**
 * RichText builder operation to replace text in an image resource formula
 * 
 * @author Karsten Lehmann
 */
public class ReplaceImageResourceFormulaOperation implements RichTextBuilderOperation {
	private Map<Pattern,Function<Matcher,String>> replacements;
	
	public ReplaceImageResourceFormulaOperation(Map<Pattern,Function<Matcher,String>> replacements) {
		this.replacements = replacements;
	}
	
	@Override
	public Document apply(RichTextBuilderContext<?> ctx, Document doc) {
		doc.convertRichTextItem(ctx.getItemName(), new ReplaceImageResourceFormulaConversion(replacements));
		
		return doc;
	}

}
