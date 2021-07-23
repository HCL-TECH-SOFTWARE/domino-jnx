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
 * Supports reading and writing {@code char} and {@link Character} values as single-element
 * strings.
 * 
 * @since 1.0.18
 */
public class CharacterDocumentValueConverter implements DocumentValueConverter {

	@Override
	public boolean supportsRead(Class<?> valueType) {
		return Character.class.equals(valueType) || char.class.equals(valueType);
	}

	@Override
	public boolean supportsWrite(Class<?> valueType, Object value) {
		if(Iterable.class.isAssignableFrom(valueType)) {
			Object firstVal = ((Iterable<?>)value).iterator().next();
			return Character.class.isInstance(firstVal);
		} else {
			return Character.class.equals(valueType) || char.class.equals(valueType);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(Document obj, String itemName, Class<T> valueType, T defaultValue) {
		String valString = obj.getAsText(itemName, ' ');
		if(valString == null || valString.isEmpty()) {
			return defaultValue;
		} else {
			return (T)Character.valueOf(valString.charAt(0));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getValueAsList(Document obj, String itemName, Class<T> valueType, List<T> defaultValue) {
		String valString = obj.getAsText(itemName, ' ');
		if(valString == null || valString.isEmpty()) {
			return defaultValue;
		} else {
			char[] chars = valString.toCharArray();
			List<Character> result = new ArrayList<>(chars.length);
			for(char c : chars) {
				result.add(c);
			}
			return (List<T>)result;
		}
	}

	@Override
	public <T> void setValue(Document obj, String itemName, T newValue) {
		if(newValue instanceof Iterable) {
			String newVal = StreamSupport.stream(((Iterable<?>) newValue).spliterator(), false).map(o -> o == null ? "" : o.toString()).collect(Collectors.joining()); //$NON-NLS-1$
			obj.replaceItemValue(itemName, newVal);
		} else {
			obj.replaceItemValue(itemName, newValue == null ? "" : newValue.toString()); //$NON-NLS-1$
		}
	}

}
