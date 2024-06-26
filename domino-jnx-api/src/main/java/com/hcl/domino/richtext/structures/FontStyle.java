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
package com.hcl.domino.richtext.structures;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.FontAttribute;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

@StructureDefinition(
  name = "FONTID",
  endianSensitive = true,
  members = {
    @StructureMember(name = "Face", type = byte.class),
    @StructureMember(name = "Attrib", type = FontAttribute.class, bitfield = true),
    @StructureMember(name = "Color", type = byte.class, unsigned = true),
    @StructureMember(name = "PointSize", type = byte.class, unsigned = true)
  }
)
public interface FontStyle extends MemoryStructure {
  @StructureGetter("Attrib")
  Set<FontAttribute> getAttributes();
  
  @StructureGetter("Attrib")
  byte getAttributesRaw();

  @StructureSetter("Attrib")
  FontStyle setAttributes(Collection<FontAttribute> attributes);
  
  @StructureSetter("Attrib")
  FontStyle setAttributesRaw(byte attributes);

  @StructureGetter("Color")
  Optional<StandardColors> getColor();

  @StructureSetter("Color")
  FontStyle setColor(StandardColors color);
  
  @StructureGetter("Color")
  short getColorRaw();
  
  @StructureSetter("Color")
  FontStyle setColorRaw(short color);

  @StructureGetter("Face")
  byte getFontFace();

  @StructureSetter("Face")
  FontStyle setFontFace(byte font);

  @StructureGetter("PointSize")
  short getPointSize();

  @StructureSetter("PointSize")
  FontStyle setPointSize(int size);
  
  /**
   * Retrieves the {@link StandardFonts} value corresponding to
   * {@link #getFontFace()}. When this value is empty, then the font is a custom
   * font that is defined in a mechanism specific to different areas of storage.
   * 
   * @return an {@link Optional} describing the {@link StandardFonts} specified,
   *         or an empty one if the value is not a standard font
   */
  default Optional<StandardFonts> getStandardFont() {
    short fontId = getFontFace();
    return Arrays.stream(StandardFonts.values())
      .filter(f -> Byte.toUnsignedInt(f.getValue()) == fontId)
      .findFirst();
  }
  
  default FontStyle setStandardFont(StandardFonts font) {
    return setFontFace(font == null ? StandardFonts.SWISS.getValue() : font.getValue());
  }

  default boolean isBold() {
    return this.getAttributes().contains(FontAttribute.BOLD);
  }

  default boolean isEmboss() {
    return this.getAttributes().contains(FontAttribute.EMBOSS);
  }

  default boolean isExtrude() {
    return this.getAttributes().contains(FontAttribute.EXTRUDE);
  }

  default boolean isItalic() {
    return this.getAttributes().contains(FontAttribute.ITALIC);
  }

  default boolean isShadow() {
    return this.getAttributes().contains(FontAttribute.SHADOW);
  }

  default boolean isStrikeout() {
    return this.getAttributes().contains(FontAttribute.STRIKEOUT);
  }

  default boolean isSub() {
    return this.getAttributes().contains(FontAttribute.SUB);
  }

  default boolean isSuper() {
    return this.getAttributes().contains(FontAttribute.SUPER);
  }

  default boolean isUnderline() {
    return this.getAttributes().contains(FontAttribute.UNDERLINE);
  }

  default FontStyle setBold(final boolean b) {
    final Set<FontAttribute> style = this.getAttributes();
    if (b) {
      style.add(FontAttribute.BOLD);
    } else {
      style.remove(FontAttribute.BOLD);
    }
    this.setAttributes(style);
    return this;
  }

  default FontStyle setEmboss(final boolean b) {
    byte style = getAttributesRaw();
    if(b) {
      style |= FontAttribute.EMBOSS.getValue();
    } else {
      style = (byte)(style & ~FontAttribute.EMBOSS.getValue());
    }
    setAttributesRaw(style);
    return this;
  }

  default FontStyle setExtrude(final boolean b) {
    byte style = getAttributesRaw();
    if(b) {
      style |= FontAttribute.EXTRUDE.getValue();
    } else {
      style = (byte)(style & ~FontAttribute.EXTRUDE.getValue());
    }
    setAttributesRaw(style);
    return this;
  }

  default FontStyle setItalic(final boolean b) {
    final Set<FontAttribute> style = this.getAttributes();
    if (b) {
      style.add(FontAttribute.ITALIC);
    } else {
      style.remove(FontAttribute.ITALIC);
    }
    this.setAttributes(style);
    return this;
  }

  default FontStyle setShadow(final boolean b) {
    byte style = getAttributesRaw();
    if(b) {
      style |= FontAttribute.SHADOW.getValue();
    } else {
      style = (byte)(style & ~FontAttribute.SHADOW.getValue());
    }
    setAttributesRaw(style);
    return this;
  }

  default FontStyle setStrikeout(final boolean b) {
    final Set<FontAttribute> style = this.getAttributes();
    if (b) {
      style.add(FontAttribute.STRIKEOUT);
    } else {
      style.remove(FontAttribute.STRIKEOUT);
    }
    this.setAttributes(style);
    return this;
  }

  default FontStyle setSub(final boolean b) {
    byte style = getAttributesRaw();
    if(b) {
      style |= FontAttribute.SUB.getValue();
    } else {
      style = (byte)(style & ~FontAttribute.SUB.getValue());
    }
    setAttributesRaw(style);
    return this;
  }

  default FontStyle setSuper(final boolean b) {
    byte style = getAttributesRaw();
    if(b) {
      style |= FontAttribute.SUPER.getValue();
    } else {
      style = (byte)(style & ~FontAttribute.SUPER.getValue());
    }
    setAttributesRaw(style);
    return this;
  }

  default FontStyle setUnderline(final boolean b) {
    final Set<FontAttribute> style = this.getAttributes();
    if (b) {
      style.add(FontAttribute.UNDERLINE);
    } else {
      style.remove(FontAttribute.UNDERLINE);
    }
    this.setAttributes(style);
    return this;
  }

  /**
   * Copy all font style attributes from another {@link FontStyle} object
   * 
   * @param otherStyle other style
   */
  default void copyFrom(FontStyle otherStyle) {
    setAttributes(otherStyle.getAttributes());
    setBold(otherStyle.isBold());
    setColorRaw(otherStyle.getColorRaw());
    setExtrude(otherStyle.isExtrude());
    setFontFace(otherStyle.getFontFace());
    setItalic(otherStyle.isItalic());
    setPointSize(otherStyle.getPointSize());
    setShadow(otherStyle.isShadow());
    setStrikeout(otherStyle.isStrikeout());
    setSub(otherStyle.isSub());
    setSuper(otherStyle.isSuper());
    setUnderline(otherStyle.isUnderline());
  }
}
