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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;

/**
 * These bit masks define the types of notes in a database. The bit masks may be or'ed together
 * to specify more than one type of note.
 */
public enum DocumentClass implements INumberEnum<Short> {
	
	/** old name for document note */
	DATA((short) 0x0001),

	/** document note */
	DOCUMENT((short) 0x0001),

	/** notefile info (help-about) note */
	INFO((short) 0x0002),
	
	/** form note */
	FORM((short) 0x0004),

	/** view note */
	VIEW((short) 0x0008),
	
	/** icon note */
	ICON((short) 0x0010),
	
	/** design note collection */
	DESIGNCOLLECTION((short) 0x0020),
	
	/** acl note */
	ACL((short) 0x0040),

	/** Notes product help index note */
	HELP_INDEX((short) 0x0080),

	/** designer's help note */
	HELP((short) 0x0100),
	
	/** filter note */
	FILTER((short) 0x0200),
	
	/** field note */
	FIELD((short) 0x0400),
	
	/** replication formula */
	REPLFORMULA((short) 0x0800),

	/** Private design note, use $PrivateDesign view to locate/classify */
	PRIVATE((short) 0x1000),

	/** MODIFIER - default version of each */
	DEFAULT((short) (0x8000 & 0xffff)),
	
	/** marker included in deletion stubs found in a query result*/
	NOTIFYDELETION((short) (0x8000 & 0xffff)),
	
	/** all note types */
	ALL((short) 0x7fff),
	
	/** all non-data notes */
	ALLNONDATA((short) 0x7ffe),
	
	/** no notes */
	NONE((short) 0x0000),
	
	/** Define symbol for those note classes that allow only one such in a file */
	SINGLE_INSTANCE((short) (
			0x0020 /* DESIGN */ |
			0x0040 /* ACL */ |
			0x0002 /* INFO */ |
			0x0010 /* ICON */ |
			0x0080 /* HELP_INDEX */
			));

	private short m_val;
	private static Map<Short,DocumentClass> classesByValue = new HashMap<>();
	static {
		for (DocumentClass currClass : values()) {
			classesByValue.put(currClass.getValue(), currClass);
		}
	}
	
	public static DocumentClass toNoteClass(int val) {
		return classesByValue.get((short)val);
	}
	
	DocumentClass(short val) {
		m_val = val;
	}

	@Override
	public Short getValue() {
		return m_val;
	}

	@Override
	public long getLongValue() {
		return m_val & 0xffff;
	}
	
	public static boolean isDesignElement(Set<DocumentClass> docClass) {
		return docClass.contains(DocumentClass.INFO) ||
				docClass.contains(DocumentClass.FORM) ||
				docClass.contains(DocumentClass.VIEW) ||
				docClass.contains(DocumentClass.ICON) ||
				docClass.contains(DocumentClass.DESIGNCOLLECTION) ||
				docClass.contains(DocumentClass.HELP_INDEX) ||
				docClass.contains(DocumentClass.HELP) ||
				docClass.contains(DocumentClass.FILTER) ||
				docClass.contains(DocumentClass.FIELD);
	}
}
