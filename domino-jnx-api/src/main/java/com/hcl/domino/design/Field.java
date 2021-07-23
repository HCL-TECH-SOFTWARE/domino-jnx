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
package com.hcl.domino.design;

public interface Field extends DesignElement {
	public enum Type {
		TEXT, DATETIME, NUMBER, DIALOGLIST, CHECKBOX, RADIOBUTTON, COMBOBOX, RICHTEXT, AUTHORS, NAMES, READERS, PASSWORD, FORMULA, TIMEZONE
	}

	public enum Kind {
		COMPUTED, COMPUTEDFORDISPLAY, COMPUTEDWHENCOMPOSED, EDITABLE
	}

	Type getFieldType();

	Field setFieldType(Type fieldType);

	Kind getKind();

	Field setKind(Kind kind);

	String getName();

	Field setName(String name);

	boolean isAllowMultiValues();

	Field setAllowMultiValues(boolean allowMultiValues);

	boolean isProtected();

	Field setProtected(boolean _protected);

	boolean isSign();

	Field setSign(boolean sign);

	boolean isSeal();

	Field setSeal(boolean seal);

	String getDefaultValueFormula();

	Field setDefaultValueFormula(String defaultValueFormula);
}