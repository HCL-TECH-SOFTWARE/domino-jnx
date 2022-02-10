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
package com.hcl.domino.commons.test.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.hcl.domino.BuildVersionInfo;
import com.hcl.domino.commons.data.BuildVersionInfoImpl;

public class TestBuildVersionInfo {
  public static class BuildsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return Stream.of(
        Arguments.of(
          new BuildVersionInfoImpl(12, 0, 1, 0, 0, 0, 0),
          true,
          12, 0, 0, 0, 0, 0
        ),
        Arguments.of(
          new BuildVersionInfoImpl(12, 0, 0, 0, 0, 0, 0),
          false,
          12, 0, 1, 0, 0, 0
        ),
        Arguments.of(
          new BuildVersionInfoImpl(11, 2, 0, 0, 0, 0, 0),
          false,
          12, 0, 1, 0, 0, 0
        ),
        Arguments.of(
          new BuildVersionInfoImpl(12, 2, 0, 0, 0, 0, 0),
          true,
          12, 0, 1, 0, 0, 0
        ),
        Arguments.of(
          new BuildVersionInfoImpl(12, 2, 0, 0, 0, 0, 0),
          false,
          12, 2, 1, 0, 0, 0
        ),
        Arguments.of(
          new BuildVersionInfoImpl(12, 2, 0, 0, 0, 0, 0),
          true,
          12, 2, 0, 0, 0, 0
        ),
        Arguments.of(
          new BuildVersionInfoImpl(12, 2, 0, 0, 0, 0, 345),
          true,
          12, 2, 0, 0, 0, 0
        )
      );
    }
    
  }

  @ParameterizedTest
  @ArgumentsSource(BuildsProvider.class)
  public void testBuildVersionInfo(BuildVersionInfo info, boolean expected, int majorVersion, int minorVersion, int qmrNumber, int qmuNumber, int hotfixNumber) {
    assertEquals(expected, info.isAtLeast(majorVersion, minorVersion, qmrNumber, qmuNumber, hotfixNumber));
  }

}
