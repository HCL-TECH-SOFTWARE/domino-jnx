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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDocumentValueConversion extends AbstractNotesRuntimeTest {

	@Test
	public void testStringRoundTrip() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", "Bar");
			assertEquals("Bar", doc.get("Foo", String.class, null));
			assertArrayEquals(new String[] { "Bar" }, doc.get("Foo", String[].class, null));
			assertEquals(Arrays.asList("Bar"), doc.get("Foo", List.class, null));
		});
	}
	
	@Test
	public void testStringArrayRoundTrip() throws Exception {
		String[] expected = new String[] { "foo", "bar", "baz" };
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Foo", expected);
			assertEquals(Arrays.asList(expected), doc.getAsList("Foo", String.class, null));
			assertArrayEquals(expected, doc.get("Foo", String[].class, null));
			assertEquals(Arrays.asList(expected), doc.get("Foo", List.class, null));
		});
	}
	
	@Test
	public void testBooleanRoundTrip() throws Exception {
		boolean expected = true;
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertEquals(expected, doc.get("Foo", Boolean.class, null));
			assertEquals(expected, doc.get("Foo", boolean.class, null));
			assertEquals(Arrays.asList(expected), doc.getAsList("Foo", Boolean.class, null));
		});
	}
	
	@Test
	public void testBooleanArrayRoundTrip() throws Exception {
		boolean[] expected = new boolean[] { true, false, true};
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertEquals(Arrays.asList(true, false, true), doc.getAsList("Foo", Boolean.class, null));
			assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));
			
			List<Boolean> list = Arrays.asList(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE);
			assertEquals(list, doc.getAsList("Foo", Boolean.class, null));
		});
	}
	
	@Test
	public void testByteRoundTrip() throws Exception {
		byte expected = 1;
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertEquals(expected, doc.get("Foo", Byte.class, null));
			assertEquals(expected, doc.get("Foo", byte.class, null));
			assertEquals(Arrays.asList(expected), doc.getAsList("Foo", Byte.class, null));
		});
	}
	
	@Test
	public void testByteArrayRoundTrip() throws Exception {
		byte[] expected = new byte[] { 1, 2, 3};
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));
			assertEquals(Arrays.asList((byte)1, (byte)2, (byte)3), doc.getAsList("Foo", Byte.class, null));
		});
	}

	@Test
	public void testShortRoundTrip() throws Exception {
		short expected = 43;
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertEquals(expected, doc.get("Foo", Short.class, null));
			assertEquals(expected, doc.get("Foo", short.class, null));
			assertEquals(Arrays.asList(expected), doc.getAsList("Foo", Short.class, null));
		});
	}
	
	@Test
	public void testShortArrayRoundTrip() throws Exception {
		short[] expected = new short[] { 1, 2, 3};
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));
			assertEquals(Arrays.asList((short)1, (short)2, (short)3), doc.getAsList("Foo", Short.class, null));
		});
	}

	@Test
	public void testIntRoundTrip() throws Exception {
		int expected = 3;
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertEquals(expected, doc.get("Foo", int.class, null));
			assertArrayEquals(new int[] { expected }, doc.get("Foo", int[].class, null));
			assertEquals(Integer.valueOf(expected), doc.get("Foo", Integer.class, null));
		});
	}
	
	@Test
	public void testIntArrayRoundTrip() throws Exception {
		int[] expected = new int[] { 1, 2, 3};
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertEquals(Arrays.asList(1d, 2d, 3d), doc.get("Foo", List.class, null));
			assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));
			
			List<Integer> list = Arrays.stream(expected).mapToObj(Integer::valueOf).collect(Collectors.toList());
			assertEquals(list, doc.getAsList("Foo", Integer.class, null));
		});
	}

	@Test
	public void testLongRoundTrip() throws Exception {
		long expected = 8;
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertEquals(expected, doc.get("Foo", long.class, null));
			assertArrayEquals(new long[] { expected }, doc.get("Foo", long[].class, null));
			assertEquals(Long.valueOf(expected), doc.get("Foo", Long.class, null));
		});
	}
	
	@Test
	public void testLongArrayRoundTrip() throws Exception {
		long[] expected = new long[] { 1, 2, 3};
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertEquals(Arrays.asList(1d, 2d, 3d), doc.get("Foo", List.class, null));
			assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));
			
			List<Long> list = Arrays.stream(expected).mapToObj(Long::valueOf).collect(Collectors.toList());
			assertEquals(list, doc.getAsList("Foo", Long.class, null));
		});
	}

	@Test
	public void testFloatRoundTrip() throws Exception {
		float expected = 8.1f;
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertEquals(expected, doc.get("Foo", float.class, null));
			assertArrayEquals(new float[] { expected }, doc.get("Foo", float[].class, null));
			assertEquals(Float.valueOf(expected), doc.get("Foo", Float.class, null));
		});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testFloatArrayRoundTrip() throws Exception {
		float[] expected = new float[] { 1.3f, 2.3f, 3.3f};
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			// Round the result for precision
			List<Double> fetched = ((List<Double>)doc.get("Foo", List.class, null)).stream()
				.map(d -> Math.round(d * 10) / 10d)
				.collect(Collectors.toList());
			assertEquals(Arrays.asList(1.3, 2.3, 3.3), fetched);
			assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));
		});
	}

	@Test
	public void testDoubleRoundTrip() throws Exception {
		double expected = 8.3;
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertEquals(expected, doc.get("Foo", double.class, null));
			assertArrayEquals(new double[] { expected }, doc.get("Foo", double[].class, null));
			assertEquals(Double.valueOf(expected), doc.get("Foo", Double.class, null));
		});
	}
	
	@Test
	public void testDoubleArrayRoundTrip() throws Exception {
		double[] expected = new double[] { 1.3, 2.3, 3.3 };
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertEquals(Arrays.asList(1.3, 2.3, 3.3), doc.get("Foo", List.class, null));
			assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));
			
			List<Double> list = Arrays.stream(expected).mapToObj(Double::valueOf).collect(Collectors.toList());
			assertEquals(list, doc.getAsList("Foo", Double.class, null));
		});
	}

	@Test
	public void testCharRoundTrip() throws Exception {
		char expected = 'f';
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertEquals(expected, doc.get("Foo", char.class, null));
			assertArrayEquals(new char[] { expected }, doc.get("Foo", char[].class, null));
			assertEquals(Character.valueOf(expected), doc.get("Foo", Character.class, null));
		});
	}
	
	@Test
	public void testCharArrayRoundTrip() throws Exception {
		char[] expected = new char[] { 'f', 'o', 'o' };
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			assertArrayEquals(expected, doc.get("Foo", expected.getClass(), null));
			
			List<Character> list = Arrays.asList('f', 'o', 'o');
			assertEquals(list, doc.getAsList("Foo", Character.class, null));
		});
	}
	
	@Test
	public void testDateRoundTrip() throws Exception {
		TemporalAccessor expected = LocalDate.of(2020, 7, 14);
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			TemporalAccessor date = doc.get("Foo", TemporalAccessor.class, null);
			assertEquals(LocalDate.from(expected), LocalDate.from(date));
		});
	}
	
	@Test
	public void testTimeRoundTrip() throws Exception {
		TemporalAccessor expected = LocalTime.of(13, 57, 01);
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			TemporalAccessor date = doc.get("Foo", TemporalAccessor.class, null);
			assertEquals(LocalTime.from(expected), LocalTime.from(date));
		});
	}
	
	@Test
	public void testDateTimeRoundTrip() throws Exception {
		LocalDate expectedD = LocalDate.of(2020, 7, 14);
		LocalTime expectedT = LocalTime.of(13, 57, 01);
		TemporalAccessor expected = OffsetDateTime.of(expectedD, expectedT, ZoneOffset.UTC);
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			TemporalAccessor date = doc.get("Foo", TemporalAccessor.class, null);
			assertEquals(OffsetDateTime.from(expected), OffsetDateTime.from(date));
		});
	}
	
	@Test
	public void testStoreParsedDate() throws Exception {
		TemporalAccessor expected = DateTimeFormatter.ISO_DATE.parse("2020-07-14");
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			DominoDateTime date = doc.get("Foo", DominoDateTime.class, null);
			assertEquals(LocalDate.from(expected), date.toLocalDate());
			assertThrows(DateTimeException.class, () -> date.toLocalTime());
		});
	}
	
	@Test
	public void testStoreParsedTime() throws Exception {
		TemporalAccessor expected = DateTimeFormatter.ISO_TIME.parse("13:57:01");
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			DominoDateTime date = doc.get("Foo", DominoDateTime.class, null);
			assertEquals(LocalTime.from(expected), date.toLocalTime());
			assertThrows(DateTimeException.class, () -> date.toLocalDate());
		});
	}
	
	@Test
	public void testStoreParsedDateTime() throws Exception {
		TemporalAccessor expected = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2020-07-14T13:57:01-05:00");
		
		withTempDb(database -> {
			Document doc = database.createDocument();
			
			doc.replaceItemValue("Foo", expected);
			DominoDateTime date = doc.get("Foo", DominoDateTime.class, null);
			assertEquals(OffsetDateTime.from(expected), date.toOffsetDateTime());
		});
	}
}
