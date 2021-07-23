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
package com.hcl.domino.runtime;

import com.hcl.domino.misc.INumberEnum;

/**
 * Enumeration of the available ways to run NSD.
 */
public enum NSDMode implements INumberEnum<Short>{
	RUN_ALL((short) 0x1000),
	RUN_CLEANUPSCRIPT_ONLY((short) 0x1),
	RUN_NSD_ONLY((short) 0x2),
	DONT_RUN_ANYTHING((short) 0x4),
	SHUTDOWN_HANG((short) 0x8),
	PANIC_DIRECT((short) 0x10),
	RUN_QOS_NSD((short) 0x20),
	NSD_AUTOMONITOR((short) 0x40);

	private short m_value;
	
	NSDMode(short value) {
		m_value = value;
	}
	
	@Override
	public Short getValue() {
		return m_value;
	}

	@Override
	public long getLongValue() {
		return m_value & 0xffff;
	}
	
	
}
