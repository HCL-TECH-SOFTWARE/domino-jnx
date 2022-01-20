package com.hcl.domino.design.navigator;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;

/**
 * Represents values for line styles in Navigator components.
 * 
 * @author Jesse Gallagher
 * @since 1.1.2
 */
public enum NavigatorLineStyle implements INumberEnum<Short> {
  SOLID(NotesConstants.VM_LINE_SOLID),
  NONE(NotesConstants.VM_LINE_NONE);
  
  private final short value;
  
  private NavigatorLineStyle(short value) {
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
