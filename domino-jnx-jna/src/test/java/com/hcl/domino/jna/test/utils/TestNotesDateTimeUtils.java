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
package com.hcl.domino.jna.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.test.AbstractJNARuntimeTest;

public class TestNotesDateTimeUtils extends AbstractJNARuntimeTest {
	// Tue Mar 17 13:39:40 EDT 2020
	private static final long epochTime = 1584466780440l;
	
	public static class JavaUtilDatesProvider implements ArgumentsProvider {
		@SuppressWarnings("deprecation")
		@Override public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(
				new Date(epochTime),
				new Date(1900-1900, 1-1, 1, 0, 0, 0),
				new Date(1980-1900, 1-1, 1, 0, 0, 0)
			).map(Arguments::of);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(JavaUtilDatesProvider.class)
	public void testRoundTripJavaUtilDateUtil(Date date) {
		DominoDateTime dt = new JNADominoDateTime(date.getTime());
		assertNotNull(dt);
		assertEquals(date, Date.from(dt.toOffsetDateTime().toInstant()));
		assertTrue(dt.toOffsetDateTime().get(ChronoField.YEAR) >= 1899);

		DominoClient client = getClient();
		DominoDateTime dt2 = client.createDateTime(date.toInstant());
		assertEquals(dt, dt2);
	}
}
