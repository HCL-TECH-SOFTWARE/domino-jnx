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
package it.com.hcl.domino.test.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
	@Test
	public void testJsonDeserializationService() {
		assertNotNull(JsonDeserializer.createDeserializer());
	}
	
	public static class DeserializerProvider implements ArgumentsProvider {
		@Override public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return JNXServiceFinder.findServices(JsonDeserializerFactory.class)
				.map(JsonDeserializerFactory::newDeserializer)
				.map(Arguments::of);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testDeserializeNewDocument(JsonDeserializer deserializer) throws Exception {
		String json = "{\"foo\":\"bar\",\"bar\": 3}";
		
		withTempDb(database -> {
			Document doc = deserializer.target(database)
				.fromJson(json);
			assertEquals("bar", doc.get("foo", String.class, null));
			assertEquals(3, doc.get("bar", int.class, 0));
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testDeserializeExistingDocument(JsonDeserializer deserializer) throws Exception {
		String json = "{\"foo\":\"bar\",\"bar\": 3}";
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Form", "Hello");
			deserializer.target(doc)
				.fromJson(json);
			assertEquals("bar", doc.get("foo", String.class, null));
			assertEquals(3, doc.get("bar", int.class, 0));
			assertEquals("Hello", doc.get("Form", String.class, null));
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testDeserializeExistingDocument2(JsonDeserializer deserializer) throws Exception {
		String json = "{\"foo\":\"bar\",\"bar\": 3}";
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Form", "Hello");
			doc = deserializer.target(doc)
				.fromJson(json);
			assertEquals("bar", doc.get("foo", String.class, null));
			assertEquals(3, doc.get("bar", int.class, 0));
			assertEquals("Hello", doc.get("Form", String.class, null));
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testBooleanValues(JsonDeserializer deserializer) throws Exception {
		String json = "{\"foo\":true,\"bar\": false}";
		
		withTempDb(database -> {
			Document doc = deserializer.target(database)
				.booleanValues("hi", "there")
				.fromJson(json);
			assertEquals("hi", doc.get("foo", String.class, null));
			assertEquals("there", doc.get("bar", String.class, null));
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testDateTimeItems(JsonDeserializer deserializer) throws Exception {
		String json = "{\"foo\":\"2020-01-01\",\"bar\": \"2020-02-02\"}";
		
		withTempDb(database -> {
			Document doc = deserializer.target(database)
				.dateTimeItems(Arrays.asList("Foo"))
				.fromJson(json);
			assertEquals(LocalDate.of(2020, 1, 1), doc.get("foo", LocalDate.class, null));
			assertEquals("2020-02-02", doc.getItemValue("bar").get(0));
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testDateTimeItems2(JsonDeserializer deserializer) throws Exception {
		String json = "{\"foo\":\"2020-01-01\",\"bar\": \"2020-02-02/2020-03-03\"}";
		
		withTempDb(database -> {
			Document doc = deserializer.target(database)
				.dateTimeItems(Arrays.asList("Foo", "Bar"))
				.fromJson(json);
			assertEquals(LocalDate.of(2020, 1, 1), doc.get("foo", LocalDate.class, null));
			LocalDate start = LocalDate.of(2020, 2, 2);
			LocalDate end = LocalDate.of(2020, 3, 3);
			DominoDateRange range = database.getParentDominoClient().createDateRange(start, end);
			assertEquals(range, doc.getItemValue("bar").get(0));
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testIllegalDateTimeItems(JsonDeserializer deserializer) throws Exception {
		String json = "{\"foo\":\"hi\"}";
		
		withTempDb(database -> {
			// This throws an IllegalStateException internally, but surfaces differently based on implementation
			assertThrows(RuntimeException.class, () -> deserializer.target(database)
				.dateTimeItems(Arrays.asList("Foo"))
				.fromJson(json));
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testArrayValues(JsonDeserializer deserializer) throws Exception {
		String json = "{\"foo\":[1, 2, 3],\"bar\": [\"hi\", \"there\"], \"baz\":[true, false, true]}";
		
		withTempDb(database -> {
			Document doc = deserializer.target(database)
				.booleanValues("hi", "there")
				.fromJson(json);
			assertEquals(Arrays.asList(1, 2, 3), doc.getAsList("foo", Integer.class, null));
			assertEquals(Arrays.asList("hi", "there"), doc.getAsList("bar", String.class, null));
			assertEquals(Arrays.asList("hi", "there", "hi"), doc.getAsList("baz", String.class, null));
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testMixedArrayValues(JsonDeserializer deserializer) throws Exception {
		String json = "{\"foo\":[1, \"foo\", 3],\"bar\": [\"hi\", \"there\"], \"baz\":[true, false, true]}";
		
		withTempDb(database -> {
			assertThrows(RuntimeException.class, () -> deserializer.target(database)
				.booleanValues("hi", "there")
				.fromJson(json));
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testIgnoreMeta(JsonDeserializer deserializer) throws Exception {
		String json = "{\"foo\":true,\"bar\": false, \"@meta\": \"hi\"}";
		
		withTempDb(database -> {
			Document doc = deserializer.target(database)
				.booleanValues("hi", "there")
				.fromJson(json);
			assertEquals("hi", doc.get("foo", String.class, null));
			assertEquals("there", doc.get("bar", String.class, null));
			assertFalse(doc.hasItem("@meta"), "@meta item should have been removed");
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testRemoveItems(JsonDeserializer deserializer) throws Exception {
		String json = "{\"foo\":\"hi\",\"bar\": \"there\"}";
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Form", "Hello");
			doc.replaceItemValue("Baz", "hi there");
			doc.replaceItemValue("$SystemField", "tester");
			deserializer.target(doc)
				.removeMissingItems(true)
				.fromJson(json);
			assertEquals("hi", doc.get("foo", String.class, null));
			assertEquals("there", doc.get("bar", String.class, null));
			assertFalse(doc.hasItem("baz"), "Baz item should have been removed");
			assertEquals("tester", doc.get("$SystemField", String.class, null));
		});
	}

	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testDetectDateTime(JsonDeserializer deserializer) throws Exception {
		String json = IOUtils.resourceToString("/json/testBasicDeserialization/datejson.json", StandardCharsets.UTF_8);
		withTempDb(database -> {
			Document doc = database.createDocument();
			deserializer.target(doc)
				.detectDateTime(true)
				.fromJson(json);
			
			{
				assertTrue(doc.hasItem("date"));
				DominoDateTime date = doc.get("date", DominoDateTime.class, null);
				assertNotEquals(null, date, "date field value should not be null");
				Temporal temporal = date.toTemporal().orElse(null);
				assertInstanceOf(LocalDate.class, temporal, "temporal version should be a LocalDate");
				assertEquals(LocalDate.of(2020, 7, 10), temporal);
			}
			{
				DominoDateTime time = doc.get("time", DominoDateTime.class, null);
				assertNotEquals(null, time, "time field value should not be null");
				Temporal temporal = time.toTemporal().orElse(null);
				assertInstanceOf(LocalTime.class, temporal, "temporal version should be a LocalTime");
				assertEquals(LocalTime.of(13, 47, 03), temporal);
			}
			{
				DominoDateTime dt = doc.get("datetime", DominoDateTime.class, null);
				assertNotEquals(null, dt, "datetime field value should not be null");
				Temporal temporal = dt.toTemporal().orElse(null);
				assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime, "temporal version should be an OffsetDateTime");
				LocalDate date = LocalDate.of(2020, 7, 10);
				LocalTime time = LocalTime.of(13, 47, 03);
				assertEquals(OffsetDateTime.of(date, time, ZoneOffset.UTC), OffsetDateTime.from(temporal));
			}
			{
				DominoDateTime dt = doc.get("datetime_offset", DominoDateTime.class, null);
				assertNotEquals(null, dt, "datetime_offset field value should not be null");
				Temporal temporal = dt.toTemporal().orElse(null);
				assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime, "temporal version should be an OffsetDateTime");
				LocalDate date = LocalDate.of(2020, 7, 10);
				LocalTime time = LocalTime.of(13, 47, 03);
				assertEquals(OffsetDateTime.of(date, time, ZoneOffset.ofHours(-5)), OffsetDateTime.from(temporal));
			}
			{
				List<DominoDateTime> dts = doc.getAsList("datetimes", DominoDateTime.class, null);
				assertNotEquals(null, dts, "datetimes field value should not be null");
				{
					Temporal temporal = dts.get(0).toTemporal().orElse(null);
					assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime, "temporal version should be an OffsetDateTime");
					LocalDate date = LocalDate.of(2020, 7, 10);
					LocalTime time = LocalTime.of(13, 47, 03);
					assertEquals(OffsetDateTime.of(date, time, ZoneOffset.UTC), OffsetDateTime.from(temporal));
				}
				{
					Temporal temporal = dts.get(1).toTemporal().orElse(null);
					assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime, "temporal version should be an OffsetDateTime");
					LocalDate date = LocalDate.of(2020, 7, 10);
					LocalTime time = LocalTime.of(13, 47, 03);
					assertEquals(OffsetDateTime.of(date, time, ZoneOffset.ofHours(-5)), OffsetDateTime.from(temporal));
				}
			}
		});
	}

	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testSpecifyDateTime(JsonDeserializer deserializer) throws Exception {
		String json = IOUtils.resourceToString("/json/testBasicDeserialization/datejson.json", StandardCharsets.UTF_8);
		withTempDb(database -> {
			Document doc = database.createDocument();
			deserializer.target(doc)
				.dateTimeItems(Arrays.asList("date", "time", "datetime", "datetime_offset", "datetimes"))
				.fromJson(json);
			
			{
				assertTrue(doc.hasItem("date"));
				DominoDateTime date = doc.get("date", DominoDateTime.class, null);
				assertNotEquals(null, date, "date field value should not be null");
				Temporal temporal = date.toTemporal().orElse(null);
				assertInstanceOf(LocalDate.class, temporal, "temporal version should be a LocalDate");
				assertEquals(LocalDate.of(2020, 7, 10), temporal);
			}
			{
				DominoDateTime time = doc.get("time", DominoDateTime.class, null);
				assertNotEquals(null, time, "time field value should not be null");
				Temporal temporal = time.toTemporal().orElse(null);
				assertInstanceOf(LocalTime.class, temporal, "temporal version should be a LocalTime");
				assertEquals(LocalTime.of(13, 47, 03), temporal);
			}
			{
				DominoDateTime dt = doc.get("datetime", DominoDateTime.class, null);
				assertNotEquals(null, dt, "datetime field value should not be null");
				Temporal temporal = dt.toTemporal().orElse(null);
				assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime, "temporal version should be an OffsetDateTime");
				LocalDate date = LocalDate.of(2020, 7, 10);
				LocalTime time = LocalTime.of(13, 47, 03);
				assertEquals(OffsetDateTime.of(date, time, ZoneOffset.UTC), OffsetDateTime.from(temporal));
			}
			{
				DominoDateTime dt = doc.get("datetime_offset", DominoDateTime.class, null);
				assertNotEquals(null, dt, "datetime_offset field value should not be null");
				Temporal temporal = dt.toTemporal().orElse(null);
				assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime, "temporal version should be an OffsetDateTime");
				LocalDate date = LocalDate.of(2020, 7, 10);
				LocalTime time = LocalTime.of(13, 47, 03);
				assertEquals(OffsetDateTime.of(date, time, ZoneOffset.ofHours(-5)), OffsetDateTime.from(temporal));
			}
			{
				List<DominoDateTime> dts = doc.getAsList("datetimes", DominoDateTime.class, null);
				assertNotEquals(null, dts, "datetimes field value should not be null");
				{
					Temporal temporal = dts.get(0).toTemporal().orElse(null);
					assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime, "temporal version should be an OffsetDateTime");
					LocalDate date = LocalDate.of(2020, 7, 10);
					LocalTime time = LocalTime.of(13, 47, 03);
					assertEquals(OffsetDateTime.of(date, time, ZoneOffset.UTC), OffsetDateTime.from(temporal));
				}
				{
					Temporal temporal = dts.get(1).toTemporal().orElse(null);
					assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime, "temporal version should be an OffsetDateTime");
					LocalDate date = LocalDate.of(2020, 7, 10);
					LocalTime time = LocalTime.of(13, 47, 03);
					assertEquals(OffsetDateTime.of(date, time, ZoneOffset.ofHours(-5)), OffsetDateTime.from(temporal));
				}
			}
		});
	}

	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testSpecifyDateTimePartial(JsonDeserializer deserializer) throws Exception {
		String json = IOUtils.resourceToString("/json/testBasicDeserialization/datejson.json", StandardCharsets.UTF_8);
		withTempDb(database -> {
			Document doc = database.createDocument();
			deserializer.target(doc)
				.dateTimeItems(Arrays.asList("date", "datetime_offset", "datetimes"))
				.fromJson(json);
			
			{
				assertTrue(doc.hasItem("date"));
				DominoDateTime date = doc.get("date", DominoDateTime.class, null);
				assertNotEquals(null, date, "date field value should not be null");
				Temporal temporal = date.toTemporal().orElse(null);
				assertInstanceOf(LocalDate.class, temporal, "temporal version should be a LocalDate");
				assertEquals(LocalDate.of(2020, 7, 10), temporal);
			}
			{
				assertTrue(doc.hasItem("time"));
				assertEquals(ItemDataType.TYPE_TEXT, doc.getFirstItem("time").get().getType());
				assertEquals("13:47:03", doc.get("time", String.class, null));
			}
			{
				assertTrue(doc.hasItem("datetime"));
				assertEquals(ItemDataType.TYPE_TEXT, doc.getFirstItem("datetime").get().getType());
				assertEquals("2020-07-10T13:47:03Z", doc.get("datetime", String.class, null));
			}
			{
				DominoDateTime dt = doc.get("datetime_offset", DominoDateTime.class, null);
				assertNotEquals(null, dt, "datetime_offset field value should not be null");
				Temporal temporal = dt.toTemporal().orElse(null);
				assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime, "temporal version should be an OffsetDateTime");
				LocalDate date = LocalDate.of(2020, 7, 10);
				LocalTime time = LocalTime.of(13, 47, 03);
				assertEquals(OffsetDateTime.of(date, time, ZoneOffset.ofHours(-5)), OffsetDateTime.from(temporal));
			}
			{
				List<DominoDateTime> dts = doc.getAsList("datetimes", DominoDateTime.class, null);
				assertNotEquals(null, dts, "datetimes field value should not be null");
				{
					Temporal temporal = dts.get(0).toTemporal().orElse(null);
					assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime, "temporal version should be an OffsetDateTime");
					LocalDate date = LocalDate.of(2020, 7, 10);
					LocalTime time = LocalTime.of(13, 47, 03);
					assertEquals(OffsetDateTime.of(date, time, ZoneOffset.UTC), OffsetDateTime.from(temporal));
				}
				{
					Temporal temporal = dts.get(1).toTemporal().orElse(null);
					assertTrue(temporal instanceof OffsetDateTime || temporal instanceof ZonedDateTime, "temporal version should be an OffsetDateTime");
					LocalDate date = LocalDate.of(2020, 7, 10);
					LocalTime time = LocalTime.of(13, 47, 03);
					assertEquals(OffsetDateTime.of(date, time, ZoneOffset.ofHours(-5)), OffsetDateTime.from(temporal));
				}
			}
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testCustomProcessorNullProp(JsonDeserializer deserializerParam) {
		assertThrows(NullPointerException.class, () -> deserializerParam.customProcessor(null, (val, item, doc) -> {}));
	}

	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testCustomProcessorEmptyProp(JsonDeserializer deserializerParam) {
		assertThrows(IllegalArgumentException.class, () -> deserializerParam.customProcessor("", (val, item, doc) -> {}));
	}

	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testCustomProcessorNullProcessor(JsonDeserializer deserializerParam) {
		assertThrows(NullPointerException.class, () -> deserializerParam.customProcessor("someitem", null));
	}
	
	@ParameterizedTest
	@ArgumentsSource(DeserializerProvider.class)
	public void testCustomProcessor(JsonDeserializer deserializerParam) throws Exception {
		deserializerParam.customProcessor("foo", (val, itemName, doc) -> doc.replaceItemValue("foo", "hey custom"));
		String json = Json.createObjectBuilder()
			.add("foo", "heh")
			.build()
			.toString();
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			deserializerParam.target(doc)
				.fromJson(json);
			assertEquals("hey custom", doc.get("foo", String.class, null));
		});
	}
}
