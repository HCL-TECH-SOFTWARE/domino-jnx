package com.hcl.domino.design;

import com.hcl.domino.misc.INumberEnum;

/**
 * Represents the way images should be repeated in various contexts.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ImageRepeatMode implements INumberEnum<Short> {
  ONCE((short)1),
  VERTICAL((short)2),
  HORIZONTAL((short)3),
  TILE((short)4),
  SIZE_TO_FIT((short)5),
  CENTER((short)6);
  private final short value;
  private ImageRepeatMode(short value) {
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
