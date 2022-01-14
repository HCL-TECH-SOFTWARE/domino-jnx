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
 * Represents the way the background image of the action bar should be repeated.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ActionBarBackgroundRepeat implements INumberEnum<Short> {
  REPEATONCE(RichTextConstants.ACTIONBAR_BACKGROUND_REPEATONCE),
  REPEATVERT(RichTextConstants.ACTIONBAR_BACKGROUND_REPEATVERT),
  REPEATHORIZ(RichTextConstants.ACTIONBAR_BACKGROUND_REPEATHORIZ),
  TILE(RichTextConstants.ACTIONBAR_BACKGROUND_TILE),
  CENTER_TILE(RichTextConstants.ACTIONBAR_BACKGROUND_CENTER_TILE),
  REPEATSIZE(RichTextConstants.ACTIONBAR_BACKGROUND_REPEATSIZE),
  REPEATCENTER(RichTextConstants.ACTIONBAR_BACKGROUND_REPEATCENTER);

  private final short value;

  ActionBarBackgroundRepeat(final int value) {
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