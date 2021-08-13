package com.hcl.domino.richtext.conversion;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Richtext conversion that scans the text content of a richtext item
 * and replaces content based on a regular expression and
 * with dynamically computed content.<br>
 * <br>
 * An example pattern (using Named RegExp Groups) could look like this:<br>
 * <br>
 * <code>
 * Pattern pattern = Pattern.compile("%includeimage:(?&lt;imgname&gt;[^%]+)\\%");<br>
 * </code>
 * <br>
 * This could be used to replace dynamic placeholders in a text with images from the local disk:<br>
 * <br>
 * <code>
 * "Text before image %includeimage:c:\temp\image.jpg% Text after image"<br>
 * </code>
 * <br>
 * For each match, the <code>BiConsumer</code> receives the <code>Matcher</code> object
 * and can produce replacement richtext content by writing data to the <code>RichTextWriter</code>:<br>
 * <br>
 * <code>
 * target.addImage(Paths.get(matcher.group("imgname")));
 * </code>
 * 
 * @author Karsten Lehmann
 */
public class PatternBasedTextReplacementConversion implements IRichTextConversion {
	private Pattern pattern;
	private BiConsumer<Matcher, RichTextWriter> consumer;
	
	/**
	 * Creates a new instance
	 * 
	 * @param pattern search pattern
	 * @param consumer consumer to produce new content
	 */
	public PatternBasedTextReplacementConversion(Pattern pattern, BiConsumer<Matcher, RichTextWriter> consumer) {
		this.pattern = pattern;
		this.consumer = consumer;
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
				.filter(s -> s instanceof CDText)
				.map(CDText.class::cast)
				.map(CDText::getText)
				.anyMatch((txt) -> {
					Matcher matcher = pattern.matcher(txt);
					return matcher.find();
				});
	}

	@Override
	public void convert(List<RichTextRecord<?>> source, RichTextWriter target) {
		source.forEach((record) -> {
			if (record instanceof CDText) {
				CDText txtRecord = (CDText) record;
				String txt = txtRecord.getText();
				
				Matcher matcher = pattern.matcher(txt);
				int currIdx = 0;

		        while (matcher.find()) {
		        	int startIdx = matcher.start();
		        	int endIdx = matcher.end();
		        	
		        	if (startIdx>currIdx) {
		        		String preTxt = txt.substring(currIdx, startIdx);
		        		if (preTxt.length()>0) {
			        		target.addText(preTxt);
		        		}
		        	}
		        	
		        	currIdx = endIdx;
		        	
		        	//consumer receives regexp matcher and writes to the target
		        	this.consumer.accept(matcher, target);
		        }
				
		        //write remaining txt until the end
		        if (currIdx < txt.length()) {
		        	String postTxt = txt.substring(currIdx, txt.length());
		        	if (postTxt.length()>0) {
		        		target.addText(postTxt);
		        	}
		        }
			}
			else {
				target.addRichTextRecord(record);
			}
		});
	}




	
}
