package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents the way the background image of the action bar should be repeated.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ActionBarBackgroundRepeat implements INumberEnum<Short> {
  REPEATONCE(RichTextConstants.ACTIONBAR_BACKGROUND_REPEATONCE),
  REPEATVERT(RichTextConstants.ACTIONBAR_BACKGROUND_REPEATVERT),
  REPEATHORIZ(RichTextConstants.ACTIONBAR_BACKGROUND_REPEATHORIZ),
  TILE(RichTextConstants.ACTIONBAR_BACKGROUND_TILE),
  CENTER_TILE(RichTextConstants.ACTIONBAR_BACKGROUND_CENTER_TILE),
  REPEATSIZE(RichTextConstants.ACTIONBAR_BACKGROUND_REPEATSIZE),
  REPEATCENTER(RichTextConstants.ACTIONBAR_BACKGROUND_REPEATCENTER);

  private final short value;

  ActionBarBackgroundRepeat(final int value) {
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