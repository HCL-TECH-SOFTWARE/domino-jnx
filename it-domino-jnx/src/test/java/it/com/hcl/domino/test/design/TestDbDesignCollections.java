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
package it.com.hcl.domino.test.design;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.CollectionColumn.TotalType;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.Folder;
import com.hcl.domino.design.View;
import com.hcl.domino.design.format.ViewColumnFormat;
import com.hcl.domino.exception.FileDoesNotExistException;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesignCollections extends AbstractNotesRuntimeTest {
	public static final int EXPECTED_IMPORT_VIEWS = 5;
	public static final int EXPECTED_IMPORT_FOLDERS = 1;
	
	private static String dbPath;
	private Database database;
	
	@BeforeEach
	public void initDesignDb() throws IOException, URISyntaxException {
		if(database == null) {
			DominoClient client = getClient();
			if(dbPath == null) {
				database = createTempDb(client);
				dbPath = database.getAbsoluteFilePath();
				populateResourceDxl("/dxl/testDbDesignCollections", database);
			} else {
				database = client.openDatabase("", dbPath);
			}
		}
	}
	
	@AfterAll
	public static void termDesignDb() {
		try {
			Files.deleteIfExists(Paths.get(dbPath));
		} catch(Throwable t) {
			System.err.println("Unable to delete database " + dbPath + ": " + t);
		}
	}
	
	@Test
	public void testFoldersAndViews() {
		DbDesign dbDesign = database.getDesign();
		Collection<CollectionDesignElement> collections = dbDesign.getCollections().collect(Collectors.toList());
		assertEquals(EXPECTED_IMPORT_VIEWS + EXPECTED_IMPORT_FOLDERS + 1, collections.size());
		
		{
			CollectionDesignElement view = collections.stream().filter(v -> "test view".equals(v.getTitle())).findFirst().orElse(null);
			assertNotNull(view);
			
			view = dbDesign.getCollection("test view").orElse(null);
			assertNotNull(view);
			assertInstanceOf(View.class, view);
			assertEquals("test view", view.getTitle());
		}
		{
			CollectionDesignElement view = collections.stream().filter(v -> "test folder".equals(v.getTitle())).findFirst().orElse(null);
			assertNotNull(view);
			
			view = dbDesign.getCollection("test folder").orElse(null);
			assertNotNull(view);
			assertInstanceOf(Folder.class, view);
			assertEquals("test folder", view.getTitle());
		}
	}
	
	@Test
	public void testViews() {
		DbDesign dbDesign = database.getDesign();
		Collection<CollectionDesignElement> collections = dbDesign.getViews().collect(Collectors.toList());
		assertEquals(EXPECTED_IMPORT_VIEWS+ 1, collections.size());
		
		{
			CollectionDesignElement view = collections.stream().filter(v -> "test view".equals(v.getTitle())).findFirst().orElse(null);
			assertNotNull(view);
			
			view = dbDesign.getCollection("test view").orElse(null);
			assertNotNull(view);
			assertInstanceOf(View.class, view);
			assertEquals("test view", view.getTitle());
			assertEquals("8.5.3", view.getDesignerVersion());
			
			{
				assertTrue(view.isProhibitRefresh());
				view.setProhibitRefresh(false);
				assertFalse(view.isProhibitRefresh());
				view.setProhibitRefresh(true);
				assertTrue(view.isProhibitRefresh());
			}
			
			{
				assertFalse(view.isHideFromWeb());
				view.setHideFromWeb(true);
				assertTrue(view.isHideFromWeb());
				view.setHideFromWeb(false);
				assertFalse(view.isHideFromWeb());
			}
			{
				assertFalse(view.isHideFromNotes());
				view.setHideFromNotes(true);
				assertTrue(view.isHideFromNotes());
				view.setHideFromNotes(false);
				assertFalse(view.isHideFromNotes());
			}
			{
				assertFalse(view.isHideFromMobile());
				view.setHideFromMobile(true);
				assertTrue(view.isHideFromMobile());
				view.setHideFromMobile(false);
				assertFalse(view.isHideFromMobile());
			}
		}
	}
	
	@Test
	public void testFolders() {
		DbDesign dbDesign = database.getDesign();
		Collection<CollectionDesignElement> collections = dbDesign.getFolders().collect(Collectors.toList());
		assertEquals(EXPECTED_IMPORT_FOLDERS, collections.size());
		
		{
			CollectionDesignElement view = collections.stream().filter(v -> "test folder".equals(v.getTitle())).findFirst().orElse(null);
			assertNotNull(view);
			
			view = dbDesign.getCollection("test folder").orElse(null);
			assertNotNull(view);
			assertInstanceOf(Folder.class, view);
			assertEquals("test folder", view.getTitle());
		}
	}
	
	@Test
	public void testExampleView() {
		DbDesign dbDesign = database.getDesign();
		View view = dbDesign.getView("Example View").get();
		assertTrue(view.isAllowCustomizations());
		assertEquals(CollectionDesignElement.OnOpen.GOTO_TOP, view.getOnOpenUISetting());
		assertEquals(CollectionDesignElement.OnRefresh.REFRESH_DISPLAY, view.getOnRefreshUISetting());
		
		List<CollectionColumn> columns = view.getColumns();
		assertEquals(12, columns.size());
		{
			CollectionColumn column = columns.get(0);
			assertEquals("Form", column.getTitle());
			assertEquals("Form", column.getItemName());
			assertEquals("", column.getFormula());
			assertFalse(column.isConstant());
			assertEquals(188, column.getDisplayWidth());
			assertEquals(ViewColumnFormat.ListDelimiter.SEMICOLON, column.getListDisplayDelimiter());
			assertEquals(TotalType.None, column.getTotalType());
			
			CollectionColumn.SortConfiguration sortConfig = column.getSortConfiguration();
			assertTrue(sortConfig.isCategory());
			assertTrue(sortConfig.isSorted());
			assertTrue(sortConfig.isSortPermuted());
			assertFalse(sortConfig.getResortToViewUnid().isPresent());
		}
		{
			CollectionColumn column = columns.get(1);
			assertEquals("Size", column.getTitle());
			assertEquals("$2", column.getItemName());
			assertEquals("@AttachmentLengths", column.getFormula());
			assertFalse(column.isConstant());
			assertEquals(ViewColumnFormat.ListDelimiter.SPACE, column.getListDisplayDelimiter());
			assertEquals(TotalType.Total, column.getTotalType());
			
			
			CollectionColumn.SortConfiguration sortConfig = column.getSortConfiguration();
			assertFalse(sortConfig.isCategory());
			assertFalse(sortConfig.isSorted());
			assertFalse(sortConfig.isSortPermuted());
			assertTrue(sortConfig.isResortToView());
			assertEquals("F7FAC064F4062A4885257BBE006FA09B", sortConfig.getResortToViewUnid().get());
		}
		{
			CollectionColumn column = columns.get(2);
			assertEquals("Created", column.getTitle());
			assertEquals("$3", column.getItemName());
			assertEquals("@Created", column.getFormula());
			assertFalse(column.isConstant());
			assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
			assertEquals(TotalType.Average, column.getTotalType());

			CollectionColumn.SortConfiguration sortConfig = column.getSortConfiguration();
			assertFalse(sortConfig.isCategory());
			assertFalse(sortConfig.isSorted());
			assertFalse(sortConfig.isSortPermuted());
			assertFalse(sortConfig.isResortToView());
			assertTrue(sortConfig.isResortAscending() && sortConfig.isResortDescending());
			assertTrue(sortConfig.isDeferResortIndexing());
			assertFalse(sortConfig.getResortToViewUnid().isPresent());
		}
		{
			CollectionColumn column = columns.get(3);
			assertEquals("Modified", column.getTitle());
			assertEquals(ViewColumnFormat.ListDelimiter.COMMA, column.getListDisplayDelimiter());
			assertEquals(TotalType.AveragePerSubcategory, column.getTotalType());
		}
		{
			CollectionColumn column = columns.get(4);
			assertEquals("Static Value!", column.getTitle());
			assertFalse(column.isUseHideWhen());
			assertEquals("SecretHideWhen", column.getHideWhenFormula());
			assertEquals(ViewColumnFormat.ListDelimiter.NEWLINE, column.getListDisplayDelimiter());
			assertEquals(TotalType.PercentOfParentCategory, column.getTotalType());
		}
		{
			CollectionColumn column = columns.get(5);
			assertEquals("#", column.getTitle());
			assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
			assertEquals(TotalType.None, column.getTotalType());
		}
		{
			CollectionColumn column = columns.get(6);
			assertEquals("I am test col 2", column.getTitle());
			assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
			assertEquals(TotalType.None, column.getTotalType());
		}
		{
			CollectionColumn column = columns.get(7);
			assertEquals("Names Guy", column.getTitle());
			assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
			assertEquals(TotalType.Percent, column.getTotalType());
		}
		{
			CollectionColumn column = columns.get(8);
			assertEquals("Names Guy 2", column.getTitle());
			assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
			assertEquals(TotalType.None, column.getTotalType());
		}
		{
			CollectionColumn column = columns.get(9);
			assertEquals("I am test col 2", column.getTitle());
			assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
			assertEquals(TotalType.None, column.getTotalType());
		}
		{
			CollectionColumn column = columns.get(10);
			assertEquals("Hidden Guy", column.getTitle());
			assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
			assertEquals(TotalType.None, column.getTotalType());
		}
		{
			CollectionColumn column = columns.get(11);
			assertEquals("Column of constant value", column.getTitle());
			assertTrue(column.isConstant());
			assertEquals("\"hello\"", column.getFormula());
			assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
			assertEquals(TotalType.None, column.getTotalType());
		}
		
	}
	
	@Test
	public void testExampleView2() {
		DbDesign dbDesign = database.getDesign();
		View view = dbDesign.getView("Example View 2").get();
		assertFalse(view.isAllowCustomizations());
		assertEquals(CollectionDesignElement.OnOpen.GOTO_LAST_OPENED, view.getOnOpenUISetting());
		assertEquals(CollectionDesignElement.OnRefresh.DISPLAY_INDICATOR, view.getOnRefreshUISetting());
	}
	
	@Test
	public void testExampleView3() {
		DbDesign dbDesign = database.getDesign();
		View view = dbDesign.getView("Example View 3").get();
		assertFalse(view.isAllowCustomizations());
		assertEquals(CollectionDesignElement.OnOpen.GOTO_BOTTOM, view.getOnOpenUISetting());
		assertEquals(CollectionDesignElement.OnRefresh.REFRESH_FROM_TOP, view.getOnRefreshUISetting());
	}
	
	@Test
	public void testExampleView4() {
		DbDesign dbDesign = database.getDesign();
		View view = dbDesign.getView("Example View 4").get();
		assertFalse(view.isAllowCustomizations());
		assertEquals(CollectionDesignElement.OnOpen.GOTO_LAST_OPENED, view.getOnOpenUISetting());
		assertEquals(CollectionDesignElement.OnRefresh.REFRESH_FROM_BOTTOM, view.getOnRefreshUISetting());
	}
	
	/**
	 * Iterates over all views and folders in mail12.ntf (if present) to test for exceptions in
	 * view-format reading
	 */
	@Test
	public void testIterateMailNtf() {
		DominoClient client = getClient();
		Database database;
		try {
			database = client.openDatabase("mail12.ntf");
		} catch(FileDoesNotExistException e) {
			// That's fine - not on 12, so skip the test
			return;
		}
		
		database.getDesign()
			.getCollections()
			.forEach(collection -> {
				String title = collection.getTitle();
				try {
					collection.getColumns().forEach(col -> {
						@SuppressWarnings("unused")
						String colName = col.getItemName();
					});
				} catch(Throwable t) {
					throw new RuntimeException(MessageFormat.format("Encountered exception in view \"{0}\"", title), t);
				}
			});
	}
}
