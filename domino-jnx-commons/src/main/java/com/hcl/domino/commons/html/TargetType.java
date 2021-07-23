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

/**
 * This can be used to determine the URL address type, some operation on the generated HTML
 * text should be done according to the URL type. For more information, please refer the
 * samples released with the C API toolkit
 * 
 * @author Karsten Lehmann
 */
public enum TargetType {
	NONE(NotesConstants.UAT_None),
	SERVER(NotesConstants.UAT_Server),
	DATABASE(NotesConstants.UAT_Database),
	VIEW(NotesConstants.UAT_View),
	FORM(NotesConstants.UAT_Form),
	NAVIGATOR(NotesConstants.UAT_Navigator),
	AGENT(NotesConstants.UAT_Agent),
	DOCUMENT(NotesConstants.UAT_Document),
	/** internal filename of attachment */
	FILENAME(NotesConstants.UAT_Filename),
	/** external filename of attachment if different */
	ACTUALFILENAME(NotesConstants.UAT_ActualFilename),
	FIELD(NotesConstants.UAT_Field),
	FIELDOFFSET(NotesConstants.UAT_FieldOffset),
	FIELDSUBOFFSET(NotesConstants.UAT_FieldSuboffset),
	PAGE(NotesConstants.UAT_Page),
	FRAMESET(NotesConstants.UAT_FrameSet),
	IMAGERESOURCE(NotesConstants.UAT_ImageResource),
	CSSRESOURCE(NotesConstants.UAT_CssResource),
	JAVASCRIPTLIB(NotesConstants.UAT_JavascriptLib),
	FILERESOURCE(NotesConstants.UAT_FileResource),
	ABOUT(NotesConstants.UAT_About),
	HELP(NotesConstants.UAT_Help),
	ICON(NotesConstants.UAT_Icon),
	SEARCHFORM(NotesConstants.UAT_SearchForm),
	SEARCHSITEFORM(NotesConstants.UAT_SearchSiteForm),
	OUTLINE(NotesConstants.UAT_Outline);

	int m_type;

	private static Map<Integer, TargetType> typesByIntValue = new HashMap<>();

	static {
		for (TargetType currType : TargetType.values()) {
			typesByIntValue.put(currType.m_type, currType);
		}
	}

	TargetType(int type) {
		m_type = type;
	}

	public int getValue() {
		return m_type;
	}

	public static TargetType getType(int intVal) {
		TargetType type = typesByIntValue.get(intVal);
		if (type==null) {
			throw new IllegalArgumentException(MessageFormat.format("Unknown int value: {0}", intVal));
		}
		return type;
	}
};