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
package com.hcl.domino.design.agent;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public enum AgentInterval implements INumberEnum<Short> {
	/** Unknown  */
	NONE(RichTextConstants.ASSISTINTERVAL_TYPE_NONE),
	MINUTES(RichTextConstants.ASSISTINTERVAL_TYPE_MINUTES),
	DAYS(RichTextConstants.ASSISTINTERVAL_TYPE_DAYS),
	WEEK(RichTextConstants.ASSISTINTERVAL_TYPE_WEEK),
	MONTH(RichTextConstants.ASSISTINTERVAL_TYPE_MONTH),
	EVENT(RichTextConstants.ASSISTINTERVAL_TYPE_EVENT),
	;
	
	private final short value;
	AgentInterval(short value) { this.value = (short)value; }
	@Override
	public Short getValue() {
		return value;
	}
	@Override
	public long getLongValue() {
		return value;
	}
}