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
package com.hcl.domino.commons.dxl;

import com.hcl.domino.misc.CNativeEnum;

/**
 * Represents the {@code DXL_IMPORT_PROPERTY} enum from xml.h.
 * 
 * @author Jesse Gallagher
 */
public enum DxlImportProperty implements CNativeEnum {
	/**
	 * WORD, Assign to value defined in DXLIMPORTOPTION
	 */
	ACLImportOption(1),
	/**
	 * WORD, Assign to value defined in DXLIMPORTOPTION
	 */
	DesignImportOption(2),
	/**
	 * WORD, Assign to value defined in DXLIMPORTOPTION
	 */
	DocumentsImportOption(3),
	/**
	 * BOOL, TRUE = create full text index, FALSE Do NOT create full text index
	 */
	CreateFullTextIndex(4),
	/**
	 * BOOL, TRUE = replace database properties, FALSE Do NOT replace database properties
	 */
	ReplaceDbProperties(5),
	/**
	 * Xml_Validation_Option, Values defined in Xml_Validation_Option
	 */
	InputValidationOption(6),
	/**
	 * BOOL, TRUE = skip replace/update ops if target DB and import DXL do not have same replicaid's [sic]
	 * <br>FALSE = allow replace/update ops even if target DB
	 */
	ReplicaRequiredForReplaceOrUpdate(7),
	/**
	 * BOOL, TRUE = importer exits on first fatal error
	 * <br>FALSE = importer continues even if fatal error found
	 */
	ExitOnFirstFatalError(8),
	/**
	 * WORD, Assign to value defined in DXLLOGOPTION. Specifies what to do if DXL contains an
	 * unknown element or attribute
	 */
	UnknownTokenLogOption(9),
	/**
	 * char*(i)/MEMHANDLE(o)  LMBCS string to be added as comment to top of result log
	 */
	ResultLogComment(10),
	/**
	 * MEMHANDLE, (readonly) The result log from the last import
	 */
	ResultLog(11),
	/**
	 * HANDLE, (readonly) An IDTABLE listing the notes imported by the last import operation
	 */
	ImportedNoteList(12)
	;
	
	int value;
	DxlImportProperty(int value) {
		this.value = value;
	}
	
	@Override
	public long getLongValue() {
		return value;
	}
	@Override
	public Integer getValue() {
		return value;
	}
}
