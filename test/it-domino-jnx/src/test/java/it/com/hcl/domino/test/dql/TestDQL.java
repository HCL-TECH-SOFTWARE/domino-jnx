package it.com.hcl.domino.test.dql;

import com.hcl.domino.commons.data.DefaultDominoDateTime;
import com.hcl.domino.data.*;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.Folder;
import com.hcl.domino.dql.DQL;
import it.com.hcl.domino.test.AbstractNotesRuntimeTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.hcl.domino.dql.DQL.*;

/**
 * @author Karsakov_S created on 06.10.2021
 */

public class TestDQL extends AbstractNotesRuntimeTest {


    /**
     * TODO: figure why fail in results!
     * Assertions.assertTrue(!resultsTable.isEmpty());
     *
     * @throws Exception
     */
    @Test
    public void testDQLIn() throws Exception {
        this.withTempDb(db -> {

            DbDesign design = db.getDesign();
            Folder folder = design.createFolder("folderName");
            Folder folder2 = design.createFolder("folderName2");
            Document document = folder.getDocument();
            document.appendItemValue("lastName", "Kasparov");
            Document document2 = folder2.getDocument();
            document.appendItemValue("lastName", "Kasparov");

            document.save();
            folder.save();
            folder2.save();

            DQL.DQLTerm dqlIn = DQL.in("folderName", "folderName2");
            System.out.println("Query: " + dqlIn);
            DQLQueryResult result = db.queryDQL(dqlIn, EnumSet.of(DBQuery.VIEWREFRESH));
            Assertions.assertNotNull(result);
            final IDTable resultsTable = result.getNoteIds().get();
//            Assertions.assertTrue(!resultsTable.isEmpty());
            System.out.println(result);
        });
    }

    /**
     * TODO: figure why fail in results!
     * Assertions.assertTrue(!resultsTable.isEmpty());
     * @throws Exception
     */
    @Test
    public void testDQLInAll() throws Exception {
        this.withTempDb(db -> {

            DbDesign design = db.getDesign();
            Folder folder = design.createFolder("folder");
            Folder folder2 = design.createFolder("folder2");

            Document document = folder.getDocument();
            document.appendItemValue("lastName", "Kasparov");

            folder.save();

            Document document1 = folder2.getDocument();
            document1.appendItemValue("lastName", "Kasparov");

            folder2.save();

            DQL.DQLTerm dqlInAll = DQL.inAll("folder", "folder2");
            System.out.println("Query1: " + dqlInAll);
            DQLQueryResult result = db.queryDQL(dqlInAll, EnumSet.of(DBQuery.VIEWREFRESH));
            Assertions.assertNotNull(result);
            final IDTable resultsTable = result.getNoteIds().get();
//            Assertions.assertTrue(!resultsTable.isEmpty());
            System.out.println(result);
        });
    }

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
            Assertions.assertInstanceOf(String.class, dqlTerm.toString());
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
     * <p>
     * if you create direct query to DB like:
     * db.queryDQL("localDateTime = @dt('" + dtNow +"')", EnumSet.of(DBQuery.EXPLAIN))
     * it works okay. (row commented out below)
     * <p>
     * if you do the same query through DQL.isEqualTo (DQL:470)
     * then it fails, because JNADatabase.queryDQL (JNADatabase:2974) getting query
     * without milliseconds, like this:
     * localDateTime = @dt('2021-10-11T13:38:47Z')
     * <p>
     * if you add correct milliseconds directly in query, it will work fine. Like this:
     * localDateTime = @dt('2021-10-11T13:38:47.130Z')
     */
    @Test
    public void testDateIsLessThanOrEqualComparisonTerm() throws Exception {
        this.withTempDb(db -> {

            Instant instantNow = Instant.now();
            Date dateNow = Date.from(instantNow);
            Date dateLater = Date.from(instantNow.plus(1, ChronoUnit.SECONDS));

            Document someDoc = db.createDocument();
            someDoc.appendItemValue("date", dateNow);
            someDoc.save();

            DQL.DQLTerm isLessThanOrEqual = DQL.and(
                    DQL.item("date").isLessThanOrEqual(dateLater));
//                    DQL.item("date").isLessThanOrEqual(dateNow)); //fails!
            System.out.println("Query: " + isLessThanOrEqual);
            DQLQueryResult result = db.queryDQL(isLessThanOrEqual, EnumSet.of(DBQuery.EXPLAIN));
//            DQLQueryResult result = db.queryDQL("date = @dt('" + instantNow +"')", EnumSet.of(DBQuery.EXPLAIN));
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
//                    DQL.item("someDate").isGreaterThanOrEqual(dateNow)); //fails!
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
     * Read the comment below in TestDQL:660
     *
     * @throws Exception
     */
    @Test
    public void testTemporalAccessorIsGreaterThanOrEqualContainsTerm() throws Exception {
        this.withTempDb(db -> {
            Document someDoc = db.createDocument();
            Instant dateTimeNow = Instant.now();
            Instant dateTimeEalier = dateTimeNow.minus(1, ChronoUnit.SECONDS);
            DefaultDominoDateTime dtNow = DefaultDominoDateTime.from(dateTimeNow);
            DefaultDominoDateTime dtEalier = DefaultDominoDateTime.from(dateTimeEalier);
            someDoc.appendItemValue("localDateTime", dtNow);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("localDateTime").isGreaterThanOrEqual(dtEalier)
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
    /**
     * Equal in ...ThanOrEqual doesn't work correctly with TemporalAccessor.
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
            Instant dateTimeLater = dateTimeNow.plus(1, ChronoUnit.SECONDS);
            DefaultDominoDateTime dtNow = DefaultDominoDateTime.from(dateTimeNow);
            DefaultDominoDateTime laterDT = DefaultDominoDateTime.from(dateTimeLater);
            someDoc.appendItemValue("localDateTime", dtNow);
            someDoc.save();
            DQLQueryResult result = db.queryDQL(DQL.and(
                    DQL.item("localDateTime").isLessThanOrEqual(laterDT)
//                    DQL.item("localDateTime").isLessThanOrEqual(dtNow)
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
            Assertions.assertTrue(!resultsTable.isEmpty());
        });
    }

    @Test
    public void testNamedView() throws Exception {
        DQL.NamedView namedView = DQL.view("testView");
        DQL.NamedViewColumn viewColumn = namedView.column("viewColumn");
        NamedViewColumn viewColumn1 = namedView.column("viewColumn");
        System.out.println(viewColumn1);
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
        System.out.println(specialValueDocumentUniqueId.getName());
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
