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

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.LengthValue;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(
	name="CDACTIONBAREXT",
	members={
		@StructureMember(name="Header", type=WSIG.class),
		@StructureMember(name="BackColor", type=ColorValue.class),
		@StructureMember(name="LineColor", type=ColorValue.class),
		@StructureMember(name="FontColor", type=ColorValue.class),
		@StructureMember(name="ButtonColor", type=ColorValue.class),
		@StructureMember(name="BtnBorderDisplay", type=CDActionBarExt.BorderDisplay.class),
		@StructureMember(name="wAppletHeight", type=short.class, unsigned=true),
		@StructureMember(name="wBarBackgroundRepeat", type=CDActionBarExt.BackgroundRepeat.class),
		@StructureMember(name="BtnWidthStyle", type=CDActionBarExt.WidthStyle.class),
		@StructureMember(name="BtnTextJustify", type=CDActionBarExt.TextJustify.class),
		@StructureMember(name="wBtnWidthAbsolute", type=short.class, unsigned=true),
		@StructureMember(name="wBtnInternalMargin", type=short.class, unsigned=true),
		@StructureMember(name="dwFlags", type=CDActionBarExt.Flag.class, bitfield=true),
		@StructureMember(name="barFontID", type=FontStyle.class),
		@StructureMember(name="barHeight", type=LengthValue.class),
		@StructureMember(name="Spare", type=int[].class, length=12)
	}
)
public interface CDActionBarExt extends RichTextRecord<WSIG> {
	enum BorderDisplay implements INumberEnum<Short> {
		ONMOUSEOVER(0),
		ALWAYS(1),
		NEVER(2),
		NOTES(3);
		private final short value;
		BorderDisplay(int value) { this.value = (short)value; }
		
		@Override
		public Short getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum BackgroundRepeat implements INumberEnum<Short> {
		REPEATONCE(1),
		REPEATVERT(2),
		REPEATHORIZ(3),
		TILE(4),
		CENTER_TILE(5),
		REPEATSIZE(6),
		REPEATCENTER(7);
		private final short value;
		BackgroundRepeat(int value) { this.value = (short)value; }
		
		@Override
		public Short getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum WidthStyle implements INumberEnum<Byte> {
		/** Width is calculated based on text length and image width */
		DEFAULT(0),
		/** Width is at least button background image width or wider if needed to fit text and image. */
		BACKGROUND(1),
		/** Width is set to value in wBtnWidthAbsolute */
		ABSOLUTE(2);
		private final byte value;
		WidthStyle(int value) { this.value = (byte)value; }
		
		@Override
		public Byte getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum TextJustify implements INumberEnum<Byte> {
		LEFT(0),
		CENTER(1),
		RIGHT(2);
		private final byte value;
		TextJustify(int value) { this.value = (byte)value; }
		
		@Override
		public Byte getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum Flag implements INumberEnum<Integer> {
		WIDTH_STYLE_VALID(0x00000001);
		private final int value;
		Flag(int value) { this.value = value; }
		
		@Override
		public Integer getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	
	@StructureGetter("Header")
	@Override
	WSIG getHeader();
	
	@StructureGetter("BackColor")
	ColorValue getBackgroundColor();
	
	@StructureGetter("LineColor")
	ColorValue getLineColor();
	
	@StructureGetter("FontColor")
	ColorValue getFontColor();
	
	@StructureGetter("ButtonColor")
	ColorValue getButtonColor();
	
	@StructureGetter("BtnBorderDisplay")
	BorderDisplay getBorderDisplay();
	@StructureSetter("BtnBorderDisplay")
	CDActionBarExt setBorderDisplay(BorderDisplay borderDisplay);
	
	@StructureGetter("wAppletHeight")
	int getAppletHeight();
	@StructureSetter("wAppletHeight")
	CDActionBarExt setAppletHeight(int appletHeight);
	
	@StructureGetter("wBarBackgroundRepeat")
	BackgroundRepeat getBackgroundRepeat();
	@StructureSetter("wBarBackgroundRepeat")
	CDActionBarExt setBackgroundRepeat(BackgroundRepeat backgroundRepeat);
	
	@StructureGetter("BtnWidthStyle")
	WidthStyle getWidthStyle();
	@StructureSetter("BtnWidthStyle")
	CDActionBarExt setWidthStyle(WidthStyle widthStyle);
	
	@StructureGetter("BtnTextJustify")
	TextJustify getTextJustify();
	@StructureSetter("BtnTextJustify")
	CDActionBarExt setTextJustify(TextJustify textJustify);
	
	@StructureGetter("wBtnWidthAbsolute")
	int getButtonWidth();
	@StructureSetter("wBtnWidthAbsolute")
	CDActionBarExt setButtonWidth(int buttonWidth);
	
	@StructureGetter("wBtnInternalMargin")
	int getButtonInternalMargin();
	@StructureSetter("wBtnInternalMargin")
	CDActionBarExt setButtonInternalMargin(int internalMargin);
	
	@StructureGetter("dwFlags")
	Set<Flag> getFlags();
	@StructureSetter("dwFlags")
	CDActionBarExt setFlags(Collection<Flag> flags);
	
	@StructureGetter("barFontID")
	FontStyle getFontStyle();
	
	@StructureGetter("barHeight")
	LengthValue getHeight();
}
