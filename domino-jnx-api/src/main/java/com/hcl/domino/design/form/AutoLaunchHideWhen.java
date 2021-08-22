package com.hcl.domino.design.form;

import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.Form;
import com.hcl.domino.misc.INumberEnum;

/**
 * Represents the conditions for hiding an auto-launch object when
 * {@link Form.AutoLaunchSettings#getType()} is an object type.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public enum AutoLaunchHideWhen implements INumberEnum<Integer> {
  OPEN_CREATE(DesignConstants.HIDE_OPEN_CREATE),
  OPEN_EDIT(DesignConstants.HIDE_OPEN_EDIT),
  OPEN_READ(DesignConstants.HIDE_OPEN_READ),
  CLOSE_CREATE(DesignConstants.HIDE_CLOSE_CREATE),
  CLOSE_EDIT(DesignConstants.HIDE_CLOSE_EDIT),
  CLOSE_READ(DesignConstants.HIDE_CLOSE_READ);
  
  private final int value;

  AutoLaunchHideWhen(final int value) {
    this.value = value;
  }

  @Override
  public long getLongValue() {
    return this.value;
  }

  @Override
  public Integer getValue() {
    return this.value;
  }
}