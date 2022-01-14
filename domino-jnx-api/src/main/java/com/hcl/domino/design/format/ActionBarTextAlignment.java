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
 * Represents the alignment options for text inside buttons in an action
 * bar.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ActionBarTextAlignment implements INumberEnum<Byte> {
  LEFT(RichTextConstants.ACTIONBAR_BUTTON_TEXT_LEFT),
  CENTER(RichTextConstants.ACTIONBAR_BUTTON_TEXT_CENTER),
  RIGHT(RichTextConstants.ACTIONBAR_BUTTON_TEXT_RIGHT);

  private final byte value;

  ActionBarTextAlignment(final int value) {
    this.value = (byte) value;
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