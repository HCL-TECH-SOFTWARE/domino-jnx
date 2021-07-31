package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.ViewFormatConstants;

/**
 * Represents the line-spacing options used in views and folders.
 * 
 * @author Jesse Gallagher
 */
public enum ViewLineSpacing implements INumberEnum<Byte> {
  SINGLE_SPACE(ViewFormatConstants.VIEW_TABLE_SINGLE_SPACE),
  ONE_POINT_25_SPACE(ViewFormatConstants.VIEW_TABLE_ONE_POINT_25_SPACE),
  ONE_POINT_50_SPACE(ViewFormatConstants.VIEW_TABLE_ONE_POINT_50_SPACE),
  ONE_POINT_75_SPACE(ViewFormatConstants.VIEW_TABLE_ONE_POINT_75_SPACE),
  DOUBLE_SPACE(ViewFormatConstants.VIEW_TABLE_DOUBLE_SPACE);

  private final byte value;

  ViewLineSpacing(final byte value) {
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