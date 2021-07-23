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
package com.hcl.domino.misc;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * Container class for a generic pair of two values
 * 
 * @author Karsten Lehmann
 *
 * @param <V1> type of value 1
 * @param <V2> type of value 2
 */
public class Pair<V1,V2> {
	private V1 value1;
	private V2 value2;
	
	public Pair(V1 key, V2 value)  {
		this.value1 = key;
		this.value2 = value;
	}
	
	public V1 getValue1() {
		return value1;
	}
	
	public void setValue1(V1 value) {
		this.value1 = value;
	}
	
	public V2 getValue2() {
		return value2;
	}
	
	public void setValue2(V2 value) {
		this.value2 = value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value1, value2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (!Objects.equals(value1, other.value1)) {
			return false;
		}
		if (!Objects.equals(value2, other.value2)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return MessageFormat.format("Pair [value1={0}, value2={1}]", value1, value2); //$NON-NLS-1$
	}
	
	
}
