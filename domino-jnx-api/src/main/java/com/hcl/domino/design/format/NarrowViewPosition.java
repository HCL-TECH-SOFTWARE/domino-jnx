package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;

/**
 * Represents positioning options for a column when presented in a Composite
 * Application.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum NarrowViewPosition implements INumberEnum<Short> {
  KEEP_ON_TOP(0),
  HIDE(2),
  WRAP(1);
  
  private final short value;
  private NarrowViewPosition(int value) {
    this.value = (short)value;
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