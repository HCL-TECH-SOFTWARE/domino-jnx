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

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.data.JNADatabase;

@SuppressWarnings("nls")
public class TestDocumentPrimaryKey extends AbstractJNARuntimeTest {
  // switch to change the used lookup mode
  private final String propEnforceRemoteNamedObjectSearch = JNADatabase.class.getName() + ".namedobjects.enforceremote";

  @Test
  public void testArbitraryUsername() throws Exception {
    final JNADominoClient client = (JNADominoClient) this.getClient();

    this.withTempDb(dbFakenames -> {
      final String pkCategory = "testcategory";
      final String pkObjectId = "configuration_" + System.currentTimeMillis();

      final Document note = dbFakenames.createDocument();
      // this adds a $name item to the note with a format similar to profile
      // documents,
      // e.g. $app_012testcategory_myobjectid (where 012 is the length of the
      // following
      // category name)
      // NSF has an internal table (named object table) that will automatically index
      // normal notes and ghost notes (special notes created via
      // NotesDatabase.createGhostNote())
      // that do not show up in views/searches) with a $name item. We can use
      // efficient
      // lookup methods to find these notes without the need to create lookup views
      note.setPrimaryKey(pkCategory, pkObjectId);
      note.replaceItemValue("Type", "Person");
      note.replaceItemValue("Lastname", "1. Primary Key test");
      note.replaceItemValue("Firstname", "1. Primary Key test");
      note.save();

      {
        final Document noteFoundViaPK = dbFakenames.getDocumentByPrimaryKey(pkCategory, pkObjectId).get();
        Assertions.assertNotNull(noteFoundViaPK, "note could be found via primary key");
        Assertions.assertEquals(note.getUNID(), noteFoundViaPK.getUNID(), "note found via primary key has correct UNID");
      }
      {
        final Map<String, Map<String, Integer>> notesByCategoryAndPK = dbFakenames.getAllDocumentsByPrimaryKey();
        Assertions.assertTrue(notesByCategoryAndPK.containsKey(pkCategory), "object id in map");

        final Map<String, Integer> notesByPK = notesByCategoryAndPK.get(pkCategory);
        Assertions.assertTrue(notesByPK.containsKey(pkObjectId), "object id in map");
        Assertions.assertTrue(notesByPK.get(pkObjectId) == note.getNoteID(), "value has the right note id");
      }
      {
        final Map<String, Integer> notesByPK = dbFakenames.getAllDocumentsByPrimaryKey(pkCategory);
        Assertions.assertTrue(notesByPK.containsKey(pkObjectId), "object id in map");
        Assertions.assertTrue(notesByPK.get(pkObjectId) == note.getNoteID(), "value has the right note id");
      }

      // switch to remote mode, using NSFSearchExtended3
      client.setCustomValue(this.propEnforceRemoteNamedObjectSearch, Boolean.TRUE);

      {
        final Document noteFoundViaPK = dbFakenames.getDocumentByPrimaryKey(pkCategory, pkObjectId).get();
        Assertions.assertNotNull(noteFoundViaPK, "Note could be found via primary key");
        Assertions.assertEquals(note.getUNID(), noteFoundViaPK.getUNID(), "note found via primary key has correct UNID");
      }
      {
        final Map<String, Map<String, Integer>> notesByCategoryAndPK = dbFakenames.getAllDocumentsByPrimaryKey();
        Assertions.assertTrue(notesByCategoryAndPK.containsKey(pkCategory), "object id in map");

        final Map<String, Integer> notesByPK = notesByCategoryAndPK.get(pkCategory);
        Assertions.assertTrue(notesByPK.containsKey(pkObjectId), "object id in map");
        Assertions.assertTrue(notesByPK.get(pkObjectId) == note.getNoteID(), "value has the right note id");
      }
      {
        final Map<String, Integer> notesByPK = dbFakenames.getAllDocumentsByPrimaryKey(pkCategory);
        Assertions.assertTrue(notesByPK.containsKey(pkObjectId), "object id in map");
        Assertions.assertTrue(notesByPK.get(pkObjectId) == note.getNoteID(), "value has the right note id");
      }

      // now delete the note and see if the index has been updated
      note.delete();

      // switch to local mode
      client.setCustomValue(this.propEnforceRemoteNamedObjectSearch, Boolean.FALSE);

      {
        final Document noteFoundViaPK = dbFakenames.getDocumentByPrimaryKey(pkCategory, pkObjectId).orElse(null);
        Assertions.assertNull(noteFoundViaPK, "deleted note could not be found via primary key");
      }
      {
        final Map<String, Integer> notesByPK = dbFakenames.getAllDocumentsByPrimaryKey(pkCategory);
        Assertions.assertTrue(!notesByPK.containsKey(pkObjectId), "deleted object not in map");
      }

      // switch to remote mode, using NSFSearchExtended3
      client.setCustomValue(this.propEnforceRemoteNamedObjectSearch, Boolean.TRUE);

      {
        final Document noteFoundViaPK = dbFakenames.getDocumentByPrimaryKey(pkCategory, pkObjectId).orElse(null);
        Assertions.assertNull(noteFoundViaPK, "deleted note could not be found via primary key");
      }
      {
        final Map<String, Integer> notesByPK = dbFakenames.getAllDocumentsByPrimaryKey(pkCategory);
        Assertions.assertTrue(!notesByPK.containsKey(pkObjectId), "deleted object not in map");
      }
    });
  }
}
