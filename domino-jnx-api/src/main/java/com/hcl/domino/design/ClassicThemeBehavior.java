package com.hcl.domino.design;

import com.hcl.domino.design.DesignElement.ThemeableClassicElement;
import com.hcl.domino.misc.INumberEnum;

/**
 * This enum describes the options available for elements conforming to
 * {@link ThemeableClassicElement}.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ClassicThemeBehavior implements INumberEnum<Byte> {
  USE_DATABASE_SETTING(DesignConstants.THEME_DEFAULT),
  INHERIT_FROM_OS(DesignConstants.THEME_ENABLE),
  DONT_INHERIT_FROM_OS(DesignConstants.THEME_DISABLE);
  
  private final byte value;
  private ClassicThemeBehavior(byte value) {
    this.value = value;
  }

  @Override
  public long getLongValue() {
    return value;
  }

  @Override
  public Byte getValue() {
    return value;
  }
}