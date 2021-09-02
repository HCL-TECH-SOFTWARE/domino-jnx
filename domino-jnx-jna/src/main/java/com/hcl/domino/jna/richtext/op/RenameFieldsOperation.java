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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Formula;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderContext;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderOperation;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.conversion.IRichTextConversion;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDIDName;
import com.hcl.domino.richtext.records.CDPabHide;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * RichText builder operation that renames fields in design richtext and takes
 * care of formula and Lotusscript event code
 * 
 * @author Karsten Lehmann
 */
public class RenameFieldsOperation implements RichTextBuilderOperation {
	private Map<Pattern,Function<Matcher,String>> replacements;
	
	public RenameFieldsOperation(Map<Pattern,Function<Matcher,String>> replacements) {
		this.replacements = replacements;
	}
	
	@Override
	public Document apply(RichTextBuilderContext<?> ctx, Document doc) {
		String firstFieldPatternStrQuoted = Pattern.quote("@firstfield");
		
		Pattern firstFieldPattern = null;
		
		//check for special pattern text to rename first field in the form
		for (Pattern currPattern : replacements.keySet()) {
			if (currPattern.pattern().equalsIgnoreCase(firstFieldPatternStrQuoted)) {
				firstFieldPattern = currPattern;
				break;
			}
		}
		
		if (firstFieldPattern!=null) {
			//resolve first field name
			String firstFieldName = doc.getRichTextItem("$body")
					.stream()
					.filter(CDField.class::isInstance)
					.map(CDField.class::cast)
					.map(record -> record.getName())
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Unable to find any field in the form"));
		
			replacements = new HashMap<>(replacements);
			replacements.put(Pattern.compile(Pattern.quote(firstFieldName), Pattern.CASE_INSENSITIVE),
					replacements.get(firstFieldPattern));
			replacements.remove(firstFieldPattern);
		}
		
		String rtItemName = ctx.getItemName();
		
		Map<String,String> renamedFields = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
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
					return record instanceof CDField || record instanceof CDIDName || record instanceof CDPabHide;
				});
			}
			
			@Override
			public void convert(List<RichTextRecord<?>> source, RichTextWriter target) {
				source.forEach((record) -> {
					if (record instanceof CDField) {
						//replace field name and field formulas
						CDField fieldRecord = (CDField) record;
						String fieldName = fieldRecord.getName();

						String newFieldName = StringUtil.replaceAllMatches(fieldName, replacements);

						if (newFieldName!=null && !newFieldName.equalsIgnoreCase(fieldName)) {
							fieldRecord.setName(newFieldName);
							renamedFields.put(fieldName, newFieldName);
							
							
							String oldLSCode = doc.get("$$"+fieldName, String.class, "");
							
							if (!StringUtil.isEmpty(oldLSCode)) {
								//and replace all field name occurrences in the LS code (if code for one field references another one)
								String newLSCode = StringUtil.replaceAllMatches(oldLSCode, replacements);
								doc.replaceItemValue("$$"+newFieldName, EnumSet.of(ItemFlag.SIGNED, ItemFlag.KEEPLINEBREAKS), newLSCode);
								doc.removeItem("$$"+fieldName);
								//now we remove the old compiled LS object code for the old field name, e.g. $textfield_O
								doc.removeItem("$"+fieldName+"_O");

								//rename field placeholder item
								Optional<Item> placeholderItem = doc.getFirstItem(fieldName);
								if (placeholderItem.isPresent()) {
									placeholderItem.get().copyToDocument(doc, newFieldName, true);
									doc.removeItem(fieldName);
								}
							}
						}

						//transform formula code as well
						String defaultValueFormulaOld = fieldRecord.getDefaultValueFormula();
						String inputValidationFormulaOld = fieldRecord.getInputValidationFormula();
						String inputTranslationFormulaOld = fieldRecord.getInputTranslationFormula();

						if (!StringUtil.isEmpty(defaultValueFormulaOld)) {
							String defaultValueFormulaNew = StringUtil.replaceAllMatches(defaultValueFormulaOld, replacements);
							if (!defaultValueFormulaNew.equals(defaultValueFormulaOld)) {
								fieldRecord.setDefaultValueFormula(defaultValueFormulaNew);
							}
						}

						if (!StringUtil.isEmpty(inputValidationFormulaOld)) {
							String inputValidationFormulaNew = StringUtil.replaceAllMatches(inputValidationFormulaOld, replacements);
							if (!inputValidationFormulaNew.equals(inputValidationFormulaOld)) {
								fieldRecord.setInputValidationFormula(inputValidationFormulaNew);
							}
						}

						if (!StringUtil.isEmpty(inputTranslationFormulaOld)) {
							String inputTranslationFormulaNew = StringUtil.replaceAllMatches(inputTranslationFormulaOld, replacements);
							if (!inputTranslationFormulaNew.equals(inputTranslationFormulaOld)) {
								fieldRecord.setInputTranslationFormula(inputTranslationFormulaNew);
							}
						}
					}
					else if (record instanceof CDIDName) {
						//process HTML attributes and styles
						CDIDName htmlAttrRecord = (CDIDName) record;
						
						String className = htmlAttrRecord.getClassName();
						String newClassName = StringUtil.replaceAllMatches(className, replacements);
						if (!newClassName.equals(className)) {
							htmlAttrRecord.setClassName(newClassName);
						}
						
						String htmlAttr = htmlAttrRecord.getHTMLAttributes();
						String newHtmlAttr = StringUtil.replaceAllMatches(htmlAttr, replacements);
						if (!newHtmlAttr.equals(htmlAttr)) {
							htmlAttrRecord.setHTMLAttributes(newHtmlAttr);
						}
						
						String id = htmlAttrRecord.getID();
						String newId = StringUtil.replaceAllMatches(htmlAttr, replacements);
						if (!newId.equals(id)) {
							htmlAttrRecord.setID(newId);
						}
						
						String name = htmlAttrRecord.getName();
						String newName = StringUtil.replaceAllMatches(name, replacements);
						if (!newName.equals(name)) {
							htmlAttrRecord.setName(newName);
						}
						
						String style = htmlAttrRecord.getStyle();
						String newStyle = StringUtil.replaceAllMatches(style, replacements);
						if (!newStyle.equals(style)) {
							htmlAttrRecord.setStyle(newStyle);
						}
						
						String title = htmlAttrRecord.getTitle();
						String newTitle = StringUtil.replaceAllMatches(title, replacements);
						if (!newTitle.equals(title)) {
							htmlAttrRecord.setTitle(newTitle);
						}
					}
					else if (record instanceof CDPabHide) {
						//process hide-when formula
						CDPabHide hideWhenRecord = (CDPabHide) record;
						
						String formula = hideWhenRecord.getFormula();
						String newFormula = StringUtil.replaceAllMatches(formula, replacements);
						if (!newFormula.equals(formula)) {
							hideWhenRecord.setFormula(newFormula);
						}
					}
					
					target.addRichTextRecord(record);
				});
			}
		});
		
		if (doc.hasItem("$fields")) {
			//$fields contains an array of the fieldnames
			List<String> oldFields = doc.getAsList("$fields", String.class, null);
			if (oldFields!=null) {
				List<String> newFields = oldFields.stream()
						.map((fieldName) -> {
							if (renamedFields.containsKey(fieldName)) {
								return renamedFields.get(fieldName);
							}
							else {
								return fieldName;
							}
						})
						.collect(Collectors.toList());
				
				doc.replaceItemValue("$fields", newFields);
			}
		}
		
		if (doc.hasItem("$WindowTitle")) {
			String windowFormula = doc.get("$WindowTitle", String.class, "");
			
			if (!StringUtil.isEmpty(windowFormula)) {
				String newWindowFormula = StringUtil.replaceAllMatches(windowFormula, replacements);
				if (!newWindowFormula.equals(windowFormula)) {
					Formula compiledWindowFormula = doc.getParentDatabase().getParentDominoClient().createFormula(newWindowFormula);
					doc.replaceItemValue("$WindowTitle", compiledWindowFormula);
				}
			}
		}
		
		return doc;
	}

}
