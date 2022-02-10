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
package com.hcl.domino.jna.richtext.op;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.hcl.domino.data.Document;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.conversion.IRichTextConversion;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * RichText conversion code for {@link Document#convertRichTextItem(String, IRichTextConversion...)}
 * to replace text in an image resource formula
 * 
 * @author Karsten Lehmann
 */
public class ReplaceImageResourceFormulaConversion implements IRichTextConversion {
	private Map<Pattern,Function<Matcher,String>> replacements;

	public ReplaceImageResourceFormulaConversion(Map<Pattern,Function<Matcher,String>> replacements) {
		this.replacements = replacements;
	}
	
	@Override
	public void richTextNavigationStart() {
	}
	
	@Override
	public void richTextNavigationEnd() {
	}

	@Override
	public boolean isMatch(List<RichTextRecord<?>> nav) {
		return nav
				.stream()
				.anyMatch((record) -> {
					if (record instanceof CDResource) {
						CDResource resourceRecord = (CDResource) record;
						Optional<List<String>> namedElementFormula = resourceRecord.getNamedElementFormulas();
						if (namedElementFormula.isPresent()) {
							List<String> formulas = namedElementFormula.get();
							
							return replacements.keySet()
									.stream()
									.anyMatch((currPattern) -> {
									  for (String currFormula : formulas) {
	                    Matcher matcher = currPattern.matcher(currFormula);
	                    boolean matches = matcher.find();
									    if (matches) {
									      return true;
									    }
									  }
										return false;
									});
						}
					}
					return false;
				});
	}

	@Override
	public void convert(List<RichTextRecord<?>> source, RichTextWriter target) {
		for (RichTextRecord<?> currRecord : source) {
			if (currRecord instanceof CDResource) {
				CDResource resourceRecord = (CDResource) currRecord;
        Optional<List<String>> namedElementFormulas = resourceRecord.getNamedElementFormulas();
				
				if (namedElementFormulas.isPresent()) {
				  List<String> oldFormulas = namedElementFormulas.get();
	        List<String> newFormulas = oldFormulas
	            .stream()
	            .map((formula) -> {
	              String newFormula = formula;
	              for (Entry<Pattern,Function<Matcher,String>> currEntry : replacements.entrySet()) {
	                newFormula = replaceAllMatches(newFormula, currEntry.getKey(), currEntry.getValue());
	              }
	              return newFormula;
	            })
	            .collect(Collectors.toList());
	        
	        if (!oldFormulas.equals(newFormulas)) {
	          resourceRecord.setNamedElementFormulas(newFormulas);
	        }
				}
				
				target.addRichTextRecord(currRecord);
			}
			else {
				target.addRichTextRecord(currRecord);
			}
		}
	}

	protected String replaceAllMatches(String txt, Pattern pattern, Function<Matcher,String> fct) {
		StringBuffer sb = new StringBuffer();
		
		Matcher matcher = pattern.matcher(txt);
		int currIdx = 0;

        while (matcher.find()) {
        	int startIdx = matcher.start();
        	int endIdx = matcher.end();
        	
        	if (startIdx>currIdx) {
        		String preTxt = txt.substring(currIdx, startIdx);
        		if (preTxt.length()>0) {
        			sb.append(preTxt);
        		}
        	}
        	
        	currIdx = endIdx;
        	
        	String newTxt = fct.apply(matcher);
        	sb.append(newTxt);
        }
		
        //write remaining txt until the end
        if (currIdx < txt.length()) {
        	String postTxt = txt.substring(currIdx, txt.length());
        	if (postTxt.length()>0) {
        		sb.append(postTxt);
        	}
        }
	
        
		return sb.toString();
	}


}
