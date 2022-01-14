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

public enum YearFormat implements INumberEnum<Byte> {
  /** 2 digit year */
  YY(RichTextConstants.DT_YFMT_YY),
  /** 4 digit year */
  YYYY(RichTextConstants.DT_YFMT_YYYY),
  /**
   * Single letter (first letter ) of epoch name and 1 or 2 digit (no leading
   * zeros) year
   */
  GE(RichTextConstants.DT_YFMT_GE),
  /**
   * Single letter (first letter ) of epoch name and 2 digit (with leading zeros,
   * if necessary) year
   */
  GEE(RichTextConstants.DT_YFMT_GEE),
  GGE(RichTextConstants.DT_YFMT_GGE),
  /** Abbreviated spelling and 2 digit (with leading zeros, if necessary) year */
  GGEE(RichTextConstants.DT_YFMT_GGEE),
  GGGE(RichTextConstants.DT_YFMT_GGGE),
  /**
   * fully spelled out epoch name and 2 digit (with leading zeros, if necessary)
   * year
   */
  GGGEE(RichTextConstants.DT_YFMT_GGGEE),
  ;

  private final byte value;

  YearFormat(final byte value) {
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