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
package it.com.hcl.domino.test.jakarta.json;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jnx.jsonb.DocumentJsonbDeserializer;
import com.ibm.commons.util.StringUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

@SuppressWarnings("nls")
public class TestJakartaJsonDeserialization extends AbstractNotesRuntimeTest {

  @Test
  public void testBasicDeserialization() throws Exception {
    this.withTempDb(database -> {
      final Jsonb jsonb = JsonbBuilder.newBuilder()
          .withConfig(
              new JsonbConfig()
                  .withDeserializers(
                      DocumentJsonbDeserializer.newDeserializer(database)))
          .build();

      Document doc;
      try (InputStream is = this.getClass().getResourceAsStream("/json/testBasicDeserialization/basicdoc.json")) {
        doc = jsonb.fromJson(is, Document.class);
      }

      Assertions.assertNotEquals(null, doc, "doc should not be null");
      Assertions.assertEquals("TestDocument", doc.get("Form", String.class, null));
      Assertions.assertEquals("bar", doc.get("foo", String.class, null));
      Assertions.assertEquals("baz", doc.get("bar", String.class, null));
      Assertions.assertEquals(1, doc.get("Number", int.class, 0));
      Assertions.assertEquals(null, doc.get("void", String.class, null));
      Assertions.assertEquals(true, doc.get("bool", boolean.class, null));
    });
  }

  @Test
  public void testDateDetection() throws Exception {
    this.withTempDb(database -> {
      final Jsonb jsonb = JsonbBuilder.newBuilder()
          .withConfig(
              new JsonbConfig()
                  .withDeserializers(
                      DocumentJsonbDeserializer.newBuilder(database)
                          .detectDateTime(true)
                          .build()))
          .build();

      Document doc;
      try (InputStream is = this.getClass().getResourceAsStream("/json/testBasicDeserialization/datejson.json")) {
        doc = jsonb.fromJson(is, Document.class);
      }

      Assertions.assertNotEquals(null, doc, "doc should not be null");
      Assertions.assertEquals("TestDocument", doc.get("Form", String.class, null));
      {
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

  /**
   * Tests that date-like values are left as strings by default
   *
   * @throws Exception if there is a problem running the test
   */
  @Test
  public void testNonDateDeserialization() throws Exception {
    this.withTempDb(database -> {
      final Jsonb jsonb = JsonbBuilder.newBuilder()
          .withConfig(
              new JsonbConfig()
                  .withDeserializers(
                      DocumentJsonbDeserializer.newDeserializer(database)))
          .build();

      Document doc;
      try (InputStream is = this.getClass().getResourceAsStream("/json/testBasicDeserialization/datejson.json")) {
        doc = jsonb.fromJson(is, Document.class);
      }

      Assertions.assertNotEquals(null, doc, "doc should not be null");
      Assertions.assertEquals("TestDocument", doc.get("Form", String.class, null));
      for (final String itemName : Arrays.asList("date", "time", "datetime", "datetime_offset")) {
        final Item date = doc.getFirstItem(itemName).orElse(null);
        Assertions.assertNotNull(date, itemName + " field should not be null");
        Assertions.assertEquals(ItemDataType.TYPE_TEXT, date.getType());
        Assertions.assertFalse(StringUtil.isEmpty(date.get(String.class, null)), itemName + " value should not be empty");
      }
    });
  }

}
