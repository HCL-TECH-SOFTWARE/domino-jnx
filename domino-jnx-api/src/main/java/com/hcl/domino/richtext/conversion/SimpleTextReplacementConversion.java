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
package com.hcl.domino.richtext.conversion;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple subclass of {@link AbstractTextReplacementConversion} that uses a from/to
 * map to find/replace text in richtext
 * 
 * @author Karsten Lehmann
 * @since 1.0.32
 */
public class SimpleTextReplacementConversion extends AbstractTextReplacementConversion {
	private Map<Pattern,String> fromPatternToString;

	/**
	 * Creates a new instance
	 * 
	 * @param fromTo from/to map of search string and replacement
	 * @param ignoreCase true to ignore the case when searching
	 */
	public SimpleTextReplacementConversion(Map<String,String> fromTo, boolean ignoreCase) {
		fromPatternToString = new HashMap<Pattern,String>();
		for (Entry<String,String> currEntry : fromTo.entrySet()) {
			String currFrom = currEntry.getKey();
			String currTo = currEntry.getValue();
			
			String currFromPattern = Pattern.quote(currFrom);
			Pattern pattern = ignoreCase ? Pattern.compile(currFromPattern, Pattern.CASE_INSENSITIVE) : Pattern.compile(currFromPattern);
			fromPatternToString.put(pattern, currTo);
		}
	}
	
	@Override
	protected boolean containsMatch(String txt) {
		for (Pattern currPattern : fromPatternToString.keySet()) {
			if (currPattern.matcher(txt).find()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected String replaceAllMatches(String txt) {
		String currTxt = txt;
		
		StringBuffer sb = new StringBuffer();
		
		for (Entry<Pattern,String> currEntry : fromPatternToString.entrySet()) {
			Pattern currPattern = currEntry.getKey();
			String currTo = currEntry.getValue();
			
			Matcher m = currPattern.matcher(currTxt);
			while (m.find()) {
				m.appendReplacement(sb, currTo);
			}
			m.appendTail(sb);
			currTxt = sb.toString();
			sb.setLength(0);
		}
		return currTxt;
	}

}
