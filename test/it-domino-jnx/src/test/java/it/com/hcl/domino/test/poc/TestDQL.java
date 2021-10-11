package it.com.hcl.domino.test.poc;

import com.hcl.domino.commons.data.*;
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

/**
 * @author Karsakov_S created on 06.10.2021
 */

public class TestDQL extends AbstractNotesRuntimeTest {


    @Test
    @Disabled
    public void testDQLIn() throws Exception {
        this.withTempDb(db -> {
            final DxlImporter importer = this.getClient().createDxlImporter();
            InputStream is = this.getClass().getResourceAsStream("/dxl/testDQL/DQLView.xml");
            importer.importDxl(is, db);
            DbDesign design = db.getDesign();
            List<View> collect = design.getViews().collect(Collectors.toList());
            DocumentSelection select = db.createDocumentSelection().select(DocumentSelection.SelectionType.VIEWS);
//            AbstractNotesRuntimeTest.generateNABPersons(db, 500);
            DQL.DQLTerm dqlIn = DQL.in("");
            DQLQueryResult dqlQueryResult = db.queryDQL(dqlIn, EnumSet.of(DBQuery.EXPLAIN));
            String explainText = dqlQueryResult.getExplainText();

        });
    }


    @Test
    public void testDQLNot() throws Exception {
        this.withTempDb(db -> {
            AbstractNotesRuntimeTest.generateNABPersons(db, 500);
            DQL.DQLTerm dqlNot = DQL.not(
                    DQL.or(DQL.item("LastName").isGreaterThan("B"),
                            DQL.item("FirstName").isGreaterThan("B"))
            );
            DQLQueryResult result = db.queryDQL(dqlNot, EnumSet.of(DBQuery.EXPLAIN));
            showResult(result);
            System.out.println(result.getExplainText());
            System.out.println(result.toString());
        });
    }

    @Test
    public void testDQLAll() throws Exception {
        this.withTempDb(db -> {
            AbstractNotesRuntimeTest.generateNABPersons(db, 500);
            DQL.DQLTerm dqlAll = DQL.all();
            DQLQueryResult result = db.queryDQL(dqlAll, EnumSet.of(DBQuery.EXPLAIN));
            System.out.println(result.getExplainText());
            System.out.println(result.toString());
        });
    }

    @Test
    @Disabled
    public void testDQLContains() throws Exception {
        this.withTempDb(db -> {
            AbstractNotesRuntimeTest.generateNABPersons(db, 500);
            DQL.DQLTerm dqlContains = DQL.contains("A*");
            DQLQueryResult result = db.queryDQL(dqlContains, EnumSet.of(DBQuery.EXPLAIN));
            String explainText = result.getExplainText();
            System.out.println(explainText);

        });
    }

    @Test
    public void testDQLAnd() throws Exception {
        this.withTempDb(db -> {

            AbstractNotesRuntimeTest.generateNABPersons(db, 500);


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

//    @Test
//    @Disabled
//    public void testDQLInAll() throws Exception {
//        this.withTempDb(db -> {
//
//            final DxlImporter importer = this.getClient().createDxlImporter();
//            InputStream is = this.getClass().getResourceAsStream("/dxl/testPoc/views.xml");
//            importer.importDxl(is, db);
////            Document someDoc = db.createDocument();
//            DbDesign design = db.getDesign();
//            View asdf = db.getDesign().createView("asdf");
//            asdf.save();
//            List<View> collect = design.getViews().collect(Collectors.toList());
//            DocumentSelection select = db.createDocumentSelection().select(DocumentSelection.SelectionType.VIEWS);
////            someDoc.save();
////            AbstractNotesRuntimeTest.generateNABPersons(db, 500);
//            DQL.DQLTerm dqlAll = DQL.inAll("tmp");
//            DQLQueryResult result = db.queryDQL(dqlAll, EnumSet.of(DBQuery.EXPLAIN));
//            String explainText = result.getExplainText();
//            System.out.println(explainText);
//        });
//    }

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
    public void TestDateGreateThanComparisonTerm() throws Exception {
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
    public void TestDateIsLessThanOrEqualComparisonTerm() throws Exception {
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
                    DQL.item("date").isLessThanOrEqual(dateNow),
                    DQL.item("date").isEqualTo(dateNow)); // fails
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
    public void TestDateIsGreaterThanOrEqualComparisonTerm() throws Exception {
        this.withTempDb(db -> {
            Date nowDate = Date.from(Instant.now());
            Date olderDate = Date.from(Instant.now());
            olderDate.setYear(1000);

            Document someDoc = db.createDocument();
            Document someDoc2 = db.createDocument();
            someDoc.appendItemValue("someDate", olderDate);
            someDoc.appendItemValue("name", "Stephan");

            someDoc2.appendItemValue("someDate2", nowDate);
            someDoc2.appendItemValue("name", "Karsakov");
            someDoc.save();
            someDoc2.save();

            System.out.println("---------------Date in DB:-------------");
            Set<FTIndex> options = new HashSet<>();
            options.add(FTIndex.STEM_INDEX);
            db.ftIndex(options);

            DQL.NoteContainsTerm noteContainsTerm = new DQL.NoteContainsTerm(true, "Karsakov");
            DQLQueryResult result0 = db.queryDQL(noteContainsTerm, EnumSet.of(DBQuery.EXPLAIN));
            showResult(result0);
            System.out.println("---------------------------------------");

            DQL.DQLTerm isGreaterThanOrEqual = DQL.and(
                    DQL.item("someDate").isGreaterThanOrEqual(nowDate),
                    DQL.item("someDate2").isEqualTo(nowDate));
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
    public void TestDoubleIsEqualToContainsTerm() throws Exception {
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
    public void TestDoubleIsGreaterThanContainsTerm() throws Exception {
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
    public void TestDoubleIsLessThanContainsTerm() throws Exception {
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
    public void TestDoubleIsLessThanOrEqualsContainsTerm() throws Exception {
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
    public void TestDoubleIsGreaterThanOrEqualsContainsTerm() throws Exception {
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
    public void TestIntIsEqualToContainsTerm() throws Exception {
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
    public void TestIntIsGreaterThanOrEqualContainsTerm() throws Exception {
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
    public void TestIntIsLessThanOrEqualContainsTerm() throws Exception {
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
    public void TestIntIsLessThanContainsTerm() throws Exception {
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
    public void TestIntIsGreaterThanContainsTerm() throws Exception {
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
    public void TestStringIsGreaterThanContainsTerm() throws Exception {
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
    public void TestStringIsLessThanOrEqualContainsTerm() throws Exception {
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
    public void TestStringIsLessThanContainsTerm() throws Exception {
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
    public void TestStringIsGreaterThanOrEqualContainsTerm() throws Exception {
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
    public void TestTemporalAccessorIsGreaterThanOrEqualContainsTerm() throws Exception {
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
    public void TestTemporalAccessorIsGreaterThanContainsTerm() throws Exception {
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
    public void TestTemporalAccessorIsLessThanContainsTerm() throws Exception {
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
    public void TestTemporalAccessorIsLessThanOrEqualContainsTerm() throws Exception {
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
    public void TestNoteContainsTerm() throws Exception {
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
