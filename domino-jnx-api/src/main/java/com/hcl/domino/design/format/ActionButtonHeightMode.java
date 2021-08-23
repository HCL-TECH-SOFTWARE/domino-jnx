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
package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;

/**
 * Describes the options for sizing the height of an action bar for display.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ActionButtonHeightMode implements INumberEnum<Short> {
  /** Use the default height of the bar */
  DEFAULT((short)0),
  /** Base the height of the bar on a font-relative value */
  EXS((short)2),
  /** Specify the bar height in pixels */
  FIXED((short)1);
  
  private final short value;
  private ActionButtonHeightMode(short value) {
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