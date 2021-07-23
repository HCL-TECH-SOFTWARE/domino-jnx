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

public enum DateTimeFlag implements INumberEnum<Integer> {
  /** Validity bit: If 1, use new DTFMT; if 0, use old TFMT */
  VALID(RichTextConstants.DT_VALID),
  /** Require 4 digit year on INPUT (not output) */
  FOURDIGITYEAR(RichTextConstants.DT_4DIGITYEAR),
  /** Require months be INPUT as letters, not digits (e.g. "jan", not 01) */
  ALPHAMONTH(RichTextConstants.DT_ALPHAMONTH),
  /** Display time element on output */
  SHOWTIME(RichTextConstants.DT_SHOWTIME),
  /** Display date element on output */
  SHOWDATE(RichTextConstants.DT_SHOWDATE),
  /** Display time on output using 24 hour clock format */
  TWENTYFOURHOUR(RichTextConstants.DT_24HOUR),
  /** Displays the date as an abbriviated date */
  SHOWABBREV(RichTextConstants.DT_SHOWABBREV),
  /** Date element order: Year, Month, Day, Day-of-week */
  STYLE_YMD(RichTextConstants.DT_STYLE_YMD),
  /** Date element order: Day-of-week, Month, Day, Year */
  STYLE_MDY(RichTextConstants.DT_STYLE_MDY),
  /** Date element order: Day-of-week, Day, Month, Year */
  STYLE_DMY(RichTextConstants.DT_STYLE_DMY),
  /** This is where we store the style value in DTFlags */
  STYLE_MSK(RichTextConstants.DT_STYLE_MSK);

  private final int value;

  DateTimeFlag(final int value) {
    this.value = value;
  }

  @Override
  public long getLongValue() {
    return this.value;
  }

  @Override
  public Integer getValue() {
    return this.value;
  }
}