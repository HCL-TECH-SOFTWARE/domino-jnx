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
package com.hcl.domino.jna.test.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.hcl.domino.commons.util.DominoUtils;

@SuppressWarnings("nls")
public class TestDominoUtils {
	@Test
	public void testReplicaIds() {
		assertTrue(DominoUtils.isReplicaId("12345678:12345678"));
		assertTrue(DominoUtils.isReplicaId("1234567812345678"));
		assertTrue(DominoUtils.isReplicaId("12345678:abcdefab"));
		assertTrue(DominoUtils.isReplicaId("12345678ABCDEFAB"));
		assertFalse(DominoUtils.isReplicaId("12345678x12345678"));
		assertFalse(DominoUtils.isReplicaId("12345678:123456789"));
		assertFalse(DominoUtils.isReplicaId("12345678123456789"));
		assertFalse(DominoUtils.isReplicaId("12345678:1234567"));
		assertFalse(DominoUtils.isReplicaId("123456781234567"));
		assertFalse(DominoUtils.isReplicaId(null));
		assertFalse(DominoUtils.isReplicaId(""));
		assertFalse(DominoUtils.isReplicaId("foo"));
		assertFalse(DominoUtils.isReplicaId("!!"));
		assertFalse(DominoUtils.isReplicaId("foo!!12345678:12345678"));
	}
}
