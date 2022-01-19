package com.hcl.domino.design;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;

/**
 * Represents the private or shared state of a design element in various
 * contexts.
 * 
 * @author Jesse Gallagher
 * @since 1.1.2
 */
public enum DesignType implements INumberEnum<Short> {
  SHARED((short)NotesConstants.DESIGN_TYPE_SHARED),
  PRIVATE((short)NotesConstants.DESIGN_TYPE_PRIVATE_DATABASE);
  
  private final short value;
  
  private DesignType(short value) {
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
