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

public enum TimeShowFormat implements INumberEnum<Byte> {
	H(RichTextConstants.DT_TSHOW_H),
	HM(RichTextConstants.DT_TSHOW_HM),
	HMS(RichTextConstants.DT_TSHOW_HMS),
	ALL(RichTextConstants.DT_TSHOW_ALL),
	;
	private final byte value;
	TimeShowFormat(byte value) { this.value = value; }
	
	@Override
	public long getLongValue() {
		return value;
	}
	@Override
	public Byte getValue() {
		return value;
	}
}