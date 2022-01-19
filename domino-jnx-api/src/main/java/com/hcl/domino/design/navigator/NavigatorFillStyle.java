package com.hcl.domino.design.navigator;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;

/**
 * Represents values for color fille styles in Navigator components.
 * 
 * @author Jesse Gallagher
 * @since 1.1.2
 */
public enum NavigatorFillStyle implements INumberEnum<Short> {
  TRANSPARENT((short)0),
  SOLID(NotesConstants.VM_FILL_SOLID);
  
  private final short value;
  
  private NavigatorFillStyle(short value) {
    this.value = value;
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
