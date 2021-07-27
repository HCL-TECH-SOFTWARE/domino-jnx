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

import java.util.NavigableMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.ItemDataType;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

/**
 * Tests if the item definition table can be read and is not empty
 *
 * @author Karsten Lehmann
 */
@SuppressWarnings("nls")
public class TestItemDefinitionTable extends AbstractNotesRuntimeTest {

  @Test
  public void testItemDefTable() throws Exception {
    this.withTempDb(tempDb -> {
      final NavigableMap<String, ItemDataType> itemDefTable = tempDb.getItemDefinitionTable();

      final String[] itemNamesToCheck = {
          "$DesignVersion",
          "$designversion",
          "$flags",
          "$formula",
          "$formulaclass",
          "$updatedby"
      };

      for (final String currItemName : itemNamesToCheck) {
        Assertions.assertTrue(itemDefTable.containsKey(currItemName), "item exists in table: " + currItemName);
      }
    });

  }
}
