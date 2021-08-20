package com.hcl.domino.design.frameset;

import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.misc.INumberEnum;

/**
 * Represents the modes for sizing a frame within a frameset.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public enum FrameSizingType implements INumberEnum<Short> {
  PIXELS(DesignConstants.PIXELS_LengthType),
  PERCENTAGE(DesignConstants.PERCENTAGE_LengthType),
  RELATIVE(DesignConstants.RELATIVE_LengthType);
  private final short value;
  private FrameSizingType(short value) {
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