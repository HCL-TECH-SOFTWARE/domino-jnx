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
package com.hcl.domino.commons.design;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.format.FieldListDelimiter;
import com.hcl.domino.design.format.FieldListDisplayDelimiter;
import com.hcl.domino.richtext.FormField;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDIDName;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class FormFieldImpl implements FormField {
	private final Collection<RichTextRecord<?>> structs;
	
	public FormFieldImpl(Collection<RichTextRecord<?>> structs) {
		this.structs = new ArrayList<>(Objects.requireNonNull(structs, "structs cannot be null"));
	}

	@Override
	public Optional<ItemDataType> getDataType() {
		return structs.stream()
			.filter(s -> s instanceof CDField)
			.map(CDField.class::cast)
			.findFirst()
			.map(CDField::getFieldType);
	}

	@Override
	public Optional<String> getDefaultValueFormula() {
		return structs.stream()
			.filter(s -> s instanceof CDField)
			.map(CDField.class::cast)
			.findFirst()
			.map(CDField::getDefaultValueFormula);
	}

	@Override
	public Optional<String> getInputTranslationFormula() {
		return structs.stream()
			.filter(s -> s instanceof CDField)
			.map(CDField.class::cast)
			.findFirst()
			.map(CDField::getInputTranslationFormula);
	}

	@Override
	public Optional<String> getInputValidityCheckFormula() {
		return structs.stream()
			.filter(s -> s instanceof CDField)
			.map(CDField.class::cast)
			.findFirst()
			.map(CDField::getInputValidationFormula);
	}

	@Override
	public String getName() {
		return structs.stream()
			.filter(s -> s instanceof CDField)
			.map(CDField.class::cast)
			.findFirst()
			.map(CDField::getName)
			.orElseThrow(() -> new IllegalStateException("Unable to find field name in field structs"));
	}

	@Override
	public String getDescription() {
		return structs.stream()
			.filter(s -> s instanceof CDField)
			.map(CDField.class::cast)
			.findFirst()
			.map(CDField::getDescription)
			.orElseThrow(() -> new IllegalStateException("Unable to find field description in field structs"));
	}

	@Override
	public Optional<List<String>> getTextListValues() {
		return structs.stream()
			.filter(s -> s instanceof CDField)
			.map(CDField.class::cast)
			.findFirst()
			.map(CDField::getTextValues)
			.orElseThrow(() -> new IllegalStateException("Unable to find field values in field structs"));
	}

	@Override
	public Optional<String> getKeywordFormula() {
		return structs.stream()
			.filter(s -> s instanceof CDField)
			.map(CDField.class::cast)
			.findFirst()
			.map(CDField::getTextValueFormula)
			.orElseThrow(() -> new IllegalStateException("Unable to find field values in field structs"));
	}

	@Override
	public String getHtmlId() {
		return structs.stream()
			.filter(s -> s instanceof CDIDName)
			.map(CDIDName.class::cast)
			.findFirst()
			.map(CDIDName::getID)
			.orElse(""); //$NON-NLS-1$
	}

	@Override
	public String getHtmlClassName() {
		return structs.stream()
			.filter(s -> s instanceof CDIDName)
			.map(CDIDName.class::cast)
			.findFirst()
			.map(CDIDName::getClassName)
			.orElse(""); //$NON-NLS-1$
	}

	@Override
	public String getHtmlStyle() {
		return structs.stream()
			.filter(s -> s instanceof CDIDName)
			.map(CDIDName.class::cast)
			.findFirst()
			.map(CDIDName::getStyle)
			.orElse(""); //$NON-NLS-1$
	}

	@Override
	public String getHtmlTitle() {
		return structs.stream()
			.filter(s -> s instanceof CDIDName)
			.map(CDIDName.class::cast)
			.findFirst()
			.map(CDIDName::getTitle)
			.orElse(""); //$NON-NLS-1$
	}

	@Override
	public String getHtmlExtraAttr() {
		return structs.stream()
			.filter(s -> s instanceof CDIDName)
			.map(CDIDName.class::cast)
			.findFirst()
			.map(CDIDName::getHTMLAttributes)
			.orElse(""); //$NON-NLS-1$
	}

	@Override
	public String getHtmlName() {
		return structs.stream()
			.filter(s -> s instanceof CDIDName)
			.map(CDIDName.class::cast)
			.findFirst()
			.map(CDIDName::getName)
			.orElse(""); //$NON-NLS-1$
	}

	@Override
	public FieldListDisplayDelimiter getListDispayDelimiter() {
		return structs.stream()
			.filter(s -> s instanceof CDField)
			.map(CDField.class::cast)
			.findFirst()
			.map(CDField::getListDisplayDelimiter)
			.orElseThrow(() -> new IllegalStateException("Unable to find field values in field structs"));
	}
	@Override
	public Set<FieldListDelimiter> getListInputDelimiters() {
		return structs.stream()
			.filter(s -> s instanceof CDField)
			.map(CDField.class::cast)
			.findFirst()
			.map(CDField::getListDelimiters)
			.orElseThrow(() -> new IllegalStateException("Unable to find field values in field structs"));
	}
}
