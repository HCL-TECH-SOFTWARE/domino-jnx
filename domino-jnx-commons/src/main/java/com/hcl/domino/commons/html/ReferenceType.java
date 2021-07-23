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
package com.hcl.domino.commons.html;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.hcl.domino.misc.NotesConstants;

public enum ReferenceType {

	/**
	 * unknown purpose
	 */
	UNKNOWN(NotesConstants.HTMLAPI_REF_UNKNOWN),
	
	/**
	 * A tag HREF= value
	 */
	HREF(NotesConstants.HTMLAPI_REF_HREF),
	
	/**
	 * IMG tag SRC= value
	 */
	IMG(NotesConstants.HTMLAPI_REF_IMG),

	/**
	 * (I)FRAME tag SRC= value
	 */
	FRAME(NotesConstants.HTMLAPI_REF_FRAME),
	
	/**
	 * Java applet reference
	 */
	APPLET(NotesConstants.HTMLAPI_REF_APPLET),

	/**
	 * plugin SRC= reference
	 */
	EMBED(NotesConstants.HTMLAPI_REF_EMBED),
	
	/**
	 * active object DATA= referendce
	 */
	OBJECT(NotesConstants.HTMLAPI_REF_OBJECT),

	/**
	 * BASE tag value
	 */
	BASE(NotesConstants.HTMLAPI_REF_BASE),
	
	/**
	 * BODY BACKGROUND
	 */
	BACKGROUND(NotesConstants.HTMLAPI_REF_BACKGROUND),
	
	/**
	 * IMG SRC= value from MIME message
	 */
	CID(NotesConstants.HTMLAPI_REF_CID);
	
	int m_type;
	
	private static Map<Integer, ReferenceType> typesByIntValue = new HashMap<>();

    static {
        for (ReferenceType currType : ReferenceType.values()) {
            typesByIntValue.put(currType.m_type, currType);
        }
    }
    
	ReferenceType(int type) {
		m_type = type;
	}
	
	public int getValue() {
		return m_type;
	}
	
	public static ReferenceType getType(int intVal) {
		ReferenceType type = typesByIntValue.get(intVal);
		if (type==null) {
			throw new IllegalArgumentException(MessageFormat.format("Unknown int value: {0}", intVal));
		}
		return type;
	}
}
