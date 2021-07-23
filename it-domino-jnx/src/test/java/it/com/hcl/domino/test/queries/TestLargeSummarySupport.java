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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.hcl.domino.BuildVersionInfo;
import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionSearchQuery.CollectionEntryProcessor;
import com.hcl.domino.data.CompactMode;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.Database.NSFVersionInfo;
import com.hcl.domino.data.DatabaseClass;
import com.hcl.domino.data.DatabaseOption;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DocumentSelection.SelectionType;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.dxl.DxlImporter;
import com.hcl.domino.dxl.DxlImporter.DXLImportOption;
import com.hcl.domino.dxl.DxlImporter.XMLValidationOption;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestLargeSummarySupport extends AbstractNotesRuntimeTest {
	/** For 12.0.0, large item support requires an updated libnotes.so; set LARGEITEMFIX_INSTALLED="true" to
	 * enable the large item tests */
	public static final String LARGEITEMFIX_INSTALLED = "LARGEITEMFIX_INSTALLED"; //$NON-NLS-1$

	public enum LargeDataLevel {
		/** R10/R11 ODS supporting up to 16MB of summary data */
		R11,
		/** R12 ODS supporting large text items and search results */
		R12
		}
	
	/**
	 * Creates a R10 compatible temp db where the R11 C API can write
	 * up to 16 MB of summary data per document or a R12 compatible
	 * temp db with large item support
	 * 
	 * @param level type of database to return
	 * @param c consumer
	 * @throws Exception
	 */
	protected void withLargeDataEnabledTempDb(LargeDataLevel level, DatabaseConsumer c) throws Exception {
		DominoClient client = getClient();
		Path tempDest = Files.createTempFile(getClass().getName(), ".nsf"); //$NON-NLS-1$
		Files.delete(tempDest);
		DatabaseClass dbClass = level==LargeDataLevel.R11 ? DatabaseClass.V10NOTEFILE : DatabaseClass.V12NOTEFILE;
		Database database = client.createDatabase(null, tempDest.toString(), false, true,
				Encryption.None, dbClass);
		//activate large summary support; on R12 databases, this auto-enables large item support as well
		database.setOption(DatabaseOption.LARGE_BUCKETS_ENABLED, true);
		database.close();
		
		//compact database to activate the large bucket / item support
		client.compact(tempDest.toString(), EnumSet.of(CompactMode.NO_INPLACE));
		database = client.openDatabase(tempDest.toString());
		
		assertNotNull(database);
		try {
			c.accept(database);
		} finally {
			database.close();
			try {
				client.deleteDatabase(null, tempDest.toString());
			} catch(Throwable t) {
				System.err.println("Unable to delete database " + tempDest + ": " + t);
			}
		}
	}
	
	protected void withResourceDxl(LargeDataLevel level, String resDirPath, DatabaseConsumer c) throws Exception {
		withLargeDataEnabledTempDb(level, database -> {
			DxlImporter importer = getClient().createDxlImporter();
			importer.setInputValidationOption(XMLValidationOption.NEVER);
			importer.setDesignImportOption(DXLImportOption.REPLACE_ELSE_CREATE);
			importer.setReplicaRequiredForReplaceOrUpdate(false);
			getResourceFiles(resDirPath).stream()
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
						importer.importDxl(is, database);
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						StreamUtil.close(is);
					}
				});
			
			//re-enable large summary support; gets reset by the DB icon DXL import
			database.setOption(DatabaseOption.LARGE_BUCKETS_ENABLED, true);
			c.accept(database);
		});
	}

	/**
	 * Writes large text items into a document and reads them before/after saving and disposing the doc
	 * 
	 * @throws Exception in case of errors
	 */
	@Test
	@EnabledIfEnvironmentVariable(named = LARGEITEMFIX_INSTALLED, matches = "true", disabledReason = "LARGEITEMFIX_INSTALLED not set to true")
	public void testLargeSummarySupport() throws Exception {
		withLargeDataEnabledTempDb(LargeDataLevel.R11, (db) -> {
			
			//and check if large summary are enabled
			assertEquals(true, db.getOption(DatabaseOption.LARGE_BUCKETS_ENABLED));

			Document docLarge = db.createDocument();
			StringWriter writer = new StringWriter();
			produceTestData(12000, writer);
			
			String sampleTxt = writer.toString();
			
			for (int i=0; i<10; i++) {
				docLarge.replaceItemValue("textitem"+(i+1), sampleTxt); //$NON-NLS-1$
				
				String testTxt = docLarge.get("textitem"+(i+1), String.class, "");
				assertEquals(sampleTxt.length(), testTxt.length());
				assertEquals(sampleTxt, testTxt);
			}
			
			docLarge.save();
			
			//close and reopen the document to test an issue 12.0.0 contained in note open code
			int noteId = docLarge.getNoteID();
			docLarge.autoClosable().close();
			
			docLarge = db.getDocumentById(noteId).get();
			
			for (int i=0; i<10; i++) {
				System.out.println("Checking item textitem"+(i+1));
				
				String itemName = "textitem"+(i+1);
				String testTxt = docLarge.get(itemName, String.class, "");
				assertEquals(sampleTxt.length(), testTxt.length(), "length check of item "+itemName);
				System.out.println("Length of written text ok: "+testTxt.length());
				
				for (int c=0; c<testTxt.length(); c++) {
					char testTxtChar = testTxt.charAt(c);
					char sampleTextChar = sampleTxt.charAt(c);
					
					if (testTxtChar != sampleTextChar) {
						System.out.println("Mismatch at position "+c);
						
						int printStartPos = Math.max(0, c - 50);
						int printEndPos = Math.min(testTxt.length(), c+51);
						
						System.out.println("Printing from "+printStartPos+" to "+printEndPos+":");
						System.out.println("Content we wrote:");
						System.out.println(sampleTxt.substring(printStartPos, printEndPos));
						System.out.println("Content we read:");
						System.out.println(testTxt.substring(printStartPos, printEndPos));
						break;
					}
				}
				assertTrue(sampleTxt.equals(testTxt), "Text is equal");
			}
		});
	}
	
	/**
	 * Tests NSF search support for large summary support in R11 (up to 16 MB
	 * of summary data per doc)
	 * 
	 * @throws Exception in case of errors
	 */
	@Test
	public void testNSFSearchWithLargeSummarySupport() throws Exception {
		withLargeDataEnabledTempDb(LargeDataLevel.R11, (db) -> {
			//write more summary data that Domino without large summary support can handle
			Document docLarge = db.createDocument();
			StringWriter writer = new StringWriter();
			produceTestData(12000, writer);
			
			String sampleTxt = writer.toString();
			
			for (int i=1; i<=10; i++) {
				docLarge.replaceItemValue("textitem"+i, sampleTxt); //$NON-NLS-1$
			}
			docLarge.replaceItemValue("Topic", "Test");
			docLarge.save();

			//Now run a formula search and tell Domino (NSFSearchExtended3) to return all
			//what we have written. For Domino <12 this is unsupported in NSFSearchExtended3.
			//Without any extra flags, it produces "Field is too large (32K) ..." for the large doc.
			//Since we use the flag SEARCH1_LARGE_BUCKETS internally, we tell Domino that we can
			//handle the exceeded summary buffer; in that case, we don't get summary data back
			//from the search and need to open the document manually. All this is handled automatically
			//in our formula search function so that developers don't have to care.
			Map<String,String> computedValues = new HashMap<>();
			for (int i=1; i<=10; i++) {
				computedValues.put("textitem"+i, "");
			}

			List<CollectionEntry> entries = db
			.queryFormula("Topic=\"Test\"", null, EnumSet.of(SearchFlag.SUMMARY), null,
					EnumSet.of(DocumentClass.DATA))
			.computeValues(computedValues)
			.collectEntries();
			
			assertEquals(1, entries.size());
			CollectionEntry entry = entries.get(0);
			
			//check if our data is complete
			for (int i=1; i<=10; i++) {
				assertEquals(sampleTxt, entry.get("textitem"+i, String.class, ""));
			}
			
			//and check that non-existing items are returned as default value (here null)
			assertNull(entry.get("otheritem", String.class, null));
		});
	}
	
	/**
	 * Creates a document with a large summary buffer and tries to read it
	 * via NSF search (R11)
	 * 
	 * @throws Exception in case of errors
	 */
	@Test
	public void testDocumentSelectionWithLargeSummarySupport() throws Exception {
		withLargeDataEnabledTempDb(LargeDataLevel.R11, (db) -> {
			//write more summary data that Domino without large summary support can handle
			Document docLarge = db.createDocument();
			StringWriter writer = new StringWriter();
			produceTestData(12000, writer);
			
			String sampleTxt = writer.toString();
			
			for (int i=1; i<=10; i++) {
				docLarge.replaceItemValue("textitem"+i, sampleTxt); //$NON-NLS-1$
			}
			docLarge.replaceItemValue("Topic", "Test");
			docLarge.save();

			IDTable docIds = db
					.createDocumentSelection()
					.select(SelectionType.DOCUMENTS)
					.withSelectionFormula("Topic=\"Test\"")
					.build();
			
			assertEquals(1, docIds.size());
			assertEquals(docLarge.getNoteID(), docIds.iterator().next());
		});
	}

	/**
	 * 
	 * 
	 * @throws Exception in case of errors
	 */
	@Test
	@EnabledIfEnvironmentVariable(named = LARGEITEMFIX_INSTALLED, matches = "true", disabledReason = "LARGEITEMFIX_INSTALLED not set to true")
	public void testNSFSearchWithLargeItemResult() throws Exception {
		// Check if we're running on V12 in the abstract first.
		// This should avoid test trouble on at least V11 macOS
		{
			BuildVersionInfo buildVersion = getClient().getBuildVersion("");
			if (buildVersion.getMajorVersion() < 12) {
				//large item storage not supported by this API version
				return;
			}
		}

		withLargeDataEnabledTempDb(LargeDataLevel.R12, (db) -> {
			//check if NSF has R12 format
			NSFVersionInfo version = db.getNSFVersionInfo();
			assertTrue(version.getMajorVersion() >= 54);
			
			//and check if large summary and item support are enabled
			assertEquals(true, db.getOption(DatabaseOption.LARGE_ITEMS_ENABLED));
			assertEquals(true, db.getOption(DatabaseOption.LARGE_BUCKETS_ENABLED));
			
			Document docLarge = db.createDocument();
			docLarge.replaceItemValue("Form", "Testform1");
			
			//now we can write up to 16 MB of summary data
			StringWriter summaryTextWriter = new StringWriter();
			produceTestData(1000000, summaryTextWriter);
			String summaryText = summaryTextWriter.toString();
			
			for (int i=0; i<10; i++) {
				docLarge.replaceItemValue("textitem"+(i+1), summaryText); //$NON-NLS-1$
			}
			
			//and non-summary text items with up to 1 GB
			StringWriter nonSummaryTextWriter = new StringWriter();
			produceTestData(20000000, nonSummaryTextWriter);
			String nonSummaryText = summaryTextWriter.toString();
			
			for (int i=0; i<10; i++) {
				docLarge.replaceItemValue("nstextitem"+(i+1), EnumSet.noneOf(ItemFlag.class), nonSummaryText); //$NON-NLS-1$
			}

			docLarge.save();
			int noteId = docLarge.getNoteID();
			docLarge.autoClosable().close();
			docLarge = db.getDocumentById(noteId).get();
			
			Map<String,String> columnValues = new HashMap<>();
			columnValues.put("form", "");
			columnValues.put("textitem1", "");
			List<Map<String, Object>> result =
					db.queryDocuments()
					.computeValues(columnValues)
					.build(0, Integer.MAX_VALUE, 
					new CollectionEntryProcessor<List<Map<String,Object>>>() {

						@Override
						public List<Map<String, Object>> start() {
							return new ArrayList<>();
						}

						@Override
						public Action entryRead(List<Map<String, Object>> result, CollectionEntry entry) {
							result.add(entry);
							String itemVal = entry.get("textitem1", String.class, "");
							
							if (itemVal.length() == summaryText.length()) {
								for (int i=0; i<itemVal.length(); i++) {
									char itemValChar = itemVal.charAt(i);
									char summaryTextChar = summaryText.charAt(i);
									
									if (itemValChar != summaryTextChar) {
										System.out.println("First mismatch at position "+i);
										break;
									}
								}
							}
							assertEquals(summaryText, itemVal);
							return Action.Continue;
						}

						@Override
						public List<Map<String, Object>> end(List<Map<String, Object>> result) {
							return result;
						}
			});
			
			assertEquals(1, result.size());
		});
	}
	
	/**
	 * Writes large items in a doc of an R12 database, reads them and compare both values.
	 * 
	 * @throws Exception in case of errors
	 */
	@Test
	@EnabledIfEnvironmentVariable(named = LARGEITEMFIX_INSTALLED, matches = "true", disabledReason = "LARGEITEMFIX_INSTALLED not set to true")
	public void testLargeItemSupport() throws Exception {
		// Check if we're running on V12 in the abstract first.
		//   This should avoid test trouble on at least V11 macOS
		{
			BuildVersionInfo buildVersion = getClient().getBuildVersion("");
			if (buildVersion.getMajorVersion() < 12) {
				//large item storage not supported by this API version
				return;
			}
		}
		
		withLargeDataEnabledTempDb(LargeDataLevel.R12, (db) -> {
			//check if NSF has R12 format
			NSFVersionInfo version = db.getNSFVersionInfo();
			assertTrue(version.getMajorVersion() >= 54);
			
			//and check if large summary and item support are enabled
			assertEquals(true, db.getOption(DatabaseOption.LARGE_ITEMS_ENABLED));
			assertEquals(true, db.getOption(DatabaseOption.LARGE_BUCKETS_ENABLED));
			
			Document docLarge = db.createDocument();
			
			//now can write up to 16 MB of summary data
			StringWriter summaryTextWriter = new StringWriter();
			produceTestData(1000000, summaryTextWriter);
			String summaryText = summaryTextWriter.toString();
			
			for (int i=0; i<10; i++) {
				docLarge.replaceItemValue("textitem"+(i+1), summaryText); //$NON-NLS-1$
			}

			//and non-summary text items with up to 1 GB
			StringWriter nonSummaryTextWriter = new StringWriter();
			produceTestData(20000000, nonSummaryTextWriter);
			String nonSummaryText = summaryTextWriter.toString();
			
			for (int i=0; i<10; i++) {
				docLarge.replaceItemValue("nstextitem"+(i+1), EnumSet.noneOf(ItemFlag.class), nonSummaryText); //$NON-NLS-1$
			}

			docLarge.save();
			int noteId = docLarge.getNoteID();
			docLarge.autoClosable().close();
			docLarge = db.getDocumentById(noteId).get();
			
			for (int i=0; i<10; i++) {
				String testStr = docLarge.get("textitem"+(i+1), String.class, "");
				assertEquals(summaryText, testStr);
			}
		});
	}
}
