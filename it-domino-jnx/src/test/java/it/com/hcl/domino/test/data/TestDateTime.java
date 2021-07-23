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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestDateTime extends AbstractNotesRuntimeTest {
	public static final boolean EXTRADEBUG = false;

	// Tue Mar 17 13:39:40 EDT 2020
	private static final long epochTime = OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 12, 0), ZoneOffset.UTC).toEpochSecond();
	private static final Locale locale = Locale.FRANCE;
	private static List<ZoneId> zones = ZoneId.getAvailableZoneIds().parallelStream()
			.map(ZoneId::of)
			.collect(Collectors.toList());
	
	public static class DatesProvider implements ArgumentsProvider {
		@Override public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(
					LocalDate.of(2020, 6, 9),  // summer
					LocalDate.of(2020, 1, 1),  // new year's
					LocalDate.of(2020, 3, 10), // spring, near DST switch
					LocalDate.of(2019, 10, 31), // Halloween
					LocalDate.of(1900, 1, 1) // pre-epoch
				)
				.map(Arguments::of);
		}
	}
	public static class TimesProvider implements ArgumentsProvider {
		@Override public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(
					LocalTime.of(16, 7, 31, 120 * 1000 * 1000), 
					LocalTime.NOON,
					LocalTime.MIDNIGHT,
					// LocalTime.MAX is not representable in Domino
					LocalTime.of(23, 59, 59, 990 * 1000 * 1000)
				).map(Arguments::of);
		}
	}
	public static class ZonesProvider implements ArgumentsProvider {
		@Override public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return zones.parallelStream()
				.map(Arguments::of);
		}
	}
	public static class ComponentsProvider implements ArgumentsProvider {
		@Override public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			List<LocalDate> dates = new DatesProvider().provideArguments(null).map(Arguments::get).map(args -> (LocalDate)args[0]).collect(Collectors.toList());
			List<LocalTime> times = new TimesProvider().provideArguments(null).map(Arguments::get).map(args -> (LocalTime)args[0]).collect(Collectors.toList());
			List<ZoneId> zones = new ZonesProvider().provideArguments(null).map(Arguments::get).map(args -> (ZoneId)args[0]).collect(Collectors.toList());
			
			return dates.stream()
				.flatMap(date -> times.stream().map(time -> Arguments.of(date, time)))
				.flatMap(args -> zones.stream().map(zone -> Arguments.of(args.get()[0], args.get()[1], zone)));
		}	
	}
	public static class JavaUtilDatesProvider implements ArgumentsProvider {
		@SuppressWarnings("deprecation")
		@Override public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(
				new Date(epochTime),
				new Date(1900-1900, 1-1, 1, 0, 0, 0),
				new Date(1980-1900, 1-1, 1, 0, 0, 0)
			).map(Arguments::of);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(JavaUtilDatesProvider.class)
	public void testRoundTripJavaUtilDate(Date date) {
		DominoClient client = getClient();
		DominoDateTime dt = client.createDateTime(date.toInstant());
		assertNotNull(dt);
		assertEquals(date, Date.from(dt.toOffsetDateTime().toInstant()));
		assertTrue(dt.toOffsetDateTime().get(ChronoField.YEAR) >= 1899);
	}

	@ParameterizedTest
	@ArgumentsSource(ZonesProvider.class)
	public void testRoundTripJavaCalendar(ZoneId zone) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(zone), locale);
		if(cal instanceof GregorianCalendar) {
			cal.setTimeInMillis(epochTime);
			ZonedDateTime calZoned = ((GregorianCalendar)cal).toZonedDateTime();
			if(!isExpressable(calZoned)) {
				return;
			}
	
			DominoClient client = getClient();
			DominoDateTime dt = client.createDateTime(calZoned);
			assertNotNull(dt);
			assertEquals(cal.getTime(), Date.from(dt.toOffsetDateTime().toInstant()));
			assertEquals(calZoned.getOffset().getTotalSeconds(), dt.toOffsetDateTime().getOffset().getTotalSeconds());
		}
	}
	
	@Test
	public void testRoundTripInstant() {
		Instant instant = Instant.ofEpochMilli(epochTime);
		
		DominoClient client = getClient();
		DominoDateTime dt = client.createDateTime(instant);
		assertNotNull(dt);
		assertEquals(instant, dt.toOffsetDateTime().toInstant());
	}

	@ParameterizedTest
	@ArgumentsSource(ZonesProvider.class)
	public void testRoundTripZonedDateTime(ZoneId zone) {
		ZonedDateTime zonedDt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochTime), zone);
		if(!isExpressable(zonedDt)) {
			return;
		}

		DominoClient client = getClient();
		DominoDateTime dt = client.createDateTime(zonedDt);
		assertNotNull(dt);
		
		// The actual zone info is lost in conversion, so just check the values
		assertEquals(zonedDt.toInstant(), dt.toOffsetDateTime().toInstant());
		assertEquals(zonedDt.toOffsetDateTime().getOffset(), dt.toOffsetDateTime().getOffset());
		assertEquals(zonedDt.getOffset().getTotalSeconds(), dt.toOffsetDateTime().getOffset().getTotalSeconds());
	}
	
	@ParameterizedTest
	@ArgumentsSource(ZonesProvider.class)
	public void testRoundTripOffsetDateTime(ZoneId zone) {
		Instant instant = Instant.ofEpochMilli(epochTime);
		OffsetDateTime offsetDt = OffsetDateTime.ofInstant(instant, zone.getRules().getOffset(instant));
		if(!isExpressable(offsetDt)) {
			return;
		}

		DominoClient client = getClient();
		DominoDateTime dt = client.createDateTime(offsetDt);
		assertNotNull(dt);
		assertEquals(offsetDt.getOffset().getTotalSeconds(), dt.toOffsetDateTime().getOffset().getTotalSeconds());
		assertEquals(Instant.from(offsetDt), Instant.from(dt.toOffsetDateTime()));
		assertEquals(offsetDt, dt.toOffsetDateTime());
	}
	
	@ParameterizedTest
	@ArgumentsSource(ComponentsProvider.class)
	public void testRoundTripOffsetDateTimes(LocalDate localDate, LocalTime localTime, ZoneId zone) {
		OffsetDateTime offsetDt = ZonedDateTime.of(localDate, localTime, zone).toOffsetDateTime();

		DominoClient client = getClient();
		DominoDateTime dt;
		try {
			dt = client.createDateTime(offsetDt);
		} catch(IllegalArgumentException e) {
			// This occurs when a historical time zone cannot be expressed in 15-minute increments - ignore
			return;
		}
		assertNotNull(dt);
		
		assertEquals(offsetDt.toInstant(), dt.toOffsetDateTime().toInstant());
		assertEquals(offsetDt.getOffset().getTotalSeconds(), dt.toOffsetDateTime().getOffset().getTotalSeconds());
		assertEquals(offsetDt, dt.toOffsetDateTime());
		
		String expectedIso = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDt);
		String dominoIso = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dt);
		assertEquals(expectedIso, dominoIso);

		assertEquals(2, dt.until(offsetDt.plus(2, ChronoUnit.DAYS), ChronoUnit.DAYS));
		long expected = offsetDt.until(offsetDt.plus(2, ChronoUnit.HOURS), ChronoUnit.HOURS);
		assertEquals(expected, dt.until(offsetDt.plus(2, ChronoUnit.HOURS), ChronoUnit.HOURS));
	}

	@ParameterizedTest
	@ArgumentsSource(DatesProvider.class)
	public void testRoundTripLocalDate(LocalDate localDate) {
		DominoClient client = getClient();
		DominoDateTime dt = client.createDateTime(localDate);
		assertNotNull(dt);
		assertEquals(localDate, dt.toLocalDate());
		
		String expectedIso = DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
		String dominoIso = DateTimeFormatter.ISO_LOCAL_DATE.format(dt);
		assertEquals(expectedIso, dominoIso);
		
		assertEquals(2, dt.until(localDate.plus(2, ChronoUnit.DAYS), ChronoUnit.DAYS));
	}

	@ParameterizedTest
	@ArgumentsSource(TimesProvider.class)
	public void testRoundTripLocalTime(LocalTime localTime) {
		DominoClient client = getClient();
		DominoDateTime dt = client.createDateTime(localTime);
		assertNotNull(dt);
		assertEquals(localTime, dt.toLocalTime());
		
		String expectedIso = DateTimeFormatter.ISO_LOCAL_TIME.format(localTime);
		String dominoIso = DateTimeFormatter.ISO_LOCAL_TIME.format(dt);
		assertEquals(expectedIso, dominoIso);

		long expected = localTime.until(localTime.plus(2, ChronoUnit.HOURS), ChronoUnit.HOURS);
		assertEquals(expected, dt.until(localTime.plus(2, ChronoUnit.HOURS), ChronoUnit.HOURS));
	}

	@Test
	public void testInvalidConversionOffsetDateTime() {
		DominoClient client = getClient();
		DominoDateTime dt = client.createDateTime(LocalDate.now());
		assertThrows(DateTimeException.class, dt::toOffsetDateTime);
		dt = client.createDateTime(LocalTime.now());
		assertThrows(DateTimeException.class, dt::toOffsetDateTime);
	}

	@Test
	public void testInvalidConversionLocalTime() {
		DominoClient client = getClient();
		DominoDateTime dt = client.createDateTime(LocalDate.now());
		assertThrows(DateTimeException.class, dt::toLocalTime);
	}

	@Test
	public void testInvalidConversionLocalDate() {
		DominoClient client = getClient();
		DominoDateTime dt = client.createDateTime(LocalTime.now());
		assertThrows(DateTimeException.class, dt::toLocalDate);
	}
	
	// Tests known innards from
	//    https://github.com/libyal/libnsfdb/blob/2e5f1ecea0ddf3788163f45dda93768451011b6f/documentation/Notes%20Storage%20Facility%20(NSF)%20database%20file%20format.asciidoc#31-nsf-date-and-time
	// Note: the Mumbai hundredths-of-seconds time appears to be wrong on GitHub, so the value used below is the value of 09:19:04
	@SuppressWarnings("nls")
	@Test
	public void testDocumentedInnards() {
		DominoClient client = getClient();
		
		{
			ZonedDateTime zdt = ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse("1996-12-10T14:49:04+05:00[America/New_York]"));
			DominoDateTime dt = client.createDateTime(zdt);
			assertArrayEquals(new int[] { 0x006CDCC0, 0x852563FC }, dt.getAdapter(int[].class));
		}
		{
			ZonedDateTime zdt = ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse("1996-12-10T14:49:04-05:30[Asia/Calcutta]"));
			DominoDateTime dt = client.createDateTime(zdt);
			assertArrayEquals(new int[] { 0x00332f20, 0x652563FC }, dt.getAdapter(int[].class));
		}
	}
	
	@SuppressWarnings("nls")
	@Test
	public void testCalendarRoundTrip() throws Exception {
		withResourceDb("/nsf/knowndt.nsf", database -> {
			DominoClient client = database.getParentDominoClient();
			
			// Database contains a known representation of "10/05/2020 01:10:07 PM EDT" (US format)
			Document knownDoc = database.getDocumentByUNID("489CC69E9C1E96CB852585F8005A812C").get();
			DominoDateTime dt = knownDoc.get("foo", DominoDateTime.class, null);
			dumpInnards(dt, "known");
			
			Instant instant = Instant.parse("2020-10-05T17:10:07Z");
			
			// Test in UTC -> DominoDateTime
			{
				DominoDateTime utc = client.createDateTime(ZonedDateTime.ofInstant(instant, ZoneId.of("UTC")));
				dumpInnards(utc, "utc");
				assertEquals(Instant.from(dt), Instant.from(utc));
			}
			// Test in EDT -> DominoDateTime
			{
				DominoDateTime edt = client.createDateTime(ZonedDateTime.ofInstant(instant, ZoneId.of("America/New_York")));
				dumpInnards(edt, "edt");
				assertEquals(Instant.from(dt), Instant.from(edt));
			}
			
			// Represent the time in London time, since that was the observed trouble
			ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.from(instant), ZoneId.of("Europe/London"));
			assertEquals(Instant.from(dt), Instant.from(zdt));
			// Test the zoned variant -> DominoDateTime
			{
				DominoDateTime midDt = client.createDateTime(zdt);
				dumpInnards(midDt, "midDt");
				assertEquals(Instant.from(dt), Instant.from(midDt));
				assertEquals(zdt.toOffsetDateTime(), midDt.toOffsetDateTime());
			}
			
			// Further convert to a Calendar as in the original trouble
			Calendar cal = GregorianCalendar.from(zdt);
	
			Document doc = database.createDocument();
			doc.replaceItemValue("foo", cal);
			DominoDateTime fromDomino = doc.get("foo", DominoDateTime.class, null);
			dumpInnards(fromDomino, "fromDomino");
			assertEquals(instant, Instant.from(fromDomino));
			
			client.createFormula(" @SetField('foo'; @Adjust(foo; 0; 0; 1; 0; 0; 0)) ")
				.evaluate(doc);
			Instant fromDominoAdjust = doc.get("foo", Instant.class, null);
			Instant instantAdjust = Instant.from(instant).plus(1, ChronoUnit.DAYS);
			assertEquals(instantAdjust, fromDominoAdjust);
		});
	}
	
	@SuppressWarnings("nls")
	public static void dumpInnards(DominoDateTime dt, String name) {
		if(EXTRADEBUG) {
			System.out.println("Innards for " + name + " (" + dt + "):");
			int[] innards = dt.getAdapter(int[].class);
			System.out.println("\tinnards[0]=" + Integer.toHexString(innards[0]) + " - " + Integer.toBinaryString(innards[0]));
			System.out.println("\tinnards[1]=" + Integer.toHexString(innards[1]) + " - " + Integer.toBinaryString(innards[1]));
			System.out.println("\tjulian day=" + (innards[1] & 0xFFFFFF));
		}
	}
	
	public static boolean isExpressable(ZonedDateTime dt) {
		return isExpressable(dt.toOffsetDateTime());
	}
	public static boolean isExpressable(OffsetDateTime dt) {
		int offsetSeconds = dt.getOffset().getTotalSeconds();
		return offsetSeconds % TimeUnit.MINUTES.toSeconds(15) == 0;
	}
}
