package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents the options for displaying borders on action-bar buttons.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ButtonBorderDisplay implements INumberEnum<Short> {
  ONMOUSEOVER(RichTextConstants.ACTION_SET_3D_ONMOUSEOVER),
  ALWAYS(RichTextConstants.ACTION_SET_3D_ALWAYS),
  NEVER(RichTextConstants.ACTION_SET_3D_NEVER),
  NOTES(RichTextConstants.ACTION_SET_3D_NOTES);

  private final short value;

  ButtonBorderDisplay(final int value) {
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