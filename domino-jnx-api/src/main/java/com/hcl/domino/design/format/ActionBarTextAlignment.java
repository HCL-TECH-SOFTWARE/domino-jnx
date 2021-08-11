package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents the alignment options for text inside buttons in an action
 * bar.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ActionBarTextAlignment implements INumberEnum<Byte> {
  LEFT(RichTextConstants.ACTIONBAR_BUTTON_TEXT_LEFT),
  CENTER(RichTextConstants.ACTIONBAR_BUTTON_TEXT_CENTER),
  RIGHT(RichTextConstants.ACTIONBAR_BUTTON_TEXT_RIGHT);

  private final byte value;

  ActionBarTextAlignment(final int value) {
    this.value = (byte) value;
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