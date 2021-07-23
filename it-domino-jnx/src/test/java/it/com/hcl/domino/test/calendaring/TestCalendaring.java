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
package it.com.hcl.domino.test.calendaring;

import java.io.StringWriter;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.calendar.CalendarDocumentOpen;
import com.hcl.domino.calendar.CalendarRead;
import com.hcl.domino.calendar.CalendarReadRange;
import com.hcl.domino.calendar.CalendarWrite;
import com.hcl.domino.data.Document;
import com.hcl.domino.exception.ImplicitScheduleFailedException;
import com.hcl.domino.misc.Pair;

@SuppressWarnings("nls")
public class TestCalendaring extends AbstractCalendaringTest {
  private static final DateTimeFormatter ICAL_DATETIME_FORMAT_UTC = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

  final Logger log = Logger.getLogger(this.getClass().getName());

  @Test
  public void testCreateCalendarEntry() {
    try {
      this.withICalImport("/ical/testCreateCalendarEntry", (db, entries) -> {
        for (final ImportedCalEntry entry : entries) {
          final EnumSet<CalendarRead> readFlags = EnumSet.of(
              CalendarRead.HIDE_X_LOTUS);

          final String uid = entry.getUid();

          final String iCal = this.getCalendaring().readCalendarEntry(db, uid, null, readFlags);

          Assertions.assertNotNull(iCal, "Data should not be null when reading created entry");
          Assertions.assertNotEquals("", iCal, "Data should not be empty when reading created entry");

          AbstractCalendaringTest.assertEqualsICal(entry.getSource(), iCal, "Original source should not differ from data");

          final EnumSet<CalendarDocumentOpen> openFlags = EnumSet.of(
              CalendarDocumentOpen.HANDLE_NOSPLIT);

          // verify if created document can be found
          final Document doc = this.getCalendaring().openCalendarEntryDocument(db, uid, null, openFlags);

          Assertions.assertNotNull(doc, "Document should not be null when reading created entry");

          // and has the correct uid
          final String docUID = this.getCalendaring().getUIDfromDocument(doc);

          Assertions.assertNotNull(docUID, "UID of document should not be null");
          Assertions.assertNotEquals("", docUID, "Empty uid returned from document");

          Assertions.assertEquals(uid, docUID, "Original uid should not differ from document uid");

          // and also, if the documents note-id can be used to find the same uid
          final String noteUID = this.getCalendaring().getUIDfromNoteID(db, doc.getNoteID());

          Assertions.assertNotNull(noteUID, "UID acquired via noteid should not be null");
          Assertions.assertNotEquals("", noteUID, "Empty uid acquired via noteid");

          Assertions.assertEquals(uid, noteUID, "Original uid should not differ from uid acquired via noteid");

          // as well as the documents unid can be used to find the same uid

          // first verify that the UNID of the document matches the apptunid
          final String apptUnid = this.getCalendaring().getApptUnidFromUID(uid);

          Assertions.assertNotNull(apptUnid, "ApptUnid should not be null");
          Assertions.assertNotEquals("", apptUnid, "Empty ApptUnid");

          final String docUnid = doc.getUNID();

          Assertions.assertEquals(docUnid, apptUnid, "Document UNID and ApptUnid not matching");

          final String unidUID = this.getCalendaring().getUIDFromUNID(db, docUnid);

          Assertions.assertNotNull(unidUID, "UID acquired via unid should not be null");
          Assertions.assertNotEquals("", unidUID, "Empty uid acquired via unid");

          Assertions.assertEquals(uid, unidUID, "Original uid should not differ from uid acquired via unid");

          doc.delete();
        }
      });
    } catch (final Exception e) {
      Assertions.fail("Error creating calendar entry", e);
    }
  }

  @Test
  public void testReadRange() {
    try {
      final String mailServerName = this.getMailServer();
      final String mailTemplate = this.getMailTemplate();

      this.withTempDbFromTemplate(mailServerName, mailTemplate, tempDb -> {
        this.withICalSource("/ical/testReadRange", name -> (name.indexOf("entry_tpl.ical") != -1), sourceTpl -> {
          if (sourceTpl.indexOf("{ISO_START}") == -1 || sourceTpl.indexOf("{ISO_END}") == -1) {
            Assertions.fail("Invalid template found for range test: Expected both tags {ISO_START} and {ISO_END}");
          }

          // first create several new entries, one every few day
          final int entryCount = 200;
          final int dayIncrement = 8;
          final ZoneId utcZone = ZoneId.of("UTC");

          final ZonedDateTime initialStart = LocalDateTime.parse("20220927T113000Z", TestCalendaring.ICAL_DATETIME_FORMAT_UTC)
              .atZone(utcZone);
          final ZonedDateTime initialEnd = LocalDateTime.parse("20220927T123000Z", TestCalendaring.ICAL_DATETIME_FORMAT_UTC)
              .atZone(utcZone);

          ZonedDateTime start = initialStart;
          ZonedDateTime end = initialEnd;

          final EnumSet<CalendarWrite> writeFlags = EnumSet.of(
              CalendarWrite.IGNORE_VERIFY_DB,
              CalendarWrite.DISABLE_IMPLICIT_SCHEDULING);

          String entrySource;
          final List<ICal> entryICal = new ArrayList<>();
          final List<String> createdUIDs = new ArrayList<>();
          final List<Pair<ZonedDateTime, ZonedDateTime>> createdEntriesRange = new ArrayList<>();
          for (int i = 1; i <= entryCount; i++) {
            entrySource = sourceTpl.replace("{SUMMARY}", "Entry " + i);

            entrySource = entrySource.replace("{ISO_START}", start.format(TestCalendaring.ICAL_DATETIME_FORMAT_UTC));
            entrySource = entrySource.replace("{ISO_END}", start.format(TestCalendaring.ICAL_DATETIME_FORMAT_UTC));

            try {
              createdUIDs.add(
                  this.getClient().getCalendaring().createCalendarEntry(tempDb, entrySource, writeFlags));
              entryICal.add(AbstractCalendaringTest.parseICal(entrySource));
              createdEntriesRange.add(new Pair<>(start, end));
            } catch (final ImplicitScheduleFailedException e) {
              // Occurs in the Docker builder, which isn't surprising
              this.log.info(MessageFormat.format("Aborting calendaring-tests: {0}", e.getMessage()));

              return;
            }

            start = start.plusDays(dayIncrement);
            end = end.plusDays(dayIncrement);
          }

          // check if we can find the proper amount of entries within the complete range
          final EnumSet<CalendarReadRange> dataToRead = EnumSet.of(
              CalendarReadRange.DTSTART,
              CalendarReadRange.DTEND,
              CalendarReadRange.SUMMARY);

          final StringWriter retICal = new StringWriter();
          final List<String> retUIDs = new ArrayList<>();

          this.getCalendaring().readRange(tempDb, initialStart.minusSeconds(1), end, 0, Integer.MAX_VALUE, dataToRead, retICal,
              retUIDs);

          Assertions.assertEquals(entryCount, retUIDs.size(), "Too few entries have been found");
          Assertions.assertArrayEquals(createdUIDs.toArray(new String[createdUIDs.size()]),
              retUIDs.toArray(new String[retUIDs.size()]), "Not all created entries have been found");

          // check if we find the matching entry for each range
          for (int i = 0; i < createdEntriesRange.size(); i++) {
            final Pair<ZonedDateTime, ZonedDateTime> range = createdEntriesRange.get(i);

            retUIDs.clear();
            retICal.getBuffer().setLength(0);
            this.getCalendaring().readRange(tempDb, range.getValue1().minusSeconds(1), range.getValue2(), 0, Integer.MAX_VALUE,
                dataToRead, retICal, retUIDs);

            Assertions.assertEquals(retUIDs.size(), 1, "Entry at index not found: " + i);
            Assertions.assertEquals(retUIDs.get(0), createdUIDs.get(i), "Wrong entry found at index: " + i);

            // verify if the summary-field matches
            final Optional<ICalProperty> exptectedSumProp = entryICal.get(i).stream().filter((prop) -> {
              return prop.getName().equals("SUMMARY");
            }).findAny();

            if (exptectedSumProp.isPresent()) {
              final Optional<ICalProperty> actualSummaryProp = AbstractCalendaringTest.parseICal(retICal.toString()).stream()
                  .filter((prop) -> {
                    return prop.getName().equals("SUMMARY");
                  }).findAny();

              Assertions.assertTrue(actualSummaryProp.isPresent(), "SUMMARY property not found on range-query for index " + i);

              Assertions.assertEquals(exptectedSumProp.get().getValue(), actualSummaryProp.get().getValue(),
                  "SUMMARY property not matching on range-query for index " + i);
            }
          }

          // now validate the paging within the results
          final int entriesToRead = 5;
          int skipCount = 0;
          final List<String> totalReadUIDs = new ArrayList<>();

          do {
            retUIDs.clear();
            retICal.getBuffer().setLength(0);

            this.getCalendaring().readRange(tempDb, initialStart.minusSeconds(1), end, skipCount, entriesToRead, dataToRead,
                retICal, retUIDs);

            totalReadUIDs.addAll(retUIDs);

            skipCount += retUIDs.size();
          } while (retUIDs.size() > 0);

          Assertions.assertEquals(entryCount, totalReadUIDs.size(), "Too few entries have been found while paging");
          Assertions.assertArrayEquals(createdUIDs.toArray(new String[createdUIDs.size()]),
              totalReadUIDs.toArray(new String[totalReadUIDs.size()]), "Not all created entries have been found while paging");

        });
      });
    } catch (final Exception e) {
      Assertions.fail("Error reading calendar range", e);
    }
  }

  @Test
  public void testUpdateCalendarEntry() {
    try {
      final String mailServerName = this.getMailServer();
      final String mailTemplate = this.getMailTemplate();

      this.withTempDbFromTemplate(mailServerName, mailTemplate, tempDb -> {
        this.withICalImport(tempDb, "/ical/testUpdateCalendarEntry", name -> (name.indexOf("entry1.ical") != -1), null,
            (db, entry1) -> {
              final EnumSet<CalendarRead> readFlags = EnumSet.of(
                  CalendarRead.HIDE_X_LOTUS);

              final String uid = entry1[0].getUid();

              Assertions.assertNotNull(uid, "UID should not be null");
              Assertions.assertNotEquals("", uid, "Empty uid returned");

              final String iCal1 = this.getCalendaring().readCalendarEntry(db, uid, null, readFlags);

              this.withICalSource("/ical/testUpdateCalendarEntry", name -> (name.indexOf("entry2.ical") != -1), source -> {
                AbstractCalendaringTest.assertNotEqualsICal(iCal1, source, "entry1.ical and entry2.ical need to differ");

                if (source.indexOf("{UID}") == -1) {
                  Assertions.fail("Invalid template found for range test: Expected tag {UID}");
                }
                final String sourceWithUID = source.replace("{UID}", uid);

                final EnumSet<CalendarWrite> writeFlags = EnumSet.of(
                    CalendarWrite.IGNORE_VERIFY_DB,
                    CalendarWrite.DISABLE_IMPLICIT_SCHEDULING);

                this.getCalendaring().updateCalendarEntry(db, sourceWithUID, uid, null, null, writeFlags);

                final String iCal2 = this.getCalendaring().readCalendarEntry(db, uid, null, readFlags);

                Assertions.assertNotEquals(iCal1, iCal2, "Entry not updated");
                AbstractCalendaringTest.assertNotEqualsICal(iCal2, sourceWithUID, "Original source should not differ from data");

              });
            });
      });
    } catch (final Exception e) {
      Assertions.fail("Error updating calendar entry", e);
    }
  }
}
