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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentValueConverter;

/**
 * Shared logic for {@link DocumentValueConverter} implementations that handle conversion
 * to and from Java primitive builtins.
 * 
 * @param <BOX> the boxed value that the subclass handles
 * @author Jesse Gallagher
 */
public abstract class AbstractPrimitiveDocumentValueConverter<BOX> implements DocumentValueConverter {


	@Override
	public boolean supportsRead(Class<?> valueType) {
		return getPrimitiveClass().equals(valueType) || getBoxedClass().equals(valueType);
	}

	@Override
	public boolean supportsWrite(Class<?> valueType, Object value) {
		if(Iterable.class.isAssignableFrom(valueType)) {
			Object firstVal = ((Iterable<?>)value).iterator().next();
			return getBoxedClass().isInstance(firstVal);
		} else {
			return getPrimitiveClass().equals(valueType) || getBoxedClass().equals(valueType);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(Document obj, String itemName, Class<T> valueType, T defaultValue) {
		double result = obj.get(itemName, Double.class, 0d);
		return (T)convertFromDouble(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getValueAsList(Document obj, String itemName, Class<T> valueType, List<T> defaultValue) {
		return obj.getAsList(itemName, Double.class, new ArrayList<>()).stream()
				.map(this::convertFromDouble)
				.map(b -> (T)b)
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void setValue(Document obj, String itemName, T newValue) {
		if(newValue instanceof Iterable) {
			List<Double> listVal = StreamSupport.stream(((Iterable<BOX>)newValue).spliterator(), false)
				.map(this::convertToDouble)
				.collect(Collectors.toList());
			obj.replaceItemValue(itemName, listVal);;
		} else {
			obj.replaceItemValue(itemName, convertToDouble((BOX)newValue));
		}
	}

	protected abstract Class<?> getPrimitiveClass();
	protected abstract Class<BOX> getBoxedClass();
	protected abstract BOX convertFromDouble(double value);
	protected abstract double convertToDouble(BOX value);
}
