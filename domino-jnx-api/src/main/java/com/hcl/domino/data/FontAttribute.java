package com.hcl.domino.data;

import com.hcl.domino.misc.INumberEnum;

public enum FontAttribute implements INumberEnum<Byte> {
  BOLD((byte) 0x01),
  ITALIC((byte) 0x02),
  UNDERLINE((byte) 0x04),
  STRIKEOUT((byte) 0x08),
  SUPER((byte) 0x10),
  SUB((byte) 0x20),
  EFFECT((byte) 0x80),
  SHADOW((byte) 0x80),
  EMBOSS((byte) 0x90),
  EXTRUDE((byte) 0xa0);

  private final byte value;

  FontAttribute(final byte value) {
    this.value = value;
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