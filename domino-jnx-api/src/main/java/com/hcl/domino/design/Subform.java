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

/**
 * Access to a database design. Search for design, database as constructor
 * parameter
 */
public interface Subform extends GenericFormOrSubform<Subform> {
  /**
   * Determines whether the subform should be included in the "Insert
   * Subform..." dialog in the design UI.
   * 
   * @return {@code true} if the subform should be included in the
   *         "Insert Subform..." list; {@code false} otherwise
   * @since 1.0.33
   */
  boolean isIncludeInInsertSubformDialog();
  
  /**
   * Determines whether the subform should be included in the "New
   * Form..." dialog in the UI.
   * 
   * @return {@code true} if the subform should be included in the "New
   *         Form..." list; {@code false} otherwise
   * @since 1.0.33
   */
  boolean isIncludeInNewFormDialog();
}