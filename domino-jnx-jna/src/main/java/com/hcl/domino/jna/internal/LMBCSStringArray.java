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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Function;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;

/** 
 * Utility class to write a const char*[] of LMBCS encoded strings.
 * Code partially copied and modified from JNA's {@link StringArray}.
 */
public class LMBCSStringArray extends Memory implements Function.PostCallRead {
	private List<Memory> natives = new ArrayList<>();
	private Object[] original;

	public LMBCSStringArray(Object[] strValues) {
		super((strValues.length + 1) * Native.POINTER_SIZE);
		this.original = strValues;

		for (int i=0; i < strValues.length;i++) {
			Pointer p = null;
			if (strValues[i] != null) {
				Memory currStrMem = NotesStringUtils.toLMBCS(strValues[i].toString(), true);
				natives.add(currStrMem);
				p = currStrMem;
			}
			setPointer(Native.POINTER_SIZE * i, p);
		}
		setPointer(Native.POINTER_SIZE * strValues.length, null);
	}

	@Override
	public void read() {
		for (int i=0;i < original.length;i++) {
			Pointer p = getPointer(i * Native.POINTER_SIZE);
			Object s = null;
			if (p != null) {
				s = NotesStringUtils.fromLMBCS(p, -1);
			}
			original[i] = s;
		}
	}

	@Override
	public String toString() {
		String s = "const char*[]"; //$NON-NLS-1$
		s += Arrays.asList(original);
		return s;
	}
}