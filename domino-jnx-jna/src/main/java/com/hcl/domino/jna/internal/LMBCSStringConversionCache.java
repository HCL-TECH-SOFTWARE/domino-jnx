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

/**
 * Cache to optimize performance of LMBCS String conversion to Java Strings.
 * 
 * @author Karsten Lehmann
 */
public class LMBCSStringConversionCache {
	//use simple cache for lmbcs-string conversion of short string
	private static final boolean USE_LMBCS2STRING_CACHE = true;
	//max length of each lmbcs-string cache entry in bytes
	private static final int MAX_LMBCS2STRING_KEY_LENGTH = 1000;

	private static final int MAX_LMBCS2STRING_SIZE_BYTES = 1000000;

	private static SizeLimitedLRUCache<LMBCSString,String> LMBCS2STRINGCACHE = new SizeLimitedLRUCache<LMBCSString,String>(MAX_LMBCS2STRING_SIZE_BYTES) {
		@Override
		protected int computeSize(LMBCSString key, String value) {
			return key.size() + value.length()*2;
		}
	};

	public static long getCacheSize() {
		return LMBCS2STRINGCACHE.getCurrentCacheSizeInUnits();
	}

	/**
	 * Converts an LMBCS string to a Java String. If already cached, no native call is made.
	 * 
	 * @param lmbcsString LMBCS string
	 * @return converted string
	 */
	public static String get(LMBCSString lmbcsString) {
		String stringFromCache = LMBCS2STRINGCACHE.get(lmbcsString);
		String convertedString;
		
		if (stringFromCache==null) {
			byte[] dataArr = lmbcsString.getData();
			
			convertedString = NotesStringUtils.fromLMBCS(dataArr);
			if (USE_LMBCS2STRING_CACHE && lmbcsString.size()<=MAX_LMBCS2STRING_KEY_LENGTH) {
				LMBCS2STRINGCACHE.put(lmbcsString, convertedString);
			}
		}
		else {
			convertedString = stringFromCache;
		}
		return convertedString;
	}
}
