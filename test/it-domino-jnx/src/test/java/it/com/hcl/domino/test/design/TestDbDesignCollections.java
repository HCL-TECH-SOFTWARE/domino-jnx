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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.CollectionColumn.TotalType;
import com.hcl.domino.data.Database;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.Folder;
import com.hcl.domino.design.View;
import com.hcl.domino.design.format.ViewColumnFormat;
import com.hcl.domino.exception.FileDoesNotExistException;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesignCollections extends AbstractNotesRuntimeTest {
  public static final int EXPECTED_IMPORT_VIEWS = 8;
  public static final int EXPECTED_IMPORT_FOLDERS = 1;

  private static String dbPath;

  @AfterAll
  public static void termDesignDb() {
    try {
      Files.deleteIfExists(Paths.get(TestDbDesignCollections.dbPath));
    } catch (final Throwable t) {
      System.err.println("Unable to delete database " + TestDbDesignCollections.dbPath + ": " + t);
    }
  }

  private Database database;

  @BeforeEach
  public void initDesignDb() throws IOException, URISyntaxException {
    if (this.database == null) {
      final DominoClient client = this.getClient();
      if (TestDbDesignCollections.dbPath == null) {
        this.database = AbstractNotesRuntimeTest.createTempDb(client);
        TestDbDesignCollections.dbPath = this.database.getAbsoluteFilePath();
        AbstractNotesRuntimeTest.populateResourceDxl("/dxl/testDbDesignCollections", this.database);
      } else {
        this.database = client.openDatabase("", TestDbDesignCollections.dbPath);
      }
    }
  }

  @Test
  public void testExampleView() {
    final DbDesign dbDesign = this.database.getDesign();
    final View view = dbDesign.getView("Example View").get();
    assertTrue(view.isAllowCustomizations());
    assertEquals(CollectionDesignElement.OnOpen.GOTO_TOP, view.getOnOpenUISetting());
    assertEquals(CollectionDesignElement.OnRefresh.REFRESH_DISPLAY, view.getOnRefreshUISetting());
    assertEquals(DesignElement.ClassicThemeBehavior.USE_DATABASE_SETTING, view.getClassicThemeBehavior());
    assertEquals(CollectionDesignElement.Style.STANDARD_OUTLINE, view.getStyle());
    assertFalse(view.isDefaultCollection());
    assertFalse(view.isDefaultCollectionDesign());
    assertFalse(view.isCollapseAllOnFirstOpen());
    assertTrue(view.isShowResponseDocumentsInHierarchy());
    assertFalse(view.isShowInViewMenu());
    assertTrue(view.isEvaluateActionsOnDocumentChange());

    final List<CollectionColumn> columns = view.getColumns();
    assertEquals(12, columns.size());
    {
      final CollectionColumn column = columns.get(0);
      assertEquals("Form", column.getTitle());
      assertEquals("Form", column.getItemName());
      assertEquals("", column.getFormula());
      assertFalse(column.isConstant());
      assertEquals(188, column.getDisplayWidth());
      assertEquals(ViewColumnFormat.ListDelimiter.SEMICOLON, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());

      final CollectionColumn.SortConfiguration sortConfig = column.getSortConfiguration();
      assertTrue(sortConfig.isCategory());
      assertTrue(sortConfig.isSorted());
      assertTrue(sortConfig.isSortPermuted());
      assertFalse(sortConfig.getResortToViewUnid().isPresent());
    }
    {
      final CollectionColumn column = columns.get(1);
      assertEquals("Size", column.getTitle());
      assertEquals("$2", column.getItemName());
      assertEquals("@AttachmentLengths", column.getFormula());
      assertFalse(column.isConstant());
      assertEquals(ViewColumnFormat.ListDelimiter.SPACE, column.getListDisplayDelimiter());
      assertEquals(TotalType.Total, column.getTotalType());

      final CollectionColumn.SortConfiguration sortConfig = column.getSortConfiguration();
      assertFalse(sortConfig.isCategory());
      assertFalse(sortConfig.isSorted());
      assertFalse(sortConfig.isSortPermuted());
      assertTrue(sortConfig.isResortToView());
      assertEquals("F7FAC064F4062A4885257BBE006FA09B", sortConfig.getResortToViewUnid().get());
    }
    {
      final CollectionColumn column = columns.get(2);
      assertEquals("Created", column.getTitle());
      assertEquals("$3", column.getItemName());
      assertEquals("@Created", column.getFormula());
      assertFalse(column.isConstant());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.Average, column.getTotalType());

      final CollectionColumn.SortConfiguration sortConfig = column.getSortConfiguration();
      assertFalse(sortConfig.isCategory());
      assertFalse(sortConfig.isSorted());
      assertFalse(sortConfig.isSortPermuted());
      assertFalse(sortConfig.isResortToView());
      assertTrue(sortConfig.isResortAscending() && sortConfig.isResortDescending());
      assertTrue(sortConfig.isDeferResortIndexing());
      assertFalse(sortConfig.getResortToViewUnid().isPresent());
    }
    {
      final CollectionColumn column = columns.get(3);
      assertEquals("Modified", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.COMMA, column.getListDisplayDelimiter());
      assertEquals(TotalType.AveragePerSubcategory, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(4);
      assertEquals("Static Value!", column.getTitle());
      assertFalse(column.isUseHideWhen());
      assertEquals("SecretHideWhen", column.getHideWhenFormula());
      assertEquals(ViewColumnFormat.ListDelimiter.NEWLINE, column.getListDisplayDelimiter());
      assertEquals(TotalType.PercentOfParentCategory, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(5);
      assertEquals("#", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(6);
      assertEquals("I am test col 2", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(7);
      assertEquals("Names Guy", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.Percent, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(8);
      assertEquals("Names Guy 2", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(9);
      assertEquals("I am test col 2", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(10);
      assertEquals("Hidden Guy", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(11);
      assertEquals("Column of constant value", column.getTitle());
      assertTrue(column.isConstant());
      assertEquals("\"hello\"", column.getFormula());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());
    }

  }

  @Test
  public void testExampleView2() {
    final DbDesign dbDesign = this.database.getDesign();
    final View view = dbDesign.getView("Example View 2").get();
    assertEquals("Example View 2", view.getTitle());
    assertEquals(Arrays.asList("test alias for view 2", "other alias"), view.getAliases());
    assertEquals("I am a comment'", view.getComment());
    assertFalse(view.isAllowCustomizations());
    assertEquals(CollectionDesignElement.OnOpen.GOTO_LAST_OPENED, view.getOnOpenUISetting());
    assertEquals(CollectionDesignElement.OnRefresh.DISPLAY_INDICATOR, view.getOnRefreshUISetting());
    assertEquals(DesignElement.ClassicThemeBehavior.DONT_INHERIT_FROM_OS, view.getClassicThemeBehavior());
    assertEquals(CollectionDesignElement.Style.STANDARD_OUTLINE, view.getStyle());
    assertFalse(view.isDefaultCollection());
    assertTrue(view.isDefaultCollectionDesign());
    assertTrue(view.isCollapseAllOnFirstOpen());
    assertTrue(view.isShowResponseDocumentsInHierarchy());
    assertFalse(view.isShowInViewMenu());
    assertFalse(view.isEvaluateActionsOnDocumentChange());
  }

  @Test
  public void testExampleView3() {
    final DbDesign dbDesign = this.database.getDesign();
    final View view = dbDesign.getView("Example View 3").get();
    assertFalse(view.isAllowCustomizations());
    assertEquals(CollectionDesignElement.OnOpen.GOTO_BOTTOM, view.getOnOpenUISetting());
    assertEquals(CollectionDesignElement.OnRefresh.REFRESH_FROM_TOP, view.getOnRefreshUISetting());
    assertEquals(DesignElement.ClassicThemeBehavior.INHERIT_FROM_OS, view.getClassicThemeBehavior());
    assertFalse(view.isDefaultCollection());
    assertFalse(view.isDefaultCollectionDesign());
    assertFalse(view.isCollapseAllOnFirstOpen());
    assertFalse(view.isShowResponseDocumentsInHierarchy());
    assertFalse(view.isShowInViewMenu());
    assertFalse(view.isEvaluateActionsOnDocumentChange());
  }

  @Test
  public void testExampleView4() {
    final DbDesign dbDesign = this.database.getDesign();
    final View view = dbDesign.getView("Example View 4").get();
    assertFalse(view.isAllowCustomizations());
    assertEquals(CollectionDesignElement.OnOpen.GOTO_LAST_OPENED, view.getOnOpenUISetting());
    assertEquals(CollectionDesignElement.OnRefresh.REFRESH_FROM_BOTTOM, view.getOnRefreshUISetting());
    assertFalse(view.isDefaultCollection());
    assertFalse(view.isDefaultCollectionDesign());
    assertTrue(view.isShowInViewMenu());
    assertFalse(view.isEvaluateActionsOnDocumentChange());
    
    assertEquals("1", view.getFormulaClass());
    view.setFormulaClass("2");
    assertEquals("2", view.getFormulaClass());
  }
  
  @Test
  public void testAllView() {
    final DbDesign dbDesign = this.database.getDesign();
    final View view = dbDesign.getView("All").get();
    assertFalse(view.isAllowCustomizations());
    assertTrue(view.isDefaultCollection());
  }

  @Test
  public void testFolders() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getFolders().collect(Collectors.toList());
    assertEquals(TestDbDesignCollections.EXPECTED_IMPORT_FOLDERS, collections.size());

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
  public void testFoldersAndViews() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getCollections().collect(Collectors.toList());
    assertEquals(TestDbDesignCollections.EXPECTED_IMPORT_VIEWS + TestDbDesignCollections.EXPECTED_IMPORT_FOLDERS + 1,
        collections.size());

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

  /**
   * Iterates over all views and folders in mail12.ntf (if present) to test for
   * exceptions in
   * view-format reading
   */
  @Test
  public void testIterateMailNtf() {
    final DominoClient client = this.getClient();
    Database database;
    try {
      database = client.openDatabase("mail12.ntf");
    } catch (final FileDoesNotExistException e) {
      // That's fine - not on 12, so skip the test
      return;
    }

    database.getDesign()
        .getCollections()
        .forEach(collection -> {
          final String title = collection.getTitle();
          try {
            collection.getColumns().forEach(col -> {
              @SuppressWarnings("unused")
              final String colName = col.getItemName();
            });
          } catch (final Throwable t) {
            throw new RuntimeException(MessageFormat.format("Encountered exception in view \"{0}\"", title), t);
          }
        });
  }

  @Test
  public void testViews() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getViews().collect(Collectors.toList());
    assertEquals(TestDbDesignCollections.EXPECTED_IMPORT_VIEWS + 1, collections.size());

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
  public void testDeletedSharedColumnView() {
    DbDesign design = this.database.getDesign();
    View view = design.getView("Shared Column Deletion View").get();
    List<CollectionColumn> columns = view.getColumns();
    assertEquals(6, columns.size());
    {
      CollectionColumn col = columns.get(0);
      assertEquals("Shared Col to Delete", col.getTitle());
      assertEquals("@NoteID + \"ghost column\"", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(1);
      assertEquals("Real col", col.getTitle());
      assertEquals("@DocNumber", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertTrue(col.isNameColumn());
      assertTrue(col.getOnlinePresenceNameColumn().isPresent());
      assertEquals("SomeOnlineCol", col.getOnlinePresenceNameColumn().get());
    }
    {
      CollectionColumn col = columns.get(2);
      assertEquals("#", col.getTitle());
      assertEquals("@DocNumber", col.getFormula());
      assertTrue(col.isSharedColumn());
      assertTrue(col.getSharedColumnName().isPresent());
      assertEquals("testcol", col.getSharedColumnName().get());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(3);
      assertEquals("Shared Col to Delete", col.getTitle());
      assertEquals("@NoteID + \"ghost column\"", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(4);
      assertEquals("Real col 2", col.getTitle());
      assertEquals("@NoteID + \"I am real col 2\"", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(5);
      assertEquals("I am test col 2", col.getTitle());
      assertEquals("Foo", col.getItemName());
      assertTrue(col.isSharedColumn());
      assertTrue(col.getSharedColumnName().isPresent());
      assertEquals("testcol2", col.getSharedColumnName().get());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
  }

  @Test
  public void testDeletedSharedColumnView2() {
    DbDesign design = this.database.getDesign();
    View view = design.getView("Shared Column Deletion View 2").get();
    List<CollectionColumn> columns = view.getColumns();
    assertEquals(6, columns.size());
    {
      CollectionColumn col = columns.get(0);
      assertEquals("Shared Col to Delete", col.getTitle());
      assertEquals("@NoteID + \"ghost column\"", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(1);
      assertEquals("Real col", col.getTitle());
      assertEquals("@DocNumber", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(2);
      assertEquals("#", col.getTitle());
      assertEquals("@DocNumber", col.getFormula());
      assertTrue(col.isSharedColumn());
      assertTrue(col.getSharedColumnName().isPresent());
      assertEquals("testcol", col.getSharedColumnName().get());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(3);
      assertEquals("Shared Col to Delete", col.getTitle());
      assertEquals("@NoteID + \"ghost column\"", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(4);
      assertEquals("Real col 2", col.getTitle());
      assertEquals("@NoteID + \"I am real col 2\"", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(5);
      assertEquals("I am test col 2", col.getTitle());
      assertEquals("Foo", col.getItemName());
      assertTrue(col.isSharedColumn());
      assertTrue(col.getSharedColumnName().isPresent());
      assertEquals("testcol2", col.getSharedColumnName().get());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
  }
  
  @Test
  public void testMail12NtfByCategory() {
    final DominoClient client = this.getClient();
    Database database;
    try {
      database = client.openDatabase("mail12.ntf");
    } catch (final FileDoesNotExistException e) {
      // That's fine - not on 12, so skip the test
      return;
    }
    
    DbDesign design = database.getDesign();
    View byCategory = design.getView("$ByCategory").get();
    byCategory.getColumns().forEach(col -> {
      System.out.println("read col " + col.getItemName());
    });
  }
}
