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
package com.hcl.domino.mime;

import java.util.Map;

/**
 * Represents a header within a {@link MimeEntity}.
 */
public interface MimeHeader {
	/**
	 * Retrieves the entity that contains this header.
	 * 
	 * @return the containing {@link MimeEntity}
	 */
	MimeEntity getEntity();
	
	/**
	 * @return the value of this header, without any parameters
	 */
	String getValue();
	
	/**
	 * Sets the value of this header, leaving any parameters intact.
	 * 
	 * @param value the value to set
	 */
	void setValue(String value);
	
	/**
	 * Sets the value of this header, including its parameters.
	 * 
	 * <p>Calling this method invalidates any objects returned by {@link #getParameters()}.</p>
	 * 
	 * @param value the value+parameter string to set
	 */
	void setValueAndParameters(String value);
	
	/**
	 * Sets a parameter of this header, leaving the value intact.
	 * 
	 * @param parameterName the name of the parameter to set
	 * @param value the value of the parameter
	 */
	void setParameter(String parameterName, String value);
	
	/**
	 * Retrieves a view of the parameters of this header.
	 * 
	 * <p>In practice, this map acts similarly to a {@link java.util.LinkedHashMap LinkedHashMap}
	 * in that parameters will be written in the header in the order that they're added.</p>
	 * 
	 * @return a mutable {@link Map} view of the parameters for this header
	 */
	Map<String, String> getParameters();
	
	/**
	 * Removes this header from its containing entity.
	 */
	void remove();
}
