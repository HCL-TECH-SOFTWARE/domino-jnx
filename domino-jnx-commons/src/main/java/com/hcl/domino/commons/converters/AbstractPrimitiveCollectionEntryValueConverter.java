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

import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntryValueConverter;
import com.hcl.domino.data.DocumentValueConverter;

/**
 * Shared logic for {@link DocumentValueConverter} implementations that handle conversion
 * to and from Java primitive builtins.
 * 
 * @param <BOX> the boxed value that the subclass handles
 * @author Jesse Gallagher
 */
public abstract class AbstractPrimitiveCollectionEntryValueConverter<BOX> implements CollectionEntryValueConverter {


	@Override
	public boolean supportsRead(Class<?> valueType) {
		return getPrimitiveClass().equals(valueType) || getBoxedClass().equals(valueType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(CollectionEntry obj, String itemName, Class<T> valueType, T defaultValue) {
		double result = obj.get(itemName, Double.class, 0d);
		return (T)convertFromDouble(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getValueAsList(CollectionEntry obj, String itemName, Class<T> valueType, List<T> defaultValue) {
		return obj.getAsList(itemName, Double.class, new ArrayList<>()).stream()
				.map(this::convertFromDouble)
				.map(b -> (T)b)
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(CollectionEntry obj, int index, Class<T> valueType, T defaultValue) {
		double result = obj.get(index, Double.class, 0d);
		return (T)convertFromDouble(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getValueAsList(CollectionEntry obj, int index, Class<T> valueType, List<T> defaultValue) {
		return obj.getAsList(index, Double.class, new ArrayList<>()).stream()
				.map(this::convertFromDouble)
				.map(b -> (T)b)
				.collect(Collectors.toList());
	}

	protected abstract Class<?> getPrimitiveClass();
	protected abstract Class<BOX> getBoxedClass();
	protected abstract BOX convertFromDouble(double value);
	protected abstract double convertToDouble(BOX value);
}
