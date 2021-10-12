package com.hcl.domino.richtext.records;

import com.hcl.domino.misc.INumberEnum;

/**
 * @author Jesse Gallagher
 * @since 1.0.43
 */
public enum DDEFormat implements INumberEnum<Short> {
  TEXT(0x01),
  METAFILE(0x02),
  BITMAP(0x03),
  RTF(0x04),
  OWNERLINK(0x06),
  OBJECTLINK(0x07),
  NATIVE(0x08),
  ICON(0x09);

  private final short value;

  DDEFormat(final int value) {
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