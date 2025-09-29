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
package com.hcl.domino.data;

import com.hcl.domino.misc.INumberEnum;

/**
 * Enum for Item datatypes
 * All datatypes below are passed to NSF in either host (machine-specific
 * byte ordering and padding) or canonical form (Intel 86 packed form).
 * The format of each datatype, as it is passed to and from NSF functions,
 * is listed below in the comment field next to each of the data types.
 * (This host/canonical issue is NOT applicable to Intel86 machines,
 * because on that machine, they are the same and no conversion is required).
 * On all other machines, use the ODS subroutine package to perform
 * conversions of those datatypes in canonical format before they can
 * be interpreted.
 */
public enum ItemDataType implements INumberEnum<Short> {
  /*    "Computable" Data Types */
  TYPE_ERROR(ItemDataTypeConstants.TYPE_ERROR),
  TYPE_UNAVAILABLE(ItemDataTypeConstants.TYPE_UNAVAILABLE),
  TYPE_TEXT(ItemDataTypeConstants.TYPE_TEXT),
  TYPE_TEXT_LIST(ItemDataTypeConstants.TYPE_TEXT_LIST),
  TYPE_NUMBER(ItemDataTypeConstants.TYPE_NUMBER),
  TYPE_NUMBER_RANGE(ItemDataTypeConstants.TYPE_NUMBER_RANGE),
  TYPE_TIME(ItemDataTypeConstants.TYPE_TIME),
  TYPE_TIME_RANGE(ItemDataTypeConstants.TYPE_TIME_RANGE),
  TYPE_FORMULA(ItemDataTypeConstants.TYPE_FORMULA),
  TYPE_USERID(ItemDataTypeConstants.TYPE_USERID),
  /*    "Non-Computable" Data Types */
  TYPE_SIGNATURE(ItemDataTypeConstants.TYPE_SIGNATURE),
  TYPE_ACTION(ItemDataTypeConstants.TYPE_ACTION),
  TYPE_WORKSHEET_DATA(ItemDataTypeConstants.TYPE_WORKSHEET_DATA),
  TYPE_VIEWMAP_LAYOUT(ItemDataTypeConstants.TYPE_VIEWMAP_LAYOUT),
  TYPE_SEAL2(ItemDataTypeConstants.TYPE_SEAL2),
  TYPE_LSOBJECT(ItemDataTypeConstants.TYPE_LSOBJECT),
  TYPE_ICON(ItemDataTypeConstants.TYPE_ICON),
  TYPE_VIEW_FORMAT(ItemDataTypeConstants.TYPE_VIEW_FORMAT),
  TYPE_SCHED_LIST(ItemDataTypeConstants.TYPE_SCHED_LIST),
  TYPE_VIEWMAP_DATASET(ItemDataTypeConstants.TYPE_VIEWMAP_DATASET),
  TYPE_SEAL(ItemDataTypeConstants.TYPE_SEAL),
  TYPE_MIME_PART(ItemDataTypeConstants.TYPE_MIME_PART),
  TYPE_SEALDATA(ItemDataTypeConstants.TYPE_SEALDATA),
  TYPE_NOTELINK_LIST(ItemDataTypeConstants.TYPE_NOTELINK_LIST),
  TYPE_COLLATION(ItemDataTypeConstants.TYPE_COLLATION),
  TYPE_RFC822_TEXT(ItemDataTypeConstants.TYPE_RFC822_TEXT),
  /** Richtext item */
  TYPE_COMPOSITE(ItemDataTypeConstants.TYPE_COMPOSITE),
  TYPE_OBJECT(ItemDataTypeConstants.TYPE_OBJECT),
  TYPE_HTML(ItemDataTypeConstants.TYPE_HTML),
  TYPE_ASSISTANT_INFO(ItemDataTypeConstants.TYPE_ASSISTANT_INFO),
  TYPE_HIGHLIGHTS(ItemDataTypeConstants.TYPE_HIGHLIGHTS),
  TYPE_NOTEREF_LIST(ItemDataTypeConstants.TYPE_NOTEREF_LIST),
  TYPE_QUERY(ItemDataTypeConstants.TYPE_QUERY),
  TYPE_USERDATA(ItemDataTypeConstants.TYPE_USERDATA),
  TYPE_INVALID_OR_UNKNOWN(ItemDataTypeConstants.TYPE_INVALID_OR_UNKNOWN),
  TYPE_SEAL_LIST(ItemDataTypeConstants.TYPE_SEAL_LIST),
  TYPE_CALENDAR_FORMAT(ItemDataTypeConstants.TYPE_CALENDAR_FORMAT),
  TYPE_OUTLINE_FORMAT(ItemDataTypeConstants.TYPE_OUTLINE_FORMAT);

  public final short value;

  ItemDataType(final int dataType) {
    this.value = (short) dataType;
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