package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.ViewFormatConstants;

/**
 * Represents the available calendar formats for display.
 * 
 * @author Jesse Gallagher
 * @since 1.0.41
 */
public enum CalendarLayout implements INumberEnum<Byte> {
  TWO_DAY(ViewFormatConstants.VIEW_CAL_FORMAT_TWO_DAY),
  ONE_WEEK(ViewFormatConstants.VIEW_CAL_FORMAT_ONE_WEEK),
  TWO_WEEKS(ViewFormatConstants.VIEW_CAL_FORMAT_TWO_WEEKS),
  ONE_MONTH(ViewFormatConstants.VIEW_CAL_FORMAT_ONE_MONTH),
  ONE_YEAR(ViewFormatConstants.VIEW_CAL_FORMAT_ONE_YEAR),
  ONE_DAY(ViewFormatConstants.VIEW_CAL_FORMAT_ONE_DAY),
  WORK_WEEK(ViewFormatConstants.VIEW_CAL_FORMAT_WORK_WEEK);

  private final byte value;

  CalendarLayout(final byte value) {
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