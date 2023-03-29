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

import com.hcl.domino.design.ClassicThemeBehavior;
import com.hcl.domino.design.format.ActionBarBackgroundRepeat;
import com.hcl.domino.design.format.ActionBarTextAlignment;
import com.hcl.domino.design.format.ActionWidthMode;
import com.hcl.domino.design.format.ButtonBorderDisplay;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.LengthValue;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(
  name = "CDACTIONBAREXT",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "BackColor", type = ColorValue.class),
    @StructureMember(name = "LineColor", type = ColorValue.class),
    @StructureMember(name = "FontColor", type = ColorValue.class),
    @StructureMember(name = "ButtonColor", type = ColorValue.class),
    @StructureMember(name = "BtnBorderDisplay", type = ButtonBorderDisplay.class),
    @StructureMember(name = "wAppletHeight", type = short.class, unsigned = true),
    @StructureMember(name = "wBarBackgroundRepeat", type = ActionBarBackgroundRepeat.class),
    @StructureMember(name = "BtnWidthStyle", type = ActionWidthMode.class),
    @StructureMember(name = "BtnTextJustify", type = ActionBarTextAlignment.class),
    @StructureMember(name = "wBtnWidthAbsolute", type = short.class, unsigned = true),
    @StructureMember(name = "wBtnInternalMargin", type = short.class, unsigned = true),
    @StructureMember(name = "dwFlags", type = CDActionBarExt.Flag.class, bitfield = true),
    @StructureMember(name = "barFontID", type = FontStyle.class),
    @StructureMember(name = "barHeight", type = LengthValue.class),
    @StructureMember(name = "wThemeSetting", type = ClassicThemeBehavior.class),
    @StructureMember(name = "wSpare", type = short.class),
    @StructureMember(name = "Spare", type = int[].class, length = 11)
  }
)
public interface CDActionBarExt extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    WIDTH_STYLE_VALID(RichTextConstants.ACTIONBAREXT_WIDTH_STYLE_VALID_FLAG);

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
  Optional<ButtonBorderDisplay> getBorderDisplay();

  /**
   * Retrieves the button border display as a {@code short}. 
   * 
   * @return the button border display as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("BtnBorderDisplay")
  short getBorderDisplayRaw();

  @StructureSetter("BtnBorderDisplay")
  CDActionBarExt setBorderDisplay(ButtonBorderDisplay borderDisplay);

  /**
   * Sets the button border display as a raw {@code short}.
   * 
   * @param borderDisplay the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("BtnBorderDisplay")
  CDActionBarExt setBorderDisplayRaw(short borderDisplay);

  @StructureGetter("wAppletHeight")
  int getAppletHeight();

  @StructureSetter("wAppletHeight")
  CDActionBarExt setAppletHeight(int appletHeight);

  @StructureGetter("wBarBackgroundRepeat")
  short getBackgroundRepeatRaw();

  @StructureSetter("wBarBackgroundRepeat")
  CDActionBarExt setBackgroundRepeatRaw(short backgroundRepeat);
  
  @StructureGetter("wBarBackgroundRepeat")
  Optional<ActionBarBackgroundRepeat> getBackgroundRepeat();

  @StructureSetter("wBarBackgroundRepeat")
  CDActionBarExt setBackgroundRepeat(ActionBarBackgroundRepeat repeat);

  @StructureGetter("BtnWidthStyle")
  Optional<ActionWidthMode> getWidthStyle();

  /**
   * Retrieves the button width style as a raw {@code byte}.
   * 
   * @return the button width style as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("BtnWidthStyle")
  byte getWidthStyleRaw();

  @StructureSetter("BtnWidthStyle")
  CDActionBarExt setWidthStyle(ActionWidthMode widthStyle);

  /**
   * Sets the button width style as a raw {@code byte}.
   * 
   * @param widthStyle the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("BtnWidthStyle")
  CDActionBarExt setWidthStyleRaw(byte widthStyle);

  @StructureGetter("BtnTextJustify")
  Optional<ActionBarTextAlignment> getTextJustify();

  /**
   * Retrieves the button text justification as a raw {@code byte}.
   * 
   * @return the button text justification as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("BtnTextJustify")
  byte getTextJustifyRaw();

  @StructureSetter("BtnTextJustify")
  CDActionBarExt setTextJustify(ActionBarTextAlignment textJustify);

  /**
   * Sets the button text justification as a raw {@code byte}.
   * 
   * @param textJustify the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("BtnTextJustify")
  CDActionBarExt setTextJustifyRaw(byte textJustify);

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
  
  @StructureGetter("wThemeSetting")
  Optional<ClassicThemeBehavior> getThemeSetting();
  
  /**
   * Retrieves the theme setting as a raw {@code byte}.
   * 
   * @return the theme setting as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("wThemeSetting")
  byte getThemeSettingRaw();
  
  @StructureSetter("wThemeSetting")
  CDActionBarExt setThemeSetting(ClassicThemeBehavior setting);

  /**
   * Sets the theme setting as a raw {@code byte}.
   * 
   * @param setting the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("wThemeSetting")
  CDActionBarExt setThemeSettingRaw(byte setting);
}
