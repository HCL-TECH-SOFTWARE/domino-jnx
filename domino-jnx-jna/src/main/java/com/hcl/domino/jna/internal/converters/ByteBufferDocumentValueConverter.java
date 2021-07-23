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

import java.util.List;

import java.nio.ByteBuffer;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentValueConverter;
import com.hcl.domino.jna.data.JNAItem;
import com.hcl.domino.jna.internal.DisposableMemory;

/**
 * {@link DocumentValueConverter} implementation that supports read-only access to item data as
 * a {@link ByteBuffer}, including the type flag.
 * 
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class ByteBufferDocumentValueConverter implements DocumentValueConverter {

	@Override
	public boolean supportsRead(Class<?> valueType) {
		return ByteBuffer.class.equals(valueType);
	}

	@Override
	public boolean supportsWrite(Class<?> valueType, Object value) {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(Document obj, String itemName, Class<T> valueType, T defaultValue) {
		JNAItem item = (JNAItem)obj.getFirstItem(itemName).orElse(null);
		if(itemName == null) {
			return defaultValue;
		}
		
		DisposableMemory itemVal = item.getValueRaw(true);
		return (T)itemVal.getByteBuffer(0, itemVal.size());
	}

	@Override
	public <T> List<T> getValueAsList(Document obj, String itemName, Class<T> valueType, List<T> defaultValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void setValue(Document obj, String itemName, T newValue) {
		throw new UnsupportedOperationException();
	}

}
