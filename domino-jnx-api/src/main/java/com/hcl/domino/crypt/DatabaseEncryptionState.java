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
package com.hcl.domino.crypt;

import com.hcl.domino.misc.INumberEnum;

/**
 * @since 1.0.18
 */
public enum DatabaseEncryptionState implements INumberEnum<Integer> {
	UNENCRYPTED(0), ENCRYPTED(1), PENDING_ENCRYPTION(2);
	
	private final int value;
	DatabaseEncryptionState(int value) {
		this.value = value;
	}
	
	@Override
	public Integer getValue() {
		return value;
	}
	@Override
	public long getLongValue() {
		return value;
	}
}
