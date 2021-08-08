package com.hcl.domino.design.action;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents the available language types for an action bar action.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ActionLanguage implements INumberEnum<Short> {
  RUN_FORMULA(RichTextConstants.ACTION_RUN_FORMULA),
  RUN_SCRIPT(RichTextConstants.ACTION_RUN_SCRIPT),
  RUN_AGENT(RichTextConstants.ACTION_RUN_AGENT),
  OLDSYS_COMMAND(RichTextConstants.ACTION_OLDSYS_COMMAND),
  SYS_COMMAND(RichTextConstants.ACTION_SYS_COMMAND),
  PLACEHOLDER(RichTextConstants.ACTION_PLACEHOLDER),
  RUN_JAVASCRIPT(RichTextConstants.ACTION_RUN_JAVASCRIPT);

  private final short value;

  ActionLanguage(final int value) {
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