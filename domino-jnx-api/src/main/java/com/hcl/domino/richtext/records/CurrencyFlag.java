package com.hcl.domino.richtext.records;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

public enum CurrencyFlag implements INumberEnum<Byte> {
  SYMFOLLOWS((byte) RichTextConstants.NCURFMT_SYMFOLLOWS),
  USESPACES((byte) RichTextConstants.NCURFMT_USESPACES),
  ISOSYMUSED((byte) RichTextConstants.NCURFMT_ISOSYMUSED),
  ;

  private final byte value;

  CurrencyFlag(final byte value) {
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