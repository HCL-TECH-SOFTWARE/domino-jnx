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
import com.hcl.domino.richtext.structures.BSIG;
import com.hcl.domino.richtext.structures.FontStyle;

/**
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(
  name = "CDACTIONBAR",
  members = {
    @StructureMember(name = "Header", type = BSIG.class),
    @StructureMember(name = "BackColor", type = short.class, unsigned = true),
    @StructureMember(name = "LineColor", type = short.class, unsigned = true),
    @StructureMember(name = "LineStyle", type = CDActionBar.LineStyle.class),
    @StructureMember(name = "BorderStyle", type = CDActionBar.BorderStyle.class),
    @StructureMember(name = "BorderWidth", type = short.class, unsigned = true),
    @StructureMember(name = "dwFlags", type = CDActionBar.Flag.class, bitfield = true),
    @StructureMember(name = "ShareID", type = int.class),
    @StructureMember(name = "FontID", type = FontStyle.class),
    @StructureMember(name = "BtnHeight", type = short.class, unsigned = true),
    @StructureMember(name = "HeightSpc", type = short.class, unsigned = true)
  }
)
public interface CDActionBar extends RichTextRecord<BSIG> {
  /**
   * Represents options in the deprecated action-bar border styles.
   * 
   * @author Jesse Gallagher
   */
  enum BorderStyle implements INumberEnum<Short> {
    NONE(0),
    MAX(1),
    VAR(2),
    ABS(3);

    private final short value;

    BorderStyle(final int value) {
      this.value = (short) value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  enum Flag implements INumberEnum<Integer> {
    NO_SYS_COLOR(0x00000001),
    ALIGN_RIGHT(0x00000002), /* Right justify buttons */
    TRANS_BUTTONS(0x00000004), /* Buttons are transparent */
    SYS_BUTTONS(0x00000008), /* Buttons use system color */
    BTNBCK_IMGRSRC(0x00000010), /* Image resource used for button background */
    BARBCK_IMGRSRC(0x00000020), /* Image resource used for bar background */
    SET_PADDING(0x00000040), /* Use the Padding setting instead of default 2 pixels */
    USE_APPLET(0x00000080), /* Use applet in browser */
    SET_HEIGHT(0x00000100), /* Use Height setting instead of default ICON_DEFAULT_HEIGHT */
    ABSOLUTE_HEIGHT(0x00000200), /* if ACTION_BAR_FLAG_SET_HEIGHT, use absolute height spec'd by user */
    BACKGROUND_HEIGHT(0x00000400), /* if ACTION_BAR_FLAG_SET_HEIGHT, use background image's height */
    SET_WIDTH(0x00000800), /* Use Width setting instead of default width */
    BACKGROUND_WIDTH(0x00001000), /* if ACTION_BAR_FLAG_SET_WIDTH, use background image's width */
    SHOW_HINKY_ALWAYS(0x00002000), /* Always show the drop down hinky if a button has a menu no matter what the border style is. */
    SUPPRESS_SYS_POPUPS(0x00004000), /* suppress the system actions in the right click pop-up (views only). */
    USE_JSCONTROL(0x00008000), /* use a JS (dojo) control to render the action bar on the web */
    USE_CUSTOMJS(0x00010000); /* use a custom js control to render the action bar on the web */

    private final int value;

    Flag(final int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Integer getValue() {
      return this.value;
    }
  }

  enum LineStyle implements INumberEnum<Short> {
    SINGLE(1),
    DOUBLE(2),
    TRIPLE(3),
    TWO(4);

    private final short value;

    LineStyle(final int value) {
      this.value = (short) value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  @StructureGetter("BackColor")
  int getBackgroundColor();

  @StructureGetter("BorderStyle")
  BorderStyle getBorderStyle();

  @StructureGetter("BtnHeight")
  int getButtonHeight();

  @StructureGetter("dwFlags")
  Set<Flag> getFlags();

  @StructureGetter("FontID")
  FontStyle getFontStyle();

  @StructureGetter("Header")
  @Override
  BSIG getHeader();

  @StructureGetter("HeightSpc")
  int getHeightSpacing();

  @StructureGetter("LineColor")
  int getLineColor();

  @StructureGetter("LineStyle")
  LineStyle getLineStyle();

  @StructureGetter("ShareID")
  int getShareId();

  @StructureSetter("BackColor")
  CDActionBar setBackgroundColor(int colorIndex);

  @StructureSetter("BorderStyle")
  CDActionBar setBorderStyle(BorderStyle borderStyle);

  @StructureSetter("BtnHeight")
  CDActionBar setButtonHeight(int btnHeight);

  @StructureSetter("dwFlags")
  CDActionBar setFlags(Collection<Flag> flags);

  @StructureSetter("HeightSpc")
  CDActionBar setHeightSpacing(int heightSpacing);

  @StructureSetter("LineColor")
  CDActionBar setLineColor(int colorIndex);

  @StructureSetter("LineStyle")
  CDActionBar setLineStyle(LineStyle lineStyle);

  @StructureSetter("ShareID")
  CDActionBar setShareId(int shareId);
}
