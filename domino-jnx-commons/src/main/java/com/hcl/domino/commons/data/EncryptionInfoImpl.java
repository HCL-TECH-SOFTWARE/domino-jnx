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
package com.hcl.domino.commons.data;

import java.util.Optional;

import com.hcl.domino.crypt.DatabaseEncryptionState;
import com.hcl.domino.crypt.EncryptionStrength;
import com.hcl.domino.data.Database.EncryptionInfo;

/**
 * @since 1.0.18
 */
public class EncryptionInfoImpl implements EncryptionInfo {
	private final Optional<DatabaseEncryptionState> state;
	private final Optional<EncryptionStrength> strength;
	
	public EncryptionInfoImpl(Optional<DatabaseEncryptionState> state, Optional<EncryptionStrength> strength) {
		this.state = state;
		this.strength = strength;
	}

	@Override
	public Optional<DatabaseEncryptionState> getState() {
		return state;
	}

	@Override
	public Optional<EncryptionStrength> getStrength() {
		return strength;
	}

	@Override
	public String toString() {
		return String.format("EncryptionInfoImpl [state=%s, strength=%s]", state, strength); //$NON-NLS-1$
	}
}