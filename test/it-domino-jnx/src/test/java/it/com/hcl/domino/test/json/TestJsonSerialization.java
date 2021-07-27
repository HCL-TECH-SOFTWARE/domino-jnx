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

import org.junit.jupiter.api.Assertions;
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
  public static class SerializerProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
      return JNXServiceFinder.findServices(JsonSerializerFactory.class)
          .map(JsonSerializerFactory::newSerializer)
          .map(Arguments::of);
    }
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testCustomProcessorArray(final JsonSerializer serializerParam) throws Exception {
    final Object[] expected = { "Foo", true, 123.4d };
    serializerParam.customProcessor("Foo", (doc, itemName) -> Arrays.asList(expected));

    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "Baz");

      final String jsonString = serializerParam.toJsonString(doc);

      final JsonObject obj = Json.createReader(new StringReader(jsonString)).readObject();

      final JsonArray arr = obj.getJsonArray("Foo");
      Assertions.assertEquals("Foo", arr.getString(0));
      Assertions.assertEquals(true, arr.getBoolean(1));
      Assertions.assertEquals(123.4d, arr.getJsonNumber(2).doubleValue());
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testCustomProcessorBoolean(final JsonSerializer serializerParam) throws Exception {
    serializerParam.customProcessor("Foo", (doc, itemName) -> true);

    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "Baz");

      final String jsonString = serializerParam.toJsonString(doc);

      final JsonObject obj = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertEquals(true, obj.getBoolean("Foo"));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testCustomProcessorDouble(final JsonSerializer serializerParam) throws Exception {
    serializerParam.customProcessor("Foo", (doc, itemName) -> 123.4d);

    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "Baz");

      final String jsonString = serializerParam.toJsonString(doc);

      final JsonObject obj = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertEquals(123.4d, obj.getJsonNumber("Foo").doubleValue());
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testCustomProcessorEmptyProp(final JsonSerializer serializerParam) {
    Assertions.assertThrows(IllegalArgumentException.class, () -> serializerParam.customProcessor("", (doc, item) -> null));
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testCustomProcessorIllegalType(final JsonSerializer serializerParam) throws Exception {
    serializerParam.customProcessor("Foo", (doc, itemName) -> new Object());

    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "Baz");

      Assertions.assertThrows(RuntimeException.class, () -> serializerParam.toJsonString(doc));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testCustomProcessorInt(final JsonSerializer serializerParam) throws Exception {
    serializerParam.customProcessor("Foo", (doc, itemName) -> 123);

    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "Baz");

      final String jsonString = serializerParam.toJsonString(doc);

      final JsonObject obj = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertEquals(123d, obj.getJsonNumber("Foo").doubleValue());
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testCustomProcessorNullProcessor(final JsonSerializer serializerParam) {
    Assertions.assertThrows(NullPointerException.class, () -> serializerParam.customProcessor("someitem", null));
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testCustomProcessorNullProp(final JsonSerializer serializerParam) {
    Assertions.assertThrows(NullPointerException.class, () -> serializerParam.customProcessor(null, (doc, item) -> null));
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testCustomProcessorObject(final JsonSerializer serializerParam) throws Exception {
    final Map<String, Object> expected = new LinkedHashMap<>();
    expected.put("a", "hello");
    expected.put("b", 123.4d);
    expected.put("c", true);
    expected.put("d", Arrays.asList(1, 2, 3));
    serializerParam.customProcessor("Foo", (doc, itemName) -> expected);

    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "Baz");

      final String jsonString = serializerParam.toJsonString(doc);

      final JsonObject obj = Json.createReader(new StringReader(jsonString)).readObject();
      final JsonObject innerObj = obj.getJsonObject("Foo");
      Assertions.assertNotNull(innerObj);
      Assertions.assertEquals("hello", innerObj.getString("a"));
      Assertions.assertEquals(123.4d, innerObj.getJsonNumber("b").doubleValue());
      Assertions.assertEquals(true, innerObj.getBoolean("c"));
      final JsonArray dVal = innerObj.getJsonArray("d");
      final List<?> expectedList = (List<?>) expected.get("d");
      for (int i = 0; i < expectedList.size(); i++) {
        Assertions.assertEquals(expectedList.get(i), dVal.getJsonNumber(i).intValue());
      }
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testCustomProcessorString(final JsonSerializer serializerParam) throws Exception {
    serializerParam.customProcessor("Foo", (doc, itemName) -> "Bar");

    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "Baz");

      final String jsonString = serializerParam.toJsonString(doc);

      final JsonObject obj = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertEquals("Bar", obj.getString("Foo"));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testDateRange(final JsonSerializer serializerParam) throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      final LocalDate today = LocalDate.now();
      final LocalDate tomorrow = LocalDate.now().plus(1, ChronoUnit.DAYS);
      final DominoDateRange range = database.getParentDominoClient().createDateRange(today, tomorrow);
      doc.replaceItemValue("DateRange", range);
      // Test this too while we're here
      doc.get("DateRange", DominoDateRange.class, null);
      doc.get("DateRange", DominoTimeType.class, null);

      final JsonSerializer serializer = serializerParam;

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");

      final String expected = DateTimeFormatter.ISO_LOCAL_DATE.format(today) + '/'
          + DateTimeFormatter.ISO_LOCAL_DATE.format(tomorrow);
      Assertions.assertEquals(expected, json.getString("DateRange"));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testDateRangeArray(final JsonSerializer serializerParam) throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      final LocalDate today = LocalDate.now();
      final LocalDate tomorrow = LocalDate.now().plus(1, ChronoUnit.DAYS);
      final DominoDateRange range = database.getParentDominoClient().createDateRange(today, tomorrow);
      doc.replaceItemValue("DateRange", Arrays.asList(range, range));

      final JsonSerializer serializer = serializerParam;

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");

      final String expected = DateTimeFormatter.ISO_LOCAL_DATE.format(today) + '/'
          + DateTimeFormatter.ISO_LOCAL_DATE.format(tomorrow);
      final List<String> found = json.getJsonArray("DateRange").stream()
          .map(JsonString.class::cast)
          .map(JsonString::getString)
          .collect(Collectors.toList());
      Assertions.assertEquals(Arrays.asList(expected, expected), found);
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testDateRangeObject(final JsonSerializer serializerParam) throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      final LocalDate today = LocalDate.now();
      final LocalDate tomorrow = LocalDate.now().plus(1, ChronoUnit.DAYS);
      final DominoDateRange range = database.getParentDominoClient().createDateRange(today, tomorrow);
      doc.replaceItemValue("DateRange", range);
      // Test this too while we're here
      doc.get("DateRange", DominoDateRange.class, null);
      doc.get("DateRange", DominoTimeType.class, null);

      final JsonSerializer serializer = serializerParam
          .dateRangeFormat(DateRangeFormat.OBJECT);

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");

      final JsonObject expected = Json.createObjectBuilder()
          .add("from", DateTimeFormatter.ISO_LOCAL_DATE.format(today))
          .add("to", DateTimeFormatter.ISO_LOCAL_DATE.format(tomorrow))
          .build();
      final JsonObject found = json.getJsonObject("DateRange");
      Assertions.assertEquals(expected, found);
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testExcludedDollarFonts(final JsonSerializer serializerParam) throws Exception {
    this.withResourceDxl("/dxl/testJsonSerialization", database -> {
      final Document doc = database
          .queryFormula(" Form='Page: Basic' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
          .getDocuments()
          .findFirst()
          .get();

      final JsonSerializer serializer = serializerParam
          .lowercaseProperties(true);

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");
      Assertions.assertFalse(json.containsKey("$fonts"), "JSON should not contain $fonts item");
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testExcludeItems(final JsonSerializer serializerParam) throws Exception {
    this.withResourceDxl("/dxl/testJsonSerialization", database -> {
      final Document doc = database
          .queryFormula(" Marker='testMimeSerialization' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
          .getDocuments()
          .findFirst()
          .get();

      final JsonSerializer serializer = serializerParam
          .excludeItems(Arrays.asList("receiveD"));

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");
      Assertions.assertEquals("Bulk Mail", json.getString("X_MXTHUNDER_Group"));
      Assertions.assertEquals(
          "<1407518313_SectionID-331186_HitID-1407517453669_SiteID-16268_EmailID-82837855_DB-34_SID-37@ss23.agm1.us>",
          json.getString("X_Track_ID"));
      Assertions.assertTrue(json.containsKey("Body"), "Body field should be present");
      Assertions.assertTrue(json.getString("Body").contains("final hours extra"));

      // Make sure the received field was excluded
      Assertions.assertFalse(json.containsKey("Received"));
      Assertions.assertEquals(
          "Itemize by SMTP Server on Arcturus/Frost(Release 9.0.1FP1HF278 | June 16, 2014) at 08/08/2014 01:18:41 PM",
          json.getJsonArray("$MIMETrack").getString(0));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testExcludeTypes(final JsonSerializer serializerParam) throws Exception {
    this.withResourceDxl("/dxl/testJsonSerialization", database -> {
      final Document doc = database
          .queryFormula(" Marker='testMimeSerialization' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
          .getDocuments()
          .findFirst()
          .get();

      final JsonSerializer serializer = serializerParam
          .excludeTypes(Arrays.asList(ItemDataType.TYPE_COMPOSITE, ItemDataType.TYPE_MIME_PART));

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");
      Assertions.assertEquals("Bulk Mail", json.getString("X_MXTHUNDER_Group"));
      Assertions.assertEquals(
          "<1407518313_SectionID-331186_HitID-1407517453669_SiteID-16268_EmailID-82837855_DB-34_SID-37@ss23.agm1.us>",
          json.getString("X_Track_ID"));
      Assertions.assertFalse(json.containsKey("Body"), "Body field should not be present");
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testForcedDollarFonts(final JsonSerializer serializerParam) throws Exception {
    this.withResourceDxl("/dxl/testJsonSerialization", database -> {
      final Document doc = database
          .queryFormula(" Form='Page: Basic' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
          .getDocuments()
          .findFirst()
          .get();

      final JsonSerializer serializer = serializerParam
          .lowercaseProperties(true)
          .includeItems(Arrays.asList("$Fonts"));

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");
      Assertions.assertTrue(json.containsKey("$fonts"), "JSON should contain $fonts item");
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testHtmlSerialization(final JsonSerializer serializer) throws Exception {
    this.withResourceDxl("/dxl/testJsonSerialization", database -> {
      final Document doc = database
          .queryFormula(" Form='Example' & Foo='Bar' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
          .getDocuments()
          .findFirst()
          .get();

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");
      Assertions.assertEquals("Bar", json.getString("Foo"));
      Assertions.assertTrue(json.containsKey("Body"), "Body field should be present");
      Assertions.assertTrue(json.getString("Body").contains("d35f528e-8cfc-4d97-9078-428da6673f51"));
      Assertions.assertTrue(json.getString("Body").contains("<br />"));
      Assertions.assertFalse(json.getString("Body").contains("<br>"));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testHtmlSerializationCustomized(final JsonSerializer serializer) throws Exception {
    this.withResourceDxl("/dxl/testJsonSerialization", database -> {
      final Document doc = database
          .queryFormula(" Form='Example' & Foo='Bar' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
          .getDocuments()
          .findFirst()
          .get();

      serializer.richTextConvertOption(HtmlConvertOption.XMLCompatibleHTML, "0");

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");
      Assertions.assertEquals("Bar", json.getString("Foo"));
      Assertions.assertTrue(json.containsKey("Body"), "Body field should be present");
      Assertions.assertTrue(json.getString("Body").contains("d35f528e-8cfc-4d97-9078-428da6673f51"));
      Assertions.assertFalse(json.getString("Body").contains("<br />"));
      Assertions.assertTrue(json.getString("Body").contains("<br>"));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testIncludeItems(final JsonSerializer serializerParam) throws Exception {
    this.withResourceDxl("/dxl/testJsonSerialization", database -> {
      final Document doc = database
          .queryFormula(" Marker='testMimeSerialization' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
          .getDocuments()
          .findFirst()
          .get();

      final JsonSerializer serializer = serializerParam
          .includeItems(Arrays.asList("bOdy"));

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");
      Assertions.assertTrue(json.containsKey("Body"), "Body field should be present");
      Assertions.assertTrue(json.getString("Body").contains("final hours extra"));

      // Make sure the received field was excluded
      Assertions.assertFalse(json.containsKey("Received"));
    });
  }

  @Test
  public void testJsonSerializationService() {
    Assertions.assertNotNull(JsonSerializer.createSerializer());
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testLowercaseItemNames(final JsonSerializer serializerParam) throws Exception {
    this.withResourceDxl("/dxl/testJsonSerialization", database -> {
      final Document doc = database
          .queryFormula(" Marker='testMimeSerialization' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
          .getDocuments()
          .findFirst()
          .get();

      final JsonSerializer serializer = serializerParam
          .lowercaseProperties(true);

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");
      Assertions.assertEquals("Bulk Mail", json.getString("x_mxthunder_group"));
      Assertions.assertEquals(
          "<1407518313_SectionID-331186_HitID-1407517453669_SiteID-16268_EmailID-82837855_DB-34_SID-37@ss23.agm1.us>",
          json.getString("x_track_id"));
      Assertions.assertTrue(json.containsKey("body"), "Body field should be present");
      Assertions.assertTrue(json.getString("body").contains("final hours extra"));

      Assertions.assertFalse(json.containsKey("Received"));
      Assertions.assertTrue(json.containsKey("received"));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testMetadata(final JsonSerializer serializerParam) throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "bar");
      doc.replaceItemValue("Bar", "baz");
      doc.save();

      final JsonSerializer serializer = serializerParam
          .includeMetadata(true);

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");
      Assertions.assertEquals("bar", json.getString("Foo"));
      Assertions.assertEquals("baz", json.getString("Bar"));

      Assertions.assertTrue(json.containsKey("@meta"), "JSON should contain a metadata object");
      final JsonObject meta = json.getJsonObject("@meta");
      Assertions.assertNotEquals(null, meta, "Metadata object should not be null");
      Assertions.assertEquals(doc.getNoteID(), meta.getInt(JsonSerializer.PROP_META_NOTEID));
      Assertions.assertEquals(doc.getUNID(), meta.getString(JsonSerializer.PROP_META_UNID));
      final TemporalAccessor cdate = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(meta.getString(JsonSerializer.PROP_META_CREATED));
      Assertions.assertEquals(doc.getCreated().toOffsetDateTime(), OffsetDateTime.from(cdate));
      final TemporalAccessor mdate = DateTimeFormatter.ISO_OFFSET_DATE_TIME
          .parse(meta.getString(JsonSerializer.PROP_META_LASTMODIFIED));
      Assertions.assertEquals(doc.getLastModified().toOffsetDateTime(), OffsetDateTime.from(mdate));
      final TemporalAccessor adate = DateTimeFormatter.ISO_OFFSET_DATE_TIME
          .parse(meta.getString(JsonSerializer.PROP_META_LASTACCESSED));
      Assertions.assertEquals(doc.getLastAccessed().toOffsetDateTime(), OffsetDateTime.from(adate));
      final TemporalAccessor fdate = DateTimeFormatter.ISO_OFFSET_DATE_TIME
          .parse(meta.getString(JsonSerializer.PROP_META_LASTMODIFIEDINFILE));
      Assertions.assertEquals(doc.getModifiedInThisFile().toOffsetDateTime(), OffsetDateTime.from(fdate));
      final TemporalAccessor added = DateTimeFormatter.ISO_OFFSET_DATE_TIME
          .parse(meta.getString(JsonSerializer.PROP_META_ADDEDTOFILE));
      Assertions.assertEquals(doc.getAddedToFile().toOffsetDateTime(), OffsetDateTime.from(added));

      final JsonArray docClassArray = meta.getJsonArray(JsonSerializer.PROP_META_NOTECLASS);
      final Collection<DocumentClass> docClass = docClassArray.stream()
          .map(JsonString.class::cast)
          .map(JsonString::getString)
          .map(DocumentClass::valueOf)
          .collect(Collectors.toSet());
      Assertions.assertEquals(doc.getDocumentClass(), docClass);

    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testMimeSerialization(final JsonSerializer serializer) throws Exception {
    this.withResourceDxl("/dxl/testJsonSerialization", database -> {
      final int noteId = database
          .queryFormula(" Marker='testMimeSerialization' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
          .getNoteIds()
          .get()
          .iterator()
          .next();
      final Document doc = database.getDocumentById(noteId, EnumSet.of(OpenDocumentMode.CONVERT_RFC822_TO_TEXT_AND_TIME)).get();

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");
      Assertions.assertEquals("Bulk Mail", json.getString("X_MXTHUNDER_Group"));
      Assertions.assertTrue(json.containsKey("Body"), "Body field should be present");
      Assertions.assertTrue(json.getString("Body").contains("final hours extra"));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testMimeSerializationNoConvert(final JsonSerializer serializer) throws Exception {
    this.withResourceDxl("/dxl/testJsonSerialization", database -> {
      final Document doc = database
          .queryFormula(" Marker='testMimeSerialization' ", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
          .getDocuments()
          .findFirst()
          .get();

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");
      Assertions.assertEquals("Bulk Mail", json.getString("X_MXTHUNDER_Group"));
      Assertions.assertEquals(
          "<1407518313_SectionID-331186_HitID-1407517453669_SiteID-16268_EmailID-82837855_DB-34_SID-37@ss23.agm1.us>",
          json.getString("X_Track_ID"));
      Assertions.assertTrue(json.containsKey("Body"), "Body field should be present");
      Assertions.assertTrue(json.getString("Body").contains("final hours extra"));
      Assertions.assertTrue(json.containsKey("Received"));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testSerializeBooleans(final JsonSerializer serializerParam) throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "Bar")
          .replaceItemValue("NumberBool", 1)
          .replaceItemValue("NumberBoolFalse", 0)
          .replaceItemValue("YBool", "Y")
          .replaceItemValue("YesBool", "Yes")
          .replaceItemValue("YBoolFalse", "N");

      final JsonSerializer serializer = serializerParam
          .booleanItemNames(Arrays.asList("NumberBool", "NumberBoolFalse", "YBool", "YesBool", "YBoolFalse", "FakeBool"))
          .booleanTrueValues(Arrays.asList("Y", "Yes", 1));

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");
      Assertions.assertThrows(ClassCastException.class, () -> json.getBoolean("Foo"));
      Assertions.assertTrue(json.getBoolean("NumberBool"));
      Assertions.assertFalse(json.getBoolean("NumberBoolFalse"));
      Assertions.assertThrows(NullPointerException.class, () -> json.getBoolean("FakeBool"));
      Assertions.assertTrue(json.getBoolean("YBool"));
      Assertions.assertTrue(json.getBoolean("YesBool"));
      Assertions.assertFalse(json.getBoolean("YBoolFalse"));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testSerializeDocument(final JsonSerializer serializer) throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "Bar");
      final Object json = serializer.toJson(doc);
      Assertions.assertNotNull(json, "JSON result should not be null");
      final String jsonString = String.valueOf(json);
      final JsonParser parser = Json.createParser(new StringReader(jsonString));
      parser.next();
      final JsonObject obj = parser.getObject();
      Assertions.assertEquals("Bar", obj.getString("Foo"));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testSerializeDocumentLowercase(final JsonSerializer serializer) throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "Bar");
      final Object json = serializer
          .lowercaseProperties(true)
          .toJson(doc);
      Assertions.assertNotNull(json, "JSON result should not be null");
      final String jsonString = String.valueOf(json);
      final JsonParser parser = Json.createParser(new StringReader(jsonString));
      parser.next();
      final JsonObject obj = parser.getObject();
      Assertions.assertEquals("Bar", obj.getString("foo"));
    });
  }

  @ParameterizedTest
  @ArgumentsSource(SerializerProvider.class)
  public void testVertxSerialization(final JsonSerializer serializer) throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "bar");
      doc.replaceItemValue("Bar", "baz");

      final String jsonString = serializer.toJsonString(doc);
      Assertions.assertFalse(StringUtil.isEmpty(jsonString), "JSON should not be empty");
      final JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();
      Assertions.assertNotEquals(null, json, "JSON object should not be null");
      Assertions.assertEquals("bar", json.getString("Foo"));
      Assertions.assertEquals("baz", json.getString("Bar"));

      Assertions.assertFalse(json.containsKey("@meta"), "JSON should not contain a metadata object");
    });
  }
}
