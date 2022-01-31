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

/**
 * Represents positioning options for a column when presented in a tile view in
 * a Composite Application.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum TileViewerPosition implements INumberEnum<Short> {
  TOP(0),
  BOTTOM(1),
  HIDE(2);
  
  private final short value;
  private TileViewerPosition(int value) {
    this.value = (short)value;
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