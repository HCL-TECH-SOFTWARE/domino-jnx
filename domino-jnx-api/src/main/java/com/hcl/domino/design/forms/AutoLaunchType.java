/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.design.forms;

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