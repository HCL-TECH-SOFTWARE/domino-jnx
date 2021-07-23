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
package com.hcl.domino.jna.internal.converters;

import java.text.MessageFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.hcl.domino.commons.converters.AbstractTemporalAccessorConverter;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentValueConverter;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.jna.data.JNADominoDateTime;

/**
 * Supports writing generic {@link TemporalAccessor} objects to a document and reading
 * them back by way of {@link DominoDateTime}.
 * 
 * @author Jesse Gallagher
 *
 */
public class TemporalAccessorDocumentValueConverter extends AbstractTemporalAccessorConverter implements DocumentValueConverter {

	@Override
	public boolean supportsRead(Class<?> valueType) {
		return supports(valueType);
	}

	@Override
	public boolean supportsWrite(Class<?> valueType, Object value) {
		if(TemporalAccessor.class.isAssignableFrom(valueType)) {
			return true;
		} else if(Iterable.class.isAssignableFrom(valueType)) {
			return StreamSupport.stream(((Iterable<?>)value).spliterator(), false)
				.allMatch(TemporalAccessor.class::isInstance);
		} else {
			return false;
		}
	}

	@Override
	public <T> T getValue(Document obj, String itemName, Class<T> valueType, T defaultValue) {
		DominoDateTime result = obj.get(itemName, DominoDateTime.class, null);
		return convert(result, valueType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getValueAsList(Document obj, String itemName, Class<T> valueType, List<T> defaultValue) {
		List<DominoDateTime> result = obj.getAsList(itemName, DominoDateTime.class, null);
		return result == null ? defaultValue : result.stream().map(dt -> (T)convert(dt, valueType)).collect(Collectors.toList());
	}

	@Override
	public <T> void setValue(Document obj, String itemName, T newValue) {
		if(newValue == null) {
			obj.removeItem(itemName);
		} else if(newValue instanceof Iterable) {
			List<DominoDateTime> val = StreamSupport.stream(((Iterable<?>)newValue).spliterator(), false)
				.map(this::toDominoFriendly)
				.map(JNADominoDateTime::new)
				.collect(Collectors.toList());
			obj.replaceItemValue(itemName, val);
		} else {
			Temporal val = toDominoFriendly(newValue);
			obj.replaceItemValue(itemName, new JNADominoDateTime(val));
		}
	}
	
	private Temporal toDominoFriendly(Object newValue) {
		Temporal val = null;
		try {
			val = ZonedDateTime.from((TemporalAccessor)newValue);
		} catch(DateTimeException e) { }
		if(val == null) {
			try {
				val = OffsetDateTime.from((TemporalAccessor)newValue);
			} catch(DateTimeException e) { }
		}
		if(val == null) {
			try {
				val = LocalDate.from((TemporalAccessor)newValue);
			} catch(DateTimeException e) { }
		}
		if(val == null) {
			try {
				val = LocalTime.from((TemporalAccessor)newValue);
			} catch(DateTimeException e) { }
		}
		if(val == null) {
			throw new IllegalArgumentException(MessageFormat.format("Unable to convert value of class {0} to a known Temporal type", newValue.getClass().getName()));
		}
		return val;
	}

}
