package com.hcl.domino.design.frameset;

import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.misc.INumberEnum;

/**
 * Represents the options for handling scrolling within a frame.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public enum FrameScrollStyle implements INumberEnum<Short> {
  ALWAYS(DesignConstants.ALWAYS_ScrollStyle),
  NEVER(DesignConstants.NEVER_ScrollStyle),
  AUTO(DesignConstants.AUTO_ScrollStyle);
  private final short value;
  private FrameScrollStyle(short value) {
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