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
package com.hcl.domino.design;

import java.util.List;
import java.util.Optional;

import com.hcl.domino.data.NotesFont;
import com.hcl.domino.design.action.ActionBarAction;
import com.hcl.domino.design.format.ActionBarBackgroundRepeat;
import com.hcl.domino.design.format.ActionBarTextAlignment;
import com.hcl.domino.design.format.ActionButtonHeightMode;
import com.hcl.domino.design.format.ActionWidthMode;
import com.hcl.domino.design.format.BorderStyle;
import com.hcl.domino.design.format.ButtonBorderDisplay;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.structures.ColorValue;

/**
 * Describes the action bar that appears at the top of collection and form
 * design elements.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public interface ActionBar extends DesignElement.ThemeableClassicElement {
  /**
   * Describes the starting alignment of actions in the action bar.
   * 
   * @author Jesse Gallagher
   * @since 1.0.32
   */
  enum Alignment {
    LEFT, RIGHT
  }
  
  /**
   * Describes the options for sizing the height of individual actions for display.
   * 
   * @author Jesse Gallagher
   * @since 1.0.32
   */
  enum ButtonHeightMode {
    DEFAULT, MINIMUM_SIZE, FIXED_SIZE, BACKGROUND_SIZE
  }
  
  /**
   * Retrieves the alignment for actions in the bar.
   * 
   * @return an {@link Alignment} instance
   */
  Alignment getAlignment();
  
  /**
   * Determines whether the action bar should be rendered using a Java applet instead of
   * HTML-based controls.
   * 
   * @return {@code true} if the action bar should be rendered using a Java applet;
   *         {@code false} otherwise
   */
  boolean isUseJavaApplet();
  
  /**
   * Determines whether the default items should be shown in the context menu.
   * 
   * @return {@code true} to show default items in the context menu;
   *         {@code false} otherwise
   */
  boolean isShowDefaultItemsInContextMenu();
  
  /**
   * Determines the height sizing mode for displaying the action bar.
   * 
   * @return a {@link ActionButtonHeightMode} instance
   */
  ActionButtonHeightMode getHeightMode();
  
  /**
   * Determines the height sizing value for the action bar. The meaning of this value depends on
   * {@link #getHeightMode()}.
   * 
   * @return the height sizing specification
   */
  double getHeightSpec();
  
  /**
   * Retrieves the font specification used when {@link #getHeightMode()} is
   * {@link ActionButtonHeightMode#EXS}.
   * 
   * @return a {@link NotesFont} instance
   */
  NotesFont getHeightSizingFont();
  
  /**
   * Retrieves the background color used to display the action bar.
   * 
   * @return a {@link ColorValue} instance
   */
  ColorValue getBackgroundColor();
  
  /**
   * Retrieves the image resource used for the action bar background, if set.
   * 
   * @return an {@link Optional} describing the bar background image, or an empty
   *         one of this is not set
   */
  Optional<CDResource> getBackgroundImage();
  
  /**
  * Retrieves the repeat mode for the background image.
  * 
  * @return an {@link BackgroundRepeatMode} for the background image
  */
  ActionBarBackgroundRepeat getBackgroundImageRepeatMode();
 
  /**
   * Retrieves the style of border to use around the action bar.
   * 
   * @return a {@link BorderStyle} instance
   */
  BorderStyle getBorderStyle();
  
  /**
   * Retrieves the color to use when displaying the action bar border.
   * 
   * @return a {@link ColorValue} instance
   */
  ColorValue getBorderColor();
  
  /**
   * Determines whether the action bar should be displayed with a drop shadow.
   * 
   * @return {@code true} if the action bar should have a drop shadow;
   *         {@code false} otherwise
   */
  boolean isUseDropShadow();
  
  /**
   * Retrieves the width (in pixels) of the drop shadow to display when
   * {@link #isUseDropShadow()} is {@code true}.
   * 
   * @return the drop shadow width in pixels.
   */
  int getDropShadowWidth();
  
  /**
   * Retrieves the inner margins of the action bar.
   * 
   * @return a {@link EdgeWidths} instance
   */
  EdgeWidths getInsideMargins();
  
  /**
   * Retrieves the widths of the borders to display around the action bar.
   * 
   * @return a {@link EdgeWidths} instance
   */
  EdgeWidths getBorderWidths();
  
  /**
   * Retrieves the outer margins of the action bar.
   * 
   * @return a {@link EdgeWidths} instance
   */
  EdgeWidths getOutsideMargins();
  
  /**
   * Determines the height sizing mode for displaying individual actions in the bar.
   * 
   * @return a {@link ButtonHeightMode} instance
   */
  ButtonHeightMode getButtonHeightMode();
  
  /**
   * Determines the height sizing value for actions in the bar. The meaning of this value depends on
   * {@link #getButtonHeightMode()}.
   * 
   * @return the height sizing specification
   */
  int getButtonHeightSpec();
  
  /**
   * Determines the width sizing mode for displaying individual actions in the bar.
   * 
   * @return a {@link ButtonWidthMode} instance
   */
  ActionWidthMode getButtonWidthMode();
  
  /**
   * Retrieves the width of actions in the bar when {@link #getButtonWidthMode()}
   * is {@link ButtonWidthMode#ABSOLUTE ABSOLUTE}.
   * @return
   */
  int getButtonWidth();
  
  /**
   * Determines whether the margin for individual buttons should be a fixed size.
   *  
   * @return {@code true} to use a fixed margin width;
   *         {@code false} to use the default behavior
   */
  boolean isFixedSizeButtonMargin();
  
  /**
   * Retrieves the size of vertical margin in the bar when {@link #isFixedSizeButtonMargin()}
   * is {@code true}.
   * 
   * @return the fixed button margin in pixels
   */
  int getButtonVerticalMarginSize();
  
  /**
   * Retrieves the button border display style.
   * 
   * @return a {@link ButtonBorderDisplay} instance
   */
  ButtonBorderDisplay getButtonBorderMode();
  
  /**
   * Determines the alignment of text inside buttons in the bar.
   * 
   * @return a {@link ActionBarTextAlignment} instance
   */
  ActionBarTextAlignment getButtonTextAlignment();
  
  /**
   * Retrieves the size of the internal margin in action buttons.
   * 
   * @return the button internal margin in pixels
   */
  int getButtonInternalMarginSize();
  
  /**
   * Determines whether drop-down indicators should always be shown, as opposed to only
   * on hover.
   * 
   * @return {@code true} if drop-down button indicators should always be shown;
   *         {@code false} to show only on hover
   */
  boolean isAlwaysShowDropDowns();
  
  /**
   * Retrieves the background color used for individual buttons.
   * 
   * @return a {@link ColorValue} instance
   */
  ColorValue getButtonBackgroundColor();
  
  /**
   * Retrieves the image resource used for the action button background, if set.
   * 
   * @return an {@link Optional} describing the button background image, or an empty
   *         one of this is not set
   */
  Optional<CDResource> getButtonBackgroundImage();
  
  /**
   * Retrieves the font information for button text.
   * 
   * @return a {@link NotesFont} instance
   */
  NotesFont getFont();
  
  /**
   * Retrieves the color used for button text.
   * 
   * @return a {@link ColorValue} instance
   */
  ColorValue getFontColor();
  
  /**
   * Retrieves the actions from the bar, in declaration order.
   *  
   * @return a {@link List} of the actions in the bar
   */
  List<ActionBarAction> getActions();
}
