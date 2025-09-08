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
package com.hcl.domino.commons.test.richtext.records;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.OriginatorID;

public class TestMemoryStructureProxy {

  @Test
  public void testColorValueRoundTrip() {
    ColorValue color = MemoryStructureUtil.newStructure(ColorValue.class, 0);
    color.setRed((short)255);
    assertEquals(255, color.getRed());
  }
  
  // Run this multiple times to ensure that method handles properly
  //   apply to new instances
  @ParameterizedTest
  @ValueSource(strings = { "12345678901234567890123456789012", "12345678901234567890123456789013" })
  public void testDefaultMethods(String unid) {
    OriginatorID oid = MemoryStructureUtil.newStructure(OriginatorID.class, 0);
    oid.setUNID(unid);
    assertEquals(unid, oid.getUNID());
  }

}
