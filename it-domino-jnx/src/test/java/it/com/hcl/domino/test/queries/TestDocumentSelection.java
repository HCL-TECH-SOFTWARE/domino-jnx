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
package it.com.hcl.domino.test.queries;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.IDTable;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDocumentSelection extends AbstractNotesRuntimeTest {
	
	@Test
	public void testDocumentSelection() throws Exception {
		withResourceDxl("/dxl/testHtmlRendering", (db) -> {
			IDTable designElementNoteIds = db
					.createDocumentSelection()
					.selectAllDesignElements()
					.build();
			
			assertFalse(designElementNoteIds.isEmpty());
			
			boolean hasForm = false;
			boolean hasView = false;
			
			for (Integer currNoteId : designElementNoteIds) {
//				System.out.println("Opening doc with noteid "+currNoteId);
				
				Document currDoc = db.getDocumentById(currNoteId).get();
				Set<DocumentClass> docClass = currDoc.getDocumentClass();
				if (docClass.contains(DocumentClass.FORM)) {
					hasForm = true;
				}
				else if (docClass.contains(DocumentClass.VIEW)) {
					hasView = true;
				}
				
//				System.out.println("Class="+currDoc.getDocumentClass()+"\t$TITLE="+currDoc.get("$TITLE", String.class, ""));
			}
			
			assertTrue(hasForm);
			assertTrue(hasView);
		});
	}
	
}
