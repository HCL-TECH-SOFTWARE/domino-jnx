/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.DominoException;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntry.SpecialValue;
import com.hcl.domino.data.CollectionSearchQuery;
import com.hcl.domino.data.CollectionSearchQuery.SelectedEntries;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.DominoCollection.Direction;
import com.hcl.domino.data.Find;
import com.hcl.domino.data.Navigate;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.dql.DQL;
import com.hcl.domino.dql.DQL.DQLTerm;
import com.hcl.domino.dxl.DxlExporter;
import com.ibm.commons.util.StringUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
/**
 * Testcases for view lookups
 */
public class TestViewQueries extends AbstractNotesRuntimeTest {

  @Test
  public void exportViews() throws Exception {
    final boolean exportViews = "true".equalsIgnoreCase(System.getProperty("jnx.viewtest.exportviews"));
    if (!exportViews) {
      if (this.log.isLoggable(Level.FINE)) {
        this.log.fine(MessageFormat.format("Skipping {0}#exportViews; set -Djnx.viewtest.exportviews=true to execute",
            this.getClass().getSimpleName()));
      }
      return;
    }

    final DominoClient client = this.getClient();
    final Database db = client.openDatabase("", "jnx/testviewqueries_design.nsf");

    final DxlExporter dxlExporter = client.createDxlExporter();

    final Path exportDir = Paths.get("src/test/resources/dxl/testViewQueries").toAbsolutePath();
    Files.createDirectories(exportDir);

    db.getAllCollections().forEach(colInfo -> {
      if (!colInfo.isFolder()) {
        final String name = colInfo.getTitle();
        final int noteId = colInfo.getNoteID();

        final Path outFilePath = exportDir.resolve(name + ".xml");

        try (OutputStream fOut = Files.newOutputStream(outFilePath, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)) {
          final Document designDoc = db.getDocumentById(noteId).get();
          dxlExporter.exportDocument(designDoc, fOut);
        } catch (final IOException e) {
          e.printStackTrace();
        }

      }
    });

    db.queryFormula("@IsMember(\"DefaultForm\";$TITLE)", null,
        EnumSet.of(SearchFlag.SUMMARY), null, EnumSet.of(DocumentClass.FORM))
        .forEachDocument(0, Integer.MAX_VALUE, (doc, loop) -> {
          final Path outFilePath = exportDir.resolve("DefaultForm.xml");

          try (OutputStream fOut = Files.newOutputStream(outFilePath, StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING)) {
            dxlExporter.exportDocument(doc, fOut);
          } catch (final IOException e) {
            e.printStackTrace();
          }
        });
  }

  @Test
  public void testCategoryLookup() throws Exception {
    this.withViewQueryTestDb(database -> {
      final DominoCollection view = database.openCollection("Lastname Birthyear Categorized").get();

      final String category = "C";

      // look up a category
      final CollectionEntry categoryEntry = view
          .query()
          .direction(Navigate.CURRENT)
          .readColumnValues()
          .readSpecialValues(SpecialValue.INDEXPOSITION)
          .startAtCategory(category)
          .firstEntry()
          .orElse(null);
      Assertions.assertNotNull(categoryEntry);

      final String catEntryName = categoryEntry.get("$0", String.class, "");
      Assertions.assertEquals(category, catEntryName);

      final String catEntryPos = categoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, "");

//      System.out.println("Category entry: " + categoryEntry);

      {
        // now try to find it via its note id
        final CollectionEntry categoryEntryById = view
            .query()
            .direction(Navigate.CURRENT)
            .readColumnValues()
            .readSpecialValues(SpecialValue.INDEXPOSITION)
            .startAtEntryId(categoryEntry.getNoteID())
            .firstEntry()
            .orElse(null);
        Assertions.assertNotNull(categoryEntryById);

        Assertions.assertEquals(categoryEntry.getNoteID(), categoryEntryById.getNoteID());
        Assertions.assertNotEquals("", categoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""));
        Assertions.assertNotEquals("", categoryEntryById.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""));

        Assertions.assertEquals(
            categoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""),
            categoryEntryById.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""));
      }

      {
        // now try to find it via its position
        final CollectionEntry categoryEntryByPos = view
            .query()
            .direction(Navigate.CURRENT)
            .readColumnValues()
            .readSpecialValues(SpecialValue.INDEXPOSITION)
            .startAtPosition(catEntryPos)
            .firstEntry()
            .orElse(null);
        Assertions.assertNotNull(categoryEntryByPos);

        Assertions.assertEquals(categoryEntry.getNoteID(), categoryEntryByPos.getNoteID());
        Assertions.assertNotEquals("", categoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""));
        Assertions.assertNotEquals("", categoryEntryByPos.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""));

        Assertions.assertEquals(
            categoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""),
            categoryEntryByPos.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""));
      }
    });
  }

  @Test
  public void testDQLSearch() throws Exception {
    this.withViewQueryTestDb(database -> {
      database.updateDQLDesignCatalog(true);

      final DominoCollection sortView = database.openCollection("Lastname Firstname Flat").get();
      sortView.resortView("lastname", Direction.Descending);

      final DQLTerm dql = DQL
          .item("Firstname")
          .isEqualTo("Nathan");

      final int skip = 0;
      final int count = Integer.MAX_VALUE;

      final Set<Integer> sortedIdsOfDQLMatches = sortView
          .query()
          .select(
              SelectedEntries
                  .deselectAll()
                  .select(dql))
          .collectIds(skip, count);

      Assertions.assertTrue(!sortedIdsOfDQLMatches.isEmpty());

      final Set<Integer> allDocIds = sortView.getAllIds(true, false);
      Assertions.assertTrue(!allDocIds.isEmpty());

      final Set<Integer> allDocIdsFiltered = new LinkedHashSet<>(allDocIds);
      allDocIdsFiltered.retainAll(sortedIdsOfDQLMatches);

      Assertions.assertEquals(sortedIdsOfDQLMatches, allDocIdsFiltered);
    });
  }

  @Test
  public void testSearchAtFirstEntry() throws Exception {
    this.withViewQueryTestDb(database -> {
      final DominoCollection view = database.openCollection("Lastname Firstname Flat").get();
      view.resortView("lastname", Direction.Descending);
      CollectionSearchQuery query = view.query();
      

      Optional<CollectionEntry> firstEntry = query.firstEntry();
      Optional<Integer> firstId = query
              .startAtFirstEntry()
              .collectIds(0, 1)
              .stream()
              .findFirst();

      Assertions.assertTrue(firstEntry.isPresent());
      Assertions.assertTrue(firstId.isPresent());
      Assertions.assertEquals(firstId.get(), firstEntry.get().getNoteID());
    });
  }

  @Test
  public void testSelectByMultipleKey() throws Exception {
    this.withViewQueryTestDb(database -> {
      final DominoCollection view = database.openCollection("Lastname Firstname Flat").get();
      CollectionSearchQuery query = view.query();

      String firstKey = "Abbo";
      String secondKey = "H";
      final List<Object> lookupKeys = Arrays.asList(firstKey, secondKey);
      query
              .startAtFirstEntry()
              .selectByKey(lookupKeys, false)
              .forEachDocument(0, 4000, (doc, loop) -> {
                String lastname = doc.getItemValue("Lastname").toString();
                String firstname = doc.getItemValue("Firstname").toString();
                Assertions.assertTrue(lastname.contains(firstKey) && firstname.contains(secondKey));
              });
      Set<Integer> ids = query.collectIds(0, 4000);

      Assertions.assertEquals(2, query.size());
      Assertions.assertEquals(2, ids.size());
    });
  }

  @Test
  public void testDeselectByKey() throws Exception {
    this.withViewQueryTestDb(database -> {
      final DominoCollection view = database.openCollection("Lastname Firstname Flat").get();
      CollectionSearchQuery query = view.query();

      final String lookupKey = "Abbo";
      Set<Integer> ids = query
              .startAtFirstEntry()
              .deselectByKey(lookupKey, false)
              .collectIds(0, 4000);

      Assertions.assertEquals(3996, ids.size());
      ids.forEach(id -> Assertions.assertFalse(database.getDocumentById(id).get()
              .getItemValue("Lastname").toString()
              .contains(lookupKey)));
    });
  }

  @Test
  public void testDeselectByMultipleKeys() throws Exception {
    this.withViewQueryTestDb(database -> {
      final DominoCollection view = database.openCollection("Lastname Firstname Flat").get();
      CollectionSearchQuery query = view.query();

      String firstKey = "Abbo";
      String secondKey = "H";
      final List<Object> lookupKeys = Arrays.asList(firstKey, secondKey);
      Set<Integer> ids = new HashSet<>();
      query.startAtFirstEntry()
              .deselectByKey(lookupKeys, false)
              .collectIds(0, 4000, ids);

      Assertions.assertEquals(3998, ids.size());
      ids.forEach(id -> {
        Document curDoc = database.getDocumentById(id).get();
        String lastname = curDoc.getItemValue("Lastname").toString();
        String firstname = curDoc.getItemValue("Firstname").toString();
        Assertions.assertFalse(lastname.contains(firstKey) && firstname.contains(secondKey));
      });
    });
  }

  @Test
  public void testLookupByDifferentLevelsOfCategory() throws Exception {
    this.withViewQueryTestDb(database -> {
      final DominoCollection view = database.openCollection("Lastname Birthyear Categorized").get();

      final List<Object> categories = Arrays.asList("A", "Abbott");
      String categoryString = categories.stream()
              .map(Object::toString)
              .collect(Collectors.joining("\\"));

      final List<CollectionEntry> categoryEntries = new ArrayList<>();
      List<SpecialValue> specialValues = Arrays.asList(SpecialValue.INDEXPOSITION, SpecialValue.SEQUENCENUMBER);
      view
              .query()
              .direction(Navigate.NEXT_DOCUMENT)
              .readColumnValues()
              .readSpecialValues(specialValues)
              .startAtCategory(categories)
              .collectEntries(0, 4000, categoryEntries);
      Assertions.assertNotEquals(Collections.emptyList(), categoryEntries, "category entry should not be empty");
      Assertions.assertEquals(4, categoryEntries.size());

      categoryEntries.forEach(categoryEntry -> {
        Assertions.assertFalse(categoryEntry.getItemNames().isEmpty(), "category should have item names");
        Assertions.assertFalse(categoryEntry.isEmpty(), "category entry should have column values");
        final String actualCategory = categoryEntry.get("$0", String.class, null);
        Assertions.assertEquals(categoryString, actualCategory);
      });
    });
  }

  @Test
  public void testLookupByCategory() throws Exception {
    this.withViewQueryTestDb(database -> {
      final DominoCollection view = database.openCollection("Lastname Birthyear Categorized").get();

      final String category = "C";

      // look up a category
      final CollectionEntry categoryEntry = view
          .query()
          .direction(Navigate.CURRENT)
          .readColumnValues()
          .readSpecialValues(SpecialValue.INDEXPOSITION)
          .startAtCategory(category)
          .firstEntry()
          .orElse(null);
      Assertions.assertNotEquals(null, categoryEntry, "category entry should not be null");
      Assertions.assertFalse(categoryEntry.getItemNames().isEmpty(), "category should have item names");
      Assertions.assertFalse(categoryEntry.isEmpty(), "category entry should have column values");
      final String cat = categoryEntry.get("$0", String.class, null);
      Assertions.assertEquals("C", cat);
    });
  }

  @Test
  public void testLookupByKey() throws Exception {
    withViewQueryTestDb((database) -> {
      final DominoCollection view = database.openCollection("Lastname Firstname Flat").get();

      final String lookupKey = "Abbo";
      final boolean exact = false;

      final int skip = 0;
      final int count = Integer.MAX_VALUE;

      final List<CollectionEntry> entries = view
          .query()
          .selectByKey(lookupKey, exact)
          .readUNID()
          .readColumnValues()
          .readSpecialValues(SpecialValue.INDEXPOSITION)
          .collectEntries(skip, count);
      
      Assertions.assertFalse(entries.isEmpty());
      entries.forEach((entry) -> {
        Assertions.assertTrue(entry.get("Lastname", String.class, "").startsWith(lookupKey));
      });
    });
  }

  // Test for issue #138 "DominoCollectionInfo returns empty strings for
  // non-system collections", though it
  // has not yet resulted in reproduction of the problem
  @Test
  public void testViewTitles() {
    final DominoClient client = this.getClient();
    final Database names = client.openDatabase("pernames.ntf");
    Assertions.assertTrue(names.getAllCollections()
        .noneMatch(c -> StringUtil.isEmpty(c.getTitle())));
  }

  @Test
  public void testExpand() throws Exception {
    this.withViewQueryTestDb(database -> {
      final DominoCollection view = database.openCollection("Lastname Birthyear Categorized").get();
      CollectionSearchQuery query = view.query().expand(CollectionSearchQuery.ExpandedEntries.collapseAll());

      List<CollectionEntry> entries = query
              .readColumnValues()
              .collectEntries(0, 4000);

      Assertions.assertEquals(25, entries.size());
    });
  }

  protected void withViewQueryTestDb(final DatabaseConsumer c) throws Exception {
    final DominoClient client = this.getClient();

    final boolean useFixedViewTestDb = "true".equalsIgnoreCase(System.getProperty("jnx.viewtest.usefixeddb"));

    if (useFixedViewTestDb) {
      final String dbFilePath = "jnx/testviewqueries.nsf";
      Database db;
      try {
        db = client.openDatabase(dbFilePath);
      } catch (final DominoException e) {
        if (e.getId() != 259) {
          throw e;
        }

        System.out.println("Generated database " + dbFilePath);

        db = client.createDatabase("", dbFilePath, true, true, Encryption.None);
        populateResourceDxl("/dxl/testViewQueries", db);
        generateNABPersons(db, 4000);
      }

      c.accept(db);
    } else {
      this.withTempDb(db -> {
        populateResourceDxl("/dxl/testViewQueries", db);
        generateNABPersons(db, 4000);

        c.accept(db);
      });
    }
  }
  
  @Test
  public void testFindPosition() throws Exception {
    this.withViewQueryTestDb(database -> {
      final DominoCollection view = database.openCollection("Lastname Birthyear Categorized").get();
      
      Optional<String> pos = view.getPositionByKey(EnumSet.of(Find.GREATER_THAN, Find.EQUAL), "aaaaaaaaa");
      if(!pos.isPresent()) {
        fail("Should have found a matching position");
      } else {
        assertFalse(pos.get() == null || pos.get().isEmpty());
        assertNotEquals("1", pos.get());
      }
    });
  }
}
