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
package it.com.hcl.domino.test.data;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDocumentValueConversion extends AbstractNotesRuntimeTest {

  @Test
  public void testBooleanArrayRoundTrip() throws Exception {
    final boolean[] expected = new boolean[] { true, false, true };

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertEquals(Arrays.asList(true, false, true), doc.getAsList("Foo", Boolean.class, null));
      Assertions.assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));

      final List<Boolean> list = Arrays.asList(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE);
      Assertions.assertEquals(list, doc.getAsList("Foo", Boolean.class, null));
    });
  }

  @Test
  public void testBooleanRoundTrip() throws Exception {
    final boolean expected = true;

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertEquals(expected, doc.get("Foo", Boolean.class, null));
      Assertions.assertEquals(expected, doc.get("Foo", boolean.class, null));
      Assertions.assertEquals(Arrays.asList(expected), doc.getAsList("Foo", Boolean.class, null));
    });
  }

  @Test
  public void testByteArrayRoundTrip() throws Exception {
    final byte[] expected = new byte[] { 1, 2, 3 };

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));
      Assertions.assertEquals(Arrays.asList((byte) 1, (byte) 2, (byte) 3), doc.getAsList("Foo", Byte.class, null));
    });
  }

  @Test
  public void testByteRoundTrip() throws Exception {
    final byte expected = 1;

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertEquals(expected, doc.get("Foo", Byte.class, null));
      Assertions.assertEquals(expected, doc.get("Foo", byte.class, null));
      Assertions.assertEquals(Arrays.asList(expected), doc.getAsList("Foo", Byte.class, null));
    });
  }

  @Test
  public void testCharArrayRoundTrip() throws Exception {
    final char[] expected = new char[] { 'f', 'o', 'o' };

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));

      final List<Character> list = Arrays.asList('f', 'o', 'o');
      Assertions.assertEquals(list, doc.getAsList("Foo", Character.class, null));
    });
  }

  @Test
  public void testCharRoundTrip() throws Exception {
    final char expected = 'f';

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertEquals(expected, doc.get("Foo", char.class, null));
      Assertions.assertArrayEquals(new char[] { expected }, doc.get("Foo", char[].class, null));
      Assertions.assertEquals(Character.valueOf(expected), doc.get("Foo", Character.class, null));
    });
  }

  @Test
  public void testDateRoundTrip() throws Exception {
    final TemporalAccessor expected = LocalDate.of(2020, 7, 14);

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      final TemporalAccessor date = doc.get("Foo", TemporalAccessor.class, null);
      Assertions.assertEquals(LocalDate.from(expected), LocalDate.from(date));
    });
  }

  @Test
  public void testDateTimeRoundTrip() throws Exception {
    final LocalDate expectedD = LocalDate.of(2020, 7, 14);
    final LocalTime expectedT = LocalTime.of(13, 57, 01);
    final TemporalAccessor expected = OffsetDateTime.of(expectedD, expectedT, ZoneOffset.UTC);

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      final TemporalAccessor date = doc.get("Foo", TemporalAccessor.class, null);
      Assertions.assertEquals(OffsetDateTime.from(expected), OffsetDateTime.from(date));
    });
  }

  @Test
  public void testDoubleArrayRoundTrip() throws Exception {
    final double[] expected = new double[] { 1.3, 2.3, 3.3 };

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertEquals(Arrays.asList(1.3, 2.3, 3.3), doc.get("Foo", List.class, null));
      Assertions.assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));

      final List<Double> list = Arrays.stream(expected).mapToObj(Double::valueOf).collect(Collectors.toList());
      Assertions.assertEquals(list, doc.getAsList("Foo", Double.class, null));
    });
  }

  @Test
  public void testDoubleRoundTrip() throws Exception {
    final double expected = 8.3;

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertEquals(expected, doc.get("Foo", double.class, null));
      Assertions.assertArrayEquals(new double[] { expected }, doc.get("Foo", double[].class, null));
      Assertions.assertEquals(Double.valueOf(expected), doc.get("Foo", Double.class, null));
    });
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFloatArrayRoundTrip() throws Exception {
    final float[] expected = new float[] { 1.3f, 2.3f, 3.3f };

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      // Round the result for precision
      final List<Double> fetched = ((List<Double>) doc.get("Foo", List.class, null)).stream()
          .map(d -> Math.round(d * 10) / 10d)
          .collect(Collectors.toList());
      Assertions.assertEquals(Arrays.asList(1.3, 2.3, 3.3), fetched);
      Assertions.assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));
    });
  }

  @Test
  public void testFloatRoundTrip() throws Exception {
    final float expected = 8.1f;

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertEquals(expected, doc.get("Foo", float.class, null));
      Assertions.assertArrayEquals(new float[] { expected }, doc.get("Foo", float[].class, null));
      Assertions.assertEquals(Float.valueOf(expected), doc.get("Foo", Float.class, null));
    });
  }

  @Test
  public void testIntArrayRoundTrip() throws Exception {
    final int[] expected = new int[] { 1, 2, 3 };

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertEquals(Arrays.asList(1d, 2d, 3d), doc.get("Foo", List.class, null));
      Assertions.assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));

      final List<Integer> list = Arrays.stream(expected).mapToObj(Integer::valueOf).collect(Collectors.toList());
      Assertions.assertEquals(list, doc.getAsList("Foo", Integer.class, null));
    });
  }

  @Test
  public void testIntRoundTrip() throws Exception {
    final int expected = 3;

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertEquals(expected, doc.get("Foo", int.class, null));
      Assertions.assertArrayEquals(new int[] { expected }, doc.get("Foo", int[].class, null));
      Assertions.assertEquals(Integer.valueOf(expected), doc.get("Foo", Integer.class, null));
    });
  }

  @Test
  public void testLongArrayRoundTrip() throws Exception {
    final long[] expected = new long[] { 1, 2, 3 };

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertEquals(Arrays.asList(1d, 2d, 3d), doc.get("Foo", List.class, null));
      Assertions.assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));

      final List<Long> list = Arrays.stream(expected).mapToObj(Long::valueOf).collect(Collectors.toList());
      Assertions.assertEquals(list, doc.getAsList("Foo", Long.class, null));
    });
  }

  @Test
  public void testLongRoundTrip() throws Exception {
    final long expected = 8;

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertEquals(expected, doc.get("Foo", long.class, null));
      Assertions.assertArrayEquals(new long[] { expected }, doc.get("Foo", long[].class, null));
      Assertions.assertEquals(Long.valueOf(expected), doc.get("Foo", Long.class, null));
    });
  }

  @Test
  public void testShortArrayRoundTrip() throws Exception {
    final short[] expected = new short[] { 1, 2, 3 };

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));
      Assertions.assertEquals(Arrays.asList((short) 1, (short) 2, (short) 3), doc.getAsList("Foo", Short.class, null));
    });
  }

  @Test
  public void testShortRoundTrip() throws Exception {
    final short expected = 43;

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      Assertions.assertEquals(expected, doc.get("Foo", Short.class, null));
      Assertions.assertEquals(expected, doc.get("Foo", short.class, null));
      Assertions.assertEquals(Arrays.asList(expected), doc.getAsList("Foo", Short.class, null));
    });
  }

  @Test
  public void testStoreParsedDate() throws Exception {
    final TemporalAccessor expected = DateTimeFormatter.ISO_DATE.parse("2020-07-14");

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      final DominoDateTime date = doc.get("Foo", DominoDateTime.class, null);
      Assertions.assertEquals(LocalDate.from(expected), date.toLocalDate());
      Assertions.assertThrows(DateTimeException.class, () -> date.toLocalTime());
    });
  }

  @Test
  public void testStoreParsedDateTime() throws Exception {
    final TemporalAccessor expected = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2020-07-14T13:57:01-05:00");

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      final DominoDateTime date = doc.get("Foo", DominoDateTime.class, null);
      Assertions.assertEquals(OffsetDateTime.from(expected), date.toOffsetDateTime());
    });
  }

  @Test
  public void testStoreParsedTime() throws Exception {
    final TemporalAccessor expected = DateTimeFormatter.ISO_TIME.parse("13:57:01");

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      final DominoDateTime date = doc.get("Foo", DominoDateTime.class, null);
      Assertions.assertEquals(LocalTime.from(expected), date.toLocalTime());
      Assertions.assertThrows(DateTimeException.class, () -> date.toLocalDate());
    });
  }

  @Test
  public void testStringArrayRoundTrip() throws Exception {
    final String[] expected = new String[] { "foo", "bar", "baz" };
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", expected);
      Assertions.assertEquals(Arrays.asList(expected), doc.getAsList("Foo", String.class, null));
      Assertions.assertArrayEquals(expected, doc.get("Foo", String[].class, null));
      Assertions.assertEquals(Arrays.asList(expected), doc.get("Foo", List.class, null));
    });
  }

  @Test
  public void testStringRoundTrip() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "Bar");
      Assertions.assertEquals("Bar", doc.get("Foo", String.class, null));
      Assertions.assertArrayEquals(new String[] { "Bar" }, doc.get("Foo", String[].class, null));
      Assertions.assertEquals(Arrays.asList("Bar"), doc.get("Foo", List.class, null));
    });
  }

  @Test
  public void testTimeRoundTrip() throws Exception {
    final TemporalAccessor expected = LocalTime.of(13, 57, 01);

    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      doc.replaceItemValue("Foo", expected);
      final TemporalAccessor date = doc.get("Foo", TemporalAccessor.class, null);
      Assertions.assertEquals(LocalTime.from(expected), LocalTime.from(date));
    });
  }
}
