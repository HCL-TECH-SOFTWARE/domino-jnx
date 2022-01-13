package com.hcl.domino.test.dql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.hcl.domino.dql.DQL;

@SuppressWarnings("nls")
public class TestDQLGenerator {
  @Test
  public void testBasicQuery() {
    String query = DQL.item("FirstName").isEqualTo("Joe").toString();
    assertEquals("FirstName = 'Joe'", query.toString());
  }
  
  public static class TemporalProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      Instant now = Instant.now();
      Instant nowHundredths = toHundredths(now);
      LocalDate localDate = LocalDate.of(2022, 1, 5);
      LocalTime localTime = LocalTime.of(13, 11, 30, 320 * 1000 * 1000);
      LocalTime localTimeMillis = LocalTime.of(13, 11, 30, 323 * 1000 * 1000);
      OffsetDateTime dt = OffsetDateTime.of(localDate, localTime, ZoneOffset.ofHours(-5));
      ZonedDateTime zdt = toHundredths(ZonedDateTime.now());
      return Stream.of(
        Arguments.of(now, DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.ofInstant(nowHundredths, ZoneId.of("UTC")))),
        Arguments.of(nowHundredths, DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.ofInstant(nowHundredths, ZoneId.of("UTC")))),
        Arguments.of(dt, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dt)),
        Arguments.of(localDate, DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)),
        Arguments.of(localTime, DateTimeFormatter.ISO_LOCAL_TIME.format(localTime)),
        Arguments.of(localTimeMillis, DateTimeFormatter.ISO_LOCAL_TIME.format(localTime)),
        Arguments.of(zdt, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zdt))
      );
    }
  }
  
  @ParameterizedTest
  @ArgumentsSource(TemporalProvider.class)
  public void testTemportalAccessor(TemporalAccessor val, String expected) {
    String query = DQL.item("FirstName").isEqualTo(val).toString();
    assertEquals(MessageFormat.format("FirstName = @dt(''{0}'')", expected), query.toString());
  }
  
  private static Instant toHundredths(Instant val) {
    long dateValueMS = val.toEpochMilli();
    long millis = dateValueMS % 1000;
    long millisRounded = 10 * (millis / 10);
    dateValueMS -= (millis-millisRounded);
    return Instant.ofEpochMilli(dateValueMS);
  }
  
  private static ZonedDateTime toHundredths(ZonedDateTime val) {
    long dateValueMS = val.toInstant().toEpochMilli();
    long millis = dateValueMS % 1000;
    long millisRounded = 10 * (millis / 10);
    dateValueMS -= (millis-millisRounded);
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateValueMS), val.getZone());
  }
}
