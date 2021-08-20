package com.hcl.domino.jna.richtext;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderContext;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderOperation;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.conversion.IRichTextConversion;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDIDName;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * RichText operation that renames fields in design richtext and takes
 * care of formula and Lotusscript event code
 * 
 * @author Karsten Lehmann
 */
public class RenameFieldsOperation implements RichTextBuilderOperation {
	private Map<String,String> newFieldNames;
	
	public RenameFieldsOperation(Map<String,String> newFieldNames) {
		//make field names case insensitive
		this.newFieldNames = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.newFieldNames.putAll(newFieldNames);
	}
	
	@Override
	public Document apply(RichTextBuilderContext<?> ctx, Document doc) {
		if (newFieldNames.containsKey("@firstfield")) {
			//resolve first field name
			String firstFieldName = doc.getRichTextItem("$body")
					.stream()
					.filter(CDField.class::isInstance)
					.map(CDField.class::cast)
					.map(record -> record.getName())
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Unable to find any field in the form"));
			
			newFieldNames.put(firstFieldName, newFieldNames.remove("@firstfield"));
		}
		
		String rtItemName = ctx.getItemName();
		
		doc.convertRichTextItem(rtItemName, new IRichTextConversion() {
			
			@Override
			public void richTextNavigationStart() {
			}
			
			@Override
			public void richTextNavigationEnd() {
			}
			
			@Override
			public boolean isMatch(List<RichTextRecord<?>> nav) {
				return nav.stream().anyMatch((record) -> {
					return record instanceof CDField || record instanceof CDIDName;
				});
			}
			
			@Override
			public void convert(List<RichTextRecord<?>> source, RichTextWriter target) {
				source.forEach((record) -> {
					if (record instanceof CDField) {
						CDField fieldRecord = (CDField) record;
						String fieldName = fieldRecord.getName();
						
						String newFieldName = newFieldNames.get(fieldName);
						if (newFieldName!=null && !newFieldName.equalsIgnoreCase(fieldName)) {
							fieldRecord.setName(newFieldName);
						}
						
						//transform formula code as well
						String defaultValueFormulaOld = fieldRecord.getDefaultValueFormula();
						String inputValidationFormulaOld = fieldRecord.getInputValidationFormula();
						String inputTranslationFormulaOld = fieldRecord.getInputTranslationFormula();

						if (!StringUtil.isEmpty(defaultValueFormulaOld)) {
							String defaultValueFormulaNew = StringUtil.replaceAllMatches(defaultValueFormulaOld, newFieldNames, true);
							if (!defaultValueFormulaNew.equals(defaultValueFormulaOld)) {
								fieldRecord.setDefaultValueFormula(defaultValueFormulaNew);
							}
						}
						
						if (!StringUtil.isEmpty(inputValidationFormulaOld)) {
							String inputValidationFormulaNew = StringUtil.replaceAllMatches(inputValidationFormulaOld, newFieldNames, true);
							if (!inputValidationFormulaNew.equals(inputValidationFormulaOld)) {
								fieldRecord.setInputValidationFormula(inputValidationFormulaNew);
							}
						}
							
						if (!StringUtil.isEmpty(inputTranslationFormulaOld)) {
							String inputTranslationFormulaNew = StringUtil.replaceAllMatches(inputTranslationFormulaOld, newFieldNames, true);
							if (!inputTranslationFormulaNew.equals(inputTranslationFormulaOld)) {
								fieldRecord.setInputTranslationFormula(inputTranslationFormulaNew);
						}
					}

					}
					else if (record instanceof CDIDName) {
						CDIDName htmlAttrRecord = (CDIDName) record;
						
					}
					target.addRichTextRecord(record);
				});
			}
		});
		
		//now we remove the old compiled LS object code for the old field names, e.g. $textfield_O
		for (Entry<String,String> currEntry : newFieldNames.entrySet()) {
			String currOldField = currEntry.getKey();
			doc.removeItem("$"+currOldField+"_O");
		}
		
		//we read the LS code for the old field names
		for (Entry<String,String> currEntry : newFieldNames.entrySet()) {
			String currOldField = currEntry.getKey();
			String currNewField = currEntry.getValue();
			
			String oldLSCode = doc.get("$$"+currOldField, String.class, "");
			
			if (!StringUtil.isEmpty(oldLSCode)) {
				//and replace all field name occurrences in the LS code (if code for one field references another one)
				String newLSCode = StringUtil.replaceAllMatches(oldLSCode, newFieldNames, true);
				doc.replaceItemValue("$$"+currNewField, EnumSet.of(ItemFlag.SIGNED, ItemFlag.KEEPLINEBREAKS), newLSCode);
				doc.removeItem("$$"+currOldField);
				
				//rename field placeholder item
				Optional<Item> placeholderItem = doc.getFirstItem(currOldField);
				if (placeholderItem.isPresent()) {
					placeholderItem.get().copyToDocument(doc, currNewField, true);
					doc.removeItem(currOldField);
				}
			}
		}
		
		if (doc.hasItem("$fields")) {
			//$fields contains an array of the fieldnames
			List<String> oldFields = doc.getAsList("$fields", String.class, null);
			if (oldFields!=null) {
				List<String> newFields = oldFields.stream()
						.map((fieldName) -> {
							if (newFieldNames.containsKey(fieldName)) {
								return newFieldNames.get(fieldName);
							}
							else {
								return fieldName;
							}
						})
						.collect(Collectors.toList());
				
				doc.replaceItemValue("$fields", newFields);
			}
		}
		
		return doc;
	}

}
