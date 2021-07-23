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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntryValueConverter;
import com.hcl.domino.data.DocumentValueConverter;

/**
 * {@link DocumentValueConverter} implementation that supports converting to
 * and from object array types.
 * 
 * @author Jesse Gallagher
 */
public class ObjectArrayCollectionEntryValueConverter implements CollectionEntryValueConverter {

	@Override
	public boolean supportsRead(Class<?> valueType) {
		return valueType.isArray() && !valueType.getComponentType().isPrimitive();
	}

	@Override
	public <T> T getValue(CollectionEntry obj, String itemName, Class<T> valueType, T defaultValue) {
		Class<?> type = valueType.getComponentType();
		List<?> val = obj.getAsList(itemName, type, null);
		if(val == null) {
			return defaultValue;
		}
		
		@SuppressWarnings("unchecked")
		T result = (T)Array.newInstance(type, val.size());
		for(int i = 0; i < val.size(); i++) {
			Array.set(result, i, val.get(i));
		}
		return result;
	}

	@Override
	public <T> List<T> getValueAsList(CollectionEntry obj, String itemName, Class<T> valueType, List<T> defaultValue) {
		// This is a weird case, and this intentionally should return an e.g. List<Object[]>
		return Arrays.asList(getValue(obj, itemName, valueType, null));
	}
	
	@Override
	public <T> T getValue(CollectionEntry obj, int index, Class<T> valueType, T defaultValue) {
		Class<?> type = valueType.getComponentType();
		List<?> val = obj.getAsList(index, type, null);
		if(val == null) {
			return defaultValue;
		}
		
		@SuppressWarnings("unchecked")
		T result = (T)Array.newInstance(type, val.size());
		for(int i = 0; i < val.size(); i++) {
			Array.set(result, i, val.get(i));
		}
		return result;
	}
	
	@Override
	public <T> List<T> getValueAsList(CollectionEntry obj, int index, Class<T> valueType, List<T> defaultValue) {
		// This is a weird case, and this intentionally should return an e.g. List<Object[]>
		return Arrays.asList(getValue(obj, index, valueType, null));
	}

}
