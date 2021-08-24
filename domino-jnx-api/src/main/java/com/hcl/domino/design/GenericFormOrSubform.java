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

import java.util.List;

import com.hcl.domino.richtext.FormField;

/**
 * Represents properties common between Forms and Subforms.
 * 
 * @param <T> the more-specific interface
 */
public interface GenericFormOrSubform<T extends GenericFormOrSubform<T>> extends GenericPageElement.ScriptablePageElement<T> {

  T addField();

  List<String> getExplicitSubformRecursive();

  List<FormField> getFields();

  List<SubformReference> getSubforms();

  T removeField();

  void swapFields(final int indexA, final int indexB);
  
  /**
   * Determines whether fields on this form or subform should be included in the database's field index.
   * 
   * @return {@code true} if fields on this form or subform should be added to the index;
   *         {@code false} otherwise
   * @since 1.0.33
   */
  boolean isIncludeFieldsInIndex();
}