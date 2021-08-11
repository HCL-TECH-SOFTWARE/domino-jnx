package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;

/**
 * Describes the options for sizing the height of an action bar for display.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ActionButtonHeightMode implements INumberEnum<Short> {
  /** Use the default height of the bar */
  DEFAULT((short)0),
  /** Base the height of the bar on a font-relative value */
  EXS((short)2),
  /** Specify the bar height in pixels */
  FIXED((short)1);
  
  private final short value;
  private ActionButtonHeightMode(short value) {
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