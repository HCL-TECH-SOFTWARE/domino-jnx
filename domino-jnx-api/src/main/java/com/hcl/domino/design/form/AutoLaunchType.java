package com.hcl.domino.design.form;

import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.misc.INumberEnum;

/**
 * Represents the behaviors for auto-launching an embedded component in the client UI.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public enum AutoLaunchType implements INumberEnum<Integer> {
  
  NONE(DesignConstants.AUTOLAUNCH_OBJTYPE_NONE),
  /**  OLE Class ID (GUID)  */
  OLE_CLASS(DesignConstants.AUTOLAUNCH_OBJTYPE_OLE_CLASS),
  /**  First OLE Object  */
  OLEOBJ(DesignConstants.AUTOLAUNCH_OBJTYPE_OLEOBJ),
  /**  First Notes doclink  */
  DOCLINK(DesignConstants.AUTOLAUNCH_OBJTYPE_DOCLINK),
  /**  First Attachment  */
  ATTACH(DesignConstants.AUTOLAUNCH_OBJTYPE_ATTACH),
  /**  AutoLaunch the url in the URL field  */
  URL(DesignConstants.AUTOLAUNCH_OBJTYPE_URL),
  ;

  private final int value;

  AutoLaunchType(final int value) {
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