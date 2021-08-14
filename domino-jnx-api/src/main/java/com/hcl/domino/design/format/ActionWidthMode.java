package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;

/**
 * Describes the options for sizing the width of individual actions for display.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ActionWidthMode implements INumberEnum<Byte> {
  /** Width is calculated based on text length and image width */
  DEFAULT(0),
  /**
   * Width is at least button background image width or wider if needed to fit
   * text and image.
   */
  BACKGROUND(1),
  /** Width is set to value in the structure */
  ABSOLUTE(2);

  private final byte value;

  ActionWidthMode(final int value) {
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