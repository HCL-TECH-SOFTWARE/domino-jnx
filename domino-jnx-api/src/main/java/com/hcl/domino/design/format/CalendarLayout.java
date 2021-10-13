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
 * Represents the available calendar formats for display.
 * 
 * @author Jesse Gallagher
 * @since 1.0.41
 */
public enum CalendarLayout implements INumberEnum<Byte> {
  TWO_DAY(ViewFormatConstants.VIEW_CAL_FORMAT_TWO_DAY),
  ONE_WEEK(ViewFormatConstants.VIEW_CAL_FORMAT_ONE_WEEK),
  TWO_WEEKS(ViewFormatConstants.VIEW_CAL_FORMAT_TWO_WEEKS),
  ONE_MONTH(ViewFormatConstants.VIEW_CAL_FORMAT_ONE_MONTH),
  ONE_YEAR(ViewFormatConstants.VIEW_CAL_FORMAT_ONE_YEAR),
  ONE_DAY(ViewFormatConstants.VIEW_CAL_FORMAT_ONE_DAY),
  WORK_WEEK(ViewFormatConstants.VIEW_CAL_FORMAT_WORK_WEEK);

  private final byte value;

  CalendarLayout(final byte value) {
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