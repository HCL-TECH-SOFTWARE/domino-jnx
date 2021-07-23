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
package com.hcl.domino.jna.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.Item.ItemFlag;

@SuppressWarnings("nls")
public class TestReaderFieldsDocument extends AbstractJNARuntimeTest {

  private boolean allItemsMatching(final List<Item> expected, final List<Item> actual) {
    if (expected.size() != actual.size()) {
      return false;
    }

    for (int i = 0; i < actual.size(); i++) {
      if (!actual.get(i).getName().equals(expected.get(i).getName())) {
        return false;
      }
    }
    return true;
  }

  @Test
  public void testGetAllReaderFields() throws IOException {
    final DominoClient client = this.getClient();

    final Database dbFakenames = client.openDatabase("", "log.nsf");
    final Document doc = dbFakenames.createDocument();

    {
      final ArrayList<Item> expectedFields = new ArrayList<>();

      for (int i = 1; i <= 3; i++) {
        doc.appendItemValue("test_field" + i, "just-a-field" + i);
        doc.appendItemValue("rf_test_field" + i, new HashSet<>(Arrays.asList(ItemFlag.READERS)), "anonymous" + i);

        expectedFields.add(doc.getFirstItem("rf_test_field" + i).get());
      }

      Assertions.assertTrue(this.allItemsMatching(expectedFields, doc.getReadersFields()), "Reading all reader-fields ok");
    }
  }

  @Test
  public void testGetAllReaderFieldsEmpty() throws IOException {
    final DominoClient client = this.getClient();

    final Database dbFakenames = client.openDatabase("", "log.nsf");
    final Document doc = dbFakenames.createDocument();

    {
      for (int i = 1; i <= 3; i++) {
        doc.appendItemValue("test_field" + i, "just-a-field" + i);
      }

      Assertions.assertTrue(doc.getReadersFields().isEmpty(), "Reading empty list of reader-fields ok");
    }
  }

  @Test
  public void testHasNoReaderFields() throws IOException {
    final DominoClient client = this.getClient();

    final Database dbFakenames = client.openDatabase("", "log.nsf");
    final Document doc = dbFakenames.createDocument();

    {
      doc.appendItemValue("test_field", "just-a-field");

      Assertions.assertEquals(false, doc.hasReadersField(), "Testing for no reader-field ok");
    }
  }

  @Test
  public void testHasReaderFields() throws IOException {
    final DominoClient client = this.getClient();

    final Database dbFakenames = client.openDatabase("", "log.nsf");
    final Document doc = dbFakenames.createDocument();

    {
      doc.appendItemValue("rf_test_field", new HashSet<>(Arrays.asList(ItemFlag.READERS)), "anonymous");

      Assertions.assertEquals(true, doc.hasReadersField(), "Testing for reader-field ok");
    }
  }
}
