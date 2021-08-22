package com.hcl.domino.design.form;

import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.Form;
import com.hcl.domino.misc.INumberEnum;

/**
 * Represents the conditions for when to auto-launch an object when
 * {@link Form.AutoLaunchSettings#getType()} is an object type.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public enum AutoLaunchWhen implements INumberEnum<Integer> {
  CREATE(DesignConstants.LAUNCH_WHEN_CREATE),
  EDIT(DesignConstants.LAUNCH_WHEN_EDIT),
  READ(DesignConstants.LAUNCH_WHEN_READ);
  
  private final int value;

  AutoLaunchWhen(final int value) {
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