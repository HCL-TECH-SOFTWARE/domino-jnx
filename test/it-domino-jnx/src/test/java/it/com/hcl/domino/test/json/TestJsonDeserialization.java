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
package it.com.hcl.domino.test.json;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.json.JsonDeserializer;
import com.hcl.domino.json.JsonDeserializerFactory;
import com.hcl.domino.misc.JNXServiceFinder;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;
import jakarta.json.Json;

@SuppressWarnings("nls")
public class TestJsonDeserialization extends AbstractNotesRuntimeTest {
  public static class DeserializerProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
      return JNXServiceFinder.findServices(JsonDeserializerFactory.class)
          .map(JsonDeserializerFactory::newDeserializer)
          .map(Arguments::of);
    }
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testArrayValues(final JsonDeserializer deserializer) throws Exception {
    final String json = "{\"foo\":[1, 2, 3],\"bar\": [\"hi\", \"there\"], \"baz\":[true, false, true]}";

    this.withTempDb(database -> {
      final Document doc = deserializer.target(database)
          .booleanValues("hi", "there")
          .fromJson(json);
      Assertions.assertEquals(Arrays.asList(1, 2, 3), doc.getAsList("foo", Integer.class, null));
      Assertions.assertEquals(Arrays.asList("hi", "there"), doc.getAsList("bar", String.class, null));
      Assertions.assertEquals(Arrays.asList("hi", "there", "hi"), doc.getAsList("baz", String.class, null));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testBooleanValues(final JsonDeserializer deserializer) throws Exception {
    final String json = "{\"foo\":true,\"bar\": false}";

    this.withTempDb(database -> {
      final Document doc = deserializer.target(database)
          .booleanValues("hi", "there")
          .fromJson(json);
      Assertions.assertEquals("hi", doc.get("foo", String.class, null));
      Assertions.assertEquals("there", doc.get("bar", String.class, null));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testCustomProcessor(final JsonDeserializer deserializerParam) throws Exception {
    deserializerParam.customProcessor("foo", (val, itemName, doc) -> doc.replaceItemValue("foo", "hey custom"));
    final String json = Json.createObjectBuilder()
        .add("foo", "heh")
        .build()
        .toString();

    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      deserializerParam.target(doc)
          .fromJson(json);
      Assertions.assertEquals("hey custom", doc.get("foo", String.class, null));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testCustomProcessorEmptyProp(final JsonDeserializer deserializerParam) {
    Assertions.assertThrows(IllegalArgumentException.class, () -> deserializerParam.customProcessor("", (val, item, doc) -> {
    }));
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testCustomProcessorNullProcessor(final JsonDeserializer deserializerParam) {
    Assertions.assertThrows(NullPointerException.class, () -> deserializerParam.customProcessor("someitem", null));
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testCustomProcessorNullProp(final JsonDeserializer deserializerParam) {
    Assertions.assertThrows(NullPointerException.class, () -> deserializerParam.customProcessor(null, (val, item, doc) -> {
    }));
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testDateTimeItems(final JsonDeserializer deserializer) throws Exception {
    final String json = "{\"foo\":\"2020-01-01\",\"bar\": \"2020-02-02\"}";

    this.withTempDb(database -> {
      final Document doc = deserializer.target(database)
          .dateTimeItems(Arrays.asList("Foo"))
          .fromJson(json);
      Assertions.assertEquals(LocalDate.of(2020, 1, 1), doc.get("foo", LocalDate.class, null));
      Assertions.assertEquals("2020-02-02", doc.getItemValue("bar").get(0));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testDateTimeItems2(final JsonDeserializer deserializer) throws Exception {
    final String json = "{\"foo\":\"2020-01-01\",\"bar\": \"2020-02-02/2020-03-03\"}";

    this.withTempDb(database -> {
      final Document doc = deserializer.target(database)
          .dateTimeItems(Arrays.asList("Foo", "Bar"))
          .fromJson(json);
      Assertions.assertEquals(LocalDate.of(2020, 1, 1), doc.get("foo", LocalDate.class, null));
      final LocalDate start = LocalDate.of(2020, 2, 2);
      final LocalDate end = LocalDate.of(2020, 3, 3);
      final DominoDateRange range = database.getParentDominoClient().createDateRange(start, end);
      Assertions.assertEquals(range, doc.getItemValue("bar").get(0));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testDeserializeExistingDocument(final JsonDeserializer deserializer) throws Exception {
    final String json = "{\"foo\":\"bar\",\"bar\": 3}";

    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Form", "Hello");
      deserializer.target(doc)
          .fromJson(json);
      Assertions.assertEquals("bar", doc.get("foo", String.class, null));
      Assertions.assertEquals(3, doc.get("bar", int.class, 0));
      Assertions.assertEquals("Hello", doc.get("Form", String.class, null));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testDeserializeExistingDocument2(final JsonDeserializer deserializer) throws Exception {
    final String json = "{\"foo\":\"bar\",\"bar\": 3}";

    this.withTempDb(database -> {
      Document doc = database.createDocument();
      doc.replaceItemValue("Form", "Hello");
      doc = deserializer.target(doc)
          .fromJson(json);
      Assertions.assertEquals("bar", doc.get("foo", String.class, null));
      Assertions.assertEquals(3, doc.get("bar", int.class, 0));
      Assertions.assertEquals("Hello", doc.get("Form", String.class, null));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testDeserializeNewDocument(final JsonDeserializer deserializer) throws Exception {
    final String json = "{\"foo\":\"bar\",\"bar\": 3}";

    this.withTempDb(database -> {
      final Document doc = deserializer.target(database)
          .fromJson(json);
      Assertions.assertEquals("bar", doc.get("foo", String.class, null));
      Assertions.assertEquals(3, doc.get("bar", int.class, 0));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testDetectDateTime(final JsonDeserializer deserializer) throws Exception {
    final String json = IOUtils.resourceToString("/json/testBasicDeserialization/datejson.json", StandardCharsets.UTF_8);
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      deserializer.target(doc)
          .detectDateTime(true)
          .fromJson(json);

      {
        Assertions.assertTrue(doc.hasItem("date"));
        final DominoDateTime date = doc.get("date", DominoDateTime.class, null);
        Assertions.assertNotEquals(null, date, "date field value should not be null");
        final Temporal temporal = date.toTemporal().orElse(null);
        Assertions.assertInstanceOf(LocalDate.class, temporal, "temporal version should be a LocalDate");
        Assertions.assertEquals(LocalDate.of(2020, 7, 10), temporal);
      }
      {
        final DominoDateTime time = doc.get("time", DominoDateTime.class, null);
        Assertions.assertNotEquals(null, time, "time field value should not be null");
        final Temporal temporal = time.toTemporal().orElse(null);
        Assertions.assertInstanceOf(LocalTime.class, temporal, "temporal version should be a LocalTime");
        Assertions.assertEquals(LocalTime.of(13, 47, 03), temporal);
      }
      {
        final DominoDateTime dt = doc.get("datetime", DominoDateTime.class, null);
        Assertions.assertNotEquals(null, dt, "datetime field value should not be null");
        final Temporal temporal = dt.toTemporal().orElse(null);
        Assertions.assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime,
            "temporal version should be an OffsetDateTime");
        final LocalDate date = LocalDate.of(2020, 7, 10);
        final LocalTime time = LocalTime.of(13, 47, 03);
        Assertions.assertEquals(OffsetDateTime.of(date, time, ZoneOffset.UTC), OffsetDateTime.from(temporal));
      }
      {
        final DominoDateTime dt = doc.get("datetime_offset", DominoDateTime.class, null);
        Assertions.assertNotEquals(null, dt, "datetime_offset field value should not be null");
        final Temporal temporal = dt.toTemporal().orElse(null);
        Assertions.assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime,
            "temporal version should be an OffsetDateTime");
        final LocalDate date = LocalDate.of(2020, 7, 10);
        final LocalTime time = LocalTime.of(13, 47, 03);
        Assertions.assertEquals(OffsetDateTime.of(date, time, ZoneOffset.ofHours(-5)), OffsetDateTime.from(temporal));
      }
      {
        final List<DominoDateTime> dts = doc.getAsList("datetimes", DominoDateTime.class, null);
        Assertions.assertNotEquals(null, dts, "datetimes field value should not be null");
        {
          final Temporal temporal = dts.get(0).toTemporal().orElse(null);
          Assertions.assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime,
              "temporal version should be an OffsetDateTime");
          final LocalDate date = LocalDate.of(2020, 7, 10);
          final LocalTime time = LocalTime.of(13, 47, 03);
          Assertions.assertEquals(OffsetDateTime.of(date, time, ZoneOffset.UTC), OffsetDateTime.from(temporal));
        }
        {
          final Temporal temporal = dts.get(1).toTemporal().orElse(null);
          Assertions.assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime,
              "temporal version should be an OffsetDateTime");
          final LocalDate date = LocalDate.of(2020, 7, 10);
          final LocalTime time = LocalTime.of(13, 47, 03);
          Assertions.assertEquals(OffsetDateTime.of(date, time, ZoneOffset.ofHours(-5)), OffsetDateTime.from(temporal));
        }
      }
    });
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testIgnoreMeta(final JsonDeserializer deserializer) throws Exception {
    final String json = "{\"foo\":true,\"bar\": false, \"@meta\": \"hi\"}";

    this.withTempDb(database -> {
      final Document doc = deserializer.target(database)
          .booleanValues("hi", "there")
          .fromJson(json);
      Assertions.assertEquals("hi", doc.get("foo", String.class, null));
      Assertions.assertEquals("there", doc.get("bar", String.class, null));
      Assertions.assertFalse(doc.hasItem("@meta"), "@meta item should have been removed");
    });
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testIllegalDateTimeItems(final JsonDeserializer deserializer) throws Exception {
    final String json = "{\"foo\":\"hi\"}";

    this.withTempDb(database -> {
      // This throws an IllegalStateException internally, but surfaces differently
      // based on implementation
      Assertions.assertThrows(RuntimeException.class, () -> deserializer.target(database)
          .dateTimeItems(Arrays.asList("Foo"))
          .fromJson(json));
    });
  }

  @Test
  public void testJsonDeserializationService() {
    Assertions.assertNotNull(JsonDeserializer.createDeserializer());
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testMixedArrayValues(final JsonDeserializer deserializer) throws Exception {
    final String json = "{\"foo\":[1, \"foo\", 3],\"bar\": [\"hi\", \"there\"], \"baz\":[true, false, true]}";

    this.withTempDb(database -> {
      Assertions.assertThrows(RuntimeException.class, () -> deserializer.target(database)
          .booleanValues("hi", "there")
          .fromJson(json));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testRemoveItems(final JsonDeserializer deserializer) throws Exception {
    final String json = "{\"foo\":\"hi\",\"bar\": \"there\"}";

    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Form", "Hello");
      doc.replaceItemValue("Baz", "hi there");
      doc.replaceItemValue("$SystemField", "tester");
      deserializer.target(doc)
          .removeMissingItems(true)
          .fromJson(json);
      Assertions.assertEquals("hi", doc.get("foo", String.class, null));
      Assertions.assertEquals("there", doc.get("bar", String.class, null));
      Assertions.assertFalse(doc.hasItem("baz"), "Baz item should have been removed");
      Assertions.assertEquals("tester", doc.get("$SystemField", String.class, null));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testSpecifyDateTime(final JsonDeserializer deserializer) throws Exception {
    final String json = IOUtils.resourceToString("/json/testBasicDeserialization/datejson.json", StandardCharsets.UTF_8);
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      deserializer.target(doc)
          .dateTimeItems(Arrays.asList("date", "time", "datetime", "datetime_offset", "datetimes"))
          .fromJson(json);

      {
        Assertions.assertTrue(doc.hasItem("date"));
        final DominoDateTime date = doc.get("date", DominoDateTime.class, null);
        Assertions.assertNotEquals(null, date, "date field value should not be null");
        final Temporal temporal = date.toTemporal().orElse(null);
        Assertions.assertInstanceOf(LocalDate.class, temporal, "temporal version should be a LocalDate");
        Assertions.assertEquals(LocalDate.of(2020, 7, 10), temporal);
      }
      {
        final DominoDateTime time = doc.get("time", DominoDateTime.class, null);
        Assertions.assertNotEquals(null, time, "time field value should not be null");
        final Temporal temporal = time.toTemporal().orElse(null);
        Assertions.assertInstanceOf(LocalTime.class, temporal, "temporal version should be a LocalTime");
        Assertions.assertEquals(LocalTime.of(13, 47, 03), temporal);
      }
      {
        final DominoDateTime dt = doc.get("datetime", DominoDateTime.class, null);
        Assertions.assertNotEquals(null, dt, "datetime field value should not be null");
        final Temporal temporal = dt.toTemporal().orElse(null);
        Assertions.assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime,
            "temporal version should be an OffsetDateTime");
        final LocalDate date = LocalDate.of(2020, 7, 10);
        final LocalTime time = LocalTime.of(13, 47, 03);
        Assertions.assertEquals(OffsetDateTime.of(date, time, ZoneOffset.UTC), OffsetDateTime.from(temporal));
      }
      {
        final DominoDateTime dt = doc.get("datetime_offset", DominoDateTime.class, null);
        Assertions.assertNotEquals(null, dt, "datetime_offset field value should not be null");
        final Temporal temporal = dt.toTemporal().orElse(null);
        Assertions.assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime,
            "temporal version should be an OffsetDateTime");
        final LocalDate date = LocalDate.of(2020, 7, 10);
        final LocalTime time = LocalTime.of(13, 47, 03);
        Assertions.assertEquals(OffsetDateTime.of(date, time, ZoneOffset.ofHours(-5)), OffsetDateTime.from(temporal));
      }
      {
        final List<DominoDateTime> dts = doc.getAsList("datetimes", DominoDateTime.class, null);
        Assertions.assertNotEquals(null, dts, "datetimes field value should not be null");
        {
          final Temporal temporal = dts.get(0).toTemporal().orElse(null);
          Assertions.assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime,
              "temporal version should be an OffsetDateTime");
          final LocalDate date = LocalDate.of(2020, 7, 10);
          final LocalTime time = LocalTime.of(13, 47, 03);
          Assertions.assertEquals(OffsetDateTime.of(date, time, ZoneOffset.UTC), OffsetDateTime.from(temporal));
        }
        {
          final Temporal temporal = dts.get(1).toTemporal().orElse(null);
          Assertions.assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime,
              "temporal version should be an OffsetDateTime");
          final LocalDate date = LocalDate.of(2020, 7, 10);
          final LocalTime time = LocalTime.of(13, 47, 03);
          Assertions.assertEquals(OffsetDateTime.of(date, time, ZoneOffset.ofHours(-5)), OffsetDateTime.from(temporal));
        }
      }
    });
  }

  @ParameterizedTest
  @ArgumentsSource(DeserializerProvider.class)
  public void testSpecifyDateTimePartial(final JsonDeserializer deserializer) throws Exception {
    final String json = IOUtils.resourceToString("/json/testBasicDeserialization/datejson.json", StandardCharsets.UTF_8);
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      deserializer.target(doc)
          .dateTimeItems(Arrays.asList("date", "datetime_offset", "datetimes"))
          .fromJson(json);

      {
        Assertions.assertTrue(doc.hasItem("date"));
        final DominoDateTime date = doc.get("date", DominoDateTime.class, null);
        Assertions.assertNotEquals(null, date, "date field value should not be null");
        final Temporal temporal = date.toTemporal().orElse(null);
        Assertions.assertInstanceOf(LocalDate.class, temporal, "temporal version should be a LocalDate");
        Assertions.assertEquals(LocalDate.of(2020, 7, 10), temporal);
      }
      {
        Assertions.assertTrue(doc.hasItem("time"));
        Assertions.assertEquals(ItemDataType.TYPE_TEXT, doc.getFirstItem("time").get().getType());
        Assertions.assertEquals("13:47:03", doc.get("time", String.class, null));
      }
      {
        Assertions.assertTrue(doc.hasItem("datetime"));
        Assertions.assertEquals(ItemDataType.TYPE_TEXT, doc.getFirstItem("datetime").get().getType());
        Assertions.assertEquals("2020-07-10T13:47:03Z", doc.get("datetime", String.class, null));
      }
      {
        final DominoDateTime dt = doc.get("datetime_offset", DominoDateTime.class, null);
        Assertions.assertNotEquals(null, dt, "datetime_offset field value should not be null");
        final Temporal temporal = dt.toTemporal().orElse(null);
        Assertions.assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime,
            "temporal version should be an OffsetDateTime");
        final LocalDate date = LocalDate.of(2020, 7, 10);
        final LocalTime time = LocalTime.of(13, 47, 03);
        Assertions.assertEquals(OffsetDateTime.of(date, time, ZoneOffset.ofHours(-5)), OffsetDateTime.from(temporal));
      }
      {
        final List<DominoDateTime> dts = doc.getAsList("datetimes", DominoDateTime.class, null);
        Assertions.assertNotEquals(null, dts, "datetimes field value should not be null");
        {
          final Temporal temporal = dts.get(0).toTemporal().orElse(null);
          Assertions.assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime,
              "temporal version should be an OffsetDateTime");
          final LocalDate date = LocalDate.of(2020, 7, 10);
          final LocalTime time = LocalTime.of(13, 47, 03);
          Assertions.assertEquals(OffsetDateTime.of(date, time, ZoneOffset.UTC), OffsetDateTime.from(temporal));
        }
        {
          final Temporal temporal = dts.get(1).toTemporal().orElse(null);
          Assertions.assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime,
              "temporal version should be an OffsetDateTime");
          final LocalDate date = LocalDate.of(2020, 7, 10);
          final LocalTime time = LocalTime.of(13, 47, 03);
          Assertions.assertEquals(OffsetDateTime.of(date, time, ZoneOffset.ofHours(-5)), OffsetDateTime.from(temporal));
        }
      }
    });
  }
}
