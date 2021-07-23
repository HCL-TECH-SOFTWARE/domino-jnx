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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
	final Logger log = Logger.getLogger(getClass().getName());
	
	private static final DateTimeFormatter ICAL_DATETIME_FORMAT_UTC = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
	
	@Test
	public void testCreateCalendarEntry() {
		try {
			withICalImport("/ical/testCreateCalendarEntry", (db, entries) -> {
				for (ImportedCalEntry entry:entries) {
					EnumSet<CalendarRead> readFlags = EnumSet.of(
						CalendarRead.HIDE_X_LOTUS
					);
					
					String uid=entry.getUid();
					
					String iCal=getCalendaring().readCalendarEntry(db, uid, null, readFlags);
					
					assertNotNull(iCal, "Data should not be null when reading created entry");
					assertNotEquals("", iCal, "Data should not be empty when reading created entry");
					
					assertEqualsICal(entry.getSource(), iCal, "Original source should not differ from data");
					
					EnumSet<CalendarDocumentOpen> openFlags = EnumSet.of(
						CalendarDocumentOpen.HANDLE_NOSPLIT
					);
					
					// verify if created document can be found
					Document doc=getCalendaring().openCalendarEntryDocument(db, uid, null, openFlags);
					
					assertNotNull(doc, "Document should not be null when reading created entry");
					
					// and has the correct uid
					String docUID = getCalendaring().getUIDfromDocument(doc);
					
					assertNotNull(docUID, "UID of document should not be null");
					assertNotEquals("", docUID, "Empty uid returned from document");
					
					assertEquals(uid, docUID, "Original uid should not differ from document uid");
					
					// and also, if the documents note-id can be used to find the same uid
					String noteUID = getCalendaring().getUIDfromNoteID(db, doc.getNoteID());
					
					assertNotNull(noteUID, "UID acquired via noteid should not be null");
					assertNotEquals("", noteUID, "Empty uid acquired via noteid");
					
					assertEquals(uid, noteUID, "Original uid should not differ from uid acquired via noteid");
					
					// as well as the documents unid can be used to find the same uid
					
					// first verify that the UNID of the document matches the apptunid
					String apptUnid=getCalendaring().getApptUnidFromUID(uid);
					
					assertNotNull(apptUnid, "ApptUnid should not be null");
					assertNotEquals("", apptUnid, "Empty ApptUnid");

					
					String docUnid=doc.getUNID();
					
					assertEquals(docUnid, apptUnid, "Document UNID and ApptUnid not matching");
					
					String unidUID = getCalendaring().getUIDFromUNID(db, docUnid);
					
					assertNotNull(unidUID, "UID acquired via unid should not be null");
					assertNotEquals("", unidUID, "Empty uid acquired via unid");
										
					assertEquals(uid, unidUID, "Original uid should not differ from uid acquired via unid");

					doc.delete();
				}
			});
		}
		catch (Exception e) {
			fail("Error creating calendar entry", e);
		}
	}
	
	@Test
	public void testUpdateCalendarEntry() {
		try {
			String mailServerName = getMailServer();
			String mailTemplate = getMailTemplate();
			
			withTempDbFromTemplate(mailServerName, mailTemplate, tempDb -> {
				withICalImport(tempDb, "/ical/testUpdateCalendarEntry", (name) -> {
					return name.indexOf("entry1.ical")!=-1;
				}, null, (db, entry1) -> {
					EnumSet<CalendarRead> readFlags = EnumSet.of(
							CalendarRead.HIDE_X_LOTUS
						);
					
					String uid=entry1[0].getUid();
					
					assertNotNull(uid, "UID should not be null");
					assertNotEquals("", uid, "Empty uid returned");

					
					String iCal1=getCalendaring().readCalendarEntry(db, uid, null, readFlags);
					
					withICalSource("/ical/testUpdateCalendarEntry", (name) -> {
						return name.indexOf("entry2.ical")!=-1;
					}, (source) -> {
						assertNotEqualsICal(iCal1, source, "entry1.ical and entry2.ical need to differ");
						
						if (source.indexOf("{UID}")==-1 ) {
							fail("Invalid template found for range test: Expected tag {UID}");
						}
						String sourceWithUID = source.replace("{UID}", uid);
						
						EnumSet<CalendarWrite> writeFlags = EnumSet.of(
							CalendarWrite.IGNORE_VERIFY_DB,
							CalendarWrite.DISABLE_IMPLICIT_SCHEDULING
						);
						
						getCalendaring().updateCalendarEntry(db, sourceWithUID, uid, null, null, writeFlags);
						
						String iCal2=getCalendaring().readCalendarEntry(db, uid, null, readFlags);
						
						assertNotEquals(iCal1, iCal2, "Entry not updated");
						assertNotEqualsICal(iCal2, sourceWithUID, "Original source should not differ from data");
						
					});
				});
			});
		}
		catch (Exception e) {
			fail("Error updating calendar entry", e);
		}
	}
	
	@Test
	public void testReadRange() {
		try {
			String mailServerName = getMailServer();
			String mailTemplate = getMailTemplate();
			
			withTempDbFromTemplate(mailServerName, mailTemplate, tempDb -> {
				withICalSource("/ical/testReadRange", (name) -> {
					return name.indexOf("entry_tpl.ical")!=-1;
				}, (sourceTpl) -> {
					if (sourceTpl.indexOf("{ISO_START}")==-1 || sourceTpl.indexOf("{ISO_END}")==-1) {
						fail("Invalid template found for range test: Expected both tags {ISO_START} and {ISO_END}");
					}
					
					// first create several new entries, one every few day
					int entryCount = 200;
					int dayIncrement = 8;
					ZoneId utcZone = ZoneId.of("UTC");
					
					ZonedDateTime initialStart = LocalDateTime.parse("20220927T113000Z", ICAL_DATETIME_FORMAT_UTC).atZone(utcZone);
					ZonedDateTime initialEnd = LocalDateTime.parse("20220927T123000Z", ICAL_DATETIME_FORMAT_UTC).atZone(utcZone);
					
					ZonedDateTime start = initialStart;
					ZonedDateTime end = initialEnd;
					
					EnumSet<CalendarWrite> writeFlags = EnumSet.of(
							CalendarWrite.IGNORE_VERIFY_DB,
							CalendarWrite.DISABLE_IMPLICIT_SCHEDULING
						);
					
					String entrySource;
					List<ICal> entryICal=new ArrayList<ICal>();
					List<String> createdUIDs=new ArrayList<String>();
					List<Pair<ZonedDateTime,ZonedDateTime>> createdEntriesRange=new ArrayList<Pair<ZonedDateTime,ZonedDateTime>>();
					for (int i = 1; i<=entryCount; i++) {
						entrySource = sourceTpl.replace("{SUMMARY}", "Entry " + i);
						
						entrySource = entrySource.replace("{ISO_START}", start.format(ICAL_DATETIME_FORMAT_UTC));
						entrySource = entrySource.replace("{ISO_END}", start.format(ICAL_DATETIME_FORMAT_UTC));
						
						try {
							createdUIDs.add(
								getClient().getCalendaring().createCalendarEntry(tempDb, entrySource, writeFlags)
							);
							entryICal.add(parseICal(entrySource));
							createdEntriesRange.add(new Pair<ZonedDateTime,ZonedDateTime>(start, end));
						} catch(ImplicitScheduleFailedException e) {
							// Occurs in the Docker builder, which isn't surprising
							log.info(MessageFormat.format("Aborting calendaring-tests: {0}", e.getMessage()));
							
							return;
						}

						start = start.plusDays(dayIncrement);
						end = end.plusDays(dayIncrement);
					}
					
					// check if we can find the proper amount of entries within the complete range
					EnumSet<CalendarReadRange> dataToRead = EnumSet.of(
						CalendarReadRange.DTSTART,
						CalendarReadRange.DTEND,
						CalendarReadRange.SUMMARY);
					
					StringWriter retICal = new StringWriter();
					List<String> retUIDs = new ArrayList<String>();
					
					getCalendaring().readRange(tempDb, initialStart.minusSeconds(1), end, 0, Integer.MAX_VALUE, dataToRead, retICal, retUIDs);
				
					assertEquals(entryCount, retUIDs.size(), "Too few entries have been found");
					assertArrayEquals(createdUIDs.toArray(new String[createdUIDs.size()]), retUIDs.toArray(new String[retUIDs.size()]), "Not all created entries have been found");
				
					// check if we find the matching entry for each range
					for (int i = 0; i<createdEntriesRange.size(); i++) {
						Pair<ZonedDateTime,ZonedDateTime> range=createdEntriesRange.get(i);
					
						retUIDs.clear();
						retICal.getBuffer().setLength(0);
						getCalendaring().readRange(tempDb, range.getValue1().minusSeconds(1), range.getValue2(), 0, Integer.MAX_VALUE, dataToRead, retICal, retUIDs);
						
 						assertEquals(retUIDs.size(), 1, "Entry at index not found: " + i);
						assertEquals(retUIDs.get(0), createdUIDs.get(i), "Wrong entry found at index: " + i);

						// verify if the summary-field matches
						Optional<ICalProperty> exptectedSumProp = entryICal.get(i).stream().filter((prop) -> {
 							return prop.getName().equals("SUMMARY");
 						}).findAny();
						
						if (exptectedSumProp.isPresent()) {
	 						Optional<ICalProperty> actualSummaryProp = parseICal(retICal.toString()).stream().filter((prop) -> {
	 							return prop.getName().equals("SUMMARY");
	 						}).findAny();

	 						assertTrue(actualSummaryProp.isPresent(), "SUMMARY property not found on range-query for index " + i);
	 						
	 						assertEquals(exptectedSumProp.get().getValue(), actualSummaryProp.get().getValue(), "SUMMARY property not matching on range-query for index " + i);
						}
					}
					
					// now validate the paging within the results
					int entriesToRead = 5;
					int skipCount = 0;
					List<String> totalReadUIDs=new ArrayList<String>();
					
					do {
						retUIDs.clear();
						retICal.getBuffer().setLength(0);
						
						getCalendaring().readRange(tempDb, initialStart.minusSeconds(1), end, skipCount, entriesToRead, dataToRead, retICal, retUIDs);
						
						totalReadUIDs.addAll(retUIDs);
						
						skipCount+=retUIDs.size();
					} while (retUIDs.size()>0);
					
					assertEquals(entryCount, totalReadUIDs.size(), "Too few entries have been found while paging");
					assertArrayEquals(createdUIDs.toArray(new String[createdUIDs.size()]), totalReadUIDs.toArray(new String[totalReadUIDs.size()]), "Not all created entries have been found while paging");

				});
			});
		}
		catch (Exception e) {
			fail("Error reading calendar range", e);
		}
	}
}
