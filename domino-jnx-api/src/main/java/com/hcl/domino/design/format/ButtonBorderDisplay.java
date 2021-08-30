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
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents the options for displaying borders on action-bar buttons.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ButtonBorderDisplay implements INumberEnum<Short> {
  ONMOUSEOVER(RichTextConstants.ACTION_SET_3D_ONMOUSEOVER),
  ALWAYS(RichTextConstants.ACTION_SET_3D_ALWAYS),
  NEVER(RichTextConstants.ACTION_SET_3D_NEVER),
  NOTES(RichTextConstants.ACTION_SET_3D_NOTES);

  private final short value;

  ButtonBorderDisplay(final int value) {
    this.value = (short) value;
  }

  @Override
  public long getLongValue() {
    return this.value;
  }

  @Override
  public Short getValue() {
    return this.value;
  }
}