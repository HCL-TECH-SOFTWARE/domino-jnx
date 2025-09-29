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
import java.util.stream.Collectors;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.format.FieldListDelimiter;
import com.hcl.domino.design.format.FieldListDisplayDelimiter;
import com.hcl.domino.richtext.FormField;
import com.hcl.domino.richtext.records.CDDataFlags;
import com.hcl.domino.richtext.records.CDExtField;
import com.hcl.domino.richtext.records.CDIDName;
import com.hcl.domino.richtext.records.ICDField;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.TFMT;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class FormFieldImpl implements FormField {
  private final Collection<RichTextRecord<?>> structs;
  private final ICDField cdField;

  public FormFieldImpl(final Collection<RichTextRecord<?>> structs) {
    this.structs = new ArrayList<>(Objects.requireNonNull(structs, "structs cannot be null"));
    this.cdField = this.structs.stream()
        .filter(s -> s instanceof ICDField)
        .map(ICDField.class::cast)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to find field description in field structs"));
  }

  @Override
  public Optional<ItemDataType> getDataType() {
    return this.cdField.getFieldType();
  }

  @Override
  public Optional<String> getDefaultValueFormula() {
    return Optional.ofNullable(this.cdField.getDefaultValueFormula());
  }

  @Override
  public String getDescription() {
    return this.cdField.getDescription();
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
    return Optional.ofNullable(this.cdField.getInputTranslationFormula());
  }

  @Override
  public Optional<String> getInputValidityCheckFormula() {
    return Optional.ofNullable(this.cdField.getInputValidationFormula());
  }

  @Override
  public Optional<String> getKeywordFormula() {
    return this.cdField.getTextValueFormula();
  }

  @Override
  public FieldListDisplayDelimiter getListDispayDelimiter() {
    return this.cdField.getListDisplayDelimiter();
  }

  @Override
  public Set<FieldListDelimiter> getListInputDelimiters() {
    return this.cdField.getListDelimiters();
  }

  @Override
  public String getName() {
    return this.cdField.getName();
  }

  @Override
  public Optional<List<String>> getTextListValues() {
    return this.cdField.getTextValues();
  }
  
  @Override
  public Kind getKind() {
    Set<ICDField.Flag> flags = this.cdField.getFlags();
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
  }
  
  @Override
  public Type getDisplayType() {
    Optional<ItemDataType> dataType = getDataType();
    if(dataType.isPresent()) {
      switch(dataType.get()) {
      case TYPE_TIME:
      case TYPE_TIME_RANGE:
        return Type.DATETIME;
      case TYPE_NUMBER:
      case TYPE_NUMBER_RANGE:
        return Type.NUMBER;
      case TYPE_COMPOSITE:
        // In this case, look for record of type 62 for Rich Text Lite
        List<CDDataFlags> dataFlags = this.structs.stream()
          .filter(s -> s instanceof CDDataFlags)
          .map(CDDataFlags.class::cast)
          .collect(Collectors.toList());
        for(CDDataFlags dataFlag : dataFlags) {
          Optional<CDDataFlags.ElementType> elemType = dataFlag.getElementType();
          if(elemType.isPresent() && elemType.get().equals(CDDataFlags.ElementType.FIELDLIMIT)) {
            return Type.RICHTEXTLITE;
          }
        }
        return Type.RICHTEXT;
      case TYPE_FORMULA:
        return Type.FORMULA;
      case TYPE_USERID:
        return Type.AUTHORS;
      default:
        // Continue below for text UI types
        break;
      }
    }
    
    Set<ICDField.Flag> flags = this.cdField.getFlags();
    Optional<CDExtField> extField = this.structs.stream()
      .filter(s -> s instanceof CDExtField)
      .findFirst()
      .map(CDExtField.class::cast);
      
    if(flags.contains(ICDField.Flag.KEYWORDS)) {
      if(flags.contains(ICDField.Flag.KEYWORDS_UI_CHECKBOX)) {
        return Type.CHECKBOX;
      } else if(flags.contains(ICDField.Flag.KEYWORDS_UI_RADIOBUTTON)) {
        return Type.RADIOBUTTON;
      }
      
      if(extField.isPresent()) {
        Set<CDExtField.Flag> extFlags = extField.get().getFlags1();
        if(extFlags.contains(CDExtField.Flag.KEYWORDS_UI_COMBO)) {
          return Type.COMBOBOX;
        } else if(extFlags.contains(CDExtField.Flag.KEYWORDS_UI_LIST)) {
          return Type.LISTBOX;
        }
      }
      
      return Type.DIALOGLIST;
    }
    
    if(flags.contains(ICDField.Flag.READERS)) {
      return Type.READERS;
    } else if(flags.contains(ICDField.Flag.READWRITERS)) {
      return Type.AUTHORS;
    } else if(flags.contains(ICDField.Flag.NAMES)) {
      return Type.NAMES;
    }
    
    if(extField.isPresent()) {
      Set<CDExtField.Flag2> extFlags2 = extField.get().getFlags2();
      if(extFlags2.contains(CDExtField.Flag2.PASSWORD)) {
        return Type.PASSWORD;
      } else if(extFlags2.contains(CDExtField.Flag2.TIMEZONE)) {
        return Type.TIMEZONE;
      } else if(extFlags2.contains(CDExtField.Flag2.COLOR)) {
        return Type.COLOR;
      }
    }
    
    return Type.TEXT;
  }
  
  @Override
  public TFMT getTimeFormat() {
    return this.cdField.getTimeFormat();
  }
}
