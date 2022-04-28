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
package com.hcl.domino.jna.test;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.hcl.domino.jna.internal.NotesStringUtils;

@SuppressWarnings("nls")
public class TestLmbcsCharsetProvider extends AbstractJNARuntimeTest {
  @ParameterizedTest
  @ValueSource(strings = { "Hello", "EkranAlıntısı1.JPG" })
  public void testAscii(final String expected) {
    final Charset charset = Charset.forName("LMBCS-native");
    final byte[] encoded = expected.getBytes(charset);
    Assertions.assertEquals(expected, NotesStringUtils.fromLMBCS(encoded));
    final String decoded = new String(encoded, charset);
    Assertions.assertEquals(expected, decoded);
  }
}
