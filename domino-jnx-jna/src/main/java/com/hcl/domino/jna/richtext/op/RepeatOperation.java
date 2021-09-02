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
