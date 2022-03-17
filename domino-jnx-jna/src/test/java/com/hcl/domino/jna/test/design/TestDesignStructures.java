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
package com.hcl.domino.jna.test.design;

import java.nio.ByteBuffer;
import java.util.EnumSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.design.format.ViewColumnFormat;
import com.hcl.domino.design.format.ViewColumnFormat.ReadingOrder;
import com.hcl.domino.jna.test.AbstractJNARuntimeTest;
import com.hcl.domino.misc.ViewFormatConstants;

@SuppressWarnings("nls")
public class TestDesignStructures extends AbstractJNARuntimeTest {
  @Test
  public void testViewColumnFormat() {
    ByteBuffer data;
    {
      final ViewColumnFormat format = MemoryStructureUtil.newStructure(ViewColumnFormat.class, 0);
      format.setSignature(ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE);
      format.setFlags(EnumSet.of(ViewColumnFormat.Flag.Hidden));
      format.setFlags2(EnumSet.of(ViewColumnFormat.Flag2.ShowValuesAsLinks));
      format.setAlignment(ViewColumnFormat.Alignment.CENTER);
      format.setTotalType(ViewColumnFormat.StatType.AVG_PER_ENTRY);
      format.setHeaderAlignment(ViewColumnFormat.Alignment.RIGHT);
      format.setHeaderReadingOrder(ReadingOrder.LTR);
      format.setReadingOrder(ReadingOrder.LTR);

      Assertions.assertEquals(EnumSet.of(ViewColumnFormat.Flag2.ShowValuesAsLinks), format.getFlags2());
      Assertions.assertEquals(ViewColumnFormat.Alignment.CENTER, format.getAlignment());
      Assertions.assertEquals(ViewColumnFormat.StatType.AVG_PER_ENTRY, format.getTotalType());
      Assertions.assertEquals(ViewColumnFormat.Alignment.RIGHT, format.getHeaderAlignment());
      Assertions.assertEquals(ViewColumnFormat.ReadingOrder.LTR, format.getHeaderReadingOrder());
      Assertions.assertEquals(ViewColumnFormat.ReadingOrder.LTR, format.getReadingOrder());

      format.setFlags2(EnumSet.of(ViewColumnFormat.Flag2.SecondResortPermute, ViewColumnFormat.Flag2.SecondResortCategorized));
      format.setAlignment(ViewColumnFormat.Alignment.RIGHT);
      format.setTotalType(ViewColumnFormat.StatType.PCT_PARENT);
      format.setHeaderAlignment(ViewColumnFormat.Alignment.CENTER);
      format.setReadingOrder(ViewColumnFormat.ReadingOrder.RTL);
      format.setTitle("hello");

      data = format.getData();
    }
    {
      final ViewColumnFormat format = MemoryStructureUtil.forStructure(ViewColumnFormat.class, () -> data);
      Assertions.assertEquals(ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE, format.getSignature());
      Assertions.assertEquals(EnumSet.of(ViewColumnFormat.Flag.Hidden), format.getFlags());
      Assertions.assertEquals(
          EnumSet.of(ViewColumnFormat.Flag2.SecondResortPermute, ViewColumnFormat.Flag2.SecondResortCategorized),
          format.getFlags2());
      Assertions.assertEquals(ViewColumnFormat.Alignment.RIGHT, format.getAlignment());
      Assertions.assertEquals(ViewColumnFormat.StatType.PCT_PARENT, format.getTotalType());
      Assertions.assertEquals(ViewColumnFormat.Alignment.CENTER, format.getHeaderAlignment());
      Assertions.assertEquals(ViewColumnFormat.ReadingOrder.RTL, format.getReadingOrder());
      Assertions.assertEquals("hello", format.getTitle());
    }
  }
}
