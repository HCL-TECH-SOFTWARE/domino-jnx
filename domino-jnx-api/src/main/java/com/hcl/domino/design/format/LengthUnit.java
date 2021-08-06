package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.structures.LengthValue;

/**
 * Represents the units available in {@link LengthValue}.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum LengthUnit implements INumberEnum<Byte> {
  UNKNOWN(RichTextConstants.CDLENGTH_UNITS_UNKNOWN),
  TWIPS(RichTextConstants.CDLENGTH_UNITS_TWIPS),
  PIXELS(RichTextConstants.CDLENGTH_UNITS_PIXELS),
  PERCENT(RichTextConstants.CDLENGTH_UNITS_PERCENT),
  EMS(RichTextConstants.CDLENGTH_UNITS_EMS),
  EXS(RichTextConstants.CDLENGTH_UNITS_EXS),
  CHARS(RichTextConstants.CDLENGTH_UNITS_CHARS);

  private final byte value;

  LengthUnit(final int value) {
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