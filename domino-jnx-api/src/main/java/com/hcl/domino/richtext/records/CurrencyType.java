package com.hcl.domino.richtext.records;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

public enum CurrencyType implements INumberEnum<Byte> {
  COMMON(RichTextConstants.NCURFMT_COMMON),
  CUSTOM(RichTextConstants.NCURFMT_CUSTOM),
  ;

  private final byte value;

  CurrencyType(final byte value) {
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