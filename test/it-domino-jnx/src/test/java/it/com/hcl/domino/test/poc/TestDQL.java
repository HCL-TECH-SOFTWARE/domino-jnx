package it.com.hcl.domino.test.poc;

import com.hcl.domino.commons.data.*;
import com.hcl.domino.commons.design.*;
import com.hcl.domino.data.*;
import com.hcl.domino.design.*;
import com.hcl.domino.dql.*;
import com.hcl.domino.dxl.*;
import it.com.hcl.domino.test.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.text.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.Date;
import java.util.stream.*;

import static com.hcl.domino.dql.DQL.*;

/**
 * @author Karsakov_S created on 06.10.2021
 */

public class TestDQL extends AbstractNotesRuntimeTest {

//
//    @Test
//    @Disabled
//    public void testDQLIn() throws Exception {
//        this.withTempDb(db -> {
//
//            Document document1 = db.createDocument();
//            document1.appendItemValue("name", "Kars");
//            document1.save();
//
//            View view = new ViewImpl(document1);
//            view.setTitle("viewTitle");
//            view.setComment("viewComment");
//            Folder folder = new FolderImpl(document1);
//            folder.setTitle("folderTitle");
//            CollectionDesignElement column = folder.addColumn();
//            column.setTitle("columnTitle");
//
//
//            view.save();
//            document1.save();
////            final DxlImporter importer = this.getClient().createDxlImporter();
////            InputStream is = this.getClass().getResourceAsStream("/dxl/testDQL/DQLView.xml");
////            importer.importDxl(is, db);
////            DbDesign design = db.getDesign();
////            List<View> collect = design.getViews().collect(Collectors.toList());
////            ViewImpl viewImpl = (ViewImpl) collect.get(0);
////            DocumentSelection select = db.createDocumentSelection().select(DocumentSelection.SelectionType.VIEWS);
//            DQL.DQLTerm dqlIn = DQL.in("name");
//            DQLTerm kars = contains("Kars");
//            DQLTerm and = and(dqlIn, kars);
//            DQLQueryResult dqlQueryResult = db.queryDQL(and, EnumSet.of(DBQuery.EXPLAIN));
//            String explainText = dqlQueryResult.getExplainText();
//            System.out.println(explainText);
//        });
//    }

    @Test
    public void testDQLNot() throws Exception {
        this.withTempDb(db -> {
            AbstractNotesRuntimeTest.generateNABPersons(db, 5);
            DQL.DQLTerm dqlNot = DQL.not(
                    DQL.item("LastName").isEqualTo("Karsakov")
            );
            DQLQueryResult result = db.queryDQL(dqlNot, EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(explainText);
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
            System.out.println(result.getExplainText());
            System.out.println(result.toString());
        });
    }

    @Test
    public void testDQLAll() throws Exception {
        this.withTempDb(db -> {
            AbstractNotesRuntimeTest.generateNABPersons(db, 5);
            DQL.DQLTerm dqlAll = DQL.all();
            DQLQueryResult result = db.queryDQL(dqlAll, EnumSet.of(DBQuery.EXPLAIN));
            String explainText = result.getExplainText();
            Assertions.assertNotNull(explainText);
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
        });
    }

    @Test
    public void testDQLContains() throws Exception {
        this.withTempDb(db -> {
            Document document = db.createDocument();
            document.appendItemValue("name", "Annetikulspole");
            document.save();
            Set<FTIndex> options = new HashSet<>();
            options.add(FTIndex.STEM_INDEX);
            db.ftIndex(options);
            DQL.DQLTerm dqlContains = DQL.contains("An*e");
            DQLQueryResult result = db.queryDQL(dqlContains, EnumSet.of(DBQuery.EXPLAIN));

            String explainText = result.getExplainText();
            Assertions.assertNotNull(explainText);
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
            System.out.println(explainText);

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

            String explainText = result.getExplainText();
            Assertions.assertNotNull(explainText);
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
            System.out.println(explainText);

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

    /**
     *
     * @throws Exception
     */
    @Test
    @Disabled
    public void testDQLInAll() throws Exception {
        this.withTempDb(db -> {

            final DxlImporter importer = this.getClient().createDxlImporter();
            InputStream is = this.getClass().getResourceAsStream("/dxl/testDbDesign/view-test.xml");
            importer.importDxl(is, db);
//            Document someDoc = db.createDocument();
            DbDesign design = db.getDesign();
            View testView1 = db.getDesign().createView("TestView1");
            View testView2 = db.getDesign().createView("TestView2");
            testView1.save();
            testView2.save();
            List<View> collect = design.getViews().collect(Collectors.toList());
            DocumentSelection select = db.createDocumentSelection().select(DocumentSelection.SelectionType.VIEWS);
//            someDoc.save();
//            AbstractNotesRuntimeTest.generateNABPersons(db, 500);
            DQL.DQLTerm dqlAll = DQL.inAll("TestView1", "TestView2", "");
            DQLQueryResult result = db.queryDQL(dqlAll, EnumSet.of(DBQuery.EXPLAIN));
            String explainText = result.getExplainText();
            System.out.println(explainText);
        });
    }

    @Test
    public void TestDateLessThanComparisonTerm() throws Exception {
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
            Assertions.assertTrue(!resultsTable.isEmpty());
        });
    }

    @Test
    public void testDateGreateThanComparisonTerm() throws Exception {
        this.withTempDb(db -> {
            Instant instantNow = Instant.now();
            Date dateNow = Date.from(instantNow);
            Date dateEalier = Date.from(instantNow.minus(60, ChronoUnit.SECONDS));
            Document someDoc = db.createDocument();
            someDoc.appendItemValue("date", dateNow);
            someDoc.save();

            DQL.DQLTerm greaterLessDate = DQL.and(
                    DQL.item("date").isGreaterThan(dateEalier));
            DQL.DQLTerm and = DQL.and(greaterLessDate);
            DQLQueryResult result = db.queryDQL(and, EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();

            Assertions.assertNotNull(explainText);
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
        });
    }

    /**
     * Equal doesn't work correctly with Date.
     *
     * if you create direct query to DB like:
     * db.queryDQL("localDateTime = @dt('" + dtNow +"')", EnumSet.of(DBQuery.EXPLAIN))
     * it works okay. (row commented out below)
     *
     * if you do the same query through DQL.isEqualTo (DQL:470)
     * then it fails, because JNADatabase.queryDQL (JNADatabase:2974) getting query
     * without milliseconds, like this:
     * localDateTime = @dt('2021-10-11T13:38:47Z')
     *
     * if you add correct milliseconds directly in query, it will work fine. Like this:
     * localDateTime = @dt('2021-10-11T13:38:47.130Z')
     *
     */
    @Test
    @Disabled
    public void testDateIsLessThanOrEqualComparisonTerm() throws Exception {
        this.withTempDb(db -> {

            Instant instantNow = Instant.now();
            Date dateNow = Date.from(instantNow);
            Date dateLater = Date.from(instantNow.plus(60, ChronoUnit.SECONDS));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dateWithoutTime = sdf.parse(sdf.format(new Date()));

            Document someDoc = db.createDocument();
            someDoc.appendItemValue("date", instantNow);
            someDoc.save();

            DQL.DQLTerm isLessThanOrEqual = DQL.and(
                    DQL.item("date").isLessThanOrEqual(dateLater));
//                    DQL.item("date").isEqualTo(dateNow)); // fails
            DQLQueryResult result = db.queryDQL(isLessThanOrEqual, EnumSet.of(DBQuery.EXPLAIN));
//            DQLQueryResult result = db.queryDQL("date = @dt('" + instantNow.toString() +"')", EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            System.out.println(explainText);
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
        });
    }

    @Test
    @Disabled
    public void testDateIsGreaterThanOrEqualComparisonTerm() throws Exception {
        this.withTempDb(db -> {
            Instant instantNow = Instant.now();
            Date dateNow = Date.from(instantNow);
            Date dateEalier = Date.from(instantNow.minus(60, ChronoUnit.SECONDS));
            Document someDoc = db.createDocument();
            someDoc.appendItemValue("someDate", dateNow);
            someDoc.save();

            DQL.DQLTerm isGreaterThanOrEqual = DQL.and(
                    DQL.item("someDate").isGreaterThanOrEqual(dateEalier));
//                    DQL.item("someDate").isEqualTo(dateNow));
            DQLQueryResult result = db.queryDQL(isGreaterThanOrEqual, EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            System.out.println(explainText);
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            Assertions.assertTrue(!resultsTable.isEmpty());
        });
    }

    /**
     * Read the comment below in TestDQL:600
     * @throws Exception
     */
    @Test
    @Disabled
    public void testTemporalAccessorIsGreaterThanOrEqualContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            Instant dateTimeNow = Instant.now();
            Instant dateTimeEalier = dateTimeNow.minus(5, ChronoUnit.SECONDS);
            DefaultDominoDateTime dtNow = DefaultDominoDateTime.from(dateTimeNow);
            DefaultDominoDateTime dtEalier = DefaultDominoDateTime.from(dateTimeEalier);
            someDoc.appendItemValue("localDateTime", dtNow);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("localDateTime").isGreaterThanOrEqual(dtEalier),
                    DQL.item("localDateTime").isEqualTo(dtNow)
            ), EnumSet.of(DBQuery.EXPLAIN));
//            DQLQueryResult result = db.queryDQL("localDateTime = @dt('" + dtNow +"')", EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            System.out.println(explainText);
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            System.out.println(explainText);
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
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
            System.out.println(explainText);
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
        });
    }

    @Test
    @Disabled
    /**
     * Equal doesn't work correctly with TemporalAccessor.
     *
     * if you create direct query to DB like:
     * db.queryDQL("localDateTime = @dt('" + dtNow +"')", EnumSet.of(DBQuery.EXPLAIN))
     * it works okay. (row commented out below)
     *
     * if you do the same query through DQL.isEqualTo (DQL:470)
     * then it fails, because JNADatabase.queryDQL (JNADatabase:2974) getting query
     * without milliseconds, like this:
     * localDateTime = @dt('2021-10-11T13:38:47Z')
     *
     * if you add correct milliseconds directly in query, it will work fine. Like this:
     * localDateTime = @dt('2021-10-11T13:38:47.130Z')
     *
     */
    public void testTemporalAccessorIsLessThanOrEqualContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            Instant dateTimeNow = Instant.now();
            Instant dateTimeLater = dateTimeNow.plus(5, ChronoUnit.SECONDS);
            DefaultDominoDateTime dtNow = DefaultDominoDateTime.from(dateTimeNow);
            DefaultDominoDateTime laterDT = DefaultDominoDateTime.from(dateTimeLater);
            someDoc.appendItemValue("localDateTime", dtNow);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("localDateTime").isLessThan(laterDT),
                    DQL.item("localDateTime").isEqualTo(dtNow)
            ), EnumSet.of(DBQuery.EXPLAIN));
//            DQLQueryResult result = db.queryDQL("localDateTime = @dt('" + dtNow +"')", EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            System.out.println(explainText);
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
        });
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
            DQLQueryResult result = db.queryDQL(noteContainsTerm, EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            String explainText = result.getExplainText();
            Assertions.assertNotNull(result.getExplainText());
            Assertions.assertTrue(explainText.length() > 0);
            final IDTable resultsTable = result.getNoteIds().get();
            Assertions.assertTrue(!resultsTable.isEmpty());
        });
    }

    @Test
    public void testView() throws Exception {
        DQL.NamedView namedView = DQL.view("testView");
        DQL.NamedViewColumn viewColumn = namedView.column("viewColumn");
        System.out.println("namedView.getViewName()" + namedView.getViewName());
        System.out.println("namedView.toString() = " + namedView.toString());
        System.out.println("viewColumn.getColumnName() = " + viewColumn.getColumnName());
        System.out.println("viewColumn.toString() = " + viewColumn.toString());


//        this.withTempDb(db -> {
//            View view = db.getDesign().createView("view1");
//            view.setTitle("viewTitle");
//            view.addColumn();
//            view.setComment("viewComment");
//            view.getAliases().add("viewAliases");
//            view.save();
//
//            Set<FTIndex> options = new HashSet<>();
//            options.add(FTIndex.STEM_INDEX);
//            db.ftIndex(options);
//
//            DQL.NamedView namedView = DQL.view("testView");
//            namedView.column("viewColumn");
//
//            DQL.SpecialValue specialValue = DQL.modifiedInThisFile();
//
//        });
    }

    @Test
    public void testSpecialViewModifiedInThisFile(){
        DQL.SpecialValue specialValueModifiedInThisFile = modifiedInThisFile();
        Assertions.assertNotNull(specialValueModifiedInThisFile.getType());
        Assertions.assertNotNull(specialValueModifiedInThisFile.toString());
    }

    @Test
    public void testSpecialViewdocumentUniqueId(){
        DQL.SpecialValue specialValueDocumentUniqueId = documentUniqueId();
        Assertions.assertNotNull(specialValueDocumentUniqueId.getType());
        Assertions.assertNotNull(specialValueDocumentUniqueId.toString());
    }

    @Test
    public void testSpecialViewCreated(){
        DQL.SpecialValue specialValueCreated = created();
        Assertions.assertNotNull(specialValueCreated.getType());
        Assertions.assertNotNull(specialValueCreated.toString());
    }

    private void showResult(DQLQueryResult result) {
        List<Document> collect = result.getDocuments().collect(Collectors.toList());
                    System.out.println("*************DATA IN DB:*************");
        collect.forEach(doc -> {
                    doc.allItems().collect(Collectors.toList()).forEach(item -> {
                                System.out.println(item.getName() + " " + item.getValue());
                            }
                    );
                    System.out.println("*************************************");
                }
        );
    }
}
