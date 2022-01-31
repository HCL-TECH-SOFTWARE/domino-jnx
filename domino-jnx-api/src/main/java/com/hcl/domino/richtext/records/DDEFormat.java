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
package com.hcl.domino.richtext.records;

import com.hcl.domino.misc.INumberEnum;

/**
 * @author Jesse Gallagher
 * @since 1.0.43
 */
public enum DDEFormat implements INumberEnum<Short> {
  TEXT(0x01),
  METAFILE(0x02),
  BITMAP(0x03),
  RTF(0x04),
  OWNERLINK(0x06),
  OBJECTLINK(0x07),
  NATIVE(0x08),
  ICON(0x09);

  private final short value;

  DDEFormat(final int value) {
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