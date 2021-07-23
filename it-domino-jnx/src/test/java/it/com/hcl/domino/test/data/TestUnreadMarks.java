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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.IDTable;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestUnreadMarks extends AbstractNotesRuntimeTest {

	@Test
	public void testUnreadMarks() throws Exception {
		withTempDb((database) -> {
			String userName = "CN=John Doe/O=ACME";

			Document doc = database.createDocument();
			doc.save();
			
			int noteId = doc.getNoteID();
			System.out.println("Document note id: "+noteId);
			
			assertTrue(doc.isUnread(userName));
			assertTrue(database.isDocumentUnread(userName, noteId));
			
			
			{
				IDTable unreadTable = database.getUnreadDocumentTable(userName, true, true).get();
				assertTrue(unreadTable.contains(doc.getNoteID()));
			}

			//mark doc read
			database.updateUnreadDocumentTable(userName, Collections.singleton(doc.getNoteID()), null);

			{
				assertFalse(database.isDocumentUnread(userName, noteId));
				assertFalse(doc.isUnread(userName));
				
				IDTable unreadTable = database.getUnreadDocumentTable(userName, true, true).get();
				assertFalse(unreadTable.contains(doc.getNoteID()));
			}

			//mark doc unread again
			database.updateUnreadDocumentTable(userName, null, Collections.singleton(doc.getNoteID()));

			assertTrue(database.isDocumentUnread(userName, noteId));
			assertTrue(doc.isUnread(userName));


			//TODO the following code fails; check if MARK_READ is working at all in local DBs
			
			//and now change the unread state when opening the doc
//			Document doc2 = database.getDocumentById(noteId, EnumSet.of(OpenDocumentMode.MARK_READ));
//			doc2.save();
//			
//			assertFalse(doc2.isUnread(userName));
//			assertFalse(doc.isUnread(userName));
//			assertFalse(database.isDocumentUnread(userName, noteId));

		});
	}
}
