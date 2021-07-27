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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.Database.FormulaQueryCallback;
import com.hcl.domino.data.Database.SearchMatch;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.TypedAccess;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestFormulaSearch extends AbstractNotesRuntimeTest {

  @Test
  public void testSearch() throws Exception {
    final String selectionFormula = "form=\"MatchForm\"";

    // specify with doc items we want to read/compute
    final Map<String, String> computeValues = new HashMap<>();
    computeValues.put("form", "");
    computeValues.put("_created", "@created");
    computeValues.put("randomvalue", "");

    final Set<SearchFlag> searchFlags = EnumSet.of(SearchFlag.NOTIFYDELETIONS);
    final Set<DocumentClass> docClass = EnumSet.of(DocumentClass.DATA, DocumentClass.NOTIFYDELETION);

    this.withTempDb(database -> {
      DominoDateTime since = null;

      {
        // Initial search, database is empty
        final AtomicInteger matchCount = new AtomicInteger();
        final AtomicInteger nonMatchCount = new AtomicInteger();
        final AtomicInteger deletionCount = new AtomicInteger();

        since = database.queryFormula(selectionFormula, null,
            searchFlags, since, docClass, computeValues, new FormulaQueryCallback() {

              @Override
              public Action deletionFound(final Database db, final SearchMatch searchMatch, final TypedAccess computedValues) {
                deletionCount.incrementAndGet();
                return Action.Continue;
              }

              @Override
              public Action matchFound(final Database db, final SearchMatch searchMatch, final TypedAccess computedValues) {
                matchCount.incrementAndGet();
                return Action.Continue;
              }

              @Override
              public Action nonMatchFound(final Database db, final SearchMatch searchMatch, final TypedAccess computedValues) {
                nonMatchCount.incrementAndGet();
                return Action.Continue;
              }
            });

        Assertions.assertNotNull(since);
        Assertions.assertEquals(0, matchCount.get());
        Assertions.assertEquals(0, nonMatchCount.get());
        Assertions.assertEquals(0, deletionCount.get());
      }

      // create a non-match
      final Document nonMatchDoc = database.createDocument();
      nonMatchDoc.replaceItemValue("Form", "NotMatchForm");
      nonMatchDoc.save();
      // System.out.println("Wrote non-match doc with UNID "+nonMatchDoc.getUNID());

      {
        final AtomicInteger matchCount = new AtomicInteger();
        final AtomicInteger nonMatchCount = new AtomicInteger();
        final AtomicInteger deletionCount = new AtomicInteger();

        since = database.queryFormula(selectionFormula, null,
            searchFlags, since, docClass, computeValues, new FormulaQueryCallback() {

              @Override
              public Action deletionFound(final Database db, final SearchMatch searchMatch, final TypedAccess computedValues) {
                deletionCount.incrementAndGet();
                return Action.Continue;
              }

              @Override
              public Action matchFound(final Database db, final SearchMatch searchMatch, final TypedAccess computedValues) {
                matchCount.incrementAndGet();
                return Action.Continue;
              }

              @Override
              public Action nonMatchFound(final Database db, final SearchMatch searchMatch, final TypedAccess computedValues) {
                nonMatchCount.incrementAndGet();
                return Action.Continue;
              }
            });

        Assertions.assertNotNull(since);
        Assertions.assertEquals(0, matchCount.get());
        Assertions.assertEquals(1, nonMatchCount.get());
        Assertions.assertEquals(0, deletionCount.get());
      }

      // delete non-match
      nonMatchDoc.delete();

      {
        final AtomicInteger matchCount = new AtomicInteger();
        final AtomicInteger nonMatchCount = new AtomicInteger();
        final AtomicInteger deletionCount = new AtomicInteger();

        since = database.queryFormula(selectionFormula, null,
            searchFlags, since, docClass, computeValues, new FormulaQueryCallback() {

              @Override
              public Action deletionFound(final Database db, final SearchMatch searchMatch, final TypedAccess computedValues) {
                // System.out.println("Deletion found, unid="+searchMatch.getUNID());

                deletionCount.incrementAndGet();
                return Action.Continue;
              }

              @Override
              public Action matchFound(final Database db, final SearchMatch searchMatch, final TypedAccess computedValues) {
                matchCount.incrementAndGet();

                return Action.Continue;
              }

              @Override
              public Action nonMatchFound(final Database db, final SearchMatch searchMatch, final TypedAccess computedValues) {
                nonMatchCount.incrementAndGet();
                return Action.Continue;
              }
            });

        Assertions.assertNotNull(since);
        Assertions.assertEquals(0, matchCount.get());
        Assertions.assertEquals(0, nonMatchCount.get());
        Assertions.assertEquals(1, deletionCount.get());
      }

      final Document matchDoc = database.createDocument();
      matchDoc.replaceItemValue("Form", "MatchForm");
      final String writtenRandomValue = UUID.randomUUID().toString();
      matchDoc.replaceItemValue("RandomValue", writtenRandomValue);
      matchDoc.save();
      final String matchDocUnid = matchDoc.getUNID();
      // System.out.println("Wrote match doc with UNID "+matchDocUnid);

      {
        final AtomicInteger matchCount = new AtomicInteger();
        final AtomicInteger nonMatchCount = new AtomicInteger();
        final AtomicInteger deletionCount = new AtomicInteger();

        since = database.queryFormula(selectionFormula, null,
            searchFlags, since, docClass, computeValues, new FormulaQueryCallback() {

              @Override
              public Action deletionFound(final Database db, final SearchMatch searchMatch, final TypedAccess computedValues) {
                deletionCount.incrementAndGet();
                return Action.Continue;
              }

              @Override
              public Action matchFound(final Database db, final SearchMatch searchMatch, final TypedAccess computedValues) {
                matchCount.incrementAndGet();

                // System.out.println("Match found, unid="+searchMatch.getUNID()+",
                // items="+computedValues.getItemNames());

                Assertions.assertEquals(matchDocUnid, searchMatch.getUNID());

                final String form = computedValues.get("form", String.class, "");
                Assertions.assertEquals("MatchForm", form, "Form is correct");

                final String readRandomValue = computedValues.get("RandomValue", String.class, "");
                Assertions.assertEquals(writtenRandomValue, readRandomValue, "Random value is correct");

                final DominoDateTime readCreationDate = computedValues.get("_created", DominoDateTime.class, null);
                Assertions.assertNotNull(readCreationDate);
                Assertions.assertNotNull(matchDoc.getCreated());
                Assertions.assertEquals(matchDoc.getCreated().toTemporal(), readCreationDate.toTemporal(),
                    "Creation date is correct");

                return Action.Continue;
              }

              @Override
              public Action nonMatchFound(final Database db, final SearchMatch searchMatch, final TypedAccess computedValues) {
                nonMatchCount.incrementAndGet();
                return Action.Continue;
              }
            });

        Assertions.assertNotNull(since);
        Assertions.assertEquals(1, matchCount.get());
        Assertions.assertEquals(0, nonMatchCount.get());
        Assertions.assertEquals(0, deletionCount.get());
      }

    });

  }
}
