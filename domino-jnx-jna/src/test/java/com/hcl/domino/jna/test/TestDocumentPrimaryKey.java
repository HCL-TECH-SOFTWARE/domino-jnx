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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.data.JNADatabase;

@SuppressWarnings("nls")
public class TestDocumentPrimaryKey extends AbstractJNARuntimeTest {
	//switch to change the used lookup mode
	private final String propEnforceRemoteNamedObjectSearch = JNADatabase.class.getName()+".namedobjects.enforceremote";


	@Test
	public void testArbitraryUsername() throws Exception {
		JNADominoClient client = (JNADominoClient) getClient();
		
		withTempDb(dbFakenames -> {
			String pkCategory = "testcategory";
			String pkObjectId = "configuration_"+System.currentTimeMillis();
	
			Document note = dbFakenames.createDocument();
			//this adds a $name item to the note with a format similar to profile documents,
			//e.g. $app_012testcategory_myobjectid (where 012 is the length of the following
			//category name)
			//NSF has an internal table (named object table) that will automatically index
			//normal notes and ghost notes (special notes created via NotesDatabase.createGhostNote())
			//that do not show up in views/searches) with a $name item. We can use efficient
			//lookup methods to find these notes without the need to create lookup views
			note.setPrimaryKey(pkCategory, pkObjectId);
			note.replaceItemValue("Type", "Person");
			note.replaceItemValue("Lastname", "1. Primary Key test");
			note.replaceItemValue("Firstname", "1. Primary Key test");
			note.save();
	
			{
				Document noteFoundViaPK = dbFakenames.getDocumentByPrimaryKey(pkCategory, pkObjectId).get();
				assertNotNull(noteFoundViaPK, "note could be found via primary key");
				assertEquals(note.getUNID(), noteFoundViaPK.getUNID(), "note found via primary key has correct UNID");
			}
			{
				Map<String,Map<String,Integer>> notesByCategoryAndPK = dbFakenames.getAllDocumentsByPrimaryKey();
				assertTrue(notesByCategoryAndPK.containsKey(pkCategory), "object id in map");
				
				Map<String,Integer> notesByPK = notesByCategoryAndPK.get(pkCategory);
				assertTrue(notesByPK.containsKey(pkObjectId), "object id in map");
				assertTrue(notesByPK.get(pkObjectId) == note.getNoteID(), "value has the right note id");
			}
			{
				Map<String,Integer> notesByPK = dbFakenames.getAllDocumentsByPrimaryKey(pkCategory);
				assertTrue(notesByPK.containsKey(pkObjectId), "object id in map");
				assertTrue(notesByPK.get(pkObjectId) == note.getNoteID(), "value has the right note id");
			}
	
			// switch to remote mode, using NSFSearchExtended3
			client.setCustomValue(propEnforceRemoteNamedObjectSearch, Boolean.TRUE);
	
			{
				Document noteFoundViaPK = dbFakenames.getDocumentByPrimaryKey(pkCategory, pkObjectId).get();
				assertNotNull(noteFoundViaPK, "Note could be found via primary key");
				assertEquals(note.getUNID(), noteFoundViaPK.getUNID(), "note found via primary key has correct UNID");
			}
			{
				Map<String,Map<String,Integer>> notesByCategoryAndPK = dbFakenames.getAllDocumentsByPrimaryKey();
				assertTrue(notesByCategoryAndPK.containsKey(pkCategory), "object id in map");
				
				Map<String,Integer> notesByPK = notesByCategoryAndPK.get(pkCategory);
				assertTrue(notesByPK.containsKey(pkObjectId), "object id in map");
				assertTrue(notesByPK.get(pkObjectId) == note.getNoteID(), "value has the right note id");
			}
			{
				Map<String,Integer> notesByPK = dbFakenames.getAllDocumentsByPrimaryKey(pkCategory);
				assertTrue(notesByPK.containsKey(pkObjectId), "object id in map");
				assertTrue(notesByPK.get(pkObjectId) == note.getNoteID(), "value has the right note id");
			}
	
			//now delete the note and see if the index has been updated
			note.delete();
	
			// switch to local mode
			client.setCustomValue(propEnforceRemoteNamedObjectSearch, Boolean.FALSE);
	
			{
				Document noteFoundViaPK = dbFakenames.getDocumentByPrimaryKey(pkCategory, pkObjectId).orElse(null);
				assertNull(noteFoundViaPK, "deleted note could not be found via primary key");
			}
			{
				Map<String,Integer> notesByPK = dbFakenames.getAllDocumentsByPrimaryKey(pkCategory);
				assertTrue(!notesByPK.containsKey(pkObjectId), "deleted object not in map");
			}
	
			// switch to remote mode, using NSFSearchExtended3
			client.setCustomValue(propEnforceRemoteNamedObjectSearch, Boolean.TRUE);
			
			{
				Document noteFoundViaPK = dbFakenames.getDocumentByPrimaryKey(pkCategory, pkObjectId).orElse(null);
				assertNull(noteFoundViaPK, "deleted note could not be found via primary key");
			}
			{
				Map<String,Integer> notesByPK = dbFakenames.getAllDocumentsByPrimaryKey(pkCategory);
				assertTrue(!notesByPK.containsKey(pkObjectId), "deleted object not in map");
			}
		});
	}
}
