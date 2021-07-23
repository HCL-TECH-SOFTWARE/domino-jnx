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

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.IDTable;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestUnreadMarks extends AbstractNotesRuntimeTest {

  @Test
  public void testUnreadMarks() throws Exception {
    this.withTempDb(database -> {
      final String userName = "CN=John Doe/O=ACME";

      final Document doc = database.createDocument();
      doc.save();

      final int noteId = doc.getNoteID();
      System.out.println("Document note id: " + noteId);

      Assertions.assertTrue(doc.isUnread(userName));
      Assertions.assertTrue(database.isDocumentUnread(userName, noteId));

      {
        final IDTable unreadTable = database.getUnreadDocumentTable(userName, true, true).get();
        Assertions.assertTrue(unreadTable.contains(doc.getNoteID()));
      }

      // mark doc read
      database.updateUnreadDocumentTable(userName, Collections.singleton(doc.getNoteID()), null);

      {
        Assertions.assertFalse(database.isDocumentUnread(userName, noteId));
        Assertions.assertFalse(doc.isUnread(userName));

        final IDTable unreadTable = database.getUnreadDocumentTable(userName, true, true).get();
        Assertions.assertFalse(unreadTable.contains(doc.getNoteID()));
      }

      // mark doc unread again
      database.updateUnreadDocumentTable(userName, null, Collections.singleton(doc.getNoteID()));

      Assertions.assertTrue(database.isDocumentUnread(userName, noteId));
      Assertions.assertTrue(doc.isUnread(userName));

      // TODO the following code fails; check if MARK_READ is working at all in local
      // DBs

      // and now change the unread state when opening the doc
      // Document doc2 = database.getDocumentById(noteId,
      // EnumSet.of(OpenDocumentMode.MARK_READ));
      // doc2.save();
      //
      // assertFalse(doc2.isUnread(userName));
      // assertFalse(doc.isUnread(userName));
      // assertFalse(database.isDocumentUnread(userName, noteId));

    });
  }
}
