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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentValueConverter;

/**
 * {@link DocumentValueConverter} implementation that supports converting to
 * and from primitive array types.
 * 
 * @author Jesse Gallagher
 */
public class PrimitiveArrayDocumentValueConverter implements DocumentValueConverter {
	@Override
	public boolean supportsRead(Class<?> valueType) {
		return valueType.isArray() && valueType.getComponentType().isPrimitive();
	}

	@Override
	public boolean supportsWrite(Class<?> valueType, Object value) {
		return valueType.isArray() && valueType.getComponentType().isPrimitive();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(Document obj, String itemName, Class<T> valueType, T defaultValue) {
		Class<?> type = valueType.getComponentType();
		Class<?> boxed = getBoxedClass(type);
		List<?> val = obj.getAsList(itemName, boxed, null);
		if(val == null) {
			return defaultValue;
		}
		
		T result = (T)Array.newInstance(type, val.size());
		for(int i = 0; i < val.size(); i++) {
			Array.set(result, i, val.get(i));
		}
		return result;
	}

	@Override
	public <T> List<T> getValueAsList(Document obj, String itemName, Class<T> valueType, List<T> defaultValue) {
		// This is a weird case, and this intentionally should return an e.g. List<int[]>
		return Arrays.asList(getValue(obj, itemName, valueType, null));
	}

	@Override
	public <T> void setValue(Document obj, String itemName, T newValue) {
		// Pour it into a List
		int length = Array.getLength(newValue);
		List<Object> listVal = new ArrayList<>(length);
		for(int i = 0; i < length; i++) {
			listVal.add(Array.get(newValue, i));
		}
		obj.replaceItemValue(itemName, listVal);
	}

	private Class<?> getBoxedClass(Class<?> clazz) {
		if(boolean.class.equals(clazz)) {
			return Boolean.class;
		} else if(char.class.equals(clazz)) {
			return Character.class;
		} else if(byte.class.equals(clazz)) {
			return Byte.class;
		} else if(short.class.equals(clazz)) {
			return Short.class;
		} else if(int.class.equals(clazz)) {
			return Integer.class;
		} else if(long.class.equals(clazz)) {
			return Long.class;
		} else if(float.class.equals(clazz)) {
			return Float.class;
		} else if(double.class.equals(clazz)) {
			return Double.class;
		} else {
			throw new IllegalArgumentException(MessageFormat.format("Unsupported {0}", clazz));
		}
	}
}
