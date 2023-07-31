/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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
import com.hcl.domino.richtext.records.CDIDName;
import com.hcl.domino.richtext.records.ICDField;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class FormFieldImpl implements FormField {
  private final Collection<RichTextRecord<?>> structs;

  public FormFieldImpl(final Collection<RichTextRecord<?>> structs) {
    this.structs = new ArrayList<>(Objects.requireNonNull(structs, "structs cannot be null"));
  }

  @Override
  public Optional<ItemDataType> getDataType() {
    return this.structs.stream()
        .filter(s -> s instanceof ICDField)
        .map(ICDField.class::cast)
        .findFirst()
        .flatMap(ICDField::getFieldType);
  }

  @Override
  public Optional<String> getDefaultValueFormula() {
    return this.structs.stream()
        .filter(s -> s instanceof ICDField)
        .map(ICDField.class::cast)
        .findFirst()
        .map(ICDField::getDefaultValueFormula);
  }

  @Override
  public String getDescription() {
    return this.structs.stream()
        .filter(s -> s instanceof ICDField)
        .map(ICDField.class::cast)
        .findFirst()
        .map(ICDField::getDescription)
        .orElseThrow(() -> new IllegalStateException("Unable to find field description in field structs"));
  }

  @Override
  public String getHtmlClassName() {
    return this.structs.stream()
        .filter(s -> s instanceof CDIDName)
        .map(CDIDName.class::cast)
        .findFirst()
        .map(CDIDName::getClassName)
        .orElse(""); //$NON-NLS-1$
  }

  @Override
  public String getHtmlExtraAttr() {
    return this.structs.stream()
        .filter(s -> s instanceof CDIDName)
        .map(CDIDName.class::cast)
        .findFirst()
        .map(CDIDName::getHTMLAttributes)
        .orElse(""); //$NON-NLS-1$
  }

  @Override
  public String getHtmlId() {
    return this.structs.stream()
        .filter(s -> s instanceof CDIDName)
        .map(CDIDName.class::cast)
        .findFirst()
        .map(CDIDName::getID)
        .orElse(""); //$NON-NLS-1$
  }

  @Override
  public String getHtmlName() {
    return this.structs.stream()
        .filter(s -> s instanceof CDIDName)
        .map(CDIDName.class::cast)
        .findFirst()
        .map(CDIDName::getName)
        .orElse(""); //$NON-NLS-1$
  }

  @Override
  public String getHtmlStyle() {
    return this.structs.stream()
        .filter(s -> s instanceof CDIDName)
        .map(CDIDName.class::cast)
        .findFirst()
        .map(CDIDName::getStyle)
        .orElse(""); //$NON-NLS-1$
  }

  @Override
  public String getHtmlTitle() {
    return this.structs.stream()
        .filter(s -> s instanceof CDIDName)
        .map(CDIDName.class::cast)
        .findFirst()
        .map(CDIDName::getTitle)
        .orElse(""); //$NON-NLS-1$
  }

  @Override
  public Optional<String> getInputTranslationFormula() {
    return this.structs.stream()
        .filter(s -> s instanceof ICDField)
        .map(ICDField.class::cast)
        .findFirst()
        .map(ICDField::getInputTranslationFormula);
  }

  @Override
  public Optional<String> getInputValidityCheckFormula() {
    return this.structs.stream()
        .filter(s -> s instanceof ICDField)
        .map(ICDField.class::cast)
        .findFirst()
        .map(ICDField::getInputValidationFormula);
  }

  @Override
  public Optional<String> getKeywordFormula() {
    return this.structs.stream()
        .filter(s -> s instanceof ICDField)
        .map(ICDField.class::cast)
        .findFirst()
        .map(ICDField::getTextValueFormula)
        .orElseThrow(() -> new IllegalStateException("Unable to find field values in field structs"));
  }

  @Override
  public FieldListDisplayDelimiter getListDispayDelimiter() {
    return this.structs.stream()
        .filter(s -> s instanceof ICDField)
        .map(ICDField.class::cast)
        .findFirst()
        .map(ICDField::getListDisplayDelimiter)
        .orElseThrow(() -> new IllegalStateException("Unable to find field values in field structs"));
  }

  @Override
  public Set<FieldListDelimiter> getListInputDelimiters() {
    return this.structs.stream()
        .filter(s -> s instanceof ICDField)
        .map(ICDField.class::cast)
        .findFirst()
        .map(ICDField::getListDelimiters)
        .orElseThrow(() -> new IllegalStateException("Unable to find field delimiters in field structs"));
  }

  @Override
  public String getName() {
    return this.structs.stream()
        .filter(s -> s instanceof ICDField)
        .map(ICDField.class::cast)
        .findFirst()
        .map(ICDField::getName)
        .orElseThrow(() -> new IllegalStateException("Unable to find field name in field structs"));
  }

  @Override
  public Optional<List<String>> getTextListValues() {
    return this.structs.stream()
        .filter(s -> s instanceof ICDField)
        .map(ICDField.class::cast)
        .findFirst()
        .map(ICDField::getTextValues)
        .orElseThrow(() -> new IllegalStateException("Unable to find field values in field structs"));
  }
  
  @Override
  public Kind getKind() {
    return this.structs.stream()
        .filter(s -> s instanceof ICDField)
        .map(ICDField.class::cast)
        .findFirst()
        .map(ICDField::getFlags)
        .map(flags -> {
          if(flags.contains(ICDField.Flag.EDITABLE)) {
            return Kind.EDITABLE;
          }
          
          if(flags.contains(ICDField.Flag.V3FAB)) {
            if(flags.contains(ICDField.Flag.COMPUTED)) {
              if(flags.contains(ICDField.Flag.STOREDV)) {
                return Kind.COMPUTED;
              }
              return Kind.COMPUTEDFORDISPLAY;
            }
            return Kind.COMPUTEDWHENCOMPOSED;
          }
          return Kind.EDITABLE;
        })
        .orElseThrow(() -> new IllegalStateException("Unable to find kind in field structs"));
  }
}
