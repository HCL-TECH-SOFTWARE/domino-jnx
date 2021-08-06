package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents the way an individual action bar action may be displayed.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ActionBarControlType implements INumberEnum<Short> {
  BUTTON(RichTextConstants.ACTION_CONTROL_TYPE_BUTTON),
  CHECKBOX(RichTextConstants.ACTION_CONTROL_TYPE_CHECKBOX),
  MENU_SEPARATOR(RichTextConstants.ACTION_CONTROL_TYPE_MENU_SEPARATOR);

  private final short value;

  ActionBarControlType(final int value) {
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