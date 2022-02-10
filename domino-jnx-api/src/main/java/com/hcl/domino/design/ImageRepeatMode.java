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
package com.hcl.domino.design;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.IStringEnum;

/**
 * Represents the way images should be repeated in various contexts.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ImageRepeatMode implements INumberEnum<Short>, IStringEnum {
  ONCE((short)1, "1"), //$NON-NLS-1$
  VERTICAL((short)2, "2"), //$NON-NLS-1$
  HORIZONTAL((short)3, "3"), //$NON-NLS-1$
  TILE((short)4, "4"), //$NON-NLS-1$
  SIZE_TO_FIT((short)5, "5"), //$NON-NLS-1$
  CENTER((short)6, "6"), //$NON-NLS-1$
  CENTER_BASED_TILING((short)7, null);
  
  private final short value;
  private final String stringVal;
  private ImageRepeatMode(short value, String stringVal) {
    this.value = value;
    this.stringVal = stringVal;
  }

  @Override
  public long getLongValue() {
    return value;
  }

  @Override
  public Short getValue() {
    return value;
  }

  @Override
  public String getStringValue() {
    return stringVal;
  }
}
