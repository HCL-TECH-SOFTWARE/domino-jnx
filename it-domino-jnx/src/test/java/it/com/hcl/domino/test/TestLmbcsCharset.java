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
package it.com.hcl.domino.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("nls")
public class TestLmbcsCharset {
	
	@Test
	public void testForNameLMBCS() {
		Charset.forName("LMBCS");
	}
	@Test
	public void testForNameLMBCSNative() {
		Charset.forName("LMBCS-native");
	}
	
	@ParameterizedTest
	@ValueSource(strings={"Hello","EkranAlıntısı1.JPG"})
	public void testRoundTrip(String expected) {
		Charset charset = Charset.forName("LMBCS-native");
		byte[] encoded = expected.getBytes(charset);
		String decoded = new String(encoded, charset);
		assertEquals(expected, decoded);
	}

}
