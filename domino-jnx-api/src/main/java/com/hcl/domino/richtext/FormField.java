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
package com.hcl.domino.richtext;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.format.FieldListDelimiter;
import com.hcl.domino.design.format.FieldListDisplayDelimiter;

/**
 * Information read for a field in the database design
 *
 * @author Karsten Lehmann
 */
public interface FormField {
  public enum Kind {
    COMPUTED, COMPUTEDFORDISPLAY, COMPUTEDWHENCOMPOSED, EDITABLE
  }
  
  public enum Type {
    TEXT, DATETIME, NUMBER, DIALOGLIST, CHECKBOX, RADIOBUTTON, LISTBOX, COMBOBOX,
    RICHTEXT, AUTHORS, NAMES, READERS, PASSWORD, FORMULA, TIMEZONE, RICHTEXTLITE,
    COLOR
  }

  /**
   * Returns the field data type
   *
   * @return an {@link Optional} describing the data type, e.g.
   *         {@link ItemDataType#TYPE_TEXT},
   *         or an empty one if not in {@link ItemDataType#values()} (unlikely)
   */
  Optional<ItemDataType> getDataType();

  /**
   * Returns the decompiled default value formula or an empty value if not present
   *
   * @return formula
   */
  Optional<String> getDefaultValueFormula();

  /**
   * Returns the content of "Help description" in the field properties
   *
   * @return description
   */
  String getDescription();

  /**
   * Returns the HTML class name
   *
   * @return class name or empty string
   */
  String getHtmlClassName();

  /**
   * Returns additional HTML attributes
   *
   * @return attribute string or empty string
   */
  String getHtmlExtraAttr();

  /**
   * Returns the HTML element id
   *
   * @return id or empty string
   */
  String getHtmlId();

  /**
   * Returns the HTML field name attribute
   *
   * @return name or empty string
   */
  String getHtmlName();

  /**
   * Returns the HTML style attribute string
   *
   * @return styles or empty string
   */
  String getHtmlStyle();

  /**
   * Returns the HTML element title
   *
   * @return title or empty string
   */
  String getHtmlTitle();

  /**
   * Returns the decompiled default input translation formula or an empty value if
   * not present
   *
   * @return formula
   */
  Optional<String> getInputTranslationFormula();

  /**
   * Returns the decompiled input validation formula or an empty value if not
   * present
   *
   * @return formula
   */
  Optional<String> getInputValidityCheckFormula();

  /**
   * If the field is a textlist computed by a formula, this method returns the
   * formula
   *
   * @return formula or an empty value if not set
   */
  Optional<String> getKeywordFormula();

  /**
   * Returns the delimiter used when displaying multiple values.
   *
   * @return the {@link FieldListDisplayDelimiter} for this field
   */
  FieldListDisplayDelimiter getListDispayDelimiter();

  /**
   * Returns the selected delimiters for multiple values when the user enters.
   *
   * @return a {@link Set} of {@link FieldListDelimiter} values
   */
  Set<FieldListDelimiter> getListInputDelimiters();

  /**
   * Returns the name if the field
   *
   * @return name
   */
  String getName();

  /**
   * If the field is a static textlist, this method returns the text list values
   *
   * @return an {@link Optional} describing the text list values, or an empty one
   *         if that does not apply
   */
  Optional<List<String>> getTextListValues();
  
  /**
   * Determines the editability/computed kind of the field
   * 
   * @return a {@link Kind} value for the field
   * @since 1.27.0
   */
  Kind getKind();
  
  /**
   * Determines the display type of the field.
   * 
   * @return a {@link Type} value for the field
   * @since 1.27.0
   */
  Type getDisplayType();
}
