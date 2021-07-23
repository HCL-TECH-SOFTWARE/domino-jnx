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
package com.hcl.domino.richtext.records;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(
	name="CDCAPTION",
	members={
		@StructureMember(name="Header", type=WSIG.class),
		@StructureMember(name="wLength", type=short.class, unsigned=true),
		@StructureMember(name="Position", type=CDCaption.Position.class),
		@StructureMember(name="FontID", type=FontStyle.class),
		@StructureMember(name="FontColor", type=ColorValue.class),
		@StructureMember(name="Reserved", type=byte[].class, length=11)
	}
)
public interface CDCaption extends RichTextRecord<WSIG> {
	enum Position implements INumberEnum<Byte> {
		BELOW_CENTER(RichTextConstants.CAPTION_POSITION_BELOW_CENTER),
		MIDDLE_CENTER(RichTextConstants.CAPTION_POSITION_MIDDLE_CENTER)
		;
		private final byte value;
		Position(byte value) { this.value = value; }
		
		@Override
		public long getLongValue() {
			return value;
		}
		@Override
		public Byte getValue() {
			return value;
		}
	}
	
	@StructureGetter("Header")
	@Override
	WSIG getHeader();
	
	@StructureGetter("wLength")
	int getLength();
	@StructureSetter("wLength")
	CDCaption setLength(int length);
	
	@StructureGetter("Position")
	Position getPosition();
	@StructureSetter("Position")
	CDCaption setPosition(Position position);
	
	@StructureGetter("FontID")
	FontStyle getFontID();
	
	@StructureGetter("FontColor")
	ColorValue getFontColor();
	
	/**
	 * Stores the text for this caption in the variable data portion of this record.
	 * 
	 * <p>The buffer will be resized, if necessary, to hold the text value.</p>
	 * 
	 * <p>This method also sets the {@code wLength} property to the appropriate value.</p>
	 * 
	 * @param captionText the caption text to set
	 * @return this record
	 */
	default CDCaption setCaptionText(String captionText) {
		byte[] captionTextBytes = captionText.getBytes(Charset.forName("LMBCS-native")); //$NON-NLS-1$
		resizeVariableData(captionTextBytes.length);
		setLength(captionTextBytes.length);
		
		ByteBuffer buf = getVariableData();
		buf.put(captionTextBytes);
		return this;
	}
}
