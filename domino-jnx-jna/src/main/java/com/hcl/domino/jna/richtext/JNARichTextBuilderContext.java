package com.hcl.domino.jna.richtext;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.commons.design.DesignUtil;
import com.hcl.domino.commons.richtext.conversion.PatternBasedTextReplacementConversion;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.GenericFormOrSubform;
import com.hcl.domino.design.RichTextBuilder;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderContext;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderOperation;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDText;

/**
 * Abstract base implementation of {@link RichtextBuilderContext}
 * 
 * @author Karsten Lehmann
 *
 * @param <T> result type
 */
public abstract class JNARichTextBuilderContext<O> implements RichTextBuilderContext<O> {
	private Database targetDb;
	private Document templateDoc;
	private String itemName;
	protected List<RichTextBuilderOperation> operations;
	private Class<O> resultType;
	
	public JNARichTextBuilderContext(Database targetDb, Document template, String itemName, Class<O> resultType) {
		this.targetDb = targetDb;
		this.templateDoc = template;
		this.itemName = itemName;
		this.resultType = resultType;
		this.operations = new ArrayList<>();
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
	public RichTextBuilderContext<O> replace(String placeholder, String label) {
		Map<String,Object> replacements = new HashMap<>();
		replacements.put(placeholder, label);
		return replace(replacements);
	}

	@Override
	public RichTextBuilderContext<O> replace(Map<String, Object> replacements) {
		for (Entry<String,Object> currEntry : replacements.entrySet()) {
			String currPlaceholderStr = currEntry.getKey();
			Object currReplacement = currEntry.getValue();
			
			String currPlaceholderPatternStr = Pattern.quote(currPlaceholderStr);
			Pattern currPlaceholderPattern = Pattern.compile(currPlaceholderPatternStr, Pattern.CASE_INSENSITIVE);

			replace(currPlaceholderPattern, (matcher) -> {
				return currReplacement;
			});
		}
		
		return this;
	}

	@Override
	public RichTextBuilderContext<O> replace(Pattern pattern, Function<Matcher, Object> labelFct) {
		operations.add((ctx, doc) -> {
			String rtItemName = ctx.getItemName();

			PatternBasedTextReplacementConversion rtConv =
					new PatternBasedTextReplacementConversion(pattern,
							(matcher, fontStyle, rtWriter) -> {
								Object newContent = labelFct.apply(matcher);
								if (newContent==null) {
									newContent = "";
								}

								if (newContent instanceof String) {
									String newContentStr = (String) newContent;
									if (newContentStr.length()>0) {
						        		CDText newContentTxtRecord = MemoryStructureUtil.newStructure(CDText.class, 0);
						        		newContentTxtRecord.setStyle(fontStyle);
						        		newContentTxtRecord.setText(newContentStr);
						        		//add CDText WSIG
						        		newContentTxtRecord.getData().put((byte) 0x85).put((byte) 0xff);
						        		rtWriter.addRichTextRecord(newContentTxtRecord);
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
									String otherRTItemName = "$Body";
									Document docToInsert = ((GenericFormOrSubform<?>)newContent).getDocument();
									rtWriter.addRichText(docToInsert, rtItemName);
								}
								else if (newContent instanceof Document) {
									Set<DocumentClass> docClass = ((Document)newContent).getDocumentClass();
									String otherRTItemName = docClass.contains(DocumentClass.DATA) ? rtItemName : "$body";
									rtWriter.addRichText((Document) newContent, otherRTItemName);
								}
								else {
									throw new IllegalArgumentException(MessageFormat.format("Invalid replacement value type: {0}", newContent.getClass().getName()));
								}
							});

			doc.convertRichTextItem(rtItemName, rtConv);
			return doc;
		});

		return this;
	}

	@Override
	public RichTextBuilderContext<O> replace(String placeholder, GenericFormOrSubform<?> formDesign) {
		String currFromPattern = Pattern.quote(placeholder);
		Pattern pattern = Pattern.compile(currFromPattern, Pattern.CASE_INSENSITIVE);
		
		replace(pattern, (matcher) -> {
			return formDesign;
		});
		
		return this;
	}

	@Override
	public RichTextBuilderContext<O> replace(String placeholder,
			RichTextBuilderContext<?> buildCtx) {
		
		Object objectToInsert = buildCtx.build();
		Optional<Document> docToInsert = unwrapDocument(objectToInsert);

		if (docToInsert.isPresent()) {
			String currFromPattern = Pattern.quote(placeholder);
			Pattern pattern = Pattern.compile(currFromPattern, Pattern.CASE_INSENSITIVE);
			
			replace(pattern, (matcher) -> {
				return docToInsert.get();
			});
		}
		
		return this;
	}

	@Override
	public RichTextBuilderContext<O> repeat(int repetitions, BiConsumer<Integer, Map<String, Object>> consumer) {
		operations.add((ctx, doc) -> {
			Database db = doc.getParentDatabase();
			RichTextBuilder formBuilder = db.getRichTextBuilder();
			
			//keep original doc content to have something to repeat
			Document docOrig = doc.copyToDatabase(db);
			@SuppressWarnings("rawtypes")
			GenericFormOrSubform formOrig = (GenericFormOrSubform) DesignUtil.createDesignElement(docOrig);
			
			String rtItemName = ctx.getItemName();

			//remove existing richtext from doc
			while (doc.hasItem(rtItemName)) {
				doc.removeItem(rtItemName);
			}
			
			//and build a combined richtext item of the repeating parts
			try (RichTextWriter rtWriter = doc.createRichTextItem(rtItemName);) {
				for (int i=0; i<repetitions; i++) {
					Map<String,Object> placeholders = new HashMap<>();
					consumer.accept(i, placeholders);
					
					@SuppressWarnings("unchecked")
					RichTextBuilderContext replaceCtx = formBuilder
					.from(formOrig)
					.replace(placeholders);
					
					Object objectRepetition = ((RichTextBuilderContext)replaceCtx).build();
					Optional<Document> docRepetition = unwrapDocument(objectRepetition);

					if (docRepetition.isPresent()) {
						rtWriter.addRichText(docRepetition.get(), ((RichTextBuilderContext)replaceCtx).getItemName());
					}
				}
			}
			
			return docOrig;
		});
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
	
	@SuppressWarnings("rawtypes")
	@Override
	public RichTextBuilderContext<O> repeat(Stream<Map<String, Object>> replacements) {
		operations.add((ctx, doc) -> {
			Database db = doc.getParentDatabase();
			RichTextBuilder richTextBuilder = db.getRichTextBuilder();
			
			//keep original doc content to have something to repeat
			Document docOrig = doc.copyToDatabase(db);
			@SuppressWarnings("rawtypes")
			GenericFormOrSubform formOrig = (GenericFormOrSubform) DesignUtil.createDesignElement(docOrig);
			
			String rtItemName = ctx.getItemName();
			
			//remove existing richtext from doc
			while (doc.hasItem(rtItemName)) {
				doc.removeItem(rtItemName);
			}
			
			//and build a combined richtext item of the repeating parts
			try (RichTextWriter rtWriter = doc.createRichTextItem(rtItemName);) {
				replacements
				.forEach((replacementMap) -> {
					@SuppressWarnings("unchecked")
					RichTextBuilderContext replaceCtx = richTextBuilder
					.from(formOrig)
					.replace(replacementMap);
					
					Object objectRepetition = ((RichTextBuilderContext)replaceCtx).build();
					Optional<Document> docRepetition = unwrapDocument(objectRepetition);
					
					if (docRepetition.isPresent()) {
						rtWriter.addRichText(docRepetition.get(), ((RichTextBuilderContext)replaceCtx).getItemName());
					}
				});
			}
			
			return docOrig;
		});
		return this;
	}

	@Override
	public RichTextBuilderContext<O> repeatTableRow(int rowIdx, Stream<Map<String, String>> replacements) {
		throw new NotYetImplementedException();
	}

	@Override
	public RichTextBuilderContext<O> repeatTableRow(int rowIdx, int nrOfRows, BiConsumer<Integer, Map<String, Object>> consumer) {
		throw new NotYetImplementedException();
	}

	@Override
	public RichTextBuilderContext<O> renameField(String newFieldName) {
		Map<String,String> newFieldNames = new HashMap<>();
		newFieldNames.put("@firstfield", newFieldName);
		return renameFields(newFieldNames);
	}

	@Override
	public RichTextBuilderContext<O> renameFields(Map<String, String> newFieldNames) {
		operations.add(new RenameFieldsOperation(newFieldNames));
		return this;
	}

	@Override
	public RichTextBuilderContext<O> apply(RichTextBuilderOperation op) {
		operations.add(op);
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
