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
package com.hcl.domino.richtext.structures;

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

@StructureDefinition(
	name="FONTID",
	endianSensitive=true,
	members={
		@StructureMember(name="Face", type=FontStyle.StandardFonts.class),
		@StructureMember(name="Attrib", type=FontStyle.Attribute.class, bitfield=true),
		@StructureMember(name="Color", type=FontStyle.StandardColors.class),
		@StructureMember(name="PointSize", type=byte.class, unsigned=true)
	}
)
public interface FontStyle extends MemoryStructure {
	@StructureGetter("PointSize")
	short getPointSize();
	
	@StructureSetter("PointSize")
	FontStyle setPointSize(int size);
	
	@StructureGetter("Face")
	StandardFonts getFontFace();

	@StructureSetter("Face")
	FontStyle setFontFace(StandardFonts font);
	
	@StructureGetter("Color")
	StandardColors getColor();
	
	@StructureSetter("Color")
	FontStyle setColor(StandardColors color);
	
	@StructureGetter("Attrib")
	Set<Attribute> getAttributes();
	
	@StructureSetter("Attrib")
	FontStyle setAttributes(Collection<Attribute> attributes);
	
	default FontStyle setBold(boolean b) {
		Set<Attribute> style = getAttributes();
		style.add(Attribute.BOLD);
		setAttributes(style);
		return this;
	}
	
	default boolean isBold() {
		return getAttributes().contains(Attribute.BOLD);
	}
	
	default FontStyle setItalic(boolean b) {
		Set<Attribute> style = getAttributes();
		style.add(Attribute.ITALIC);
		setAttributes(style);
		return this;
	}
	
	default boolean isItalic() {
		return getAttributes().contains(Attribute.ITALIC);
	}
	
	default FontStyle setUnderline(boolean b) {
		Set<Attribute> style = getAttributes();
		style.add(Attribute.UNDERLINE);
		setAttributes(style);
		return this;
	}
	
	default boolean isUnderline() {
		return getAttributes().contains(Attribute.UNDERLINE);
	}
	
	default FontStyle setStrikeout(boolean b) {
		Set<Attribute> style = getAttributes();
		style.add(Attribute.STRIKEOUT);
		setAttributes(style);
		return this;
	}
	
	default boolean isStrikeout() {
		return getAttributes().contains(Attribute.STRIKEOUT);
	}
	
	default FontStyle setSuper(boolean b) {
		Set<Attribute> style = getAttributes();
		style.add(Attribute.SUPER);
		setAttributes(style);
		return this;
	}
	
	default boolean isSuper() {
		return getAttributes().contains(Attribute.SUPER);
	}
	
	default FontStyle setSub(boolean b) {
		Set<Attribute> style = getAttributes();
		style.add(Attribute.SUB);
		setAttributes(style);
		return this;
	}
	
	default boolean isSub() {
		return getAttributes().contains(Attribute.SUB);
	}
	
	default FontStyle setShadow(boolean b) {
		Set<Attribute> style = getAttributes();
		style.add(Attribute.SHADOW);
		setAttributes(style);
		return this;
	}
	
	default boolean isShadow() {
		return getAttributes().contains(Attribute.SHADOW);
	}
	
	default FontStyle setExtrude(boolean b) {
		Set<Attribute> style = getAttributes();
		style.add(Attribute.EXTRUDE);
		setAttributes(style);
		return this;
	}
	
	default boolean isExtrude() {
		return getAttributes().contains(Attribute.EXTRUDE);
	}
	
	/**
	 * These symbols define the standard type faces.
	 * The Face member of the {@link FontStyle} may be either one of these standard font faces,
	 * or a font ID resolved by a font table.
	 */
	public enum StandardFonts implements INumberEnum<Byte> {
		/** (e.g. Times Roman family) */
		ROMAN(0),
		/** (e.g. Helv family) */
		SWISS(1),
		/** (e.g. Monotype Sans WT) */
		UNICODE(2),
		/** (e.g. Arial */
		USERINTERFACE(3),
		/** (e.g. Courier family) */
		TYPEWRITER(4),
		/** returned if font is not in the standard table; cannot be set via {@link FontStyle#setFontFace(StandardFonts)} */
		CUSTOMFONT((byte)255);
		
		private byte m_face;
		
		StandardFonts(int face) {
			m_face = (byte) (face & 0xff);
		}
		
		@Override
		public long getLongValue() {
			return m_face;
		}
		
		@Override
		public Byte getValue() {
			return m_face;
		}
	}
	
	/**
	 * These symbols are used to specify text color, graphic color and background color in a variety of C API structures.
	 * 
	 * @author Karsten Lehmann
	 */
	public enum StandardColors implements INumberEnum<Byte> {
		BLACK(0),
		WHITE(1),
		RED(2),
		GREEN(3),
		BLUE(4),
		MAGENTA(5),
		YELLOW(6),
		CYAN(7),
		DKRED(8),
		DKGREEN(9),
		DKBLUE(10),
		DKMAGENTA(11),
		DKYELLOW(12),
		DKCYAN(13),
		GRAY(14),
		LTGRAY(15);
		
		private byte m_color;
		
		StandardColors(int colorIdx) {
			m_color = (byte) (colorIdx & 0xff);
		}
		
		@Override
		public long getLongValue() {
			return m_color;
		}
		
		@Override
		public Byte getValue() {
			return m_color;
		}
	}
	
	public enum Attribute implements INumberEnum<Byte> {
		BOLD((byte)0x01),
		ITALIC((byte)0x02),
		UNDERLINE((byte)0x04),
		STRIKEOUT((byte)0x08),
		SUPER((byte)0x10),
		SUB((byte)0x20),
		EFFECT((byte)0x80),
		SHADOW((byte)0x80),
		EMBOSS((byte)0x90),
		EXTRUDE((byte)0xa0)
		;
		private final byte value;
		Attribute(byte value) { this.value = value; }
		
		@Override
		public long getLongValue() {
			return value;
		}
		@Override
		public Byte getValue() {
			return value;
		}
	}
	
}
