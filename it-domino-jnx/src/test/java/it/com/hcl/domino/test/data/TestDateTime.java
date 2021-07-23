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

import org.junit.jupiter.api.Assertions;
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
  public static class ComponentsProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
      final List<LocalDate> dates = new DatesProvider().provideArguments(null).map(Arguments::get).map(args -> (LocalDate) args[0])
          .collect(Collectors.toList());
      final List<LocalTime> times = new TimesProvider().provideArguments(null).map(Arguments::get).map(args -> (LocalTime) args[0])
          .collect(Collectors.toList());
      final List<ZoneId> zones = new ZonesProvider().provideArguments(null).map(Arguments::get).map(args -> (ZoneId) args[0])
          .collect(Collectors.toList());

      return dates.stream()
          .flatMap(date -> times.stream().map(time -> Arguments.of(date, time)))
          .flatMap(args -> zones.stream().map(zone -> Arguments.of(args.get()[0], args.get()[1], zone)));
    }
  }

  public static class DatesProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
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

  public static class JavaUtilDatesProvider implements ArgumentsProvider {
    @SuppressWarnings("deprecation")
    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
      return Stream.of(
          new Date(TestDateTime.epochTime),
          new Date(1900 - 1900, 1 - 1, 1, 0, 0, 0),
          new Date(1980 - 1900, 1 - 1, 1, 0, 0, 0)).map(Arguments::of);
    }
  }

  public static class TimesProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
      return Stream.of(
          LocalTime.of(16, 7, 31, 120 * 1000 * 1000),
          LocalTime.NOON,
          LocalTime.MIDNIGHT,
          // LocalTime.MAX is not representable in Domino
          LocalTime.of(23, 59, 59, 990 * 1000 * 1000)).map(Arguments::of);
    }
  }

  public static class ZonesProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
      return TestDateTime.zones.parallelStream()
          .map(Arguments::of);
    }
  }

  public static final boolean EXTRADEBUG = false;
  // Tue Mar 17 13:39:40 EDT 2020
  private static final long epochTime = OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 12, 0), ZoneOffset.UTC).toEpochSecond();
  private static final Locale locale = Locale.FRANCE;
  private static List<ZoneId> zones = ZoneId.getAvailableZoneIds().parallelStream()
      .map(ZoneId::of)
      .collect(Collectors.toList());

  @SuppressWarnings("nls")
  public static void dumpInnards(final DominoDateTime dt, final String name) {
    if (TestDateTime.EXTRADEBUG) {
      System.out.println("Innards for " + name + " (" + dt + "):");
      final int[] innards = dt.getAdapter(int[].class);
      System.out.println("\tinnards[0]=" + Integer.toHexString(innards[0]) + " - " + Integer.toBinaryString(innards[0]));
      System.out.println("\tinnards[1]=" + Integer.toHexString(innards[1]) + " - " + Integer.toBinaryString(innards[1]));
      System.out.println("\tjulian day=" + (innards[1] & 0xFFFFFF));
    }
  }

  public static boolean isExpressable(final OffsetDateTime dt) {
    final int offsetSeconds = dt.getOffset().getTotalSeconds();
    return offsetSeconds % TimeUnit.MINUTES.toSeconds(15) == 0;
  }

  public static boolean isExpressable(final ZonedDateTime dt) {
    return TestDateTime.isExpressable(dt.toOffsetDateTime());
  }

  @SuppressWarnings("nls")
  @Test
  public void testCalendarRoundTrip() throws Exception {
    this.withResourceDb("/nsf/knowndt.nsf", database -> {
      final DominoClient client = database.getParentDominoClient();

      // Database contains a known representation of "10/05/2020 01:10:07 PM EDT" (US
      // format)
      final Document knownDoc = database.getDocumentByUNID("489CC69E9C1E96CB852585F8005A812C").get();
      final DominoDateTime dt = knownDoc.get("foo", DominoDateTime.class, null);
      TestDateTime.dumpInnards(dt, "known");

      final Instant instant = Instant.parse("2020-10-05T17:10:07Z");

      // Test in UTC -> DominoDateTime
      {
        final DominoDateTime utc = client.createDateTime(ZonedDateTime.ofInstant(instant, ZoneId.of("UTC")));
        TestDateTime.dumpInnards(utc, "utc");
        Assertions.assertEquals(Instant.from(dt), Instant.from(utc));
      }
      // Test in EDT -> DominoDateTime
      {
        final DominoDateTime edt = client.createDateTime(ZonedDateTime.ofInstant(instant, ZoneId.of("America/New_York")));
        TestDateTime.dumpInnards(edt, "edt");
        Assertions.assertEquals(Instant.from(dt), Instant.from(edt));
      }

      // Represent the time in London time, since that was the observed trouble
      final ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.from(instant), ZoneId.of("Europe/London"));
      Assertions.assertEquals(Instant.from(dt), Instant.from(zdt));
      // Test the zoned variant -> DominoDateTime
      {
        final DominoDateTime midDt = client.createDateTime(zdt);
        TestDateTime.dumpInnards(midDt, "midDt");
        Assertions.assertEquals(Instant.from(dt), Instant.from(midDt));
        Assertions.assertEquals(zdt.toOffsetDateTime(), midDt.toOffsetDateTime());
      }

      // Further convert to a Calendar as in the original trouble
      final Calendar cal = GregorianCalendar.from(zdt);

      final Document doc = database.createDocument();
      doc.replaceItemValue("foo", cal);
      final DominoDateTime fromDomino = doc.get("foo", DominoDateTime.class, null);
      TestDateTime.dumpInnards(fromDomino, "fromDomino");
      Assertions.assertEquals(instant, Instant.from(fromDomino));

      client.createFormula(" @SetField('foo'; @Adjust(foo; 0; 0; 1; 0; 0; 0)) ")
          .evaluate(doc);
      final Instant fromDominoAdjust = doc.get("foo", Instant.class, null);
      final Instant instantAdjust = Instant.from(instant).plus(1, ChronoUnit.DAYS);
      Assertions.assertEquals(instantAdjust, fromDominoAdjust);
    });
  }

  // Tests known innards from
  // https://github.com/libyal/libnsfdb/blob/2e5f1ecea0ddf3788163f45dda93768451011b6f/documentation/Notes%20Storage%20Facility%20(NSF)%20database%20file%20format.asciidoc#31-nsf-date-and-time
  // Note: the Mumbai hundredths-of-seconds time appears to be wrong on GitHub, so
  // the value used below is the value of 09:19:04
  @SuppressWarnings("nls")
  @Test
  public void testDocumentedInnards() {
    final DominoClient client = this.getClient();

    {
      final ZonedDateTime zdt = ZonedDateTime
          .from(DateTimeFormatter.ISO_DATE_TIME.parse("1996-12-10T14:49:04+05:00[America/New_York]"));
      final DominoDateTime dt = client.createDateTime(zdt);
      Assertions.assertArrayEquals(new int[] { 0x006CDCC0, 0x852563FC }, dt.getAdapter(int[].class));
    }
    {
      final ZonedDateTime zdt = ZonedDateTime
          .from(DateTimeFormatter.ISO_DATE_TIME.parse("1996-12-10T14:49:04-05:30[Asia/Calcutta]"));
      final DominoDateTime dt = client.createDateTime(zdt);
      Assertions.assertArrayEquals(new int[] { 0x00332f20, 0x652563FC }, dt.getAdapter(int[].class));
    }
  }

  @Test
  public void testInvalidConversionLocalDate() {
    final DominoClient client = this.getClient();
    final DominoDateTime dt = client.createDateTime(LocalTime.now());
    Assertions.assertThrows(DateTimeException.class, dt::toLocalDate);
  }

  @Test
  public void testInvalidConversionLocalTime() {
    final DominoClient client = this.getClient();
    final DominoDateTime dt = client.createDateTime(LocalDate.now());
    Assertions.assertThrows(DateTimeException.class, dt::toLocalTime);
  }

  @Test
  public void testInvalidConversionOffsetDateTime() {
    final DominoClient client = this.getClient();
    DominoDateTime dt = client.createDateTime(LocalDate.now());
    Assertions.assertThrows(DateTimeException.class, dt::toOffsetDateTime);
    dt = client.createDateTime(LocalTime.now());
    Assertions.assertThrows(DateTimeException.class, dt::toOffsetDateTime);
  }

  @Test
  public void testRoundTripInstant() {
    final Instant instant = Instant.ofEpochMilli(TestDateTime.epochTime);

    final DominoClient client = this.getClient();
    final DominoDateTime dt = client.createDateTime(instant);
    Assertions.assertNotNull(dt);
    Assertions.assertEquals(instant, dt.toOffsetDateTime().toInstant());
  }

  @ParameterizedTest
  @ArgumentsSource(ZonesProvider.class)
  public void testRoundTripJavaCalendar(final ZoneId zone) {
    final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(zone), TestDateTime.locale);
    if (cal instanceof GregorianCalendar) {
      cal.setTimeInMillis(TestDateTime.epochTime);
      final ZonedDateTime calZoned = ((GregorianCalendar) cal).toZonedDateTime();
      if (!TestDateTime.isExpressable(calZoned)) {
        return;
      }

      final DominoClient client = this.getClient();
      final DominoDateTime dt = client.createDateTime(calZoned);
      Assertions.assertNotNull(dt);
      Assertions.assertEquals(cal.getTime(), Date.from(dt.toOffsetDateTime().toInstant()));
      Assertions.assertEquals(calZoned.getOffset().getTotalSeconds(), dt.toOffsetDateTime().getOffset().getTotalSeconds());
    }
  }

  @ParameterizedTest
  @ArgumentsSource(JavaUtilDatesProvider.class)
  public void testRoundTripJavaUtilDate(final Date date) {
    final DominoClient client = this.getClient();
    final DominoDateTime dt = client.createDateTime(date.toInstant());
    Assertions.assertNotNull(dt);
    Assertions.assertEquals(date, Date.from(dt.toOffsetDateTime().toInstant()));
    Assertions.assertTrue(dt.toOffsetDateTime().get(ChronoField.YEAR) >= 1899);
  }

  @ParameterizedTest
  @ArgumentsSource(DatesProvider.class)
  public void testRoundTripLocalDate(final LocalDate localDate) {
    final DominoClient client = this.getClient();
    final DominoDateTime dt = client.createDateTime(localDate);
    Assertions.assertNotNull(dt);
    Assertions.assertEquals(localDate, dt.toLocalDate());

    final String expectedIso = DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
    final String dominoIso = DateTimeFormatter.ISO_LOCAL_DATE.format(dt);
    Assertions.assertEquals(expectedIso, dominoIso);

    Assertions.assertEquals(2, dt.until(localDate.plus(2, ChronoUnit.DAYS), ChronoUnit.DAYS));
  }

  @ParameterizedTest
  @ArgumentsSource(TimesProvider.class)
  public void testRoundTripLocalTime(final LocalTime localTime) {
    final DominoClient client = this.getClient();
    final DominoDateTime dt = client.createDateTime(localTime);
    Assertions.assertNotNull(dt);
    Assertions.assertEquals(localTime, dt.toLocalTime());

    final String expectedIso = DateTimeFormatter.ISO_LOCAL_TIME.format(localTime);
    final String dominoIso = DateTimeFormatter.ISO_LOCAL_TIME.format(dt);
    Assertions.assertEquals(expectedIso, dominoIso);

    final long expected = localTime.until(localTime.plus(2, ChronoUnit.HOURS), ChronoUnit.HOURS);
    Assertions.assertEquals(expected, dt.until(localTime.plus(2, ChronoUnit.HOURS), ChronoUnit.HOURS));
  }

  @ParameterizedTest
  @ArgumentsSource(ZonesProvider.class)
  public void testRoundTripOffsetDateTime(final ZoneId zone) {
    final Instant instant = Instant.ofEpochMilli(TestDateTime.epochTime);
    final OffsetDateTime offsetDt = OffsetDateTime.ofInstant(instant, zone.getRules().getOffset(instant));
    if (!TestDateTime.isExpressable(offsetDt)) {
      return;
    }

    final DominoClient client = this.getClient();
    final DominoDateTime dt = client.createDateTime(offsetDt);
    Assertions.assertNotNull(dt);
    Assertions.assertEquals(offsetDt.getOffset().getTotalSeconds(), dt.toOffsetDateTime().getOffset().getTotalSeconds());
    Assertions.assertEquals(Instant.from(offsetDt), Instant.from(dt.toOffsetDateTime()));
    Assertions.assertEquals(offsetDt, dt.toOffsetDateTime());
  }

  @ParameterizedTest
  @ArgumentsSource(ComponentsProvider.class)
  public void testRoundTripOffsetDateTimes(final LocalDate localDate, final LocalTime localTime, final ZoneId zone) {
    final OffsetDateTime offsetDt = ZonedDateTime.of(localDate, localTime, zone).toOffsetDateTime();

    final DominoClient client = this.getClient();
    DominoDateTime dt;
    try {
      dt = client.createDateTime(offsetDt);
    } catch (final IllegalArgumentException e) {
      // This occurs when a historical time zone cannot be expressed in 15-minute
      // increments - ignore
      return;
    }
    Assertions.assertNotNull(dt);

    Assertions.assertEquals(offsetDt.toInstant(), dt.toOffsetDateTime().toInstant());
    Assertions.assertEquals(offsetDt.getOffset().getTotalSeconds(), dt.toOffsetDateTime().getOffset().getTotalSeconds());
    Assertions.assertEquals(offsetDt, dt.toOffsetDateTime());

    final String expectedIso = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDt);
    final String dominoIso = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dt);
    Assertions.assertEquals(expectedIso, dominoIso);

    Assertions.assertEquals(2, dt.until(offsetDt.plus(2, ChronoUnit.DAYS), ChronoUnit.DAYS));
    final long expected = offsetDt.until(offsetDt.plus(2, ChronoUnit.HOURS), ChronoUnit.HOURS);
    Assertions.assertEquals(expected, dt.until(offsetDt.plus(2, ChronoUnit.HOURS), ChronoUnit.HOURS));
  }

  @ParameterizedTest
  @ArgumentsSource(ZonesProvider.class)
  public void testRoundTripZonedDateTime(final ZoneId zone) {
    final ZonedDateTime zonedDt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(TestDateTime.epochTime), zone);
    if (!TestDateTime.isExpressable(zonedDt)) {
      return;
    }

    final DominoClient client = this.getClient();
    final DominoDateTime dt = client.createDateTime(zonedDt);
    Assertions.assertNotNull(dt);

    // The actual zone info is lost in conversion, so just check the values
    Assertions.assertEquals(zonedDt.toInstant(), dt.toOffsetDateTime().toInstant());
    Assertions.assertEquals(zonedDt.toOffsetDateTime().getOffset(), dt.toOffsetDateTime().getOffset());
    Assertions.assertEquals(zonedDt.getOffset().getTotalSeconds(), dt.toOffsetDateTime().getOffset().getTotalSeconds());
  }
}
