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
package com.hcl.domino.commons.mime;

import java.util.EnumSet;

import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.misc.NotesConstants;

/**
 * Flags for {@link ItemDataType#TYPE_MIME_PART} items
 * 
 * @author Karsten Lehmann
 */
public enum MimePartOptions {
	
	/** Mime part has boundary. */
	HAS_BOUNDARY (NotesConstants.MIME_PART_HAS_BOUNDARY),
	
	/** Mime part has headers. */
	HAS_HEADERS (NotesConstants.MIME_PART_HAS_HEADERS),

	/** Mime part has body in database object. */
	BODY_IN_DBOBJECT (NotesConstants.MIME_PART_BODY_IN_DBOBJECT),
	
	/** Mime part has shared database object. Used only with MIME_PART_BODY_IN_DBOBJECT. */
	SHARED_DBOBJECT (NotesConstants.MIME_PART_SHARED_DBOBJECT),

	/** Skip for conversion, only used during MIME-&gt;CD conversion. */
	SKIP_FOR_CONVERSION (NotesConstants.MIME_PART_SKIP_FOR_CONVERSION);
	
	private int m_val;
	
	MimePartOptions(int val) {
		m_val = val;
	}
	
	public int getValue() {
		return m_val;
	}
	
	public static short toBitMask(EnumSet<MimePartOptions> findSet) {
		int result = 0;
		if (findSet!=null) {
			for (MimePartOptions currFind : values()) {
				if (findSet.contains(currFind)) {
					result = result | currFind.getValue();
				}
			}
		}
		return (short) (result & 0xffff);
	}
	
	public static int toBitMaskInt(EnumSet<MimePartOptions> findSet) {
		int result = 0;
		if (findSet!=null) {
			for (MimePartOptions currFind : values()) {
				if (findSet.contains(currFind)) {
					result = result | currFind.getValue();
				}
			}
		}
		return result;
	}

}
