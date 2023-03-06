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
import com.hcl.domino.misc.StructureSupport;
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
 * @since 1.0.35
 */
@StructureDefinition(
  name = "CDEMBEDDEDOUTLINE",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDEmbeddedOutline.Flag.class, bitfield = true),
    @StructureMember(name = "Unised", type = int[].class, length = 3),
    @StructureMember(name = "Alignment", type = short.class),
    @StructureMember(name = "SpaceBetweenEntries", type = short.class, unsigned = true),
    @StructureMember(name = "LabelLength", type = short.class, unsigned = true), // variable
    @StructureMember(name = "Style", type = CDEmbeddedOutline.Style.class),
    @StructureMember(name = "Title_VOffset", type = short.class, unsigned = true),
    @StructureMember(name = "Title_HOffset", type = short.class, unsigned = true),
    @StructureMember(name = "Title_Height", type = short.class, unsigned = true),
    @StructureMember(name = "TopLevel_VOffset", type = short.class, unsigned = true),
    @StructureMember(name = "TopLevel_HOffset", type = short.class, unsigned = true),
    @StructureMember(name = "TopLevel_Height", type = short.class, unsigned = true),
    @StructureMember(name = "SubLevel_VOffset", type = short.class, unsigned = true),
    @StructureMember(name = "SubLevel_HOffset", type = short.class, unsigned = true),
    @StructureMember(name = "SubLevel_Height", type = short.class, unsigned = true),
    @StructureMember(name = "NameLength", type = short.class, unsigned = true), // variable
    @StructureMember(name = "TargetFrameLength", type = short.class, unsigned = true), // variable
    @StructureMember(name = "SelectFontID", type = FontStyle[].class, length = 3),
    @StructureMember(name = "MouseFontID", type = FontStyle[].class, length = 3),
    @StructureMember(name = "Font_VOffset", type = short[].class, length = 3, unsigned = true),
    @StructureMember(name = "Font_HOffset", type = short[].class, length = 3, unsigned = true),
    @StructureMember(name = "Align", type = CDEmbeddedOutline.Alignment[].class, length = 3),
    @StructureMember(name = "Control_BackColor", type = ColorValue.class),
    @StructureMember(name = "BackColor", type = ColorValue[].class, length = 9),
    @StructureMember(name = "SelectFontColor", type = ColorValue[].class, length = 3),
    @StructureMember(name = "Repeat", type = CDEmbeddedOutline.Repeat[].class, length = 4),
    @StructureMember(name = "Background_Align", type = CDEmbeddedOutline.Alignment[].class, length = 4),
    @StructureMember(name = "Background_VOffset", type = short[].class, length = 4, unsigned = true),
    @StructureMember(name = "Background_HOffset", type = short[].class, length = 4, unsigned = true),
    @StructureMember(name = "wBackground_Image", type = short[].class, length = 4),
    @StructureMember(name = "NormalFontColor", type = ColorValue[].class, length = 3),
    @StructureMember(name = "MouseFontColor", type = ColorValue[].class, length = 3),
    @StructureMember(name = "RootLength", type = short.class, unsigned = true), // variable
    @StructureMember(name = "TopLevel_PixelHeight", type = short.class, unsigned = true),
    @StructureMember(name = "wColWidth", type = short.class, unsigned = true),
    @StructureMember(name = "SpareWord", type = short.class),
    @StructureMember(name = "Spare", type = int[].class, length = 4),
  }
)
public interface CDEmbeddedOutline extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    DISPLAYHORZ(RichTextConstants.EMBEDDEDOUTLINE_FLAG_DISPLAYHORZ),
    HASIMAGELABEL(RichTextConstants.EMBEDDEDOUTLINE_FLAG_HASIMAGELABEL),
    TILEIMAGE(RichTextConstants.EMBEDDEDOUTLINE_FLAG_TILEIMAGE),
    USEAPPLET_INBROWSER(RichTextConstants.EMBEDDEDOUTLINE_FLAG_USEAPPLET_INBROWSER),
    TYPE_TITLE(RichTextConstants.EMBEDDEDOUTLINE_FLAG_TYPE_TITLE),
    SHOWTWISTIE(RichTextConstants.EMBEDDEDOUTLINE_FLAG_SHOWTWISTIE),
    TITLEFIXED(RichTextConstants.EMBEDDEDOUTLINE_FLAG_TITLEFIXED),
    TOPLEVELFIXED(RichTextConstants.EMBEDDEDOUTLINE_FLAG_TOPLEVELFIXED),
    SUBLEVELFIXED(RichTextConstants.EMBEDDEDOUTLINE_FLAG_SUBLEVELFIXED),
    TREE_STYLE(RichTextConstants.EMBEDDEDOUTLINE_FLAG_TREE_STYLE),
    HASNAME(RichTextConstants.EMBEDDEDOUTLINE_FLAG_HASNAME),
    HASTARGETFRAME(RichTextConstants.EMBEDDEDOUTLINE_FLAG_HASTARGETFRAME),
    ALLTHESAME(RichTextConstants.EMBEDDEDOUTLINE_FLAG_ALLTHESAME),
    BACK_ALLTHESAME(RichTextConstants.EMBEDDEDOUTLINE_FLAG_BACK_ALLTHESAME),
    EXPAND_DATA(RichTextConstants.EMBEDDEDOUTLINE_FLAG_EXPAND_DATA),
    EXPAND_ALL(RichTextConstants.EMBEDDEDOUTLINE_FLAG_EXPAND_ALL),
    EXPAND_FIRST(RichTextConstants.EMBEDDEDOUTLINE_FLAG_EXPAND_FIRST),
    EXPAND_SAVED(RichTextConstants.EMBEDDEDOUTLINE_FLAG_EXPAND_SAVED),
    EXPAND_NONE(RichTextConstants.EMBEDDEDOUTLINE_FLAG_EXPAND_NONE),
    HASROOTNAME(RichTextConstants.EMBEDDEDOUTLINE_FLAG_HASROOTNAME),
    RTLREADING(RichTextConstants.EMBEDDEDOUTLINE_FLAG_RTLREADING),
    TWISTIEIMAGE(RichTextConstants.EMBEDDEDOUTLINE_FLAG_TWISTIEIMAGE),
    HANDLEFOLDERUNREAD(RichTextConstants.EMBEDDEDOUTLINE_FLAG_HANDLEFOLDERUNREAD),
    NEWSTYLE_TWISTIE(RichTextConstants.EMBEDDEDOUTLINE_FLAG_NEWSTYLE_TWISTIE),
    MAINTAINFOLDERUNREAD(RichTextConstants.EMBEDDEDOUTLINE_FLAG_MAINTAINFOLDERUNREAD),
    USEJSCTLINBROWSER(RichTextConstants.EMBEDDEDOUTLINE_FLAG_USEJSCTLINBROWSER),
    USECUSTOMJSINBROWSER(RichTextConstants.EMBEDDEDOUTLINE_FLAG_USECUSTOMJSINBROWSER);
    
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
  enum Style implements INumberEnum<Short> {
    HIDE((short)0),
    SIMPLE((short)1),
    HIERARCHICAL((short)2);
    
    private final short value;
    private Style(short value) {
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
  enum Alignment implements INumberEnum<Short> {
    TOPLEFT(RichTextConstants.ALIGNMENT_TOPLEFT),
    TOPCENTER(RichTextConstants.ALIGNMENT_TOPCENTER),
    TOPRIGHT(RichTextConstants.ALIGNMENT_TOPRIGHT),
    MIDDLELEFT(RichTextConstants.ALIGNMENT_MIDDLELEFT),
    MIDDLECENTER(RichTextConstants.ALIGNMENT_MIDDLECENTER),
    MIDDLERIGHT(RichTextConstants.ALIGNMENT_MIDDLERIGHT),
    BOTTOMLEFT(RichTextConstants.ALIGNMENT_BOTTOMLEFT),
    BOTTOMCENTER(RichTextConstants.ALIGNMENT_BOTTOMCENTER),
    BOTTOMRIGHT(RichTextConstants.ALIGNMENT_BOTTOMRIGHT);
    
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
  enum Repeat implements INumberEnum<Short> {
    ONCE((short)0),
    VERTICAL((short)1),
    HORIZONTAL((short)2),
    TILE((short)3),
    SIZE_TO_FIT((short)4);
    
    private final short value;
    private Repeat(short value) {
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

  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDEmbeddedOutline setFlags(Collection<Flag> flags);
  
  @StructureGetter("SpaceBetweenEntries")
  int getSpaceBetweenEntries();
  
  @StructureSetter("SpaceBetweenEntries")
  CDEmbeddedOutline setSpaceBetweenEntries(int space);
  
  @StructureGetter("LabelLength")
  int getLabelLength();
  
  @StructureSetter("LabelLength")
  CDEmbeddedOutline setLabelLength(int len);
  
  @StructureGetter("Style")
  Optional<Style> getStyle();
  
  /**
   * Retrieves the style as a raw {@code short}.
   * 
   * @return the style as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("Style")
  short getStyleRaw();
  
  @StructureSetter("Style")
  CDEmbeddedOutline setStyle(Style style);
  
  /**
   * Sets the style as a raw {@code short}.
   * 
   * @param style the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("Style")
  CDEmbeddedOutline setStyleRaw(short style);
  
  @StructureGetter("Title_VOffset")
  int getTitleVerticalOffset();
  
  @StructureSetter("Title_VOffset")
  CDEmbeddedOutline setTitleVerticalOffset(int offset);
  
  @StructureGetter("Title_HOffset")
  int getTitleHorizontalOffset();
  
  @StructureSetter("Title_HOffset")
  CDEmbeddedOutline setTitleHorizontalOffset(int offset);
  
  @StructureGetter("Title_Height")
  int getTitleHeight();
  
  @StructureSetter("Title_Height")
  CDEmbeddedOutline setTitleHeight(int offset);
  
  @StructureGetter("TopLevel_VOffset")
  int getTopLevelVerticalOffset();
  
  @StructureSetter("TopLevel_VOffset")
  CDEmbeddedOutline setTopLevelVerticalOffset(int offset);
  
  @StructureGetter("TopLevel_HOffset")
  int getTopLevelHorizontalOffset();
  
  @StructureSetter("TopLevel_HOffset")
  CDEmbeddedOutline setTopLevelHorizontalOffset(int offset);
  
  @StructureGetter("TopLevel_Height")
  int getTopLevelHeight();
  
  @StructureSetter("TopLevel_Height")
  CDEmbeddedOutline setTopLevelHeight(int offset);
  
  @StructureGetter("SubLevel_VOffset")
  int getSubLevelVerticalOffset();
  
  @StructureSetter("SubLevel_VOffset")
  CDEmbeddedOutline setSubLevelVerticalOffset(int offset);
  
  @StructureGetter("SubLevel_HOffset")
  int getSubLevelHorizontalOffset();
  
  @StructureSetter("SubLevel_HOffset")
  CDEmbeddedOutline setSubLevelHorizontalOffset(int offset);
  
  @StructureGetter("SubLevel_Height")
  int getSubLevelHeight();
  
  @StructureSetter("SubLevel_Height")
  CDEmbeddedOutline setSubLevelHeight(int offset);
  
  @StructureGetter("NameLength")
  int getNameLength();
  
  @StructureSetter("NameLength")
  CDEmbeddedOutline setNameLength(int offset);
  
  @StructureGetter("TargetFrameLength")
  int getTargetFrameLength();
  
  @StructureSetter("TargetFrameLength")
  CDEmbeddedOutline setTargetFrameLength(int offset);
  
  @StructureGetter("SelectFontID")
  FontStyle[] getSelectFontIDs();
  
  @StructureGetter("MouseFontID")
  FontStyle[] getMouseFontIDs();
  
  @StructureGetter("Font_VOffset")
  int[] getFontVerticalOffsets();
  
  @StructureSetter("Font_VOffset")
  CDEmbeddedOutline setFontVerticalOffsets(int[] offsets);
  
  @StructureGetter("Font_HOffset")
  int[] getFontHorizontalOffsets();
  
  @StructureSetter("Font_HOffset")
  CDEmbeddedOutline setFontHorizontalOffsets(int[] offsets);
  
  @StructureGetter("Align")
  Alignment[] getAlignments();
  
  @StructureSetter("Align")
  CDEmbeddedOutline setAlignments(Alignment[] alignments);
  
  @StructureGetter("Control_BackColor")
  ColorValue getControlBackgroundColor();
  
  @StructureGetter("BackColor")
  ColorValue[] getBackgroundColors();
  
  @StructureGetter("SelectFontColor")
  ColorValue[] getSelectionFontColors();

  @StructureGetter("Repeat")
  Repeat[] getBackgroundRepeatModes();

  /**
   * Retrieves the background repeat modes an array of {@code short}.
   * 
   * @return the background repeat modes as an array of {@code short}
   * @since 1.24.0
   */
  @StructureGetter("Repeat")
  short[] getBackgroundRepeatModesRaw();
  
  @StructureSetter("Repeat")
  CDEmbeddedOutline setBackgroundRepeatModes(Repeat[] repeats);
  
  /**
   * Sets the background repeat modes as an array of {@code short}.
   * 
   * @param repeats the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("Repeat")
  CDEmbeddedOutline setBackgroundRepeatModesRaw(short[] repeats);

  @StructureGetter("Background_Align")
  Alignment[] getBackgroundAlignments();

  /**
   * Retrieves the background alignments an array of {@code short}.
   * 
   * @return the background alignments as an array of {@code short}
   * @since 1.24.0
   */
  @StructureGetter("Background_Align")
  short[] getBackgroundAlignmentsRaw();
  
  @StructureSetter("Background_Align")
  CDEmbeddedOutline setBackgroundAlignments(Alignment[] alignments);
  
  /**
   * Sets the background alignments an array of {@code short}.
   * 
   * @param alignments the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("Background_Align")
  CDEmbeddedOutline setBackgroundAlignmentsRaw(short[] alignments);
  
  @StructureGetter("Background_VOffset")
  int[] getBackgroundVerticalOffsets();
  
  @StructureSetter("Background_VOffset")
  CDEmbeddedOutline setBackgroundVerticalOffsets(int[] offsets);
  
  @StructureGetter("Background_HOffset")
  int[] getBackgroundHorizontalOffsets();
  
  @StructureSetter("Background_HOffset")
  CDEmbeddedOutline setBackgroundHorizontalOffsets(int[] offsets);
  
  @StructureGetter("NormalFontColor")
  ColorValue[] getNormalFontColors();
  
  @StructureGetter("MouseFontColor")
  ColorValue[] getMouseFontColors();
  
  @StructureGetter("RootLength")
  int getRootLength();
  
  @StructureSetter("RootLength")
  CDEmbeddedOutline setRootLength(int len);
  
  @StructureGetter("TopLevel_PixelHeight")
  int getTopLevelPixelHeight();
  
  @StructureSetter("TopLevel_PixelHeight")
  CDEmbeddedOutline setTopLevelPixelHeight(int height);
  
  @StructureGetter("wColWidth")
  int getColumnWidth();
  
  @StructureSetter("wColWidth")
  CDEmbeddedOutline setColumnWidth(int width);
  
  default String getName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getNameLength()
    );
  }
  
  default CDEmbeddedOutline setName(String name) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getNameLength(),
      name,
      this::setNameLength
    );
  }
  
  default String getTargetFrame() {
    return StructureSupport.extractStringValue(
      this,
      getNameLength(),
      getTargetFrameLength()
    );
  }
  
  default CDEmbeddedOutline setTargetFrame(String frame) {
    return StructureSupport.writeStringValue(
      this,
      getNameLength(),
      getTargetFrameLength(),
      frame,
      this::setTargetFrameLength
    );
  }
  
  default String getRootEntry() {
    return StructureSupport.extractStringValue(
      this,
      getNameLength() + getTargetFrameLength(),
      getRootLength()
    );
  }
  
  default CDEmbeddedOutline setRootEntry(String name) {
    return StructureSupport.writeStringValue(
      this,
      getNameLength() + getTargetFrameLength(),
      getRootLength(),
      name,
      this::setRootLength
    );
  }
}
