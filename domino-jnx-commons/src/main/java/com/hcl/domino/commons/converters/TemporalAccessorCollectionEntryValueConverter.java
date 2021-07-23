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

import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.stream.Collectors;

import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntryValueConverter;
import com.hcl.domino.data.DominoDateTime;

/**
 * Supports reading generic {@link TemporalAccessor} objects from a collection entry
 * 
 * @author Jesse Gallagher
 *
 */
public class TemporalAccessorCollectionEntryValueConverter extends AbstractTemporalAccessorConverter implements CollectionEntryValueConverter {

	@Override
	public boolean supportsRead(Class<?> valueType) {
		return supports(valueType);
	}

	@Override
	public <T> T getValue(CollectionEntry obj, String itemName, Class<T> valueType, T defaultValue) {
		DominoDateTime result = obj.get(itemName, DominoDateTime.class, null);
		return convert(result, valueType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getValueAsList(CollectionEntry obj, String itemName, Class<T> valueType, List<T> defaultValue) {
		List<DominoDateTime> result = obj.getAsList(itemName, DominoDateTime.class, null);
		return result == null ? defaultValue : result.stream().map(dt -> (T)convert(dt, valueType)).collect(Collectors.toList());
	}

	@Override
	public <T> T getValue(CollectionEntry obj, int index, Class<T> valueType, T defaultValue) {
		DominoDateTime result = obj.get(index, DominoDateTime.class, null);
		return convert(result, valueType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getValueAsList(CollectionEntry obj, int index, Class<T> valueType, List<T> defaultValue) {
		List<DominoDateTime> result = obj.getAsList(index, DominoDateTime.class, null);
		return result == null ? defaultValue : result.stream().map(dt -> (T)convert(dt, valueType)).collect(Collectors.toList());
	}
}
