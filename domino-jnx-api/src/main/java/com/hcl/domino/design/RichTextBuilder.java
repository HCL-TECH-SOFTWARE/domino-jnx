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

package com.hcl.domino.design;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hcl.domino.data.Document;
import com.hcl.domino.richtext.conversion.IRichTextConversion;

/**
 * Produces normal and design richtext by combining existing parts
 * 
 * @since 1.0.32
 */
public interface RichTextBuilder {

	/**
	 * Creates a new build context by copying the design of the specified form/subform
	 * 
	 * @param templateForm template form
	 * @return build context
	 */
	<F extends GenericFormOrSubform<F>> RichTextBuilderContext<F> from(F templateForm);

	/**
	 * Creates a new build context by copying the specified data document containing
	 * a richtext item
	 * 
	 * @param templateDoc document
	 * @param rtItemName richtext item
	 */
	RichTextBuilderContext<Document> from(Document templateDoc, String rtItemName);

	public interface RichTextBuilderContext<O> {
		
		/**
		 * Returns the parent builder
		 * 
		 * @return builder
		 */
		public RichTextBuilder getRichTextBuilder();
		
		/**
		 * Returns the type of the build result
		 * 
		 * @return type
		 */
		public Class<O> getResultType();
		
		/**
		 * Returns the name of the richtext item to apply the transformations
		 * 
		 * @return item name, e.g. $Body for design richtext
		 */
		public String getItemName();
		
		/**
		 * Inserts text for a label and formula/LotusScript code in the DB design
		 * 
		 * @param placeholder placeholder
		 * @param label replacement
		 * @return build context
		 */
		default RichTextBuilderContext<O> replace(String placeholder, String label) {
			Map<Pattern,Function<Matcher, Object>> replacementsWithPattern = new HashMap<>();
			Pattern pattern = Pattern.compile(Pattern.quote(placeholder), Pattern.CASE_INSENSITIVE);
			replacementsWithPattern.put(pattern, (matcher) -> {
				return label;
			});
			return replaceExt(replacementsWithPattern);
		}

		/**
		 * Replaces multiple texts in the specified design element with strings or other design elements
		 * 
		 * @param replacements mapping of placeholder / new text or design
		 * @return build context
		 */
		default RichTextBuilderContext<O> replace(Map<String,Object> replacements) {
			Map<Pattern,Function<Matcher, Object>> replacementsWithPattern = new HashMap<>();
			for (Entry<String,Object> currEntry : replacements.entrySet()) {
				Pattern currPattern = Pattern.compile(Pattern.quote(currEntry.getKey()), Pattern.CASE_INSENSITIVE);
				replacementsWithPattern.put(currPattern, (matcher) -> {
					return currEntry.getValue();
				});
			}
			
			return replaceExt(replacementsWithPattern);
		}

		/**
		 * Inserts placeholders for a label and formula/LotusScript code
		 * matching a pattern with dynamically computed design
		 * 
		 * @param pattern pattern
		 * @param labelFct function to compute replacement
		 * @return build context
		 */
		default RichTextBuilderContext<O> replace(Pattern pattern, Function<Matcher,Object> labelFct) {
			Map<Pattern,Function<Matcher, Object>> replacementsWithPattern = new HashMap<>();
			replacementsWithPattern.put(pattern, labelFct);
			return replaceExt(replacementsWithPattern);
		}

		/**
		 * Inserts a single design element for a label in the DB design
		 * 
		 * @param placeholder name of placeholder
		 * @param formDesign design to insert
		 * @return build context
		 */
		default RichTextBuilderContext<O> replace(String placeholder, GenericFormOrSubform<?> formDesign) {
			Map<Pattern,Function<Matcher, Object>> replacementsWithPattern = new HashMap<>();
			Pattern pattern = Pattern.compile(Pattern.quote(placeholder), Pattern.CASE_INSENSITIVE);
			replacementsWithPattern.put(pattern, (matcher) -> {
				return formDesign;
			});
			return replaceExt(replacementsWithPattern);
		}

		/**
		 * Inserts a single design element for a label in the DB design
		 * 
		 * @param placeholder name of placeholder
		 * @param buildCtx build context
		 * @return build context
		 */
		default RichTextBuilderContext<O> replace(String placeholder, RichTextBuilderContext<?> buildCtx) {
			Map<Pattern,Function<Matcher, Object>> replacementsWithPattern = new HashMap<>();
			Pattern pattern = Pattern.compile(Pattern.quote(placeholder), Pattern.CASE_INSENSITIVE);
			replacementsWithPattern.put(pattern, (matcher) -> {
				return buildCtx;
			});
			return replaceExt(replacementsWithPattern);
		}

		/**
		 * Replaces multiple texts in the specified richtext with values computed via functions
		 * 
		 * @param replacements mapping of pattern and computation function
		 * @return build context
		 */
		RichTextBuilderContext<O> replaceExt(Map<Pattern,Function<Matcher, Object>> replacements);

		/**
		 * Repeats richtext, applying replace operations on each repetition
		 * 
		 * @param repetitions number of repetitions
		 * @param replacements mappings of patterns and functions to compute the replace value for each repetition
		 * @return build context
		 */
		RichTextBuilderContext<O> repeat(int repetitions,
				Map<Pattern, BiFunction<Integer,Matcher,Object>> replacements);

		/**
		 * Repeats richtext, applying replace operations on each repetition
		 * 
		 * @param repetitions number of repetitions
		 * @param from placeholders
		 * @param replacements function to compute new values for each repetition and placeholder
		 * @return build context
		 */
		default RichTextBuilderContext<O> repeat(int repetitions, Collection<String> from,
				BiFunction<Integer,String,Object> replacements) {
			
			Map<Pattern, BiFunction<Integer,Matcher,Object>> replacementsWithPatterns = new HashMap<>();
			for (String currFrom : from) {
				String currFromQuoted = Pattern.quote(currFrom);
				Pattern currFromPattern = Pattern.compile(currFromQuoted, Pattern.CASE_INSENSITIVE);

				replacementsWithPatterns.put(currFromPattern, (idx,matcher)->{
					return replacements.apply(idx, currFrom);
				});
			}
			
			return repeat(repetitions, replacementsWithPatterns);
		}

		/**
		 * Locates the first table in the specified design element and repeats
		 * its rows, applying string replacements
		 * 
		 * @param rowIdx index of row to repeat
		 * @param nrOfRows number of repetitions
		 * @param from strings to replace
		 * @param toFct function to compute what to insert
		 * @param ignoreCase true for case-insensitive matching
		 * @return build context
		 */
		default RichTextBuilderContext<O> repeatTableRow(int rowIdx, int nrOfRows, Collection<String> from,
				BiFunction<Integer,String,Object> toFct) {
			
			Map<Pattern, BiFunction<Integer,Matcher,Object>> replacements = new HashMap<>();
			if (from!=null) {
				for (String currFrom : from) {
					String currFromQuoted = Pattern.quote(currFrom);
					Pattern currFromPattern = Pattern.compile(currFromQuoted, Pattern.CASE_INSENSITIVE);
					
					replacements.put(currFromPattern, (idx,matcher) -> {
						return toFct==null ? "" : toFct.apply(idx, currFrom);
					});
				}
			}
			
			return repeatTableRowExt(rowIdx, nrOfRows, replacements);
		}

		/**
		 * Locates the first table in the specified design element and repeats
		 * its rows, applying string replacements
		 * 
		 * @param rowIdx index of row to repeat
		 * @param nrOfRows number of repetitions
		 * @param replacements map of patterns with functions to compute what to insert
		 * @return build context
		 */
		RichTextBuilderContext<O> repeatTableRowExt(int rowIdx, int nrOfRows,
				Map<Pattern, BiFunction<Integer,Matcher,Object>> replacements);

		/**
		 * Renames the first field in the form/subform and modifies the formula/LotusScript event
		 * code and hide-when formulas accordingly
		 * 
		 * @param newFieldName new field name
		 * @return build context
		 */
		RichTextBuilderContext<O> renameField(String newFieldName);

		/**
		 * Renames a field in the form/subform and modifies the formula/LotusScript event
		 * code and hide-when formulas accordingly
		 * 
		 * @param oldFieldName old fieldname
		 * @param newFieldName new fieldname
		 * @return build context
		 */
		default RichTextBuilderContext<O> renameField(String oldFieldName, String newFieldName) {
			Map<Pattern,Function<Matcher,String>> replacements = new HashMap<>();
			Pattern pattern = Pattern.compile(Pattern.quote(oldFieldName), Pattern.CASE_INSENSITIVE);
			replacements.put(pattern, (matcher) -> {
				return newFieldName;
			});
			
			return renameFields(replacements);
		}

		/**
		 * Renames the names of fields in the form/subform and modifies the formula/LotusScript event
		 * code and hide-when formulas accordingly
		 * 
		 * @param replacements mapping of fieldname pattern and a function to compute the new name
		 * @return build context
		 */
		RichTextBuilderContext<O> renameFields(Map<Pattern,Function<Matcher,String>> replacements);

		/**
		 * Rewrites image resource formulas
		 * 
		 * @param oldTxt text to replace
		 * @param newTxt text to insert
		 * @return build context
		 */
		default RichTextBuilderContext<O> replaceInImageResourceFormula(String oldTxt, String newTxt) {
			Map<Pattern,Function<Matcher,String>> replacements = new HashMap<>();
			Pattern pattern = Pattern.compile(Pattern.quote(oldTxt), Pattern.CASE_INSENSITIVE);
			replacements.put(pattern, (matcher)->{
				return newTxt;
			});
			
			return replaceInImageResourceFormula(replacements);
		}

		/**
		 * Rewrites image resource formulas
		 * 
		 * @param replacements mapping of regexp patterns and functions to compute the new value
		 * @return build context
		 */
		RichTextBuilderContext<O> replaceInImageResourceFormula(Map<Pattern,Function<Matcher,String>> replacements);
		
		/**
		 * Applies a custom build operation on the work document (supports switching
		 * to a new work document if required)
		 * 
		 * @param op operation
		 * @return build context
		 */
		RichTextBuilderContext<O> apply(RichTextBuilderOperation op);
		
		/**
		 * Applies a custom inline richtext conversion on the work document
		 * 
		 * @param rtConv conversion
		 * @return build context
		 */
		RichTextBuilderContext<O> apply(IRichTextConversion rtConv);
		
		/**
		 * Applies the replace/repeat operations and produces the final result (unsaved))
		 * 
		 * @return result
		 */
		O build();

	}
	
	/**
	 * One operation in the richtext build pipeline
	 */
	public interface RichTextBuilderOperation {

		/**
		 * Applies a richtext build operation on the work document, e.g. to replace
		 * placeholder text with content.
		 * 
		 * @param ctx build context
		 * @param doc work document
		 * @return passed document with applied operation or a new one
		 */
		Document apply(RichTextBuilderContext<?> ctx, Document doc);
		
	}

}
