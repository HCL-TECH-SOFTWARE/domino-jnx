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
package com.hcl.domino.data;

import com.hcl.domino.misc.INumberEnum;

/**
 * Enum for Item datatypes
 * 
 * All datatypes below are passed to NSF in either host (machine-specific
byte ordering and padding) or canonical form (Intel 86 packed form).
The format of each datatype, as it is passed to and from NSF functions,
is listed below in the comment field next to each of the data types.
(This host/canonical issue is NOT applicable to Intel86 machines,
because on that machine, they are the same and no conversion is required).
On all other machines, use the ODS subroutine package to perform
conversions of those datatypes in canonical format before they can
be interpreted.
 */
public enum ItemDataType implements INumberEnum<Short> {
	/*	"Computable" Data Types */
	TYPE_ERROR (0 + (1 << 8)),
	TYPE_UNAVAILABLE (0 + (2 << 8)),
	TYPE_TEXT (0 + (5 << 8)),
	TYPE_TEXT_LIST (1 + (5 << 8)),
	TYPE_NUMBER (0 + (3 << 8)),
	TYPE_NUMBER_RANGE (1 + (3 << 8)),
	TYPE_TIME (0 + (4 << 8)),
	TYPE_TIME_RANGE (1 + (4 << 8)),
	TYPE_FORMULA (0 + (6 << 8)),
	TYPE_USERID (0 + (7 << 8)),
	/*	"Non-Computable" Data Types */
	TYPE_SIGNATURE (8 + (0 << 8)),
	TYPE_ACTION (16 + (0 << 8)),
	TYPE_WORKSHEET_DATA (13 + (0 << 8)),
	TYPE_VIEWMAP_LAYOUT (19 + (0 << 8)),
	TYPE_SEAL2 (31 + (0 << 8)),
	TYPE_LSOBJECT (20 + (0 << 8)),
	TYPE_ICON (6 + (0 << 8)),
	TYPE_VIEW_FORMAT (5 + (0 << 8)),
	TYPE_SCHED_LIST (22 + (0 << 8)),
	TYPE_VIEWMAP_DATASET (18 + (0 << 8)),
	TYPE_SEAL (9 + (0 << 8)),
	TYPE_MIME_PART (25 + (0 << 8)),
	TYPE_SEALDATA (10 + (0 << 8)),
	TYPE_NOTELINK_LIST (7 + (0 << 8)),
	TYPE_COLLATION (2 + (0 << 8)),
	TYPE_RFC822_TEXT (2 + (5 << 8)),
	/** Richtext item */
	TYPE_COMPOSITE (1 + (0 << 8)),
	TYPE_OBJECT (3 + (0 << 8)),
	TYPE_HTML (21 + (0 << 8)),
	TYPE_ASSISTANT_INFO (17 + (0 << 8)),
	TYPE_HIGHLIGHTS (12 + (0 << 8)),
	TYPE_NOTEREF_LIST (4 + (0 << 8)),
	TYPE_QUERY (15 + (0 << 8)),
	TYPE_USERDATA (14 + (0 << 8)),
	TYPE_INVALID_OR_UNKNOWN (0 + (0 << 8)),
	TYPE_SEAL_LIST (11 + (0 << 8)),
	TYPE_CALENDAR_FORMAT (24 + (0 << 8));
	
	private short m_dataType;
	
	ItemDataType(int dataType) {
		m_dataType = (short)dataType;
	}
	
	@Override
	public long getLongValue() {
		return m_dataType;
	}
	
	@Override
	public Short getValue() {
		return m_dataType;
	}
}