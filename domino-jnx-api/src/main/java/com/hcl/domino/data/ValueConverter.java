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
package com.hcl.domino.data;

import java.util.List;

/**
 * Generic value converter interface
 * 
 * @author Karsten Lehmann
 *
 * @param <OBJ> type of object containing the data
 */
public interface ValueConverter<OBJ> {


	/**
	 * Return <code>true</code> here for all value types that
	 * the implementation class supports to read object
	 * data.
	 * 
	 * @param valueType requested return value type
	 * @return true if supported type
	 */
	boolean supportsRead(Class<?> valueType);

	/**
	 * Return <code>true</code> here for all value types that
	 * the implementation class supports to write object
	 * data.
	 * 
	 * @param valueType requested return value type
	 * @param value the requested value to write (may be null)
	 * @return true if supported type
	 */
	boolean supportsWrite(Class<?> valueType, Object value);

	/**
	 * Implement this method to return the converted value type.<br>
	 * Will only be invoked if {@link #supportsRead(Class)} returns
	 * true.
	 * 
	 * @param <T> value type
	 * @param obj object
	 * @param itemName item to read
	 * @param valueType requested return value type
	 * @param defaultValue default value to return if object property is not set
	 * @return return value or null
	 */
	<T> T getValue(OBJ obj, String itemName, Class<T> valueType, T defaultValue);
	
	/**
	 * Implement this method to return a list of the converted value type.<br>
	 * Will only be invoked if {@link #supportsRead(Class)} returns
	 * true.
	 * 
	 * @param <T> value type
	 * @param obj object
	 * @param itemName item to read
	 * @param valueType requested return value type
	 * @param defaultValue default value to return if object property is not set
	 * @return return value or null
	 */
	<T> List<T> getValueAsList(OBJ obj, String itemName, Class<T> valueType, List<T> defaultValue);

	/**
	 * Implement this method to write a value to the object
	 * 
	 * @param <T> value type
	 * @param obj object
	 * @param itemName name of item to write
	 * @param newValue new value
	 */
	<T> void setValue(OBJ obj, String itemName, T newValue);

	/**
	 * Determines the relative priority of this converter compared to other converters
	 * of the same type.
	 * 
	 * <p>The default value is {@code 0} and higher values take precedence.</p>
	 * 
	 * @return the relative priority value of this converter
	 */
	default int getPriority() {
		return 0;
	}
}
