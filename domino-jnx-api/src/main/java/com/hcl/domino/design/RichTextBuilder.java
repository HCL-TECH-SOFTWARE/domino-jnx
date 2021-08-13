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

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.hcl.domino.data.Document;

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
		RichTextBuilderContext<O> replace(String placeholder, String label);

		/**
		 * Replaces multiple texts in the specified design element with strings or other design elements
		 * 
		 * @param replacements mapping of placeholder / new text or design
		 * @return build context
		 */
		RichTextBuilderContext<O> replace(Map<String,Object> replacements);
	
		/**
		 * Inserts placeholders for a label and formula/LotusScript code
		 * matching a pattern with dynamically computed design
		 * 
		 * @param pattern pattern
		 * @param labelFct function to compute replacement
		 * @return build context
		 */
		RichTextBuilderContext<O> replace(Pattern pattern, Function<Matcher,Object> labelFct);

		/**
		 * Inserts a single design element for a label in the DB design
		 * 
		 * @param placeholder name of placeholder
		 * @param formDesign design to insert
		 * @return build context
		 */
		RichTextBuilderContext<O> replace(String placeholder, GenericFormOrSubform<?> formDesign);

		/**
		 * Inserts a single design element for a label in the DB design
		 * 
		 * @param placeholder name of placeholder
		 * @param buildCtx build context
		 * @return build context
		 */
		RichTextBuilderContext<O> replace(String placeholder, RichTextBuilderContext<?> buildCtx);

		/**
		 * Repeats form/subform design
		 * 
		 * @param repetitions number of repetitions
		 * @param consumer consumer is called for each row to provide mappings, either (String,String), (String,Form), (String,Subform) or (String,FormBuildContext)
		 * @return build context
		 */
		RichTextBuilderContext<O> repeat(int repetitions,
				BiConsumer<Integer,Map<String,Object>> consumer);

		/**
		 * Repeats the design of a form/subform and applies text/design replacements for each repetition
		 * 
		 * @param <T> type of form/subform
		 * @param replacements stream of replacements for each row
		 * @return build context
		 */
		RichTextBuilderContext<O> repeat(Stream<Map<String,Object>> replacements);

		/**
		 * Locates the first table in the specified design element and repeats
		 * its rows, applying string replacements
		 * 
		 * @param rowIdx index of row to repeat
		 * @param replacements stream of replacements for each row
		 * @return build context
		 */
		RichTextBuilderContext<O> repeatTableRow(int rowIdx, Stream<Map<String,String>> replacements);
		
		/**
		 * Locates the first table in the specified design element and repeats
		 * its rows, applying string replacements
		 * 
		 * @param rowIdx index of row to repeat
		 * @param nrOfRows number of rows
		 * @param consumer consumer is called for each row to provide mappings, either (String,String), (String,Form), (String,Subform) or (String,FormBuildContext)
		 * @return build context
		 */
		RichTextBuilderContext<O> repeatTableRow(int rowIdx, int nrOfRows, BiConsumer<Integer, Map<String,Object>> consumer);
		
		/**
		 * Renames the first field in the form/subform and modifies the formula/LotusScript event
		 * code accordingly
		 * 
		 * @param newFieldName new field name
		 * @return build context
		 */
		RichTextBuilderContext<O> renameField(String newFieldName);

		/**
		 * Renames the first field in the form/subform and modifies the formula/LotusScript event
		 * code accordingly
		 * 
		 * @param newFieldNames mapping of old and new field names
		 * @return build context
		 */
		RichTextBuilderContext<O> renameFields(Map<String,String> newFieldNames);

		/**
		 * Applies a custom operation on the work document
		 * 
		 * @param op operation
		 * @return build context
		 */
		RichTextBuilderContext<O> apply(RichTextBuilderOperation op);
		
		/**
		 * Applies the replace/repeat operations and produces the final result (unsaved))
		 * 
		 * @return result
		 */
		O build();

	}
	
	public interface RichTextBuilderOperation {

		Document apply(RichTextBuilderContext<?> ctx, Document doc);
		
	}

}
