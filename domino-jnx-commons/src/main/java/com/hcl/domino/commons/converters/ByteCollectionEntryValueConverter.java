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

import com.hcl.domino.data.DocumentValueConverter;

/**
 * {@link DocumentValueConverter} implementation that supports converting boolean values
 * to and from 0d and 1d.
 * 
 * @author Jesse Gallagher
 */
public class ByteCollectionEntryValueConverter extends AbstractPrimitiveCollectionEntryValueConverter<Byte> {
	
	@Override
	protected Class<Byte> getBoxedClass() {
		return Byte.class;
	}
	@Override
	protected Class<?> getPrimitiveClass() {
		return byte.class;
	}
	@Override
	protected Byte convertFromDouble(double value) {
		return (byte)value;
	}
	@Override
	protected double convertToDouble(Byte value) {
		return value;
	}
}
