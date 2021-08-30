package com.hcl.domino.data;

import com.hcl.domino.misc.INumberEnum;

/**
 * These symbols are used to specify text color, graphic color and background
 * color in a variety of C API structures.
 *
 * @author Karsten Lehmann
 */
public enum StandardColors implements INumberEnum<Byte> {
  BLACK(0),
  WHITE(1),
  RED(2),
  GREEN(3),
  BLUE(4),
  MAGENTA(5),
  YELLOW(6),
  CYAN(7),
  DKRED(8),
  DKGREEN(9),
  DKBLUE(10),
  DKMAGENTA(11),
  DKYELLOW(12),
  DKCYAN(13),
  GRAY(14),
  LTGRAY(15);

  private final byte m_color;

  StandardColors(final int colorIdx) {
    this.m_color = (byte) (colorIdx & 0xff);
  }

  @Override
  public long getLongValue() {
    return this.m_color;
  }

  @Override
  public Byte getValue() {
    return this.m_color;
  }
}
