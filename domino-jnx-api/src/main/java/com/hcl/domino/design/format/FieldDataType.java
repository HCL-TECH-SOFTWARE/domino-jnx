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
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public enum FieldDataType implements INumberEnum<Short> {
  ERROR(RichTextConstants.FIELD_TYPE_ERROR),
  NUMBER(RichTextConstants.FIELD_TYPE_NUMBER),
  TIME(RichTextConstants.FIELD_TYPE_TIME),
  RICH_TEXT(RichTextConstants.FIELD_TYPE_RICH_TEXT),
  AUTHORS(RichTextConstants.FIELD_TYPE_AUTHORS),
  READERS(RichTextConstants.FIELD_TYPE_READERS),
  NAMES(RichTextConstants.FIELD_TYPE_NAMES),
  KEYWORDS(RichTextConstants.FIELD_TYPE_KEYWORDS),
  TEXT(RichTextConstants.FIELD_TYPE_TEXT),
  SECTION(RichTextConstants.FIELD_TYPE_SECTION),
  PASSWORD(RichTextConstants.FIELD_TYPE_PASSWORD),
  FORMULA(RichTextConstants.FIELD_TYPE_FORMULA),
  TIMEZONE(RichTextConstants.FIELD_TYPE_TIMEZONE);

  private final short value;

  FieldDataType(final short value) {
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