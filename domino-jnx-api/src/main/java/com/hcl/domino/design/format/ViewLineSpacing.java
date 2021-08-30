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
import com.hcl.domino.misc.ViewFormatConstants;

/**
 * Represents the line-spacing options used in views and folders.
 * 
 * @author Jesse Gallagher
 */
public enum ViewLineSpacing implements INumberEnum<Byte> {
  SINGLE_SPACE(ViewFormatConstants.VIEW_TABLE_SINGLE_SPACE),
  ONE_POINT_25_SPACE(ViewFormatConstants.VIEW_TABLE_ONE_POINT_25_SPACE),
  ONE_POINT_50_SPACE(ViewFormatConstants.VIEW_TABLE_ONE_POINT_50_SPACE),
  ONE_POINT_75_SPACE(ViewFormatConstants.VIEW_TABLE_ONE_POINT_75_SPACE),
  DOUBLE_SPACE(ViewFormatConstants.VIEW_TABLE_DOUBLE_SPACE);

  private final byte value;

  ViewLineSpacing(final byte value) {
    this.value = value;
  }

  @Override
  public long getLongValue() {
    return this.value;
  }

  @Override
  public Byte getValue() {
    return this.value;
  }
}