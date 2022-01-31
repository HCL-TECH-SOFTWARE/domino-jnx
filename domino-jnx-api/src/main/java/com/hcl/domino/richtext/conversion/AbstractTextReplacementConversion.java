/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.domino.richtext.conversion;

import java.util.List;

import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Abstract base class for a search/replace in richtext
 * 
 * @author Karsten Lehmann
 * @since 1.0.32
 */
public abstract class AbstractTextReplacementConversion implements IRichTextConversion {

	@Override
	public void richTextNavigationStart() {
	}
	
	@Override
	public void richTextNavigationEnd() {
	}

	/**
	 * Implement this method to check if the specified text contains
	 * text to be replaced
	 * 
	 * @param txt text
	 * @return true if match
	 */
	protected abstract boolean containsMatch(String txt);
	
	/**
	 * Replace all matches in the specified text
	 * 
	 * @param txt text
	 * @return new text
	 */
	protected abstract String replaceAllMatches(String txt);

	@Override
	public boolean isMatch(List<RichTextRecord<?>> nav) {
		return nav
				.stream()
				.filter(s -> s instanceof CDText)
		        .map(CDText.class::cast)
		        .map(CDText::getText)
		        .anyMatch(this::containsMatch);
	}

	@Override
	public void convert(List<RichTextRecord<?>> source, RichTextWriter target) {
		source.forEach((record) -> {
			if (record instanceof CDText) {
				CDText textRecord = (CDText) record;
				String txt = textRecord.getText();
				if (containsMatch(txt)) {
					String replacedTxt = replaceAllMatches(txt);
					textRecord.setText(replacedTxt);
				}
			}
			target.addRichTextRecord(record);
		});
	}


}
