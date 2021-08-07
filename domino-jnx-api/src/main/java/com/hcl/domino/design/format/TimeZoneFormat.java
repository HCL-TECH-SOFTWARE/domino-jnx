package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

public enum TimeZoneFormat implements INumberEnum<Byte> {
  /** all times converted to THIS zone */
  NEVER(RichTextConstants.TZFMT_NEVER),
  /** show only when outside this zone */
  SOMETIMES(RichTextConstants.TZFMT_SOMETIMES),
  /** show on all times, regardless */
  ALWAYS(RichTextConstants.TZFMT_ALWAYS);

  private final byte value;

  TimeZoneFormat(final byte value) {
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