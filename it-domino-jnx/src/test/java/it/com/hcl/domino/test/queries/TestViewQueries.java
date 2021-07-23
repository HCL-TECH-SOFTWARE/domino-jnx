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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.DominoException;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntry.SpecialValue;
import com.hcl.domino.data.CollectionSearchQuery.SelectedEntries;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.DominoCollection.Direction;
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

  protected void initViewQueryTestDbDesign(final Database db) throws Exception {
    final DominoClient client = this.getClient();
    final DxlImporter importer = client.createDxlImporter();
    importer.setInputValidationOption(XMLValidationOption.NEVER);
    importer.setDesignImportOption(DXLImportOption.REPLACE_ELSE_CREATE);
    importer.setReplicaRequiredForReplaceOrUpdate(false);

    AbstractNotesRuntimeTest.getResourceFiles("/dxl/testViewQueries").stream()
        .filter(Objects::nonNull)
        .map(name -> PathUtil.concat("/", name, '/'))
        .map(name -> StringUtil.endsWithIgnoreCase(name, ".xml") ? (InputStream) this.getClass().getResourceAsStream(name)
            : StringUtil.endsWithIgnoreCase(name, ".xml.gz")
                ? AbstractNotesRuntimeTest.call(() -> new GZIPInputStream(this.getClass().getResourceAsStream(name)))
                : null)
        .filter(Objects::nonNull)
        .forEach(is -> {
          try {
            importer.importDxl(is, db);
          } catch (final IOException e2) {
            throw new RuntimeException(e2);
          } finally {
            StreamUtil.close(is);
          }
        });

    // sign imported design
    db
        .queryFormula("@true", null, new HashSet<>(), null, EnumSet.of(DocumentClass.ALLNONDATA))
        .getDocuments()
        .forEach(doc -> {
          doc.sign();
          doc.save();

          // String name = doc.get("$TITLE", String.class, null);
          // System.out.println("Signed "+name);
        });

    final int SAMPLE_SIZE = 4000;
    System.out.println("Generating " + SAMPLE_SIZE + " persons in database " + db.getServer() + "!!" + db.getRelativeFilePath());
    AbstractNotesRuntimeTest.generateNABPersons(db, 4000);
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

      System.out.println("Category entry: " + categoryEntry);

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
    final DominoClient client = this.getClient();
    final Database dbFakenames = client.openDatabase("fakenames.nsf");
    final DominoCollection view = dbFakenames.openCollection("($Users)").get();

    final String lookupKey = "Abbo";
    final boolean exact = false;

    final int skip = 0;
    final int count = Integer.MAX_VALUE;

    @SuppressWarnings("unused")
    final List<CollectionEntry> entries = view
        .query()
        .selectByKey(lookupKey, exact)
        .readUNID()
        .readColumnValues()
        .readSpecialValues(SpecialValue.INDEXPOSITION)
        .collectEntries(skip, count);
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
        this.initViewQueryTestDbDesign(db);
      }

      c.accept(db);
    } else {
      this.withTempDb(database -> {
        this.initViewQueryTestDbDesign(database);

        c.accept(database);
      });
    }
  }
}
