package com.hcl.domino.jna.richtext.op;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderContext;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderOperation;

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
