package com.hcl.domino.jna.richtext;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.conversion.IRichTextConversion;
import com.hcl.domino.richtext.conversion.SimpleTextReplacementConversion;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.RichTextRecord;

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
						Optional<String> namedElementFormula = resourceRecord.getNamedElementFormula();
						if (namedElementFormula.isPresent()) {
							String formula = namedElementFormula.get();
							
							return replacements.keySet()
									.stream()
									.anyMatch((currPattern) -> {
										Matcher matcher = currPattern.matcher(formula);
										boolean matches = matcher.find();
										return matches;
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
				Optional<String> namedElementFormula = resourceRecord.getNamedElementFormula();
				if (namedElementFormula.isPresent()) {
					String formula = namedElementFormula.get();
					String newFormula = formula;
					for (Entry<Pattern,Function<Matcher,String>> currEntry : replacements.entrySet()) {
						newFormula = replaceAllMatches(newFormula, currEntry.getKey(), currEntry.getValue());
					}
					if (!newFormula.equals(formula)) {
						resourceRecord.setNamedElementFormula(newFormula);
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
