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
package com.hcl.domino.design.simplesearch;

/**
 * Represents a search for documents by a field value.
 * 
 * <p>Terms of this type contain values when searching against text
 * items, but the specialized types {@link ByDateFieldTerm} and
 * {@link ByNumberFieldTerm} contain additional rules for working
 * against date and number items, respectively.</p>
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ByFieldTerm extends SimpleSearchTerm {
  enum TextRule {
    /**
     * Indicates that a text item contains the associated text
     */
    CONTAINS,
    /**
     * Indicates that a text item does not contain the associated
     * text
     */
    DOES_NOT_CONTAIN
  }
  
  /**
   * Determines the field query type for this term when used for text
   * comparison.
   * 
   * @return a {@link TextRule} instance
   */
  TextRule getTextRule();
  
  /**
   * Retrieves the name of the field to query.
   * 
   * @return the name of the field to query
   */
  String getFieldName();

  /**
   * Retrieves the text value that is used when this term is applied
   * to text items.
   * 
   * @return a string field value
   */
  String getTextValue();
}
