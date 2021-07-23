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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.DominoException;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntry.SpecialValue;
import com.hcl.domino.data.CollectionSearchQuery.SelectedEntries;
import com.hcl.domino.data.DominoCollection.Direction;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.Navigate;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.dql.DQL;
import com.hcl.domino.dql.DQL.DQLTerm;
import com.hcl.domino.dxl.DxlExporter;
import com.hcl.domino.dxl.DxlImporter;
import com.hcl.domino.dxl.DxlImporter.DXLImportOption;
import com.hcl.domino.dxl.DxlImporter.XMLValidationOption;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestViewQueries extends AbstractNotesRuntimeTest {

	@Test
	public void exportViews() throws Exception {
		boolean exportViews = "true".equalsIgnoreCase(System.getProperty("jnx.viewtest.exportviews"));
		if(!exportViews) {
			if(log.isLoggable(Level.FINE)) {
				log.fine(MessageFormat.format("Skipping {0}#exportViews; set -Djnx.viewtest.exportviews=true to execute", getClass().getSimpleName()));
			}
			return;
		}
		
		DominoClient client = getClient();
		Database db = client.openDatabase("", "jnx/testviewqueries_design.nsf");

		DxlExporter dxlExporter = client.createDxlExporter();

		Path exportDir = Paths.get("src/test/resources/dxl/testViewQueries").toAbsolutePath();
		Files.createDirectories(exportDir);

		db.getAllCollections().forEach((colInfo) -> {
			if (!colInfo.isFolder()) {
				String name = colInfo.getTitle();
				int noteId = colInfo.getNoteID();

				Path outFilePath = exportDir.resolve(name+".xml");

				try (OutputStream fOut = Files.newOutputStream(outFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
					Document designDoc = db.getDocumentById(noteId).get();
					dxlExporter.exportDocument(designDoc, fOut);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});

		db.queryFormula("@IsMember(\"DefaultForm\";$TITLE)", null,
				EnumSet.of(SearchFlag.SUMMARY), null, EnumSet.of(DocumentClass.FORM))
		.forEachDocument(0, Integer.MAX_VALUE, (doc, loop) -> {
			Path outFilePath = exportDir.resolve("DefaultForm.xml");

			try (OutputStream fOut = Files.newOutputStream(outFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				dxlExporter.exportDocument(doc, fOut);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	protected void initViewQueryTestDbDesign(Database db) throws Exception {
		DominoClient client = getClient();
		DxlImporter importer = client.createDxlImporter();
		importer.setInputValidationOption(XMLValidationOption.NEVER);
		importer.setDesignImportOption(DXLImportOption.REPLACE_ELSE_CREATE);
		importer.setReplicaRequiredForReplaceOrUpdate(false);
		
		getResourceFiles("/dxl/testViewQueries").stream()
		.filter(Objects::nonNull)
		.map(name -> PathUtil.concat("/", name, '/'))
		.map(name ->
		StringUtil.endsWithIgnoreCase(name, ".xml") ?
				(InputStream)getClass().getResourceAsStream(name) :
					StringUtil.endsWithIgnoreCase(name, ".xml.gz") ?
							call(() -> new GZIPInputStream(getClass().getResourceAsStream(name))) :
								null
				)
		.filter(Objects::nonNull)
		.forEach(is -> {
			try {
				importer.importDxl(is, db);
			} catch (IOException e2) {
				throw new RuntimeException(e2);
			} finally {
				StreamUtil.close(is);
			}
		});

		//sign imported design
		db
		.queryFormula("@true", null, new HashSet<>(), null, EnumSet.of(DocumentClass.ALLNONDATA))			
		.getDocuments()
		.forEach((doc) -> {
			doc.sign();
			doc.save();
			
//			String name = doc.get("$TITLE", String.class, null);
//			System.out.println("Signed "+name);
		});
		
		int SAMPLE_SIZE = 4000;
		System.out.println("Generating "+SAMPLE_SIZE+" persons in database "+db.getServer()+"!!"+db.getRelativeFilePath());
		generateNABPersons(db, 4000);
	}

	
	protected void withViewQueryTestDb(DatabaseConsumer c) throws Exception {
		DominoClient client = getClient();
		
		boolean useFixedViewTestDb = "true".equalsIgnoreCase(System.getProperty("jnx.viewtest.usefixeddb"));
		
		if (useFixedViewTestDb) {
			String dbFilePath = "jnx/testviewqueries.nsf";
			Database db;
			try {
				db = client.openDatabase(dbFilePath);
			}
			catch (DominoException e) {
				if (e.getId() != 259) {
					throw e;
				}

				System.out.println("Generated database "+dbFilePath);

				db = client.createDatabase("", dbFilePath, true, true, Encryption.None);
				initViewQueryTestDbDesign(db);
			}
			
			c.accept(db);
		}
		else {
			withTempDb((database) -> {
				initViewQueryTestDbDesign(database);
				
				c.accept(database);
			});
		}
	}

	@Test
	public void testLookupByKey() throws Exception {
		DominoClient client = getClient();
		Database dbFakenames = client.openDatabase("fakenames.nsf");
		DominoCollection view = dbFakenames.openCollection("($Users)").get();
		
		String lookupKey = "Abbo";
		boolean exact = false;
		
		int skip = 0;
		int count = Integer.MAX_VALUE;
		
		@SuppressWarnings("unused")
		List<CollectionEntry> entries = view
			.query()
			.selectByKey(lookupKey, exact)
			.readUNID()
			.readColumnValues()
			.readSpecialValues(SpecialValue.INDEXPOSITION)
			.collectEntries(skip, count);
	}
	
	@Test
	public void testLookupByCategory() throws Exception {
		withViewQueryTestDb((database) -> {
			DominoCollection view = database.openCollection("Lastname Birthyear Categorized").get();

			String category = "C";

			//look up a category
			CollectionEntry categoryEntry = view
					.query()
					.direction(Navigate.CURRENT)
					.readColumnValues()
					.readSpecialValues(SpecialValue.INDEXPOSITION)
					.startAtCategory(category)
					.firstEntry()
					.orElse(null);
			assertNotEquals(null, categoryEntry, "category entry should not be null");
			assertFalse(categoryEntry.getItemNames().isEmpty(), "category should have item names");
			assertFalse(categoryEntry.isEmpty(), "category entry should have column values");
			String cat = categoryEntry.get("$0", String.class, null);
			assertEquals("C", cat);
		});
	}
	
	@Test
	public void testDQLSearch() throws Exception {
		withViewQueryTestDb((database) -> {
			database.updateDQLDesignCatalog(true);
			
			DominoCollection sortView = database.openCollection("Lastname Firstname Flat").get();
			sortView.resortView("lastname", Direction.Descending);
			
			DQLTerm dql = DQL
					.item("Firstname")
					.isEqualTo("Nathan");
			
			int skip = 0;
			int count = Integer.MAX_VALUE;
			
			Set<Integer> sortedIdsOfDQLMatches = sortView
			.query()
			.select(
					SelectedEntries
					.deselectAll()
					.select(dql))
			.collectIds(skip, count);
			
			assertTrue(!sortedIdsOfDQLMatches.isEmpty());
			
			Set<Integer> allDocIds = sortView.getAllIds(true, false);
			assertTrue(!allDocIds.isEmpty());
			
			Set<Integer> allDocIdsFiltered = new LinkedHashSet<>(allDocIds);
			allDocIdsFiltered.retainAll(sortedIdsOfDQLMatches);
			
			assertEquals(sortedIdsOfDQLMatches, allDocIdsFiltered);
		});
	}
	
	@Test
	public void testCategoryLookup() throws Exception {
		withViewQueryTestDb((database) -> {
			DominoCollection view = database.openCollection("Lastname Birthyear Categorized").get();

			String category = "C";

			//look up a category
			CollectionEntry categoryEntry = view
					.query()
					.direction(Navigate.CURRENT)
					.readColumnValues()
					.readSpecialValues(SpecialValue.INDEXPOSITION)
					.startAtCategory(category)
					.firstEntry()
					.orElse(null);
			assertNotNull(categoryEntry);

			String catEntryName = categoryEntry.get("$0", String.class, "");
			assertEquals(category, catEntryName);

			String catEntryPos = categoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, "");
			
			System.out.println("Category entry: "+categoryEntry);

			{
				//now try to find it via its note id
				CollectionEntry categoryEntryById = view
						.query()
						.direction(Navigate.CURRENT)
						.readColumnValues()
						.readSpecialValues(SpecialValue.INDEXPOSITION)
						.startAtEntryId(categoryEntry.getNoteID())
						.firstEntry()
						.orElse(null);
				assertNotNull(categoryEntryById);

				assertEquals(categoryEntry.getNoteID(), categoryEntryById.getNoteID());
				assertNotEquals("", categoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""));
				assertNotEquals("", categoryEntryById.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""));

				assertEquals(
						categoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""),
						categoryEntryById.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""));
			}
			
			{
				//now try to find it via its position
				CollectionEntry categoryEntryByPos = view
						.query()
						.direction(Navigate.CURRENT)
						.readColumnValues()
						.readSpecialValues(SpecialValue.INDEXPOSITION)
						.startAtPosition(catEntryPos)
						.firstEntry()
						.orElse(null);
				assertNotNull(categoryEntryByPos);

				assertEquals(categoryEntry.getNoteID(), categoryEntryByPos.getNoteID());
				assertNotEquals("", categoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""));
				assertNotEquals("", categoryEntryByPos.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""));

				assertEquals(
						categoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""),
						categoryEntryByPos.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""));
			}
		});
	}
	
	// Test for issue #138 "DominoCollectionInfo returns empty strings for non-system collections", though it
	//   has not yet resulted in reproduction of the problem
	@Test
	public void testViewTitles() {
		DominoClient client = getClient();
		Database names = client.openDatabase("pernames.ntf");
		assertTrue(names.getAllCollections()
			.noneMatch(c -> StringUtil.isEmpty(c.getTitle())));
	}
}
