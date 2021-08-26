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
package com.hcl.domino.test.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hcl.domino.data.StandardColors;

public class TestStandardColors {

  @Test
  public void testStandardColorsIndexes() {
    // The enum values of StandardColors are intended to be the integers 0 - 239
    
    // values() should return the values in declaration order
    StandardColors[] colors = StandardColors.values();
    for(int i = 0; i < colors.length; i++) {
      int fi = i;
      assertEquals(i, Byte.toUnsignedInt(colors[i].getValue()), () -> colors[fi] + " should have value " + fi);
    }
  }

}
