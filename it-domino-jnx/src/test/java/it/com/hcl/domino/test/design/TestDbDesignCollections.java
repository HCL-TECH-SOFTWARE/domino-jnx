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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.CollectionColumn.TotalType;
import com.hcl.domino.data.Database;
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
    Assertions.assertTrue(view.isAllowCustomizations());
    Assertions.assertEquals(CollectionDesignElement.OnOpen.GOTO_TOP, view.getOnOpenUISetting());
    Assertions.assertEquals(CollectionDesignElement.OnRefresh.REFRESH_DISPLAY, view.getOnRefreshUISetting());

    final List<CollectionColumn> columns = view.getColumns();
    Assertions.assertEquals(12, columns.size());
    {
      final CollectionColumn column = columns.get(0);
      Assertions.assertEquals("Form", column.getTitle());
      Assertions.assertEquals("Form", column.getItemName());
      Assertions.assertEquals("", column.getFormula());
      Assertions.assertFalse(column.isConstant());
      Assertions.assertEquals(188, column.getDisplayWidth());
      Assertions.assertEquals(ViewColumnFormat.ListDelimiter.SEMICOLON, column.getListDisplayDelimiter());
      Assertions.assertEquals(TotalType.None, column.getTotalType());

      final CollectionColumn.SortConfiguration sortConfig = column.getSortConfiguration();
      Assertions.assertTrue(sortConfig.isCategory());
      Assertions.assertTrue(sortConfig.isSorted());
      Assertions.assertTrue(sortConfig.isSortPermuted());
      Assertions.assertFalse(sortConfig.getResortToViewUnid().isPresent());
    }
    {
      final CollectionColumn column = columns.get(1);
      Assertions.assertEquals("Size", column.getTitle());
      Assertions.assertEquals("$2", column.getItemName());
      Assertions.assertEquals("@AttachmentLengths", column.getFormula());
      Assertions.assertFalse(column.isConstant());
      Assertions.assertEquals(ViewColumnFormat.ListDelimiter.SPACE, column.getListDisplayDelimiter());
      Assertions.assertEquals(TotalType.Total, column.getTotalType());

      final CollectionColumn.SortConfiguration sortConfig = column.getSortConfiguration();
      Assertions.assertFalse(sortConfig.isCategory());
      Assertions.assertFalse(sortConfig.isSorted());
      Assertions.assertFalse(sortConfig.isSortPermuted());
      Assertions.assertTrue(sortConfig.isResortToView());
      Assertions.assertEquals("F7FAC064F4062A4885257BBE006FA09B", sortConfig.getResortToViewUnid().get());
    }
    {
      final CollectionColumn column = columns.get(2);
      Assertions.assertEquals("Created", column.getTitle());
      Assertions.assertEquals("$3", column.getItemName());
      Assertions.assertEquals("@Created", column.getFormula());
      Assertions.assertFalse(column.isConstant());
      Assertions.assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      Assertions.assertEquals(TotalType.Average, column.getTotalType());

      final CollectionColumn.SortConfiguration sortConfig = column.getSortConfiguration();
      Assertions.assertFalse(sortConfig.isCategory());
      Assertions.assertFalse(sortConfig.isSorted());
      Assertions.assertFalse(sortConfig.isSortPermuted());
      Assertions.assertFalse(sortConfig.isResortToView());
      Assertions.assertTrue(sortConfig.isResortAscending() && sortConfig.isResortDescending());
      Assertions.assertTrue(sortConfig.isDeferResortIndexing());
      Assertions.assertFalse(sortConfig.getResortToViewUnid().isPresent());
    }
    {
      final CollectionColumn column = columns.get(3);
      Assertions.assertEquals("Modified", column.getTitle());
      Assertions.assertEquals(ViewColumnFormat.ListDelimiter.COMMA, column.getListDisplayDelimiter());
      Assertions.assertEquals(TotalType.AveragePerSubcategory, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(4);
      Assertions.assertEquals("Static Value!", column.getTitle());
      Assertions.assertFalse(column.isUseHideWhen());
      Assertions.assertEquals("SecretHideWhen", column.getHideWhenFormula());
      Assertions.assertEquals(ViewColumnFormat.ListDelimiter.NEWLINE, column.getListDisplayDelimiter());
      Assertions.assertEquals(TotalType.PercentOfParentCategory, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(5);
      Assertions.assertEquals("#", column.getTitle());
      Assertions.assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      Assertions.assertEquals(TotalType.None, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(6);
      Assertions.assertEquals("I am test col 2", column.getTitle());
      Assertions.assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      Assertions.assertEquals(TotalType.None, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(7);
      Assertions.assertEquals("Names Guy", column.getTitle());
      Assertions.assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      Assertions.assertEquals(TotalType.Percent, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(8);
      Assertions.assertEquals("Names Guy 2", column.getTitle());
      Assertions.assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      Assertions.assertEquals(TotalType.None, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(9);
      Assertions.assertEquals("I am test col 2", column.getTitle());
      Assertions.assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      Assertions.assertEquals(TotalType.None, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(10);
      Assertions.assertEquals("Hidden Guy", column.getTitle());
      Assertions.assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      Assertions.assertEquals(TotalType.None, column.getTotalType());
    }
    {
      final CollectionColumn column = columns.get(11);
      Assertions.assertEquals("Column of constant value", column.getTitle());
      Assertions.assertTrue(column.isConstant());
      Assertions.assertEquals("\"hello\"", column.getFormula());
      Assertions.assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      Assertions.assertEquals(TotalType.None, column.getTotalType());
    }

  }

  @Test
  public void testExampleView2() {
    final DbDesign dbDesign = this.database.getDesign();
    final View view = dbDesign.getView("Example View 2").get();
    Assertions.assertFalse(view.isAllowCustomizations());
    Assertions.assertEquals(CollectionDesignElement.OnOpen.GOTO_LAST_OPENED, view.getOnOpenUISetting());
    Assertions.assertEquals(CollectionDesignElement.OnRefresh.DISPLAY_INDICATOR, view.getOnRefreshUISetting());
  }

  @Test
  public void testExampleView3() {
    final DbDesign dbDesign = this.database.getDesign();
    final View view = dbDesign.getView("Example View 3").get();
    Assertions.assertFalse(view.isAllowCustomizations());
    Assertions.assertEquals(CollectionDesignElement.OnOpen.GOTO_BOTTOM, view.getOnOpenUISetting());
    Assertions.assertEquals(CollectionDesignElement.OnRefresh.REFRESH_FROM_TOP, view.getOnRefreshUISetting());
  }

  @Test
  public void testExampleView4() {
    final DbDesign dbDesign = this.database.getDesign();
    final View view = dbDesign.getView("Example View 4").get();
    Assertions.assertFalse(view.isAllowCustomizations());
    Assertions.assertEquals(CollectionDesignElement.OnOpen.GOTO_LAST_OPENED, view.getOnOpenUISetting());
    Assertions.assertEquals(CollectionDesignElement.OnRefresh.REFRESH_FROM_BOTTOM, view.getOnRefreshUISetting());
  }

  @Test
  public void testFolders() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getFolders().collect(Collectors.toList());
    Assertions.assertEquals(TestDbDesignCollections.EXPECTED_IMPORT_FOLDERS, collections.size());

    {
      CollectionDesignElement view = collections.stream().filter(v -> "test folder".equals(v.getTitle())).findFirst().orElse(null);
      Assertions.assertNotNull(view);

      view = dbDesign.getCollection("test folder").orElse(null);
      Assertions.assertNotNull(view);
      Assertions.assertInstanceOf(Folder.class, view);
      Assertions.assertEquals("test folder", view.getTitle());
    }
  }

  @Test
  public void testFoldersAndViews() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getCollections().collect(Collectors.toList());
    Assertions.assertEquals(TestDbDesignCollections.EXPECTED_IMPORT_VIEWS + TestDbDesignCollections.EXPECTED_IMPORT_FOLDERS + 1,
        collections.size());

    {
      CollectionDesignElement view = collections.stream().filter(v -> "test view".equals(v.getTitle())).findFirst().orElse(null);
      Assertions.assertNotNull(view);

      view = dbDesign.getCollection("test view").orElse(null);
      Assertions.assertNotNull(view);
      Assertions.assertInstanceOf(View.class, view);
      Assertions.assertEquals("test view", view.getTitle());
    }
    {
      CollectionDesignElement view = collections.stream().filter(v -> "test folder".equals(v.getTitle())).findFirst().orElse(null);
      Assertions.assertNotNull(view);

      view = dbDesign.getCollection("test folder").orElse(null);
      Assertions.assertNotNull(view);
      Assertions.assertInstanceOf(Folder.class, view);
      Assertions.assertEquals("test folder", view.getTitle());
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
    Assertions.assertEquals(TestDbDesignCollections.EXPECTED_IMPORT_VIEWS + 1, collections.size());

    {
      CollectionDesignElement view = collections.stream().filter(v -> "test view".equals(v.getTitle())).findFirst().orElse(null);
      Assertions.assertNotNull(view);

      view = dbDesign.getCollection("test view").orElse(null);
      Assertions.assertNotNull(view);
      Assertions.assertInstanceOf(View.class, view);
      Assertions.assertEquals("test view", view.getTitle());
      Assertions.assertEquals("8.5.3", view.getDesignerVersion());

      {
        Assertions.assertTrue(view.isProhibitRefresh());
        view.setProhibitRefresh(false);
        Assertions.assertFalse(view.isProhibitRefresh());
        view.setProhibitRefresh(true);
        Assertions.assertTrue(view.isProhibitRefresh());
      }

      {
        Assertions.assertFalse(view.isHideFromWeb());
        view.setHideFromWeb(true);
        Assertions.assertTrue(view.isHideFromWeb());
        view.setHideFromWeb(false);
        Assertions.assertFalse(view.isHideFromWeb());
      }
      {
        Assertions.assertFalse(view.isHideFromNotes());
        view.setHideFromNotes(true);
        Assertions.assertTrue(view.isHideFromNotes());
        view.setHideFromNotes(false);
        Assertions.assertFalse(view.isHideFromNotes());
      }
      {
        Assertions.assertFalse(view.isHideFromMobile());
        view.setHideFromMobile(true);
        Assertions.assertTrue(view.isHideFromMobile());
        view.setHideFromMobile(false);
        Assertions.assertFalse(view.isHideFromMobile());
      }
    }
  }
}
