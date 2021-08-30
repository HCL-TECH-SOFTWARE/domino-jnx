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
 * Describes the options for sizing the width of individual actions for display.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ActionWidthMode implements INumberEnum<Byte> {
  /** Width is calculated based on text length and image width */
  DEFAULT(0),
  /**
   * Width is at least button background image width or wider if needed to fit
   * text and image.
   */
  BACKGROUND(1),
  /** Width is set to value in the structure */
  ABSOLUTE(2);

  private final byte value;

  ActionWidthMode(final int value) {
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