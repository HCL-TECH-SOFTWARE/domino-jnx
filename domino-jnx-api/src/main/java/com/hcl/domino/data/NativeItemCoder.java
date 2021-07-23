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

import com.hcl.domino.misc.JNXServiceFinder;

/**
 * This service interface represents an implementation-contributed service that is able to decode
 * item-type data from native memory
 * 
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public interface NativeItemCoder {
	static NativeItemCoder get() {
		return JNXServiceFinder.findRequiredService(NativeItemCoder.class, NativeItemCoder.class.getClassLoader());
	}
	
	List<String> decodeStringList(byte[] buf);
	
	byte[] encodeStringList(List<String> values);
}
