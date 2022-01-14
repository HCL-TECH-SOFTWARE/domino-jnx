/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents the options for the order of date components in collection
 * columns and fields.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum DateComponentOrder implements INumberEnum<Byte> {
  YMDW(RichTextConstants.DT_STYLE_YMD),
  WMDY(RichTextConstants.DT_STYLE_MDY),
  WDMY(RichTextConstants.DT_STYLE_DMY);
  
  private final byte value;
  private DateComponentOrder(int value) {
    this.value = (byte)value;
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
