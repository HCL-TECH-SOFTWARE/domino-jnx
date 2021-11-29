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
 */package it.com.hcl.domino.test.dql;

import static com.hcl.domino.dql.DQL.containsAll;
import static com.hcl.domino.dql.DQL.created;
import static com.hcl.domino.dql.DQL.item;
import static com.hcl.domino.dql.DQL.modifiedInThisFile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.commons.data.DefaultDominoDateTime;
import com.hcl.domino.data.DBQuery;
import com.hcl.domino.data.DQLQueryResult;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.FTIndex;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.dql.DQL;
import com.hcl.domino.dql.DQL.DQLTerm;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

/**
 * Test cases for DQL query building and searches
 * 
 * @author Karsakov_S created on 06.10.2021
 */
@SuppressWarnings("nls")
public class TestDQL extends AbstractNotesRuntimeTest {
    private static final Logger log = Logger.getLogger(TestDQL.class.getName());
    static {
      log.setLevel(Level.OFF);
    }
    
    /**
     * TODO: figure why fail in results!
     * Assertions.assertTrue(!resultsTable.isEmpty());
     *
     * @throws Exception if there is an exception running the test
     */
    @SuppressWarnings("unused")
    @Test
    public void testDQLIn() throws Exception {
        this.withTempDb(db -> {

            //create two new folders with the design of the default view
            int folderNoteId = db.createFolder("folderName");
            int folderNoteId2 = db.createFolder("folderName2");
            
            //these don't work yet because design element creation for views and folders is still tbd:
            //DbDesign design = db.getDesign();
            //Folder folder = design.createFolder("folderName");
            //Folder folder2 = design.createFolder("folderName2");
            
            Document document1 = db.createDocument();
            document1.appendItemValue("lastName", "Kasparov");
            Document document2 = db.createDocument();
            document2.appendItemValue("lastName", "Kasparov");

            document1.save();
            document2.save();

            int noteId1 = document1.getNoteID();
            Assertions.assertTrue(noteId1!=0);
            int noteId2 = document2.getNoteID();
            Assertions.assertTrue(noteId2!=0);
            
            db.addToFolder("folderName", Arrays.asList(noteId1));
            IDTable folderIDTable1 = db.getIDTableForFolder("folderName", true);
            int[] folderNoteIds = folderIDTable1.toIntArray();
            Assertions.assertTrue(folderNoteIds.length>0);
            
            db.addToFolder("folderName2", Arrays.asList(noteId2));
            IDTable folderIDTable2 = db.getIDTableForFolder("folderName2", true);
            int[] folderNoteIds2 = folderIDTable2.toIntArray();
            Assertions.assertTrue(folderNoteIds2.length>0);
            
            DQL.DQLTerm dqlIn = DQL.in("folderName", "folderName2");
            log.log(Level.INFO, "Query: " + dqlIn);
            DQLQueryResult result = db.queryDQL(dqlIn, EnumSet.of(DBQuery.VIEWREFRESH));
            Assertions.assertNotNull(result);
            final IDTable resultsTable = result.getNoteIds().get();
            
            Assertions.assertTrue(resultsTable.size()==2);
            Assertions.assertTrue(resultsTable.contains(noteId1));
            Assertions.assertTrue(resultsTable.contains(noteId2));
            
            log.log(Level.INFO, String.valueOf(result));
        });
    }

    /**
     * TODO: figure why fail in results!
     * Assertions.assertTrue(!resultsTable.isEmpty());
     * @throws Exception if there is an exception running the test
     */
    @SuppressWarnings("unused")
    @Test
    public void testDQLInAll() throws Exception {
        this.withTempDb(db -> {
            //create two new folders with the design of the default view
            int folderNoteId = db.createFolder("folder1");
            int folderNoteId2 = db.createFolder("folder2");

            //these don't work yet because design element creation for views and folders is still tbd:
            //DbDesign design = db.getDesign();
            //Folder folder = design.createFolder("folder");
            //Folder folder2 = design.createFolder("folder2");

            Document document1 = db.createDocument();
            document1.appendItemValue("lastName", "Kasparov");

            Document document2 = db.createDocument();
            document2.appendItemValue("lastName", "Kasparov");
            
            document1.save();
            document2.save();

            int noteId1 = document1.getNoteID();
            Assertions.assertTrue(noteId1!=0);
            int noteId2 = document2.getNoteID();
            Assertions.assertTrue(noteId2!=0);

            db.addToFolder("folder1", Arrays.asList(noteId1));
            db.addToFolder("folder2", Arrays.asList(noteId1));
            
            db.addToFolder("folder2", Arrays.asList(noteId2));
            
            DQL.DQLTerm dqlInAll = DQL.inAll("folder1", "folder2");
            log.log(Level.INFO, "Query1: " + dqlInAll);
            DQLQueryResult result = db.queryDQL(dqlInAll, EnumSet.of(DBQuery.VIEWREFRESH));
            Assertions.assertNotNull(result);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1);
            Assertions.assertTrue(resultsTable.contains(noteId1));
            
            log.log(Level.INFO, String.valueOf(result));
        });
    }

    @Test
    public void testDQLNot() throws Exception {
        this.withTempDb(db -> {
            final int nrOfDocs = 5;
            AbstractNotesRuntimeTest.generateNABPersons(db, nrOfDocs);
            DQL.DQLTerm dqlNot = DQL.not(
                    DQL.item("LastName").isEqualTo("Karsakov")
            );
            DQLQueryResult result = db.queryDQL(dqlNot, EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(explainText);
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==nrOfDocs);
            log.log(Level.INFO, result.getExplainText());
            log.log(Level.INFO, result.toString());
        });
    }

    @Test
    public void testDQLAll() throws Exception {
        this.withTempDb(db -> {
            final int nrOfDocs = 5;
            AbstractNotesRuntimeTest.generateNABPersons(db, nrOfDocs);
            DQL.DQLTerm dqlAll = DQL.all();
            DQLQueryResult result = db.queryDQL(dqlAll, EnumSet.of(DBQuery.EXPLAIN));
            String explainText = result.getExplainText();
            Assertions.assertNotNull(explainText);
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==nrOfDocs);
        });
    }

    @Test
    public void testDQLContains() throws Exception {
        this.withTempDb(db -> {
            Document document = db.createDocument();
            document.appendItemValue("firstName", "Annetikulspole");
            document.appendItemValue("lastName", "Dnetikulspole");
            document.save();
            Set<FTIndex> options = new HashSet<>();
            options.add(FTIndex.STEM_INDEX);
            db.ftIndex(options);
            DQL.DQLTerm dqlContains = DQL.contains("An*le", "Dn*e");
            DQLQueryResult result = db.queryDQL(dqlContains, EnumSet.of(DBQuery.EXPLAIN));
            Assertions.assertInstanceOf(String.class, dqlContains.toString());
            String explainText = result.getExplainText();
            Assertions.assertNotNull(explainText);
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==document.getNoteID());
            log.log(Level.INFO, explainText);
        });
    }

    @Test
    public void testDQLContainsAll() throws Exception {
        this.withTempDb(db -> {
            Document document = db.createDocument();
            document.appendItemValue("firstname", "Annetikulspole");
            document.appendItemValue("lastname", "Faerhdarhsaere45");
            document.save();
            Set<FTIndex> options = new HashSet<>();
            options.add(FTIndex.STEM_INDEX);
            db.ftIndex(options);
            DQLTerm dqlTerm = containsAll("An*e", "Fae*5");
            DQLQueryResult result = db.queryDQL(dqlTerm, EnumSet.of(DBQuery.EXPLAIN));
            Assertions.assertInstanceOf(String.class, dqlTerm.toString());
            String explainText = result.getExplainText();
            Assertions.assertNotNull(explainText);
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==document.getNoteID());
            log.log(Level.INFO, explainText);
        });
    }

    @Test
    public void testDQLAnd() throws Exception {
        this.withTempDb(db -> {
            AbstractNotesRuntimeTest.generateNABPersons(db, 10);
            DQL.DQLTerm dqlAnd = DQL.and(
                    DQL.item("LastName").isGreaterThan("A"),
                    DQL.item("FirstName").isGreaterThan("A"));
            DQLQueryResult result = db.queryDQL(dqlAnd, EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(explainText);
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
        });
    }

    @Test
    public void testDateLessThanComparisonTerm() throws Exception {
        this.withTempDb(db -> {
            Instant instantNow = Instant.now();
            Date dateNow = Date.from(instantNow);
            Date dateLater = Date.from(instantNow.plus(60, ChronoUnit.SECONDS));
            Document someDoc = db.createDocument();
            someDoc.appendItemValue("date", dateNow);
            someDoc.save();

            DQL.DQLTerm greaterLessDate = DQL.and(
                    DQL.item("date").isLessThan(dateLater));
            DQL.DQLTerm and = DQL.and(greaterLessDate);
            DQLQueryResult result = db.queryDQL(and, EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();

            Assertions.assertNotNull(explainText);
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testDateGreateThanComparisonTerm() throws Exception {
        this.withTempDb(db -> {
            Instant instantNow = Instant.now();
            Date dateNow = Date.from(instantNow);
            Date dateEarlier = Date.from(instantNow.minus(60, ChronoUnit.SECONDS));
            Document someDoc = db.createDocument();
            someDoc.appendItemValue("date", dateNow);
            someDoc.save();

            DQL.DQLTerm greaterLessDate = DQL.and(
                    DQL.item("date").isGreaterThan(dateEarlier));
            DQL.DQLTerm and = DQL.and(greaterLessDate);
            DQLQueryResult result = db.queryDQL(and, EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();

            Assertions.assertNotNull(explainText);
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testDateIsLessThanOrEqualComparisonTerm() throws Exception {
        this.withTempDb(db -> {

            Instant instantNow = Instant.now();
            Date dateNow = Date.from(instantNow);

            Document someDoc = db.createDocument();
            someDoc.appendItemValue("date", dateNow);
            someDoc.save();

            DQL.DQLTerm isLessThanOrEqual = DQL.and(
                    DQL.item("date").isLessThanOrEqual(dateNow));
            log.log(Level.INFO, "Query: " + isLessThanOrEqual);
            DQLQueryResult result = db.queryDQL(isLessThanOrEqual, EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            log.log(Level.INFO, explainText);
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testDateIsGreaterThanOrEqualComparisonTerm() throws Exception {
        this.withTempDb(db -> {
            Instant instantNow = Instant.now();
            Date dateNow = Date.from(instantNow);
            Document someDoc = db.createDocument();
            someDoc.appendItemValue("someDate", dateNow);
            someDoc.save();

            DQL.DQLTerm isGreaterThanOrEqual = DQL.and(
                    DQL.item("someDate").isGreaterThanOrEqual(dateNow));
            DQLQueryResult result = db.queryDQL(isGreaterThanOrEqual, EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testDoubleIsEqualToContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            double val = 0.22;
            someDoc.appendItemValue("val", val);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(DQL.item("val").isEqualTo(val)), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testDoubleIsGreaterThanContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            double val = 0.22;
            someDoc.appendItemValue("val", val);
            someDoc.save();
            val = val - 0.01;
            DQLQueryResult result = db.queryDQL(DQL.and(DQL.item("val").isGreaterThan(val)), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testDoubleIsLessThanContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            double val = 0.22;
            someDoc.appendItemValue("val", val);
            someDoc.save();
            val += 0.01;
            DQLQueryResult result = db.queryDQL(DQL.and(DQL.item("val").isLessThan(val)), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testDoubleIsLessThanOrEqualsContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            double val = 0.22;
            someDoc.appendItemValue("val", val);
            val += 0.01;
            someDoc.appendItemValue("val2", val);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("val").isLessThanOrEqual(val),
                    DQL.item("val2").isLessThanOrEqual(val)
            ), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            log.log(Level.INFO, explainText);
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testDoubleIsGreaterThanOrEqualsContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            double val = 0.22;
            someDoc.appendItemValue("val", val);
            val -= 0.01;
            someDoc.appendItemValue("val2", val);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("val").isGreaterThanOrEqual(val),
                    DQL.item("val2").isGreaterThanOrEqual(val)
            ), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testIntIsEqualToContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            int val = 123;
            someDoc.appendItemValue("val", val);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("val").isEqualTo(val)
            ), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testIntIsGreaterThanOrEqualContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            int val = 123;
            someDoc.appendItemValue("val", val);
            val -= 1;
            someDoc.appendItemValue("val2", val);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("val").isGreaterThanOrEqual(val),
                    DQL.item("val2").isGreaterThanOrEqual(val)
            ), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testIntIsLessThanOrEqualContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            int val = 123;
            someDoc.appendItemValue("val", val);
            val += 1;
            someDoc.appendItemValue("val2", val);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("val").isLessThanOrEqual(val),
                    DQL.item("val2").isLessThanOrEqual(val)
            ), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testIntIsLessThanContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            int val = 123;
            someDoc.appendItemValue("val", val);
            someDoc.save();
            val += 1;
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("val").isLessThan(val)
            ), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testIntIsGreaterThanContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            int val = 123;
            someDoc.appendItemValue("val", val);
            someDoc.save();
            val -= 1;
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("val").isGreaterThan(val)
            ), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testStringIsGreaterThanContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            String val = "Stephan";
            someDoc.appendItemValue("name", val);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("name").isGreaterThan("Ss")
            ), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testStringIsLessThanOrEqualContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            String val = "Stephan";
            someDoc.appendItemValue("name", val);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("name").isLessThanOrEqual("Stephan"),
                    DQL.item("name").isLessThanOrEqual("StephanK")
            ), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testStringIsLessThanContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            String val = "Stephan";
            someDoc.appendItemValue("name", val);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("name").isLessThan("StephanK")
            ), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testStringIsGreaterThanOrEqualContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            String val = "Stephan";
            someDoc.appendItemValue("name", val);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("name").isGreaterThanOrEqual("Stepha"),
                    DQL.item("name").isGreaterThanOrEqual("Stephan")
            ), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testTemporalAccessorIsGreaterThanOrEqualContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            Instant dateTimeNow = Instant.now();
            DefaultDominoDateTime dtNow = DefaultDominoDateTime.from(dateTimeNow);
            someDoc.appendItemValue("localDateTime", dtNow);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.item("localDateTime").isGreaterThanOrEqual(dtNow),
                EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            log.log(Level.INFO, explainText);
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testTemporalAccessorIsGreaterThanContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            Instant dateTimeNow = Instant.now();
            Instant dateTimeEalier = dateTimeNow.minus(5, ChronoUnit.SECONDS);
            DefaultDominoDateTime dtNow = DefaultDominoDateTime.from(dateTimeNow);
            DefaultDominoDateTime earlierDT = DefaultDominoDateTime.from(dateTimeEalier);
            someDoc.appendItemValue("localDateTime", dtNow);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.item("localDateTime").isGreaterThan(earlierDT), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            log.log(Level.INFO, explainText);
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testTemporalAccessorIsLessThanContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            Instant dateTimeNow = Instant.now();
            Instant dateTimeLater = dateTimeNow.plus(5, ChronoUnit.SECONDS);
            DefaultDominoDateTime dtNow = DefaultDominoDateTime.from(dateTimeNow);
            DefaultDominoDateTime laterDT = DefaultDominoDateTime.from(dateTimeLater);
            someDoc.appendItemValue("localDateTime", dtNow);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.item("localDateTime").isLessThan(laterDT), EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            log.log(Level.INFO, explainText);
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    public void testTemporalAccessorIsLessThanOrEqualContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            Instant dateTimeNow = Instant.now();
            Instant dateTimeLater = dateTimeNow.plus(1, ChronoUnit.SECONDS);
            DefaultDominoDateTime dtNow = DefaultDominoDateTime.from(dateTimeNow);
            System.out.println("dateTimeNow="+dateTimeNow);
            System.out.println("dtNow="+dtNow);
            
            someDoc.appendItemValue("localDateTime", dtNow);
            someDoc.save();
            
            DQLQueryResult result = db.queryDQL(DQL.item("localDateTime").isLessThanOrEqual(dtNow),
                EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            log.log(Level.INFO, explainText);
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testAndTermException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            DQL.and(new DQLTerm[0]);
        }, "And arguments value is empty");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            DQL.and(null);
        }, "And arguments value is empty");
    }

    @Test
    public void testOrTermException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            DQL.or(new DQLTerm[0]);
        }, "And arguments value is empty");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            DQL.or(null);
        }, "And arguments value is empty");
    }

    @Test
    public void testInException() {
        double[] doubleValues = new double[0];
        int[] intValues = new int[0];
        String[] strValues = new String[0];
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                DQL.item("").in(intValues)
        );
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                DQL.item("").in(doubleValues)
        );
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                DQL.item("").in(strValues)
        );
    }

    @Test
    public void testNoteContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            someDoc.appendItemValue("Name", "Stephan");
            someDoc.save();

            Set<FTIndex> options = new HashSet<>();
            options.add(FTIndex.STEM_INDEX);
            db.ftIndex(options);

            DQL.NoteContainsTerm noteContainsTerm = new DQL.NoteContainsTerm(true, "Stephan");
            Assertions.assertInstanceOf(String.class, noteContainsTerm.toString());
            DQLQueryResult result = db.queryDQL(noteContainsTerm, EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(resultsTable.size()==1 && resultsTable.iterator().next()==someDoc.getNoteID());
        });
    }

    @Test
    public void testSpecialViewModifiedInThisFile() {
        DQL.SpecialValue specialValueModifiedInThisFile = modifiedInThisFile();
        Assertions.assertNotNull(specialValueModifiedInThisFile.getType());
        Assertions.assertNotNull(specialValueModifiedInThisFile.toString());
    }

    @Test
    public void testSpecialViewdocumentUniqueId() {
        DQL.NamedItem specialValueDocumentUniqueId = item("");
        log.log(Level.INFO, specialValueDocumentUniqueId.getName());
        Assertions.assertNotNull(specialValueDocumentUniqueId.getName());
        Assertions.assertNotNull(specialValueDocumentUniqueId.toString());
    }

    @Test
    public void testItemThrowIllegalArgumentException() {
        DQL.NamedItem specialValue1 = item(" name");
        DQL.NamedItem specialValue2 = item("'name'");
        Assertions.assertThrows(IllegalArgumentException.class, specialValue1::toString);
        Assertions.assertThrows(IllegalArgumentException.class, specialValue2::toString);
    }

    @Test
    public void testSpecialViewCreated() {
        DQL.SpecialValue specialValueCreated = created();
        Assertions.assertNotNull(specialValueCreated.getType());
        Assertions.assertNotNull(specialValueCreated.toString());
    }

    private void showResult(DQLQueryResult result) {
        StringBuilder sb = new StringBuilder();
      
        List<Document> collect = result.getDocuments().collect(Collectors.toList());
        sb.append("\n*************DATA IN DB:*************\n");
        collect.forEach(doc -> {
                    doc.allItems().forEach(item -> {
                                sb.append(item.getName() + "=" + item.getValue() + "\n");
                            }
                    );
                    sb.append("*************************************\n");
                }
        );
        log.log(Level.INFO, sb.toString());
    }
    
    @Test
    public void testDQLSearch() throws Exception {
      this.withTempDb(database -> {
        AbstractNotesRuntimeTest.generateNABPersons(database, 500);

        {
          final DQLQueryResult result = database
              .queryDQL(
                  DQL.or(
                      DQL.item("Firstname").isEqualTo("Alexa"),
                      DQL.item("Firstname").isEqualTo("Carlos")),
                  EnumSet.of(DBQuery.EXPLAIN));

          final String explainTxt = result.getExplainText();

          Assertions.assertNotNull(explainTxt);
          Assertions.assertTrue(explainTxt.length() > 0);

          final IDTable resultsTable = result.getNoteIds().get();
          Assertions.assertTrue(!resultsTable.isEmpty());
        }
      });

    }
}
