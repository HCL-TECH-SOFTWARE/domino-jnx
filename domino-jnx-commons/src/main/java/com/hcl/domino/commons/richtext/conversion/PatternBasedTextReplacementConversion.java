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
package com.hcl.domino.commons.richtext.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hcl.domino.commons.util.TriConsumer;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.conversion.IRichTextConversion;
import com.hcl.domino.richtext.records.CDBegin;
import com.hcl.domino.richtext.records.CDEnd;
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
 * For each match, the {@link TriConsumer} receives the <code>Matcher</code> object and {@link FontStyle} of the replaced text
 * and can produce replacement richtext content by writing data to the {@link RichTextWriter}:<br>
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
		AtomicBoolean inTextBegin = new AtomicBoolean();
		List<RichTextRecord<?>> txtBeginRecords = new ArrayList<>();
		
		source.forEach((record) -> {
			if (record instanceof CDBegin && ((CDBegin)record).getSignature() == RichTextConstants.SIG_CD_TEXT) {
				inTextBegin.set(true);
				txtBeginRecords.clear();
				txtBeginRecords.add(record);
			}
			else if (record instanceof CDEnd && ((CDEnd)record).getSignature() == RichTextConstants.SIG_CD_TEXT) {
				inTextBegin.set(false);
				
				//txtBeginRecords contains CDBEGIN, CDTEXT, CDCOLOR, CDEND
				CDText txtRecord = txtBeginRecords
						.stream()
						.filter(CDText.class::isInstance)
						.map(CDText.class::cast)
						.findFirst()
						.get();
				
				String txt = txtRecord.getText();
				Matcher matcher = pattern.matcher(txt);
				int currIdx = 0;

		        while (matcher.find()) {
		        	int startIdx = matcher.start();
		        	int endIdx = matcher.end();
		        	
		        	if (startIdx>currIdx) {
		        		String preTxt = txt.substring(currIdx, startIdx);
		        		if (preTxt.length()>0) {
		        			//insert whole BEGIN/END block with CDTEXT, but modify its text content
		        			for (RichTextRecord<?> currRecordOfBeginEndBlock : txtBeginRecords) {
		        				if (currRecordOfBeginEndBlock instanceof CDText) {
		        					target.addRichTextRecord(CDText.class, (preTxtRecord) -> {
				        				preTxtRecord.setStyle(((CDText)currRecordOfBeginEndBlock).getStyle());
						        		preTxtRecord.setText(preTxt);
				        			});
		        				}
		        				else {
		        					target.addRichTextRecord(currRecordOfBeginEndBlock);
		        				}
		        			}
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
		        		for (RichTextRecord<?> currRecordOfBeginEndBlock : txtBeginRecords) {
	        				if (currRecordOfBeginEndBlock instanceof CDText) {
	        					target.addRichTextRecord(CDText.class, (postTxtRecord) -> {
	        						postTxtRecord.setStyle(((CDText)currRecordOfBeginEndBlock).getStyle());
	        						postTxtRecord.setText(postTxt);
			        			});
	        				}
	        				else {
	        					target.addRichTextRecord(currRecordOfBeginEndBlock);
	        				}
	        			}
		        	}
		        }
			}
			else if (inTextBegin.get()) {
				//keep recording content of CDTEXT begin/end block until the end is reached
				txtBeginRecords.add(record);
			}
			else if (record instanceof CDText) {
				//inline CDTEXT without begin/end (so no special color to take care of)
				
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
		        			target.addRichTextRecord(CDText.class, (preTxtRecord) -> {
		        				preTxtRecord.setStyle(txtRecord.getStyle());
				        		preTxtRecord.setText(preTxt);
		        			});
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
		        		target.addRichTextRecord(CDText.class, (postTxtRecord) -> {
		        			postTxtRecord.setStyle(txtRecord.getStyle());
			        		postTxtRecord.setText(postTxt);
		        		});
		        	}
		        }
			}
			else {
				target.addRichTextRecord(record);
			}
		});
	}




	
}
