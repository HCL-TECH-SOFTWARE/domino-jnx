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

public enum DateTimeFlag2 implements INumberEnum<Integer> {
	/** Use the 4.X format structure instead of this 5.X format structure */
	USE_TFMT(RichTextConstants.DT_USE_TFMT)
	;
	private final int value;
	DateTimeFlag2(int value) { this.value = value; }
	
	@Override
	public long getLongValue() {
		return value;
	}
	@Override
	public Integer getValue() {
		return value;
	}
}