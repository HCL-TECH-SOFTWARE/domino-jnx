package com.hcl.domino.commons.richtext.conversion;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.commons.util.DumpUtil;
import com.hcl.domino.commons.util.TriConsumer;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.conversion.IRichTextConversion;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.FontStyle;

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
	private TriConsumer<Matcher, FontStyle, RichTextWriter> consumer;
	
	/**
	 * Creates a new instance
	 * 
	 * @param pattern search pattern
	 * @param consumer consumer to produce new content
	 */
	public PatternBasedTextReplacementConversion(Pattern pattern, TriConsumer<Matcher, FontStyle, RichTextWriter> consumer) {
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
			        		CDText preTxtRecord = MemoryStructureUtil.newStructure(CDText.class, 0);
			        		preTxtRecord.setStyle(txtRecord.getStyle());
			        		preTxtRecord.setText(preTxt);
			        		//add CDText WSIG
			        		preTxtRecord.getData().put((byte) 0x85).put((byte) 0xff);
			        		target.addRichTextRecord(preTxtRecord);
		        		}
		        	}
		        	
		        	currIdx = endIdx;
		        	
		        	//consumer receives regexp matcher and writes to the target
		        	this.consumer.accept(matcher, txtRecord.getStyle(), target);
		        }
				
		        //write remaining txt until the end
		        if (currIdx < txt.length()) {
		        	String postTxt = txt.substring(currIdx, txt.length());
		        	if (postTxt.length()>0) {
		        		CDText postTxtRecord = MemoryStructureUtil.newStructure(CDText.class, 0);
		        		postTxtRecord.setStyle(txtRecord.getStyle());
		        		postTxtRecord.setText(postTxt);
		        		//add CDText WSIG
		        		postTxtRecord.getData().put((byte) 0x85).put((byte) 0xff);
		        		target.addRichTextRecord(postTxtRecord);
		        	}
		        }
			}
			else {
				target.addRichTextRecord(record);
			}
		});
	}




	
}
