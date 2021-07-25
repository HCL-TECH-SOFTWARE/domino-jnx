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
package it.com.hcl.domino.test.data;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.DominoCollection;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestColumnValueConversion extends AbstractNotesRuntimeTest {

  // Now rounded to hundredths of a second
  private final OffsetDateTime now = OffsetDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis() / 10 * 10),
      ZoneId.systemDefault());

  @Test
  public void testColumnValues() throws Exception {
    this.withResourceDxl("/dxl/testViewDataConversion", database -> {
      database.createDocument()
          .replaceItemValue("Form", "Conversion Doc")
          .replaceItemValue("StringField", "hello")
          .replaceItemValue("NumberField", 3.1)
          .replaceItemValue("DateField", this.now)
          .save();

      final DominoCollection coll = database.openCollection("Conversion Docs").get();
      coll.refresh();
      final CollectionEntry entry = coll.query()
          .readColumnValues()
          .firstEntry()
          .orElse(null);
      Assertions.assertNotEquals(null, entry);

      Assertions.assertEquals("hello", entry.get("StringField", String.class, null));
      Assertions.assertEquals(3.1, entry.get("NumberField", Double.class, null));
      Assertions.assertEquals(3, entry.get("NumberField", int.class, null));
      Assertions.assertArrayEquals(new double[] { 3.1 }, entry.get("NumberField", double[].class, null));
      Assertions.assertEquals(this.now.toInstant(), entry.get("DateField", OffsetDateTime.class, null).toInstant());
    });
  }

}
