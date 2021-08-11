package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;

/**
 * Represents positioning options for a column when presented in a tile view in
 * a Composite Application.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum TileViewerPosition implements INumberEnum<Short> {
  TOP(0),
  BOTTOM(1),
  HIDE(2);
  
  private final short value;
  private TileViewerPosition(int value) {
    this.value = (short)value;
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