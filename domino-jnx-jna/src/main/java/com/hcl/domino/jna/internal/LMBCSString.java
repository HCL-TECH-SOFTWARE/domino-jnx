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
package com.hcl.domino.jna.internal;

import java.util.Arrays;

/**
 * Utility class that lazily converts a string from LMBCS format to Java String
 * 
 * @author Karsten Lehmann
 */
public class LMBCSString {
	private String m_strValue;
	private byte[] m_data;
	private int m_hashCode;
	
	/**
	 * Creates a new instance
	 * 
	 * @param data data in LMBCS format
	 */
	public LMBCSString(byte[] data) {
		m_data = data;
	}
	
	/**
	 * Returns the size of the internal data in bytes
	 * 
	 * @return size
	 */
	public int size() {
		return m_data.length;
	}
	
	public byte[] getData() {
		return m_data;
	}
	
	@Override
	public int hashCode() {
		if (m_hashCode==0) {
			m_hashCode = Arrays.hashCode(m_data);
		}
		return m_hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LMBCSString) {
			LMBCSString otherLMBCSStr = (LMBCSString) obj;
			
			if (m_hashCode!=0 && otherLMBCSStr.m_hashCode!=0 && m_hashCode!=otherLMBCSStr.m_hashCode) {
				return false;
			}
			
			boolean equal = Arrays.equals(m_data, ((LMBCSString)obj).m_data);
			return equal;
		}
		return false;
	}
	
	/**
	 * Returns the string value. Converts from LMBCS on the first call.
	 * 
	 * @return value
	 */
	public String getValue() {
		if (m_strValue==null) {
			m_strValue = LMBCSStringConversionCache.get(this);
		}
		return m_strValue;
	}

	@Override
	public String toString() {
		return getValue();
	}
}
