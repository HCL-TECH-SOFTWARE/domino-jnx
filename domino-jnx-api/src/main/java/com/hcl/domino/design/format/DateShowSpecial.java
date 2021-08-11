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

public enum DateShowSpecial implements INumberEnum<Byte> {
  /** Use 'Today', 'Yesterday', 'Tomorrow', when possible */
  TODAY(RichTextConstants.DT_DSPEC_TODAY),
  /** Always display year on OUTPUT as 4 digit year */
  Y4(RichTextConstants.DT_DSPEC_Y4),
  /** Show 4 digit year for 21st century */
  SHOW_21ST_4DIGIT(RichTextConstants.DT_DSPEC_21Y4),
  /** Display year when not the current year */
  CURYR(RichTextConstants.DT_DSPEC_CURYR),
  ;

  private final byte value;

  DateShowSpecial(final byte value) {
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