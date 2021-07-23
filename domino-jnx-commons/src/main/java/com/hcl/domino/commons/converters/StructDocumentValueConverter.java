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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentValueConverter;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

/**
 * {@link DocumentValueConverter} implementation that allows reading items as instances of
 * {@link MemoryStructure} sub-interfaces.
 * 
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class StructDocumentValueConverter implements DocumentValueConverter {

	@Override
	public boolean supportsRead(Class<?> valueType) {
		return MemoryStructure.class.isAssignableFrom(valueType) && !MemoryStructure.class.equals(valueType);
	}

	@Override
	public boolean supportsWrite(Class<?> valueType, Object value) {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(Document obj, String itemName, Class<T> valueType, T defaultValue) {
		ByteBuffer buf = obj.get(itemName, ByteBuffer.class, null);
		if(buf == null) {
			return defaultValue;
		}
		// Chop off the data type
		buf.position(2);
		buf = buf.slice().order(ByteOrder.nativeOrder());
		return (T)MemoryStructureWrapperService.get().wrapStructure((Class<? extends MemoryStructure>)valueType, buf);
	}

	@Override
	public <T> List<T> getValueAsList(Document obj, String itemName, Class<T> valueType, List<T> defaultValue) {
		throw new NotYetImplementedException();
	}

	@Override
	public <T> void setValue(Document obj, String itemName, T newValue) {
		throw new UnsupportedOperationException();
	}

}
