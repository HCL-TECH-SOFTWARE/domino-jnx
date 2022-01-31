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
package com.hcl.domino.commons.test.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.hcl.domino.commons.util.DominoUtils;

public class TestDominoUtils {
  
  private static class ArrayProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return Stream.of(
        new Object[] { new byte[] { 1, 2, 3 }, new byte[] { 4, 5, 6 }, new byte[] { 1, 2, 3 } },
        new Object[] { new byte[] { 1, 2, 3, 4 }, new byte[] { 4, 5, 6 }, new byte[] { 1, 2, 3 } },
        new Object[] { new byte[] { 1, 2 }, new byte[] { 4, 5, 6 }, new byte[] { 1, 2, 0 } }
      ).map(Arguments::of);
    }
  }

  @ParameterizedTest
  @ArgumentsSource(ArrayProvider.class)
  public void testOverwriteArray(byte[] source, byte[] dest, byte[] expected) {
    DominoUtils.overwriteArray(source, dest);
    assertArrayEquals(expected, dest);
  }
}
