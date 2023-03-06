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
package com.hcl.domino.richtext.records;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.HotspotType;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDPLACEHOLDER",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Type", type = HotspotType.class),
    @StructureMember(name = "Flags", type = CDPlaceholder.Flag.class, bitfield = true),
    @StructureMember(name = "Width", type = short.class, unsigned = true),
    @StructureMember(name = "Height", type = short.class, unsigned = true),
    @StructureMember(name = "FontID", type = FontStyle.class),
    @StructureMember(name = "Characters", type = short.class, unsigned = true),
    @StructureMember(name = "SpaceBetween", type = short.class, unsigned = true),
    @StructureMember(name = "TextAlignment", type = CDPlaceholder.Alignment.class),
    @StructureMember(name = "SpaceWord", type = short.class, unsigned = true),
    // These are stored as an array of size [2], but this is a more-convenient representation for Java
    @StructureMember(name = "SubFontID1", type = FontStyle.class),
    @StructureMember(name = "SubFontID2", type = FontStyle.class),
    @StructureMember(name = "DataLength", type = short.class, unsigned = true),
    @StructureMember(name = "BackgroundColor", type = ColorValue.class),
    @StructureMember(name = "ColorRGB", type = ColorValue.class),
    @StructureMember(name = "SpareWord", type = short.class),
    @StructureMember(name = "Spare", type = int[].class, length = 3),
  }  
)
public interface CDPlaceholder extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    FITTOWINDOW(RichTextConstants.PLACEHOLDER_FLAG_FITTOWINDOW),
    DRAWBACKGROUND(RichTextConstants.PLACEHOLDER_FLAG_DRAWBACKGROUND),
    USEPERCENTAGE(RichTextConstants.PLACEHOLDER_FLAG_USEPERCENTAGE),
    SCROLLBARS(RichTextConstants.PLACEHOLDER_FLAG_SCROLLBARS),
    CONTENTSONLY(RichTextConstants.PLACEHOLDER_FLAG_CONTENTSONLY),
    ALIGNCENTER(RichTextConstants.PLACEHOLDER_FLAG_ALIGNCENTER),
    ALIGNRIGHT(RichTextConstants.PLACEHOLDER_FLAG_ALIGNRIGHT),
    FITTOWINDOWHEIGHT(RichTextConstants.PLACEHOLDER_FLAG_FITTOWINDOWHEIGHT),
    TILEIMAGE(RichTextConstants.PLACEHOLDER_FLAG_TILEIMAGE),
    DISPLAYHORZ(RichTextConstants.PLACEHOLDER_FLAG_DISPLAYHORZ),
    DONTEXPANDSELECTIONS(RichTextConstants.PLACEHOLDER_FLAG_DONTEXPANDSELECTIONS),
    EXPANDCURRENT(RichTextConstants.PLACEHOLDER_FLAG_EXPANDCURRENT),
    FITCONTENTSWIDTH(RichTextConstants.PLACEHOLDER_FLAG_FITCONTENTSWIDTH),
    FIXEDWIDTH(RichTextConstants.PLACEHOLDER_FLAG_FIXEDWIDTH),
    FIXEDHEIGHT(RichTextConstants.PLACEHOLDER_FLAG_FIXEDHEIGHT),
    FITCONTENTS(RichTextConstants.PLACEHOLDER_FLAG_FITCONTENTS),
    PROP_WIDTH(RichTextConstants.PLACEHOLDER_FLAG_PROP_WIDTH),
    PROP_BOTH(RichTextConstants.PLACEHOLDER_FLAG_PROP_BOTH),
    SCROLLERS(RichTextConstants.PLACEHOLDER_FLAG_SCROLLERS);
    private final int value;
    private Flag(int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Integer getValue() {
      return value;
    }
  }
  
  enum Alignment implements INumberEnum<Short> {
    LEFT(RichTextConstants.PLACEHOLDER_ALIGN_LEFT),
    CENTER(RichTextConstants.PLACEHOLDER_ALIGN_CENTER),
    RIGHT(RichTextConstants.PLACEHOLDER_ALIGN_RIGHT);
    private final short value;
    private Alignment(short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Short getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("Type")
  Optional<HotspotType> getPlaceholderType();
  
  /**
   * Retrieves the placeholder type as a raw {@code short}.
   * 
   * @return the placeholder type as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("Type")
  short getPlaceholderTypeRaw();
  
  @StructureSetter("Type")
  CDPlaceholder setPlaceholderType(HotspotType type);
  
  /**
   * Sets the placeholder type as a raw {@code short}.
   * 
   * @param type the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("Type")
  CDPlaceholder setPlaceholderTypeRaw(short type);
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDPlaceholder setFlags(Collection<Flag> flags);
  
  @StructureGetter("Width")
  int getWidth();
  
  @StructureSetter("Width")
  CDPlaceholder setWidth(int width);
  
  @StructureGetter("Height")
  int getHeight();
  
  @StructureSetter("Height")
  CDPlaceholder setHeight(int height);
  
  @StructureGetter("FontID")
  FontStyle getFontID();
  
  @StructureGetter("Characters")
  int getCharacters();
  
  @StructureSetter("Characters")
  CDPlaceholder setCharacters(int characters);
  
  @StructureGetter("SpaceBetween")
  int getSpaceBetween();
  
  @StructureSetter("SpaceBetween")
  CDPlaceholder setSpaceBetween(int space);
  
  @StructureGetter("TextAlignment")
  Optional<Alignment> getAlignment();
  
  /**
   * Retrieves the alignment as a raw {@code short}.
   * 
   * @return the alignment as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("TextAlignment")
  short getAlignmentRaw();
  
  @StructureSetter("TextAlignment")
  CDPlaceholder setAlignment(Alignment alignment);
  
  /**
   * Sets the alignment as a raw {@code short}.
   * 
   * @param alignment the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("TextAlignment")
  CDPlaceholder setAlignmentRaw(short alignment);
  
  @StructureGetter("SubFontID1")
  FontStyle getSubFontID1();
  
  @StructureGetter("SubFontID2")
  FontStyle getSubFontID2();
  
  @StructureGetter("DataLength")
  int getDataLength();
  
  @StructureSetter("DataLength")
  CDPlaceholder setDataLength(int len);
  
  @StructureGetter("BackgroundColor")
  ColorValue getBackgroundColor();
  
  @StructureGetter("ColorRGB")
  ColorValue getColorRGB();
}
