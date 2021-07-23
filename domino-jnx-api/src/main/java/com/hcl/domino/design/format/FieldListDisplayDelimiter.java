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

public enum FieldListDisplayDelimiter implements INumberEnum<Short> {
  SPACE(RichTextConstants.LDDELIM_SPACE),

  COMMA(RichTextConstants.LDDELIM_COMMA),
  SEMICOLON(RichTextConstants.LDDELIM_SEMICOLON),
  NEWLINE(RichTextConstants.LDDELIM_NEWLINE),
  BLANKLINE(RichTextConstants.LDDELIM_BLANKLINE);

  private final short value;

  FieldListDisplayDelimiter(final short value) {
    this.value = value;
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