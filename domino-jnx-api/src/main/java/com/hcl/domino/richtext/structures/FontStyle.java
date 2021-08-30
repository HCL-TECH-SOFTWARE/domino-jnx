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
    @StructureMember(name = "Color", type = StandardColors.class),
    @StructureMember(name = "PointSize", type = byte.class, unsigned = true)
  }
)
public interface FontStyle extends MemoryStructure {
  @StructureGetter("Attrib")
  Set<FontAttribute> getAttributes();

  @StructureSetter("Attrib")
  FontStyle setAttributes(Collection<FontAttribute> attributes);

  @StructureGetter("Color")
  StandardColors getColor();

  @StructureSetter("Color")
  FontStyle setColor(StandardColors color);

  @StructureGetter("Face")
  byte getFontFace();

  @StructureSetter("Face")
  FontStyle setFontFace(byte font);

  @StructureGetter("PointSize")
  short getPointSize();

  @StructureSetter("PointSize")
  FontStyle setPointSize(int size);
  
  /**
   * Retrieves the {@link StandardFont} value corresponding to
   * {@link #getFontFace()}. When this value is empty, then the font is a custom
   * font that is defined in a mechanism specific to different areas of storage.
   * 
   * @return an {@link Optional} describing the {@link StandardFont} specified,
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
    style.add(FontAttribute.BOLD);
    this.setAttributes(style);
    return this;
  }

  default FontStyle setExtrude(final boolean b) {
    final Set<FontAttribute> style = this.getAttributes();
    style.add(FontAttribute.EXTRUDE);
    this.setAttributes(style);
    return this;
  }

  default FontStyle setItalic(final boolean b) {
    final Set<FontAttribute> style = this.getAttributes();
    style.add(FontAttribute.ITALIC);
    this.setAttributes(style);
    return this;
  }

  default FontStyle setShadow(final boolean b) {
    final Set<FontAttribute> style = this.getAttributes();
    style.add(FontAttribute.SHADOW);
    this.setAttributes(style);
    return this;
  }

  default FontStyle setStrikeout(final boolean b) {
    final Set<FontAttribute> style = this.getAttributes();
    style.add(FontAttribute.STRIKEOUT);
    this.setAttributes(style);
    return this;
  }

  default FontStyle setSub(final boolean b) {
    final Set<FontAttribute> style = this.getAttributes();
    style.add(FontAttribute.SUB);
    this.setAttributes(style);
    return this;
  }

  default FontStyle setSuper(final boolean b) {
    final Set<FontAttribute> style = this.getAttributes();
    style.add(FontAttribute.SUPER);
    this.setAttributes(style);
    return this;
  }

  default FontStyle setUnderline(final boolean b) {
    final Set<FontAttribute> style = this.getAttributes();
    style.add(FontAttribute.UNDERLINE);
    this.setAttributes(style);
    return this;
  }

}
