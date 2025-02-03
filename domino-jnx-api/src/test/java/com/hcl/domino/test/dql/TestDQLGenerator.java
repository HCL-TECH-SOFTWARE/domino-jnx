/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.domino.test.dql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
  
  @Test
  public void testEmptyFormula() {
    assertThrows(IllegalArgumentException.class, () -> DQL.formula((String)null));
    assertThrows(IllegalArgumentException.class, () -> DQL.formula(""));
  }

//  @Test
  public void testFormulas() {
    assertEquals("@formula('Foo=\"Hello\"')", DQL.formula("Foo=\"Hello\"").toString());
    assertEquals("@formula('Foo=\\'Hello\\'')", DQL.formula("Foo='Hello'").toString());
    
    assertEquals("Bar = 'Baz' and @formula('Foo=''Hello''')", DQL.and(DQL.item("Bar").isEqualTo("Baz"), DQL.formula("Foo='Hello'")).toString());
  }
  
  @Test
  public void testTermEquivalence() {
    assertEquals(DQL.item("Foo").isEqualTo("Bar"), DQL.item("Foo").isEqualTo("Bar"));
  }
  
  @Test
  public void testInAll() {
    assertEquals("part_no in all (389, 27883, 388388, 587992)", DQL.item("part_no").inAll(389, 27883, 388388, 587992).toString());
    assertEquals("part_no in all (389.1, 27883.1, 388388.1, 587992.1)", DQL.item("part_no").inAll(389.1, 27883.1, 388388.1, 587992.1).toString());
    assertEquals("part_no in all ('foo', 'bar')", DQL.item("part_no").inAll("foo", "bar").toString());
  }
  
}
