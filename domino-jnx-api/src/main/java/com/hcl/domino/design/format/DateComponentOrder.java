package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents the options for the order of date components in collection
 * columns and fields.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum DateComponentOrder implements INumberEnum<Byte> {
  YMDW(RichTextConstants.DT_STYLE_YMD),
  WMDY(RichTextConstants.DT_STYLE_MDY),
  WDMY(RichTextConstants.DT_STYLE_DMY);
  
  private final byte value;
  private DateComponentOrder(int value) {
    this.value = (byte)value;
  }
  
  @Override
  public long getLongValue() {
    return value;
  }
  
  @Override
  public Byte getValue() {
    return value;
  }
}
