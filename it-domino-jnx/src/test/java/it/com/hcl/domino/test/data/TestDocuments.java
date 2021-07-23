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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.Database.OpenDocumentMode;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.exception.LotusScriptCompilationException;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDocuments extends AbstractNotesRuntimeTest {

	@Test
	public void testNamesNsfNewNote() {
		DominoClient client = getClient();
		Database names = client.openDatabase("names.nsf"); //$NON-NLS-1$
		
		Document note = names.createDocument();
		assertNotEquals(null, note);
		
		note.replaceItemValue("foo", "bar");
		assertEquals("bar", note.get("foo", String.class, ""));
		
		assertEquals("", note.get("does-not-exist", String.class, ""));
		
		note.setUNID("12345678901234567890123456789012");
		assertEquals("12345678901234567890123456789012", note.getUNID());
	}
	
	@Test
	public void testNamesNsfAclNote() {
		DominoClient client = getClient();
		Database names = client.openDatabase("names.nsf"); //$NON-NLS-1$
		
		Document acl = names.getDocumentById(0xFFFF0000 | 0x0040).get(); // ACL note by Special ID
		assertNotEquals(null, acl);
		
		Set<DocumentClass> classes = acl.getDocumentClass();
		assertNotEquals(null, classes);
		assertTrue(classes.contains(DocumentClass.ACL));
		assertFalse(classes.contains(DocumentClass.DATA));
	}
	
	@Test
	public void testDocumentInvalidCreationDate() throws Exception {
		withResourceDb("/nsf/invalidtimedate.nsf", database -> {
			CollectionEntry entry = database.queryDocuments()
				.computeValues("Created", "@Created")
				.collectEntries()
				.get(0);
			
			DominoDateTime dt = entry.get("Created", DominoDateTime.class, null);
			assertNotNull(dt);
			assertFalse(dt.isValid());
			
			Document doc = database.queryFormula(" Subject='**** Calendaring and Scheduling Meta Data Doc - DO NOT MODIFY ****' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
				.getDocuments()
				.findFirst()
				.get();
			dt = doc.getCreated();
			assertNotNull(dt);
			assertFalse(dt.isValid());
		});
	}
	
	@Test
	public void testAppendItemValueVector() throws Exception {
		withTempDb(database -> {
			Document testdoc = database.createDocument();
			Vector<String> someobject = new Vector<String>();
			someobject.add("cat1");
			someobject.add("cat2");
			testdoc.appendItemValue("categories", someobject);
			assertEquals(someobject, testdoc.getAsList("categories", String.class, null));
		});
	}
	
	@Test
	public void testSetGetBasicItemTypes() throws Exception {
		withTempDb(database -> {
			Document testdoc = database.createDocument();
			
			{
				String str = "testval";
				testdoc.replaceItemValue("strvalue", str);
				assertEquals(str, testdoc.get("strvalue", String.class, null));
			}
			{
				DominoDateTime dt = getClient().createDateTime(
						Instant.now().with(ChronoField.MILLI_OF_SECOND, 50) // Domino can only store 1/100 seconds
						);
				testdoc.replaceItemValue("datevalue", dt);
				assertEquals(dt, testdoc.get("datevalue", DominoDateTime.class, null));
			}
			{
				int nr = 1352;
				testdoc.replaceItemValue("intvalue", nr);
				assertEquals(nr, testdoc.get("intvalue", Integer.class, null));
			}
			{
				double nr = 13.52;
				testdoc.replaceItemValue("doublevalue", nr);
				assertEquals(nr, testdoc.get("doublevalue", Double.class, null));
			}
			
			{
				List<String> strValues = Arrays.asList("A", "B");
				testdoc.replaceItemValue("strvalues", strValues);
				assertEquals(strValues, testdoc.getAsList("strvalues", String.class, null));
				assertEquals(strValues.get(0), testdoc.get("strvalues", String.class, null));
			}
			{
				List<Integer> intValues = Arrays.asList(1,3,5,2);
				testdoc.replaceItemValue("intvalues", intValues);
				assertEquals(intValues, testdoc.getAsList("intvalues", Integer.class, null));
				assertEquals(intValues.get(0), testdoc.get("intvalues", Integer.class, null));
			}
			{
				List<Double> doubleValues = Arrays.asList(1.5,3.4,5.3,2.1);
				testdoc.replaceItemValue("doublevalues", doubleValues);
				assertEquals(doubleValues, testdoc.getAsList("doublevalues", Double.class, null));
				assertEquals(doubleValues.get(0), testdoc.get("doublevalues", Double.class, null));
			}
			{
				DominoDateTime dt1 = getClient().createDateTime(
						Instant.now().with(ChronoField.MILLI_OF_SECOND, 40) // Domino can only store 1/100 seconds
						);
				DominoDateTime dt2 = getClient().createDateTime(
						Instant.now().plusSeconds(10000).with(ChronoField.MILLI_OF_SECOND, 50) // Domino can only store 1/100 seconds
						);
				List<DominoDateTime> dtValues = Arrays.asList(dt1, dt2);
				testdoc.replaceItemValue("datevalues", dtValues);
				assertEquals(dtValues, testdoc.getAsList("datevalues", DominoDateTime.class, null));
				assertEquals(dtValues.get(0), testdoc.get("datevalues", DominoDateTime.class, null));
			}
		});
	}
	
	@Test
	public void testAppendToTextList() throws Exception {
		withTempDb(database -> {
			Document testdoc = database.createDocument();
			
			List<String> values = new ArrayList<>();
			values.add("A");
			values.add("B");
			testdoc.replaceItemValue("list", values);
			assertEquals(values, testdoc.getAsList("list", String.class, null));
			
			testdoc.appendToTextList("list", "C", true);
			testdoc.appendToTextList("list", "D", true);
			testdoc.appendToTextList("list", "A", false);

			values.add("C");
			values.add("D");
			assertEquals(values, testdoc.getAsList("list", String.class, null));
		});
	}
	
	@Test
	public void testForEachItemName() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("foo", "bar");
			doc.replaceItemValue("bar", "baz");
			doc.forEachItem("foo", (item, loop) -> {
				assertEquals("foo", item.getName());
			});
		});
	}
	
	@Test
	public void testCompileLotusScript() throws Exception {
		withResourceDxl("/dxl/testCompileLotusScript", database -> {
			Document doc = database.queryFormula("$TITLE='Test LS Form'", null, EnumSet.noneOf(SearchFlag.class), null, EnumSet.of(DocumentClass.ALLNONDATA))
				.getDocuments()
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Unable to find Test LS Form"));
			assertFalse(doc.hasItem("$SCRIPTOBJ_0"), "Form should not yet have compiled LotusScript");
			doc.compileLotusScript();
			assertTrue(doc.hasItem("$SCRIPTOBJ_0"), "Form should now have compiled LotusScript");
		});
	}
	
	@Test
	public void testCompileInvalidLotusScript() throws Exception {
		withResourceDxl("/dxl/testCompileLotusScript", database -> {
			Document doc = database.queryFormula("$TITLE='Test LS'", null, EnumSet.noneOf(SearchFlag.class), null, EnumSet.of(DocumentClass.ALLNONDATA))
				.getDocuments()
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Unable to find Test LS Library"));
			assertFalse(doc.hasItem("$ScriptLib_O"), "Library should not yet have compiled LotusScript");
			assertThrows(LotusScriptCompilationException.class, () -> doc.compileLotusScript());
			assertFalse(doc.hasItem("$ScriptLib_O"), "Library should still not have compiled LotusScript");
		});
	}
	
	@Test
	public void testResponseParent() throws Exception {
		withTempDb(database -> {
			String parentUnid;
			{
				Document parent = database.createDocument();
				parent.replaceItemValue("Form", "Parent");
				parent.save();
				assertEquals("", parent.getParentDocumentUNID());
				parentUnid = parent.getUNID();
			}
			
			Document child = database.createDocument();
			child.replaceItemValue("Form", "Child");
			child.makeResponse(parentUnid);
			child.save();
			
			String unid = child.getParentDocumentUNID();
			assertEquals(parentUnid, unid);
			Document parent = database.getDocumentByUNID(unid).get();
			assertEquals("Parent", parent.get("Form", String.class, null));
		});
	}
	
	@Test
	public void testResponseCount() throws Exception {
		withTempDb(database -> {
			String parentUnid;
			{
				Document parent = database.createDocument();
				parent.replaceItemValue("Form", "Parent");
				parent.save();
				assertEquals("", parent.getParentDocumentUNID());
				parentUnid = parent.getUNID();
			}
			
			for(int i = 0; i < 10; i++) {
				Document child = database.createDocument();
				child.replaceItemValue("Form", "Child");
				child.makeResponse(parentUnid);
				child.save();
			}
			
			Document parent = database.getDocumentByUNID(parentUnid).get();
			assertEquals(10, parent.getResponseCount());
		});
	}
	
	@Test
	public void testResponseTable() throws Exception {
		withTempDb(database -> {
			String parentUnid;
			{
				Document parent = database.createDocument();
				parent.replaceItemValue("Form", "Parent");
				parent.save();
				assertEquals("", parent.getParentDocumentUNID());
				parentUnid = parent.getUNID();
			}
			
			Set<String> childIds = new HashSet<>();
			
			for(int i = 0; i < 10; i++) {
				Document child = database.createDocument();
				child.replaceItemValue("Form", "Child");
				child.makeResponse(parentUnid);
				child.save();
				childIds.add(child.getUNID());
			}
			
			Document parent = database.getDocumentByUNID(parentUnid, EnumSet.of(OpenDocumentMode.LOAD_RESPONSES)).get();
			assertEquals(10, parent.getResponseCount());
			IDTable children = parent.getResponses();
			Set<String> foundChildIds = children.stream()
				.map(database::getDocumentById)
				.map(Optional::get)
				.map(Document::getUNID)
				.collect(Collectors.toSet());
			assertEquals(childIds, foundChildIds);
		});
	}
	
	@Test
	public void writeDesignNote() throws Exception {
		withTempDb(database -> {
			String docId;
			{
				Document doc = database.createDocument();
				assertFalse(doc.getDocumentClass().contains(DocumentClass.FORM));
				assertTrue(doc.getDocumentClass().contains(DocumentClass.DATA));
				doc.setDocumentClass(DocumentClass.FORM);
				assertTrue(doc.getDocumentClass().contains(DocumentClass.FORM));
				assertFalse(doc.getDocumentClass().contains(DocumentClass.DATA));
				doc.save();
				docId = doc.getUNID();
			}
			{
				Document doc = database.getDocumentByUNID(docId).get();
				assertTrue(doc.getDocumentClass().contains(DocumentClass.FORM));
				assertFalse(doc.getDocumentClass().contains(DocumentClass.DATA));
			}
		});
	}
	
	/**
	 * Ensures that doc.removeItem can be called directly without performing a hasItem test
	 * @throws Exception if a test problem occurs
	 */
	@Test
	public void testRemoveFakeItem() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Heh", "Hey");
			doc.removeItem("FooBarBaz");
			assertEquals("Hey", doc.getAsText("Heh", ' '));
		});
	}
}
