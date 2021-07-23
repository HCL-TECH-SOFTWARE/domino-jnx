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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.jna.internal.NotesStringUtils;
import com.sun.jna.Memory;

@SuppressWarnings("nls")
public class TestNotesStringUtils {

  @Test
  public void testStringListRoundTrip() {
    final List<String> strings = Arrays.asList("foo", "bar", "baz");
    final Memory mem = NotesStringUtils.toLMBCS(strings);
    final List<String> out = NotesStringUtils.fromLMBCSStringList(mem, strings.size());
    Assertions.assertEquals(strings, out);
  }

}
