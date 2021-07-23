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

@StructureDefinition(name = "FONTID", endianSensitive = true, members = {
    @StructureMember(name = "Face", type = FontStyle.StandardFonts.class),
    @StructureMember(name = "Attrib", type = FontStyle.Attribute.class, bitfield = true),
    @StructureMember(name = "Color", type = FontStyle.StandardColors.class),
    @StructureMember(name = "PointSize", type = byte.class, unsigned = true)
})
public interface FontStyle extends MemoryStructure {
  public enum Attribute implements INumberEnum<Byte> {
    BOLD((byte) 0x01),
    ITALIC((byte) 0x02),
    UNDERLINE((byte) 0x04),
    STRIKEOUT((byte) 0x08),
    SUPER((byte) 0x10),
    SUB((byte) 0x20),
    EFFECT((byte) 0x80),
    SHADOW((byte) 0x80),
    EMBOSS((byte) 0x90),
    EXTRUDE((byte) 0xa0);

    private final byte value;

    Attribute(final byte value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Byte getValue() {
      return this.value;
    }
  }

  /**
   * These symbols are used to specify text color, graphic color and background
   * color in a variety of C API structures.
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

    private final byte m_color;

    StandardColors(final int colorIdx) {
      this.m_color = (byte) (colorIdx & 0xff);
    }

    @Override
    public long getLongValue() {
      return this.m_color;
    }

    @Override
    public Byte getValue() {
      return this.m_color;
    }
  }

  /**
   * These symbols define the standard type faces.
   * The Face member of the {@link FontStyle} may be either one of these standard
   * font faces,
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
    /**
     * returned if font is not in the standard table; cannot be set via
     * {@link FontStyle#setFontFace(StandardFonts)}
     */
    CUSTOMFONT((byte) 255);

    private final byte m_face;

    StandardFonts(final int face) {
      this.m_face = (byte) (face & 0xff);
    }

    @Override
    public long getLongValue() {
      return this.m_face;
    }

    @Override
    public Byte getValue() {
      return this.m_face;
    }
  }

  @StructureGetter("Attrib")
  Set<Attribute> getAttributes();

  @StructureGetter("Color")
  StandardColors getColor();

  @StructureGetter("Face")
  StandardFonts getFontFace();

  @StructureGetter("PointSize")
  short getPointSize();

  default boolean isBold() {
    return this.getAttributes().contains(Attribute.BOLD);
  }

  default boolean isExtrude() {
    return this.getAttributes().contains(Attribute.EXTRUDE);
  }

  default boolean isItalic() {
    return this.getAttributes().contains(Attribute.ITALIC);
  }

  default boolean isShadow() {
    return this.getAttributes().contains(Attribute.SHADOW);
  }

  default boolean isStrikeout() {
    return this.getAttributes().contains(Attribute.STRIKEOUT);
  }

  default boolean isSub() {
    return this.getAttributes().contains(Attribute.SUB);
  }

  default boolean isSuper() {
    return this.getAttributes().contains(Attribute.SUPER);
  }

  default boolean isUnderline() {
    return this.getAttributes().contains(Attribute.UNDERLINE);
  }

  @StructureSetter("Attrib")
  FontStyle setAttributes(Collection<Attribute> attributes);

  default FontStyle setBold(final boolean b) {
    final Set<Attribute> style = this.getAttributes();
    style.add(Attribute.BOLD);
    this.setAttributes(style);
    return this;
  }

  @StructureSetter("Color")
  FontStyle setColor(StandardColors color);

  default FontStyle setExtrude(final boolean b) {
    final Set<Attribute> style = this.getAttributes();
    style.add(Attribute.EXTRUDE);
    this.setAttributes(style);
    return this;
  }

  @StructureSetter("Face")
  FontStyle setFontFace(StandardFonts font);

  default FontStyle setItalic(final boolean b) {
    final Set<Attribute> style = this.getAttributes();
    style.add(Attribute.ITALIC);
    this.setAttributes(style);
    return this;
  }

  @StructureSetter("PointSize")
  FontStyle setPointSize(int size);

  default FontStyle setShadow(final boolean b) {
    final Set<Attribute> style = this.getAttributes();
    style.add(Attribute.SHADOW);
    this.setAttributes(style);
    return this;
  }

  default FontStyle setStrikeout(final boolean b) {
    final Set<Attribute> style = this.getAttributes();
    style.add(Attribute.STRIKEOUT);
    this.setAttributes(style);
    return this;
  }

  default FontStyle setSub(final boolean b) {
    final Set<Attribute> style = this.getAttributes();
    style.add(Attribute.SUB);
    this.setAttributes(style);
    return this;
  }

  default FontStyle setSuper(final boolean b) {
    final Set<Attribute> style = this.getAttributes();
    style.add(Attribute.SUPER);
    this.setAttributes(style);
    return this;
  }

  default FontStyle setUnderline(final boolean b) {
    final Set<Attribute> style = this.getAttributes();
    style.add(Attribute.UNDERLINE);
    this.setAttributes(style);
    return this;
  }

}
