package com.hcl.domino.jna.richtext.op;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.commons.richtext.conversion.PatternBasedTextReplacementConversion;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.GenericFormOrSubform;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderContext;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderOperation;
import com.hcl.domino.richtext.records.CDText;

public class ReplaceOperation implements RichTextBuilderOperation {
	private Map<Pattern, Function<Matcher, Object>> replacements;

	public ReplaceOperation(Map<Pattern, Function<Matcher, Object>> replacements) {
		this.replacements = replacements;
	}

	@Override
	public Document apply(RichTextBuilderContext<?> ctx, Document doc) {
		String rtItemName = ctx.getItemName();

		for (Entry<Pattern,Function<Matcher,Object>> currEntry : replacements.entrySet()) {
			Pattern currPattern = currEntry.getKey();
			Function<Matcher,Object> currFct = currEntry.getValue();

			PatternBasedTextReplacementConversion rtConv =
					new PatternBasedTextReplacementConversion(currPattern,
							(matcher, fontStyle, rtWriter) -> {
								Object newContent = currFct.apply(matcher);
								if (newContent==null) {
									newContent = "";
								}

								if (newContent instanceof String) {
									String newContentStr = (String) newContent;
									if (newContentStr.length()>0) {
										rtWriter.addRichTextRecord(CDText.class, (newContentTxtRecord) -> {
											newContentTxtRecord.setStyle(fontStyle);
											newContentTxtRecord.setText(newContentStr);
										});
									}
								}
								else if (newContent instanceof RichTextBuilderContext<?>) {
									Document docToInsert = null;
									Object result = ((RichTextBuilderContext<?>)newContent).build();
									if (result instanceof Document) {
										docToInsert = (Document) result;
									}
									else if (result instanceof DesignElement) {
										docToInsert  = ((DesignElement)result).getDocument();
									}

									if (docToInsert!=null) {
										rtWriter.addRichText(docToInsert, ((RichTextBuilderContext<?>)newContent).getItemName());
									}
								}
								else if (newContent instanceof GenericFormOrSubform<?>) {
									Document docToInsert = ((GenericFormOrSubform<?>)newContent).getDocument();
									RichTextUtil.addOtherRichTextItem(doc, rtWriter, docToInsert, "$body", true);
//									RichTextUtil.addOtherRichTextItem(doc, rtWriter, docToInsert, "$body", false);
								}
								else if (newContent instanceof Document) {
									Set<DocumentClass> docClass = ((Document)newContent).getDocumentClass();
									String otherRTItemName = docClass.contains(DocumentClass.DATA) ? rtItemName : "$body";
									RichTextUtil.addOtherRichTextItem(doc, rtWriter, (Document) newContent, otherRTItemName, true);
								}
								else {
									throw new IllegalArgumentException(MessageFormat.format("Invalid replacement value type: {0}", newContent.getClass().getName()));
								}
							});

			doc.convertRichTextItem(rtItemName, rtConv);
		}

		return doc;
	}

}
