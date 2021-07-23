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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.hcl.domino.data.Database.OpenDocumentMode;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoTimeType;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.html.HtmlConvertOption;
import com.hcl.domino.json.DateRangeFormat;
import com.hcl.domino.json.JsonSerializer;
import com.hcl.domino.json.JsonSerializerFactory;
import com.hcl.domino.misc.JNXServiceFinder;
import com.ibm.commons.util.StringUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.stream.JsonParser;

@SuppressWarnings("nls")
public class TestJsonSerialization extends AbstractNotesRuntimeTest {
	@Test
	public void testJsonSerializationService() {
		assertNotNull(JsonSerializer.createSerializer());
	}
	
	public static class SerializerProvider implements ArgumentsProvider {
		@Override public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return JNXServiceFinder.findServices(JsonSerializerFactory.class)
				.map(JsonSerializerFactory::newSerializer)
				.map(Arguments::of);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testSerializeDocument(JsonSerializer serializer) throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", "Bar");
			Object json = serializer.toJson(doc);
			assertNotNull(json, "JSON result should not be null");
			String jsonString = String.valueOf(json);
			JsonParser parser = Json.createParser(new StringReader(jsonString));
			parser.next();
			JsonObject obj = parser.getObject();
			assertEquals("Bar", obj.getString("Foo"));
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testSerializeDocumentLowercase(JsonSerializer serializer) throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", "Bar");
			Object json = serializer
					.lowercaseProperties(true)
					.toJson(doc);
			assertNotNull(json, "JSON result should not be null");
			String jsonString = String.valueOf(json);
			JsonParser parser = Json.createParser(new StringReader(jsonString));
			parser.next();
			JsonObject obj = parser.getObject();
			assertEquals("Bar", obj.getString("foo"));
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testVertxSerialization(JsonSerializer serializer) throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", "bar");
			doc.replaceItemValue("Bar", "baz");
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			assertEquals("bar", json.getString("Foo"));
			assertEquals("baz", json.getString("Bar"));
			
			assertFalse(json.containsKey("@meta"), "JSON should not contain a metadata object");
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testHtmlSerialization(JsonSerializer serializer) throws Exception {
		withResourceDxl("/dxl/testJsonSerialization", database -> {
			Document doc = database.queryFormula(" Form='Example' & Foo='Bar' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
				.getDocuments()
				.findFirst()
				.get();
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			assertEquals("Bar", json.getString("Foo"));
			assertTrue(json.containsKey("Body"), "Body field should be present");
			assertTrue(json.getString("Body").contains("d35f528e-8cfc-4d97-9078-428da6673f51"));
			assertTrue(json.getString("Body").contains("<br />"));
			assertFalse(json.getString("Body").contains("<br>"));
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testHtmlSerializationCustomized(JsonSerializer serializer) throws Exception {
		withResourceDxl("/dxl/testJsonSerialization", database -> {
			Document doc = database.queryFormula(" Form='Example' & Foo='Bar' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
				.getDocuments()
				.findFirst()
				.get();
			
			serializer.richTextConvertOption(HtmlConvertOption.XMLCompatibleHTML, "0");
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			assertEquals("Bar", json.getString("Foo"));
			assertTrue(json.containsKey("Body"), "Body field should be present");
			assertTrue(json.getString("Body").contains("d35f528e-8cfc-4d97-9078-428da6673f51"));
			assertFalse(json.getString("Body").contains("<br />"));
			assertTrue(json.getString("Body").contains("<br>"));
		});
	}
	

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testMimeSerialization(JsonSerializer serializer) throws Exception {
		withResourceDxl("/dxl/testJsonSerialization", database -> {
			int noteId = database.queryFormula(" Marker='testMimeSerialization' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
				.getNoteIds()
				.get()
				.iterator()
				.next();
			Document doc = database.getDocumentById(noteId, EnumSet.of(OpenDocumentMode.CONVERT_RFC822_TO_TEXT_AND_TIME)).get();
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			assertEquals("Bulk Mail", json.getString("X_MXTHUNDER_Group"));
			assertTrue(json.containsKey("Body"), "Body field should be present");
			assertTrue(json.getString("Body").contains("final hours extra"));
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testMimeSerializationNoConvert(JsonSerializer serializer) throws Exception {
		withResourceDxl("/dxl/testJsonSerialization", database -> {
			Document doc = database.queryFormula(" Marker='testMimeSerialization' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
				.getDocuments()
				.findFirst()
				.get();
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			assertEquals("Bulk Mail", json.getString("X_MXTHUNDER_Group"));
			assertEquals("<1407518313_SectionID-331186_HitID-1407517453669_SiteID-16268_EmailID-82837855_DB-34_SID-37@ss23.agm1.us>", json.getString("X_Track_ID"));
			assertTrue(json.containsKey("Body"), "Body field should be present");
			assertTrue(json.getString("Body").contains("final hours extra"));
			assertTrue(json.containsKey("Received"));
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testExcludeItems(JsonSerializer serializerParam) throws Exception {
		withResourceDxl("/dxl/testJsonSerialization", database -> {
			Document doc = database.queryFormula(" Marker='testMimeSerialization' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
				.getDocuments()
				.findFirst()
				.get();
			
			JsonSerializer serializer = serializerParam
					.excludeItems(Arrays.asList("receiveD"));
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			assertEquals("Bulk Mail", json.getString("X_MXTHUNDER_Group"));
			assertEquals("<1407518313_SectionID-331186_HitID-1407517453669_SiteID-16268_EmailID-82837855_DB-34_SID-37@ss23.agm1.us>", json.getString("X_Track_ID"));
			assertTrue(json.containsKey("Body"), "Body field should be present");
			assertTrue(json.getString("Body").contains("final hours extra"));
			
			// Make sure the received field was excluded
			assertFalse(json.containsKey("Received"));
			assertEquals("Itemize by SMTP Server on Arcturus/Frost(Release 9.0.1FP1HF278 | June 16, 2014) at 08/08/2014 01:18:41 PM", json.getJsonArray("$MIMETrack").getString(0));
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testIncludeItems(JsonSerializer serializerParam) throws Exception {
		withResourceDxl("/dxl/testJsonSerialization", database -> {
			Document doc = database.queryFormula(" Marker='testMimeSerialization' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
				.getDocuments()
				.findFirst()
				.get();
			
			JsonSerializer serializer = serializerParam
					.includeItems(Arrays.asList("bOdy"));
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			assertTrue(json.containsKey("Body"), "Body field should be present");
			assertTrue(json.getString("Body").contains("final hours extra"));
			
			// Make sure the received field was excluded
			assertFalse(json.containsKey("Received"));
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testLowercaseItemNames(JsonSerializer serializerParam) throws Exception {
		withResourceDxl("/dxl/testJsonSerialization", database -> {
			Document doc = database.queryFormula(" Marker='testMimeSerialization' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
				.getDocuments()
				.findFirst()
				.get();
			
			JsonSerializer serializer = serializerParam
					.lowercaseProperties(true);
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			assertEquals("Bulk Mail", json.getString("x_mxthunder_group"));
			assertEquals("<1407518313_SectionID-331186_HitID-1407517453669_SiteID-16268_EmailID-82837855_DB-34_SID-37@ss23.agm1.us>", json.getString("x_track_id"));
			assertTrue(json.containsKey("body"), "Body field should be present");
			assertTrue(json.getString("body").contains("final hours extra"));
			
			assertFalse(json.containsKey("Received"));
			assertTrue(json.containsKey("received"));
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testExcludeTypes(JsonSerializer serializerParam) throws Exception {
		withResourceDxl("/dxl/testJsonSerialization", database -> {
			Document doc = database.queryFormula(" Marker='testMimeSerialization' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
				.getDocuments()
				.findFirst()
				.get();

			JsonSerializer serializer = serializerParam
					.excludeTypes(Arrays.asList(ItemDataType.TYPE_COMPOSITE, ItemDataType.TYPE_MIME_PART));
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			assertEquals("Bulk Mail", json.getString("X_MXTHUNDER_Group"));
			assertEquals("<1407518313_SectionID-331186_HitID-1407517453669_SiteID-16268_EmailID-82837855_DB-34_SID-37@ss23.agm1.us>", json.getString("X_Track_ID"));
			assertFalse(json.containsKey("Body"), "Body field should not be present");
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testMetadata(JsonSerializer serializerParam) throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", "bar");
			doc.replaceItemValue("Bar", "baz");
			doc.save();
			
			JsonSerializer serializer = serializerParam
					.includeMetadata(true);
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			assertEquals("bar", json.getString("Foo"));
			assertEquals("baz", json.getString("Bar"));
			
			assertTrue(json.containsKey("@meta"), "JSON should contain a metadata object");
			JsonObject meta = json.getJsonObject("@meta");
			assertNotEquals(null, meta, "Metadata object should not be null");
			assertEquals(doc.getNoteID(), meta.getInt(JsonSerializer.PROP_META_NOTEID));
			assertEquals(doc.getUNID(), meta.getString(JsonSerializer.PROP_META_UNID));
			TemporalAccessor cdate = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(meta.getString(JsonSerializer.PROP_META_CREATED));
			assertEquals(doc.getCreated().toOffsetDateTime(), OffsetDateTime.from(cdate));
			TemporalAccessor mdate = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(meta.getString(JsonSerializer.PROP_META_LASTMODIFIED));
			assertEquals(doc.getLastModified().toOffsetDateTime(), OffsetDateTime.from(mdate));
			TemporalAccessor adate = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(meta.getString(JsonSerializer.PROP_META_LASTACCESSED));
			assertEquals(doc.getLastAccessed().toOffsetDateTime(), OffsetDateTime.from(adate));
			TemporalAccessor fdate = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(meta.getString(JsonSerializer.PROP_META_LASTMODIFIEDINFILE));
			assertEquals(doc.getModifiedInThisFile().toOffsetDateTime(), OffsetDateTime.from(fdate));
			TemporalAccessor added = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(meta.getString(JsonSerializer.PROP_META_ADDEDTOFILE));
			assertEquals(doc.getAddedToFile().toOffsetDateTime(), OffsetDateTime.from(added));
			
			JsonArray docClassArray = meta.getJsonArray(JsonSerializer.PROP_META_NOTECLASS);
			Collection<DocumentClass> docClass = docClassArray.stream()
					.map(JsonString.class::cast)
					.map(JsonString::getString)
					.map(DocumentClass::valueOf)
					.collect(Collectors.toSet());
			assertEquals(doc.getDocumentClass(), docClass);
			
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testExcludedDollarFonts(JsonSerializer serializerParam) throws Exception {
		withResourceDxl("/dxl/testJsonSerialization", database -> {
			Document doc = database.queryFormula(" Form='Page: Basic' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
				.getDocuments()
				.findFirst()
				.get();
			
			JsonSerializer serializer = serializerParam
					.lowercaseProperties(true);
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			assertFalse(json.containsKey("$fonts"), "JSON should not contain $fonts item");
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testForcedDollarFonts(JsonSerializer serializerParam) throws Exception {
		withResourceDxl("/dxl/testJsonSerialization", database -> {
			Document doc = database.queryFormula(" Form='Page: Basic' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
				.getDocuments()
				.findFirst()
				.get();
			
			JsonSerializer serializer = serializerParam
					.lowercaseProperties(true)
					.includeItems(Arrays.asList("$Fonts"));
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			assertTrue(json.containsKey("$fonts"), "JSON should contain $fonts item");
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testSerializeBooleans(JsonSerializer serializerParam) throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", "Bar")
				.replaceItemValue("NumberBool", 1)
				.replaceItemValue("NumberBoolFalse", 0)
				.replaceItemValue("YBool", "Y")
				.replaceItemValue("YesBool", "Yes")
				.replaceItemValue("YBoolFalse", "N");

			
			JsonSerializer serializer = serializerParam
					.booleanItemNames(Arrays.asList("NumberBool", "NumberBoolFalse", "YBool", "YesBool", "YBoolFalse", "FakeBool"))
					.booleanTrueValues(Arrays.asList("Y", "Yes", 1));
			

			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			assertThrows(ClassCastException.class, () -> json.getBoolean("Foo"));
			assertTrue(json.getBoolean("NumberBool"));
			assertFalse(json.getBoolean("NumberBoolFalse"));
			assertThrows(NullPointerException.class, () -> json.getBoolean("FakeBool"));
			assertTrue(json.getBoolean("YBool"));
			assertTrue(json.getBoolean("YesBool"));
			assertFalse(json.getBoolean("YBoolFalse"));
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testDateRange(JsonSerializer serializerParam) throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			LocalDate today = LocalDate.now();
			LocalDate tomorrow = LocalDate.now().plus(1, ChronoUnit.DAYS);
			DominoDateRange range = database.getParentDominoClient().createDateRange(today, tomorrow);
			doc.replaceItemValue("DateRange", range);
			// Test this too while we're here
			doc.get("DateRange", DominoDateRange.class, null);
			doc.get("DateRange", DominoTimeType.class, null);
			
			JsonSerializer serializer = serializerParam;
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			
			String expected = DateTimeFormatter.ISO_LOCAL_DATE.format(today) + '/' + DateTimeFormatter.ISO_LOCAL_DATE.format(tomorrow);
			assertEquals(expected, json.getString("DateRange"));
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testDateRangeArray(JsonSerializer serializerParam) throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			LocalDate today = LocalDate.now();
			LocalDate tomorrow = LocalDate.now().plus(1, ChronoUnit.DAYS);
			DominoDateRange range = database.getParentDominoClient().createDateRange(today, tomorrow);
			doc.replaceItemValue("DateRange", Arrays.asList(range, range));
			
			JsonSerializer serializer = serializerParam;
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			
			String expected = DateTimeFormatter.ISO_LOCAL_DATE.format(today) + '/' + DateTimeFormatter.ISO_LOCAL_DATE.format(tomorrow);
			List<String> found = json.getJsonArray("DateRange").stream()
				.map(JsonString.class::cast)
				.map(JsonString::getString)
				.collect(Collectors.toList());
			assertEquals(Arrays.asList(expected, expected), found);
		});
	}
	
	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testDateRangeObject(JsonSerializer serializerParam) throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			LocalDate today = LocalDate.now();
			LocalDate tomorrow = LocalDate.now().plus(1, ChronoUnit.DAYS);
			DominoDateRange range = database.getParentDominoClient().createDateRange(today, tomorrow);
			doc.replaceItemValue("DateRange", range);
			// Test this too while we're here
			doc.get("DateRange", DominoDateRange.class, null);
			doc.get("DateRange", DominoTimeType.class, null);
			
			JsonSerializer serializer = serializerParam
				.dateRangeFormat(DateRangeFormat.OBJECT);
			
			String jsonString = serializer.toJsonString(doc);
			assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
			JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
			assertNotEquals(null, json, "JSON object should not be null");
			
			JsonObject expected = Json.createObjectBuilder()
				.add("from", DateTimeFormatter.ISO_LOCAL_DATE.format(today))
				.add("to", DateTimeFormatter.ISO_LOCAL_DATE.format(tomorrow))
				.build();
			JsonObject found = json.getJsonObject("DateRange");
			assertEquals(expected, found);
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testCustomProcessorNullProp(JsonSerializer serializerParam) {
		assertThrows(NullPointerException.class, () -> serializerParam.customProcessor(null, (doc, item) -> null));
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testCustomProcessorEmptyProp(JsonSerializer serializerParam) {
		assertThrows(IllegalArgumentException.class, () -> serializerParam.customProcessor("", (doc, item) -> null));
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testCustomProcessorNullProcessor(JsonSerializer serializerParam) {
		assertThrows(NullPointerException.class, () -> serializerParam.customProcessor("someitem", null));
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testCustomProcessorString(JsonSerializer serializerParam) throws Exception {
		serializerParam.customProcessor("Foo", (doc, itemName) -> "Bar");
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", "Baz");
			
			String jsonString = serializerParam.toJsonString(doc);

			JsonObject obj = Json.createReader(new StringReader(jsonString)).readObject();
			assertEquals("Bar", obj.getString("Foo"));
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testCustomProcessorBoolean(JsonSerializer serializerParam) throws Exception {
		serializerParam.customProcessor("Foo", (doc, itemName) -> true);
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", "Baz");
			
			String jsonString = serializerParam.toJsonString(doc);

			JsonObject obj = Json.createReader(new StringReader(jsonString)).readObject();
			assertEquals(true, obj.getBoolean("Foo"));
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testCustomProcessorDouble(JsonSerializer serializerParam) throws Exception {
		serializerParam.customProcessor("Foo", (doc, itemName) -> 123.4d);
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", "Baz");
			
			String jsonString = serializerParam.toJsonString(doc);

			JsonObject obj = Json.createReader(new StringReader(jsonString)).readObject();
			assertEquals(123.4d, obj.getJsonNumber("Foo").doubleValue());
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testCustomProcessorInt(JsonSerializer serializerParam) throws Exception {
		serializerParam.customProcessor("Foo", (doc, itemName) -> 123);
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", "Baz");
			
			String jsonString = serializerParam.toJsonString(doc);

			JsonObject obj = Json.createReader(new StringReader(jsonString)).readObject();
			assertEquals(123d, obj.getJsonNumber("Foo").doubleValue());
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testCustomProcessorArray(JsonSerializer serializerParam) throws Exception {
		Object[] expected = { "Foo", true, 123.4d };
		serializerParam.customProcessor("Foo", (doc, itemName) -> Arrays.asList(expected));
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", "Baz");
			
			String jsonString = serializerParam.toJsonString(doc);

			JsonObject obj = Json.createReader(new StringReader(jsonString)).readObject();
			
			JsonArray arr = obj.getJsonArray("Foo");
			assertEquals("Foo", arr.getString(0));
			assertEquals(true, arr.getBoolean(1));
			assertEquals(123.4d, arr.getJsonNumber(2).doubleValue());
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testCustomProcessorObject(JsonSerializer serializerParam) throws Exception {
		Map<String, Object> expected = new LinkedHashMap<>();
		expected.put("a", "hello");
		expected.put("b", 123.4d);
		expected.put("c", true);
		expected.put("d", Arrays.asList(1, 2, 3));
		serializerParam.customProcessor("Foo", (doc, itemName) -> expected);
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", "Baz");
			
			String jsonString = serializerParam.toJsonString(doc);
			
			JsonObject obj = Json.createReader(new StringReader(jsonString)).readObject();
			JsonObject innerObj = obj.getJsonObject("Foo");
			assertNotNull(innerObj);
			assertEquals("hello", innerObj.getString("a"));
			assertEquals(123.4d, innerObj.getJsonNumber("b").doubleValue());
			assertEquals(true, innerObj.getBoolean("c"));
			JsonArray dVal = innerObj.getJsonArray("d");
			List<?> expectedList = (List<?>)expected.get("d");
			for(int i = 0; i < expectedList.size(); i++) {
				assertEquals(expectedList.get(i), dVal.getJsonNumber(i).intValue());
			}
		});
	}

	@ParameterizedTest
	@ArgumentsSource(SerializerProvider.class)
	public void testCustomProcessorIllegalType(JsonSerializer serializerParam) throws Exception {
		serializerParam.customProcessor("Foo", (doc, itemName) -> new Object());
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", "Baz");
			
			assertThrows(RuntimeException.class, () -> serializerParam.toJsonString(doc));
		});
	}
}
