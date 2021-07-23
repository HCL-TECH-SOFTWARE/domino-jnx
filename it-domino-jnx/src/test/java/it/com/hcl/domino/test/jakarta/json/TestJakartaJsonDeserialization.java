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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;

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
		withTempDb(database -> {
			Jsonb jsonb = JsonbBuilder.newBuilder()
				.withConfig(
					new JsonbConfig()
						.withDeserializers(
							DocumentJsonbDeserializer.newDeserializer(database)
						)
				).build();
			
			Document doc;
			try(InputStream is = getClass().getResourceAsStream("/json/testBasicDeserialization/basicdoc.json")) {
				doc = jsonb.fromJson(is, Document.class);
			}
			
			assertNotEquals(null, doc, "doc should not be null");
			assertEquals("TestDocument", doc.get("Form", String.class, null));
			assertEquals("bar", doc.get("foo", String.class, null));
			assertEquals("baz", doc.get("bar", String.class, null));
			assertEquals(1, doc.get("Number", int.class, 0));
			assertEquals(null, doc.get("void", String.class, null));
			assertEquals(true, doc.get("bool", boolean.class, null));
		});
	}
	
	/**
	 * Tests that date-like values are left as strings by default
	 * @throws Exception if there is a problem running the test
	 */
	@Test
	public void testNonDateDeserialization() throws Exception {
		withTempDb(database -> {
			Jsonb jsonb = JsonbBuilder.newBuilder()
				.withConfig(
					new JsonbConfig()
						.withDeserializers(
							DocumentJsonbDeserializer.newDeserializer(database)
						)
				).build();
			
			Document doc;
			try(InputStream is = getClass().getResourceAsStream("/json/testBasicDeserialization/datejson.json")) {
				doc = jsonb.fromJson(is, Document.class);
			}
			
			assertNotEquals(null, doc, "doc should not be null");
			assertEquals("TestDocument", doc.get("Form", String.class, null));
			for(String itemName : Arrays.asList("date", "time", "datetime", "datetime_offset")) {
				Item date = doc.getFirstItem(itemName).orElse(null);
				assertNotNull(date, itemName + " field should not be null");
				assertEquals(ItemDataType.TYPE_TEXT, date.getType());
				assertFalse(StringUtil.isEmpty(date.get(String.class, null)), itemName + " value should not be empty");
			}
		});
	}
	
	@Test
	public void testDateDetection() throws Exception {
		withTempDb(database -> {
			Jsonb jsonb = JsonbBuilder.newBuilder()
				.withConfig(
					new JsonbConfig()
						.withDeserializers(
							DocumentJsonbDeserializer.newBuilder(database)
								.detectDateTime(true)
								.build()
						)
				).build();
			
			Document doc;
			try(InputStream is = getClass().getResourceAsStream("/json/testBasicDeserialization/datejson.json")) {
				doc = jsonb.fromJson(is, Document.class);
			}
			
			assertNotEquals(null, doc, "doc should not be null");
			assertEquals("TestDocument", doc.get("Form", String.class, null));
			{
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

}
