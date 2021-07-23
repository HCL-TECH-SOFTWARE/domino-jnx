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
package com.hcl.domino.commons.converters;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.hcl.domino.data.DominoDateTime;

public abstract class AbstractTemporalAccessorConverter {
	private static final Map<Predicate<Class<?>>, Function<DominoDateTime, TemporalAccessor>> accessors = new HashMap<>();
	static {
		accessors.put(c -> c.equals(TemporalAccessor.class), dt -> dt);
		accessors.put(c -> c.equals(Temporal.class), dt -> dt);
		accessors.put(c -> ChronoLocalDate.class.isAssignableFrom(c), DominoDateTime::toLocalDate);
		accessors.put(c -> LocalTime.class.isAssignableFrom(c), DominoDateTime::toLocalTime);
		accessors.put(c -> ChronoLocalDateTime.class.isAssignableFrom(c), dt -> LocalDateTime.from(dt));
		accessors.put(c -> OffsetDateTime.class.isAssignableFrom(c), DominoDateTime::toOffsetDateTime);
		accessors.put(c -> Instant.class.isAssignableFrom(c), dt -> Instant.from(dt));
	}

	protected boolean supports(Class<?> clazz) {
		return accessors.keySet().stream().anyMatch(p -> p.test(clazz));
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T convert(DominoDateTime val, Class<?> clazz) {
		return (T)accessors.entrySet()
			.stream()
			.filter(entry -> entry.getKey().test(clazz))
			.findFirst()
			.map(entry -> entry.getValue().apply(val))
			.orElse(null);
	}
}
